package org.mindustrytool.libs.ui.components;

import arc.scene.Element;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Seq;

import org.mindustrytool.libs.ui.component.Component;
import org.mindustrytool.libs.ui.layout.LayoutEngine;
import org.mindustrytool.libs.ui.layout.NodeSizing;
import org.mindustrytool.libs.ui.layout.NodeSizing.SizeMode;
import org.mindustrytool.libs.ui.layout.LayoutSpec;

import arc.func.Cons;

/**
 * Layout is a container component that positions and dimensions its children
 * dynamically based on {@link LayoutSpec} and {@link LayoutEngine} calculations.
 */
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
                NodeSizing nodeSizing = spec.sizing;
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
                NodeSizing nodeSizing = spec.sizing;
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
                NodeSizing nodeSizing = spec.sizing;
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
     * Adds a child component to this container layout.
     *
     * @param child the child component to add
     * @return this layout instance for chaining
     */
    public Layout child(Component child) {
        children.add(child);
        group.addChild(child.element());
        return this;
    }

    /**
     * Configures the layout specification properties (e.g. justify, alignment, direction).
     *
     * @param configurator the layout spec configurator callback
     * @return this layout instance for chaining
     */
    public Layout style(Cons<LayoutSpec> configurator) {
        configurator.get(spec);
        group.invalidateHierarchy();
        return this;
    }

    /**
     * Configures the sizing properties of this layout container itself.
     *
     * @param configurator the node sizing configurator callback
     * @return this layout instance for chaining
     */
    public Layout size(Cons<NodeSizing> configurator) {
        configurator.get(spec.sizing);
        group.invalidateHierarchy();
        return this;
    }

    @Override
    public Element element() {
        return group;
    }

    @Override
    public NodeSizing sizing() {
        return spec.sizing();
    }

    @Override
    public void dispose() {
        for (Component child : children) {
            child.dispose();
        }
    }
}
