package org.mindustrytool.ui.components;

import arc.scene.Element;

import org.mindustrytool.ui.layout.SizingProvider;

public interface Component extends SizingProvider {

    Element element();

    default void dispose() {

    }
}
