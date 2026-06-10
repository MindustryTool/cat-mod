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

public class CustomUIComponent extends AbstractComponent {

    public class Style extends ComponentStyle<Style> {
        float tl = 8f, tr = 8f, br = 8f, bl = 8f;
        final Color fillColor = new Color(Color.darkGray);
        float borderWidth;
        final Color borderColor = new Color(Color.white);
        int borderStyle;
        float dashLen = 10f, dashRatio = 0.5f;
        Gradient gradient;
        Texture fillTexture;
        final Color textureTint = new Color(Color.white);
        float uvSx = 1f, uvSy = 1f, uvOx, uvOy;
        float innerShadowSpread, innerShadowBlur;
        final Color innerShadowColor = new Color(0, 0, 0, 0);
        float glowSpread;
        final Color glowColor = new Color(0, 0, 0, 0);
        float opacity = 1f;

        Style() {
        }

        @Override
        protected NodeSizing sizing() { return sizing; }

        @Override
        protected Element styledElement() { return element; }

        public Style radius(float v) { tl = tr = br = bl = v; return this; }

        public Style radius(float tl, float tr, float br, float bl) {
            this.tl = tl; this.tr = tr; this.br = br; this.bl = bl;
            return this;
        }

        public Style fill(Color value) { fillColor.set(value); return this; }

        public Style fill(Gradient g) { gradient = g; return this; }

        public Style border(float w, Color c) {
            borderWidth = w; borderColor.set(c); return this;
        }

        public Style border(float w, Color c, int style) {
            borderWidth = w; borderColor.set(c); borderStyle = style; return this;
        }

        public Style dash(float len, float ratio) {
            dashLen = len; dashRatio = ratio; return this;
        }

        public Style innerShadow(float spread, float blur, Color color) {
            innerShadowSpread = spread; innerShadowBlur = blur;
            innerShadowColor.set(color); return this;
        }

        public Style glow(float spread, Color color) {
            glowSpread = spread; glowColor.set(color); return this;
        }

        public Style opacity(float v) { opacity = v; return this; }

        public Style texture(Texture tex, Color tint) {
            fillTexture = tex; textureTint.set(tint); return this;
        }

        public Style loadImage(String url, Color tint) {
            Texture tex = ImageLoader.get(url);
            if (tex != null) {
                fillTexture = tex; textureTint.set(tint);
                return this;
            }
            ImageLoader.load(url, t -> {
                fillTexture = t; textureTint.set(tint);
                element.invalidateHierarchy();
            });
            return this;
        }

        public Style loadImage(String url) {
            return loadImage(url, Color.white);
        }

        public Style uv(float sx, float sy, float ox, float oy) {
            uvSx = sx; uvSy = sy; uvOx = ox; uvOy = oy; return this;
        }

        public Style size(Cons<NodeSizing> c) { c.get(sizing); return this; }
    }

    public final Style style;
    final CustomElement drawer = new CustomElement();
    private Effect styleEffect;
    private Effect sizeEffect;

    // ─── Animation State ───

    private boolean animating;
    private final StyleBuffer animFrom = new StyleBuffer();
    private final StyleBuffer animTo = new StyleBuffer();
    private float animElapsed, animDuration;
    private Ease animEase;

    private static class StyleBuffer {
        final Color fillColor = new Color();
        float tl, tr, br, bl;
        float borderWidth;
        final Color borderColor = new Color();
        int borderStyle;
        float dashLen, dashRatio;
        float innerShadowSpread, innerShadowBlur;
        final Color innerShadowColor = new Color();
        float opacity;
        float glowSpread;
        final Color glowColor = new Color();

        void capture(Style s) {
            fillColor.set(s.fillColor);
            tl = s.tl; tr = s.tr; br = s.br; bl = s.bl;
            borderWidth = s.borderWidth;
            borderColor.set(s.borderColor);
            borderStyle = s.borderStyle;
            dashLen = s.dashLen; dashRatio = s.dashRatio;
            innerShadowSpread = s.innerShadowSpread;
            innerShadowBlur = s.innerShadowBlur;
            innerShadowColor.set(s.innerShadowColor);
            opacity = s.opacity;
            glowSpread = s.glowSpread;
            glowColor.set(s.glowColor);
        }

        void apply(Style s) {
            s.fillColor.set(fillColor);
            s.tl = tl; s.tr = tr; s.br = br; s.bl = bl;
            s.borderWidth = borderWidth;
            s.borderColor.set(borderColor);
            s.borderStyle = borderStyle;
            s.dashLen = dashLen; s.dashRatio = dashRatio;
            s.innerShadowSpread = innerShadowSpread;
            s.innerShadowBlur = innerShadowBlur;
            s.innerShadowColor.set(innerShadowColor);
            s.opacity = opacity;
            s.glowSpread = glowSpread;
            s.glowColor.set(glowColor);
        }

        void lerp(StyleBuffer a, StyleBuffer b, float t) {
            fillColor.set(a.fillColor).lerp(b.fillColor, t);
            tl = a.tl + (b.tl - a.tl) * t;
            tr = a.tr + (b.tr - a.tr) * t;
            br = a.br + (b.br - a.br) * t;
            bl = a.bl + (b.bl - a.bl) * t;
            borderWidth = a.borderWidth + (b.borderWidth - a.borderWidth) * t;
            borderColor.set(a.borderColor).lerp(b.borderColor, t);
            borderStyle = t < 0.5f ? a.borderStyle : b.borderStyle;
            dashLen = a.dashLen + (b.dashLen - a.dashLen) * t;
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
        public float getPrefWidth() { return sizing.getFixedWidth(); }

        @Override
        public float getPrefHeight() { return sizing.getFixedHeight(); }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (animating) updateAnimation(delta);
        }

        @Override
        protected void setScene(Scene scene) {
            super.setScene(scene);
            if (scene == null) CustomUIComponent.this.dispose();
        }

        @Override
        public void draw() {
            float x = this.x, y = this.y, w = getWidth(), h = getHeight();
            if (w <= 0f || h <= 0f) return;

            Style s = style;

            if (s.gradient != null) {
                drawer.fillGradient(x, y, w, h, s.tl, s.tr, s.br, s.bl, s.gradient);
            } else if (s.fillTexture != null) {
                drawer.fillTexture(x, y, w, h, s.tl, s.tr, s.br, s.bl,
                    s.fillTexture, s.textureTint,
                    s.uvSx, s.uvSy, s.uvOx, s.uvOy);
            } else if (s.borderWidth > 0.001f) {
                drawer.fillWithBorder(x, y, w, h, s.tl, s.tr, s.br, s.bl,
                    s.fillColor, s.borderWidth, s.borderColor);
            } else {
                drawer.fill(x, y, w, h, s.tl, s.tr, s.br, s.bl, s.fillColor);
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

        if (!animating) animTo.apply(style);
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
    public Element element() { return element; }

    @Override
    public void dispose() {
        super.dispose();
        drawer.dispose();
    }
}
