package org.mindustrytool.ui.components;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.gl.Shader;
import arc.scene.Element;
import arc.scene.event.Touchable;

import org.mindustrytool.ui.layout.NodeSizing;
import org.mindustrytool.ui.layout.Sizing;
import org.mindustrytool.ui.layout.SizingProvider;
import org.mindustrytool.ui.spec.LayoutSpec;

import arc.func.Cons;

public class SDFRoundedBox implements Component, SizingProvider {

    public static class Builder {
        private float cornerRadius = 8f;
        private final Color color = Color.white.cpy();
        private Cons<NodeSizing> sizeFn;

        public Builder cornerRadius(float v) { cornerRadius = v; return this; }
        public Builder color(Color v) { color.set(v); return this; }
        public Builder size(Cons<NodeSizing> fn) { sizeFn = fn; return this; }

        public SDFRoundedBox build() {
            var box = new SDFRoundedBox(cornerRadius, color);
            if (sizeFn != null) sizeFn.get(box.sizing);
            return box;
        }
    }

    public static Builder build() { return new Builder(); }

    private static final String VERT = """
        attribute vec4 a_position;
        attribute vec4 a_color;
        attribute vec2 a_texCoord0;
        attribute vec4 a_mix_color;
        uniform mat4 u_projTrans;
        varying vec4 v_color;
        varying vec2 v_texCoord0;
        void main() {
          v_color = a_color;
          v_color.a = v_color.a * (255.0/254.0);
          v_texCoord0 = a_texCoord0;
          gl_Position = u_projTrans * a_position;
        }
        """;

    private static final String FRAG = """
        precision mediump float;
        uniform float u_cornerRadius;
        uniform vec2 u_size;
        varying vec4 v_color;
        varying vec2 v_texCoord0;
        float roundedBoxSDF(vec2 p, vec2 s, float r) {
          vec2 q = abs(p) - s + r;
          return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
        }
        void main() {
          vec2 halfSize = u_size * 0.5;
          vec2 pos = v_texCoord0 * u_size - halfSize;
          float dist = roundedBoxSDF(pos, halfSize, u_cornerRadius);
          float alpha = 1.0 - smoothstep(0.0, 2.0, dist);
          gl_FragColor = v_color * vec4(1.0, 1.0, 1.0, alpha);
        }
        """;

    private final Shader shader = new Shader(VERT, FRAG);
    private final float cornerRadius;
    private final Color color = new Color();
    private final LayoutSpec sizing;

    private final Element element = new Element() {
        @Override
        public void draw() {
            float x = this.x, y = this.y, w = getWidth(), h = getHeight();
            if (w <= 0f || h <= 0f) return;

            Draw.flush();
            Shader prev = Draw.getShader();
            Draw.shader(shader);

            shader.bind();
            shader.setUniformf("u_cornerRadius", cornerRadius);
            shader.setUniformf("u_size", w, h);

            Draw.color(color);
            float packed = Draw.getColorPacked();
            Texture tex = Core.atlas.white().texture;
            Fill.quad(tex,
                x, y, packed, 0f, 0f,
                x + w, y, packed, 1f, 0f,
                x + w, y + h, packed, 1f, 1f,
                x, y + h, packed, 0f, 1f);

            Draw.flush();
            Draw.shader(prev);
        }
    };

    private SDFRoundedBox(float cornerRadius, Color color) {
        this.cornerRadius = cornerRadius;
        this.color.set(color);
        this.sizing = new LayoutSpec();
        sizing.grow();
        element.touchable = Touchable.disabled;
    }

    @Override public Element element() { return element; }
    @Override public Sizing sizing() { return sizing; }

    @Override
    public void dispose() {
        shader.dispose();
    }
}
