package org.mindustrytool.libs.signal;

import arc.Core;
import arc.util.Timer;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A conditional multi-threaded signal. Subscribers register with a condition
 * (Predicate) and a target thread. When {@link #update} changes the state and
 * the condition is met, the subscriber's action is dispatched to its
 * designated thread.
 * <p>
 * Safe to call {@link #update} from any thread. Subscribers run on their
 * specified target thread without blocking the caller.
 * <p>
 * Example:
 * <pre>{@code
 * var signal = new MultithreadSignal<String>();
 * signal.subscribe("DONE"::equals, () -> ui.show("Done!"), ThreadTarget.ofMain());
 * signal.update("DONE");
 * }</pre>
 */
public final class MultithreadSignal<T> {
    private final ReentrantLock lock = new ReentrantLock();
    private final List<Entry<T>> entries = new ArrayList<>();
    private volatile T state;

    /**
     * Registers a subscriber.
     *
     * @param condition activation predicate (thread-safe, called under internal lock)
     * @param action    the action to run when the condition is met
     * @param target    target thread for dispatching the action (e.g. main, IO)
     * @return a Handle to {@link Handle#dispose} when no longer needed
     */
    public Handle subscribe(Predicate<T> condition, Runnable action, ThreadTarget target) {
        Objects.requireNonNull(condition);
        Objects.requireNonNull(action);
        Objects.requireNonNull(target);

        var handle = new Handle();
        var entry = new Entry<>(condition, action, target, handle);

        try {
            lock.lock();
            entries.add(entry);
        } finally {
            lock.unlock();
        }

        handle.disposeAction = () -> {
            try {
                lock.lock();
                entries.removeIf(e -> e.handle == handle);
            } finally {
                lock.unlock();
            }
        };

        return handle;
    }

    /**
     * Registers a subscriber that runs on the game's main thread via {@code Core.app.post}.
     * Convenience shorthand for {@code subscribe(condition, action, ThreadTarget.ofMain())}.
     */
    public Handle subscribeOnMain(Predicate<T> condition, Runnable action) {
        return subscribe(condition, action, ThreadTarget.ofMain());
    }

    /**
     * Registers a subscriber that runs on a background arc Timer thread.
     * Suitable for I/O, network, and heavy processing.
     * Convenience shorthand for {@code subscribe(condition, action, ThreadTarget.ofIO())}.
     */
    public Handle subscribeOnIO(Predicate<T> condition, Runnable action) {
        return subscribe(condition, action, ThreadTarget.ofIO());
    }

    /**
     * Updates the state and triggers all subscribers whose condition is met.
     * Actions are dispatched asynchronously to their designated threads,
     * never blocking the calling thread.
     *
     * @param newState the new state value
     */
    public void update(T newState) {
        List<Entry<T>> copy;

        try {
            lock.lock();
            state = newState;
            copy = List.copyOf(entries);
        } finally {
            lock.unlock();
        }

        for (var e : copy) {
            if (!e.handle.disposed && e.condition.test(newState)) {
                e.target.dispatch(e.action);
            }
        }
    }

    /**
     * Registers a persistent state mutator. Like {@link #subscribe}, but
     * transforms state via {@code fn} instead of running an arbitrary action.
     * After mutation, all other subscribers whose condition is met are
     * dispatched, enabling state machine pipelines.
     * <p>
     * The mutation's own condition is NOT re-checked after the transformation
     * to prevent trivial re-entrant cycles.
     *
     * @param condition activation predicate (thread-safe, called under internal lock)
     * @param fn        state transformation to apply when condition is met
     * @param target    target thread for applying the transformation
     * @return a Handle to {@link Handle#dispose} when the mutator is no longer needed
     */
    public Handle mutate(Predicate<T> condition, java.util.function.UnaryOperator<T> fn, ThreadTarget target) {
        Objects.requireNonNull(condition);
        Objects.requireNonNull(fn);
        Objects.requireNonNull(target);

        var handle = new Handle();
        Runnable mutation = () -> {
            T current = state();
            T next = fn.apply(current);
            List<Entry<T>> copy;

            try {
                lock.lock();
                state = next;
                copy = List.copyOf(entries);
            } finally {
                lock.unlock();
            }

            for (var e : copy) {
                if (e.handle != handle && !e.handle.disposed && e.condition.test(next)) {
                    e.target.dispatch(e.action);
                }
            }
        };
        var entry = new Entry<>(condition, mutation, target, handle);

        try {
            lock.lock();
            entries.add(entry);
        } finally {
            lock.unlock();
        }

        handle.disposeAction = () -> {
            try {
                lock.lock();
                entries.removeIf(e -> e.handle == handle);
            } finally {
                lock.unlock();
            }
        };

        return handle;
    }

    /**
     * Registers a persistent state mutator that runs on the main thread.
     * Convenience shorthand for {@code mutate(condition, fn, ThreadTarget.ofMain())}.
     */
    public Handle mutateOnMain(Predicate<T> condition, java.util.function.UnaryOperator<T> fn) {
        return mutate(condition, fn, ThreadTarget.ofMain());
    }

    /**
     * Registers a persistent state mutator that runs on the IO thread.
     * Convenience shorthand for {@code mutate(condition, fn, ThreadTarget.ofIO())}.
     */
    public Handle mutateOnIO(Predicate<T> condition, java.util.function.UnaryOperator<T> fn) {
        return mutate(condition, fn, ThreadTarget.ofIO());
    }

    /**
     * Returns the current state. Volatile read — safe without a lock.
     */
    public T state() {
        return state;
    }

    /**
     * Controls a subscription. Call {@link #dispose} to unsubscribe.
     */
    public static final class Handle {

        private volatile @Getter boolean disposed = false;
        private Runnable disposeAction;

        private Handle() {

        }

        /**
         * Unsubscribes the subscriber. Safe to call from any thread.
         */
        public void dispose() {
            if (disposed) return;
            disposed = true;

            if (disposeAction != null) disposeAction.run();
        }
    }

    private record Entry<T>(Predicate<? super T> condition, Runnable action, ThreadTarget target, Handle handle) {

    }

    /**
     * Specifies the target thread for dispatching a subscriber action.
     * <p>
     * Built-in shorthands:
     * <ul>
     *   <li>{@link #ofMain()} — dispatches to the game's main thread via {@code Core.app.post}</li>
     *   <li>{@link #ofIO()} — dispatches to a background arc Timer thread</li>
     *   <li>{@link #of(Consumer)} — custom dispatcher</li>
     *   <li>{@link #of(Executor)} — wraps an {@link Executor}</li>
     * </ul>
     */
    public static final class ThreadTarget {
        private final Consumer<Runnable> dispatcher;

        private ThreadTarget(Consumer<Runnable> dispatcher) {
            this.dispatcher = dispatcher;
        }

        /**
         * Creates a ThreadTarget from a custom dispatcher.
         *
         * @param dispatcher accepts a Runnable and runs it on the desired thread
         */
        public static ThreadTarget of(Consumer<Runnable> dispatcher) {
            return new ThreadTarget(dispatcher);
        }

        /**
         * Creates a ThreadTarget from an {@link Executor}.
         *
         * @param executor the executor to dispatch actions with
         */
        public static ThreadTarget of(Executor executor) {
            return new ThreadTarget(executor::execute);
        }

        /**
         * Shorthand that dispatches to the game's main thread via {@code Core.app.post}.
         */
        public static ThreadTarget ofMain() {
            return of((Consumer<Runnable>) Core.app::post);
        }

        /**
         * Shorthand that dispatches to a background arc Timer thread.
         * Suitable for I/O, network, and heavy processing that must not block
         * the main thread.
         */
        public static ThreadTarget ofIO() {
            return of((Consumer<Runnable>) r -> Timer.schedule(r, 0));
        }

        void dispatch(Runnable action) {
            dispatcher.accept(action);
        }
    }
}
