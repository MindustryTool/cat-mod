package org.mindustrytool.libs.signal;

public final class Effect {
    private final Reaction reaction;

    public Effect(Runnable action) {
        this.reaction = new Reaction() {

            @Override
            protected void execute() {
                action.run();
            }
        };

        this.reaction.run();
    }

    public void dispose() {
        reaction.dispose();
    }
}
