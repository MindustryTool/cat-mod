package org.mindustrytool.ui.layout;

public interface Sizing {

    enum SizeMode {WRAP, GROW, FIXED}

    SizeMode widthMode();

    SizeMode heightMode();

    float fixedWidth();

    float fixedHeight();

    float growWeightX();

    float growWeightY();

    float padLeft();

    float padRight();

    float padTop();

    float padBottom();

    float minWidth();

    float maxWidth();

    float minHeight();

    float maxHeight();

    default float constrainW(float v) {
        float mw = minWidth(), mxw = maxWidth();
        if (mw >= 0f && v < mw) v = mw;
        if (mxw >= 0f && v > mxw) v = mxw;
        return v;
    }

    default float constrainH(float v) {
        float mh = minHeight(), mxh = maxHeight();
        if (mh >= 0f && v < mh) v = mh;
        if (mxh >= 0f && v > mxh) v = mxh;
        return v;
    }

    default float padH() {
        return padLeft() + padRight();
    }

    default float padV() {
        return padTop() + padBottom();
    }
}
