package org.mindustrytool.libs.signal;

import arc.func.Prov;

/**
 * A derived reactive value. The {@code action} function runs immediately on creation
 * and re-runs whenever its tracked signal dependencies change. The result is
 * exposed via {@link #get()}.
 * <p>
 * Computed values are lazy — they only recompute when their dependencies change
 * and the value is read. They cannot be set directly.
 */
public final class Computed<T> {
    private final Signal<T> signal;
    private final Reaction reaction;

    /**
     * Creates a computed value driven by the given provider function.
     * The function runs immediately to capture its initial value and subscribes
     * to any signals read inside it.
     */
    public Computed(Prov<T> action) {
        this.signal = new Signal<>(null);
        this.reaction = new Reaction() {
            @Override
            protected void execute() {
                signal.set(action.get());
            }
        };

        this.reaction.run();
    }

    /**
     * Returns the current computed value. If called inside another reactive
     * context, the outer reaction subscribes to this computed's internal signal.
     */
    public T get() {
        return signal.get();
    }

    /**
     * Disposes the computed value, unsubscribing from all tracked signals.
     */
    public void dispose() {
        reaction.dispose();
    }
}
