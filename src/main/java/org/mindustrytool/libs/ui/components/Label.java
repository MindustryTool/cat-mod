package org.mindustrytool.libs.ui.components;

import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.Scene;
import arc.struct.Seq;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.ui.component.Component;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.layout.NodeSpec;

import arc.func.Cons;

import static arc.Core.scene;

public class Label implements Component {

    public class Style extends ComponentStyle<Style> {
        public final arc.scene.ui.Label.LabelStyle labelStyle;
        Color textColor;
        int textAlign;
        float fontScale = 1.0f;
        boolean wrap;

        Style() {
            this.labelStyle = element.getStyle();
        }

        @Override
        protected NodeSpec sizing() {
            return sizing;
        }

        @Override
        protected Element styledElement() {
            return element;
        }

        public Style text(String value) {
            element.setText(value);
            return this;
        }

        public Style textColor(Color value) {
            this.textColor = value;
            this.labelStyle.fontColor = value;
            element.setStyle(labelStyle);
            return this;
        }

        public Style textAlign(int value) {
            this.textAlign = value;
            element.setAlignment(value);
            return this;
        }

        public Style fontScale(float value) {
            this.fontScale = value;
            element.setFontScale(value);
            return this;
        }

        public Style wrap(boolean value) {
            this.wrap = value;
            element.setWrap(value);
            return this;
        }

        public Style size(Cons<NodeSpec> configurator) {
            configurator.get(sizing);
            return this;
        }
    }

    protected final NodeSpec sizing = new NodeSpec();
    protected final Seq<Effect> subscriptions = new Seq<>();

    private final arc.scene.ui.Label element;
    public final Style style;

    private Effect styleEffect;
    private Effect sizeEffect;

    private Label() {
        var arcStyle = new arc.scene.ui.Label.LabelStyle(scene.getStyle(arc.scene.ui.Label.LabelStyle.class));
        this.element = new arc.scene.ui.Label("", arcStyle) {
            @Override
            protected void setScene(Scene sceneInstance) {
                super.setScene(sceneInstance);
                if (sceneInstance == null) {
                    Label.this.dispose();
                }
            }
        };
        element.userObject = this;
        this.style = new Style();
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    public static Label of() {
        return new Label();
    }

    public Label style(Cons<Style> configurator) {
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

    public Label size(Cons<NodeSpec> configurator) {
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
    }
}
