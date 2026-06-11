package org.mindustrytool.libs.ui.core;

import arc.graphics.Color;

/**
 * Lightweight single-value animation utility.
 *
 * <p>Use the factory methods {@link #of(float)} and {@link #of(Color)} to
 * create instances, then drive each frame with {@link #update(float)}.
 *
 * <p>Two concrete implementations are provided as static inner classes:
 * <ul>
 *   <li>{@link Float}  — primitive {@code float}, zero boxing overhead.</li>
 *   <li>{@link Color}  — RGBA stored as four primitive floats, no {@link Color} allocation on {@link Color#get}.</li>
 * </ul>
 *
 * <p>Example:
 * <pre>{@code
 * AnimatedValue.Float alpha = AnimatedValue.of(0f);
 * alpha.animateTo(1f, 300, Ease.quadOut);
 *
 * // each frame:
 * alpha.update(delta);
 * float a = alpha.get();
 * }</pre>
 */
public interface AnimatedValue {

    /**
     * Advances the animation by {@code delta} seconds.
     *
     * @return {@code true} while still animating, {@code false} when complete
     */
    boolean update(float delta);

    /** Returns {@code true} if the animation is still running. */
    boolean isActive();

    /** Jumps to the target value immediately and stops the animation. */
    void finish();


    /** Creates an {@link Float} starting at {@code initial}. */
    static AnimatedValue.Float of(float initial) {
        return new AnimatedValue.Float(initial);
    }


    /** Creates a {@link Color} starting at a copy of {@code initial}. */
    static AnimatedValue.Color of(arc.graphics.Color initial) {
        return new AnimatedValue.Color(initial);
    }


    // ─── Float ───────────────────────────────────────────────────────────────

    /**
     * Animates a single {@code float} value.
     * All storage uses primitives — no boxing.
     */
    final class Float implements AnimatedValue {

        private float value, from, to;
        private float elapsed, duration;
        private Ease ease;
        private boolean active;


        public Float(float initial) {
            value = from = to = initial;
        }


        /** Sets the target value. */
        public Float to(float target) {
            to = target;
            return this;
        }


        /** Sets the animation duration in milliseconds. */
        public Float duration(long ms) {
            duration = Math.max(ms, 0L) / 1000f;
            return this;
        }


        /** Sets the easing function. */
        public Float ease(Ease ease) {
            this.ease = ease;
            return this;
        }


        /**
         * Captures the current value as the start point and begins the timer.
         * If {@code duration} is zero the target is applied immediately.
         */
        public Float play() {
            from = value;
            elapsed = 0f;
            active = duration > 0f;
            if (!active) value = to;
            return this;
        }


        /** Convenience combining {@link #to}, {@link #duration}, {@link #ease}, and {@link #play}. */
        public void animateTo(float target, long ms, Ease ease) {
            to(target).duration(ms).ease(ease).play();
        }


        @Override
        public boolean update(float delta) {
            if (!active) return false;
            elapsed += delta;
            float t = Math.min(elapsed / duration, 1f);
            value = from + (to - from) * ease.apply(t);
            if (t >= 1f) {
                value = to;
                active = false;
            }
            return active;
        }


        /** Returns the current interpolated value as a primitive {@code float}. */
        public float get() {
            return value;
        }


        @Override
        public boolean isActive() {
            return active;
        }


        @Override
        public void finish() {
            value = to;
            active = false;
        }
    }


    // ─── Color ───────────────────────────────────────────────────────────────

    /**
     * Animates an RGBA color.
     * The four components are stored as primitive {@code float} fields —
     * no {@link arc.graphics.Color} object is allocated during interpolation.
     * Use {@link #get(arc.graphics.Color)} to write the result into an existing color.
     */
    final class Color implements AnimatedValue {

        private float r, g, b, a;
        private float fromR, fromG, fromB, fromA;
        private float toR, toG, toB, toA;
        private float elapsed, duration;
        private Ease ease;
        private boolean active;


        public Color(arc.graphics.Color initial) {
            r = fromR = toR = initial.r;
            g = fromG = toG = initial.g;
            b = fromB = toB = initial.b;
            a = fromA = toA = initial.a;
        }


        /** Sets the target color from an {@link arc.graphics.Color}. */
        public Color to(arc.graphics.Color color) {
            toR = color.r;
            toG = color.g;
            toB = color.b;
            toA = color.a;
            return this;
        }


        /** Sets the target color from raw RGBA components. */
        public Color to(float r, float g, float b, float a) {
            toR = r;
            toG = g;
            toB = b;
            toA = a;
            return this;
        }


        /** Sets the animation duration in milliseconds. */
        public Color duration(long ms) {
            duration = Math.max(ms, 0L) / 1000f;
            return this;
        }


        /** Sets the easing function. */
        public Color ease(Ease ease) {
            this.ease = ease;
            return this;
        }


        /**
         * Captures the current color as the start point and begins the timer.
         * If {@code duration} is zero the target is applied immediately.
         */
        public Color play() {
            fromR = r;
            fromG = g;
            fromB = b;
            fromA = a;
            elapsed = 0f;
            active = duration > 0f;
            if (!active) {
                r = toR;
                g = toG;
                b = toB;
                a = toA;
            }
            return this;
        }


        /** Convenience combining {@link #to(arc.graphics.Color)}, {@link #duration}, {@link #ease}, and {@link #play}. */
        public void animateTo(arc.graphics.Color target, long ms, Ease ease) {
            to(target).duration(ms).ease(ease).play();
        }


        @Override
        public boolean update(float delta) {
            if (!active) return false;
            elapsed += delta;
            float t = Math.min(elapsed / duration, 1f);
            float et = ease.apply(t);
            float eu = 1f - et;
            r = fromR * eu + toR * et;
            g = fromG * eu + toG * et;
            b = fromB * eu + toB * et;
            a = fromA * eu + toA * et;
            if (t >= 1f) {
                r = toR;
                g = toG;
                b = toB;
                a = toA;
                active = false;
            }
            return active;
        }


        /**
         * Writes the current interpolated color into {@code out} and returns it.
         * No allocation is performed.
         */
        public arc.graphics.Color get(arc.graphics.Color out) {
            return out.set(r, g, b, a);
        }


        @Override
        public boolean isActive() {
            return active;
        }


        @Override
        public void finish() {
            r = toR;
            g = toG;
            b = toB;
            a = toA;
            active = false;
        }
    }
}
