package com.neko.libs.state;

import arc.Core;
import arc.func.Cons;

/**
 * Persistent {@link State} backed by {@link arc.Core#settings}.
 * Any call to {@link #setValue} also persists the value to settings.
 */
public class Pref<T> extends State<T> {

    private final String   key;
    private final Cons<T>  persist;

    public Pref(String key, T initial, Cons<T> persist) {
        super(initial);
        this.key     = key;
        this.persist = persist;
    }

    public String getKey() { return key; }

    @Override
    public void setValue(T v) {
        persist.get(v);
        super.setValue(v);
    }

    // ── Factories ─────────────────────────────────────────────────────────────

    public static Pref<Boolean> boolPref(String key, boolean defaultValue) {
        return new Pref<>(key, Core.settings.getBool(key, defaultValue),
                          val -> Core.settings.put(key, val));
    }

    public static Pref<Float> floatPref(String key, float defaultValue) {
        return new Pref<>(key, Core.settings.getFloat(key, defaultValue),
                          val -> Core.settings.put(key, val));
    }

    public static Pref<Integer> intPref(String key, int defaultValue) {
        return new Pref<>(key, Core.settings.getInt(key, defaultValue),
                          val -> Core.settings.put(key, val));
    }

    public static Pref<String> stringPref(String key, String defaultValue) {
        return new Pref<>(key, Core.settings.getString(key, defaultValue),
                          val -> Core.settings.put(key, val));
    }
}