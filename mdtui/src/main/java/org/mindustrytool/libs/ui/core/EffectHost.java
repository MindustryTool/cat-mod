package org.mindustrytool.libs.ui.core;

import arc.struct.Seq;

import org.mindustrytool.libs.signal.Effect;

public final class EffectHost {
    private final Seq<Effect> tracked = new Seq<>();

    public Effect add(Runnable body) {
        var effect = Effect.of(body);
        tracked.add(effect);
        return effect;
    }

    public void disposeAll() {
        tracked.each(Effect::dispose);
        tracked.clear();
    }
}
