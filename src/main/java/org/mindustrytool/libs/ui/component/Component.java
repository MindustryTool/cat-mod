package org.mindustrytool.libs.ui.kernel;

import arc.scene.Element;

import org.mindustrytool.libs.ui.layout.Sizing;

public interface Component {

    Element element();

    Sizing sizing();

    default void dispose() {
    }
}
