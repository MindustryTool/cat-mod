package org.mindustrytool.libs.ui.component;

import arc.struct.Seq;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.signal.ThreadTarget;

/**
 * Manages the lifecycle of reactive {@link Effect}s for a single component.
 * <p>
 * All effects added via {@link #add(Runnable)} are tracked and can be
 * bulk-disposed via {@link #disposeAll()} (typically called when the owning
 * component is removed from the scene).
 */
public final class EffectHost {
    private final Seq<Effect> tracked = new Seq<>();

    /** Creates and tracks a main-thread effect. */
    public Effect add(Runnable body) {
        var effect = Effect.ofMain(body);
        tracked.add(effect);

        return effect;
    }

    /** Creates and tracks an effect dispatched to the given {@link ThreadTarget}. */
    public Effect add(Runnable body, ThreadTarget target) {
        var effect = new Effect(body, target);
        tracked.add(effect);

        return effect;
    }

    /** Disposes all tracked effects and clears the list. */
    public void disposeAll() {
        tracked.each(Effect::dispose);
        tracked.clear();
    }
}
