package org.mindustrytool.ui.layout;

public class NodeSizing implements Sizing {
    protected Runnable onInvalidate;

    SizeMode widthMode = SizeMode.WRAP, heightMode = SizeMode.WRAP;
    float fixedWidth, fixedHeight;
    float growWeightX = 1f, growWeightY = 1f;
    float padTop, padBottom, padLeft, padRight;
    float minWidth = -1f, maxWidth = -1f, minHeight = -1f, maxHeight = -1f;

    public NodeSizing onInvalidate(Runnable r) { this.onInvalidate = r; return this; }

    @Override public SizeMode widthMode() { return widthMode; }
    @Override public SizeMode heightMode() { return heightMode; }
    @Override public float fixedWidth() { return fixedWidth; }
    @Override public float fixedHeight() { return fixedHeight; }
    @Override public float growWeightX() { return growWeightX; }
    @Override public float growWeightY() { return growWeightY; }
    @Override public float padTop() { return padTop; }
    @Override public float padBottom() { return padBottom; }
    @Override public float padLeft() { return padLeft; }
    @Override public float padRight() { return padRight; }
    @Override public float minWidth() { return minWidth; }
    @Override public float maxWidth() { return maxWidth; }
    @Override public float minHeight() { return minHeight; }
    @Override public float maxHeight() { return maxHeight; }

    public NodeSizing widthMode(SizeMode v) { widthMode = v; return this; }
    public NodeSizing heightMode(SizeMode v) { heightMode = v; return this; }
    public NodeSizing fixedWidth(float v) { widthMode = SizeMode.FIXED; fixedWidth = v; return this; }
    public NodeSizing fixedHeight(float v) { heightMode = SizeMode.FIXED; fixedHeight = v; return this; }
    public NodeSizing growWeightX(float v) { growWeightX = v; return this; }
    public NodeSizing growWeightY(float v) { growWeightY = v; return this; }
    public NodeSizing grow() { widthMode = SizeMode.GROW; heightMode = SizeMode.GROW; return this; }
    public NodeSizing growX() { widthMode = SizeMode.GROW; return this; }
    public NodeSizing growY() { heightMode = SizeMode.GROW; return this; }
    public NodeSizing w(float v) { widthMode = SizeMode.FIXED; fixedWidth = v; return this; }
    public NodeSizing h(float v) { heightMode = SizeMode.FIXED; fixedHeight = v; return this; }
    public NodeSizing p(float all) { return p(all, all, all, all); }
    public NodeSizing p(float v, float h) { return p(v, h, v, h); }
    public NodeSizing p(float t, float r, float b, float l) {
        padTop = t; padRight = r; padBottom = b; padLeft = l; return this;
    }
    public NodeSizing pt(float v) { padTop = v; return this; }
    public NodeSizing pb(float v) { padBottom = v; return this; }
    public NodeSizing pl(float v) { padLeft = v; return this; }
    public NodeSizing pr(float v) { padRight = v; return this; }
    public NodeSizing minW(float v) { minWidth = v; return this; }
    public NodeSizing maxW(float v) { maxWidth = v; return this; }
    public NodeSizing minH(float v) { minHeight = v; return this; }
    public NodeSizing maxH(float v) { maxHeight = v; return this; }
}
