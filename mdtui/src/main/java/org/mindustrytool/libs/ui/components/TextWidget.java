package org.mindustrytool.libs.ui.components;

import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.FontCache;
import arc.graphics.g2d.GlyphLayout;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.util.Align;

import lombok.Builder;

import org.mindustrytool.libs.ui.widget.ElementNode;
import org.mindustrytool.libs.ui.widget.Widget;
import org.mindustrytool.libs.ui.layout.LayoutSpec;

/**
 * A declarative, immutable text widget supporting custom scaling, alignment, coloring,
 * and wrapping layout.
 *
 * @param layoutSpec  the layout spec rules and sizing constraints.
 * @param text        the string text content to display.
 * @param color       the coloring tint.
 * @param font        the custom Font to render with.
 * @param fontScale   font scaling factor.
 * @param labelAlign  label alignment within the bound container (combination of Align top/bottom/left/right).
 * @param lineAlign   line justification layout (Align left/right/center).
 * @param wrap        true if text wrapping should be calculated.
 * @param ellipsis    ellipsis string replacement when text overflows bounds, or null if disabled.
 */
@Builder(toBuilder = true)
public record TextWidget(
    LayoutSpec layoutSpec,
    String text,
    Color color,
    Font font,
    float fontScale,
    int labelAlign,
    int lineAlign,
    boolean wrap,
    String ellipsis
) implements Widget {

    /**
     * Lombok builder class helper that defines the default properties for a TextWidget builder.
     */
    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    public static class TextWidgetBuilder {
        private LayoutSpec layoutSpec = LayoutSpec.defaultSpec();
        private String text = "";
        private Color color = Color.white;
        private float fontScale = 1f;
        private int labelAlign = Align.left;
        private int lineAlign = Align.left;
        private boolean wrap = false;
    }

    @Override
    public ElementNode createElement() {
        return new TextElementNode(this);
    }
}

/**
 * Backing ElementNode that binds a {@link TextWidget} to a mutable {@link TextElement}.
 */
class TextElementNode extends ElementNode {

    /**
     * The backing Arc element for rendering text.
     */
    private final TextElement element = new TextElement();

    /**
     * Constructs a text element node.
     *
     * @param widget the text widget blueprint.
     */
    TextElementNode(TextWidget widget) {
        super(widget);
        
        arcElement = element;
        arcElement.userObject = this;
    }

    @Override
    public void mount(ElementNode parent) {
        element.setWidget((TextWidget) widget);
        element.color.set(((TextWidget) widget).color());
    }

    @Override
    public void update(Widget newWidget) {
        super.update(newWidget);
        
        element.setWidget((TextWidget) newWidget);
        element.color.set(((TextWidget) newWidget).color());
    }

    @Override
    public LayoutSpec sizing() {
        return ((TextWidget) widget).layoutSpec();
    }
}

/**
 * Backing Arc scene Element representing text rendering.
 * Operates glyph text calculation and layout rendering on demand.
 */
class TextElement extends Element {

    /**
     * Shared glyph layout instance for calculating preferred dimensions.
     */
    private static final GlyphLayout prefSizeLayout = new GlyphLayout();

    /**
     * The glyph layout instance containing visual bounds of the active text.
     */
    private final GlyphLayout layout = new GlyphLayout();

    /**
     * Cached preferred size vector.
     */
    private final Vec2 prefSize = new Vec2();

    /**
     * The bound text widget blueprint containing layout and style settings.
     */
    TextWidget widget;

    /**
     * Cached font cache used to optimize text drawing operations.
     */
    FontCache cache;

    /**
     * The last font configuration used to track font updates.
     */
    private Font lastFont;

    /**
     * The last measured preferred height used to trigger hierarchy invalidation on wrap.
     */
    private float lastPrefHeight;

    /**
     * Flag indicating whether the cached preferred size has been invalidated.
     */
    private boolean prefSizeInvalid = true;

    @Override
    public void invalidate() {
        super.invalidate();
        prefSizeInvalid = true;
    }

    @Override
    public void draw() {
        validate();
        if (cache == null) return;
        
        cache.tint(color);
        cache.setPosition(x, y);
        cache.draw();
    }

