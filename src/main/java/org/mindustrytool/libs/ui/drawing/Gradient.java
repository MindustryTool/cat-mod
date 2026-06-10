package org.mindustrytool.libs.ui.drawing;

import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.Texture.TextureWrap;
import arc.util.Disposable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Gradient represents a color transition texture generated dynamically in memory
 * and cached on the GPU.
 */
public class Gradient implements Disposable {
    public static final int RESOLUTION = 256;

    private final Texture texture;
    private final int type;   // 0=linear, 1=radial, 2=conic
    private final float directionX;
    private final float directionY;
    private final float repeat;

    private Gradient(Texture texture, int type, float directionX, float directionY, float repeat) {
        this.texture = texture;
        this.type = type;
        this.directionX = directionX;
        this.directionY = directionY;
        this.repeat = repeat;
    }

    public Texture getTexture() {
        return texture;
    }

    public float[] getParams() {
        return new float[]{directionX, directionY, type, repeat};
    }

    public int getType() {
        return type;
    }

    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }

    // ─── Builder API ───

    public static Builder build() {
        return new Builder();
    }

    public static Gradient linear(float angleDegrees, Object... stops) {
        float angleRadians = (float) Math.toRadians(angleDegrees);
        return build().stops(stops).type(0).direction((float) Math.cos(angleRadians), (float) Math.sin(angleRadians)).create();
    }

    public static Gradient radial(float centerX, float centerY, Object... stops) {
        return build().stops(stops).type(1).direction(centerX * 2f - 1f, centerY * 2f - 1f).create();
    }

    public static Gradient conic(float centerX, float centerY, Object... stops) {
        return build().stops(stops).type(2).direction(centerX * 2f - 1f, centerY * 2f - 1f).create();
    }

    public static class Builder {
        private int type;
        private float directionX;
        private float directionY;
        private float repeat = 1f;
        private float[] positions;
        private Color[] colors;
        private boolean hardStop;

        public Builder type(int type) {
            this.type = type;
            return this;
        }

        public Builder direction(float directionX, float directionY) {
            this.directionX = directionX;
            this.directionY = directionY;
            return this;
        }

        public Builder repeat(float repeat) {
            this.repeat = repeat;
            return this;
        }

        public Builder hardStop() {
            this.hardStop = true;
            return this;
        }

        public Builder stops(Object... stops) {
            int count = stops.length / 2;
            positions = new float[count];
            colors = new Color[count];
            for (int i = 0; i < count; i++) {
                positions[i] = (Float) stops[i * 2];
                Object colorObject = stops[i * 2 + 1];
                colors[i] = (colorObject instanceof Color) ? (Color) colorObject : Color.valueOf(colorObject.toString());
            }
            return this;
        }

        public Gradient create() {
            ByteBuffer buffer = ByteBuffer.allocateDirect(RESOLUTION * 4);
            buffer.order(ByteOrder.nativeOrder());
            for (int x = 0; x < RESOLUTION; x++) {
                float interpolationFactor = (float) x / (RESOLUTION - 1);
                Color sampledColor = sampleStops(interpolationFactor);
                buffer.put((byte) (sampledColor.r * 255f));
                buffer.put((byte) (sampledColor.g * 255f));
                buffer.put((byte) (sampledColor.b * 255f));
                buffer.put((byte) (sampledColor.a * 255f));
            }
            buffer.flip();
            Pixmap pixmap = new Pixmap(buffer, RESOLUTION, 1);
            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            texture.setFilter(TextureFilter.linear, TextureFilter.linear);
            texture.setWrap(TextureWrap.clampToEdge, TextureWrap.clampToEdge);
            return new Gradient(texture, type, directionX, directionY, repeat);
        }

        private Color sampleStops(float t) {
            if (t <= positions[0]) {
                return colors[0];
            }
            if (t >= positions[positions.length - 1]) {
                return colors[colors.length - 1];
            }
            for (int i = 0; i < positions.length - 1; i++) {
                float p0 = positions[i];
                float p1 = positions[i + 1];
                if (t >= p0 && t <= p1) {
                    if (hardStop && Math.abs(p1 - p0) < 0.001f) {
                        return t < (p0 + p1) * 0.5f ? colors[i] : colors[i + 1];
                    }
                    float localFactor = (t - p0) / (p1 - p0);
                    Color color = new Color(colors[i]);
                    color.lerp(colors[i + 1], localFactor);
                    return color;
                }
            }
            return colors[colors.length - 1];
        }
    }
}
