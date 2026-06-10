package org.mindustrytool.libs.ui.components;

import arc.scene.Element;
import arc.scene.Scene;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.ui.component.AbstractComponent;
import org.mindustrytool.libs.ui.component.Component;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.layout.NodeSpec;
import org.mindustrytool.libs.ui.layout.NodeSpec.SizeMode;

import arc.func.Cons;

import static arc.Core.scene;

/**
 * ScrollPane is a container component that wraps a child component and allows scrolling (both vertical and horizontal).
 * It supports reactive custom configurations such as scrollbar behavior, smooth scrolling, and sizing.
 */
public class ScrollPane extends AbstractComponent {

    /**
     * Style builder for ScrollPane, supporting scrollbar visibility, fade, scroll directions, and sizing.
     */
    public class Style extends ComponentStyle<Style> {
        boolean fadeScrollBars;
        boolean scrollBarsOnTop;
        boolean disableX;
        boolean disableY;
        boolean overscroll = true;
        boolean smoothScrolling = true;
        boolean flickScroll = true;
        boolean clip = true;

        public final arc.scene.ui.ScrollPane.ScrollPaneStyle scrollPaneStyle;

        Style() {
            this.scrollPaneStyle = element.getStyle();
        }

        @Override
        protected NodeSpec sizing() {
            return sizing;
        }

        @Override
        protected Element styledElement() {
            return element;
        }

        /**
         * Sets whether scrollbars should automatically fade out when not in use.
         *
         * @param value true to fade scrollbars, false to show them permanently
         * @return this style builder instance
         */
        public Style fadeScrollBars(boolean value) {
            fadeScrollBars = value;
            if (value) {
                element.setupFadeScrollBars(0.5f, 2.0f);
            }
            if (value && scrollBarsOnTop) {
                // Do nothing if scrollbars are on top and fading is enabled
                return this;
            } else {
                element.setFadeScrollBars(value);
            }
            return this;
        }

        /**
         * Sets whether scrollbars are positioned on top of the content layer instead of taking layout space.
         *
         * @param value true to show scrollbars on top
         * @return this style builder instance
         */
        public Style scrollBarsOnTop(boolean value) {
            scrollBarsOnTop = value;
            element.setScrollbarsOnTop(value);
            if (value && fadeScrollBars) {
                element.setFadeScrollBars(false);
            }
            return this;
        }

        /**
         * Sets whether horizontal scrolling is disabled.
         *
         * @param value true to disable horizontal scrolling
         * @return this style builder instance
         */
        public Style disableX(boolean value) {
            disableX = value;
            element.setScrollingDisabledX(value);
            return this;
        }

        /**
         * Sets whether vertical scrolling is disabled.
         *
         * @param value true to disable vertical scrolling
         * @return this style builder instance
         */
        public Style disableY(boolean value) {
            disableY = value;
            element.setScrollingDisabledY(value);
            return this;
        }

        /**
         * Sets whether overscroll (elastic boundary bounce) is enabled.
         *
         * @param value true to enable overscroll
         * @return this style builder instance
         */
        public Style overscroll(boolean value) {
            overscroll = value;
            element.setOverscroll(value, value);
            return this;
        }

        /**
         * Sets whether smooth scrolling (lerping to target scroll position) is enabled.
         *
         * @param value true to enable smooth scrolling
         * @return this style builder instance
         */
        public Style smoothScrolling(boolean value) {
            smoothScrolling = value;
            element.setSmoothScrolling(value);
            return this;
        }

        /**
         * Sets whether flick-to-scroll (kinetic scrolling) is enabled.
         *
         * @param value true to enable flick scrolling
         * @return this style builder instance
         */
        public Style flickScroll(boolean value) {
            flickScroll = value;
            element.setFlickScroll(value);
            return this;
        }

        /**
         * Sets whether clipping is enabled to hide children components extending past scroll pane borders.
         *
         * @param value true to enable clipping
         * @return this style builder instance
         */
        public Style clip(boolean value) {
            clip = value;
            element.setClip(value);
            return this;
        }

        /**
         * Configures layout sizing.
         *
         * @param configurator the node sizing configurator callback
         * @return this style builder instance
         */
        public Style size(Cons<NodeSpec> configurator) {
            configurator.get(sizing);
            applySize();
            return this;
        }
    }

    public final Style style;
    private final arc.scene.ui.ScrollPane element;
    private Component childComponent;

    private Effect styleEffect;
    private Effect sizeEffect;

    private ScrollPane() {
        arc.scene.ui.ScrollPane.ScrollPaneStyle scrollPaneStyle =
            new arc.scene.ui.ScrollPane.ScrollPaneStyle(scene.getStyle(arc.scene.ui.ScrollPane.ScrollPaneStyle.class));
        this.element = new arc.scene.ui.ScrollPane(new arc.scene.ui.Label(""), scrollPaneStyle) {
            @Override
            protected void setScene(Scene sceneInstance) {
                super.setScene(sceneInstance);
                if (sceneInstance == null) {
                    ScrollPane.this.dispose();
                }
            }
        };
        element.userObject = this;
        this.style = new Style();
        sizing.onInvalidate(() -> {
            applySize();
            element.invalidateHierarchy();
        });
    }

    /**
     * Factory method to create a new ScrollPane instance.
     *
     * @return a new ScrollPane component instance
     */
    public static ScrollPane of() {
        return new ScrollPane();
    }

    /**
     * Sets the child component contained inside the scrollable view.
     *
     * @param child the scrollable child component
     * @return this scroll pane instance for chaining
     */
    public ScrollPane child(Component child) {
        this.childComponent = child;
        element.setWidget(child.element());
        return this;
    }

    /**
     * Configures the scroll pane style properties reactively.
     *
     * @param configurator the style configurator callback
     * @return this scroll pane instance for chaining
     */
    public ScrollPane style(Cons<Style> configurator) {
        if (styleEffect != null) {
            styleEffect.dispose();
            subscriptions.remove(styleEffect);
        }
        styleEffect = new Effect(() -> {
            configurator.get(style);
            element.invalidateHierarchy();
        });
        subscriptions.add(styleEffect);
        return this;
    }

    /**
     * Configures the scroll pane sizing constraints reactively.
     *
     * @param configurator the sizing configurator callback
     * @return this scroll pane instance for chaining
     */
    public ScrollPane size(Cons<NodeSpec> configurator) {
        if (sizeEffect != null) {
            sizeEffect.dispose();
            subscriptions.remove(sizeEffect);
        }
        sizeEffect = new Effect(() -> {
            configurator.get(sizing);
            element.invalidateHierarchy();
        });
        subscriptions.add(sizeEffect);
        return this;
    }

    private void applySize() {
        if (sizing.getWidthMode() == SizeMode.FIXED) {
            element.setWidth(sizing.getFixedWidth());
        }
        if (sizing.getHeightMode() == SizeMode.FIXED) {
            element.setHeight(sizing.getFixedHeight());
        }
    }

    @Override
    public Element element() {
        return element;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (childComponent != null) {
            childComponent.dispose();
        }
    }
}
