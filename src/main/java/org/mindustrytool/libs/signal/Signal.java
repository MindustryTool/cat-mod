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

    public Signal(T initial) {
        this.value = initial;
    }

    public static <T> Signal<T> of(T value) {
        return new Signal<>(value);
    }

    public T get() {
        ReactiveContext.collectSubscriber(this);

        return value;
    }

    public void set(T newValue) {
        if (Objects.equals(value, newValue)) return;

        value = newValue;
        notifySubscribers();
    }

    void addSubscriber(Reaction reaction) {
        synchronized (subscriberLock) {
            subscribers.add(reaction);
        }
    }

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
