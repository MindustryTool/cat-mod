package org.mindustrytool.libs.ui.core;




import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.util.Http;
import arc.util.Http.HttpMethod;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.signal.Signal;
import org.mindustrytool.util.Resources;

@Slf4j
@UtilityClass
public class ImageLoader {

    public static Signal<ImageResource> get(String url) {
        if (url == null || url.isEmpty()) return new Signal<>(ImageResource.failed());

        var signal = new Signal<>(ImageResource.failed());

        Effect.ofIO(() -> {
            var state = signal.get();
            if (state.state() != ImageLoadState.IDLE) return;

            Pixmap pixmap = resolvePixmap(url);
            signal.set(pixmap != null ? ImageResource.decoded(pixmap) : ImageResource.failed());
        });

        Effect.ofMain(() -> {
            var state = signal.get();
            if (state.state() != ImageLoadState.DECODED) return;
            Pixmap pixmap = state.pixmap();
            try {
                Texture t = new Texture(pixmap);
                t.setFilter(TextureFilter.linear);
                signal.set(ImageResource.loaded(t));
            } catch (Exception e) {
                log.error("Failed to create texture from Pixmap", e);
                signal.set(ImageResource.failed());
            } finally {
                if (pixmap != null && !pixmap.isDisposed()) pixmap.dispose();
            }
        });

        signal.set(ImageResource.idle());
        return signal;
    }

    private static Pixmap resolvePixmap(String url) {
        Resources.IMAGE_CACHE_DIR.mkdirs();
        var cachedFile = Resources.IMAGE_CACHE_DIR.child(url.replaceAll("[^a-zA-Z0-9._-]", "-"));

        if (cachedFile.exists()) return new Pixmap(cachedFile.readBytes());

        String downloadUrl = url.contains("api.mindustry-tool.com") && !url.contains("format=")
            ? url + (url.contains("?") ? "&format=jpeg" : "?format=jpeg")
            : url;

        var ref = new HttpResult[]{null};
        Http.request(HttpMethod.GET, downloadUrl)
            .timeout(15000)
            .error(err -> ref[0] = new HttpResult(null, err))
            .block(res -> ref[0] = new HttpResult(res.getResult(), null));
        var httpRef = ref[0];

        if (httpRef.error() != null) {
            log.error("Failed to load image: {}", url, httpRef.error());
            return null;
        }

        if (httpRef.bytes() == null || httpRef.bytes().length == 0) {
            log.error("Empty response bytes for: {}", url);
            return null;
        }

        cachedFile.writeBytes(httpRef.bytes());
        return new Pixmap(httpRef.bytes());
    }

    private record HttpResult(byte[] bytes, Throwable error) {

    }

    public enum ImageLoadState {
        IDLE,
        DECODED,
        LOADED,
        FAILED
    }

    public record ImageResource(ImageLoadState state, Texture texture, Pixmap pixmap) {
        public static ImageResource idle() {
            return new ImageResource(ImageLoadState.IDLE, null, null);
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
