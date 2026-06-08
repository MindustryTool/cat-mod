package com.neko.libs.simpleui.layout;

import arc.scene.Element;
import arc.util.pooling.Pool;
import com.neko.libs.simpleui.components.UIComponent;
import com.neko.libs.simpleui.layout.Sizing.SizeMode;
import com.neko.libs.simpleui.spec.LayoutSpec;
import com.neko.libs.simpleui.spec.LayoutSpec.Items;
import com.neko.libs.simpleui.spec.LayoutSpec.Justify;

import java.util.Arrays;

public class LayoutEngine {
    private static final Pool<float[]> floats = new Pool<>(10) {
        @Override
        protected float[] newObject() { return new float[8]; }
    };

    public static float prefWidth(Sizing spec, boolean isColumn, float gap, Iterable<Element> children) {
        float total = spec.padH();
        if (spec.widthMode() == SizeMode.FIXED) return spec.constrainW(spec.fixedWidth());
        float maxChild = 0f;
        int childCount = 0;
        for (Element c : children) {
            Sizing cs = sizingOf(c);
            if (cs == null) continue;
            float cw = (cs.widthMode() == SizeMode.FIXED || cs.widthMode() == SizeMode.GROW)
                ? cs.fixedWidth() : childPrefWidth(c, cs);
            cw = cs.constrainW(cw);
            if (isColumn) { maxChild = Math.max(maxChild, cw); } else { total += cw; }
            childCount++;
        }
        if (isColumn) total += maxChild;
        if (childCount > 1) total += gap * (childCount - 1);
        return spec.constrainW(total);
    }

    public static float prefHeight(Sizing spec, boolean isColumn, float gap, Iterable<Element> children) {
        float total = spec.padV();
        if (spec.heightMode() == SizeMode.FIXED) return spec.constrainH(spec.fixedHeight());
        int childCount = 0;
        float maxChild = 0f;
        for (Element c : children) {
            Sizing cs = sizingOf(c);
            if (cs == null) continue;
            float ch = (cs.heightMode() == SizeMode.FIXED || cs.heightMode() == SizeMode.GROW)
                ? cs.fixedHeight() : childPrefHeight(c, cs);
            ch = cs.constrainH(ch);
            if (isColumn) { total += ch; } else { maxChild = Math.max(maxChild, ch); }
            childCount++;
        }
        if (!isColumn) total += maxChild;
        if (childCount > 1) total += gap * (childCount - 1);
        return spec.constrainH(total);
    }

    public static void layout(LayoutSpec spec, Iterable<Element> children, float x, float y, float w, float h) {
        float gap = spec.gap();
        float contentW = w, contentH = h;

        int count = 0;
        for (Element ignored : children) count++;
        if (count == 0) return;

        float[] ws = floats.obtain();
        float[] hs = floats.obtain();
        if (ws.length < count) { ws = new float[count]; } else { Arrays.fill(ws, 0, count, 0f); }
        if (hs.length < count) { hs = new float[count]; } else { Arrays.fill(hs, 0, count, 0f); }

        if (spec.isColumn()) {
            computeColumnSizes(spec, children, contentW, contentH, gap, count, ws, hs);
            float totalH = 0f;
            for (int i = 0; i < count; i++) totalH += hs[i];
            totalH += gap * (count - 1);
            float extra = contentH - totalH;

            float[] offsets = computeJustifyOffsets(extra, count, gap, spec.justify());
            float cy = y + contentH - offsets[0];
            int idx = 0;
            for (Element c : children) {
                if (sizingOf(c) == null) { idx++; continue; }
                cy -= hs[idx];
                float cx = itemX(x, contentW, ws[idx], spec.items());
                if (spec.items() == Items.STRETCH) ws[idx] = contentW;
                c.setBounds(cx, cy, ws[idx], hs[idx]);
                cy -= offsets[idx + 1];
                idx++;
            }
        } else {
            computeRowSizes(spec, children, contentW, contentH, gap, count, ws, hs);
            float totalW = 0f;
            for (int i = 0; i < count; i++) totalW += ws[i];
            totalW += gap * (count - 1);
            float extra = contentW - totalW;

            float[] offsets = computeJustifyOffsets(extra, count, gap, spec.justify());
            float cx = x + offsets[0];
            int idx = 0;
            for (Element c : children) {
                if (sizingOf(c) == null) { idx++; continue; }
                float cy = itemY(y, contentH, hs[idx], spec.items());
                if (spec.items() == Items.STRETCH) hs[idx] = contentH;
                c.setBounds(cx, cy, ws[idx], hs[idx]);
                cx += ws[idx] + offsets[idx + 1];
                idx++;
            }
        }
        floats.free(ws);
        floats.free(hs);
    }

