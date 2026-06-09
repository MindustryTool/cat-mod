package org.mindustrytool.ui.layout;

import arc.scene.Element;

import org.mindustrytool.ui.components.Component;
import org.mindustrytool.ui.layout.Sizing.SizeMode;
import org.mindustrytool.ui.spec.LayoutSpec;
import org.mindustrytool.ui.spec.LayoutSpec.Items;
import org.mindustrytool.ui.spec.LayoutSpec.Justify;

public class LayoutEngine {

    public static float prefWidth(Sizing spec, boolean isColumn, float gap, Iterable<Element> children) {
        return prefAxis(spec, isColumn, true, gap, children);
    }

    public static float prefHeight(Sizing spec, boolean isColumn, float gap, Iterable<Element> children) {
        return prefAxis(spec, isColumn, false, gap, children);
    }

    private static float prefAxis(Sizing spec, boolean isColumn, boolean axisX, float gap, Iterable<Element> children) {
        float total = axisX ? spec.padH() : spec.padV();
        SizeMode fixedMode = axisX ? spec.widthMode() : spec.heightMode();
        float fixedSize = axisX ? spec.fixedWidth() : spec.fixedHeight();
        if (fixedMode == SizeMode.FIXED) return axisX ? spec.constrainW(fixedSize) : spec.constrainH(fixedSize);

        float maxChild = 0f;
        int childCount = 0;
        boolean mainAxis = (isColumn != axisX);
        for (Element c : children) {
            if (!c.visible) continue;
            Sizing cs = sizingOf(c);
            float cv;
            if (cs == null) {
                cv = axisX ? childPrefWidth(c) : childPrefHeight(c);
            } else {
                SizeMode childFixed = axisX ? cs.widthMode() : cs.heightMode();
                float cf = (childFixed == SizeMode.FIXED)
                    ? (axisX ? cs.fixedWidth() : cs.fixedHeight())
                    : (axisX ? childPrefWidth(c) : childPrefHeight(c));
                cv = axisX ? cs.constrainW(cf) : cs.constrainH(cf);
            }
            if (mainAxis) { total += cv; } else { maxChild = Math.max(maxChild, cv); }
            childCount++;
        }
        if (!mainAxis) total += maxChild;
        if (childCount > 1) total += gap * (childCount - 1);
        return axisX ? spec.constrainW(total) : spec.constrainH(total);
    }

    public static void layout(LayoutSpec spec, Iterable<Element> children, float x, float y, float w, float h) {
        float gap = spec.gap();
        float contentW = w, contentH = h;

        int count = 0;
        for (Element c : children) { if (c.visible) count++; }
        if (count == 0) return;

        float[] ws = new float[count];
        float[] hs = new float[count];
        computeSizes(spec, children, contentW, contentH, gap, count, ws, hs);

        float extra, totalMain = 0f;
        boolean isColumn = spec.isColumn();
        for (int i = 0; i < count; i++) totalMain += isColumn ? hs[i] : ws[i];
        totalMain += gap * (count - 1);
        extra = (isColumn ? contentH : contentW) - totalMain;

        float[] offsets = computeJustifyOffsets(extra, count, gap, spec.justify());
        float cp = (isColumn ? y + contentH : x) + offsets[0];
        int idx = 0;
        for (Element c : children) {
            if (!c.visible) continue;
            if (isColumn) {
                cp -= hs[idx];
                float cx = itemPos(x, contentW, ws[idx], spec.items());
                if (spec.items() == Items.STRETCH) ws[idx] = contentW;
                c.setBounds(cx, cp, ws[idx], hs[idx]);
                cp -= offsets[idx + 1];
            } else {
                float cy = itemPos(y, contentH, hs[idx], spec.items());
                if (spec.items() == Items.STRETCH) hs[idx] = contentH;
                c.setBounds(cp, cy, ws[idx], hs[idx]);
                cp += ws[idx] + offsets[idx + 1];
            }
            idx++;
        }
    }

    private static void computeSizes(LayoutSpec spec, Iterable<Element> children,
                                      float contentW, float contentH, float gap,
                                      int count, float[] ws, float[] hs) {
        boolean isColumn = spec.isColumn();
        float totalGap = gap * (count - 1);
        float avail = (isColumn ? contentH : contentW) - totalGap;
        int idx = 0, growCount = 0;
        float totalGrow = 0f, nonGrow = 0f;
        for (Element c : children) {
            if (!c.visible) continue;
            Sizing cs = sizingOf(c);
            if (cs == null) {
                if (isColumn) { hs[idx] = childPrefHeight(c); nonGrow += hs[idx]; ws[idx] = childPrefWidth(c); }
                else { ws[idx] = childPrefWidth(c); nonGrow += ws[idx]; hs[idx] = childPrefHeight(c); }
            } else {
                boolean grow = isColumn ? (cs.heightMode() == SizeMode.GROW) : (cs.widthMode() == SizeMode.GROW);
                float mainSize = isColumn ? childHeight(c, cs) : childWidth(c, cs);
                if (grow) { mainSize = 0f; growCount++; totalGrow += isColumn ? cs.growWeightY() : cs.growWeightX(); }
                else { nonGrow += mainSize; }
                if (isColumn) { hs[idx] = mainSize; ws[idx] = childWidth(c, cs); }
                else { ws[idx] = mainSize; hs[idx] = childHeight(c, cs); }
            }
            idx++;
        }
        avail -= nonGrow;
        if (growCount > 0 && avail > 0) {
            idx = 0;
            for (Element c : children) {
                if (!c.visible) continue;
                Sizing cs = sizingOf(c);
                if (cs != null) {
                    boolean grow = isColumn ? (cs.heightMode() == SizeMode.GROW) : (cs.widthMode() == SizeMode.GROW);
                    if (grow) {
                        float share = (totalGrow > 0f
                            ? ((isColumn ? cs.growWeightY() : cs.growWeightX()) / totalGrow)
                            : (1f / growCount)) * avail;
                        if (isColumn) hs[idx] = share; else ws[idx] = share;
                    }
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
            default -> {
                offsets[0] = 0f;
                for (int i = 1; i <= count; i++) offsets[i] = gap;
            }
        }
        return offsets;
    }

    private static float itemPos(float start, float contentSize, float itemSize, Items items) {
        return switch (items) {
            case CENTER -> start + (contentSize - itemSize) / 2f;
            case END -> start + (contentSize - itemSize);
            default -> start;
        };
    }

    private static float childWidth(Element e, Sizing s) {
        float w = (s.widthMode() == SizeMode.FIXED) ? s.fixedWidth() : childPrefWidth(e);
        return s.constrainW(w);
    }

    private static float childHeight(Element e, Sizing s) {
        float h = (s.heightMode() == SizeMode.FIXED) ? s.fixedHeight() : childPrefHeight(e);
        return s.constrainH(h);
    }

    private static float childPrefWidth(Element e) { return e.getPrefWidth(); }
    private static float childPrefHeight(Element e) { return e.getPrefHeight(); }

    public static Sizing sizingOf(Element e) {
        Object o = e.userObject;
        if (o instanceof Component) return ((Component) o).sizing();
        return null;
    }
}