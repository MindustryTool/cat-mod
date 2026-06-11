package org.mindustrytool.mdtui.util;

import arc.graphics.Color;

import lombok.experimental.UtilityClass;

/**
 * Central colour palette — Dracula + macOS Editor inspired.
 * <p>
 * All colours are in {@code RRGGBBAA} hex format.
 */
@UtilityClass
public class Theme {
    // -- Text colours --
    /** Primary text on dark backgrounds. */
    public static final Color TEXT_BRIGHT = Color.valueOf("f2f2f5");
    /** Default body text. */
    public static final Color TEXT_PRIMARY = Color.valueOf("c8c8d8");
    /** Muted / secondary text. */
    public static final Color TEXT_SECONDARY = Color.valueOf("8888aa");
    /** Disabled / placeholder text. */
    public static final Color TEXT_GHOST = Color.valueOf("55556a");
    /** Accent-coloured text (links, highlights). */
    public static final Color TEXT_ACCENT = Color.valueOf("6c8ebf");

    // -- Accent colours --
    /** Primary action / link colour. */
    public static final Color ACCENT_PRIMARY = Color.valueOf("6c8ebf");
    /** Warning / highlight gold. */
    public static final Color ACCENT_GOLD = Color.valueOf("f0c040");
    /** Error / danger red. */
    public static final Color ACCENT_RED = Color.valueOf("d9534f");
    /** Success green. */
    public static final Color ACCENT_GREEN = Color.valueOf("5cb85c");
    /** Info blue. */
    public static final Color ACCENT_BLUE = Color.valueOf("5bc0de");

    // -- Surface colours --
    /** Deepest background (page level). */
    public static final Color BACKGROUND = Color.valueOf("141418");
    /** Card / panel surface. */
    public static final Color SURFACE = Color.valueOf("1c1c22");
    /** Border / divider. */
    public static final Color BORDER = Color.valueOf("303052");

    // -- Dracula & macOS Editor palette (supplemental) --
    /** Dracula editor background. */
    public static final Color DRACULA_BG = Color.valueOf("282a36");
    /** macOS-style red. */
    public static final Color MAC_RED = Color.valueOf("ff5555");
    /** macOS-style yellow. */
    public static final Color MAC_YELLOW = Color.valueOf("ffb86c");
    /** macOS-style green. */
    public static final Color MAC_GREEN = Color.valueOf("50fa7b");
    /** Dracula pink. */
    public static final Color DRACULA_PINK = Color.valueOf("ff79c6");
    /** Dracula cyan. */
    public static final Color DRACULA_CYAN = Color.valueOf("8be9fd");
    /** Dracula yellow. */
    public static final Color DRACULA_YELLOW = Color.valueOf("f1fa8c");
}
