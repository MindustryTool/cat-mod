package org.mindustrytool.libs.ui.core;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.scene.Element;
import arc.scene.Scene;
import arc.scene.event.Touchable;

import org.mindustrytool.libs.signal.MultithreadSignal;
import org.mindustrytool.libs.ui.component.Component;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.component.EffectHost;
import org.mindustrytool.libs.ui.layout.NodeSpec;

/**
 * A custom-rendered visual element — the primary rendering primitive of the UI system.
 *
 * <p>Uses {@link CustomDraw} (SDF shader) to paint rounded rectangles with fills, gradients,
 * borders, shadows, glow, and backdrop-filter effects.
 *
 * <p><b>Lifecycle:</b> the component disposes itself automatically when its arc {@link Element}
 * is detached from the scene. Call {@link #dispose()} explicitly only if the element is never
 * added to a scene.
 */
public class CustomComponent implements Component {

    /**
     * Visual style properties for CustomComponent.
     */
    public class Style extends ComponentStyle<Style> {

        public enum BackgroundMode {SOLID, GRADIENT, TEXTURE, BACKDROP}

        public BackgroundMode backgroundMode = BackgroundMode.SOLID;
        public float topLeftRadius = 8f;
        public float topRightRadius = 8f;
        public float bottomRightRadius = 8f;
        public float bottomLeftRadius = 8f;
        public final Color fillColor = new Color(Color.darkGray);

        public float borderWidth;
        public final Color borderColor = new Color(Color.white);
        public int borderStyle;
        /** The border dash length and ratio. */
        public float dashLength = 10f;
        public float dashRatio = 0.5f;

        /** Fixed slots for stacked gradients (gradient0 is base, gradient3 is top overlay). */
        public Gradient gradient0;
        public Gradient gradient1;
        public Gradient gradient2;
        public Gradient gradient3;

        /** Background texture fill. */
        public Texture fillTexture;
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

        public int backdropIterations;
        public float backdropBlend = 0.8f;
        public float backdropWeight = 0.8f;
        public float backdropMinAlpha = 0.8f;

        public int filterMode;
        public float filterAmount;
        public float noiseAmount;

        Style() {}

        @Override
        protected NodeSpec sizing() {
            return sizing;
        }

        @Override
        protected Element styledElement() {
            return element;
        }

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

        public Style background(Color value) {
            backgroundMode = BackgroundMode.SOLID;
            fillColor.set(value);
            return this;
        }

        /**
         * Sets the base gradient in slot 0 and clears other slots.
         *
         * @param value the Gradient
         * @return this style builder instance
         */
        public Style background(Gradient value) {
            backgroundMode = BackgroundMode.GRADIENT;
            gradient0 = value;
            gradient1 = null;
            gradient2 = null;
            gradient3 = null;
            return this;
        }

        /**
         * Sets multiple gradients in order starting from slot 0.
         *
         * @param values the array of Gradients
         * @return this style builder instance
         */
        public Style background(Gradient... values) {
            backgroundMode = BackgroundMode.GRADIENT;
            gradient0 = values.length > 0 ? values[0] : null;
            gradient1 = values.length > 1 ? values[1] : null;
            gradient2 = values.length > 2 ? values[2] : null;
            gradient3 = values.length > 3 ? values[3] : null;
            return this;
        }

        /**
         * Appends a gradient to the first empty slot.
         *
         * @param value the Gradient
         * @return this style builder instance
         */
        public Style addGradient(Gradient value) {
            backgroundMode = BackgroundMode.GRADIENT;
            if (gradient0 == null) gradient0 = value;
            else if (gradient1 == null) gradient1 = value;
            else if (gradient2 == null) gradient2 = value;
            else if (gradient3 == null) gradient3 = value;
            return this;
        }

        /**
         * Sets a gradient in a specific fixed slot (0 to 3) to control layer ordering.
         *
         * @param slot  the slot index (0: base, 3: top overlay)
         * @param value the Gradient
         * @return this style builder instance
         */
        public Style gradient(int slot, Gradient value) {
            backgroundMode = BackgroundMode.GRADIENT;
            if (slot == 0) gradient0 = value;
            else if (slot == 1) gradient1 = value;
            else if (slot == 2) gradient2 = value;
            else if (slot == 3) gradient3 = value;
            return this;
        }

