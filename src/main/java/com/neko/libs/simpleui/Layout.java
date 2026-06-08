package com.neko.libs.simpleui;

import arc.scene.Element;
import arc.scene.ui.layout.WidgetGroup;

import com.neko.libs.simpleui.components.Component;
import com.neko.libs.simpleui.layout.LayoutEngine;
import com.neko.libs.simpleui.layout.Sizing;
import com.neko.libs.simpleui.layout.Sizing.SizeMode;
import com.neko.libs.simpleui.spec.LayoutSpec;

import java.util.function.Consumer;

public class Layout implements Component {
    private final LayoutSpec spec;
    private final WidgetGroup group;

    private Layout(boolean isColumn, Component... children) {
        this.spec = new LayoutSpec();
        if (isColumn) spec.col();

        this.group = new WidgetGroup() {
            { setTransform(true); }

            @Override
            public float getPrefWidth() {
                LayoutSpec s = spec;
                if (s.widthMode() == SizeMode.FIXED) return s.constrainW(s.fixedWidth());
                if (s.widthMode() == SizeMode.GROW) return 0f;
                float cw = LayoutEngine.prefWidth(s, s.isColumn(), s.gap(), getChildren());
                return s.constrainW(cw);
            }

            @Override
            public float getPrefHeight() {
                LayoutSpec s = spec;
                if (s.heightMode() == SizeMode.FIXED) return s.constrainH(s.fixedHeight());
                if (s.heightMode() == SizeMode.GROW) return 0f;
                float ch = LayoutEngine.prefHeight(s, s.isColumn(), s.gap(), getChildren());
                return s.constrainH(ch);
            }

            @Override
            public void layout() {
                LayoutSpec s = spec;
                float w = getWidth(), h = getHeight();
                float ix = s.padLeft(), iy = s.padBottom();
                float iw = Math.max(0f, w - s.padH()), ih = Math.max(0f, h - s.padV());
                LayoutEngine.layout(s, getChildren(), ix, iy, iw, ih);
            }
        };

        group.userObject = this;
        spec.onInvalidate(group::invalidateHierarchy);
        for (Component c : children) group.addChild(c.element());
    }

    public static Layout column(Component... children) { return new Layout(true, children); }
    public static Layout row(Component... children) { return new Layout(false, children); }

    public Layout style(Consumer<LayoutSpec> fn) {
        fn.accept(spec);
        group.invalidateHierarchy();
        return this;
    }

    public Layout add(Component child) {
        group.addChild(child.element());
        return this;
    }

    public Layout size(float w, float h) { group.setSize(w, h); return this; }
    public Layout pos(float x, float y) { group.setPosition(x, y); return this; }

    @Override
    public Element element() { return group; }

    @Override
    public Sizing sizing() { return spec; }

    @Override
    public void onDestroy() {
        for (Element child : group.getChildren()) {
            Object o = child.userObject;
            if (o instanceof Component) ((Component) o).onDestroy();
        }
    }
}
