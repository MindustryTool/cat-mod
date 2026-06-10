package org.mindustrytool.libs.ui.component;

import arc.scene.Element;

import org.mindustrytool.libs.ui.layout.NodeSizing;

public interface Component {

    Element element();

    NodeSizing sizing();

    void dispose();
}
