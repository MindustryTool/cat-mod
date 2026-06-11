package org.mindustrytool.libs.ui.core;

import arc.graphics.Color;
import arc.graphics.Texture;
import arc.scene.Element;
import arc.scene.Scene;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;

import org.mindustrytool.libs.signal.MultithreadSignal;
import org.mindustrytool.libs.ui.component.Component;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.layout.NodeSpec;

import arc.func.Cons;

/**
 * A custom-rendered visual element — the primary rendering primitive of the UI system.
 *
 * <p>Uses {@link CustomDraw} (SDF shader) to paint rounded rectangles with fills, gradients,
 * borders, shadows, glow, and frosted-glass effects. Animates between two {@link StyleSnapshot}
 * states using any {@link Ease} function.
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * CustomComponent.of()
 *     .style(s -> s.fill(Color.blue).radius(12f).opacity(0.9f))
 *     .size(sz -> sz.fixedWidth(200f).fixedHeight(80f))
 *     .onClick(() -> System.out.println("clicked"));
 * }</pre>
 *
 * <p><b>Lifecycle:</b> the component disposes itself automatically when its arc {@link Element}
 * is detached from the scene. Call {@link #dispose()} explicitly only if the element is never
 * added to a scene.
 */
public class CustomComponent implements Component {

    // ─── Style ───────────────────────────────────────────────────────────────

    /**
     * Visual style properties. All fields that appear in {@link StyleSnapshot} are animatable;
     * fields not in the snapshot ({@code gradient}, {@code fillTexture}, UV params) change
     * discretely.
     */
    public class Style extends ComponentStyle<Style> {

        public float topLeftRadius = 8f;
        public float topRightRadius = 8f;
        public float bottomRightRadius = 8f;
        public float bottomLeftRadius = 8f;
        public final Color fillColor = new Color(Color.darkGray);

        public float borderWidth;
        public final Color borderColor = new Color(Color.white);
        public int borderStyle;
        public float dashLength = 10f;
        public float dashRatio = 0.5f;

        public Gradient gradient;
        public Texture fillTexture;
        public final Color textureTint = new Color(Color.white);
        public float uvScaleX = 1f;
        public float uvScaleY = 1f;
        public float uvOffsetX;
        public float uvOffsetY;

        public float innerShadowSpread;
        public float innerShadowBlur;
        public final Color innerShadowColor = new Color(0, 0, 0, 0);

        public float glowSpread;
        public final Color glowColor = new Color(0, 0, 0, 0);

        public float opacity = 1f;


        Style() {}


        @Override
        protected NodeSpec sizing() {
            return sizing;
        }


        @Override
        protected Element styledElement() {
            return element;
        }


        // --- Fluent style builders ---

        public Style radius(float value) {
            topLeftRadius = topRightRadius = bottomRightRadius = bottomLeftRadius = value;
            return this;
        }


        public Style radius(float topLeft, float topRight, float bottomRight, float bottomLeft) {
            topLeftRadius = topLeft;
            topRightRadius = topRight;
            bottomRightRadius = bottomRight;
            bottomLeftRadius = bottomLeft;
            return this;
        }


        public Style fill(Color value) {
            fillColor.set(value);
            return this;
        }


        public Style fill(Gradient value) {
            gradient = value;
            return this;
        }


        public Style texture(Texture texture, Color tint) {
            fillTexture = texture;
            textureTint.set(tint);
            return this;
        }


        public Style uv(float scaleX, float scaleY, float offsetX, float offsetY) {
            uvScaleX = scaleX;
            uvScaleY = scaleY;
            uvOffsetX = offsetX;
            uvOffsetY = offsetY;
            return this;
        }


        public Style border(float width, Color color) {
            borderWidth = width;
            borderColor.set(color);
            return this;
        }


        public Style border(float width, Color color, int style) {
            borderWidth = width;
            borderColor.set(color);
            borderStyle = style;
            return this;
        }


        public Style dash(float length, float ratio) {
            dashLength = length;
            dashRatio = ratio;
            return this;
        }


        public Style innerShadow(float spread, float blur, Color color) {
            innerShadowSpread = spread;
            innerShadowBlur = blur;
            innerShadowColor.set(color);
            return this;
        }


        public Style glow(float spread, Color color) {
            glowSpread = spread;
            glowColor.set(color);
            return this;
        }


        public Style opacity(float value) {
            opacity = value;
            return this;
        }


        public Style size(Cons<NodeSpec> configurator) {
            configurator.get(sizing);
            return this;
        }
    }


    // ─── Fields ──────────────────────────────────────────────────────────────

    protected final NodeSpec sizing = new NodeSpec();

    public final Style style;

    final CustomDraw drawer = new CustomDraw();

    private MultithreadSignal.Handle imageHandle;
    private Texture ownedTexture;


    // ─── Animation state ─────────────────────────────────────────────────────

    private boolean animating;
    private StyleSnapshot animFrom;
    private StyleSnapshot animTo;
    private float animElapsed;
    private float animDuration;
    private Ease animEase;


