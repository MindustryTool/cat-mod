package org.mindustrytool.libs.ui.widget;

import org.jetbrains.annotations.NotNull;

/**
 * Represent a declarative, immutable configuration for a user interface element.
 * Widgets form the blueprint of the UI tree, which are instantiated into mutable
 * {@link ElementNode}s for layout, styling, and rendering.
 */
public interface Widget {

    /**
     * Creates the mutable {@link ElementNode} instance backing this widget configuration.
     *
     * @return a new mutable element node.
     */
    ElementNode createElement();

    /**
     * Checks if this widget configuration can be used to update an existing element node.
     * By default, returns true if both widgets share the exact same class type.
     *
     * @param newWidget the new widget configuration.
     * @return true if the node can be updated, false otherwise.
     */
    default boolean canUpdate(@NotNull Widget newWidget) {
        return getClass() == newWidget.getClass();
    }

    /**
     * Retrieves an optional key identifier used to preserve identity during reconciliation.
     *
     * @return the identity key of this widget, or null if not keyed.
     */
    default Object key() {
        return null;
    }
}
