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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import arc.func.Cons;

public class ImageLoader {
    private static final Fi cacheDir = Vars.dataDirectory.child("neko-content-caches");
    private static final Map<String, WeakReference<Texture>> cache = new ConcurrentHashMap<>();
    private static final Map<String, List<Cons<Texture>>> pending = new ConcurrentHashMap<>();

    public static void load(String url, Cons<Texture> callback) {
        if (url == null || url.isEmpty()) return;

        WeakReference<Texture> ref = cache.get(url);
        if (ref != null) {
            Texture tex = ref.get();
            if (tex != null && !tex.isDisposed()) {
                callback.get(tex);
                return;
            }
        }

        List<Cons<Texture>> cbs = pending.get(url);
        if (cbs != null) {
            cbs.add(callback);
            return;
        }

        ArrayList<Cons<Texture>> list = new ArrayList<>();
        list.add(callback);
        pending.put(url, list);

        cacheDir.mkdirs();
        Fi cachedFile = cacheDir.child(sanitize(url));

        if (cachedFile.exists()) {
            Core.app.post(() -> {
                try {
                    byte[] bytes = cachedFile.readBytes();
                    Pixmap pixmap = new Pixmap(bytes);
                    Texture texture = new Texture(pixmap);
                    texture.setFilter(TextureFilter.linear);
                    pixmap.dispose();
                    deliver(url, texture);
                } catch (Exception e) {
                    fail(url, e);
                }
            });
        } else {
            Http.get(url + "?format=jpeg")
                .timeout(15000)
                .error(e -> fail(url, e))
                .submit(res -> {
                    byte[] bytes = res.getResult();
                    if (bytes.length == 0) { fail(url, new RuntimeException("empty response")); return; }
                    try {
                        cachedFile.writeBytes(bytes);
                    } catch (Exception e) { Log.err(url, e); }
                    Core.app.post(() -> {
                        try {
                            Pixmap pixmap = new Pixmap(bytes);
                            Texture texture = new Texture(pixmap);
                            texture.setFilter(TextureFilter.linear);
                            pixmap.dispose();
                            deliver(url, texture);
                        } catch (Exception e) {
                            fail(url, e);
                        }
                    });
                });
        }
    }

    public static Texture get(String url) {
        WeakReference<Texture> ref = cache.get(url);
        if (ref != null) {
            Texture tex = ref.get();
            if (tex != null && !tex.isDisposed()) return tex;
            cache.remove(url);
        }
        return null;
    }

    public static boolean isValidImageLink(String url) {
        return url != null && url.matches("^https?://[^?\\s]+\\.(png|jpg|jpeg)(\\?.*)?$");
    }

    public static void cancel(String url) {
        pending.remove(url);
    }

    public static void clearCache() {
        Iterator<Map.Entry<String, WeakReference<Texture>>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, WeakReference<Texture>> e = it.next();
            Texture tex = e.getValue().get();
            if (tex != null && !tex.isDisposed()) tex.dispose();
            it.remove();
        }
    }

    private static void deliver(String url, Texture texture) {
        cache.put(url, new WeakReference<>(texture));
        List<Cons<Texture>> cbs = pending.remove(url);
        if (cbs != null) {
            for (Cons<Texture> cb : cbs) {
                try { cb.get(texture); } catch (Exception e) { Log.err(url, e); }
            }
        }
    }

    private static void fail(String url, Throwable e) {
        pending.remove(url);
        if (!(e instanceof HttpStatusException se && se.status.code == 404)) {
            Log.err(url, e);
        }
    }

    private static String sanitize(String url) {
        return url.replace(":", "-").replace("/", "-").replace("?", "-").replace("&", "-");
    }
}
