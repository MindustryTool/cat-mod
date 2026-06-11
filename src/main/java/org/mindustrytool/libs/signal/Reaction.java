package org.mindustrytool.libs.signal;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * Package-private base class for reactive computations ({@link Effect},
 * {@link Computed}). Tracks signal dependencies and re-runs
 * {@link #execute()} when any of them change.
 * <p>
 * Auto-tracking works via {@link ReactiveContext}: before execution the
 * reaction pushes itself onto a per-thread stack. Any {@link Signal#get()}
 * call during {@link #execute()} registers the signal as a dependency.
 */
@Slf4j
abstract class Reaction {
    final Set<Signal<?>> dependencies = Collections.newSetFromMap(new WeakHashMap<>());
    volatile boolean disposed = false;
    private volatile boolean running = false;
    final ThreadTarget target;

    Reaction(ThreadTarget target) {
        this.target = target;
    }

    /** User-defined computation. Runs inside a tracking context. */
    protected abstract void execute();

    /** Runs or re-runs this reaction. Clears and rebuilds dependency graph. */
    final void run() {
        if (disposed) return;

        if (running) {
            log.error("Reaction cycle detected", new IllegalStateException("Reaction cycle"));
            return;
        }

        try {
            ReactiveContext.push(this);
            running = true;

            for (var signal : dependencies) signal.removeSubscriber(this);
            dependencies.clear();

            execute();
        } finally {
            ReactiveContext.pop();
            running = false;
        }
    }

    /** Registers a signal as a dependency of this reaction. */
    void linkDependency(Signal<?> signal) {
        dependencies.add(signal);
    }

    /** Unsubscribes from all tracked signals. Safe to call from any thread. */
    void dispose() {
        disposed = true;

        for (var signal : dependencies) signal.removeSubscriber(this);
        dependencies.clear();
    }
}