    private static void computeColumnSizes(LayoutSpec spec, Iterable<Element> children,
                                            float contentW, float contentH, float gap,
                                            int count, float[] ws, float[] hs) {
        float totalGap = gap * (count - 1);
        float availH = contentH - totalGap;
        int idx = 0, growCount = 0;
        float totalGrowH = 0f, nonGrowH = 0f;
        for (Element c : children) {
            Sizing cs = sizingOf(c);
            if (cs == null) { idx++; continue; }
            hs[idx] = childPrefHeight(c, cs);
            if (cs.heightMode() == SizeMode.GROW) { hs[idx] = 0f; growCount++; totalGrowH += cs.growWeightY(); }
            else { nonGrowH += hs[idx]; }
            ws[idx] = childWidth(c, cs);
            idx++;
        }
        availH -= nonGrowH;
        if (growCount > 0 && availH > 0) {
            idx = 0;
            for (Element c : children) {
                Sizing cs = sizingOf(c);
                if (cs != null && cs.heightMode() == SizeMode.GROW) {
                    hs[idx] = (totalGrowH > 0f ? (cs.growWeightY() / totalGrowH) : (1f / growCount)) * availH;
                }
                idx++;
            }
        }
    }

    private static void computeRowSizes(LayoutSpec spec, Iterable<Element> children,
                                         float contentW, float contentH, float gap,
                                         int count, float[] ws, float[] hs) {
        float totalGap = gap * (count - 1);
        float availW = contentW - totalGap;
        int idx = 0, growCount = 0;
        float totalGrowW = 0f, nonGrowW = 0f;
        for (Element c : children) {
            Sizing cs = sizingOf(c);
            if (cs == null) { idx++; continue; }
            ws[idx] = childPrefWidth(c, cs);
            if (cs.widthMode() == SizeMode.GROW) { ws[idx] = 0f; growCount++; totalGrowW += cs.growWeightX(); }
            else { nonGrowW += ws[idx]; }
            hs[idx] = childHeight(c, cs);
            idx++;
        }
        availW -= nonGrowW;
        if (growCount > 0 && availW > 0) {
            idx = 0;
            for (Element c : children) {
                Sizing cs = sizingOf(c);
                if (cs != null && cs.widthMode() == SizeMode.GROW) {
                    ws[idx] = (totalGrowW > 0f ? (cs.growWeightX() / totalGrowW) : (1f / growCount)) * availW;
                }
                idx++;
            }
        }
    }

    private static float[] computeJustifyOffsets(float extra, int count, float gap,
                                                  Justify justify) {
        float[] offsets = new float[count + 1];
        switch (justify) {
            case END -> {
                offsets[0] = extra;
                for (int i = 1; i <= count; i++) offsets[i] = gap;
            }
            case CENTER -> {
                float half = extra / 2f;
                offsets[0] = half;
                for (int i = 1; i <= count; i++) offsets[i] = gap;
            }
            case BETWEEN -> {
                float between = (count > 1) ? extra / (count - 1) : 0f;
                offsets[0] = 0f;
                for (int i = 1; i <= count; i++) offsets[i] = gap + between;
            }
            case AROUND -> {
                float around = extra / count;
                offsets[0] = around;
                for (int i = 1; i <= count; i++) offsets[i] = gap + around * 2f;
            }
            default -> { // START
                offsets[0] = 0f;
                for (int i = 1; i <= count; i++) offsets[i] = gap;
            }
        }
        return offsets;
    }

    private static float itemX(float x, float contentW, float cw, Items items) {
        return switch (items) {
            case CENTER -> x + (contentW - cw) / 2f;
            case END -> x + (contentW - cw);
            default -> x;
        };
    }

    private static float itemY(float y, float contentH, float ch, Items items) {
        return switch (items) {
            case CENTER -> y + (contentH - ch) / 2f;
            case END -> y + (contentH - ch);
            default -> y;
        };
    }

    private static float childWidth(Element e, Sizing s) {
        return (s.widthMode() == SizeMode.GROW || s.widthMode() == SizeMode.FIXED)
            ? s.constrainW(s.fixedWidth()) : childPrefWidth(e, s);
    }

    private static float childHeight(Element e, Sizing s) {
        return (s.heightMode() == SizeMode.GROW || s.heightMode() == SizeMode.FIXED)
            ? s.constrainH(s.fixedHeight()) : childPrefHeight(e, s);
    }

    private static float childPrefWidth(Element e, Sizing s) { return e.getPrefWidth(); }
    private static float childPrefHeight(Element e, Sizing s) { return e.getPrefHeight(); }

    public static Sizing sizingOf(Element e) {
        Object o = e.userObject;
        if (o instanceof UIComponent) return ((UIComponent) o).sizing();
        return null;
    }
}
