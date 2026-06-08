package org.mindustrytool.signal;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;

import java.util.HashSet;
import java.util.Set;

public abstract class ReadOnlySignal<T> {
    protected T value;
    final Set<Subscriber> subscribers = new HashSet<>();
    final Seq<Cons<T>> listeners = new Seq<>();

    protected ReadOnlySignal(T initial) {
        this.value = initial;
    }

    public abstract T get();

    public Runnable onChange(Cons<T> listener) {
        listener.get(get());
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    final void trackRead() {
        Computed<?> active = ReactiveContext.active();
        if (active != null && active != this) {
            active.addDependency(this);
        }
    }

    final void notifyValue(T newValue) {
        value = newValue;
        for (var sub : Set.copyOf(subscribers)) sub.markDirty();
        for (var l : listeners) {
            try {
                l.get(newValue);
            } catch (Exception e) {
                Log.err("ReadOnlySignal listener failed", e);
            }
        }
    }
}
