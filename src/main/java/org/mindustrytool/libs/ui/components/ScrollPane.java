package org.mindustrytool.libs.ui.components;

import arc.scene.Element;
import arc.scene.Scene;
import arc.struct.Seq;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.ui.component.Component;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.layout.NodeSpec;
import org.mindustrytool.libs.ui.layout.NodeSpec.SizeMode;

import arc.func.Cons;

import static arc.Core.scene;

public class ScrollPane implements Component {

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

        public Style fadeScrollBars(boolean value) {
            fadeScrollBars = value;
            if (value) {
                element.setupFadeScrollBars(0.5f, 2.0f);
            }
            if (value && scrollBarsOnTop) {
                return this;
            } else {
                element.setFadeScrollBars(value);
            }
            return this;
        }

        public Style scrollBarsOnTop(boolean value) {
            scrollBarsOnTop = value;
            element.setScrollbarsOnTop(value);
            if (value && fadeScrollBars) {
                element.setFadeScrollBars(false);
            }
            return this;
        }

        public Style disableX(boolean value) {
            disableX = value;
            element.setScrollingDisabledX(value);
            return this;
        }

        public Style disableY(boolean value) {
            disableY = value;
            element.setScrollingDisabledY(value);
            return this;
        }

        public Style overscroll(boolean value) {
            overscroll = value;
            element.setOverscroll(value, value);
            return this;
        }

        public Style smoothScrolling(boolean value) {
            smoothScrolling = value;
            element.setSmoothScrolling(value);
            return this;
        }

        public Style flickScroll(boolean value) {
            flickScroll = value;
            element.setFlickScroll(value);
            return this;
        }

        public Style clip(boolean value) {
            clip = value;
            element.setClip(value);
            return this;
        }

        public Style size(Cons<NodeSpec> configurator) {
            configurator.get(sizing);
            applySize();
            return this;
        }
    }

    protected final NodeSpec sizing = new NodeSpec();
    protected final Seq<Effect> subscriptions = new Seq<>();

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

    public static ScrollPane of() {
        return new ScrollPane();
    }

    public ScrollPane child(Component child) {
        this.childComponent = child;
        element.setWidget(child.element());
        return this;
    }

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
    public NodeSpec sizing() {
        return sizing;
    }

    @Override
    public void dispose() {
        subscriptions.each(Effect::dispose);
        subscriptions.clear();
        if (childComponent != null) {
            childComponent.dispose();
        }
    }
}
