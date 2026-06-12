package org.mindustrytool.libs.signal;

/**
 * A reactive side effect. Runs the given action immediately on creation,
 * tracking any {@link Signal} reads inside it. When those signals change,
 * the effect re-runs on its designated {@link ThreadTarget}.
 * <p>
 * Usage:
 * <pre>{@code
 * Effect.ofMain(() -> {
 *     var val = counter.get();
 *     label.setText("Count: " + val);
 * });
 * }</pre>
 */
public final class Effect {
    private final Reaction reaction;

    /**
     * Creates a side-effect that runs {@code action} immediately and
     * re-runs whenever tracked signals change, dispatching to {@code target}.
     *
     * @param action the side-effect logic
     * @param target the thread to re-run on
     */
    public Effect(Runnable action, ThreadTarget target) {
        this.reaction = new Reaction(target) {

            @Override
            protected void execute() {
                action.run();
            }
        };

        this.reaction.run();
    }

    /** Factory for an effect with a custom {@link ThreadTarget}. */
    public static Effect of(Runnable action, ThreadTarget target) {
        return new Effect(action, target);
    }

    /** Factory for an effect that runs on the main thread. */
    public static Effect ofMain(Runnable action) {
        return new Effect(action, ThreadTarget.MAIN);
    }

    /** Factory for an effect that runs on the IO thread. */
    public static Effect ofIO(Runnable action) {
        return new Effect(action, ThreadTarget.IO);
    }

    /** Manually re-runs this effect. */
    public void run() {
        reaction.run();
    }

    /** Disposes the effect, unsubscribing from all tracked signals. */
    public void dispose() {
        reaction.dispose();
    }
}
