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

    /** Parent width (set by containing El before layout). */
    public float pw = 0f;
    /** Parent height (set by containing El before layout). */
    public float ph = 0f;

    // ── Parent-dimension shadow stack ──────────────────────────────────────
    // Each push stores the previous pw/ph so pop can restore them.
    // Depth-first layout guarantees paired LIFO usage.

    private float prevPw, prevPh;

    /** Push {@code pw/ph} to {@code w/h}, saving current values for restoration. */
    public void pushParent(float w, float h) {
        prevPw = pw;
        prevPh = ph;
        pw = w;
        ph = h;
    }

    /** Restore {@code pw/ph} to the values saved at the preceding {@link #pushParent}. */
    public void popParent() {
        pw = prevPw;
        ph = prevPh;
    }

    private LayoutCtx() {}

    /** Call once before each layout pass. */
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
            case "pw"  -> pw;
            case "ph"  -> ph;
            default    -> 0f;
        };
    }
}
