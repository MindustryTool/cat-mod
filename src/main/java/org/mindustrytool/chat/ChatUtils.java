package org.mindustrytool.chat;

import arc.ApplicationListener;
import arc.Core;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.serialization.Json;
import mindustry.Vars;
import mindustry.mod.Mods.LoadedMod;
import org.mindustrytool.NekoMod;

import java.util.concurrent.ConcurrentHashMap;

public class ChatUtils {
    public static LoadedMod mod;
    private static final ConcurrentHashMap<String, TextureRegionDrawable> iconCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<TextureRegionDrawable, TextureRegionDrawable> scalableIconCache = new ConcurrentHashMap<>();
    private static final Json json = new Json();

    public static String toJson(Object object) {
        return json.toJson(object);
    }

    public static <T> T fromJson(Class<T> clazz, String jsonStr) {
        return json.fromJson(clazz, jsonStr);
    }

    @SuppressWarnings("unchecked")
    public static <T> Seq<T> fromJsonArray(Class<T> elementType, String jsonStr) {
        return json.fromJson(Seq.class, elementType, jsonStr);
    }

    public static TextureRegionDrawable scalable(TextureRegionDrawable original) {
        return scalableIconCache.computeIfAbsent(original, key -> new TextureRegionDrawable(original.getRegion()));
    }

    public static TextureRegionDrawable icons(String name) {
        if (iconCache.containsKey(name)) {
            return iconCache.get(name);
        }

        try {
            if (mod == null) {
                mod = Vars.mods.getMod(NekoMod.class);
            }
            if (mod != null && mod.root != null) {
                var texture = new TextureRegion(new Texture(mod.root.child("icons").child(name)));
                var drawable = new TextureRegionDrawable(texture);
                iconCache.put(name, drawable);
                return drawable;
            }
        } catch (Exception e) {
            Log.err("Failed to load icon " + name, e);
        }

        iconCache.put(name, mindustry.gen.Icon.book);
        return mindustry.gen.Icon.book;
    }

    public static void onAppExit(Runnable callback) {
        Core.app.addListener(new ApplicationListener() {
            @Override
            public void exit() {
                try {
                    callback.run();
                } catch (Throwable e) {
                    Log.err(e);
                }
            }
        });
    }
}
