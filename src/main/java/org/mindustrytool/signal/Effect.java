package org.mindustrytool.signal;

public final class Effect {

    private final Reaction reaction;

    public Effect(Runnable fn) {
        reaction = new Reaction() {

            @Override
            protected void execute() {
                fn.run();
            }
        };
    }

    public void dispose() {
        reaction.dispose();
    }
}

