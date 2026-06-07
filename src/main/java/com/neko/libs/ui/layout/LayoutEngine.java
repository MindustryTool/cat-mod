package com.neko.libs.ui.layout;

import com.neko.libs.ui.El;
import com.neko.libs.ui.style.StyleSpec;
import com.neko.libs.ui.style.StyleSpec.Align.Items;
import com.neko.libs.ui.style.StyleSpec.Align.Justify;
import com.neko.libs.ui.style.StyleSpec.SizeMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Flex-like layout engine for the {@link El} tree.
 *
 * <h3>Fixes over old LinearLayout</h3>
 * <ul>
 *   <li>Gap applied only <em>between</em> children (not after the last).</li>
 *   <li>justify:center / end / between fully implemented.</li>
 *   <li>items:start / center / end / stretch implemented.</li>
 * </ul>
 *
 * <h3>Arc Y-axis</h3>
 * Y=0 is at the screen bottom; Y increases upward.
 * Column layout cursor starts at {@code innerY + innerH} (top) and moves down.
 */
public final class LayoutEngine {
    private LayoutEngine() {}

    // ── Preferred sizing ──────────────────────────────────────────────────────

    public static float prefWidth(LayoutCtx ctx, StyleSpec spec,
                                  List<El> children, float availW) {
        List<El> vis = visible(children);
        if (vis.isEmpty()) return 0f;
        if (spec.isColumn) {
            float max = 0f;
            for (El c : vis) max = Math.max(max, c.prefWidth(ctx, availW));
            return max;
        } else {
            float total = 0f;
            for (El c : vis) total += c.prefWidth(ctx, availW);
            return total + spec.gap * Math.max(0, vis.size() - 1);
        }
    }

    public static float prefHeight(LayoutCtx ctx, StyleSpec spec,
                                   List<El> children, float availW, float availH) {
        List<El> vis = visible(children);
        if (vis.isEmpty()) return 0f;
        if (spec.isColumn) {
            float total = 0f;
            for (El c : vis) total += c.prefHeight(ctx, availW, availH);
            return total + spec.gap * Math.max(0, vis.size() - 1);
        } else {
            float max = 0f;
            for (El c : vis) max = Math.max(max, c.prefHeight(ctx, availW, availH));
            return max;
        }
    }

    // ── Layout pass ───────────────────────────────────────────────────────────

