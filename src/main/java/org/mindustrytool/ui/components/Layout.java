package org.mindustrytool.ui.components;

import arc.scene.Element;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Seq;

import org.mindustrytool.ui.layout.LayoutEngine;
import org.mindustrytool.ui.layout.NodeSizing;
import org.mindustrytool.ui.layout.Sizing;
import org.mindustrytool.ui.layout.Sizing.SizeMode;
import org.mindustrytool.ui.spec.LayoutSpec;

import arc.func.Cons;

public class Layout implements Component {
    public static class Builder {
        private boolean column;
        private final Seq<Component> children = new Seq<>();
        private Cons<LayoutSpec> styleFn;
        private Cons<NodeSizing> sizeFn;

        public Builder col() { column = true; return this; }
        public Builder row() { column = false; return this; }
        public Builder add(Component c) { children.add(c); return this; }
        public Builder children(Component... v) { for (Component c : v) children.add(c); return this; }
        public Builder style(Cons<LayoutSpec> fn) { styleFn = fn; return this; }
        public Builder size(Cons<NodeSizing> fn) { sizeFn = fn; return this; }

        public Layout build() {
            Layout l = new Layout(column, children.toArray(Component.class));
            if (styleFn != null) l.style(styleFn);
            if (sizeFn != null) l.size(sizeFn);
            return l;
        }
    }

    private final LayoutSpec spec;
    private final WidgetGroup group;
    private final Seq<Component> children = new Seq<>();

    Layout(boolean isColumn, Component... children) {
        this.spec = new LayoutSpec();
        if (isColumn) spec.col();

        this.group = new WidgetGroup() {
            { setTransform(true); }

            @Override
            public float getPrefWidth() {
                LayoutSpec s = spec;
                if (s.widthMode() == SizeMode.FIXED) return s.constrainW(s.fixedWidth());
                if (s.widthMode() == SizeMode.GROW) return 0f;
                return s.constrainW(LayoutEngine.prefWidth(s, s.isColumn(), s.gap(), getChildren()));
            }

            @Override
            public float getPrefHeight() {
                LayoutSpec s = spec;
                if (s.heightMode() == SizeMode.FIXED) return s.constrainH(s.fixedHeight());
                if (s.heightMode() == SizeMode.GROW) return 0f;
                return s.constrainH(LayoutEngine.prefHeight(s, s.isColumn(), s.gap(), getChildren()));
            }

            @Override
            public void layout() {
                LayoutSpec s = spec;
                float w = getWidth(), h = getHeight();
                LayoutEngine.layout(s, getChildren(), s.padLeft(), s.padBottom(),
                    Math.max(0f, w - s.padH()), Math.max(0f, h - s.padV()));
            }
        };

        group.userObject = this;
        spec.onInvalidate(group::invalidateHierarchy);
        for (Component c : children) {
            this.children.add(c);
            group.addChild(c.element());
        }
    }

    public static Builder build() { return new Builder(); }
    public static Layout column(Component... children) { return new Layout(true, children); }
    public static Layout row(Component... children) { return new Layout(false, children); }

    public Layout style(Cons<LayoutSpec> fn) { fn.get(spec); group.invalidateHierarchy(); return this; }
    public Layout size(Cons<NodeSizing> fn) { fn.get(spec); group.invalidateHierarchy(); return this; }
    public Layout add(Component child) { children.add(child); group.addChild(child.element()); return this; }

    @Override public Element element() { return group; }
    @Override public Sizing sizing() { return spec; }

    @Override
    public void dispose() {
        for (Component c : children) c.dispose();
    }
}