    @Override
    public void layout() {
        if (cache == null) return;
        
        Font font = cache.getFont();
        float baseScaleX = font.getScaleX();
        float baseScaleY = font.getScaleY();
        boolean scaleChanged = widget.fontScale() != 1f;
        
        if (scaleChanged) {
            font.getData().setScale(baseScaleX * widget.fontScale(), baseScaleY * widget.fontScale());
        }

        boolean wrap = widget.wrap() && widget.ellipsis() == null;
        if (wrap) {
            float prefHeight = getPrefHeight();
            if (prefHeight != lastPrefHeight) {
                lastPrefHeight = prefHeight;
                invalidateHierarchy();
            }
        }

        float width = getWidth(), height = getHeight();
        float x = 0, y = 0;
        String text = widget.text();

        GlyphLayout layout = this.layout;
        float textWidth, textHeight;
        
        if (wrap || text.contains("\n")) {
            layout.setText(font, text, 0, text.length(), Color.white, width, widget.lineAlign(), wrap, widget.ellipsis());
            textWidth = layout.width;
            textHeight = layout.height;

            if ((widget.labelAlign() & Align.left) == 0) {
                if ((widget.labelAlign() & Align.right) != 0) {
                    x += width - textWidth;
                } else {
                    x += (width - textWidth) / 2;
                }
            }
        } else {
            textWidth = width;
            textHeight = font.getData().capHeight;
        }

        if ((widget.labelAlign() & Align.top) != 0) {
            y += cache.getFont().isFlipped() ? 0 : height - textHeight;
            y += font.getDescent();
        } else if ((widget.labelAlign() & Align.bottom) != 0) {
            y += cache.getFont().isFlipped() ? height - textHeight : 0;
            y -= font.getDescent();
        } else {
            y += (height - textHeight) / 2;
        }
        
        if (!cache.getFont().isFlipped()) {
            y += textHeight;
        }

        layout.setText(font, text, 0, text.length(), Color.white, textWidth, widget.lineAlign(), wrap, widget.ellipsis());
        cache.setText(layout, x, y);

        if (scaleChanged) {
            font.getData().setScale(baseScaleX, baseScaleY);
        }
    }

    @Override
    public float getPrefWidth() {
        if (cache == null) return 0;
        if (widget.wrap()) return 0;
        if (prefSizeInvalid) {
            computePrefSize();
        }
        
        return prefSize.x;
    }

    @Override
    public float getPrefHeight() {
        if (cache == null) return 0;
        if (prefSizeInvalid) {
            computePrefSize();
        }
        
        Font font = cache.getFont();
        float descent = font.getDescent();
        
        if (widget.fontScale() != 1f) {
            float baseScaleY = font.getScaleY();
            font.getData().setScale(baseScaleY * widget.fontScale(), baseScaleY * widget.fontScale());
            descent *= widget.fontScale();
            font.getData().setScale(baseScaleY, baseScaleY);
        }
        
        return prefSize.y - descent * 2;
    }

    /**
     * Binds a new text widget configuration and invalidates the layout if font or layout changes.
     *
     * @param w the text widget configuration.
     */
    void setWidget(TextWidget w) {
        this.widget = w;
        
        if (w.font() != lastFont) {
            cache = w.font() == null ? null : w.font().newFontCache();
            lastFont = w.font();
        }
        
        invalidateHierarchy();
    }

    /**
     * Computes the preferred width and height of the text element using the configured font, scale, and constraints.
     */
    private void computePrefSize() {
        prefSizeInvalid = false;
        
        Font font = cache.getFont();
        float baseScaleX = font.getScaleX();
        float baseScaleY = font.getScaleY();
        boolean scaleChanged = widget.fontScale() != 1f;
        
        if (scaleChanged) {
            font.getData().setScale(baseScaleX * widget.fontScale(), baseScaleY * widget.fontScale());
        }

        GlyphLayout ps = TextElement.prefSizeLayout;
        String text = widget.text();
        
        if (widget.wrap() && widget.ellipsis() == null) {
            ps.setText(font, text, Color.white, getWidth(), Align.left, true);
        } else {
            ps.setText(font, text, 0, text.length(), Color.white, 0, widget.lineAlign(), widget.wrap(), widget.ellipsis());
        }
        
        prefSize.set(ps.width, ps.height);

        if (scaleChanged) {
            font.getData().setScale(baseScaleX, baseScaleY);
        }
    }
}
