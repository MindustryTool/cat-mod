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

    public static Effect of(Runnable action) {
        return new Effect(action);
    }


    public void dispose() {
        reaction.dispose();
    }
}
