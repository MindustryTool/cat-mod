package org.mindustrytool.libs.ui.components;

import arc.scene.Element;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Seq;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.ui.component.Component;
import org.mindustrytool.libs.ui.layout.LayoutEngine;
import org.mindustrytool.libs.ui.layout.NodeSpec;
import org.mindustrytool.libs.ui.layout.NodeSpec.SizeMode;
import org.mindustrytool.libs.ui.layout.LayoutSpec;

import arc.func.Cons;

public class Layout implements Component {
    private final LayoutSpec spec;
    private final WidgetGroup group;

    private Component background;
    private final Seq<Component> staticChildren = new Seq<>();
    private final Seq<Component> currentChildren = new Seq<>();
    private arc.func.Prov<Seq<Component>> childrenProvider = () -> staticChildren;

    private final Effect rebuildEffect;
    private final Seq<Effect> subscriptions = new Seq<>();

    private Layout() {
        this.spec = new LayoutSpec();

        this.group = new WidgetGroup() {
            {
                setTransform(true);
            }

            @Override
            protected void setScene(arc.scene.Scene scene) {
                super.setScene(scene);
                if (scene == null) {
                    Layout.this.dispose();
                }
            }

            @Override
            public float getPrefWidth() {
                NodeSpec nodeSpec = spec;
                if (nodeSpec.getWidthMode() == SizeMode.FIXED) {
                    return nodeSpec.constrainWidth(nodeSpec.getFixedWidth());
                }
                if (nodeSpec.getWidthMode() == SizeMode.GROW) {
                    return 0.0f;
                }
                float preferredWidth = LayoutEngine.prefWidth(nodeSpec, spec.isColumn(), spec.gap(), foregroundChildren());
                return nodeSpec.constrainWidth(preferredWidth);
            }

            @Override
            public float getPrefHeight() {
                NodeSpec nodeSpec = spec;
                if (nodeSpec.getHeightMode() == SizeMode.FIXED) {
                    return nodeSpec.constrainHeight(nodeSpec.getFixedHeight());
                }
                if (nodeSpec.getHeightMode() == SizeMode.GROW) {
                    return 0.0f;
                }
                float preferredHeight = LayoutEngine.prefHeight(nodeSpec, spec.isColumn(), spec.gap(), foregroundChildren());
                return nodeSpec.constrainHeight(preferredHeight);
            }

            @Override
            public void layout() {
                NodeSpec nodeSpec = spec;
                float containerWidth = getWidth();
                float containerHeight = getHeight();

                if (background != null) {
                    Element bgEl = background.element();
                    bgEl.setSize(containerWidth, containerHeight);
                    bgEl.setPosition(0, 0);
                }

                float layoutWidth = Math.max(0.0f, containerWidth - nodeSpec.getHorizontalPadding());
                float layoutHeight = Math.max(0.0f, containerHeight - nodeSpec.getVerticalPadding());

                LayoutEngine.layout(spec, foregroundChildren(), nodeSpec.getPaddingLeft(), nodeSpec.getPaddingBottom(),
                    layoutWidth, layoutHeight);
            }
        };

        group.userObject = this;
        spec.onInvalidate(group::invalidateHierarchy);

        this.rebuildEffect = new Effect(() -> {
            Seq<Component> newChildren = childrenProvider.get();
            rebuild(newChildren);
        });
        subscriptions.add(rebuildEffect);
    }

    private Seq<Element> foregroundChildren() {
        Seq<Element> els = new Seq<>();
        for (int i = 0; i < currentChildren.size; i++) {
            els.add(currentChildren.get(i).element());
        }
        return els;
    }

    private void rebuild(Seq<Component> newChildren) {
        java.util.Set<Component> newSet = new java.util.HashSet<>();
        for (int i = 0; i < newChildren.size; i++) {
            newSet.add(newChildren.get(i));
        }
        for (int i = 0; i < currentChildren.size; i++) {
            Component oldChild = currentChildren.get(i);
            if (!newSet.contains(oldChild)) {
                oldChild.dispose();
            }
        }

        currentChildren.clear();
        currentChildren.addAll(newChildren);

        group.clearChildren();
        if (background != null) {
            group.addChild(background.element());
        }
        for (int i = 0; i < newChildren.size; i++) {
            group.addChild(newChildren.get(i).element());
        }

        group.invalidateHierarchy();
    }

    public static Layout of() {
        return new Layout();
    }

    public Layout background(Component bg) {
        if (this.background != null && this.background != bg) {
            this.background.dispose();
        }
        this.background = bg;
        rebuildEffect.run();
        return this;
    }

    public Layout child(Component child) {
        staticChildren.add(child);
        rebuildEffect.run();
        return this;
    }

    public Layout children(arc.func.Prov<Seq<Component>> provider) {
        this.childrenProvider = provider;
        rebuildEffect.run();
        return this;
    }

    public Layout style(Cons<LayoutSpec> configurator) {
        Effect styleEffect = new Effect(() -> {
            configurator.get(spec);
            group.invalidateHierarchy();
        });
        subscriptions.add(styleEffect);
        return this;
    }

    @Override
    public Element element() {
        return group;
    }

    @Override
    public NodeSpec sizing() {
        return spec;
    }

    @Override
    public void dispose() {
        subscriptions.each(Effect::dispose);
        subscriptions.clear();
        if (background != null) background.dispose();
        for (int i = 0; i < currentChildren.size; i++) {
            currentChildren.get(i).dispose();
        }
    }
}
