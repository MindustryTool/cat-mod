package com.neko.libs.ui;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import com.neko.libs.state.State;
import com.neko.libs.ui.layout.LayoutCtx;
import com.neko.libs.ui.layout.LayoutEngine;
import com.neko.libs.ui.style.StyleParser;
import com.neko.libs.ui.style.StyleSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Core node of the NekoUI tree.
 *
 * <h3>Two construction modes</h3>
 * <pre>
 * // Single style (most common)
 * new El("col gap:8 p:16")
 *
 * // Named variants — style selected by a State<String>
 * new El(variantState,
 *     "desktop: col gap:8  p:16 w:400",
 *     "mobile:  col gap:4  p:8  w:{sw-16}")
 * </pre>
 *
 * <h3>Building the tree</h3>
 * <pre>
 * El root = new El("col gap:8").add(
 *     new NLabel("Hello"),
 *     new NDivider()
 * );
 * </pre>
 */
public class El {

    // ── Style variants ────────────────────────────────────────────────────────
    // Key = variant label ("" for single-style mode), Value = raw style string.
    private final Map<String, String> variants = new LinkedHashMap<>();

    // If non-null, active variant key is driven by this state.
    private State<String> variantState = null;

    // ── Tree structure ────────────────────────────────────────────────────────
    protected final List<El>  children = new ArrayList<>();
    private         El        parent   = null;

    // ── Layout result ─────────────────────────────────────────────────────────
    float x, y, w, h;

    // ── Dirty flag ────────────────────────────────────────────────────────────
    private boolean dirty = true;

    // ── Resolved spec (set during layout pass) ────────────────────────────────
    protected StyleSpec spec = null;

    // ── Visibility (reactive, external) ──────────────────────────────────────
    private boolean visible = true;

    // ── Animation (opacity) ───────────────────────────────────────────────────
    private float curOpacity  = 1f;
    private float animElapsed = 0f;

    // ── Events ────────────────────────────────────────────────────────────────
    private Runnable onClick  = null;
    boolean hovered  = false;
    boolean pressed  = false;

    // ── State bindings (for cleanup on detach) ────────────────────────────────
    private final List<Runnable> detachCallbacks = new ArrayList<>();

    // ── Constructors ──────────────────────────────────────────────────────────

    public El(String style) {
        variants.put("", style);
    }

    /**
     * Named-variant constructor.
     * Each {@code variantDef} must be {@code "label: style tokens..."}.
     */
    public El(State<String> variantState, String... variantDefs) {
        this.variantState = variantState;
        for (String def : variantDefs) {
            int colon = def.indexOf(':');
            if (colon < 0) {
                variants.put("", def.trim());
            } else {
                variants.put(def.substring(0, colon).trim(),
                             def.substring(colon + 1).trim());
            }
        }
        // Auto-invalidate when variant key changes
        detachCallbacks.add(variantState.subscribe(v -> invalidate()));
    }

    // ── Tree building ─────────────────────────────────────────────────────────

    /** Add one or more children. Returns {@code this} for chaining. */
    public El add(El... els) {
        for (El e : els) {
            if (e.parent != null) e.parent.children.remove(e);
            e.parent = this;
            children.add(e);
        }
        invalidate();
        return this;
    }

    public El remove(El e) {
        if (children.remove(e)) {
            e.parent = null;
            invalidate();
        }
        return this;
    }

    public void clearChildren() {
        for (El c : children) c.parent = null;
        children.clear();
        invalidate();
    }

    // ── Visibility ────────────────────────────────────────────────────────────

    public El setVisible(boolean v) {
        if (this.visible != v) { this.visible = v; invalidate(); }
        return this;
    }

    public boolean isVisible() { return visible; }

    public <T> El visibleWhen(State<T> state, java.util.function.Predicate<T> pred) {
        return bind(state, v -> setVisible(pred.test(v)));
    }

    // ── Events ────────────────────────────────────────────────────────────────

    public El onClick(Runnable r)  { this.onClick = r; return this; }
    boolean isTouchable()          { return onClick != null; }

    public void fireClick()               { if (onClick != null) onClick.run(); }
    public void setHovered(boolean h)     { this.hovered = h; invalidate(); }
    public void setPressed(boolean p)     { this.pressed = p; invalidate(); }
    public boolean isHovered()            { return hovered; }
    public boolean isPressed()            { return pressed; }

    // ── State binding ─────────────────────────────────────────────────────────

    public <T> El bind(State<T> state, Cons<T> apply) {
        detachCallbacks.add(state.onChange(apply));
        return this;
    }

    // ── Hit testing ───────────────────────────────────────────────────────────

    public boolean hitTest(float px, float py) {
        return px >= x && px < x + w && py >= y && py < y + h;
    }

