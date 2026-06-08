package com.neko.libs.ui;

import arc.func.Cons;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.scene.ui.layout.WidgetGroup;
import com.neko.libs.signal.Signal;
import com.neko.libs.ui.layout.LayoutCtx;
import com.neko.libs.ui.layout.LayoutEngine;
import com.neko.libs.ui.style.StyleParser;
import com.neko.libs.ui.style.StyleSpec;
import com.neko.libs.ui.style.StyleSpec.SizeMode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class El extends WidgetGroup {

    // ── Style variants ────────────────────────────────────────────────────

    private final Map<String, String> variants = new LinkedHashMap<>();
    private Signal<String> variantState = null;

    // ── Spec ──────────────────────────────────────────────────────────────

    protected StyleSpec spec = null;
    private boolean specExplicit = false;
    private boolean bound = false;
    private Runnable bind;

    // ── Hover / press (for child widgets; containers ignore these) ────────

    boolean hovered  = false;
    boolean pressed  = false;

    // ── Cleanup on detach ─────────────────────────────────────────────────

    private final List<Runnable> detachCallbacks = new ArrayList<>();

    // ── Constructors ──────────────────────────────────────────────────────

    public El() {
        setTransform(true);
    }

    public El(String style) {
        setTransform(true);
        variants.put("", style);
    }

    public El(Signal<String> variantState, String... variantDefs) {
        setTransform(true);
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
        detachCallbacks.add(variantState.onChange(v -> invalidate()));
    }

    public El(StyleSpec spec) {
        setTransform(true);
        this.spec = spec;
        this.specExplicit = true;
    }

    // ── Bind (runs once on first layout) ─────────────────────────────────────

    public El bind(Runnable r) {
        this.bind = r;
        return this;
    }

    public <T> El bind(Signal<T> state, Cons<T> apply) {
        detachCallbacks.add(state.onChange(apply));
        return this;
    }

    // ── Hover / press ────────────────────────────────────────────────────────

    public void setHovered(boolean h) { this.hovered = h; }
    public void setPressed(boolean p) { this.pressed = p; }
    public boolean isHovered()        { return hovered; }
    public boolean isPressed()        { return pressed; }

    // ── Style resolution ─────────────────────────────────────────────────────

    public StyleSpec resolveSpec() {
        return resolveSpec(LayoutCtx.INSTANCE);
    }

    public StyleSpec resolveSpec(LayoutCtx ctx) {
        if (specExplicit) return spec;
        spec = StyleParser.parse(activeStyle(), ctx);
        if (!bound) { bound = true; if (bind != null) bind.run(); }
        return spec;
    }

    private String activeStyle() {
        if (variantState != null) {
            String key = variantState.get();
            if (variants.containsKey(key)) return variants.get(key);
        }
        return variants.isEmpty() ? "" : variants.values().iterator().next();
    }

    // ── Preferred sizing ─────────────────────────────────────────────────────

    @Override
    public float getPrefWidth() {
        if (spec == null) return 0f;
        if (spec.widthMode() == SizeMode.FIXED) return spec.constrainW(spec.fixedWidth());
        if (spec.widthMode() == SizeMode.GROW) return 0f;
        float contentW = LayoutEngine.prefWidth(spec, getChildren());
        return spec.constrainW(contentW + spec.padH());
    }

    @Override
    public float getPrefHeight() {
        if (spec == null) return 0f;
        if (spec.heightMode() == SizeMode.FIXED) return spec.constrainH(spec.fixedHeight());
        if (spec.heightMode() == SizeMode.GROW) return 0f;
        float contentH = LayoutEngine.prefHeight(spec, getChildren());
        return spec.constrainH(contentH + spec.padV());
    }

    // ── Layout ───────────────────────────────────────────────────────────────

    @Override
    public void layout() {
        resolveSpec();
        if (!visible) return;
        if (spec != null && spec.hidden()) return;

        float w = getWidth();
        float h = getHeight();

        if (spec != null) {
            LayoutCtx.INSTANCE.pushParent(w, h);

            float innerX = spec.padLeft();
            float innerY = spec.padBottom();
            float innerW = Math.max(0f, w - spec.padH());
            float innerH = Math.max(0f, h - spec.padV());

            LayoutEngine.layout(spec, getChildren(), innerX, innerY, innerW, innerH);

            LayoutCtx.INSTANCE.popParent();
        }
    }

    // ── Drawing ──────────────────────────────────────────────────────────────

    @Override
    public void draw() {
        if (transform) applyTransform(computeTransform());

        validate();

        if (spec != null && !spec.hidden()) {
            if (spec.background() != null) {
                Draw.color(spec.background(), spec.opacity());
                Fill.rect(getWidth() / 2f, getHeight() / 2f, getWidth(), getHeight());
            }
            if (spec.borderColor() != null) {
                Draw.color(spec.borderColor(), spec.opacity());
                Lines.stroke(spec.borderWidth());
                Lines.rect(0, 0, getWidth(), getHeight());
            }
            Draw.color();
            Lines.stroke(1f);
        }

        drawChildren();

        if (transform) resetTransform();
    }

    // ── Cleanup ──────────────────────────────────────────────────────────────

    @Override
    public boolean remove() {
        for (Runnable r : detachCallbacks) r.run();
        detachCallbacks.clear();
        return super.remove();
    }
}
