package org.mindustrytool.ui.components;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.gl.Shader;
import arc.scene.Element;
import arc.scene.event.Touchable;

import org.mindustrytool.ui.kernel.AbstractComponent;
import org.mindustrytool.ui.style.ComponentStyle;
import org.mindustrytool.ui.layout.NodeSizing;

import arc.func.Cons;

public class CustomUIComponent extends AbstractComponent {

    public class Style extends ComponentStyle {
        float cornerRadius = 8f;
        final Color boxColor = new Color(Color.darkGray);

        Style(NodeSizing sizing) { super(sizing); }

        public Style cornerRadius(float v) { cornerRadius = v; return this; }
        public Style color(Color v) { boxColor.set(v); return this; }
        public Style size(Cons<NodeSizing> fn) { fn.get(sizing); return this; }
    }

    public final Style style;

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
        uniform float u_cornerRadius;
        uniform vec2 u_size;
        uniform vec4 u_color;
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
          gl_FragColor = u_color * vec4(1.0, 1.0, 1.0, alpha);
        }
        """;

    private final Shader shader = new Shader(VERT, FRAG);

    private final Element element = new Element() {
        @Override
        public float getPrefWidth() {
            return sizing.fixedWidth();
        }

        @Override
        public float getPrefHeight() {
            return sizing.fixedHeight();
        }

        @Override
        public void draw() {
            float x = this.x, y = this.y, w = getWidth(), h = getHeight();
            if (w <= 0f || h <= 0f) return;

            Draw.flush();
            Shader prev = Draw.getShader();
            Draw.shader(shader);

            shader.bind();
            shader.setUniformf("u_cornerRadius", style.cornerRadius);
            shader.setUniformf("u_size", w, h);
            shader.setUniformf("u_color", style.boxColor.r, style.boxColor.g, style.boxColor.b, style.boxColor.a);
            Draw.color(style.boxColor);
            drawQuad(x, y, w, h, style.boxColor);

            Draw.flush();
            Draw.shader(prev);
            Draw.color();
        }
    };

    private void drawQuad(float x, float y, float w, float h, Color color) {
        float packed = color.toFloatBits();
        Texture tex = Core.atlas.white().texture;
        Fill.quad(tex,
            x, y, packed, 0f, 0f,
            x + w, y, packed, 1f, 0f,
            x + w, y + h, packed, 1f, 1f,
            x, y + h, packed, 0f, 1f);
    }

    private CustomUIComponent() {
        this.style = new Style(sizing);
        sizing.onInvalidate(element::invalidateHierarchy);
        sizing.grow();
        element.touchable = Touchable.disabled;
        element.userObject = this;
    }

    public static CustomUIComponent of() { return new CustomUIComponent(); }

    public CustomUIComponent style(Cons<Style> fn) { fn.get(style); element.invalidateHierarchy(); return this; }
    public CustomUIComponent size(Cons<NodeSizing> fn) { fn.get(sizing); element.invalidateHierarchy(); return this; }

    @Override public Element element() { return element; }

    @Override
    public void dispose() {
        super.dispose();
        shader.dispose();
    }
}
