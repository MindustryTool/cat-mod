package org.mindustrytool.libs.ui.components;

import arc.scene.Element;
import arc.scene.Scene;
import arc.scene.style.Drawable;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.ui.component.AbstractComponent;
import org.mindustrytool.libs.ui.component.Component;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.layout.NodeSizing;

import arc.func.Cons;

import static arc.Core.scene;

/**
 * Button is a standard UI button component.
 * It supports child rendering inside the button and reactive dynamic styling/sizing changes.
 * It automatically disposes itself and releases signal connections when removed from the scene graph.
 */
public class Button extends AbstractComponent {

    /**
     * Style builder for Button, supporting custom themes, colors, and sizing configurations.
     */
    public class Style extends ComponentStyle<Style> {
        public final arc.scene.ui.Button.ButtonStyle buttonStyle;

        Style() {
            this.buttonStyle = element.getStyle();
        }

        @Override
        protected NodeSizing sizing() {
            return sizing;
        }

        @Override
        protected Element styledElement() {
            return element;
        }

        private void applySkin() {
            arc.scene.ui.Button.ButtonStyle skinStyle = scene.getStyle(arc.scene.ui.Button.ButtonStyle.class);
            buttonStyle.up = skinStyle.up;
            buttonStyle.down = skinStyle.down;
            buttonStyle.over = skinStyle.over;
            buttonStyle.checked = skinStyle.checked;
        }

        /**
         * Sets the button variant to be flat/ghost (no background graphics).
         *
         * @return this style builder instance
         */
        public Style ghostVariant() {
            buttonStyle.up = buttonStyle.down = buttonStyle.over = buttonStyle.checked = null;
            element.setStyle(buttonStyle);
            return this;
        }

        /**
         * Sets the button variant to primary theme.
         *
         * @return this style builder instance
         */
        public Style primaryVariant() {
            applySkin();
            element.setStyle(buttonStyle);
            return this;
        }

        /**
         * Sets the background drawable for the up (unpressed) state.
         *
         * @param drawable the up state drawable
         * @return this style builder instance
         */
        public Style up(Drawable drawable) {
            buttonStyle.up = drawable;
            element.setStyle(buttonStyle);
            return this;
        }

        /**
         * Sets the background drawable for the down (pressed) state.
         *
         * @param drawable the down state drawable
         * @return this style builder instance
         */
        public Style down(Drawable drawable) {
            buttonStyle.down = drawable;
            element.setStyle(buttonStyle);
            return this;
        }

        /**
         * Sets the background drawable for the hovered state.
         *
         * @param drawable the hovered state drawable
         * @return this style builder instance
         */
        public Style over(Drawable drawable) {
            buttonStyle.over = drawable;
            element.setStyle(buttonStyle);
            return this;
        }

        /**
         * Sets the background drawable for the checked state.
         *
         * @param drawable the checked state drawable
         * @return this style builder instance
         */
        public Style checkedDrawable(Drawable drawable) {
            buttonStyle.checked = drawable;
            element.setStyle(buttonStyle);
            return this;
        }

        /**
         * Sets the background drawable for the disabled state.
         *
         * @param drawable the disabled state drawable
         * @return this style builder instance
         */
        public Style disabledDrawable(Drawable drawable) {
            buttonStyle.disabled = drawable;
            element.setStyle(buttonStyle);
            return this;
        }

        /**
         * Sets whether the button is disabled.
         *
         * @param value true to disable the button
         * @return this style builder instance
         */
        public Style disabled(boolean value) {
            element.setDisabled(value);
            return this;
        }

        /**
         * Sets the checked state.
         *
         * @param value true to check the button, false to uncheck it
         * @return this style builder instance
         */
        public Style checked(boolean value) {
            element.setChecked(value);
            return this;
        }

        /**
         * Sets whether click events are fired programmatically.
         *
         * @param value true to enable programmatic change events
         * @return this style builder instance
         */
        public Style programmaticChangeEvents(boolean value) {
            element.setProgrammaticChangeEvents(value);
            return this;
        }

        /**
         * Adds an action listener triggered when the button is clicked.
         *
         * @param listener the click callback action
         * @return this style builder instance
         */
        public Style clicked(Runnable listener) {
            element.clicked(listener);
            return this;
        }

        /**
         * Adds an action listener triggered when the button checked state changes.
         *
         * @param listener the change callback action
         * @return this style builder instance
         */
        public Style changed(Runnable listener) {
            element.changed(listener);
            return this;
        }

        /**
         * Configures layout sizing for the button.
         *
         * @param configurator the node sizing configurator callback
         * @return this style builder instance
         */
        public Style size(Cons<NodeSizing> configurator) {
            configurator.get(sizing);
            return this;
        }
    }

    private final arc.scene.ui.Button element;
    public final Style style;
    private Component childComponent;

    private Effect styleEffect;
    private Effect sizeEffect;

    private Button() {
        arc.scene.ui.Button.ButtonStyle arcStyle =
            new arc.scene.ui.Button.ButtonStyle(scene.getStyle(arc.scene.ui.Button.ButtonStyle.class));
        this.element = new arc.scene.ui.Button(arcStyle) {
            @Override
            protected void setScene(Scene sceneInstance) {
                super.setScene(sceneInstance);
                if (sceneInstance == null) {
                    Button.this.dispose();
                }
            }
        };
        element.userObject = this;
        this.style = new Style();
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    /**
     * Factory method to create a new Button instance.
     *
     * @return a new Button component instance
     */
    public static Button of() {
        return new Button();
    }

    /**
     * Sets the child component rendered inside the button (e.g. text label, icon).
     *
     * @param child the child component
     * @return this button instance for chaining
     */
    public Button child(Component child) {
        this.childComponent = child;
        element.add(child.element());
        return this;
    }

    /**
     * Configures the button style properties reactively.
     *
     * @param configurator the style configurator callback
     * @return this button instance for chaining
     */
    public Button style(Cons<Style> configurator) {
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
     * Configures the button sizing constraints reactively.
     *
     * @param configurator the sizing configurator callback
     * @return this button instance for chaining
     */
    public Button size(Cons<NodeSizing> configurator) {
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
        if (childComponent != null) {
            childComponent.dispose();
        }
    }
}