    /**
     * Returns the deepest touchable El at (px, py), or {@code null}.
     * Searches children back-to-front (last child rendered on top).
     */
    public El hitAt(float px, float py) {
        if (!visible || spec == null || spec.hidden) return null;
        if (!hitTest(px, py)) return null;
        for (int i = children.size() - 1; i >= 0; i--) {
            El hit = children.get(i).hitAt(px, py);
            if (hit != null) return hit;
        }
        return isTouchable() ? this : null;
    }

    // ── Invalidation ──────────────────────────────────────────────────────────

    public void invalidate() {
        if (dirty) return;
        dirty = true;
        if (parent != null) parent.invalidate();
    }

    public boolean isDirty() { return dirty; }

    // ── Style resolution ──────────────────────────────────────────────────────

    public StyleSpec resolveSpec(LayoutCtx ctx) {
        return StyleParser.parse(activeStyle(), ctx);
    }

    private String activeStyle() {
        if (variantState != null) {
            String key = variantState.getValue();
            if (variants.containsKey(key)) return variants.get(key);
        }
        // Fallback: first variant
        return variants.isEmpty() ? "" : variants.values().iterator().next();
    }

    // ── Preferred sizing (called by LayoutEngine) ─────────────────────────────

    public float prefWidth(LayoutCtx ctx, float availW) {
        StyleSpec s = resolveSpec(ctx);
        if (!visible || s.hidden) return 0f;
        if (s.widthMode == StyleSpec.SizeMode.FIXED)
            return s.constrainW(s.fixedWidth);
        if (s.widthMode == StyleSpec.SizeMode.GROW) return 0f;
        // WRAP: measure content
        float inner = Math.max(0f, availW - s.padH());
        float cw = contentPrefWidth(ctx, inner, s);
        return s.constrainW(cw + s.padH());
    }

    public float prefHeight(LayoutCtx ctx, float availW, float availH) {
        StyleSpec s = resolveSpec(ctx);
        if (!visible || s.hidden) return 0f;
        if (s.heightMode == StyleSpec.SizeMode.FIXED)
            return s.constrainH(s.fixedHeight);
        if (s.heightMode == StyleSpec.SizeMode.GROW) return 0f;
        float innerW = Math.max(0f, availW - s.padH());
        float ch = contentPrefHeight(ctx, innerW, availH, s);
        return s.constrainH(ch + s.padV());
    }

    /** Override in subclasses (leaf widgets) to report content size. */
    protected float contentPrefWidth(LayoutCtx ctx, float innerW, StyleSpec s) {
        return LayoutEngine.prefWidth(ctx, s, children, innerW);
    }

    protected float contentPrefHeight(LayoutCtx ctx, float innerW, float availH, StyleSpec s) {
        return LayoutEngine.prefHeight(ctx, s, children, innerW, availH);
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    public void layout(LayoutCtx ctx, float x, float y, float w, float h) {
        this.x = x; this.y = y; this.w = w; this.h = h;
        this.dirty = false;

        spec = resolveSpec(ctx);
        if (!visible || spec.hidden) return;

        float innerX = x + spec.padLeft();
        float innerY = y + spec.padBottom();   // Arc Y-up: padBottom = space at the bottom
        float innerW = Math.max(0f, w - spec.padH());
        float innerH = Math.max(0f, h - spec.padV());

        LayoutEngine.layout(ctx, spec, children, innerX, innerY, innerW, innerH);
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    public void draw() {
        if (!visible || spec == null || spec.hidden) return;

        float alpha = spec.opacity() * curOpacity;

        // Background
        if (spec.background() != null) {
            Color prev = Draw.getColor().cpy();
            Draw.color(spec.background(), alpha);
            Fill.rect(x + w / 2f, y + h / 2f, w, h);
            Draw.color(prev);
        }

        // Border
        if (spec.borderColor() != null) {
            Color prev       = Draw.getColor().cpy();
            float prevStroke = Lines.getStroke();
            Draw.color(spec.borderColor(), alpha);
            Lines.stroke(spec.borderWidth());
            Lines.rect(x, y, w, h);
            Lines.stroke(prevStroke);
            Draw.color(prev);
        }

        // Content (overridden by widget subclasses)
        drawContent(alpha);

        // Children
        for (El child : children) child.draw();
    }

    /** Override to draw widget-specific content (text, icon, etc.). */
    protected void drawContent(float alpha) {}

    // ── Getters (for external access) ─────────────────────────────────────────

    public float getX() { return x; }
    public float getY() { return y; }
    public float getW() { return w; }
    public float getH() { return h; }

    // ── Detach (cleanup) ──────────────────────────────────────────────────────

    public void detach() {
        for (Runnable r : detachCallbacks) r.run();
        detachCallbacks.clear();
        for (El child : children) child.detach();
        if (parent != null) parent.remove(this);
    }
}