    // ─── Arc Element ─────────────────────────────────────────────────────────

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
            if (!animating) return;
            updateAnimation(delta);
        }


        @Override
        protected void setScene(Scene scene) {
            super.setScene(scene);
            if (scene == null) CustomComponent.this.dispose();
        }


        @Override
        public void draw() {
            float width = getWidth();
            float height = getHeight();
            if (width <= 0f || height <= 0f) return;
            drawWith(style, x, y, width, height);
        }
    };


    // ─── Constructor ─────────────────────────────────────────────────────────

    private CustomComponent() {
        style = new Style();
        sizing.onInvalidate(element::invalidateHierarchy);
        sizing.grow();
        element.touchable = Touchable.disabled;
        element.userObject = this;
    }


    public static CustomComponent of() {
        return new CustomComponent();
    }


    // ─── Public API ──────────────────────────────────────────────────────────

    /** Applies a style configurator immediately (no signal tracking). */
    public CustomComponent style(Cons<Style> configurator) {
        configurator.get(style);
        element.invalidateHierarchy();
        return this;
    }


    /** Applies a size configurator immediately. */
    public CustomComponent size(Cons<NodeSpec> configurator) {
        configurator.get(sizing);
        element.invalidateHierarchy();
        return this;
    }


    /**
     * Animates from the current style to the state produced by {@code configurator}.
     * If {@code durationMs} is 0, the target state is applied immediately.
     */
    public CustomComponent anim(long durationMs, Ease ease, Cons<Style> configurator) {
        animFrom = StyleSnapshot.from(style);
        configurator.get(style);
        animTo = StyleSnapshot.from(style);
        animFrom.applyTo(style);

        animElapsed = 0f;
        animDuration = Math.max(durationMs, 0L) / 1000f;
        animEase = ease;
        animating = durationMs > 0;

        if (!animating) animTo.applyTo(style);
        element.invalidateHierarchy();
        return this;
    }


    /**
     * Loads an image from {@code url} asynchronously and applies it as a fill texture.
     * Disposes any previously loaded owned texture.
     */
    public CustomComponent loadImage(String url, Color tint) {
        disposeImageResources();
        var signal = ImageLoader.get(url);
        imageHandle = signal.subscribeOnMain(
            result -> result.state() == ImageLoader.ImageLoadState.LOADED
                   || result.state() == ImageLoader.ImageLoadState.FAILED,
            () -> {
                var result = signal.state();
                if (result.state() != ImageLoader.ImageLoadState.LOADED) return;
                style.fillTexture = result.texture();
                ownedTexture = result.texture();
                style.textureTint.set(tint);
                element.invalidateHierarchy();
            }
        );
        var current = signal.state();
        if (current.state() == ImageLoader.ImageLoadState.LOADED) {
            style.fillTexture = current.texture();
            ownedTexture = current.texture();
            style.textureTint.set(tint);
            element.invalidateHierarchy();
        }
        return this;
    }


    public CustomComponent loadImage(String url) {
        return loadImage(url, Color.white);
    }


    // ─── Touch / event API ───────────────────────────────────────────────────

    /** Overrides the touchable mode. Default is {@code Touchable.disabled}. */
    public CustomComponent touchable(Touchable mode) {
        element.touchable = mode;
        return this;
    }


    /** Makes the element touchable and registers a click handler. */
    public CustomComponent onClick(Runnable handler) {
        element.touchable = Touchable.enabled;
        element.clicked(handler);
        return this;
    }


    /** Makes the element touchable and registers hover enter/exit handlers. */
    public CustomComponent onHover(Runnable onEnter, Runnable onExit) {
        element.touchable = Touchable.enabled;
        element.addListener(new InputListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Element fromActor) {
                if (pointer != -1) return;
                onEnter.run();
            }


            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Element toActor) {
                if (pointer != -1) return;
                onExit.run();
            }
        });
        return this;
    }


    // ─── Component interface ─────────────────────────────────────────────────

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
        disposeImageResources();
        drawer.dispose();
    }


    // ─── Private helpers ─────────────────────────────────────────────────────

    private void disposeImageResources() {
        if (ownedTexture != null) {
            ownedTexture.dispose();
            ownedTexture = null;
        }
        if (imageHandle != null) {
            imageHandle.dispose();
            imageHandle = null;
        }
    }


    private void updateAnimation(float delta) {
        animElapsed += delta;
        float t = Math.min(animElapsed / animDuration, 1f);
        StyleSnapshot.lerp(animFrom, animTo, animEase.apply(t)).applyTo(style);
        element.invalidateHierarchy();

        if (t < 1f) return;
        animTo.applyTo(style);
        animating = false;
    }


    private void drawWith(Style s, float x, float y, float width, float height) {
        if (s.gradient != null) {
            drawer.fillGradient(x, y, width, height,
                s.topLeftRadius, s.topRightRadius, s.bottomRightRadius, s.bottomLeftRadius,
                s.gradient);
            return;
        }
        if (s.fillTexture != null) {
            drawer.fillTexture(x, y, width, height,
                s.topLeftRadius, s.topRightRadius, s.bottomRightRadius, s.bottomLeftRadius,
                s.fillTexture, s.textureTint,
                s.uvScaleX, s.uvScaleY, s.uvOffsetX, s.uvOffsetY);
            return;
        }
        if (s.borderWidth > 0.001f) {
            drawer.fillWithBorder(x, y, width, height,
                s.topLeftRadius, s.topRightRadius, s.bottomRightRadius, s.bottomLeftRadius,
                s.fillColor, s.borderWidth, s.borderColor);
            return;
        }
        drawer.fill(x, y, width, height,
            s.topLeftRadius, s.topRightRadius, s.bottomRightRadius, s.bottomLeftRadius,
            s.fillColor);
    }
}
