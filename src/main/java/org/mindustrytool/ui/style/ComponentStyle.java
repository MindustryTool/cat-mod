package org.mindustrytool.ui.style;

import org.mindustrytool.ui.layout.NodeSizing;

import arc.func.Cons;

public abstract class ComponentStyle {
    protected final NodeSizing sizing;

    protected ComponentStyle(NodeSizing sizing) {
        this.sizing = sizing;
    }
}