    public static void layout(LayoutCtx ctx, StyleSpec spec, List<El> children,
                               float innerX, float innerY, float innerW, float innerH) {
        List<El> vis = visible(children);
        int n = vis.size();
        if (n == 0) return;

        boolean isCol    = spec.isColumn;
        float   mainSp   = isCol ? innerH : innerW;
        float   crossSp  = isCol ? innerW : innerH;
        float[] sizes    = new float[n];
        float   sumFixed = 0f, totalGrowW = 0f;

        // ── Pass 1: FIXED and WRAP ────────────────────────────────────────────
        for (int i = 0; i < n; i++) {
            El c = vis.get(i);
            StyleSpec cs = c.resolveSpec(ctx);
            SizeMode mainMode = isCol ? cs.heightMode : cs.widthMode;

            switch (mainMode) {
                case FIXED -> {
                    float fixed = isCol ? cs.fixedHeight : cs.fixedWidth;
                    sizes[i]  = isCol ? cs.constrainH(fixed) : cs.constrainW(fixed);
                    sumFixed += sizes[i];
                }
                case GROW -> {
                    sizes[i]    = Float.NaN;
                    totalGrowW += isCol ? cs.growWeightY : cs.growWeightX;
                }
                default -> { // WRAP
                    float pref = isCol
                        ? c.prefHeight(ctx, Math.max(0f, crossSp), mainSp)
                        : c.prefWidth(ctx, mainSp);
                    sizes[i]  = isCol ? cs.constrainH(pref) : cs.constrainW(pref);
                    sumFixed += sizes[i];
                }
            }
        }

        // ── Pass 2: distribute flex space to GROW ────────────────────────────
        float totalGap  = spec.gap * Math.max(0, n - 1);
        float flexSpace = Math.max(0f, mainSp - sumFixed - totalGap);

        for (int i = 0; i < n; i++) {
            if (Float.isNaN(sizes[i])) {
                El c = vis.get(i);
                StyleSpec cs = c.resolveSpec(ctx);
                float weight = isCol ? cs.growWeightY : cs.growWeightX;
                float size   = totalGrowW > 0f ? flexSpace * weight / totalGrowW : 0f;
                sizes[i] = isCol ? cs.constrainH(size) : cs.constrainW(size);
            }
        }

        // ── Justify ───────────────────────────────────────────────────────────
        float sumAll    = 0f;
        for (float s : sizes) sumAll += s;
        float totalUsed    = sumAll + totalGap;
        Justify justify    = spec.justify();

        float betweenExtra = (justify == Justify.BETWEEN && n > 1)
            ? Math.max(0f, mainSp - sumAll) / (n - 1)
            : 0f;
        float effectiveGap = spec.gap + betweenExtra;

        // ── Pass 3: position ──────────────────────────────────────────────────
        if (isCol) {
            float curY = switch (justify) {
                case START   -> innerY + innerH;                      // top
                case END     -> innerY + totalUsed;                   // bottom-align
                case CENTER  -> innerY + (innerH + totalUsed) / 2f;
                case BETWEEN -> innerY + innerH;
            };

            for (int i = 0; i < n; i++) {
                El c = vis.get(i);
                float childH = sizes[i];
                float childW = crossSize(ctx, c, c.resolveSpec(ctx), innerW, crossSp, true);
                float cx     = crossPos(c.resolveSpec(ctx), spec.items(), innerX, innerW, childW, true);

                curY -= childH;
                c.layout(ctx, cx, curY, childW, childH);
                if (i < n - 1) curY -= effectiveGap;   // ← gap only between children
            }

        } else {
            float curX = switch (justify) {
                case START   -> innerX;
                case END     -> innerX + innerW - totalUsed;
                case CENTER  -> innerX + (innerW - totalUsed) / 2f;
                case BETWEEN -> innerX;
            };

            for (int i = 0; i < n; i++) {
                El c = vis.get(i);
                float childW = sizes[i];
                float childH = crossSize(ctx, c, c.resolveSpec(ctx), innerH, crossSp, false);
                float cy     = crossPos(c.resolveSpec(ctx), spec.items(), innerY, innerH, childH, false);

                c.layout(ctx, curX, cy, childW, childH);
                curX += childW;
                if (i < n - 1) curX += effectiveGap;   // ← gap only between children
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static float crossSize(LayoutCtx ctx, El c, StyleSpec cs,
                                   float innerCross, float crossSp, boolean isCol) {
        SizeMode mode = isCol ? cs.widthMode : cs.heightMode;
        if (mode == SizeMode.GROW)
            return isCol ? cs.constrainW(innerCross) : cs.constrainH(innerCross);
        if (mode == SizeMode.FIXED)
            return isCol ? cs.constrainW(cs.fixedWidth) : cs.constrainH(cs.fixedHeight);
        // WRAP
        float pref = isCol
            ? c.prefWidth(ctx, innerCross)
            : c.prefHeight(ctx, crossSp, crossSp);
        return isCol ? cs.constrainW(pref) : cs.constrainH(pref);
    }

    private static float crossPos(StyleSpec cs, Items containerItems,
                                   float innerStart, float innerSize,
                                   float childSize, boolean isCol) {
        Items items = cs.items() != Items.STRETCH ? cs.items() : containerItems;
        return switch (items) {
            case CENTER  -> innerStart + (innerSize - childSize) / 2f;
            case END     -> innerStart + innerSize - childSize;       // top (Y-up) / right (X)
            default      -> innerStart;                                // START or STRETCH
        };
    }

    private static List<El> visible(List<El> children) {
        List<El> vis = new ArrayList<>(children.size());
        for (El c : children) { if (c.isVisible()) vis.add(c); }
        return vis;
    }
}
