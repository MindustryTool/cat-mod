package com.neko.libs.ui.style;

import arc.graphics.Color;
import arc.graphics.g2d.Font;
import mindustry.ui.Fonts;

/**
 * Centralized design tokens for NekoUI.
 *
 * <p>All colors are shared instances — do NOT call {@link Color#set} or
 * other mutating methods on them. Use {@link Color#cpy()} first if you
 * need a modified copy.
 *
 * <p>Fonts are retrieved lazily via Arc's {@link Fonts} registry.
 */
public final class Theme {

    private Theme() {}

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Background colors
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /** Deepest background — scene/modal backdrop. */
    public static final Color bgVoid    = Color.valueOf("0d0d0f");
    /** Default panel background. */
    public static final Color bgBase    = Color.valueOf("141418");
    /** Elevated surface (card, panel). */
    public static final Color bgSurface = Color.valueOf("1c1c22");
    /** Slightly raised element (hover state, secondary panel). */
    public static final Color bgRaised  = Color.valueOf("26262f");
    /** Input/field background. */
    public static final Color bgInput   = Color.valueOf("2a2a35");

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Border colors
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    public static final Color borderSubtle  = Color.valueOf("2e2e3a");
    public static final Color borderDefault = Color.valueOf("44445a");
    public static final Color borderActive  = Color.valueOf("7a7aaa");

    /** Default border stroke width in pixels. */
    public static final float BORDER_W = 1f;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Accent / status colors
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    public static final Color accentPrimary = Color.valueOf("6c8ebf");
    public static final Color accentGold    = Color.valueOf("f0c040");
    public static final Color accentRed     = Color.valueOf("d9534f");
    public static final Color accentGreen   = Color.valueOf("5cb85c");
    public static final Color accentBlue    = Color.valueOf("5bc0de");

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Text colors
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /** High-contrast white — headings, important values. */
    public static final Color textBright    = Color.valueOf("f2f2f5");
    /** Default readable text. */
    public static final Color textPrimary   = Color.valueOf("c8c8d8");
    /** De-emphasized text — labels, captions. */
    public static final Color textSecondary = Color.valueOf("8888aa");
    /** Disabled / placeholder text. */
    public static final Color textGhost     = Color.valueOf("55556a");
    /** Accent-colored text — links, highlights. */
    public static final Color textAccent    = accentGold;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Fonts (lazy — Fonts registry initialized after game boot)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /** Default UI font. */
    public static Font fontDefault() { return Fonts.def; }

    /** Larger title font (falls back to def if unavailable). */
    public static Font fontTitle() {
        return Fonts.def; // swap with a larger font if your mod ships one
    }

    /** Monospace / technical font. */
    public static Font fontMono() {
        return Fonts.def;
    }

    /** Icon font (e.g. for Mindustry icon glyphs). */
    public static Font fontIcon() {
        return Fonts.icon != null ? Fonts.icon : Fonts.def;
    }
}
