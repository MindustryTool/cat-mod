package com.neko.libs.ui.style;

import arc.graphics.Color;
import arc.math.Interp;

/**
 * Resolved style for one layout pass.
 *
 * <h3>Lazy sub-objects</h3>
 * Only the fields actually present in the style string are allocated.
 * A bare {@code "col gap:8"} element creates no {@link Pad}, {@link Visual},
 * {@link Align}, {@link Constraint}, or {@link Transition} objects.
 *
 * <p>Access all properties through the safe accessor methods on this class
 * ({@link #padH()}, {@link #background()}, {@link #justify()}, …).
 * These return sensible defaults when the sub-object is {@code null}.
 */
public final class StyleSpec {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Core — always present, always read by layout engine
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /** {@code true} = column (top→bottom), {@code false} = row (left→right). */
    public boolean isColumn = true;

    public float gap = 0f;

    /** Hides this element and all its children from layout and drawing. */
    public boolean hidden = false;

    // ── Self sizing (always read, so kept inline) ─────────────────────────

    public enum SizeMode { WRAP, GROW, FIXED }

    public SizeMode widthMode  = SizeMode.WRAP;
    public SizeMode heightMode = SizeMode.WRAP;
    public float    fixedWidth  = 0f;
    public float    fixedHeight = 0f;
    public float    growWeightX = 1f;
    public float    growWeightY = 1f;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Lazy sub-objects — null when not used
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /** Inner padding (space between border and children). Null = zero. */
    public Pad pad;

    /** Cross/main-axis alignment. Null = justify:start, items:stretch. */
    public Align align;

    /** Background, border, opacity. Null = no visual decoration. */
    public Visual visual;

    /** min/max size constraints. Null = unconstrained. */
    public Constraint constraint;

    /** CSS-like transition animation. Null = instant. */
    public Transition transition;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Sub-object classes
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    public static final class Pad {
        public float top, right, bottom, left;
    }

    public static final class Align {
        public enum Justify { START, CENTER, END, BETWEEN }
        public enum Items   { START, CENTER, END, STRETCH }
        public Justify justify = Justify.START;
        public Items   items   = Items.STRETCH;
    }

    public static final class Visual {
        public Color background  = null;
        public Color borderColor = null;
        public float borderWidth = 1f;
        public float opacity     = 1f;
    }

    public static final class Constraint {
        public float minWidth = -1f, maxWidth  = -1f;
        public float minHeight = -1f, maxHeight = -1f;
    }

    public static final class Transition {
        public float  duration = 150f;
        public Interp ease     = Interp.smooth;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Lazy init helpers (used by StyleParser)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    public Pad        pad()        { return pad        != null ? pad        : (pad        = new Pad());        }
    public Align      align()      { return align      != null ? align      : (align      = new Align());      }
    public Visual     visual()     { return visual     != null ? visual     : (visual     = new Visual());     }
    public Constraint constraint() { return constraint != null ? constraint : (constraint = new Constraint()); }
    public Transition transition() { return transition != null ? transition : (transition = new Transition()); }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Safe accessors — return defaults when sub-object is null
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    // Padding
    public float padH()      { return pad != null ? pad.left + pad.right  : 0f; }
    public float padV()      { return pad != null ? pad.top  + pad.bottom : 0f; }
    public float padLeft()   { return pad != null ? pad.left   : 0f; }
    public float padRight()  { return pad != null ? pad.right  : 0f; }
    public float padTop()    { return pad != null ? pad.top    : 0f; }
    public float padBottom() { return pad != null ? pad.bottom : 0f; }

    // Alignment
    public Align.Justify justify() {
        return align != null ? align.justify : Align.Justify.START;
    }
    public Align.Items items() {
        return align != null ? align.items : Align.Items.STRETCH;
    }

    // Visual
    public Color  background()  { return visual != null ? visual.background  : null;  }
    public Color  borderColor() { return visual != null ? visual.borderColor : null;  }
    public float  borderWidth() { return visual != null ? visual.borderWidth : 1f;    }
    public float  opacity()     { return visual != null ? visual.opacity     : 1f;    }

    // Constraints
    public float minWidth()  { return constraint != null ? constraint.minWidth  : -1f; }
    public float maxWidth()  { return constraint != null ? constraint.maxWidth  : -1f; }
    public float minHeight() { return constraint != null ? constraint.minHeight : -1f; }
    public float maxHeight() { return constraint != null ? constraint.maxHeight : -1f; }

    // Transition
    public float  transitionDuration() { return transition != null ? transition.duration : 0f;          }
    public Interp transitionEase()     { return transition != null ? transition.ease     : Interp.linear; }

    // ── Utility ───────────────────────────────────────────────────────────────

    public float constrain(float v, float min, float max) {
        if (min >= 0f && v < min) v = min;
        if (max >= 0f && v > max) v = max;
        return v;
    }

    public float constrainW(float v) { return constrain(v, minWidth(),  maxWidth());  }
    public float constrainH(float v) { return constrain(v, minHeight(), maxHeight()); }
}
