package org.mindustrytool.libs.signal;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A reactive value holder. When {@link #get()} is called inside a reactive context
 * ({@link Effect} or {@link Computed}), the reaction automatically subscribes to this signal.
 * Calling {@link #set(Object)} notifies all dependent reactions to re-run.
 * <p>
 * Signals use weak references for subscribers, allowing unused reactions to be
 * garbage-collected without explicit disposal.
 */
public final class Signal<T> {
    private T value;
    final Set<Reaction> subscribers = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Creates a signal with the given initial value.
     */
    public Signal(T initial) {
        this.value = initial;
    }

    /**
     * Convenience factory for creating a signal.
     */
    public static <T> Signal<T> of(T value) {
        return new Signal<>(value);
    }

    /**
     * Returns the current value. If called inside a reactive context
     * ({@link Effect} or {@link Computed}), the currently-executing reaction
     * is automatically subscribed to this signal.
     */
    public T get() {
        ReactiveContext.active(this);

        return value;
    }

    /**
     * Sets a new value. Skips if the value hasn't changed (using {@link Objects#equals}).
     * All subscribed reactions are notified and re-run on change.
     */
    public void set(T newValue) {
        if (Objects.equals(value, newValue)) return;

        value = newValue;
        for (var r : subscribers.toArray(new Reaction[0])) r.run();
    }
}
