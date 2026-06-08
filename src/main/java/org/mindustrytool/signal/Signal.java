package org.mindustrytool.signal;

import arc.func.Prov;

public class Signal<T> extends ReadOnlySignal<T> {

    public Signal(T initialValue) {
        super(initialValue);
    }

    public static <T> Signal<T> of(T value) {
        return new Signal<>(value);
    }

    public static <T> Computed<T> computed(Prov<T> fn) {
        return new Computed<>(fn);
    }

    @Override
    public T get() {
        trackRead();
        return value;
    }

    public void set(T newValue) {
        if (value == newValue || (value != null && value.equals(newValue))) return;
        notifyValue(newValue);
    }
}
