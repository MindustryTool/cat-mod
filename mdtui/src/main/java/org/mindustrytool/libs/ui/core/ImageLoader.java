package org.mindustrytool.libs.ui.core;

import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.signal.Signal;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

@Slf4j
@UtilityClass
public class ImageLoader {

    private static final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build();

    private static final Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://example.com/")
        .client(client)
        .build();

    private static final ImageApi api = retrofit.create(ImageApi.class);

    public static Signal<ImageResource> get(String url) {
        if (url == null || url.isEmpty()) return new Signal<>(ImageResource.failed());

        var signal = new Signal<>(ImageResource.failed());

        Effect.of(() -> {
            var state = signal.get();
            if (state.state() != ImageLoadState.IDLE) return;

            new Thread(() -> {
                try {
                    Pixmap pixmap = resolvePixmap(url);
                    signal.set(pixmap != null ? ImageResource.decoded(pixmap) : ImageResource.failed());
                } catch (Exception e) {
                    log.error("Failed to download image: {}", url, e);
                    signal.set(ImageResource.failed());
                }
            }).start();
        });

        Effect.of(() -> {
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

    private static Pixmap resolvePixmap(String url) throws Exception {
        Resources.IMAGE_CACHE_DIR.mkdirs();
        var cachedFile = Resources.IMAGE_CACHE_DIR.child(url.replaceAll("[^a-zA-Z0-9._-]", "-"));

        if (cachedFile.exists()) return new Pixmap(cachedFile.readBytes());

        String downloadUrl = url.contains("api.mindustry-tool.com") && !url.contains("format=")
            ? url + (url.contains("?") ? "&format=jpeg" : "?format=jpeg")
            : url;

        Response<ResponseBody> response = api.downloadImage(downloadUrl).execute();
        if (!response.isSuccessful()) {
            log.error("Failed to load image: {} - HTTP {}", url, response.code());
            return null;
        }

        ResponseBody body = response.body();
        if (body == null) {
            log.error("Empty response body for: {}", url);
            return null;
        }

        byte[] bytes = body.bytes();
        if (bytes.length == 0) {
            log.error("Empty response bytes for: {}", url);
            return null;
        }

        cachedFile.writeBytes(bytes);
        return new Pixmap(bytes);
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
