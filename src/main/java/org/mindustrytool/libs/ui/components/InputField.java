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
 * InputField is a reactive wrapper around Arc's TextField, allowing user text input.
 * It supports reactive styling/sizing configurations and cleans up resources when removed from the scene.
 */
public class InputField extends AbstractComponent {

    /**
     * Style builder for InputField, supporting text content, placeholder message text, color configurations, and sizing.
     */
    public class Style extends ComponentStyle<Style> {
        public final arc.scene.ui.TextField.TextFieldStyle textFieldStyle;

        Style() {
            this.textFieldStyle = element.getStyle();
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
         * Sets the text content of the input field.
         *
         * @param value the text value
         * @return this style builder instance
         */
        public Style text(String value) {
            element.setText(value);
            return this;
        }

        /**
         * Sets the placeholder / message text shown when empty.
         *
         * @param value the placeholder message
         * @return this style builder instance
         */
        public Style placeholder(String value) {
            element.setMessageText(value);
            return this;
        }

        /**
         * Sets the text color.
         *
         * @param value the text color
         * @return this style builder instance
         */
        public Style textColor(Color value) {
            textFieldStyle.fontColor = value;
            element.setStyle(textFieldStyle);
            return this;
        }

        /**
         * Sets the maximum length of text allowed in the input field.
         *
         * @param value the maximum length
         * @return this style builder instance
         */
        public Style maxLength(int value) {
            element.setMaxLength(value);
            return this;
        }

        /**
         * Sets whether password mode is enabled (characters masked).
         *
         * @param value true to enable password mode
         * @return this style builder instance
         */
        public Style passwordMode(boolean value) {
            element.setPasswordMode(value);
            return this;
        }

        /**
         * Sets whether the input field is disabled.
         *
         * @param value true to disable the input field
         * @return this style builder instance
         */
        public Style disabled(boolean value) {
            element.setDisabled(value);
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

    private final arc.scene.ui.TextField element;
    public final Style style;

    private Effect styleEffect;
    private Effect sizeEffect;

    private InputField() {
        var baseStyle = scene.getStyle(arc.scene.ui.TextField.TextFieldStyle.class);
        var customStyle = new arc.scene.ui.TextField.TextFieldStyle(baseStyle);
        this.element = new arc.scene.ui.TextField("", customStyle) {
            @Override
            protected void setScene(Scene sceneInstance) {
                super.setScene(sceneInstance);
                if (sceneInstance == null) {
                    InputField.this.dispose();
                }
            }
        };
        element.userObject = this;
        this.style = new Style();
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    /**
     * Factory method to create a new InputField instance.
     *
     * @return a new InputField component instance
     */
    public static InputField of() {
        return new InputField();
    }

    /**
     * Configures the input field style properties reactively.
     *
     * @param configurator the style configurator callback
     * @return this input field instance for chaining
     */
    public InputField style(Cons<Style> configurator) {
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
     * Configures the input field sizing constraints reactively.
     *
     * @param configurator the sizing configurator callback
     * @return this input field instance for chaining
     */
    public InputField size(Cons<NodeSpec> configurator) {
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
