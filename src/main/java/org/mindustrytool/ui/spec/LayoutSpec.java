package org.mindustrytool.ui.spec;

import org.mindustrytool.ui.layout.NodeSizing;

public class LayoutSpec extends NodeSizing {

    public enum Justify { START, CENTER, END, BETWEEN, AROUND }
    public enum Items { START, CENTER, END, STRETCH }

    boolean column;
    float gap;
    Justify justify = Justify.START;
    Items items = Items.STRETCH;

    @Override public LayoutSpec onInvalidate(Runnable r) { super.onInvalidate(r); return this; }
    @Override public LayoutSpec w(float v) { super.w(v); return this; }
    @Override public LayoutSpec h(float v) { super.h(v); return this; }
    @Override public LayoutSpec grow() { super.grow(); return this; }
    @Override public LayoutSpec growX() { super.growX(); return this; }
    @Override public LayoutSpec growY() { super.growY(); return this; }
    @Override public LayoutSpec p(float all) { super.p(all); return this; }
    @Override public LayoutSpec p(float v, float h) { super.p(v, h); return this; }
    @Override public LayoutSpec p(float t, float r, float b, float l) { super.p(t, r, b, l); return this; }
    @Override public LayoutSpec pt(float v) { super.pt(v); return this; }
    @Override public LayoutSpec pb(float v) { super.pb(v); return this; }
    @Override public LayoutSpec pl(float v) { super.pl(v); return this; }
    @Override public LayoutSpec pr(float v) { super.pr(v); return this; }
    @Override public LayoutSpec minW(float v) { super.minW(v); return this; }
    @Override public LayoutSpec maxW(float v) { super.maxW(v); return this; }
    @Override public LayoutSpec minH(float v) { super.minH(v); return this; }
    @Override public LayoutSpec maxH(float v) { super.maxH(v); return this; }
    @Override public LayoutSpec fixedWidth(float v) { super.fixedWidth(v); return this; }
    @Override public LayoutSpec fixedHeight(float v) { super.fixedHeight(v); return this; }

    public LayoutSpec col() { column = true; return this; }
    public LayoutSpec row() { column = false; return this; }
    public LayoutSpec gap(float v) { gap = v; return this; }
    public LayoutSpec justify(Justify v) { justify = v; return this; }
    public LayoutSpec items(Items v) { items = v; return this; }

    public boolean isColumn() { return column; }
    public float gap() { return gap; }
    public Justify justify() { return justify; }
    public Items items() { return items; }
}
