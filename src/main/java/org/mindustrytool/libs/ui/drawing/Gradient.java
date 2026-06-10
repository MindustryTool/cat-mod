package org.mindustrytool.libs.ui.drawing;

import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.Texture.TextureWrap;
import arc.util.Disposable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Gradient implements Disposable {
    public static final int RESOLUTION = 256;

    private final Texture texture;
    private final int type;   // 0=linear, 1=radial, 2=conic
    private final float dx, dy;
    private final float repeat;

    private Gradient(Texture texture, int type, float dx, float dy, float repeat) {
        this.texture = texture;
        this.type = type;
        this.dx = dx;
        this.dy = dy;
        this.repeat = repeat;
    }

    public Texture getTexture() { return texture; }
    public float[] getParams() {
        return new float[]{dx, dy, type, repeat};
    }
    public int getType() { return type; }

    @Override
    public void dispose() {
        if (texture != null) texture.dispose();
    }

    // ─── Builder API ───

    public static Builder build() { return new Builder(); }

    public static Gradient linear(float angleDeg, Object... stops) {
        float rad = (float)Math.toRadians(angleDeg);
        return build().stops(stops).type(0).direction((float)Math.cos(rad), (float)Math.sin(rad)).create();
    }

    public static Gradient radial(float cx, float cy, Object... stops) {
        return build().stops(stops).type(1).direction(cx * 2f - 1f, cy * 2f - 1f).create();
    }

    public static Gradient conic(float cx, float cy, Object... stops) {
        return build().stops(stops).type(2).direction(cx * 2f - 1f, cy * 2f - 1f).create();
    }

    public static class Builder {
        private int type;
        private float dx, dy, repeat = 1f;
        private float[] positions;
        private Color[] colors;
        private boolean hardStop;

        public Builder type(int type) { this.type = type; return this; }
        public Builder direction(float dx, float dy) { this.dx = dx; this.dy = dy; return this; }
        public Builder repeat(float repeat) { this.repeat = repeat; return this; }
        public Builder hardStop() { this.hardStop = true; return this; }

        public Builder stops(Object... stops) {
            int count = stops.length / 2;
            positions = new float[count];
            colors = new Color[count];
            for (int i = 0; i < count; i++) {
                positions[i] = (Float) stops[i * 2];
                Object c = stops[i * 2 + 1];
                colors[i] = (c instanceof Color) ? (Color) c : Color.valueOf(c.toString());
            }
            return this;
        }

        public Gradient create() {
            ByteBuffer buf = ByteBuffer.allocateDirect(RESOLUTION * 4);
            buf.order(ByteOrder.nativeOrder());
            for (int x = 0; x < RESOLUTION; x++) {
                float t = (float) x / (RESOLUTION - 1);
                Color c = sampleStops(t);
                buf.put((byte)(c.r * 255f));
                buf.put((byte)(c.g * 255f));
                buf.put((byte)(c.b * 255f));
                buf.put((byte)(c.a * 255f));
            }
            buf.flip();
            Pixmap pix = new Pixmap(buf, RESOLUTION, 1);
            Texture tex = new Texture(pix);
            pix.dispose();
            tex.setFilter(TextureFilter.linear, TextureFilter.linear);
            tex.setWrap(TextureWrap.clampToEdge, TextureWrap.clampToEdge);
            return new Gradient(tex, type, dx, dy, repeat);
        }

        private Color sampleStops(float t) {
            if (t <= positions[0]) return colors[0];
            if (t >= positions[positions.length - 1]) return colors[colors.length - 1];
            for (int i = 0; i < positions.length - 1; i++) {
                float p0 = positions[i], p1 = positions[i + 1];
                if (t >= p0 && t <= p1) {
                    if (hardStop && Math.abs(p1 - p0) < 0.001f) {
                        return t < (p0 + p1) * 0.5f ? colors[i] : colors[i + 1];
                    }
                    float local = (t - p0) / (p1 - p0);
                    Color c = new Color(colors[i]);
                    c.lerp(colors[i + 1], local);
                    return c;
                }
            }
            return colors[colors.length - 1];
        }
    }
}
