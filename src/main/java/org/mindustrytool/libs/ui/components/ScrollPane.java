package org.mindustrytool.libs.ui.components;

import arc.scene.Element;

import org.mindustrytool.libs.ui.component.AbstractComponent;
import org.mindustrytool.libs.ui.component.Component;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.layout.NodeSizing;
import org.mindustrytool.libs.ui.layout.NodeSizing.SizeMode;

import arc.func.Cons;

import static arc.Core.scene;

public class ScrollPane extends AbstractComponent {
    public class Style extends ComponentStyle<Style> {
        boolean fadeScrollBars;
        boolean scrollBarsOnTop;
        boolean disableX;
        boolean disableY;
        boolean overscroll = true;
        boolean smoothScrolling = true;
        boolean flickScroll = true;
        boolean clip = true;

        Style(NodeSizing sizing) { super(sizing); }

        public Style fadeScrollBars(boolean v) {
            fadeScrollBars = v;
            if (v) element.setupFadeScrollBars(0.5f, 2f);
            if (v && scrollBarsOnTop) {
            } else {
                element.setFadeScrollBars(v);
            }
            return this;
        }

        public Style scrollBarsOnTop(boolean v) {
            scrollBarsOnTop = v;
            element.setScrollbarsOnTop(v);
            if (v && fadeScrollBars) element.setFadeScrollBars(false);
            return this;
        }

        public Style disableX(boolean v) { disableX = v; element.setScrollingDisabledX(v); return this; }
        public Style disableY(boolean v) { disableY = v; element.setScrollingDisabledY(v); return this; }
        public Style overscroll(boolean v) { overscroll = v; element.setOverscroll(v, v); return this; }
        public Style smoothScrolling(boolean v) { smoothScrolling = v; element.setSmoothScrolling(v); return this; }
        public Style flickScroll(boolean v) { flickScroll = v; element.setFlickScroll(v); return this; }
        public Style clip(boolean v) { clip = v; element.setClip(v); return this; }
        public Style size(Cons<NodeSizing> fn) { fn.get(sizing); applySize(); return this; }
    }

    public final Style style;
    private final arc.scene.ui.ScrollPane element;
    private Component childComponent;

    private ScrollPane() {
        this.style = new Style(sizing);
        arc.scene.ui.ScrollPane.ScrollPaneStyle s = new arc.scene.ui.ScrollPane.ScrollPaneStyle(scene.getStyle(arc.scene.ui.ScrollPane.ScrollPaneStyle.class));
        this.element = new arc.scene.ui.ScrollPane(new arc.scene.ui.Label(""), s);
        element.userObject = this;
        sizing.onInvalidate(() -> { applySize(); element.invalidateHierarchy(); });
    }

    public static ScrollPane of() { return new ScrollPane(); }

    public ScrollPane child(Component c) {
        childComponent = c;
        element.setWidget(c.element());
        return this;
    }

    public ScrollPane style(Cons<Style> fn) { fn.get(style); element.invalidateHierarchy(); return this; }
    public ScrollPane size(Cons<NodeSizing> fn) { fn.get(sizing); element.invalidateHierarchy(); return this; }

    private void applySize() {
        if (sizing.getWidthMode() == SizeMode.FIXED) element.setWidth(sizing.getFixedWidth());
        if (sizing.getHeightMode() == SizeMode.FIXED) element.setHeight(sizing.getFixedHeight());
    }

    @Override public Element element() { return element; }

    @Override
    public void dispose() {
        super.dispose();
        if (childComponent != null) childComponent.dispose();
    }
}
