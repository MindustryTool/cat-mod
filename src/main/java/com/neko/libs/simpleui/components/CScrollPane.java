package com.neko.libs.simpleui.components;

import arc.scene.Element;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.ScrollPane.ScrollPaneStyle;
import arc.struct.Seq;

import com.neko.libs.simpleui.layout.NodeSizing;
import com.neko.libs.simpleui.layout.Sizing;
import com.neko.libs.simpleui.layout.Sizing.SizeMode;

import java.util.function.Consumer;
import java.util.function.Function;

import static arc.Core.scene;

public class CScrollPane implements UIComponent {
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
                // arc bug: fadeScrollBars prevents widgetAreaBounds expansion
                // when scrollbarsOnTop is active, causing scrollbar overlap.
                // Skip setFadeScrollBars so the layout path is correct.
                // Scrollbar will remain visible (no fade) with proper expansion.
            } else {
                element.setFadeScrollBars(v);
            }
            return this;
        }

        public Style scrollBarsOnTop(boolean v) {
            scrollBarsOnTop = v;
            element.setScrollbarsOnTop(v);
            if (v && fadeScrollBars) {
                element.setFadeScrollBars(false);
            }
            return this;
        }

        public Style disableX(boolean v) {
            disableX = v;
            element.setScrollingDisabledX(v);
            return this;
        }

        public Style disableY(boolean v) {
            disableY = v;
            element.setScrollingDisabledY(v);
            return this;
        }

        public Style overscroll(boolean v) {
            overscroll = v;
            element.setOverscroll(v, v);
            return this;
        }

        public Style smoothScrolling(boolean v) {
            smoothScrolling = v;
            element.setSmoothScrolling(v);
            return this;
        }

        public Style flickScroll(boolean v) {
            flickScroll = v;
            element.setFlickScroll(v);
            return this;
        }

        public Style clip(boolean v) {
            clip = v;
            element.setClip(v);
            return this;
        }
    }

    private final ScrollPane element;
    public final Style style = new Style();
    public final NodeSizing sizing = new NodeSizing();
    private UIComponent childComponent;
    private final Seq<Runnable> subs = new Seq<>();

    private CScrollPane(Element child) {
        ScrollPaneStyle s = new ScrollPaneStyle(scene.getStyle(ScrollPaneStyle.class));
        this.element = new ScrollPane(child, s);
        sizing.onInvalidate(() -> {
            applySize();
            element.invalidateHierarchy();
        });
    }

    public static CScrollPane of(Element child) {
        return new CScrollPane(child);
    }

    public static CScrollPane of(UIComponent child) {
        CScrollPane pane = new CScrollPane(child.element());
        pane.childComponent = child;
        return pane;
    }

    public CScrollPane style(Consumer<Style> fn) {
        fn.accept(style);
        return this;
    }

    public CScrollPane size(Consumer<NodeSizing> fn) {
        fn.accept(sizing);
        applySize();
        element.invalidateHierarchy();
        return this;
    }

    public CScrollPane bind(Function<CScrollPane, Runnable> fn) {
        Runnable cleanup = fn.apply(this);
        if (cleanup != null) subs.add(cleanup);
        return this;
    }

    private void applySize() {
        if (sizing.widthMode() == SizeMode.FIXED) element.setWidth(sizing.fixedWidth());
        if (sizing.heightMode() == SizeMode.FIXED) element.setHeight(sizing.fixedHeight());
    }

    @Override
    public Element element() { return element; }

    @Override
    public Sizing sizing() { return sizing; }

    @Override
    public void onDestroy() {
        subs.each(Runnable::run);
        subs.clear();
        if (childComponent != null) childComponent.onDestroy();
    }
}
