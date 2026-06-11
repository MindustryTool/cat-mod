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
     * Disposes and removes all tracked effects.
     * Call from {@link Component#dispose()}.
     */
    public void disposeAll() {
        tracked.each(Effect::dispose);
        tracked.clear();
    }
}
