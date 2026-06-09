package org.mindustrytool.libs.ui.style;

import org.mindustrytool.libs.ui.layout.NodeSizing;

public abstract class ComponentStyle {
    protected final NodeSizing sizing;

    protected ComponentStyle(NodeSizing sizing) {
        this.sizing = sizing;
    }
}
