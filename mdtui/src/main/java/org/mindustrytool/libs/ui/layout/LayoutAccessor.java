package org.mindustrytool.libs.ui.layout;

/**
 * LayoutAccessor abstracts access to a scene graph node's properties.
 * This allows the LayoutEngine to remain independent of any specific Scene Graph framework (e.g., Arc UI)
 * and makes it fully mockable for unit testing without graphical dependencies.
 *
 * @param <T> the type of layout node element (e.g., Element, or MockNode in tests)
 */
public interface LayoutAccessor<T> {

    /**
     * Checks if the given layout node is visible and should participate in layout calculations.
     *
     * @param node the layout node
     * @return true if visible, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isVisible(T node);

    /**
     * Retrieves the preferred width of the given layout node based on its internal content.
     *
     * @param node the layout node
     * @return the preferred width coordinate value
     */
    float getPreferredWidth(T node);

    /**
     * Retrieves the preferred height of the given layout node based on its internal content.
     *
     * @param node the layout node
     * @return the preferred height coordinate value
     */
    float getPreferredHeight(T node);

    /**
     * Assigns the final position bounds and dimensions to the given layout node.
     *
     * @param node      the layout node
     * @param xPosition the horizontal position coordinate
     * @param yPosition the vertical position coordinate
     * @param width     the final width of the node
     * @param height    the final height of the node
     */
    void setBounds(T node, float xPosition, float yPosition, float width, float height);

    /**
     * Retrieves the sizing specification assigned to the given layout node.
     *
     * @param node the layout node
     * @return the node sizing instance, or null if none is specified
     */
    NodeSpec<?> getSizing(T node);
}
