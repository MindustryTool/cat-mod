package org.mindustrytool.libs.signal;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import arc.util.Log;

/**
 * Package-private base class for reactive computations ({@link Effect}, {@link Computed}).
 * A reaction tracks which signals it depends on and re-runs its {@link #execute()}
 * when any of those signals change.
 * <p>
 * Auto-tracking works via {@link ReactiveContext}: before execution, the reaction
 * pushes itself onto a thread-local stack. Any {@link Signal#get()} call detects
 * the active reaction and registers it as a subscriber.
 */
abstract class Reaction {
    final Set<Signal<?>> dependencies = Collections.newSetFromMap(new WeakHashMap<>());
    private boolean disposed = false;
    private boolean running = false;

    /**
     * The user-defined computation to run.
     */
    protected abstract void execute();

    /**
     * Runs or re-runs this reaction. Clears old dependencies first, then
     * re-executes to rebuild the dependency graph via auto-tracking.
     * Prevents re-entrant cycles.
     */
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

    /**
     * Links this reaction to the given signal as a dependency.
     * Called by {@link ReactiveContext#active} during signal reads.
     */
    void link(Signal<?> signal) {
        dependencies.add(signal);
    }

    /**
     * Disposes this reaction, removing it from all signal subscriber lists.
     */
    void dispose() {
        disposed = true;

        for (var s : dependencies) s.subscribers.remove(this);
        dependencies.clear();
    }
}
