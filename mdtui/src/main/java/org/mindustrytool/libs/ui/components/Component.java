package org.mindustrytool.libs.ui.components;

import arc.func.Cons;
import arc.input.KeyCode;
import arc.scene.Element;

import org.mindustrytool.libs.ui.layout.NodeSpec;

/**
 * Root interface for all UI components in the reactive component system.
 * <p>
 * Every component wraps an arc {@link Element} and exposes its sizing via a
 * {@link NodeSpec}. Components must also implement {@link #dispose()} to clean
 * up reactive subscriptions and native resources when they are removed from the
 * scene graph.
 */
public interface Component {

    Element element();

    NodeSpec sizing();

    void dispose();

    /**
     * Fires when the element is clicked (touch down then up).
     */
    default Component onClick(Runnable r) {
        element().clicked(r);
        return this;
    }

    /**
     * Fires when the element is clicked with a specific button.
     */
    default Component onClick(KeyCode button, Runnable r) {
        element().clicked(button, r);
        return this;
    }

    /**
     * Fires on touch down (immediate, no wait for release).
     */
    default Component onTap(Runnable r) {
        element().tapped(r);
        return this;
    }

    /**
     * Fires when the pointer enters the element.
     */
    default Component onHover(Runnable r) {
        element().hovered(r);
        return this;
    }

    /**
     * Fires when the pointer exits the element.
     */
    default Component onExit(Runnable r) {
        element().exited(r);
        return this;
    }

    /**
     * Fires when the pointer is released over the element.
     */
    default Component onRelease(Runnable r) {
        element().released(r);
        return this;
    }

    /**
     * Fires on a key press while the element has focus.
     */
    default Component onKeyDown(Cons<KeyCode> cons) {
        element().keyDown(cons);
        return this;
    }
}
