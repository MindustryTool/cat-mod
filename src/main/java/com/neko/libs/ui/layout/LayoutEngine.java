package com.neko.libs.ui.layout;

import arc.scene.Element;
import arc.struct.Seq;
import com.neko.libs.ui.El;
import com.neko.libs.ui.style.StyleSpec;
import com.neko.libs.ui.style.StyleSpec.Align.Items;
import com.neko.libs.ui.style.StyleSpec.Align.Justify;
import com.neko.libs.ui.style.StyleSpec.SizeMode;

public final class LayoutEngine {
    private LayoutEngine() {}

    private static StyleSpec specOf(Element e) {
        if (e instanceof El) return ((El) e).resolveSpec();
        return null;
    }

    // ── Preferred sizing ──────────────────────────────────────────────────

    public static float prefWidth(StyleSpec spec, Seq<Element> children) {
        Seq<Element> vis = visible(children);
        if (vis.size == 0) return 0f;
        if (spec.isColumn()) {
            float max = 0f;
            for (Element c : vis) max = Math.max(max, prefW(c, specOf(c), false, 0f));
            return max;
        } else {
            float total = 0f;
            for (Element c : vis) total += prefW(c, specOf(c), false, 0f);
            return total + spec.gap() * Math.max(0, vis.size - 1);
        }
    }

    public static float prefHeight(StyleSpec spec, Seq<Element> children) {
        Seq<Element> vis = visible(children);
        if (vis.size == 0) return 0f;
        if (spec.isColumn()) {
            float total = 0f;
            for (Element c : vis) total += prefH(c, specOf(c), true, 0f);
            return total + spec.gap() * Math.max(0, vis.size - 1);
        } else {
            float max = 0f;
            for (Element c : vis) max = Math.max(max, prefH(c, specOf(c), true, 0f));
            return max;
        }
    }

    // ── Main layout ───────────────────────────────────────────────────────

