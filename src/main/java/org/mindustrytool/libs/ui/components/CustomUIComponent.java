package org.mindustrytool.libs.ui.components;

import arc.graphics.Color;
import arc.graphics.Texture;
import arc.scene.Element;
import arc.scene.Scene;
import arc.scene.event.Touchable;
import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.ui.animation.Ease;
import org.mindustrytool.libs.ui.component.AbstractComponent;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.drawing.CustomElement;
import org.mindustrytool.libs.ui.drawing.Gradient;
import org.mindustrytool.util.ImageLoader;
import org.mindustrytool.libs.ui.layout.NodeSizing;

import arc.func.Cons;

/**
 * CustomUIComponent is a premium component that renders visual elements using a CustomElement drawing engine
 * with advanced SDF shapes, borders, shadows, textures, animations, and async image loading.
 */
public class CustomUIComponent extends AbstractComponent {

    /**
     * Style builder for CustomUIComponent, supporting advanced SDF shapes, borders,
     * shadows, textures, and gradient configuration.
     */
    public class Style extends ComponentStyle<Style> {
        float topLeftRadius = 8f;
        float topRightRadius = 8f;
        float bottomRightRadius = 8f;
        float bottomLeftRadius = 8f;
        final Color fillColor = new Color(Color.darkGray);
        float borderWidth;
        final Color borderColor = new Color(Color.white);
        int borderStyle;
        float dashLength = 10f;
        float dashRatio = 0.5f;
        Gradient gradient;
        Texture fillTexture;
        final Color textureTint = new Color(Color.white);
        float uvScaleX = 1f;
        float uvScaleY = 1f;
        float uvOffsetX;
        float uvOffsetY;
        float innerShadowSpread;
        float innerShadowBlur;
        final Color innerShadowColor = new Color(0, 0, 0, 0);
        float glowSpread;
        final Color glowColor = new Color(0, 0, 0, 0);
        float opacity = 1f;

        String currentUrl;
        Cons<Texture> activeCallback;

        Style() {
        }

        @Override
        protected NodeSizing sizing() {
            return sizing;
        }

        @Override
        protected Element styledElement() {
            return element;
        }

        public Style radius(float value) {
            this.topLeftRadius = this.topRightRadius = this.bottomRightRadius = this.bottomLeftRadius = value;
            return this;
        }

        public Style radius(float topLeft, float topRight, float bottomRight, float bottomLeft) {
            this.topLeftRadius = topLeft;
            this.topRightRadius = topRight;
            this.bottomRightRadius = bottomRight;
            this.bottomLeftRadius = bottomLeft;
            return this;
        }

        public Style fill(Color value) {
            this.fillColor.set(value);
            return this;
        }

        public Style fill(Gradient value) {
            this.gradient = value;
            return this;
        }

        public Style border(float width, Color color) {
            this.borderWidth = width;
            this.borderColor.set(color);
            return this;
        }

        public Style border(float width, Color color, int style) {
            this.borderWidth = width;
            this.borderColor.set(color);
            this.borderStyle = style;
            return this;
        }

        public Style dash(float length, float ratio) {
            this.dashLength = length;
            this.dashRatio = ratio;
            return this;
        }

        public Style innerShadow(float spread, float blur, Color color) {
            this.innerShadowSpread = spread;
            this.innerShadowBlur = blur;
            this.innerShadowColor.set(color);
            return this;
        }

        public Style glow(float spread, Color color) {
            this.glowSpread = spread;
            this.glowColor.set(color);
            return this;
        }

        public Style opacity(float value) {
            this.opacity = value;
            return this;
        }

        public Style texture(Texture texture, Color tint) {
            this.fillTexture = texture;
            this.textureTint.set(tint);
            return this;
        }

