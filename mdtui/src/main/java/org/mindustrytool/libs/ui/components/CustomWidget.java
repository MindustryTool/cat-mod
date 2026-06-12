package org.mindustrytool.libs.ui.components;

import arc.graphics.Color;
import arc.graphics.Texture;
import arc.scene.Element;
import arc.scene.Scene;

import lombok.Builder;

import org.mindustrytool.libs.ui.widget.ElementNode;
import org.mindustrytool.libs.ui.widget.Widget;
import org.mindustrytool.libs.ui.layout.LayoutSpec;

@Builder(toBuilder = true)
public record CustomWidget(
    LayoutSpec layoutSpec,
    BackgroundMode backgroundMode,
    float topLeftRadius,
    float topRightRadius,
    float bottomRightRadius,
    float bottomLeftRadius,
    Color fillColor,
    Gradient gradient0,
    Gradient gradient1,
    Gradient gradient2,
    Gradient gradient3,
    Texture fillTexture,
    float uvScaleX,
    float uvScaleY,
    float uvOffsetX,
    float uvOffsetY,
    float borderWidth,
    Color borderColor,
    int borderStyle,
    float dashLength,
    float dashRatio,
    float innerShadowSpread,
    float innerShadowBlur,
    Color innerShadowColor,
    float glowSpread,
    Color glowColor,
    float opacity,
    int backdropIterations,
    float backdropBlend,
    float backdropWeight,
    float backdropMinAlpha,
    int filterMode,
    float filterAmount,
    float noiseAmount
) implements Widget {

    public enum BackgroundMode { SOLID, GRADIENT, TEXTURE, BACKDROP }

    public static class CustomWidgetBuilder {
        private LayoutSpec layoutSpec = LayoutSpec.defaultSpec();
        private BackgroundMode backgroundMode = BackgroundMode.SOLID;
        private float topLeftRadius = 8f;
        private float topRightRadius = 8f;
        private float bottomRightRadius = 8f;
        private float bottomLeftRadius = 8f;
        private float uvScaleX = 1f;
        private float uvScaleY = 1f;
        private float uvOffsetX = 0f;
        private float uvOffsetY = 0f;
        private float borderWidth = 0f;
        private int borderStyle = 0;
        private float dashLength = 10f;
        private float dashRatio = 0.5f;
        private float innerShadowSpread = 0f;
        private float innerShadowBlur = 0f;
        private float glowSpread = 0f;
        private float opacity = 1f;
        private int backdropIterations = 0;
        private float backdropBlend = 0.8f;
        private float backdropWeight = 0.8f;
        private float backdropMinAlpha = 0.8f;
        private int filterMode = 0;
        private float filterAmount = 0f;
        private float noiseAmount = 0f;
    }

    public CustomWidget() {
        this(LayoutSpec.defaultSpec(), BackgroundMode.SOLID, 8f, 8f, 8f, 8f,
             null, null, null, null, null, null, 1f, 1f, 0f, 0f, 0f, null, 0,
             10f, 0.5f, 0f, 0f, null, 0f, null, 1f, 0, 0.8f, 0.8f, 0.8f, 0, 0f, 0f);
    }

    @Override
    public LayoutSpec getLayoutSpec() {
        return layoutSpec;
    }

    @Override
    public ElementNode createElement() {
        return new CustomElementNode(this);
    }
}

class CustomElementNode extends ElementNode {
    private final CustomDraw drawer = new CustomDraw();

    CustomElementNode(CustomWidget widget) {
        super(widget);
        arcElement = new Element() {
            {
                userObject = CustomElementNode.this;
            }
            @Override
            public float getPrefWidth() {
                float fw = sizing().getFixedWidth();
                if (fw > 0f) return fw;
                return 0f;
            }

            @Override
            public float getPrefHeight() {
                float fh = sizing().getFixedHeight();
                if (fh > 0f) return fh;
                return 0f;
            }

            @Override
            protected void setScene(Scene scene) {
                boolean hadScene = getScene() != null;
                super.setScene(scene);
                if (hadScene && scene == null) CustomElementNode.this.dispose();
            }

            @Override
            public void draw() {
                float w = getWidth(), h = getHeight();
                if (w <= 0f || h <= 0f) return;
                drawer.draw(x, y, w, h, (CustomWidget) widget);
            }
        };
    }

    @Override
    public void mount(ElementNode parent) {
    }

    @Override
    public void update(Widget newWidget) {
        super.update(newWidget);
        arcElement.invalidateHierarchy();
    }

    @Override
    public void dispose() {
        drawer.dispose();
        super.dispose();
    }
}
