package org.mindustrytool.libs.signal;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import arc.util.Log;

abstract class Reaction {
    final Set<Signal<?>> dependencies = Collections.newSetFromMap(new WeakHashMap<>());
    private boolean disposed = false;
    private boolean running = false;

    protected abstract void execute();

    final void run() {
        if (disposed) return;

        if (running) {
            Log.err("Reaction cycle detected", new IllegalStateException("Reaction cycle"));
            return;
        }

        try {
            ReactiveContext.push(this);
            running = true;

            for (var s : dependencies) s.subscribers.remove(this);
            dependencies.clear();

            execute();
        } finally {
            ReactiveContext.pop();
            running = false;
        }
    }

    void link(Signal<?> signal) {
        dependencies.add(signal);
    }

    void dispose() {
        disposed = true;

        for (var s : dependencies) s.subscribers.remove(this);
        dependencies.clear();
    }
}

