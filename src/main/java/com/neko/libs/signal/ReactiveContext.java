package com.neko.libs.signal;

import java.util.ArrayDeque;
import java.util.Deque;

final class ReactiveContext {

    private static final Deque<Signal.Computed<?>> stack = new ArrayDeque<>();


    static void push(Signal.Computed<?> c) {
        stack.push(c);
    }

    static void pop() {
        stack.pop();
    }

    static Signal.Computed<?> active() {
        return stack.peekFirst();
    }
}
