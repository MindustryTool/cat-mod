package org.mindustrytool.libs.ui.element;

import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.FontCache;
import arc.graphics.g2d.GlyphLayout;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.util.Align;

import lombok.Getter;

import org.mindustrytool.NekoMod;
import org.mindustrytool.mdtui.util.FontManager;

/**
 * A state-free, pure-property UI element that displays text.
 * Designed to be controlled directly by external UI components (e.g., {@code Text}).
 *
 * <p>Unlike Arc's original {@code Label}, this class does not hold dynamic suppliers
 * or internal localized bundle translations, and does not perform per-frame updates.
 * Instead, it exposes direct property setters that instantly trigger the appropriate invalidation
 * levels (hierarchy invalidation or local size invalidation) when changed.
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li><b>Direct Invalidation:</b> Property changes (e.g., text, font, scale) trigger hierarchy invalidations
 *       on the spot, while alignment changes trigger local layout invalidations.</li>
 *   <li><b>State-Free Drawing:</b> Renders by applying the inherited {@code Element.color} directly to
 *       the underlying {@code FontCache} without redundant alpha calculations.</li>
 *   <li><b>Font Scale Optimization:</b> Temporarily alters and restores the shared {@code Font} scale
 *       only if the scale is non-native, avoiding expensive calculations for default sized text.</li>
 * </ul>
 */
public class TextElement extends Element {
    protected static final GlyphLayout prefSizeLayout = new GlyphLayout();

    protected final GlyphLayout layout = new GlyphLayout();
    protected final Vec2 prefSize = new Vec2();

    protected @Getter String text = "";
    protected @Getter Font font = NekoMod.getFeather().instance(FontManager.class).getJetbrainsMono();
    protected @Getter boolean wrap;
    protected @Getter float fontScaleX = 1;
    protected @Getter float fontScaleY = 1;
    private float baseFontScaleX = font.getScaleX();
    private float baseFontScaleY = font.getScaleY();
    protected @Getter int labelAlign = Align.left;
    protected @Getter int lineAlign = Align.left;
    protected @Getter String ellipsis;

    protected FontCache cache = font.newFontCache();
    protected float lastPrefHeight;
    protected boolean prefSizeInvalid = true;
    protected boolean fontScaleChanged = false;

    /** Sets the display text. Triggers hierarchy invalidation. */
    public void setText(String text) {
        if (this.text.equals(text) || text == null) return;
        this.text = text;
        invalidateHierarchy();
    }

    /** Sets the font. Creates a new {@link FontCache} for the given font. */
    public void setFont(Font font) {
        if (this.font == font || font == null) return;
        this.font = font;
        this.cache = font.newFontCache();
        baseFontScaleX = font.getScaleX();
        baseFontScaleY = font.getScaleY();
        invalidateHierarchy();
    }

    /** Enables or disables text wrapping at the element width boundary. */
    public void setWrap(boolean wrap) {
        if (this.wrap == wrap) return;
        this.wrap = wrap;
        invalidateHierarchy();
    }

    /** Sets the horizontal font scale factor. */
    public void setFontScaleX(float fontScaleX) {
        if (this.fontScaleX == fontScaleX) return;
        this.fontScaleX = fontScaleX;
        fontScaleChanged = (fontScaleX != 1f || fontScaleY != 1f);
        invalidateHierarchy();
    }

    /** Sets the vertical font scale factor. */
    public void setFontScaleY(float fontScaleY) {
        if (this.fontScaleY == fontScaleY) return;
        this.fontScaleY = fontScaleY;
        fontScaleChanged = (fontScaleX != 1f || fontScaleY != 1f);
        invalidateHierarchy();
    }

    /** Sets uniform font scale on both axes. */
    public void setFontScale(float fontScale) {
        setFontScale(fontScale, fontScale);
    }

    /** Sets independent font scale on X and Y axes. */
    public void setFontScale(float fontScaleX, float fontScaleY) {
        if (this.fontScaleX == fontScaleX && this.fontScaleY == fontScaleY) return;
        this.fontScaleX = fontScaleX;
        this.fontScaleY = fontScaleY;
        fontScaleChanged = (fontScaleX != 1f || fontScaleY != 1f);
        invalidateHierarchy();
    }

    /** Sets the alignment of the text block within the element's bounds. */
    public void setLabelAlign(int labelAlign) {
        if (this.labelAlign == labelAlign) return;
        this.labelAlign = labelAlign;
        invalidateHierarchy();
    }

    /** Sets the alignment of individual lines of text. */
    public void setLineAlign(int lineAlign) {
        if (this.lineAlign == lineAlign) return;
        this.lineAlign = lineAlign;
        invalidateHierarchy();
    }