        /**
         * Loads a remote image from the given URL and applies it as a texture.
         *
         * @param url  the URL of the image
         * @param tint the tint color
         * @return this style builder instance
         */
        public Style loadImage(String url, Color tint) {
            if (currentUrl != null && activeCallback != null) {
                ImageLoader.cancel(currentUrl, activeCallback);
            }

            this.currentUrl = url;

            Texture cached = ImageLoader.get(url);
            if (cached != null) {
                this.fillTexture = cached;
                this.textureTint.set(tint);
                this.activeCallback = null;
                this.currentUrl = null;
                return this;
            }

            Cons<Texture> callback = new Cons<Texture>() {
                @Override
                public void get(Texture loadedTexture) {
                    if (activeCallback == this) {
                        if (loadedTexture != null) {
                            fillTexture = loadedTexture;
                            textureTint.set(tint);
                            element.invalidateHierarchy();
                        }
                        activeCallback = null;
                        currentUrl = null;
                    }
                }
            };

            this.activeCallback = callback;
            ImageLoader.load(url, callback);
            return this;
        }

        public Style loadImage(String url) {
            return loadImage(url, Color.white);
        }

        public Style uv(float scaleX, float scaleY, float offsetX, float offsetY) {
            this.uvScaleX = scaleX;
            this.uvScaleY = scaleY;
            this.uvOffsetX = offsetX;
            this.uvOffsetY = offsetY;
            return this;
        }

        public Style size(Cons<NodeSizing> configurator) {
            configurator.get(sizing);
            return this;
        }
    }

    public final Style style;
    final CustomElement drawer = new CustomElement();
    private Effect styleEffect;
    private Effect sizeEffect;

    // ─── Animation State ───
    private boolean animating;
    private final StyleBuffer animFrom = new StyleBuffer();
    private final StyleBuffer animTo = new StyleBuffer();
    private float animElapsed;
    private float animDuration;
    private Ease animEase;

    private static class StyleBuffer {
        final Color fillColor = new Color();
        float topLeftRadius;
        float topRightRadius;
        float bottomRightRadius;
        float bottomLeftRadius;
        float borderWidth;
        final Color borderColor = new Color();
        int borderStyle;
        float dashLength;
        float dashRatio;
        float innerShadowSpread;
        float innerShadowBlur;
        final Color innerShadowColor = new Color();
        float opacity;
        float glowSpread;
        final Color glowColor = new Color();

        void capture(Style s) {
            fillColor.set(s.fillColor);
            topLeftRadius = s.topLeftRadius;
            topRightRadius = s.topRightRadius;
            bottomRightRadius = s.bottomRightRadius;
            bottomLeftRadius = s.bottomLeftRadius;
            borderWidth = s.borderWidth;
            borderColor.set(s.borderColor);
            borderStyle = s.borderStyle;
            dashLength = s.dashLength;
            dashRatio = s.dashRatio;
            innerShadowSpread = s.innerShadowSpread;
            innerShadowBlur = s.innerShadowBlur;
            innerShadowColor.set(s.innerShadowColor);
            opacity = s.opacity;
            glowSpread = s.glowSpread;
            glowColor.set(s.glowColor);
        }

        void apply(Style s) {
            s.fillColor.set(fillColor);
            s.topLeftRadius = topLeftRadius;
            s.topRightRadius = topRightRadius;
            s.bottomRightRadius = bottomRightRadius;
            s.bottomLeftRadius = bottomLeftRadius;
            s.borderWidth = borderWidth;
            s.borderColor.set(borderColor);
            s.borderStyle = borderStyle;
            s.dashLength = dashLength;
            s.dashRatio = dashRatio;
            s.innerShadowSpread = innerShadowSpread;
            s.innerShadowBlur = innerShadowBlur;
            s.innerShadowColor.set(innerShadowColor);
            s.opacity = opacity;
            s.glowSpread = glowSpread;
            s.glowColor.set(glowColor);
        }

        void lerp(StyleBuffer a, StyleBuffer b, float t) {
            fillColor.set(a.fillColor).lerp(b.fillColor, t);
            topLeftRadius = a.topLeftRadius + (b.topLeftRadius - a.topLeftRadius) * t;
            topRightRadius = a.topRightRadius + (b.topRightRadius - a.topRightRadius) * t;
            bottomRightRadius = a.bottomRightRadius + (b.bottomRightRadius - a.bottomRightRadius) * t;
            bottomLeftRadius = a.bottomLeftRadius + (b.bottomLeftRadius - a.bottomLeftRadius) * t;
            borderWidth = a.borderWidth + (b.borderWidth - a.borderWidth) * t;
            borderColor.set(a.borderColor).lerp(b.borderColor, t);
            borderStyle = t < 0.5f ? a.borderStyle : b.borderStyle;
            dashLength = a.dashLength + (b.dashLength - a.dashLength) * t;
            dashRatio = a.dashRatio + (b.dashRatio - a.dashRatio) * t;
            innerShadowSpread = a.innerShadowSpread + (b.innerShadowSpread - a.innerShadowSpread) * t;
            innerShadowBlur = a.innerShadowBlur + (b.innerShadowBlur - a.innerShadowBlur) * t;
            innerShadowColor.set(a.innerShadowColor).lerp(b.innerShadowColor, t);
            opacity = a.opacity + (b.opacity - a.opacity) * t;
            glowSpread = a.glowSpread + (b.glowSpread - a.glowSpread) * t;
            glowColor.set(a.glowColor).lerp(b.glowColor, t);
        }
    }

