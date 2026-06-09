package org.mindustrytool.signal;

import java.util.HashSet;
import java.util.Set;

import arc.util.Log;

public abstract class Reaction {

    private final Set<Signal<?>> deps = new HashSet<>();
    private boolean dirty = true;
    private boolean disposed;
    private boolean running;

    public Reaction() {
        run();
    }

    protected abstract void execute();


    public final void run() {
        if (disposed) return;

        if (running) {
            Log.err("Reaction cycle detected in: " + getClass().getName() + " (dependencies: " + deps.size() + ")",
                new IllegalStateException("Reaction cycle"));
            return;
        }

        try {
            running = true;

            for (Signal<?> s : deps) s.subscribers.remove(this);
            deps.clear();
            ReactiveContext.push(this);

            execute();

            dirty = false;
        } finally {
            ReactiveContext.pop();
            running = false;
        }
    }

    void markDirty() {
        if (disposed || dirty) return;

        dirty = true;
        run();
    }

    void link(Signal<?> signal) {
        deps.add(signal);
    }

    public void dispose() {
        disposed = true;

        for (Signal<?> s : deps) s.subscribers.remove(this);
        deps.clear();
    }
}
