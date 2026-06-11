package org.mindustrytool.libs.ui.components;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.input.KeyCode;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Seq;

import org.mindustrytool.libs.ui.component.Component;
import org.mindustrytool.libs.ui.component.EffectHost;
import org.mindustrytool.libs.ui.core.CustomComponent;
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

    private static final float SCROLL_BAR_WIDTH = 4f;
    private static final float SCROLL_BAR_PADDING = 2f;
    private static final float SCROLL_FRICTION = 6f;       // exponential decay coefficient
    private static final float OVERSCROLL_SPRING = 12f;    // spring-back stiffness


    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    // ─── Fields ──────────────────────────────────────────────────────────────

    private final LayoutSpec spec;
    private final WidgetGroup group;
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

        group = new WidgetGroup() {
            { setTransform(true); }

            @Override
            protected void setScene(arc.scene.Scene scene) {
                super.setScene(scene);
                if (scene == null) Layout.this.dispose();
            }

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
            public void act(float delta) {
                super.act(delta);
                if (scrollableX || scrollableY) updateScrollPhysics(delta);
            }

            @Override
            public void draw() {
                if (scrollableX || scrollableY) {
                    Draw.flush();
                    if (clipBegin(0, 0, getWidth(), getHeight())) {
                        validate();
                        drawChildren();
                        Draw.flush();
                        clipEnd();
                    }
                } else {
                    super.draw();
                }
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

                if (scrollableX || scrollableY) {
                    applyScrollOffset(containerWidth, containerHeight);
                }
            }
        };

        group.userObject = this;
        spec.onInvalidate(group::invalidateHierarchy);

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
        if (scrollableY) return this;
        scrollableY = true;
        addScrollTouchListener();
        return this;
    }


    /** Enables horizontal scrolling. Content outside the group bounds is scissor-clipped. */
    public Layout scrollX() {
        if (scrollableX) return this;
        scrollableX = true;
        addScrollTouchListener();
        return this;
    }


    /** Enables both axes of scrolling. */
    public Layout scroll() {
        return scrollY().scrollX();
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


    private boolean touchListenerAdded = false;

    private void addScrollTouchListener() {
        if (touchListenerAdded) return;
        touchListenerAdded = true;

        group.addListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                if (pointer != 0) return false;
                touchStartX = prevTouchX = x;
                touchStartY = prevTouchY = y;
                prevTouchTime = arc.Core.graphics.getDeltaTime();
                instantVelocityX = instantVelocityY = 0f;
                velocityX = velocityY = 0f;
                return true;
            }


            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (pointer != 0) return;
                float currentTime = arc.Core.graphics.getDeltaTime();
                float dt = currentTime - prevTouchTime;

                if (dt > 0f) {
                    instantVelocityX = -(x - prevTouchX) / dt;
                    instantVelocityY = -(y - prevTouchY) / dt;
                }

                if (scrollableX) scrollX = clamp(scrollX - (x - prevTouchX), -50f, maxScrollX + 50f);
                if (scrollableY) scrollY = clamp(scrollY - (y - prevTouchY), -50f, maxScrollY + 50f);

                prevTouchX = x;
                prevTouchY = y;
                prevTouchTime = currentTime;
                group.invalidate();
            }


            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                if (pointer != 0) return;
                velocityX = instantVelocityX;
                velocityY = instantVelocityY;
            }
        });
    }


    private void updateScrollPhysics(float delta) {
        // Apply fling velocity
        if (scrollableX) scrollX += velocityX * delta;
        if (scrollableY) scrollY += velocityY * delta;

        // Exponential friction
        float friction = (float) Math.pow(0.01, delta * SCROLL_FRICTION);
        velocityX *= friction;
        velocityY *= friction;

        // Spring-back when past edges
        if (scrollableX) {
            if (scrollX < 0f)         scrollX += (-scrollX)         * delta * OVERSCROLL_SPRING;
            if (scrollX > maxScrollX) scrollX -= (scrollX - maxScrollX) * delta * OVERSCROLL_SPRING;
        }
        if (scrollableY) {
            if (scrollY < 0f)         scrollY += (-scrollY)         * delta * OVERSCROLL_SPRING;
            if (scrollY > maxScrollY) scrollY -= (scrollY - maxScrollY) * delta * OVERSCROLL_SPRING;
        }

        // Snap to zero velocity when negligible
        if (Math.abs(velocityX) < 1f) velocityX = 0f;
        if (Math.abs(velocityY) < 1f) velocityY = 0f;

        group.invalidate();
    }


    private void applyScrollOffset(float containerWidth, float containerHeight) {
        Seq<Element> children = foregroundElements();

        // Measure natural content extent before applying scroll offset
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        for (int i = 0; i < children.size; i++) {
            Element child = children.get(i);
            minY = Math.min(minY, child.y);
            maxY = Math.max(maxY, child.y + child.getHeight());
            minX = Math.min(minX, child.x);
            maxX = Math.max(maxX, child.x + child.getWidth());
        }

        float contentHeight = (maxY == -Float.MAX_VALUE) ? 0f : maxY - minY;
        float contentWidth  = (maxX == -Float.MAX_VALUE) ? 0f : maxX - minX;

        float availableHeight = containerHeight - spec.getVerticalPadding();
        float availableWidth  = containerWidth  - spec.getHorizontalPadding();

        maxScrollY = Math.max(0f, contentHeight - availableHeight);
        maxScrollX = Math.max(0f, contentWidth  - availableWidth);

        scrollY = arc.math.Mathf.clamp(scrollY, 0f, maxScrollY);
        scrollX = arc.math.Mathf.clamp(scrollX, 0f, maxScrollX);

        // Shift children by scroll offset
        // In arc's Y-up system: scroll down (see content below) → children shift up (+y)
        for (int i = 0; i < children.size; i++) {
            Element child = children.get(i);
            if (scrollableY) child.y += scrollY;
            if (scrollableX) child.x -= scrollX;
        }

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

        group.clearChildren();
        if (background != null) group.addChild(background.element());
        for (int i = 0; i < newChildren.size; i++) group.addChild(newChildren.get(i).element());



        group.invalidateHierarchy();
    }
}
