package com.neko.libs.signal;

import arc.func.Cons;
import arc.func.Prov;
import arc.struct.Seq;
import arc.util.Log;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Signal<T> {

    protected T value;
    protected final Set<Computed<?>> subscribers = new HashSet<>();
    protected final Seq<Cons<T>> onChangeListeners = new Seq<>();


    public Signal(T initialValue) {
        this.value = initialValue;
    }

    public static <T> Signal<T> of(T value) {
        return new Signal<>(value);
    }

    public static <T> Computed<T> computed(Prov<T> fn) {
        return new Computed<>(fn);
    }

    private static <T> void fire(Cons<T> listener, T value) {
        try {
            listener.get(value);
        } catch (Exception e) {
            Log.err("Signal notification failed", e);
        }
    }


    protected void trackRead() {
        Computed<?> active = ReactiveContext.active();
        if (active != null && active != this) {
            subscribers.add(active);
            active.addDependency(this);
        }
    }


    public T get() {
        trackRead();

        return value;
    }

    public void set(T newValue) {
        if (Objects.equals(value, newValue)) return;

        this.value = newValue;

        var snapshot = new HashSet<>(subscribers);
        for (var downstream : snapshot) downstream.markDirty();
        for (var downstream : snapshot) downstream.get();
        for (var listener : onChangeListeners) fire(listener, newValue);
    }

    public Runnable onChange(Cons<T> listener) {
        fire(listener, get());
        onChangeListeners.add(listener);

        return () -> onChangeListeners.remove(listener);
    }


    public static class Computed<T> extends Signal<T> {

        private final Prov<T> computeFn;
        private boolean dirty = true;
        private boolean computing;
        private final Set<Signal<?>> dependencies = new HashSet<>();


        Computed(Prov<T> computeFn) {
            super(null);
            this.computeFn = computeFn;
            recompute();
        }

        @Override
        public T get() {
            if (dirty) recompute();
            trackRead();

            return value;
        }

        @Override
        public void set(T v) {

        }

        public void dispose() {
            for (Signal<?> dep : dependencies) dep.subscribers.remove(this);
            dependencies.clear();
            subscribers.clear();
            onChangeListeners.clear();
        }

        private EvalResult<T> evaluate() {
            if (computing) {
                Log.err("Computed cycle detected, computing=" + System.identityHashCode(this));
                return EvalResult.fail();
            }

            try {
                ReactiveContext.push(this);
                computing = true;
                return EvalResult.ok(computeFn.get());
            } catch (Exception e) {
                Log.err("Computed evaluate failed", e);
                return EvalResult.fail();
            } finally {
                computing = false;
                ReactiveContext.pop();
            }
        }

        private void recompute() {
            var oldDeps = new HashSet<>(dependencies);
            for (Signal<?> dep : oldDeps) dep.subscribers.remove(this);
            dependencies.clear();

            var result = evaluate();
            if (!result.ok) {
                Log.err("Computed recompute failed, will retry on next change");
                for (Signal<?> dep : oldDeps) dep.subscribers.add(this);
                dependencies.addAll(oldDeps);

                dirty = true;
                return;
            }

            if (Objects.equals(value, result.value)) {
                dirty = false;
            } else {
                dirty = false;
                super.set(result.value);
            }
        }

        void addDependency(Signal<?> dep) {
            dependencies.add(dep);
        }

        void markDirty() {
            if (dirty) return;
            dirty = true;
            for (Computed<?> downstream : subscribers) downstream.markDirty();
        }


        private record EvalResult<T>(T value, boolean ok) {
            static <T> EvalResult<T> ok(T value) {
                return new EvalResult<>(value, true);
            }

            static <T> EvalResult<T> fail() {
                return new EvalResult<>(null, false);
            }
        }
    }
}
