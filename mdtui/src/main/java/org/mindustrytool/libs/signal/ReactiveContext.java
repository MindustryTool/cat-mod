package org.mindustrytool.libs.signal;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Per-thread stack tracking the currently-executing {@link Reaction}.
 * <p>
 * When a {@link Signal#get()} is called during a reaction's
 * {@link Reaction#execute()}, this class links the signal to the
 * top-of-stack reaction so it receives change notifications.
 * <p>
 * This is package-private; users interact with it indirectly through
 * {@link Signal#get()}, {@link Effect}, and {@link Computed}.
 */
final class ReactiveContext {
    private static final ThreadLocal<Deque<Reaction>> STACK =
        ThreadLocal.withInitial(ArrayDeque::new);

    static void push(Reaction r) {
        STACK.get().push(r);
    }

    static void pop() {
        STACK.get().pop();
    }

    /**
     * Registers {@code signal} as a dependency of the currently-executing
     * reaction (if any). Called by {@link Signal#get()}.
     */
    static void collectSubscriber(Signal<?> signal) {
        var active = STACK.get().peek();
        if (active == null) return;

        active.linkDependency(signal);
        signal.addSubscriber(active);
    }
}
