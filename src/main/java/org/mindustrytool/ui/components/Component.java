package org.mindustrytool.ui.components;

import arc.scene.Element;

import org.mindustrytool.ui.layout.Sizing;

public interface Component {

    Element element();
    default Sizing sizing() { return null; }
    default void dispose() { }
}