    // ─── Element ───
    private final Element element = new Element() {
        @Override
        public float getPrefWidth() {
            return sizing.getFixedWidth();
        }

        @Override
        public float getPrefHeight() {
            return sizing.getFixedHeight();
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (animating) {
                updateAnimation(delta);
            }
        }

        @Override
        protected void setScene(Scene scene) {
            super.setScene(scene);
            if (scene == null) {
                CustomUIComponent.this.dispose();
            }
        }

        @Override
        public void draw() {
            float xPos = this.x;
            float yPos = this.y;
            float w = getWidth();
            float h = getHeight();
            if (w <= 0f || h <= 0f) {
                return;
            }

            Style s = style;

            if (s.gradient != null) {
                drawer.fillGradient(xPos, yPos, w, h, s.topLeftRadius, s.topRightRadius, s.bottomRightRadius, s.bottomLeftRadius, s.gradient);
            } else if (s.fillTexture != null) {
                drawer.fillTexture(xPos, yPos, w, h, s.topLeftRadius, s.topRightRadius, s.bottomRightRadius, s.bottomLeftRadius,
                    s.fillTexture, s.textureTint,
                    s.uvScaleX, s.uvScaleY, s.uvOffsetX, s.uvOffsetY);
            } else if (s.borderWidth > 0.001f) {
                drawer.fillWithBorder(xPos, yPos, w, h, s.topLeftRadius, s.topRightRadius, s.bottomRightRadius, s.bottomLeftRadius,
                    s.fillColor, s.borderWidth, s.borderColor);
            } else {
                drawer.fill(xPos, yPos, w, h, s.topLeftRadius, s.topRightRadius, s.bottomRightRadius, s.bottomLeftRadius, s.fillColor);
            }
        }
    };

    // ─── Constructor ───
    private CustomUIComponent() {
        this.style = new Style();
        sizing.onInvalidate(element::invalidateHierarchy);
        sizing.grow();
        element.touchable = Touchable.disabled;
        element.userObject = this;
    }

    public static CustomUIComponent of() {
        return new CustomUIComponent();
    }

    // ─── Public API ───
    public CustomUIComponent style(Cons<Style> configurator) {
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

    public CustomUIComponent anim(long durationMs, Ease ease, Cons<Style> configurator) {
        animFrom.capture(style);
        configurator.get(style);
        animTo.capture(style);
        animFrom.apply(style);

        animElapsed = 0f;
        animDuration = Math.max(durationMs, 0L) / 1000f;
        animEase = ease;
        animating = durationMs > 0;

        if (!animating) {
            animTo.apply(style);
        }
        element.invalidateHierarchy();
        return this;
    }

    public CustomUIComponent size(Cons<NodeSizing> configurator) {
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

    // ─── Internal ───
    private void updateAnimation(float delta) {
        animElapsed += delta;
        float t = Math.min(animElapsed / animDuration, 1f);
        float pt = animEase.apply(t);

        StyleBuffer tmp = new StyleBuffer();
        tmp.lerp(animFrom, animTo, pt);
        tmp.apply(style);
        element.invalidateHierarchy();

        if (t >= 1f) {
            animTo.apply(style);
            animating = false;
        }
    }

    @Override
    public Element element() {
        return element;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (style.currentUrl != null && style.activeCallback != null) {
            ImageLoader.cancel(style.currentUrl, style.activeCallback);
        }
        style.activeCallback = null;
        style.currentUrl = null;
        drawer.dispose();
    }
}
