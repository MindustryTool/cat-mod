package org.mindustrytool.libs.signal;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Thread-local stack tracking the currently-executing {@link Reaction}.
 * Enables automatic dependency discovery: when a {@link Signal#get()} is called
 * during a reaction's execution, {@link #active(Signal)} links the signal to
 * the top-of-stack reaction.
 * <p>
 * This is package-private; users interact with it indirectly through
 * {@link Signal#get()}, {@link Effect}, and {@link Computed}.
 */
final class ReactiveContext {
    private static final Deque<Reaction> STACK = new ArrayDeque<>();

    static void push(Reaction r) {
        STACK.push(r);
    }

    static void pop() {
        STACK.pop();
    }

    /**
     * Called by {@link Signal#get()} to register the signal as a dependency
     * of the currently-active reaction, if any.
     */
    static void active(Signal<?> signal) {
        var active = STACK.peek();
        if (active == null) return;

        active.link(signal);
        signal.subscribers.add(active);
    }
}
