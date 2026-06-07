package com.neko.libs.ui.layout;

import arc.Core;

/**
 * Context passed through every layout pass.
 * Provides resolved environment variables usable in style expressions.
 */
public final class LayoutCtx {

    public static final LayoutCtx INSTANCE = new LayoutCtx();

    /** Device-independent scale factor (1.0 on most desktops, >1 on hi-DPI / mobile). */
    public float scl = 1f;
    /** Current screen width in pixels. */
    public float sw  = 800f;
    /** Current screen height in pixels. */
    public float sh  = 600f;

    private LayoutCtx() {}

    /** Call once before each layout pass (NPanel does this automatically). */
    public void refresh() {
        if (Core.graphics == null) return;
        sw  = Core.graphics.getWidth();
        sh  = Core.graphics.getHeight();
        scl = Core.graphics.getDensity();
        if (scl <= 0f) scl = 1f;
    }

    /** Resolve a named variable for use in style expressions. */
    public float resolve(String name) {
        return switch (name) {
            case "scl" -> scl;
            case "sw"  -> sw;
            case "sh"  -> sh;
            default    -> 0f;
        };
    }
}