        public Style background(Texture texture) {
            return background(texture, Color.white);
        }

        public Style background(Texture texture, Color tint) {
            backgroundMode = BackgroundMode.TEXTURE;
            fillTexture = texture;
            fillColor.set(tint);
            return this;
        }

        /**
         * Alias for {@link #backdrop(int, float, float, float)}.
         */
        public Style background(int iterations) {
            return backdrop(iterations, 0.8f, 0.8f, 0.8f);
        }

        /**
         * Alias for {@link #backdrop(int, float, float, float)}.
         */
        public Style background(int iterations, float blend) {
            return backdrop(iterations, blend, 0.8f, 0.8f);
        }

        public Style backdrop(int iterations) {
            return backdrop(iterations, 0.8f, 0.8f, 0.8f);
        }

        public Style backdrop(int iterations, float blend, float weight, float minAlpha) {
            backgroundMode = BackgroundMode.BACKDROP;
            backdropIterations = iterations;
            backdropBlend = blend;
            backdropWeight = weight;
            backdropMinAlpha = minAlpha;
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

        public Style grayscale(float amount) {
            filterMode = 1;
            filterAmount = amount;
            return this;
        }

        public Style sepia(float amount) {
            filterMode = 2;
            filterAmount = amount;
            return this;
        }

        public Style brightness(float amount) {
            filterMode = 3;
            filterAmount = amount;
            return this;
        }

        public Style invert(float amount) {
            filterMode = 4;
            filterAmount = amount;
            return this;
        }

        public Style noise(float amount) {
            noiseAmount = amount;
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
    }

    protected final NodeSpec sizing = new NodeSpec();
    public final Style style = new Style();
    protected final CustomDraw drawer = new CustomDraw();

    private MultithreadSignal.Handle imageHandle;
    private Texture ownedTexture;

    private final EffectHost effects = new EffectHost();

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
        protected void setScene(Scene scene) {
            super.setScene(scene);
            if (scene == null) CustomComponent.this.dispose();
        }

        @Override
        public void draw() {
            float width = getWidth();
            float height = getHeight();
            if (width <= 0f || height <= 0f) return;
            drawer.draw(x, y, width, height, style);
        }
    };

    private CustomComponent() {
        sizing.onInvalidate(element::invalidateHierarchy);
        sizing.grow();

        element.touchable = Touchable.disabled;
        element.userObject = this;
    }

    public static CustomComponent of() {
        return new CustomComponent();
    }

    /**
     * Applies a style configurator immediately (no signal tracking).
     */
    public CustomComponent style(Cons<Style> configurator) {
        configurator.get(style);
        element.invalidateHierarchy();
        return this;
    }

    /**
     * Applies a size configurator immediately.
     */
    public CustomComponent size(Cons<NodeSpec> configurator) {
        configurator.get(sizing);
        element.invalidateHierarchy();
        return this;
    }

    /**
     * Loads an image from {@code url} asynchronously and applies it as a fill texture.
     * Disposes any previously loaded owned texture.
     */
    public CustomComponent loadImage(String url, Color tint) {

        var signal = ImageLoader.get(url);
        imageHandle = signal.subscribeOnMain(
            res -> res.state() == ImageLoader.ImageLoadState.LOADED,
            () -> {
                var res = signal.state();
                if (res.state() != ImageLoader.ImageLoadState.LOADED) return;

                disposeImageResources();
                applyLoadedTexture(res.texture(), tint);
            });

        return this;
    }

    public CustomComponent loadImage(String url) {
        return loadImage(url, Color.white);
    }

    /**
     * Overrides the touchable mode. Default is {@code Touchable.disabled}.
     */
    public CustomComponent touchable(Touchable mode) {
        element.touchable = mode;
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
        effects.disposeAll();
        disposeImageResources();
        drawer.dispose();
    }

    private void applyLoadedTexture(Texture texture, Color tint) {
        style.backgroundMode = Style.BackgroundMode.TEXTURE;
        style.fillTexture = texture;
        ownedTexture = texture;
        style.fillColor.set(tint);
        element.invalidateHierarchy();
    }

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
}
