package org.mindustrytool.libs.signal;

import arc.Core;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

public final class Signal<T> {
    private T value;
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

    public void set(T v) {
        if (Objects.equals(value, v)) return;

        if (Core.app != null && !Core.app.isOnMainThread()) {
            Core.app.post(() -> set(v));
            return;
        }

        value = v;
        notifySubscribers();
    }

    private void notifySubscribers() {
        for (var r : subscribers) if (!r.disposed) r.run();
    }
}
