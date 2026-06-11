package org.mindustrytool.libs.signal;

import arc.Core;
import arc.util.Timer;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Specifies the target thread for dispatching a reaction's execution.
 * <p>
 * Built-in shorthands:
 * <ul>
 *   <li>{@link #MAIN} — dispatches to the game's main thread via
 *       {@code Core.app.post}, or runs synchronously if already on main</li>
 *   <li>{@link #IO} — dispatches to a background arc Timer thread</li>
 * </ul>
 * Custom dispatchers can be created via {@link #of(Consumer)} or
 * {@link #of(Executor)}.
 */
public final class ThreadTarget {
    private final Consumer<Runnable> dispatcher;

    /** Dispatches to the main thread. Runs synchronously if already on main. */
    public static final ThreadTarget MAIN = new ThreadTarget(r -> {
        if (Core.app.isOnMainThread()) r.run();
        else Core.app.post(r);
    });

    /** Dispatches to a background arc Timer thread. */
    public static final ThreadTarget IO = new ThreadTarget(r ->
        Timer.schedule(r, 0));

    private ThreadTarget(Consumer<Runnable> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static ThreadTarget of(Consumer<Runnable> dispatcher) {
        return new ThreadTarget(dispatcher);
    }

    public static ThreadTarget of(Executor executor) {
        return new ThreadTarget(executor::execute);
    }

    void dispatch(Runnable action) {
        dispatcher.accept(action);
    }
}
