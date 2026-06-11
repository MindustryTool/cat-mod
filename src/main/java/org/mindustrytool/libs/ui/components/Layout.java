package org.mindustrytool.libs.ui.components;

import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Seq;

import org.mindustrytool.libs.ui.component.Component;
import org.mindustrytool.libs.ui.component.EffectHost;
import org.mindustrytool.libs.ui.element.ScrollElement;
import org.mindustrytool.libs.ui.layout.LayoutEngine;
import org.mindustrytool.libs.ui.layout.LayoutSpec;
import org.mindustrytool.libs.ui.layout.NodeSpec;
import org.mindustrytool.libs.ui.layout.NodeSpec.SizeMode;

import arc.func.Cons;
import arc.func.Prov;

/**
 * Flexbox-based container that can host child {@link Component}s, an optional background,
 * and optional scroll behaviour.
 *
 * <p><b>Style effects accumulate</b> — each call to {@link #style(Cons)} registers a new
 * reactive configurator. This allows composing orthogonal concerns:
 * <pre>{@code
 * Layout.of()
 *     .style(s -> { s.reset(false); s.column().gap(8f); })
 *     .style(s -> s.padding(16f))
 *     .scrollY();
 * }</pre>
 *
 * <p>Call {@link LayoutSpec#reset(boolean)} at the start of a configurator when you want to
 * start from default values rather than building on top of previous configurator state.
 *
 * <p><b>Scroll:</b> enabled via {@link #scrollY()} / {@link #scrollX()} / {@link #scroll()}.
 * Supports fling physics and an auto-hiding scroll-bar rendered using {@link CustomComponent}.
 */
public class Layout implements Component {

    // ─── Fields ──────────────────────────────────────────────────────────────

    private final LayoutSpec spec;
    private final ScrollElement group;
    private final WidgetGroup contentGroup;
    private final EffectHost effects = new EffectHost();

    private Component background;
    private final Seq<Component> staticChildren = new Seq<>();
    private final Seq<Component> currentChildren = new Seq<>();
    private Prov<Seq<Component>> childrenProvider = () -> staticChildren;


    // ─── Scroll state ─────────────────────────────────────────────────────────

    private boolean scrollableX;
    private boolean scrollableY;
    private float scrollX;
    private float scrollY;
    private float maxScrollX;
    private float maxScrollY;
    private float velocityX;
    private float velocityY;




    // ─── Touch tracking ───────────────────────────────────────────────────────

    private float touchStartX;
    private float touchStartY;
    private float prevTouchX;
    private float prevTouchY;
    private float prevTouchTime;
    private float instantVelocityX;
    private float instantVelocityY;


    // ─── Constructor ─────────────────────────────────────────────────────────

    private Layout() {
        spec = new LayoutSpec();

        contentGroup = new WidgetGroup() {
            { setTransform(true); }

            @Override
            public float getPrefWidth() {
                NodeSpec nodeSpec = spec;
                if (nodeSpec.getWidthMode() == SizeMode.FIXED)
                    return nodeSpec.constrainWidth(nodeSpec.getFixedWidth());
                if (nodeSpec.getWidthMode() == SizeMode.GROW)
                    return 0f;
                return nodeSpec.constrainWidth(
                    LayoutEngine.prefWidth(nodeSpec, spec.isColumn(), spec.getGap(), foregroundElements()));
            }

            @Override
            public float getPrefHeight() {
                NodeSpec nodeSpec = spec;
                if (nodeSpec.getHeightMode() == SizeMode.FIXED)
                    return nodeSpec.constrainHeight(nodeSpec.getFixedHeight());
                if (nodeSpec.getHeightMode() == SizeMode.GROW)
                    return 0f;
                return nodeSpec.constrainHeight(
                    LayoutEngine.prefHeight(nodeSpec, spec.isColumn(), spec.getGap(), foregroundElements()));
            }

            @Override
            public void layout() {
                float containerWidth = getWidth();
                float containerHeight = getHeight();

                if (background != null) {
                    background.element().setSize(containerWidth, containerHeight);
                    background.element().setPosition(0f, 0f);
                }

                float layoutWidth = Math.max(0f, containerWidth - spec.getHorizontalPadding());
                float layoutHeight = Math.max(0f, containerHeight - spec.getVerticalPadding());

                LayoutEngine.layout(spec, foregroundElements(),
                    spec.getPaddingLeft(), spec.getPaddingBottom(),
                    layoutWidth, layoutHeight);
            }
        };

        group = new ScrollElement(contentGroup) {
            @Override
            protected void setScene(arc.scene.Scene scene) {
                super.setScene(scene);
                if (scene == null) Layout.this.dispose();
            }
        };

        // Scroll defaults: disabled on both axes by default
        group.getX().setDisabled(true);
        group.getY().setDisabled(true);

        group.userObject = this;
        spec.onInvalidate(contentGroup::invalidateHierarchy);

        effects.add(() -> rebuild(childrenProvider.get()));
    }