    public static void layout(StyleSpec spec, Seq<Element> children,
                               float innerX, float innerY, float innerW, float innerH) {
        Seq<Element> vis = visible(children);
        int n = vis.size;
        if (n == 0) return;

        boolean isCol   = spec.isColumn();
        float   mainSp  = isCol ? innerH : innerW;
        float   crossSp = isCol ? innerW : innerH;
        float[] sizes   = new float[n];
        float   sumFixed = 0f, totalGrowW = 0f;

        // ── Pass 1: measure ───────────────────────────────────────────────
        for (int i = 0; i < n; i++) {
            Element c = vis.get(i);
            StyleSpec cs = specOf(c);
            SizeMode mainMode = isCol ? (cs != null ? cs.heightMode() : SizeMode.WRAP)
                                      : (cs != null ? cs.widthMode() : SizeMode.WRAP);

            switch (mainMode) {
                case FIXED -> {
                    float fixed = cs != null ? (isCol ? cs.fixedHeight() : cs.fixedWidth()) : 0f;
                    sizes[i]  = cs != null ? (isCol ? cs.constrainH(fixed) : cs.constrainW(fixed)) : fixed;
                    sumFixed += sizes[i];
                }
                case GROW -> {
                    sizes[i]    = Float.NaN;
                    float w = cs != null ? (isCol ? cs.growWeightY() : cs.growWeightX()) : 1f;
                    totalGrowW += w;
                }
                default -> { // WRAP
                    sizes[i] = isCol ? prefH(c, cs, true, crossSp) : prefW(c, cs, false, crossSp);
                    if (cs != null) sizes[i] = isCol ? cs.constrainH(sizes[i]) : cs.constrainW(sizes[i]);
                    sumFixed += sizes[i];
                }
            }
        }

        // ── Pass 2: distribute flex ───────────────────────────────────────
        float totalGap  = spec.gap() * Math.max(0, n - 1);
        float flexSpace = Math.max(0f, mainSp - sumFixed - totalGap);

        for (int i = 0; i < n; i++) {
            if (!Float.isNaN(sizes[i])) continue;
            Element c = vis.get(i);
            StyleSpec cs = specOf(c);
            float weight = cs != null ? (isCol ? cs.growWeightY() : cs.growWeightX()) : 1f;
            float size   = totalGrowW > 0f ? flexSpace * weight / totalGrowW : 0f;
            sizes[i] = cs != null ? (isCol ? cs.constrainH(size) : cs.constrainW(size)) : size;
        }

        // ── Justify ───────────────────────────────────────────────────────
        float sumAll    = 0f;
        for (float s : sizes) sumAll += s;
        float totalUsed    = sumAll + totalGap;
        Justify justify    = spec.justify();

        float betweenExtra = (justify == Justify.BETWEEN && n > 1)
            ? Math.max(0f, mainSp - sumAll) / (n - 1)
            : 0f;
        float effectiveGap = spec.gap() + betweenExtra;

        // ── Pass 3: position ──────────────────────────────────────────────
        if (isCol) {
            float curY = switch (justify) {
                case START   -> innerY + innerH;
                case END     -> innerY + totalUsed;
                case CENTER  -> innerY + (innerH + totalUsed) / 2f;
                case BETWEEN -> innerY + innerH;
            };

            for (int i = 0; i < n; i++) {
                Element c = vis.get(i);
                float childH = sizes[i];
                float childW = crossSize(c, specOf(c), innerW, crossSp, true);
                float cx     = crossPos(specOf(c), spec.items(), innerX, innerW, childW, true);

                curY -= childH;
                c.setBounds(cx, curY, childW, childH);
                if (i < n - 1) curY -= effectiveGap;
            }
        } else {
            float curX = switch (justify) {
                case START   -> innerX;
                case END     -> innerX + innerW - totalUsed;
                case CENTER  -> innerX + (innerW - totalUsed) / 2f;
                case BETWEEN -> innerX;
            };

            for (int i = 0; i < n; i++) {
                Element c = vis.get(i);
                float childW = sizes[i];
                float childH = crossSize(c, specOf(c), innerH, crossSp, false);
                float cy     = crossPos(specOf(c), spec.items(), innerY, innerH, childH, false);

                c.setBounds(curX, cy, childW, childH);
                curX += childW;
                if (i < n - 1) curX += effectiveGap;
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static float prefW(Element e, StyleSpec cs, boolean isCol, float crossSp) {
        if (cs != null && cs.widthMode() == SizeMode.FIXED) return cs.constrainW(cs.fixedWidth());
        return e.getPrefWidth();
    }

    private static float prefH(Element e, StyleSpec cs, boolean isCol, float crossSp) {
        if (cs != null && cs.heightMode() == SizeMode.FIXED) return cs.constrainH(cs.fixedHeight());
        return e.getPrefHeight();
    }

    private static float crossSize(Element e, StyleSpec cs,
                                    float innerCross, float crossSp, boolean isCol) {
        SizeMode mode = cs != null ? (isCol ? cs.widthMode() : cs.heightMode()) : SizeMode.WRAP;
        if (mode == SizeMode.GROW)
            return cs != null ? (isCol ? cs.constrainW(innerCross) : cs.constrainH(innerCross)) : innerCross;
        if (mode == SizeMode.FIXED) {
            float v = cs != null ? (isCol ? cs.fixedWidth() : cs.fixedHeight()) : 0f;
            return cs != null ? (isCol ? cs.constrainW(v) : cs.constrainH(v)) : v;
        }
        float pref = isCol ? e.getPrefWidth() : e.getPrefHeight();
        return cs != null ? (isCol ? cs.constrainW(pref) : cs.constrainH(pref)) : pref;
    }

    private static float crossPos(StyleSpec cs, Items containerItems,
                                   float innerStart, float innerSize,
                                   float childSize, boolean isCol) {
        Items items = cs != null && cs.items() != Items.STRETCH ? cs.items() : containerItems;
        return switch (items) {
            case CENTER  -> innerStart + (innerSize - childSize) / 2f;
            case END     -> innerStart + innerSize - childSize;
            default      -> innerStart;
        };
    }

    private static Seq<Element> visible(Seq<Element> children) {
        Seq<Element> vis = new Seq<>();
        for (Element c : children) {
            if (c.visible) vis.add(c);
        }
        return vis;
    }
}
