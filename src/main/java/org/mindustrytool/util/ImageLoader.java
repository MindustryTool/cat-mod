package org.mindustrytool.util;

import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.util.Http;
import arc.util.Http.HttpMethod;
import arc.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import lombok.experimental.UtilityClass;
import org.mindustrytool.libs.signal.MultithreadSignal;

/**
 * Reactive image loader backed by a state-machine signal pipeline.
 * <p>
 * Each URL gets a {@link MultithreadSignal} that drives the image through
 * {@link ImageLoadState IDLE → LOADING → DECODED → LOADED} on the correct
 * threads (IO for download + Pixmap, Main for VRAM upload) and caches the
 * signal weakly so the same inflight URL is never loaded twice.
 * <p>
 * External consumers subscribe to the signal and react to whichever state
 * they care about — typically {@link ImageLoadState#LOADED} or
 * {@link ImageLoadState#FAILED}.
 */
@UtilityClass
public class ImageLoader {
    private static final Map<String, WeakReference<MultithreadSignal<ImageResource>>> cache = new HashMap<>();
    private static final ReentrantLock lock = new ReentrantLock();

    /**
     * Returns a reactive signal for the given image URL.
     * <p>
     * The signal drives the image through {@link ImageLoadState}:
     * <ol>
     *   <li>{@link ImageLoadState#IDLE} — initial state (kicked by this method)</li>
     *   <li>{@link ImageLoadState#LOADING} — download started (IO thread)</li>
     *   <li>{@link ImageLoadState#DECODED} — Pixmap ready (IO thread)</li>
     *   <li>{@link ImageLoadState#LOADED} — Texture uploaded to VRAM (Main thread)</li>
     *   <li>{@link ImageLoadState#FAILED} — any stage failed</li>
     * </ol>
     * The same inflight URL is cached weakly and reused.
     *
     * @param url the image URL to load
     * @return a signal whose state progresses through the image lifecycle
     */
    public static MultithreadSignal<ImageResource> get(String url) {
        if (url == null || url.isEmpty()) {
            var empty = new MultithreadSignal<ImageResource>();
            empty.update(ImageResource.failed());
            return empty;
        }

        lock.lock();
        try {
            var ref = cache.get(url);
            if (ref != null) {
                var signal = ref.get();
                if (signal != null) return signal;
            }

            var signal = new MultithreadSignal<ImageResource>();
            cache.put(url, new WeakReference<>(signal));

            // State machine: IDLE -> LOADING (IO thread)
            signal.mutateOnIO(
                r -> r.state() == ImageLoadState.IDLE,
                s -> ImageResource.loading()
            );

            // LOADING -> DECODED | FAILED (IO thread: HTTP + Pixmap)
            signal.mutateOnIO(
                r -> r.state() == ImageLoadState.LOADING,
                s -> {
                    Pixmap pixmap = resolvePixmap(url);
                    if (pixmap == null) return ImageResource.failed();
                    return ImageResource.decoded(pixmap);
                }
            );

            // DECODED -> LOADED | FAILED (Main thread: upload Pixmap to VRAM)
            signal.mutateOnMain(
                r -> r.state() == ImageLoadState.DECODED,
                s -> {
                    Pixmap pixmap = s.pixmap();
                    try {
                        Texture t = new Texture(pixmap);
                        t.setFilter(TextureFilter.linear);
                        return ImageResource.loaded(t);
                    } catch (Exception e) {
                        Log.err("Failed to create texture from Pixmap", e);
                        return ImageResource.failed();
                    } finally {
                        if (pixmap != null && !pixmap.isDisposed()) pixmap.dispose();
                    }
                }
            );

            // Kick
            signal.update(ImageResource.idle());
            return signal;
        } finally {
            lock.unlock();
        }
    }

    private static Pixmap resolvePixmap(String url) {
        Resources.IMAGE_CACHE_DIR.mkdirs();
        var cachedFile = Resources.IMAGE_CACHE_DIR.child(url.replaceAll("[:/?&]", "-"));

        if (cachedFile.exists()) {
            return new Pixmap(cachedFile.readBytes());
        }

        String downloadUrl = url.contains("api.mindustry-tool.com") && !url.contains("format=")
            ? url + (url.contains("?") ? "&format=jpeg" : "?format=jpeg")
            : url;

        var httpRef = new Object() { byte[] bytes; Throwable error; };
        Http.request(HttpMethod.GET, downloadUrl)
            .timeout(15000)
            .error(err -> httpRef.error = err)
            .block(res -> httpRef.bytes = res.getResult());

        if (httpRef.error != null) {
            Log.err("Failed to load image: " + url, httpRef.error);
            return null;
        }

        if (httpRef.bytes == null || httpRef.bytes.length == 0) {
            Log.err("Empty response bytes for: " + url);
            return null;
        }

        cachedFile.writeBytes(httpRef.bytes);
        return new Pixmap(httpRef.bytes);
    }

    /**
     * Clears the in-memory signal cache. Active signals already returned via
     * {@link #get} continue to work; new calls will start fresh.
     */
    public static void clearCache() {
        lock.lock();
        try {
            cache.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Quick validation for common image URL patterns.
     *
     * @param url the URL to check
     * @return true if the URL looks like a PNG/JPEG image link
     */
    public static boolean isValidImageLink(String url) {
        return url != null && url.matches("^https?://[^?\\s]+\\.(png|jpg|jpeg)(\\?.*)?$");
    }

    /**
     * Lifecycle states of an image resource as it moves through the
     * loading pipeline.
     */
    public enum ImageLoadState {
        IDLE,
        LOADING,
        DECODED,
        LOADED,
        FAILED
    }

    /**
     * Immutable snapshot of an image at one point in its lifecycle.
     * Only the fields relevant to the current state are populated.
     *
     * @param state   the current lifecycle state
     * @param texture the loaded texture (non-null only in {@link ImageLoadState#LOADED})
     * @param pixmap  the decoded pixmap (non-null only in {@link ImageLoadState#DECODED})
     */
    public static record ImageResource(ImageLoadState state, Texture texture, Pixmap pixmap) {
        public static ImageResource idle() {
            return new ImageResource(ImageLoadState.IDLE, null, null);
        }

        public static ImageResource loading() {
            return new ImageResource(ImageLoadState.LOADING, null, null);
        }

        public static ImageResource decoded(Pixmap p) {
            return new ImageResource(ImageLoadState.DECODED, null, p);
        }

        public static ImageResource loaded(Texture t) {
            return new ImageResource(ImageLoadState.LOADED, t, null);
        }

        public static ImageResource failed() {
            return new ImageResource(ImageLoadState.FAILED, null, null);
        }
    }
}
