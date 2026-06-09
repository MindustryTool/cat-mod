package org.mindustrytool.signal;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class Signal<T> {

    private T value;
    final Set<Reaction> subscribers = new HashSet<>();

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
        for (Reaction r : Set.copyOf(subscribers)) r.markDirty();
    }
}

