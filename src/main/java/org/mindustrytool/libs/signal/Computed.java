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

    public static <T> Computed<T> of(Prov<T> action, ThreadTarget target) {
        return new Computed<>(action, target);
    }

    public static <T> Computed<T> ofMain(Prov<T> action) {
        return new Computed<>(action, ThreadTarget.MAIN);
    }

    public static <T> Computed<T> ofIO(Prov<T> action) {
        return new Computed<>(action, ThreadTarget.IO);
    }

    public T get() {
        return signal.get();
    }

    public void dispose() {
        reaction.dispose();
    }
}
