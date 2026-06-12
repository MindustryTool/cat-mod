package org.mindustrytool.libs.ui.widget;

import arc.scene.Element;

import lombok.Getter;

import org.mindustrytool.libs.ui.layout.LayoutSpec;

public abstract class ElementNode {
    public @Getter Widget widget;
    protected @Getter Element arcElement;

    public ElementNode(Widget widget) {
        this.widget = widget;
    }

    public LayoutSpec sizing() {
        return widget.getLayoutSpec();
    }


    public abstract void mount(ElementNode parent);

    public void update(Widget newWidget) {
        this.widget = newWidget;
    }

    public void dispose() {
        if (arcElement != null) {
            arcElement.remove();
            arcElement = null;
        }
    }
}

