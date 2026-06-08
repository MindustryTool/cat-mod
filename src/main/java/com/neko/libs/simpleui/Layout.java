package com.neko.libs.simpleui;

import arc.scene.Element;
import arc.scene.ui.layout.WidgetGroup;

import com.neko.libs.simpleui.components.UIComponent;
import com.neko.libs.simpleui.layout.LayoutEngine;
import com.neko.libs.simpleui.layout.NodeSizing;
import com.neko.libs.simpleui.layout.Sizing;
import com.neko.libs.simpleui.layout.Sizing.SizeMode;
import com.neko.libs.simpleui.spec.ContainerSpec;
import java.util.function.Consumer;

public class Layout implements UIComponent {
    private final ContainerSpec spec;
    private final WidgetGroup group;

    private Layout(boolean isColumn, UIComponent... children) {
        this.spec = new ContainerSpec();
        if (isColumn) spec.col();

        this.group = new WidgetGroup() {
            { setTransform(true); }

            @Override
            public float getPrefWidth() {
                ContainerSpec s = spec;
                if (s.widthMode() == SizeMode.FIXED) return s.constrainW(s.fixedWidth());
                if (s.widthMode() == SizeMode.GROW) return 0f;
                float cw = LayoutEngine.prefWidth(s, s.isColumn(), s.gap(), getChildren());
                return s.constrainW(cw);
            }

            @Override
            public float getPrefHeight() {
                ContainerSpec s = spec;
                if (s.heightMode() == SizeMode.FIXED) return s.constrainH(s.fixedHeight());
                if (s.heightMode() == SizeMode.GROW) return 0f;
                float ch = LayoutEngine.prefHeight(s, s.isColumn(), s.gap(), getChildren());
                return s.constrainH(ch);
            }

            @Override
            public void layout() {
                ContainerSpec s = spec;
                float w = getWidth(), h = getHeight();
                float ix = s.padLeft(), iy = s.padBottom();
                float iw = Math.max(0f, w - s.padH()), ih = Math.max(0f, h - s.padV());
                LayoutEngine.layout(s, getChildren(), ix, iy, iw, ih);
            }
        };

        group.userObject = this;
        spec.onInvalidate(group::invalidateHierarchy);
        for (UIComponent c : children) group.addChild(c.element());
    }

    public static Layout column(UIComponent... children) { return new Layout(true, children); }
    public static Layout row(UIComponent... children) { return new Layout(false, children); }

    public static UIComponent glue() {
        return new UIComponent() {
            private final Element el = new Element();
            private final NodeSizing sizing = new NodeSizing().growX();
            { el.userObject = this; }
            @Override public Element element() { return el; }
            @Override public Sizing sizing() { return sizing; }
        };
    }

    public Layout style(Consumer<ContainerSpec> fn) {
        fn.accept(spec);
        group.invalidateHierarchy();
        return this;
    }

    public Layout add(UIComponent child) {
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
        spec.dispose();
        for (Element child : group.getChildren()) {
            Object o = child.userObject;
            if (o instanceof UIComponent) ((UIComponent) o).onDestroy();
        }
    }
}
