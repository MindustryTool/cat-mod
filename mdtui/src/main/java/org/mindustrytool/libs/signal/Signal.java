package org.mindustrytool.libs.signal;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A reactive value holder. When {@link #get()} is called inside a reactive
 * context ({@link Effect} or {@link Computed}), the calling reaction
 * automatically subscribes to this signal.
 * <p>
 * Calling {@link #set(Object)} notifies all subscribed reactions. Each
 * reaction runs on its designated {@link ThreadTarget}; reactions with no
 * target run synchronously on the calling thread.
 * <p>
 * Subscribers are held via weak references. Unreferenced reactions are
 * garbage-collected without explicit disposal.
 */
public final class Signal<T> {
    private volatile T value;
    final Object subscriberLock = new Object();
    final Set<Reaction> subscribers = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Creates a signal with the given initial value.
     *
     * @param initial the starting value (may be null)
     */
    public Signal(T initial) {
        this.value = initial;
    }

    /** Convenience factory equivalent to {@code new Signal<>(value)}. */
    public static <T> Signal<T> of(T value) {
        return new Signal<>(value);
    }

    /**
     * Returns the current value. When called inside a reactive context
     * ({@link Effect} or {@link Computed}) the calling reaction automatically
     * subscribes to future changes.
     */
    public T get() {
        ReactiveContext.collectSubscriber(this);

        return value;
    }

    /**
     * Updates the value and notifies all subscribers. No-op if the new value
     * is {@link Objects#equals equal} to the current one.
     * <p>
     * Each subscriber is dispatched to its designated {@link ThreadTarget};
     * subscribers without a target run synchronously on the calling thread.
     *
     * @param newValue the new value (may be null)
     */
    public void set(T newValue) {
        if (Objects.equals(value, newValue)) return;

        value = newValue;
        notifySubscribers();
    }

    /** Registers a reaction to be notified on value changes. */
    void addSubscriber(Reaction reaction) {
        synchronized (subscriberLock) {
            subscribers.add(reaction);
        }
    }

    /** Unregisters a reaction. Safe to call multiple times. */
    void removeSubscriber(Reaction reaction) {
        synchronized (subscriberLock) {
            subscribers.remove(reaction);
        }
    }

    private void notifySubscribers() {
        Reaction[] snapshot;
        synchronized (subscriberLock) {
            snapshot = subscribers.toArray(new Reaction[0]);
        }

        for (var r : snapshot) {
            if (r.disposed) continue;

            if (r.target != null) r.target.dispatch(r::run);
            else r.run();
        }
    }
}
