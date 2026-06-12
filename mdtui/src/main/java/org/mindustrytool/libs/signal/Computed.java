package org.mindustrytool.libs.signal;

import arc.func.Prov;

public final class Computed<T> {
    private final Signal<T> signal;
    private final Reaction reaction;

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

    public static <T> Computed<T> of(Prov<T> action) {
        return new Computed<>(action);
    }


    public T get() {
        return signal.get();
    }

    public void dispose() {
        reaction.dispose();
    }
}
