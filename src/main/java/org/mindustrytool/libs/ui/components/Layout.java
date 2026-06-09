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
    private final LayoutSpec spec;
    private final WidgetGroup group;
    private final Seq<Component> children = new Seq<>();

    private Layout() {
        this.spec = new LayoutSpec();

        this.group = new WidgetGroup() {
            { setTransform(true); }

            @Override
            public float getPrefWidth() {
                var s = spec.sizing;
                if (s.widthMode() == SizeMode.FIXED) return s.constrainW(s.fixedWidth());
                if (s.widthMode() == SizeMode.GROW) return 0f;
                return s.constrainW(LayoutEngine.prefWidth(s, spec.isColumn(), spec.gap(), getChildren()));
            }

            @Override
            public float getPrefHeight() {
                var s = spec.sizing;
                if (s.heightMode() == SizeMode.FIXED) return s.constrainH(s.fixedHeight());
                if (s.heightMode() == SizeMode.GROW) return 0f;
                return s.constrainH(LayoutEngine.prefHeight(s, spec.isColumn(), spec.gap(), getChildren()));
            }

            @Override
            public void layout() {
                var s = spec.sizing;
                float w = getWidth(), h = getHeight();
                LayoutEngine.layout(spec, getChildren(), s.padLeft(), s.padBottom(),
                    Math.max(0f, w - s.padH()), Math.max(0f, h - s.padV()));
            }
        };

        group.userObject = this;
        spec.onInvalidate(group::invalidateHierarchy);
    }

    public static Layout of() { return new Layout(); }

    public Layout child(Component c) {
        children.add(c);
        group.addChild(c.element());
        return this;
    }

    public Layout style(Cons<LayoutSpec> fn) { fn.get(spec); group.invalidateHierarchy(); return this; }
    public Layout size(Cons<NodeSizing> fn) { fn.get(spec.sizing); group.invalidateHierarchy(); return this; }

    @Override public Element element() { return group; }
    @Override public Sizing sizing() { return spec.sizing(); }

    @Override
    public void dispose() {
        for (Component c : children) c.dispose();
    }
}
