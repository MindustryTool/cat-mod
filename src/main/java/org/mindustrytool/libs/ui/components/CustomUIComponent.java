package org.mindustrytool.libs.ui.components;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.gl.Shader;
import arc.scene.Element;
import arc.scene.Scene;
import arc.scene.event.Touchable;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.ui.component.AbstractComponent;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.layout.NodeSizing;

import arc.func.Cons;

/**
 * CustomUIComponent is a specialized UI component that renders a shader-based rounded box background.
 * It supports reactive custom parameters such as corner radius and background color, and disposes of its GL resources cleanly.
 */
public class CustomUIComponent extends AbstractComponent {

    /**
     * Style builder for CustomUIComponent, supporting custom corner radius, background color, and sizing.
     */
    public class Style extends ComponentStyle<Style> {
        float cornerRadius = 8.0f;
        final Color boxColor = new Color(Color.darkGray);

        Style() {
        }

        @Override
        protected NodeSizing sizing() {
            return sizing;
        }

        @Override
        protected Element styledElement() {
            return element;
        }

        /**
         * Sets the corner radius of the rounded box shader.
         *
         * @param value the corner radius value
         * @return this style builder instance
         */
        public Style cornerRadius(float value) {
            this.cornerRadius = value;
            return this;
        }

        /**
         * Sets the background color.
         *
         * @param value the background color
         * @return this style builder instance
         */
        public Style color(Color value) {
            this.boxColor.set(value);
            return this;
        }

        /**
         * Configures layout sizing.
         *
         * @param configurator the node sizing configurator callback
         * @return this style builder instance
         */
        public Style size(Cons<NodeSizing> configurator) {
            configurator.get(sizing);
            return this;
        }
    }

    public final Style style;

    private static final String VERTEX_SHADER_SOURCE = """
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

    private static final String FRAGMENT_SHADER_SOURCE = """
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

    private final Shader shader = new Shader(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);

    private final Element element = new Element() {
        @Override
        public float getPrefWidth() {
            return sizing.getFixedWidth();
        }

        @Override
        public float getPrefHeight() {
            return sizing.getFixedHeight();
        }

        @Override
        protected void setScene(Scene sceneInstance) {
            super.setScene(sceneInstance);
            if (sceneInstance == null) {
                CustomUIComponent.this.dispose();
            }
        }

        @Override
        public void draw() {
            float xPosition = this.x;
            float yPosition = this.y;
            float width = getWidth();
            float height = getHeight();
            if (width <= 0.0f || height <= 0.0f) return;

            Draw.flush();
            Shader previousShader = Draw.getShader();
            Draw.shader(shader);

            shader.bind();
            shader.setUniformf("u_cornerRadius", style.cornerRadius);
            shader.setUniformf("u_size", width, height);
            shader.setUniformf("u_color", style.boxColor.r, style.boxColor.g, style.boxColor.b, style.boxColor.a);
            Draw.color(style.boxColor);
            drawQuad(xPosition, yPosition, width, height, style.boxColor);

            Draw.flush();
            Draw.shader(previousShader);
            Draw.color();
        }
    };

    private Effect styleEffect;
    private Effect sizeEffect;

    private void drawQuad(float xPosition, float yPosition, float width, float height, Color color) {
        float packedColor = color.toFloatBits();
        Texture whiteTexture = Core.atlas.white().texture;
        Fill.quad(whiteTexture,
            xPosition, yPosition, packedColor, 0f, 0f,
            xPosition + width, yPosition, packedColor, 1f, 0f,
            xPosition + width, yPosition + height, packedColor, 1f, 1f,
            xPosition, yPosition + height, packedColor, 0f, 1f);
    }

    private CustomUIComponent() {
        this.style = new Style();
        sizing.onInvalidate(element::invalidateHierarchy);
        sizing.grow();
        element.touchable = Touchable.disabled;
        element.userObject = this;
    }

    /**
     * Factory method to create a new CustomUIComponent instance.
     *
     * @return a new CustomUIComponent component instance
     */
    public static CustomUIComponent of() {
        return new CustomUIComponent();
    }

    /**
     * Configures the component style properties reactively.
     *
     * @param configurator the style configurator callback
     * @return this custom component instance for chaining
     */
    public CustomUIComponent style(Cons<Style> configurator) {
        if (styleEffect != null) {
            styleEffect.dispose();
            subscriptions.remove(styleEffect);
        }
        styleEffect = new Effect(() -> {
            configurator.get(style);
            element.invalidateHierarchy();
        });
        subscriptions.add(styleEffect);
        return this;
    }

    /**
     * Configures the component sizing constraints reactively.
     *
     * @param configurator the sizing configurator callback
     * @return this custom component instance for chaining
     */
    public CustomUIComponent size(Cons<NodeSizing> configurator) {
        if (sizeEffect != null) {
            sizeEffect.dispose();
            subscriptions.remove(sizeEffect);
        }
        sizeEffect = new Effect(() -> {
            configurator.get(sizing);
            element.invalidateHierarchy();
        });
        subscriptions.add(sizeEffect);
        return this;
    }

    @Override
    public Element element() {
        return element;
    }

    @Override
    public void dispose() {
        super.dispose();
        shader.dispose();
    }
}
