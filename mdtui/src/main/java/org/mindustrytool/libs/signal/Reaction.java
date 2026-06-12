package org.mindustrytool.libs.signal;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class Reaction {
    final Set<Signal<?>> dependencies = Collections.newSetFromMap(new WeakHashMap<>());
    volatile boolean disposed = false;
    private volatile boolean running = false;

    protected abstract void execute();

    final void run() {
        if (disposed) return;

        if (running) {
            log.error("Reaction cycle detected", new IllegalStateException("Reaction cycle"));
            return;
        }

        try {
            ReactiveContext.push(this);
            running = true;

            for (var signal : dependencies) signal.subscribers.remove(this);
            dependencies.clear();

            execute();
        } finally {
            ReactiveContext.pop();
            running = false;
        }
    }

    void linkDependency(Signal<?> signal) {
        dependencies.add(signal);
    }

    void dispose() {
        disposed = true;

        for (var signal : dependencies) signal.subscribers.remove(this);
        dependencies.clear();
    }
}
