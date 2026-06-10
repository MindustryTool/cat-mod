package org.mindustrytool.libs.signal;

/**
 * A reactive side-effect. The given {@code action} runs immediately on creation,
 * automatically tracking any {@link Signal} reads inside it. When those signals
 * change, the effect re-runs.
 * <p>
 * Effects are commonly used for UI updates, logging, or any side-effect that
 * should react to signal changes.
 */
public final class Effect {
    private final Reaction reaction;

    /**
     * Creates an effect that runs the given action immediately and re-runs
     * whenever its tracked signal dependencies change.
     */
    public Effect(Runnable action) {
        this.reaction = new Reaction() {
            @Override
            protected void execute() {
                action.run();
            }
        };

        this.reaction.run();
    }

    /**
     * Manually triggers the effect to re-run.
     */
    public void run() {
        reaction.run();
    }

    /**
     * Disposes the effect, unsubscribing from all tracked signals.
     * No further re-runs will occur after disposal.
     */
    public void dispose() {
        reaction.dispose();
    }
}
