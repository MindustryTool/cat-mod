package com.neko.libs.ui.style;

import arc.graphics.Color;
import arc.math.Interp;
import com.neko.libs.ui.style.StyleSpec.Align.Items;
import com.neko.libs.ui.style.StyleSpec.Align.Justify;
import com.neko.libs.ui.style.StyleSpec.SizeMode;
import com.neko.libs.ui.style.StyleSpec.Variant;

public class StyleSpecBuilder {
    private final StyleSpec spec = new StyleSpec();

    public StyleSpec build() { return spec; }

    public StyleSpecBuilder col()       { spec.col(); return this; }
    public StyleSpecBuilder row()       { spec.row(); return this; }
    public StyleSpecBuilder gap(float v)    { spec.gap = v; return this; }
    public StyleSpecBuilder hidden(boolean v) { spec.hidden = v; return this; }

    public StyleSpecBuilder textColor(Color v)  { spec.textColor = v; return this; }
    public StyleSpecBuilder textAlign(int v)    { spec.textAlign = v; return this; }
    public StyleSpecBuilder wrap(boolean v)     { spec.wrap = v; return this; }
    public StyleSpecBuilder widthMode(SizeMode v)  { spec.widthMode = v; return this; }
    public StyleSpecBuilder heightMode(SizeMode v) { spec.heightMode = v; return this; }
    public StyleSpecBuilder fixedWidth(float v)   { spec.fixedWidth = v; return this; }
    public StyleSpecBuilder fixedHeight(float v)  { spec.fixedHeight = v; return this; }
    public StyleSpecBuilder growWeightX(float v)  { spec.growWeightX = v; return this; }
    public StyleSpecBuilder growWeightY(float v)  { spec.growWeightY = v; return this; }

    public StyleSpecBuilder grow()    { spec.grow(); return this; }
    public StyleSpecBuilder growX()   { spec.growX(); return this; }
    public StyleSpecBuilder growY()   { spec.growY(); return this; }
    public StyleSpecBuilder w(float v)  { spec.w(v); return this; }
    public StyleSpecBuilder h(float v)  { spec.h(v); return this; }

    public StyleSpecBuilder p(float all)      { spec.p(all); return this; }
    public StyleSpecBuilder px(float v)       { spec.px(v); return this; }
    public StyleSpecBuilder py(float v)       { spec.py(v); return this; }
    public StyleSpecBuilder pt(float v)       { spec.pt(v); return this; }
    public StyleSpecBuilder pb(float v)       { spec.pb(v); return this; }
    public StyleSpecBuilder pl(float v)       { spec.pl(v); return this; }
    public StyleSpecBuilder pr(float v)       { spec.pr(v); return this; }

    public StyleSpecBuilder justify(Justify v) { spec.justify(v); return this; }
    public StyleSpecBuilder items(Items v)     { spec.items(v); return this; }

    public StyleSpecBuilder bg(Color v)       { spec.bg(v); return this; }
    public StyleSpecBuilder border(Color v)   { spec.border(v); return this; }
    public StyleSpecBuilder borderWidth(float v) { spec.borderWidth(v); return this; }
    public StyleSpecBuilder opacity(float v)  { spec.opacity(v); return this; }

    public StyleSpecBuilder minW(float v)  { spec.minW(v); return this; }
    public StyleSpecBuilder maxW(float v)  { spec.maxW(v); return this; }
    public StyleSpecBuilder minH(float v)  { spec.minH(v); return this; }
    public StyleSpecBuilder maxH(float v)  { spec.maxH(v); return this; }

    public StyleSpecBuilder duration(float v) { spec.duration(v); return this; }
    public StyleSpecBuilder ease(Interp v)    { spec.ease(v); return this; }

    public StyleSpecBuilder variant(Variant v)  { spec.variant(v); return this; }
    public StyleSpecBuilder disabled(boolean v) { spec.disabled(v); return this; }
}
