package org.mindustrytool.libs.ui.animation;

import arc.graphics.Color;
import arc.math.Mathf;

public class AnimatedValue<T> {
    private T value, from, to;
    private float elapsed, duration;
    private Ease ease;
    private boolean active;
    private final Interpolator<T> interpolator;

    public interface Interpolator<T> {
        T lerp(T a, T b, float t);
    }

    public AnimatedValue(T initial, Interpolator<T> interpolator) {
        this.value = initial;
        this.from = initial;
        this.to = initial;
        this.interpolator = interpolator;
    }

    public void animateTo(T target, long durationMs, Ease ease) {
        this.from = value;
        this.to = target;
        this.elapsed = 0f;
        this.duration = Math.max(durationMs, 0L) / 1000f;
        this.ease = ease;
        this.active = durationMs > 0;
        if (!this.active) this.value = target;
    }

    public boolean update(float delta) {
        if (!active) return false;
        elapsed += delta;
        float t = Math.min(elapsed / duration, 1f);
        value = interpolator.lerp(from, to, ease.apply(t));
        if (t >= 1f) {
            value = to;
            active = false;
        }
        return true;
    }

    public T get() { return value; }
    public boolean isActive() { return active; }
    public void finish() { value = to; active = false; }

    // --- Built-in interpolators ---

    public static final Interpolator<Float> FLOAT = (a, b, t) -> a + (b - a) * t;

    public static final Interpolator<Color> COLOR = (a, b, t) -> {
        Color c = new Color(a);
        c.lerp(b, t);
        return c;
    };

    // Factory methods
    public static AnimatedValue<Float> of(float initial) {
        return new AnimatedValue<>(initial, FLOAT);
    }

    public static AnimatedValue<Color> of(Color initial) {
        return new AnimatedValue<>(new Color(initial), COLOR);
    }
}
