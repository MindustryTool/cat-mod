package com.neko.libs.ui.widget;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import com.neko.libs.ui.El;
import com.neko.libs.ui.style.Theme;
import mindustry.ui.Fonts;

/**
 * Custom-drawn button with hover / press / disabled visual states.
 *
 * <p>Style variant tokens (parsed in addition to base El tokens):
 * <pre>
 *   primary  — gold background
 *   danger   — red background
 *   ghost    — transparent background, no border
 * </pre>
 *
 * <p>Fluent setters take priority over style tokens.
 */
public class NBtn extends El {

    public enum Variant { DEFAULT, PRIMARY, DANGER, GHOST }

    private static final float PAD_H = 16f;
    private static final float PAD_V = 10f;
    private static final float ICON_SIZE = 24f;
    private static final float ICON_GAP  = 8f;

    private String  text;
    private Variant variant  = Variant.DEFAULT;
    private boolean disabled = false;

    private float cachedTextW = -1f;

    // ── Constructors ──────────────────────────────────────────────────────────

    public NBtn(String text, String style) {
        super(style);
        this.text = text;
        parseVariant(style);
        // Buttons are touchable by default
        onClick(() -> {});  // placeholder so hitTest passes; real onClick replaces this
    }

    public NBtn(String text) {
        this(text, "");
    }

    // ── Fluent setters ────────────────────────────────────────────────────────

    @Override
    public NBtn onClick(Runnable r) { super.onClick(r); return this; }

    public NBtn primary()  { this.variant = Variant.PRIMARY; return this; }
    public NBtn danger()   { this.variant = Variant.DANGER;  return this; }
    public NBtn ghost()    { this.variant = Variant.GHOST;   return this; }
    public NBtn disable(boolean d) { this.disabled = d; invalidate(); return this; }

    public void setText(String t) {
        if (!t.equals(this.text)) { this.text = t; cachedTextW = -1f; invalidate(); }
    }

    // ── Sizing ────────────────────────────────────────────────────────────────

    @Override
    protected float contentPrefWidth(com.neko.libs.ui.layout.LayoutCtx ctx, float innerW,
                                     com.neko.libs.ui.style.StyleSpec s) {
        if (cachedTextW < 0f) {
            arc.graphics.g2d.GlyphLayout gl = new arc.graphics.g2d.GlyphLayout();
            gl.setText(Fonts.def, text);
            cachedTextW = gl.width;
        }
        return cachedTextW + PAD_H * 2f;
    }

    @Override
    protected float contentPrefHeight(com.neko.libs.ui.layout.LayoutCtx ctx, float innerW,
                                      float availH, com.neko.libs.ui.style.StyleSpec s) {
        return Fonts.def.getLineHeight() + PAD_V * 2f;
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    @Override
    protected void drawContent(float alpha) {
        boolean hov = isHovered() && !disabled;
        boolean pre = isPressed() && !disabled;

        Color bg = switch (variant) {
            case PRIMARY -> pre ? Theme.accentGold.cpy().mul(0.75f, 0.75f, 0.75f, 1f)
                                : hov ? Theme.accentGold.cpy().mul(0.88f, 0.88f, 0.88f, 1f)
                                      : Theme.accentGold;
            case DANGER  -> pre ? Theme.accentRed
                                : hov ? Theme.accentRed.cpy().mul(1f, 1f, 1f, 0.75f)
                                      : Theme.accentRed.cpy().mul(1f, 1f, 1f, 0.5f);
            case GHOST   -> pre ? Theme.bgInput
                                : hov ? Theme.bgRaised
                                      : Color.clear;
            default      -> pre ? Theme.bgInput
                                : hov ? Theme.bgRaised
                                      : Theme.bgSurface;
        };

        float a = disabled ? 0.4f : alpha;

        // Background
        Color prev = Draw.getColor().cpy();
        Draw.color(bg, a);
        Fill.rect(getX() + getW() / 2f, getY() + getH() / 2f, getW(), getH());

        // Border (skip for ghost)
        if (variant != Variant.GHOST) {
            Color border = (hov || pre) ? Theme.borderActive : Theme.borderDefault;
            float borderA = disabled ? 0.3f : (pre ? 1f : hov ? 0.7f : 1f);
            Draw.color(border, borderA);
            float prevStroke = Lines.getStroke();
            Lines.stroke(Theme.BORDER_W);
            Lines.rect(getX(), getY(), getW(), getH());
            Lines.stroke(prevStroke);
        }

        // Text (vertically centered)
        float yOff = pre ? -1f : 0f;
        float textX = getX() + PAD_H;
        float textY = getY() + getH() / 2f + Fonts.def.getCapHeight() / 2f + yOff;

        arc.graphics.g2d.GlyphLayout gl = new arc.graphics.g2d.GlyphLayout();
        gl.setText(Fonts.def, text);

        Color textCol = variant == Variant.PRIMARY ? Theme.bgVoid : Theme.textPrimary;
        Color oldC = Fonts.def.getColor().cpy();
        Fonts.def.setColor(textCol.r, textCol.g, textCol.b, textCol.a * a);
        Fonts.def.draw(gl, textX, textY);
        Fonts.def.setColor(oldC);

        Draw.color(prev);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void parseVariant(String style) {
        if (style == null) return;
        for (String tok : style.split("\\s+")) {
            switch (tok) {
                case "primary" -> variant = Variant.PRIMARY;
                case "danger"  -> variant = Variant.DANGER;
                case "ghost"   -> variant = Variant.GHOST;
            }
        }
    }
}
