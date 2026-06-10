package org.mindustrytool.libs.ui.component;

import arc.scene.Element;

import org.mindustrytool.libs.ui.layout.NodeSpec;

/**
 * Root interface for all UI components in the reactive component system.
 * <p>
 * Every component wraps an arc {@link Element} and exposes its sizing via a
 * {@link NodeSpec}. Components must also implement {@link #dispose()} to clean
 * up reactive subscriptions and native resources when they are removed from the
 * scene graph.
 * <p>
 * Implementations should prefer the reactive builder pattern:
 * <pre>{@code
 * Label.of()
 *     .style(s -> s.text("Hello").textColor(Color.WHITE).fontScale(1.2f))
 *     .size(sz -> sz.fixedWidth(200f));
 * }</pre>
 */
public interface Component {

    /**
     * Returns the underlying arc scene-graph element.
     * This is the object that gets added to a parent group or layout.
     */
    Element element();

    /**
     * Returns the sizing specification that controls this component's
     * layout constraints (width, height, padding, grow behaviour, etc.).
     */
    NodeSpec sizing();

    /**
     * Releases all resources held by this component.
     * <p>
     * This includes disposing reactive {@link org.mindustrytool.libs.signal.Effect}
     * subscriptions, freeing native textures, and removing children if applicable.
     * Called automatically when the element is detached from the scene.
     */
    void dispose();
}
