package com.neko.libs.simpleui.spec;

public class ContainerSpec extends LayoutSpec {

    @Override
    public ContainerSpec onInvalidate(Runnable r) {
        super.onInvalidate(r);
        return this;
    }

    @Override
    public ContainerSpec w(float v) {
        super.w(v);
        return this;
    }

    @Override
    public ContainerSpec h(float v) {
        super.h(v);
        return this;
    }

    @Override
    public ContainerSpec grow() {
        super.grow();
        return this;
    }

    @Override
    public ContainerSpec growX() {
        super.growX();
        return this;
    }

    @Override
    public ContainerSpec growY() {
        super.growY();
        return this;
    }

    @Override
    public ContainerSpec p(float all) {
        super.p(all);
        return this;
    }

    @Override
    public ContainerSpec p(float v, float h) {
        super.p(v, h);
        return this;
    }

    @Override
    public ContainerSpec p(float t, float r, float b, float l) {
        super.p(t, r, b, l);
        return this;
    }

    @Override
    public ContainerSpec pt(float v) {
        super.pt(v);
        return this;
    }

    @Override
    public ContainerSpec pb(float v) {
        super.pb(v);
        return this;
    }

    @Override
    public ContainerSpec pl(float v) {
        super.pl(v);
        return this;
    }

    @Override
    public ContainerSpec pr(float v) {
        super.pr(v);
        return this;
    }

    @Override
    public ContainerSpec minW(float v) {
        super.minW(v);
        return this;
    }

    @Override
    public ContainerSpec maxW(float v) {
        super.maxW(v);
        return this;
    }

    @Override
    public ContainerSpec minH(float v) {
        super.minH(v);
        return this;
    }

    @Override
    public ContainerSpec maxH(float v) {
        super.maxH(v);
        return this;
    }

    @Override
    public ContainerSpec fixedWidth(float v) {
        super.fixedWidth(v);
        return this;
    }

    @Override
    public ContainerSpec fixedHeight(float v) {
        super.fixedHeight(v);
        return this;
    }

    @Override
    public ContainerSpec col() {
        super.col();
        return this;
    }

    @Override
    public ContainerSpec row() {
        super.row();
        return this;
    }

    @Override
    public ContainerSpec gap(float v) {
        super.gap(v);
        return this;
    }

    @Override
    public ContainerSpec justify(Justify v) {
        super.justify(v);
        return this;
    }

    @Override
    public ContainerSpec items(Items v) {
        super.items(v);
        return this;
    }
}
