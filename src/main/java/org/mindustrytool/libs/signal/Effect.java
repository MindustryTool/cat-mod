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

    public Effect(Runnable action, ThreadTarget target) {
        this.reaction = new Reaction(target) {

            @Override
            protected void execute() {
                action.run();
            }
        };

        this.reaction.run();
    }

    public static Effect of(Runnable action, ThreadTarget target) {
        return new Effect(action, target);
    }

    public static Effect ofMain(Runnable action) {
        return new Effect(action, ThreadTarget.MAIN);
    }

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
