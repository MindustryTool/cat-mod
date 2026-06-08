package org.mindustrytool.signal;

import arc.func.Prov;
import arc.util.Log;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Computed<T> extends ReadOnlySignal<T> implements Subscriber {

    private final Prov<T> computeFn;
    private boolean dirty = true;
    private boolean computing;
    private final Set<ReadOnlySignal<?>> dependencies = new HashSet<>();

    Computed(Prov<T> fn) {
        super(null);
        this.computeFn = fn;
        recompute();
    }

    @Override
    public T get() {
        if (dirty) recompute();
        trackRead();
        return value;
    }

    @Override
    public void markDirty() {
        if (dirty) return;
        dirty = true;
        for (var sub : Set.copyOf(subscribers)) sub.markDirty();
    }

    public void dispose() {
        for (var dep : dependencies) dep.subscribers.remove(this);
        dependencies.clear();
        subscribers.clear();
        listeners.clear();
    }

    void addDependency(ReadOnlySignal<?> dep) {
        dependencies.add(dep);
        dep.subscribers.add(this);
    }

    private void recompute() {
        if (computing) { Log.err("Computed cycle detected"); return; }

        var oldDeps = Set.copyOf(dependencies);
        for (var dep : oldDeps) dep.subscribers.remove(this);
        dependencies.clear();

        try {
            ReactiveContext.push(this);
            computing = true;
            T newVal = computeFn.get();
            if (!Objects.equals(value, newVal)) {
                notifyValue(newVal);
            }
            dirty = false;
        } catch (Exception e) {
            Log.err("Computed evaluate failed", e);
            for (var dep : oldDeps) {
                dep.subscribers.add(this);
                dependencies.add(dep);
            }
            dirty = true;
        } finally {
            computing = false;
            ReactiveContext.pop();
        }
    }
}
