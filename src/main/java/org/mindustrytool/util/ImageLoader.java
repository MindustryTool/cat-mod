package org.mindustrytool.util;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.util.Http;
import arc.util.Http.HttpStatusException;
import arc.util.Log;
import mindustry.Vars;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import arc.func.Cons;

/**
 * ImageLoader handles asynchronous downloading, local caching, and in-memory caching
 * of textures from web URLs. It is designed to be fully thread-safe and prevent memory leaks.
 */
public class ImageLoader {
    private static final Fi cacheDirectory = Vars.dataDirectory.child("neko-content-caches");
    private static final Map<String, WeakReference<Texture>> memoryCache = new ConcurrentHashMap<>();
    private static final Map<String, CopyOnWriteArrayList<Cons<Texture>>> pendingRequests = new ConcurrentHashMap<>();

    /**
     * Loads a texture from the specified URL asynchronously.
     * If cached, the callback runs immediately.
     *
     * @param url      the image URL to load
     * @param callback the action callback receiving the loaded Texture
     */
    public static void load(String url, Cons<Texture> callback) {
        if (url == null || url.isEmpty() || callback == null) {
            return;
        }

        WeakReference<Texture> ref = memoryCache.get(url);
        if (ref != null) {
            Texture cachedTexture = ref.get();
            if (cachedTexture != null && !cachedTexture.isDisposed()) {
                callback.get(cachedTexture);
                return;
            }
            memoryCache.remove(url);
        }

        boolean[] shouldStartDownload = {false};
        pendingRequests.compute(url, (key, existingList) -> {
            if (existingList == null) {
                existingList = new CopyOnWriteArrayList<>();
                shouldStartDownload[0] = true;
            }
            existingList.add(callback);
            return existingList;
        });

        if (!shouldStartDownload[0]) {
            return;
        }

        cacheDirectory.mkdirs();
        Fi cachedFile = cacheDirectory.child(sanitize(url));

        if (cachedFile.exists()) {
            Core.app.post(() -> {
                try {
                    byte[] bytes = cachedFile.readBytes();
                    Pixmap pixmap = new Pixmap(bytes);
                    Texture texture = new Texture(pixmap);
                    texture.setFilter(TextureFilter.linear);
                    pixmap.dispose();
                    deliver(url, texture);
                } catch (Exception exception) {
                    fail(url, exception);
                }
            });
        } else {
            Http.get(url + "?format=jpeg")
                .timeout(15000)
                .error(error -> fail(url, error))
                .submit(response -> {
                    byte[] bytes = response.getResult();
                    if (bytes.length == 0) {
                        fail(url, new RuntimeException("Empty response received from image source"));
                        return;
                    }
                    try {
                        cachedFile.writeBytes(bytes);
                    } catch (Exception exception) {
                        Log.err("Failed to write cached file for: " + url, exception);
                    }
                    Core.app.post(() -> {
                        try {
                            Pixmap pixmap = new Pixmap(bytes);
                            Texture texture = new Texture(pixmap);
                            texture.setFilter(TextureFilter.linear);
                            pixmap.dispose();
                            deliver(url, texture);
                        } catch (Exception exception) {
                            fail(url, exception);
                        }
                    });
                });
        }
    }

    /**
     * Synchronously retrieves a cached texture from memory if present and active.
     *
     * @param url the image URL
     * @return the cached Texture, or null if not loaded or disposed
     */
    public static Texture get(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        WeakReference<Texture> ref = memoryCache.get(url);
        if (ref != null) {
            Texture cachedTexture = ref.get();
            if (cachedTexture != null && !cachedTexture.isDisposed()) {
                return cachedTexture;
            }
            memoryCache.remove(url);
        }
        return null;
    }

    /**
     * Validates if the given URL is a valid web image link format.
     *
     * @param url the URL to validate
     * @return true if it is a valid format
     */
    public static boolean isValidImageLink(String url) {
        return url != null && url.matches("^https?://[^?\\s]+\\.(png|jpg|jpeg)(\\?.*)?$");
    }

    /**
     * Cancels a specific pending callback for the given URL to prevent leaks.
     *
     * @param url      the request URL
     * @param callback the callback to cancel
     */
    public static void cancel(String url, Cons<Texture> callback) {
        if (url == null || url.isEmpty() || callback == null) {
            return;
        }
        pendingRequests.computeIfPresent(url, (key, list) -> {
            list.remove(callback);
            return list.isEmpty() ? null : list;
        });
    }

    /**
     * Disposes and clears all cached textures in memory.
     */
    public static void clearCache() {
        Iterator<Map.Entry<String, WeakReference<Texture>>> iterator = memoryCache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, WeakReference<Texture>> entry = iterator.next();
            Texture texture = entry.getValue().get();
            if (texture != null && !texture.isDisposed()) {
                texture.dispose();
            }
            iterator.remove();
        }
    }

    private static void deliver(String url, Texture texture) {
        memoryCache.put(url, new WeakReference<>(texture));
        List<Cons<Texture>> callbacks = pendingRequests.remove(url);
        if (callbacks != null) {
            for (Cons<Texture> callback : callbacks) {
                try {
                    callback.get(texture);
                } catch (Exception exception) {
                    Log.err("Error executing callback for url: " + url, exception);
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
            Core.app.post(() -> {
                for (Cons<Texture> callback : callbacks) {
                    try {
                        callback.get(null);
                    } catch (Exception exception) {
                        Log.err("Error executing failure callback for url: " + url, exception);
                    }
                }
            });
        }
    }

    private static String sanitize(String url) {
        return url.replace(":", "-").replace("/", "-").replace("?", "-").replace("&", "-");
    }
}
