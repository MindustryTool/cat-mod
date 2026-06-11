package org.mindustrytool.libs.ui.component;

import arc.struct.Seq;

import org.mindustrytool.libs.signal.Effect;

/**
 * Manages a collection of reactive {@link Effect}s for a component.
 *
 * <p>Use via composition — each component holds one instance as a field.
 *
 * <p>Two usage patterns:
 * <ul>
 *   <li>{@link #add(Runnable)} — permanent subscription, lives until {@link #disposeAll()}.</li>
 *   <li>{@link #replace(Effect, Runnable)} — slotted subscription, disposes the previous
 *       effect before creating a new one. Suitable for style/size effects that can be
 *       overwritten by the caller.</li>
 * </ul>
 */
public final class EffectHost {
    private final Seq<Effect> tracked = new Seq<>();

    /**
     * Creates a new {@link Effect} from {@code body}, tracks it, and returns it.
     */
    public Effect add(Runnable body) {
        var effect = new Effect(body);
        tracked.add(effect);
        return effect;
    }

    /**
     * Disposes the previous {@code effect} (if not null), removes it from tracking,
     * creates a new {@link Effect} from {@code body}, tracks it, and returns the new effect.
     */
    public Effect replace(Effect effect, Runnable body) {
        if (effect != null) {
            effect.dispose();
            tracked.remove(effect, true);
        }
        var newEffect = new Effect(body);
        tracked.add(newEffect);
        return newEffect;
    }

    /**
     * Disposes and removes all tracked effects.
     * Call from {@link Component#dispose()}.
     */
    public void disposeAll() {
        tracked.each(Effect::dispose);
        tracked.clear();
    }
}
