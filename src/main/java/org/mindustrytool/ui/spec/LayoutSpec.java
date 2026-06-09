package org.mindustrytool.ui.spec;

import org.mindustrytool.ui.layout.Sizing;
import org.mindustrytool.ui.layout.NodeSizing;
import arc.func.Cons;

public class LayoutSpec {

    public enum Justify { START, CENTER, END, BETWEEN, AROUND }
    public enum Items { START, CENTER, END, STRETCH }

    public final NodeSizing sizing = new NodeSizing();
    boolean column;
    float gap;
    Justify justify = Justify.START;
    Items items = Items.STRETCH;

    public Sizing sizing() { return sizing; }

    public LayoutSpec col() { column = true; return this; }
    public LayoutSpec row() { column = false; return this; }
    public LayoutSpec gap(float v) { gap = v; return this; }
    public LayoutSpec justify(Justify v) { justify = v; return this; }
    public LayoutSpec items(Items v) { items = v; return this; }
    public LayoutSpec p(float all) { sizing.p(all); return this; }
    public LayoutSpec p(float v, float h) { sizing.p(v, h); return this; }
    public LayoutSpec p(float t, float r, float b, float l) { sizing.p(t, r, b, l); return this; }
    public LayoutSpec minW(float v) { sizing.minW(v); return this; }
    public LayoutSpec maxW(float v) { sizing.maxW(v); return this; }
    public LayoutSpec minH(float v) { sizing.minH(v); return this; }
    public LayoutSpec maxH(float v) { sizing.maxH(v); return this; }
    public LayoutSpec size(Cons<NodeSizing> fn) { fn.get(sizing); return this; }
    public LayoutSpec onInvalidate(Runnable r) { sizing.onInvalidate(r); return this; }

    public boolean isColumn() { return column; }
    public float gap() { return gap; }
    public Justify justify() { return justify; }
    public Items items() { return items; }
}