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

public class InputField implements Component {

    public class Style extends ComponentStyle<Style> {
        public final arc.scene.ui.TextField.TextFieldStyle textFieldStyle;

        Style() {
            this.textFieldStyle = element.getStyle();
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

        public Style placeholder(String value) {
            element.setMessageText(value);
            return this;
        }

        public Style textColor(Color value) {
            textFieldStyle.fontColor = value;
            element.setStyle(textFieldStyle);
            return this;
        }

        public Style maxLength(int value) {
            element.setMaxLength(value);
            return this;
        }

        public Style passwordMode(boolean value) {
            element.setPasswordMode(value);
            return this;
        }

        public Style disabled(boolean value) {
            element.setDisabled(value);
            return this;
        }

        public Style size(Cons<NodeSpec> configurator) {
            configurator.get(sizing);
            return this;
        }
    }

    protected final NodeSpec sizing = new NodeSpec();
    protected final Seq<Effect> subscriptions = new Seq<>();

    private final arc.scene.ui.TextField element;
    public final Style style;

    private Effect styleEffect;
    private Effect sizeEffect;

    private InputField() {
        var baseStyle = scene.getStyle(arc.scene.ui.TextField.TextFieldStyle.class);
        var customStyle = new arc.scene.ui.TextField.TextFieldStyle(baseStyle);
        this.element = new arc.scene.ui.TextField("", customStyle) {
            @Override
            protected void setScene(Scene sceneInstance) {
                super.setScene(sceneInstance);
                if (sceneInstance == null) {
                    InputField.this.dispose();
                }
            }
        };
        element.userObject = this;
        this.style = new Style();
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    public static InputField of() {
        return new InputField();
    }

    public InputField style(Cons<Style> configurator) {
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

    public InputField size(Cons<NodeSpec> configurator) {
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
