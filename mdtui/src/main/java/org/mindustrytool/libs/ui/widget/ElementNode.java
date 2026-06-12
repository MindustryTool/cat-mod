package org.mindustrytool.libs.ui.widget;

import arc.scene.Element;
import lombok.Getter;
import org.mindustrytool.libs.ui.layout.LayoutSpec;

/**
 * A mutable element in the UI tree that corresponds to a declarative {@link Widget} configuration.
 * Elements manage the lifecycle of the underlying Arc UI {@link Element}s, handling mounting,
 * layout updates, and component state.
 */
public abstract class ElementNode {

    @Getter
    protected Widget widget;

    @Getter
    protected Element arcElement;

    /**
     * Constructs an element node bound to a widget configuration.
     *
     * @param widget the initial widget configuration.
     */
    public ElementNode(Widget widget) {
        this.widget = widget;
    }

    /**
     * Resolves the current {@link LayoutSpec} of the associated widget.
     *
     * @return the layout specification of the widget.
     */
    public abstract LayoutSpec sizing();

    /**
     * Mounts this element node onto its parent node.
     * This method initializes the backing Arc element and structures it into the parent's hierarchy.
     *
     * @param parent the parent element node, or null if mounting as a root element.
     */
    public abstract void mount(ElementNode parent);

    /**
     * Updates this element node with a new widget configuration.
     * Classes overriding this method must call {@code super.update(newWidget)} to update the bound widget.
     *
     * @param newWidget the new widget configuration.
     */
    public void update(Widget newWidget) {
        this.widget = newWidget;
    }

    /**
     * Disposes of this element, removing its backing Arc UI element from the scene tree
     * and freeing associated resources.
     */
    public void dispose() {
        if (arcElement == null) {
            return;
        }

        arcElement.remove();
        arcElement = null;
    }
}
