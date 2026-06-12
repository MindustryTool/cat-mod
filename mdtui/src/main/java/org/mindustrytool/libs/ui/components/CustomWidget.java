package org.mindustrytool.libs.ui.components;

import arc.graphics.Color;
import arc.graphics.Texture;
import arc.scene.Element;
import arc.scene.Scene;
import lombok.Builder;
import org.mindustrytool.libs.ui.widget.ElementNode;
import org.mindustrytool.libs.ui.widget.Widget;
import org.mindustrytool.libs.ui.layout.LayoutSpec;

/**
 * A declarative, immutable custom element widget backing high-performance shaders, custom borders,
 * shadow effects, glassmorphism blur, color filtering, and noise grains.
 *
 * @param layoutSpec         the layout rules and sizing constraints.
 * @param backgroundMode     the rendering mode of the background (SOLID, GRADIENT, TEXTURE, BACKDROP).
 * @param topLeftRadius      corner radius for top-left edge.
 * @param topRightRadius     corner radius for top-right edge.
 * @param bottomRightRadius  corner radius for bottom-right edge.
 * @param bottomLeftRadius   corner radius for bottom-left edge.
 * @param fillColor          solid color fill when backgroundMode is SOLID.
 * @param gradient0          first gradient stop configuration when backgroundMode is GRADIENT.
 * @param gradient1          second gradient stop configuration when backgroundMode is GRADIENT.
 * @param gradient2          third gradient stop configuration when backgroundMode is GRADIENT.
 * @param gradient3          fourth gradient stop configuration when backgroundMode is GRADIENT.
 * @param fillTexture        custom texture fill when backgroundMode is TEXTURE.
 * @param uvScaleX           horizontal texture UV scaling factor.
 * @param uvScaleY           vertical texture UV scaling factor.
 * @param uvOffsetX          horizontal texture UV offset position.
 * @param uvOffsetY          vertical texture UV offset position.
 * @param borderWidth        thickness of the element border.
 * @param borderColor        color of the element border.
 * @param borderStyle        style index of the border (e.g. solid or dashed).
 * @param dashLength         length of dashes when borderStyle represents dashed borders.
 * @param dashRatio          solid-to-gap ratio when borderStyle represents dashed borders.
 * @param innerShadowSpread  spread size of inner shadow.
 * @param innerShadowBlur    blur size of inner shadow.
 * @param innerShadowColor   color of inner shadow.
 * @param glowSpread         spread size of outer glow.
 * @param glowColor          color of outer glow.
 * @param opacity            alpha opacity channel.
 * @param backdropIterations count of backdrop blur passes when backgroundMode is BACKDROP.
 * @param backdropBlend      blend amount of backdrop blur.
 * @param backdropWeight     mix factor of blurred backdrop.
 * @param backdropMinAlpha   minimum backdrop opacity.
 * @param filterMode         color filter mode index (e.g. sepia, invert, grayscale).
 * @param filterAmount       strength amount of the color filter.
 * @param noiseAmount        grain amount of custom screen space noise overlay.
 */
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

    /**
     * Sizing rendering backgrounds modes.
     */
    public enum BackgroundMode {
        /** Fills background with a solid flat color. */
        SOLID,
        /** Fills background with multi-stop linear/radial gradients. */
        GRADIENT,
        /** Fills background with custom textures. */
        TEXTURE,
        /** Performs glassmorphism real-time capture and blur. */
        BACKDROP
    }

    /**
     * Custom builder specifying the default parameters for CustomWidget.
     */
    @SuppressWarnings({"unused", "FieldMayBeFinal"})
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

    /**
     * Default constructor creating a default CustomWidget.
     */
    public CustomWidget() {
        this(LayoutSpec.defaultSpec(), BackgroundMode.SOLID, 8f, 8f, 8f, 8f,
             null, null, null, null, null, null, 1f, 1f, 0f, 0f, 0f, null, 0,
             10f, 0.5f, 0f, 0f, null, 0f, null, 1f, 0, 0.8f, 0.8f, 0.8f, 0, 0f, 0f);
    }

    @Override
    public ElementNode createElement() {
        return new CustomElementNode(this);
    }
}

/**
 * Backing ElementNode that integrates custom shader drawing using {@link CustomDraw}.
 */
class CustomElementNode extends ElementNode {

    /**
     * The drawing helper used to render custom background and border styles.
     */
    private final CustomDraw drawer = new CustomDraw();

    /**
     * Constructs a custom element node.
     *
     * @param widget the custom widget blueprint configuration.
     */
    CustomElementNode(CustomWidget widget) {
        super(widget);
        
        this.arcElement = new Element() {
            {
                userObject = CustomElementNode.this;
            }
            
            @Override
            public float getPrefWidth() {
                float fw = sizing().getFixedWidth();
                
                if (fw > 0f) {
                    return fw;
                }
                
                return 0f;
            }

            @Override
            public float getPrefHeight() {
                float fh = sizing().getFixedHeight();
                
                if (fh > 0f) {
                    return fh;
                }
                
                return 0f;
            }

            @Override
            protected void setScene(Scene scene) {
                boolean hadScene = getScene() != null;
                super.setScene(scene);
                
                if (hadScene && scene == null) {
                    CustomElementNode.this.dispose();
                }
            }

            @Override
            public void draw() {
                float w = getWidth(), h = getHeight();
                
                if (w <= 0f || h <= 0f) {
                    return;
                }
                
                drawer.draw(x, y, w, h, (CustomWidget) widget);
            }
        };
    }

    @Override
    public LayoutSpec sizing() {
        return ((CustomWidget) widget).layoutSpec();
    }

    @Override
    public void mount(ElementNode parent) {
        // CustomWidget does not hold child elements; no mount logic required.
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
