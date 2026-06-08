package org.mindustrytool.signal;

import java.util.ArrayDeque;
import java.util.Deque;

final class ReactiveContext {

    private static final Deque<Computed<?>> STACK = new ArrayDeque<>();

    static void push(Computed<?> c) {
        STACK.push(c);
    }

    static void pop() {
        STACK.pop();
    }

    static Computed<?> active() {
        return STACK.peekFirst();
    }
}
