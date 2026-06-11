package org.mindustrytool.libs.ui.core;

import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.Texture.TextureWrap;
import arc.struct.Seq;
import arc.util.Disposable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Gradient represents a color transition texture generated dynamically in memory
 * and cached on the GPU.
 *
 * <p>Uses a Java record to carry the generated Texture and its shader parameter array.
 * Supported gradient types include linear, radial, and conic sweeps.
 *
 * @param texture the generated 1D OpenGL texture mapping the color stops
 * @param params  the parameter array passed directly to shader uniforms:
 *                [0]=directionX, [1]=directionY, [2]=type (0:linear, 1:radial, 2:conic), [3]=repeat
 */
public record Gradient(Texture texture, float... params) implements Disposable {

    /** The resolution of the generated gradient texture. */
    public static final int RESOLUTION = 256;

    /**
     * Stop represents a color stop point along the gradient ramp.
     *
     * @param position the normalized position (0.0 to 1.0) along the gradient
     * @param color    the Color associated with this stop position
     */
    public record Stop(float position, Color color) {}

    /**
     * Creates a type-safe color Stop with a Color object.
     *
     * @param position the normalized position (0.0 to 1.0)
     * @param color    the Color object
     * @return a new Stop instance
     */
    public static Stop stop(float position, Color color) {
        return new Stop(position, color);
    }

    /**
     * Creates a type-safe color Stop with a hex color string.
     *
     * @param position the normalized position (0.0 to 1.0)
     * @param hexColor the hex representation of the color (e.g. "ff0000" or "red")
     * @return a new Stop instance
     */
    public static Stop stop(float position, String hexColor) {
        return new Stop(position, Color.valueOf(hexColor));
    }

    /**
     * Disposes the GPU texture resource associated with this gradient.
     */
    @Override
    public void dispose() {
        if (texture != null) texture.dispose();
    }

    /**
     * Starts building a new Gradient with a builder.
     *
     * @return a new Builder instance
     */
    public static Builder build() {
        return new Builder();
    }

    /**
     * Creates a linear gradient texture with a specific angle and color stops.
     *
     * @param angleDegrees the angle of the gradient in degrees
     * @param stops        the color stops along the gradient
     * @return a newly created Gradient
     */
    public static Gradient linear(float angleDegrees, Stop... stops) {
        float angleRadians = (float) Math.toRadians(angleDegrees);
        return build().stops(stops).type(0).direction((float) Math.cos(angleRadians), (float) Math.sin(angleRadians)).create();
    }

    /**
     * Creates a radial gradient texture centered at specific coordinates.
     *
     * @param centerX the normalized X coordinate of the center (0.0 to 1.0)
     * @param centerY the normalized Y coordinate of the center (0.0 to 1.0)
     * @param stops   the color stops along the gradient
     * @return a newly created Gradient
     */
    public static Gradient radial(float centerX, float centerY, Stop... stops) {
        return build().stops(stops).type(1).direction(centerX * 2f - 1f, centerY * 2f - 1f).create();
    }

    /**
     * Creates a conic sweep gradient texture centered at specific coordinates.
     *
     * @param centerX the normalized X coordinate of the center (0.0 to 1.0)
     * @param centerY the normalized Y coordinate of the center (0.0 to 1.0)
     * @param stops   the color stops along the gradient
     * @return a newly created Gradient
     */
    public static Gradient conic(float centerX, float centerY, Stop... stops) {
        return build().stops(stops).type(2).direction(centerX * 2f - 1f, centerY * 2f - 1f).create();
    }

    /**
     * Builder is responsible for constructing and rasterizing the gradient texture.
     */
    public static class Builder {
        private int type;
        private float directionX;
        private float directionY;
        private float repeat = 1f;
        private final Seq<Stop> stops = new Seq<>();
        private boolean hardStop;

        /**
         * Sets the type of the gradient (0:linear, 1:radial, 2:conic).
         *
         * @param type the type ID
         * @return this builder instance
         */
        public Builder type(int type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the direction vector or center point coordinates.
         *
         * @param directionX the X component of direction or center
         * @param directionY the Y component of direction or center
         * @return this builder instance
         */
        public Builder direction(float directionX, float directionY) {
            this.directionX = directionX;
            this.directionY = directionY;
            return this;
        }

        /**
         * Sets the scaling/repeating factor of the gradient pattern.
         *
         * @param repeat the repeating frequency multiplier
         * @return this builder instance
         */
        public Builder repeat(float repeat) {
            this.repeat = repeat;
            return this;
        }

        /**
         * Enables hard-edges/sharp color transitions instead of smooth interpolations.
         *
         * @return this builder instance
         */
        public Builder hardStop() {
            this.hardStop = true;
            return this;
        }

        /**
         * Adds a single color stop to the gradient.
         *
         * @param position the normalized position (0.0 to 1.0)
         * @param color    the Color associated with the stop
         * @return this builder instance
         */
        public Builder addStop(float position, Color color) {
            stops.add(new Stop(position, color));
            return this;
        }

        /**
         * Adds a single color stop using a hex color string.
         *
         * @param position the normalized position (0.0 to 1.0)
         * @param hexColor the hex representation of the color
         * @return this builder instance
         */
        public Builder addStop(float position, String hexColor) {
            stops.add(new Stop(position, Color.valueOf(hexColor)));
            return this;
        }

        /**
         * Adds multiple color stops at once.
         *
         * @param stops the array of color stops
         * @return this builder instance
         */
        public Builder stops(Stop... stops) {
            this.stops.addAll(stops);
            return this;
        }

        /**
         * Rasterizes the color stops into a 1D Pixmap, uploads it to an OpenGL Texture,
         * and returns the finalized Gradient record.
         *
         * @return a new Gradient instance
         */
        public Gradient create() {
            if (stops.isEmpty()) {
                stops.add(new Stop(0f, Color.white));
                stops.add(new Stop(1f, Color.white));
            }
            stops.sort((a, b) -> Float.compare(a.position(), b.position()));

            ByteBuffer buffer = ByteBuffer.allocateDirect(RESOLUTION * 4).order(ByteOrder.nativeOrder());
            for (int x = 0; x < RESOLUTION; x++) {
                Color c = sampleStops((float) x / (RESOLUTION - 1));
                buffer.put((byte) (c.r * 255f)).put((byte) (c.g * 255f)).put((byte) (c.b * 255f)).put((byte) (c.a * 255f));
            }
            buffer.flip();

            Pixmap pixmap = new Pixmap(buffer, RESOLUTION, 1);
            Texture texture = new Texture(pixmap);
            pixmap.dispose();

            texture.setFilter(TextureFilter.linear, TextureFilter.linear);
            texture.setWrap(TextureWrap.clampToEdge, TextureWrap.clampToEdge);
            return new Gradient(texture, directionX, directionY, type, repeat);
        }

        private Color sampleStops(float t) {
            Stop first = stops.first();
            if (t <= first.position()) return first.color();
            Stop last = stops.peek();
            if (t >= last.position()) return last.color();

            for (int i = 0; i < stops.size - 1; i++) {
                Stop s0 = stops.get(i);
                Stop s1 = stops.get(i + 1);
                float p0 = s0.position();
                float p1 = s1.position();
                if (t >= p0 && t <= p1) {
                    if (hardStop && Math.abs(p1 - p0) < 0.001f) {
                        return t < (p0 + p1) * 0.5f ? s0.color() : s1.color();
                    }
                    float localFactor = (t - p0) / (p1 - p0);
                    Color color = new Color(s0.color());
                    color.lerp(s1.color(), localFactor);
                    return color;
                }
            }
            return last.color();
        }
    }
}
