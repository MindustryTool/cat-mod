package org.mindustrytool.libs.ui.components;

import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.Scene;
import arc.scene.style.Drawable;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.ui.component.AbstractComponent;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.layout.NodeSizing;

import arc.func.Cons;

import static arc.Core.scene;

/**
 * Checkbox is a standard checkbox UI component that displays a toggleable checked/unchecked state alongside a label.
 * It supports reactive dynamic styling/sizing changes and automatically disposes itself when removed from the scene.
 */
public class Checkbox extends AbstractComponent {

    /**
     * Style builder for Checkbox, supporting labels, color styles, states, and sizing.
     */
    public class Style extends ComponentStyle<Style> {
        public final arc.scene.ui.CheckBox.CheckBoxStyle checkBoxStyle;

        Style() {
            this.checkBoxStyle = element.getStyle();
        }

        @Override
        protected NodeSizing sizing() {
            return sizing;
        }

        @Override
        protected Element styledElement() {
            return element;
        }

        private void apply() {
            element.setStyle(checkBoxStyle);
        }

        /**
         * Sets the label text displayed next to the checkbox.
         *
         * @param value the text label
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
            checkBoxStyle.fontColor = value;
            apply();
            return this;
        }

        /**
         * Sets the drawable for the checked state.
         *
         * @param drawable the checked state drawable
         * @return this style builder instance
         */
        public Style checkboxOn(Drawable drawable) {
            checkBoxStyle.checkboxOn = drawable;
            apply();
            return this;
        }

        /**
         * Sets the drawable for the unchecked state.
         *
         * @param drawable the unchecked state drawable
         * @return this style builder instance
         */
        public Style checkboxOff(Drawable drawable) {
            checkBoxStyle.checkboxOff = drawable;
            apply();
            return this;
        }

        /**
         * Sets the drawable for the hovered state.
         *
         * @param drawable the hovered state drawable
         * @return this style builder instance
         */
        public Style checkboxOver(Drawable drawable) {
            checkBoxStyle.checkboxOver = drawable;
            apply();
            return this;
        }

        /**
         * Sets the checked state.
         *
         * @param value true to check the box, false to uncheck it
         * @return this style builder instance
         */
        public Style checked(boolean value) {
            element.setChecked(value);
            return this;
        }

        /**
         * Sets whether the checkbox is disabled.
         *
         * @param value true to disable the checkbox
         * @return this style builder instance
         */
        public Style disabled(boolean value) {
            element.setDisabled(value);
            return this;
        }

        /**
         * Adds an action listener triggered when the checkbox state changes.
         *
         * @param listener the action callback
         * @return this style builder instance
         */
        public Style changed(Runnable listener) {
            element.changed(listener);
            return this;
        }

        /**
         * Configures layout sizing for the checkbox.
         *
         * @param configurator the node sizing configurator callback
         * @return this style builder instance
         */
        public Style size(Cons<NodeSizing> configurator) {
            configurator.get(sizing);
            return this;
        }
    }

    private final arc.scene.ui.CheckBox element;
    public final Style style;

    private Effect styleEffect;
    private Effect sizeEffect;

    private Checkbox() {
        var baseStyle = scene.getStyle(arc.scene.ui.CheckBox.CheckBoxStyle.class);
        this.element = new arc.scene.ui.CheckBox("", baseStyle) {
            @Override
            protected void setScene(Scene sceneInstance) {
                super.setScene(sceneInstance);
                if (sceneInstance == null) {
                    Checkbox.this.dispose();
                }
            }
        };
        element.userObject = this;
        this.style = new Style();
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    /**
     * Factory method to create a new Checkbox instance.
     *
     * @return a new Checkbox component instance
     */
    public static Checkbox of() {
        return new Checkbox();
    }

    /**
     * Configures the checkbox style properties reactively.
     *
     * @param configurator the style configurator callback
     * @return this checkbox instance for chaining
     */
    public Checkbox style(Cons<Style> configurator) {
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
     * Configures the checkbox sizing constraints reactively.
     *
     * @param configurator the sizing configurator callback
     * @return this checkbox instance for chaining
     */
    public Checkbox size(Cons<NodeSizing> configurator) {
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
