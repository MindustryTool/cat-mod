package org.mindustrytool.util;

import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.util.Http.HttpStatusException;
import arc.util.Log;
import mindustry.Vars;
import arc.files.Fi;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import arc.func.Cons;
import org.mindustrytool.libs.signal.Signal;

/**
 * Handles caching and reactive loading of textures.
 * Fully single-threaded on the main thread. All background network callbacks
 * are encapsulated in AsyncHttp and return execution to the main thread.
 * Layout is completely flattened to avoid deeply nested callback/caching logic.
 */
public class ImageLoader {
    private static final Fi cacheDirectory = Vars.tmpDirectory.child("neko-content-caches");

    private static final Map<String, WeakReference<Texture>> memoryCache = new HashMap<>();
    private static final Map<String, List<Cons<Texture>>> pendingRequests = new HashMap<>();
    private static final Map<String, WeakReference<Signal<Texture>>> signalCache = new HashMap<>();

    /**
     * Loads a texture asynchronously and returns a reactive Signal.
     * The signal is initially set to the cached texture (or null) and automatically updates when loaded.
     */
    public static Signal<Texture> loadSignal(String url) {
        if (url == null || url.isEmpty()) {
            Log.info("[ImageLoader] loadSignal: empty/null URL");
            return Signal.of(null);
        }

        Signal<Texture> signal = getCachedSignal(url);
        if (signal != null) {
            Log.info("[ImageLoader] loadSignal: found cached signal for " + url);
            return signal;
        }

        Texture cachedTexture = get(url);
        Log.info("[ImageLoader] loadSignal: cachedTexture in memory is " + (cachedTexture != null ? "found" : "null") + " for " + url);
        Signal<Texture> newSignal = Signal.of(cachedTexture);
        signalCache.put(url, new WeakReference<>(newSignal));

        if (cachedTexture == null) {
            Log.info("[ImageLoader] loadSignal: triggering load for " + url);
            load(url, loadedTexture -> {
                Log.info("[ImageLoader] loadCallback: loadedTexture is " + (loadedTexture != null ? "found" : "null") + " for " + url);
                if (loadedTexture != null) {
                    newSignal.set(loadedTexture);
                }
            });
        }

        return newSignal;
    }

    /**
     * Loads a texture from the specified URL asynchronously.
     */
    public static void load(String url, Cons<Texture> callback) {
        if (url == null || url.isEmpty() || callback == null) {
            return;
        }

        // 1. Check in-memory cache
        Texture cached = get(url);
        if (cached != null) {
            callback.get(cached);
            return;
        }

        // 2. Queue request. If a download is already in progress, stop here.
        if (enqueueRequest(url, callback)) {
            return;
        }

        // 3. Load from disk cache or fetch from web
        Fi cachedFile = getCacheFile(url);
        if (cachedFile.exists()) {
            loadFromDisk(url, cachedFile);
        } else {
            downloadAndCache(url, cachedFile);
        }
    }

    private static Signal<Texture> getCachedSignal(String url) {
        WeakReference<Signal<Texture>> ref = signalCache.get(url);
        return ref != null ? ref.get() : null;
    }

    private static boolean enqueueRequest(String url, Cons<Texture> callback) {
        boolean isAlreadyPending = pendingRequests.containsKey(url);
        pendingRequests.computeIfAbsent(url, k -> new ArrayList<>()).add(callback);
        return isAlreadyPending;
    }

    private static Fi getCacheFile(String url) {
        cacheDirectory.mkdirs();
        return cacheDirectory.child(sanitize(url));
    }

    private static void loadFromDisk(String url, Fi cachedFile) {
        Log.info("[ImageLoader] loadFromDisk: loading " + url + " from cached file " + cachedFile.name());
        try {
            byte[] bytes = cachedFile.readBytes();
            deliverPixmapBytes(url, bytes);
        } catch (Exception e) {
            Log.err("[ImageLoader] loadFromDisk error", e);
            fail(url, e);
        }
    }

    private static void downloadAndCache(String url, Fi cachedFile) {
        String downloadUrl = url;
        if (url.contains("api.mindustry-tool.com") && !url.contains("format=")) {
            downloadUrl = url.contains("?") ? url + "&format=jpeg" : url + "?format=jpeg";
        }

        Log.info("[ImageLoader] downloadAndCache: downloading from " + downloadUrl);
        AsyncHttp.get(downloadUrl)
            .timeout(15000)
            .submitBytes(
                bytes -> {
                    Log.info("[ImageLoader] downloadAndCache: download success, bytes length = " + (bytes != null ? bytes.length : 0));
                    handleDownloadSuccess(url, cachedFile, bytes);
                },
                error -> {
                    Log.err("[ImageLoader] downloadAndCache: download error", error);
                    fail(url, error);
                }
            );
    }

    private static void handleDownloadSuccess(String url, Fi cachedFile, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            fail(url, new RuntimeException("Empty response received from image source"));
            return;
        }
        try {
            cachedFile.writeBytes(bytes);
            deliverPixmapBytes(url, bytes);
        } catch (Exception e) {
            fail(url, e);
        }
    }

    private static void deliverPixmapBytes(String url, byte[] bytes) {
        Pixmap pixmap = new Pixmap(bytes);
        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.linear);
        pixmap.dispose();
        deliver(url, texture);
    }

    /**
     * Synchronously retrieves a cached texture from memory if present and active.
     */
    public static Texture get(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        WeakReference<Texture> ref = memoryCache.get(url);
        if (ref != null) {
            Texture cached = ref.get();
            if (cached != null && !cached.isDisposed()) {
                return cached;
            }
            memoryCache.remove(url);
        }
        return null;
    }

    public static boolean isValidImageLink(String url) {
        return url != null && url.matches("^https?://[^?\\s]+\\.(png|jpg|jpeg)(\\?.*)?$");
    }

    public static void cancel(String url, Cons<Texture> callback) {
        if (url == null || url.isEmpty() || callback == null) {
            return;
        }
        List<Cons<Texture>> list = pendingRequests.get(url);
        if (list != null) {
            list.remove(callback);
            if (list.isEmpty()) {
                pendingRequests.remove(url);
            }
        }
    }

    public static void clearCache() {
        Iterator<Map.Entry<String, WeakReference<Texture>>> it = memoryCache.entrySet().iterator();
        while (it.hasNext()) {
            Texture texture = it.next().getValue().get();
            if (texture != null && !texture.isDisposed()) {
                texture.dispose();
            }
            it.remove();
        }
        signalCache.clear();
    }

    private static void deliver(String url, Texture texture) {
        memoryCache.put(url, new WeakReference<>(texture));
        List<Cons<Texture>> callbacks = pendingRequests.remove(url);
        if (callbacks != null) {
            for (Cons<Texture> callback : callbacks) {
                try {
                    callback.get(texture);
                } catch (Exception e) {
                    Log.err("Error executing callback for url: " + url, e);
                }
            }
        }
    }

    private static void fail(String url, Throwable error) {
        List<Cons<Texture>> callbacks = pendingRequests.remove(url);
        if (!(error instanceof HttpStatusException httpStatusException && httpStatusException.status.code == 404)) {
            Log.err("Failed to load image from: " + url, error);
        }
        if (callbacks != null) {
            for (Cons<Texture> callback : callbacks) {
                try {
                    callback.get(null);
                } catch (Exception e) {
                    Log.err("Error executing failure callback for url: " + url, e);
                }
            }
        }
    }

    private static String sanitize(String url) {
        return url.replace(":", "-").replace("/", "-").replace("?", "-").replace("&", "-");
    }
}
