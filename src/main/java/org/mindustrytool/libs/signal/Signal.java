package org.mindustrytool.libs.signal;

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
        ReactiveContext.active(this);

        return value;
    }

    public void set(T newValue) {
        if (Objects.equals(value, newValue)) return;

        value = newValue;
        for (var r : subscribers.toArray(new Reaction[0])) r.run();
    }
}


