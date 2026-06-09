package org.mindustrytool.signal;

import java.util.ArrayDeque;
import java.util.Deque;

final class ReactiveContext {

    private static final Deque<Reaction> STACK = new ArrayDeque<>();

    static void push(Reaction r) {
        STACK.push(r);
    }

    static void pop() {
        STACK.pop();
    }

    static void active(Signal<?> signal) {
        Reaction active = STACK.peek();
        if (active == null) return;

        active.link(signal);
        signal.subscribers.add(active);
    }
}
