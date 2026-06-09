package org.mindustrytool.libs.ui.components;

import arc.scene.Element;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Seq;


import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.ui.component.Component;
import org.mindustrytool.libs.ui.layout.LayoutEngine;
import org.mindustrytool.libs.ui.layout.NodeSizing;
import org.mindustrytool.libs.ui.layout.NodeSizing.SizeMode;
import org.mindustrytool.libs.ui.layout.LayoutSpec;

import arc.func.Cons;

/**
 * Layout is a container component that positions and dimensions its children
 * dynamically based on {@link LayoutSpec} and {@link LayoutEngine} calculations.
 * It supports reactive dynamic children rebuilds and layout spec styling updates.
 */
public class Layout implements Component {
    private final LayoutSpec spec;
    private final WidgetGroup group;

    // Statically added child components via child() method
    private final Seq<Component> staticChildren = new Seq<>();
    // Currently active child components rendered in this layout
    private final Seq<Component> currentChildren = new Seq<>();
    // Lambda provider of the children, defaulting to the staticChildren list
    private arc.func.Prov<Seq<Component>> childrenProvider = () -> staticChildren;

    // Reactive effects managing layout rebuilds and style changes
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
                NodeSizing nodeSizing = spec;
                if (nodeSizing.getWidthMode() == SizeMode.FIXED) {
                    return nodeSizing.constrainWidth(nodeSizing.getFixedWidth());
                }
                if (nodeSizing.getWidthMode() == SizeMode.GROW) {
                    return 0.0f;
                }
                float preferredWidth = LayoutEngine.prefWidth(nodeSizing, spec.isColumn(), spec.gap(), getChildren());
                return nodeSizing.constrainWidth(preferredWidth);
            }

            @Override
            public float getPrefHeight() {
                NodeSizing nodeSizing = spec;
                if (nodeSizing.getHeightMode() == SizeMode.FIXED) {
                    return nodeSizing.constrainHeight(nodeSizing.getFixedHeight());
                }
                if (nodeSizing.getHeightMode() == SizeMode.GROW) {
                    return 0.0f;
                }
                float preferredHeight = LayoutEngine.prefHeight(nodeSizing, spec.isColumn(), spec.gap(), getChildren());
                return nodeSizing.constrainHeight(preferredHeight);
            }

            @Override
            public void layout() {
                NodeSizing nodeSizing = spec;
                float containerWidth = getWidth();
                float containerHeight = getHeight();
                float layoutWidth = Math.max(0.0f, containerWidth - nodeSizing.getHorizontalPadding());
                float layoutHeight = Math.max(0.0f, containerHeight - nodeSizing.getVerticalPadding());

                LayoutEngine.layout(spec, getChildren(), nodeSizing.getPaddingLeft(), nodeSizing.getPaddingBottom(),
                    layoutWidth, layoutHeight);
            }
        };

        group.userObject = this;
        spec.onInvalidate(group::invalidateHierarchy);

        // Track and automatically rebuild child components upon signal changes
        this.rebuildEffect = new Effect(() -> {
            Seq<Component> newChildren = childrenProvider.get();
            rebuild(newChildren);
        });
        subscriptions.add(rebuildEffect);
    }

    /**
     * Reconciles the new children list with the current children,
     * removing and disposing obsolete components, and updating the UI tree.
     */
    private void rebuild(Seq<Component> newChildren) {
        // Find and dispose old components that are not present in the new list
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

        // Sync the current children list
        currentChildren.clear();
        currentChildren.addAll(newChildren);

        // Sync the Arc UI Scene Graph group children
        group.clearChildren();
        for (int i = 0; i < newChildren.size; i++) {
            group.addChild(newChildren.get(i).element());
        }

        // Invalidate hierarchy to force recalculation of preferred sizes and repaint
        group.invalidateHierarchy();
    }

    /**
     * Factory method to create a new Layout instance.
     *
     * @return a new Layout component instance
     */
    public static Layout of() {
        return new Layout();
    }

    /**
     * Adds a static child component to this container layout.
     *
     * @param child the child component to add
     * @return this layout instance for chaining
     */
    public Layout child(Component child) {
        staticChildren.add(child);
        rebuildEffect.run();
        return this;
    }

    /**
     * Sets a reactive provider for the child components of this layout.
     * Any signal reads evaluated inside the provider will trigger automatic rebuilds on changes.
     *
     * @param provider the children provider callback returning a sequence of components
     * @return this layout instance for chaining
     */
    public Layout children(arc.func.Prov<Seq<Component>> provider) {
        this.childrenProvider = provider;
        rebuildEffect.run();
        return this;
    }

    /**
     * Configures the layout specification properties (e.g. justify, alignment, direction) reactively.
     *
     * @param configurator the layout spec configurator callback
     * @return this layout instance for chaining
     */
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
    public NodeSizing sizing() {
        return spec;
    }

    @Override
    public void dispose() {
        subscriptions.each(Effect::dispose);
        subscriptions.clear();
        for (int i = 0; i < currentChildren.size; i++) {
            currentChildren.get(i).dispose();
        }
    }
}
