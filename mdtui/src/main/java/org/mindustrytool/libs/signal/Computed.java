package org.mindustrytool.libs.signal;

import arc.func.Prov;

/**
 * A derived reactive value. The provider function runs eagerly on creation
 * and re-runs whenever its tracked signal dependencies change. The result
 * is exposed via {@link #get()}.
 * <p>
 * Usage:
 * <pre>{@code
 * Computed<String> greeting = Computed.ofMain(() -> {
 *     var name = userName.get();
 *     return "Hello, " + name;
 * });
 * }</pre>
 */
public final class Computed<T> {
    private final Signal<T> signal;
    private final Reaction reaction;

    /**
     * Creates a computed value that derives its result from the given provider.
     * The provider runs immediately to capture its initial value and subscribes
     * to any signals read inside it. Re-computation is dispatched to
     * {@code target}.
     *
     * @param action the provider function
     * @param target the thread to re-compute on
     */
    public Computed(Prov<T> action, ThreadTarget target) {
        this.signal = new Signal<>(null);
        this.reaction = new Reaction(target) {

            @Override
            protected void execute() {
                signal.set(action.get());
            }
        };

        this.reaction.run();
    }

    /** Factory for a computed with a custom {@link ThreadTarget}. */
    public static <T> Computed<T> of(Prov<T> action, ThreadTarget target) {
        return new Computed<>(action, target);
    }

    /** Factory for a computed that re-computes on the main thread. */
    public static <T> Computed<T> ofMain(Prov<T> action) {
        return new Computed<>(action, ThreadTarget.MAIN);
    }

    /** Factory for a computed that re-computes on the IO thread. */
    public static <T> Computed<T> ofIO(Prov<T> action) {
        return new Computed<>(action, ThreadTarget.IO);
    }

    /** Returns the current derived value. */
    public T get() {
        return signal.get();
    }

    /** Disposes this computed, unsubscribing from all tracked signals. */
    public void dispose() {
        reaction.dispose();
    }
}
