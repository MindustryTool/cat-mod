package org.mindustrytool.libs.ui.widget;

import org.jetbrains.annotations.NotNull;

public interface Widget {
    ElementNode createElement();

    default org.mindustrytool.libs.ui.layout.LayoutSpec getLayoutSpec() {
        return org.mindustrytool.libs.ui.layout.LayoutSpec.defaultSpec();
    }

    default boolean canUpdate(@NotNull Widget newWidget) {
        return getClass() == newWidget.getClass();
    }

    default Object key() {
        return null;
    }
}

