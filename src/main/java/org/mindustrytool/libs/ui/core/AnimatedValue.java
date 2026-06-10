package org.mindustrytool.libs.ui.animation;

import arc.graphics.Color;

/**
 * A single interpolated value that animates from its current state toward a
 * target over time using an easing function.
 * <p>
 * Fluent API:
 * <pre>{@code
 * var anim = AnimatedValue.of(0f).to(100f).duration(1000).ease(Ease.bounceOut).play();
 * // then each frame:
 * anim.update(delta);
 * float val = anim.get();
 * }</pre>
 * <p>
 * Backward-compatible with {@link #animateTo(Object, long, Ease)}.
 */
public final class AnimatedValue<T> {
    private T value, from, to;
    private float elapsed, duration;
    private long durationMs;
    private Ease ease;
    private boolean active;
    private final Interpolator<T> interpolator;

    /**
     * Defines how two values of type {@code T} are interpolated over normalised time.
     */
    public interface Interpolator<T> {
        T lerp(T a, T b, float t);
    }

    /**
     * Creates an animated value starting at {@code initial}.
     * Use {@link #of(float)} or {@link #of(Color)} for common types.
     */
    public AnimatedValue(T initial, Interpolator<T> interpolator) {
        this.value = initial;
        this.from = initial;
        this.to = initial;
        this.interpolator = interpolator;
    }

    /**
     * Sets the target value.
     */
    public AnimatedValue<T> to(T target) {
        this.to = target;
        return this;
    }

    /**
     * Sets the animation duration in milliseconds.
     */
    public AnimatedValue<T> duration(long ms) {
        this.durationMs = ms;
        return this;
    }

    /**
     * Sets the easing function.
     */
    public AnimatedValue<T> ease(Ease ease) {
        this.ease = ease;
        return this;
    }

    /**
     * Starts the animation. Captures the current value as the starting point
     * and begins the timer. Returns {@code this} for chaining.
     */
    public AnimatedValue<T> play() {
        this.from = value;
        this.elapsed = 0f;
        this.duration = Math.max(durationMs, 0L) / 1000f;
        this.active = durationMs > 0;
        if (!active) value = to;
        return this;
    }

    /**
     * Convenience that combines {@link #to(Object)}, {@link #duration(long)},
     * {@link #ease(Ease)}, and {@link #play()} in one call.
     */
    public void animateTo(T target, long durationMs, Ease ease) {
        to(target).duration(durationMs).ease(ease).play();
    }

    /**
     * Advances the animation by {@code delta} seconds.
     *
     * @return true while still animating, false when complete
     */
    public boolean update(float delta) {
        if (!active) return false;
        elapsed += delta;
        float t = Math.min(elapsed / duration, 1f);
        value = interpolator.lerp(from, to, ease.apply(t));
        if (t >= 1f) {
            value = to;
            active = false;
        }
        return active;
    }

    /**
     * Returns the current interpolated value.
     */
    public T get() {
        return value;
    }

    /**
     * Returns true if the animation is still running.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Jumps to the target value and stops.
     */
    public void finish() {
        value = to;
        active = false;
    }

    // --- Built-in interpolators ---

    public static final Interpolator<Float> FLOAT = (a, b, t) -> a + (b - a) * t;

    public static final Interpolator<Color> COLOR = (a, b, t) -> {
        Color c = new Color(a);
        c.lerp(b, t);
        return c;
    };

    /**
     * Creates an animated float starting at {@code initial}.
     */
    public static AnimatedValue<Float> of(float initial) {
        return new AnimatedValue<>(initial, FLOAT);
    }

    /**
     * Creates an animated Color starting at a copy of {@code initial}.
     */
    public static AnimatedValue<Color> of(Color initial) {
        return new AnimatedValue<>(new Color(initial), COLOR);
    }
}
