package org.mindustrytool.libs.ui.components;

import arc.scene.Element;
import arc.scene.Scene;
import arc.struct.Seq;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.ui.component.AbstractComponent;
import org.mindustrytool.libs.ui.component.Component;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.layout.NodeSizing;

import arc.func.Cons;

/**
 * Stack is a UI container component that overlays all of its children components on top of each other.
 * It supports reactive custom configurations and automatic cleanup of child resources upon removal.
 */
public class Stack extends AbstractComponent {

    public class Style extends ComponentStyle<Style> {
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
    private final arc.scene.ui.layout.Stack element;
    private final Seq<Component> children = new Seq<>();

    private Effect styleEffect;
    private Effect sizeEffect;

    private Stack() {
        this.element = new arc.scene.ui.layout.Stack() {
            @Override
            protected void setScene(Scene sceneInstance) {
                super.setScene(sceneInstance);
                if (sceneInstance == null) {
                    Stack.this.dispose();
                }
            }
        };
        element.userObject = this;
        this.style = new Style();
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    /**
     * Factory method to create a new Stack instance.
     *
     * @return a new Stack component instance
     */
    public static Stack of() {
        return new Stack();
    }

    /**
     * Adds a child component overlayed in this stack container.
     *
     * @param child the overlay child component
     * @return this stack instance for chaining
     */
    public Stack child(Component child) {
        this.children.add(child);
        this.element.add(child.element());
        return this;
    }

    /**
     * Configures the stack style properties reactively.
     *
     * @param configurator the style configurator callback
     * @return this stack instance for chaining
     */
    public Stack style(Cons<Style> configurator) {
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
     * Configures the stack sizing constraints reactively.
     *
     * @param configurator the sizing configurator callback
     * @return this stack instance for chaining
     */
    public Stack size(Cons<NodeSizing> configurator) {
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
        for (int i = 0; i < children.size; i++) {
            children.get(i).dispose();
        }
    }
}