    // ─── Factory ─────────────────────────────────────────────────────────────

    public static Layout of() {
        return new Layout();
    }


    // ─── Child management ─────────────────────────────────────────────────────

    public Layout background(Component bg) {
        if (background != null && background != bg) background.dispose();
        background = bg;
        triggerRebuild();
        return this;
    }


    public Layout child(Component child) {
        staticChildren.add(child);
        triggerRebuild();
        return this;
    }


    public Layout children(Prov<Seq<Component>> provider) {
        childrenProvider = provider;
        triggerRebuild();
        return this;
    }


    // ─── Style ───────────────────────────────────────────────────────────────

    /**
     * Registers a reactive layout style configurator.
     *
     * <p>Each call adds a new {@link org.mindustrytool.libs.signal.Effect} — configurators
     * accumulate and are re-run in registration order when their signal dependencies change.
     * Call {@link LayoutSpec#reset(boolean)} inside the configurator to start from defaults.
     */
    public Layout style(Cons<LayoutSpec> configurator) {
        effects.add(() -> {
            configurator.get(spec);
            group.invalidateHierarchy();
        });
        return this;
    }


    // ─── Scroll API ───────────────────────────────────────────────────────────

    /** Enables vertical scrolling. Content outside the group bounds is scissor-clipped. */
    public Layout scrollY() {
        group.getY().setDisabled(false);
        return this;
    }


    /** Enables horizontal scrolling. Content outside the group bounds is scissor-clipped. */
    public Layout scrollX() {
        group.getX().setDisabled(false);
        return this;
    }


    /** Enables both axes of scrolling. */
    public Layout scroll() {
        group.getX().setDisabled(false);
        group.getY().setDisabled(false);
        return this;
    }


    // ─── Component interface ─────────────────────────────────────────────────

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
        effects.disposeAll();
        if (background != null) background.dispose();
        for (int i = 0; i < currentChildren.size; i++) currentChildren.get(i).dispose();
    }





    // ─── Private helpers ─────────────────────────────────────────────────────

    private Seq<Element> foregroundElements() {
        Seq<Element> elements = new Seq<>(currentChildren.size);
        for (int i = 0; i < currentChildren.size; i++) elements.add(currentChildren.get(i).element());
        return elements;
    }


    private void triggerRebuild() {
        // Force the rebuildEffect to re-run immediately (the effect itself reads childrenProvider)
        rebuild(childrenProvider.get());
    }


    private void rebuild(Seq<Component> newChildren) {
        java.util.Set<Component> newSet = new java.util.HashSet<>();
        for (int i = 0; i < newChildren.size; i++) newSet.add(newChildren.get(i));

        for (int i = 0; i < currentChildren.size; i++) {
            Component oldChild = currentChildren.get(i);
            if (!newSet.contains(oldChild)) oldChild.dispose();
        }

        currentChildren.clear();
        currentChildren.addAll(newChildren);

        contentGroup.clearChildren();
        if (background != null) contentGroup.addChild(background.element());
        for (int i = 0; i < newChildren.size; i++) contentGroup.addChild(newChildren.get(i).element());

        contentGroup.invalidateHierarchy();
        group.invalidateHierarchy();
    }
}
