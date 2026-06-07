package com.neko.libs.ui.widget;

import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.util.Align;
import com.neko.libs.state.State;
import com.neko.libs.ui.El;
import com.neko.libs.ui.layout.LayoutCtx;
import com.neko.libs.ui.style.StyleSpec;
import mindustry.ui.Fonts;

/**
 * Text label widget.
 *
 * <p>Style tokens (in addition to base El tokens):
 * <pre>
 *   color:NAME   — text color (Theme semantic names or hex)
 *   font:title   — use Theme.fontTitle()
 *   font:mono    — use Theme.fontMono() (default)
 *   align:left|center|right
 *   wrap         — enable text wrapping
 * </pre>
 *
 * <p>Fluent setters override style-token defaults.
 */
public class NLabel extends El {

    private String     text;
    private Color      color = com.neko.libs.ui.style.Theme.textPrimary;
    private Font      font  = Fonts.def;
    private int        align = Align.left;
    private boolean    wrap  = false;

    private final GlyphLayout glyphLayout = new GlyphLayout();
    private float cachedAvailW = -1f;

    // ── Constructors ──────────────────────────────────────────────────────────

    public NLabel(String text, String style) {
        super(style);
        this.text = text;
        applyStyleOverrides(style);
    }

    public NLabel(String text) {
        this(text, "");
    }

    /** Reactive label — updates automatically when state changes. */
    public NLabel(State<String> state, String style) {
        super(style);
        this.text = state.getValue();
        applyStyleOverrides(style);
        bind(state, v -> { this.text = v; invalidateMeasure(); });
    }

    public NLabel(State<String> state) {
        this(state, "");
    }

    // ── Fluent setters ────────────────────────────────────────────────────────

    public NLabel color(Color c)     { this.color = c; return this; }
    public NLabel font(Font f)   { this.font  = f; invalidateMeasure(); return this; }
    public NLabel align(int a)       { this.align = a; return this; }
    public NLabel wrap(boolean w)    { this.wrap  = w; invalidateMeasure(); return this; }

    public void setText(String t) {
        if (t == null) t = "";
        if (!t.equals(this.text)) { this.text = t; invalidateMeasure(); }
    }

    // ── Sizing ────────────────────────────────────────────────────────────────

    @Override
    protected float contentPrefWidth(LayoutCtx ctx, float innerW, StyleSpec s) {
        ensureLayout(innerW);
        return glyphLayout.width;
    }

    @Override
    protected float contentPrefHeight(LayoutCtx ctx, float innerW, float availH, StyleSpec s) {
        ensureLayout(wrap ? innerW : Float.MAX_VALUE);
        return glyphLayout.height;
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    @Override
    protected void drawContent(float alpha) {
        float drawW = wrap ? getW() : Float.MAX_VALUE;
        ensureLayout(drawW);

        Color old = font.getColor().cpy();
        font.setColor(color.r, color.g, color.b, color.a * alpha);

        float drawX = getX();
        if (!wrap) {
            if (align == Align.center) drawX = getX() + (getW() - glyphLayout.width) / 2f;
            else if (align == Align.right) drawX = getX() + getW() - glyphLayout.width;
        }

        // In Arc (Y-up), font.draw uses Y = top of text.
        font.draw(glyphLayout, drawX, getY() + getH());
        font.setColor(old);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void ensureLayout(float availW) {
        if (Math.abs(cachedAvailW - availW) > 0.5f) {
            if (wrap && availW < Float.MAX_VALUE) {
                glyphLayout.setText(font, text, color, availW, align, true);
            } else {
                glyphLayout.setText(font, text);
            }
            cachedAvailW = availW;
        }
    }

    private void invalidateMeasure() {
        cachedAvailW = -1f;
        invalidate();
    }

    /** Parse label-specific tokens from style string. */
    private void applyStyleOverrides(String style) {
        if (style == null || style.isBlank()) return;
        for (String tok : style.split("\\s+")) {
            switch (tok) {
                case "wrap"         -> this.wrap = true;
                case "align:left"   -> this.align = Align.left;
                case "align:center" -> this.align = Align.center;
                case "align:right"  -> this.align = Align.right;
                case "font:title"   -> this.font = com.neko.libs.ui.style.Theme.fontTitle();
                case "font:mono"    -> this.font = com.neko.libs.ui.style.Theme.fontMono();
                case "font:icon"    -> this.font = com.neko.libs.ui.style.Theme.fontIcon();
            }
            if (tok.startsWith("color:")) {
                String name = tok.substring(6);
                Color c = resolveTextColor(name);
                if (c != null) this.color = c;
            }
        }
    }

    private static Color resolveTextColor(String name) {
        return switch (name) {
            case "bright"    -> com.neko.libs.ui.style.Theme.textBright;
            case "primary"   -> com.neko.libs.ui.style.Theme.textPrimary;
            case "secondary" -> com.neko.libs.ui.style.Theme.textSecondary;
            case "ghost"     -> com.neko.libs.ui.style.Theme.textGhost;
            case "accent"    -> com.neko.libs.ui.style.Theme.textAccent;
            case "red"       -> com.neko.libs.ui.style.Theme.accentRed;
            case "green"     -> com.neko.libs.ui.style.Theme.accentGreen;
            case "blue"      -> com.neko.libs.ui.style.Theme.accentBlue;
            default -> {
                try { yield Color.valueOf(name); }
                catch (Exception e) { yield null; }
            }
        };
    }
}