    /** Sets both label and line alignment to the same value. */
    public void setAlignment(int alignment) {
        setAlignment(alignment, alignment);
    }

    /** Sets independent label and line alignment. */
    public void setAlignment(int labelAlign, int lineAlign) {
        if (this.labelAlign == labelAlign && this.lineAlign == lineAlign) return;
        this.labelAlign = labelAlign;
        this.lineAlign = lineAlign;
        invalidateHierarchy();
    }

    /** Sets the ellipsis string for truncation when wrapping is disabled. */
    public void setEllipsis(String ellipsis) {
        if (this.ellipsis == null && ellipsis == null) return;
        if (this.ellipsis != null && this.ellipsis.equals(ellipsis)) return;
        this.ellipsis = ellipsis;
        invalidateHierarchy();
    }

    /** Enables or disables standard "..." ellipsis truncation. */
    public void setEllipsis(boolean ellipsis) {
        setEllipsis(ellipsis ? "..." : null);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        prefSizeInvalid = true;
    }

    @Override
    public void draw() {
        validate();
        if (cache != null) {
            cache.tint(Color.white);
            cache.setPosition(x, y);
            cache.draw();
        }
    }

    @Override
    public void layout() {
        if (cache == null) return;
        Font font = cache.getFont();
        float oldScaleX = font.getScaleX();
        float oldScaleY = font.getScaleY();
        if (fontScaleChanged) font.getData().setScale(baseFontScaleX * fontScaleX, baseFontScaleY * fontScaleY);

        boolean wrap = this.wrap && ellipsis == null;
        if (wrap) {
            float prefHeight = getPrefHeight();
            if (prefHeight != lastPrefHeight) {
                lastPrefHeight = prefHeight;
                invalidateHierarchy();
            }
        }

        float width = getWidth(), height = getHeight();
        float x = 0, y = 0;

        GlyphLayout layout = this.layout;
        float textWidth, textHeight;
        if (wrap || text.contains("\n")) {
            layout.setText(font, text, 0, text.length(), Color.white, width, lineAlign, wrap, ellipsis);
            textWidth = layout.width;
            textHeight = layout.height;

            if ((labelAlign & Align.left) == 0) {
                if ((labelAlign & Align.right) != 0) x += width - textWidth;
                else x += (width - textWidth) / 2;
            }
        } else {
            textWidth = width;
            textHeight = font.getData().capHeight;
        }

        if ((labelAlign & Align.top) != 0) {
            y += cache.getFont().isFlipped() ? 0 : height - textHeight;
            y += font.getDescent();
        } else if ((labelAlign & Align.bottom) != 0) {
            y += cache.getFont().isFlipped() ? height - textHeight : 0;
            y -= font.getDescent();
        } else {
            y += (height - textHeight) / 2;
        }
        if (!cache.getFont().isFlipped()) y += textHeight;

        layout.setText(font, text, 0, text.length(), Color.white, textWidth, lineAlign, wrap, ellipsis);
        cache.setText(layout, x, y);

        if (fontScaleChanged) font.getData().setScale(oldScaleX, oldScaleY);
    }

    @Override
    public float getPrefWidth() {
        if (cache == null) return 0;
        if (wrap) return 0;
        if (prefSizeInvalid) scaleAndComputePrefSize();
        return prefSize.x;
    }

    @Override
    public float getPrefHeight() {
        if (cache == null) return 0;
        if (prefSizeInvalid) scaleAndComputePrefSize();
        float descentScaleCorrection = 1;
        if (fontScaleChanged) descentScaleCorrection = (baseFontScaleY * fontScaleY) / font.getScaleY();
        return prefSize.y - font.getDescent() * descentScaleCorrection * 2;
    }

    private void scaleAndComputePrefSize() {
        if (cache == null) return;
        Font font = cache.getFont();
        float oldScaleX = font.getScaleX();
        float oldScaleY = font.getScaleY();
        if (fontScaleChanged) font.getData().setScale(baseFontScaleX * fontScaleX, baseFontScaleY * fontScaleY);

        computePrefSize();

        if (fontScaleChanged) font.getData().setScale(oldScaleX, oldScaleY);
    }

    private void computePrefSize() {
        prefSizeInvalid = false;
        GlyphLayout prefSizeLayout = TextElement.prefSizeLayout;
        if (wrap && ellipsis == null) {
            prefSizeLayout.setText(cache.getFont(), text, Color.white, getWidth(), Align.left, true);
        } else {
            prefSizeLayout.setText(cache.getFont(), text, 0, text.length(), Color.white, 0, lineAlign, wrap, ellipsis);
        }
        prefSize.set(prefSizeLayout.width, prefSizeLayout.height);
    }
}
