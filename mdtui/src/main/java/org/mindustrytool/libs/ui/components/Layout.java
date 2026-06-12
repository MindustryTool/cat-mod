package org.mindustrytool.libs.ui.components;

import arc.func.Cons;
import arc.func.Prov;
import arc.scene.Element;
import arc.scene.Scene;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Seq;

import org.mindustrytool.libs.ui.core.EffectHost;
import org.mindustrytool.libs.ui.element.ScrollElement;
import org.mindustrytool.libs.ui.layout.LayoutEngine;
import org.mindustrytool.libs.ui.layout.LayoutSpec;
import org.mindustrytool.libs.ui.layout.NodeSpec;
import org.mindustrytool.libs.ui.layout.NodeSpec.SizeMode;

import java.util.HashSet;
import java.util.Set;

/**
 * Flexbox-based container that can host child {@link Component}s and an optional background.
 * It uses a {@link ScrollElement} root group to support scrolling.
 *
 * <p>Style effects accumulate — each call to {@link #style(Cons)} registers a new
 * reactive configurator. This allows composing orthogonal concerns:
 * <pre>{@code
 * Layout.of()
 *     .style(s -> { s.reset(false); s.column().gap(8f); })
 *     .style(s -> s.padding(16f));
 * }</pre>
 *
 * <p>Call {@link LayoutSpec#reset(boolean)} at the start of a configurator when you want to
 * start from default values rather than building on top of previous configurator state.
 */
public class Layout implements Component {

    private final LayoutSpec spec;
    private final ScrollElement group;
    private final WidgetGroup contentGroup;
    public final Style style;

    private final EffectHost effects = new EffectHost();

    private Component background;
    private final Seq<Component> currentChildren = new Seq<>();

    private Layout() {
        spec = new LayoutSpec();

        contentGroup = new WidgetGroup() {
            {
                setTransform(true);
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
            protected void setScene(Scene scene) {
                boolean hadScene = getScene() != null;
                super.setScene(scene);
                if (hadScene && scene == null) Layout.this.dispose();
            }
        };

        // Scroll defaults: disabled on both axes by default
        group.getX().setDisabled(true);
        group.getY().setDisabled(true);

        group.userObject = this;
        spec.onInvalidate(contentGroup::invalidateHierarchy);
        style = new Style();
    }

    /** Creates a new empty Layout. */
    public static Layout of() {
        return new Layout();
    }

    /**
     * Sets the background component. Disposes the previous background if
     * different from the new one.
     */
    public Layout background(Component newBackground) {
        if (background != null && background != newBackground) background.dispose();
        background = newBackground;
        rebuild(currentChildren);
        return this;
    }

    /**
     * Registers a reactive children provider. The provider is re-evaluated
     * whenever its tracked signal dependencies change, causing the layout
     * to rebuild its child list.
     */
    public Layout children(Prov<Seq<Component>> provider) {
        effects.add(() -> rebuild(provider.get()));
        return this;
    }

    /**
     * Registers a reactive layout style configurator.
     *
     * <p>Each call adds a new {@link org.mindustrytool.libs.signal.Effect} — configurators
     * accumulate and are re-run in registration order when their signal dependencies change.
     * Call {@link LayoutSpec#reset(boolean)} inside the configurator to start from defaults.
     */
    public Layout style(Cons<Style> configurator) {
        effects.add(() -> {
            configurator.get(style);
            group.invalidateHierarchy();
        });
        return this;
    }

    /**
     * Builder-style configurator class to set properties on the underlying elements.
     * Inherits from ContainerStyle to provide flexbox layout options.
     */
    public class Style extends ContainerStyle<Style> {
        @Override
        protected NodeSpec sizing() {
            return spec;
        }

        @Override
        protected Element styledElement() {
            return group;
        }

        @Override
        protected LayoutSpec layoutSpec() {
            return spec;
        }

        /** Enable or disable horizontal scrolling. */
        public Style scrollX(boolean scrollable) {
            group.getX().setDisabled(!scrollable);
            return this;
        }

        /** Enable or disable vertical scrolling. */
        public Style scrollY(boolean scrollable) {
            group.getY().setDisabled(!scrollable);
            return this;
        }

        /** Enable or disable scrolling on both axes. */
        public Style scroll(boolean scrollX, boolean scrollY) {
            group.getX().setDisabled(!scrollX);
            group.getY().setDisabled(!scrollY);
            return this;
        }

        /** Enable or disable scrollbar fading. */
        public Style fadeScrollBars(boolean fade) {
            group.setFadeScrollBars(fade);
            return this;
        }

        /** Enable or disable smooth scroll interpolation. */
        public Style smoothScrolling(boolean smooth) {
            group.setSmoothScrolling(smooth);
            return this;
        }

        /** Enable or disable viewport clipping. */
        public Style clip(boolean clip) {
            group.setClip(clip);
            return this;
        }
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
        effects.disposeAll();
        if (background != null) background.dispose();
        for (int i = 0; i < currentChildren.size; i++) currentChildren.get(i).dispose();
    }

    private Seq<Element> foregroundElements() {
        return currentChildren.map(Component::element);
    }

    private void rebuild(Seq<Component> newChildren) {
        Set<Component> newSet = new HashSet<>();
        for (var newComponent : newChildren) newSet.add(newComponent);

        for (var currentComponent : currentChildren)
            if (!newSet.contains(currentComponent)) currentComponent.dispose();

        currentChildren.clear();
        currentChildren.addAll(newChildren);

        contentGroup.clearChildren();
        if (background != null) contentGroup.addChild(background.element());
        for (var component : newChildren) contentGroup.addChild(component.element());

        contentGroup.invalidateHierarchy();
        group.invalidateHierarchy();
    }
}
