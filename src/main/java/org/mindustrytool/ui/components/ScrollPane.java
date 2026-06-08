package org.mindustrytool.ui.components;

import arc.scene.Element;
import arc.struct.Seq;

import org.mindustrytool.ui.layout.NodeSizing;
import org.mindustrytool.ui.layout.Sizing;
import org.mindustrytool.ui.layout.Sizing.SizeMode;

import arc.func.Cons;
import arc.func.Func;

import static arc.Core.scene;

public class ScrollPane implements Component {
    public static class Builder {
        private Component child;
        private Cons<Style> styleFn;
        private Cons<NodeSizing> sizeFn;

        public Builder child(Component v) { child = v; return this; }
        public Builder style(Cons<Style> fn) { styleFn = fn; return this; }
        public Builder size(Cons<NodeSizing> fn) { sizeFn = fn; return this; }

        public ScrollPane build() {
            ScrollPane p = ScrollPane.of(child);
            if (styleFn != null) p.style(styleFn);
            if (sizeFn != null) p.size(sizeFn);
            return p;
        }
    }

    public class Style {
        boolean fadeScrollBars;
        boolean scrollBarsOnTop;
        boolean disableX;
        boolean disableY;
        boolean overscroll = true;
        boolean smoothScrolling = true;
        boolean flickScroll = true;
        boolean clip = true;

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
    }

    private final arc.scene.ui.ScrollPane element;
    public final Style style = new Style();
    public final NodeSizing sizing = new NodeSizing();
    private Component childComponent;
    private final Seq<Runnable> subscriptions = new Seq<>();

    private ScrollPane(Element child) {
        arc.scene.ui.ScrollPane.ScrollPaneStyle s = new arc.scene.ui.ScrollPane.ScrollPaneStyle(scene.getStyle(arc.scene.ui.ScrollPane.ScrollPaneStyle.class));
        this.element = new arc.scene.ui.ScrollPane(child, s);
        element.userObject = this;
        sizing.onInvalidate(() -> { applySize(); element.invalidateHierarchy(); });
    }

    public static Builder build() { return new Builder(); }
    public static ScrollPane of(Component child) {
        ScrollPane pane = new ScrollPane(child.element());
        pane.childComponent = child;
        return pane;
    }

    public ScrollPane style(Cons<Style> fn) { fn.get(style); return this; }

    public ScrollPane size(Cons<NodeSizing> fn) {
        fn.get(sizing); applySize(); element.invalidateHierarchy(); return this;
    }

    public ScrollPane bind(Func<ScrollPane, Runnable> fn) {
        Runnable cleanup = fn.get(this);
        if (cleanup != null) subscriptions.add(cleanup);
        return this;
    }

    private void applySize() {
        if (sizing.widthMode() == SizeMode.FIXED) element.setWidth(sizing.fixedWidth());
        if (sizing.heightMode() == SizeMode.FIXED) element.setHeight(sizing.fixedHeight());
    }

    @Override public Element element() { return element; }
    @Override public Sizing sizing() { return sizing; }

    @Override
    public void dispose() {
        subscriptions.each(Runnable::run); subscriptions.clear();
        if (childComponent != null) childComponent.dispose();
    }
}
