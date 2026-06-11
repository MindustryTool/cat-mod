package org.mindustrytool.libs.ui.component;

import arc.struct.Seq;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.signal.ThreadTarget;

public final class EffectHost {
    private final Seq<Effect> tracked = new Seq<>();

    public Effect add(Runnable body) {
        var effect = Effect.ofMain(body);
        tracked.add(effect);

        return effect;
    }

    public Effect add(Runnable body, ThreadTarget target) {
        var effect = new Effect(body, target);
        tracked.add(effect);

        return effect;
    }

    public void disposeAll() {
        tracked.each(Effect::dispose);
        tracked.clear();
    }
}
