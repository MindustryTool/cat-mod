package org.mindustrytool.libs.ui.components;

import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.Scene;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.ui.component.AbstractComponent;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.layout.NodeSpec;

import arc.func.Cons;

import static arc.Core.scene;

/**
 * Label is a basic UI component used to display read-only text labels.
 * It supports reactive dynamic style updates such as text changes, scaling, alignments, colors, and wrapping.
 */
public class Label extends AbstractComponent {

    /**
     * Style builder for Label, supporting text content, text alignment, scale, wrapping, color, and sizing.
     */
    public class Style extends ComponentStyle<Style> {
        public final arc.scene.ui.Label.LabelStyle labelStyle;
        Color textColor;
        int textAlign;
        float fontScale = 1.0f;
        boolean wrap;

        Style() {
            this.labelStyle = element.getStyle();
        }

        @Override
        protected NodeSpec sizing() {
            return sizing;
        }

        @Override
        protected Element styledElement() {
            return element;
        }

        /**
         * Sets the text displayed on the label.
         *
         * @param value the text content
         * @return this style builder instance
         */
        public Style text(String value) {
            element.setText(value);
            return this;
        }

        /**
         * Sets the text color.
         *
         * @param value the text color
         * @return this style builder instance
         */
        public Style textColor(Color value) {
            this.textColor = value;
            this.labelStyle.fontColor = value;
            element.setStyle(labelStyle);
            return this;
        }

        /**
         * Sets the text alignment using Align bitmask flags.
         *
         * @param value the align flags
         * @return this style builder instance
         */
        public Style textAlign(int value) {
            this.textAlign = value;
            element.setAlignment(value);
            return this;
        }

        /**
         * Sets the font scaling factor.
         *
         * @param value the font scale
         * @return this style builder instance
         */
        public Style fontScale(float value) {
            this.fontScale = value;
            element.setFontScale(value);
            return this;
        }

        /**
         * Enables/disables multiline text wrapping.
         *
         * @param value true to enable wrapping, false otherwise
         * @return this style builder instance
         */
        public Style wrap(boolean value) {
            this.wrap = value;
            element.setWrap(value);
            return this;
        }

        /**
         * Configures layout sizing.
         *
         * @param configurator the node sizing configurator callback
         * @return this style builder instance
         */
        public Style size(Cons<NodeSpec> configurator) {
            configurator.get(sizing);
            return this;
        }
    }

    private final arc.scene.ui.Label element;
    public final Style style;

    private Effect styleEffect;
    private Effect sizeEffect;

    private Label() {
        var arcStyle = new arc.scene.ui.Label.LabelStyle(scene.getStyle(arc.scene.ui.Label.LabelStyle.class));
        this.element = new arc.scene.ui.Label("", arcStyle) {
            @Override
            protected void setScene(Scene sceneInstance) {
                super.setScene(sceneInstance);
                if (sceneInstance == null) {
                    Label.this.dispose();
                }
            }
        };
        element.userObject = this;
        this.style = new Style();
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    /**
     * Factory method to create a new Label instance.
     *
     * @return a new Label component instance
     */
    public static Label of() {
        return new Label();
    }

    /**
     * Configures the label style properties reactively.
     *
     * @param configurator the style configurator callback
     * @return this label instance for chaining
     */
    public Label style(Cons<Style> configurator) {
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
     * Configures the label sizing constraints reactively.
     *
     * @param configurator the sizing configurator callback
     * @return this label instance for chaining
     */
    public Label size(Cons<NodeSpec> configurator) {
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
}
