package com.neko.libs.state;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;

/**
 * Reactive value holder.
 * Listeners registered via {@link #onChange} are called immediately with the
 * current value and on every subsequent {@link #setValue} call.
 * Listeners registered via {@link #subscribe} are only called on future changes.
 */
public class State<T> {

    private final Seq<Cons<T>> listeners = new Seq<>();
    private T value;

    public State(T initial) {
        this.value = initial;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T v) {
        this.value = v;
        for (var listener : listeners) {
            try {
                listener.get(v);
            } catch (Exception e) {
                Log.err("Error notifying state listener with value [" + v + "]", e);
            }
        }
    }

    /** Register a listener that fires immediately and on every change. Returns an unsubscribe handle. */
    public Runnable onChange(Cons<T> fn) {
        try {
            fn.get(value);
            listeners.add(fn);
        } catch (Exception e) {
            Log.err("Error notifying state listener with value [" + value + "]", e);
        }
        return () -> listeners.remove(fn);
    }

    /** Register a listener that fires only on future changes. Returns an unsubscribe handle. */
    public Runnable subscribe(Cons<T> fn) {
        listeners.add(fn);
        return () -> listeners.remove(fn);
    }

    public static <T> State<T> of(T initial) {
        return new State<>(initial);
    }
}