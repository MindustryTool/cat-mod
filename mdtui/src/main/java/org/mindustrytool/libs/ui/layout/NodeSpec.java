package org.mindustrytool.libs.ui.layout;

import lombok.Getter;

@SuppressWarnings({"UnusedReturnValue", "unchecked"})
public class NodeSpec<T extends NodeSpec<T>> {

    public enum SizeMode {
        WRAP,
        GROW,
        FIXED
    }

    public enum AlignSelf {
        AUTO,
        START,
        CENTER,
        END,
        STRETCH
    }

    protected Runnable onInvalidateCallback;

    private @Getter SizeMode widthMode = SizeMode.WRAP;
    private @Getter SizeMode heightMode = SizeMode.WRAP;
    private @Getter float fixedWidth = 0.0f;
    private @Getter float fixedHeight = 0.0f;
    private @Getter float growWeightHorizontal = 1.0f;
    private @Getter float growWeightVertical = 1.0f;
    private @Getter AlignSelf alignSelf = AlignSelf.AUTO;

    private @Getter float paddingTop = 0.0f;
    private @Getter float paddingBottom = 0.0f;
    private @Getter float paddingLeft = 0.0f;
    private @Getter float paddingRight = 0.0f;

    private @Getter float minimumWidth = -1.0f;
    private @Getter float maximumWidth = -1.0f;
    private @Getter float minimumHeight = -1.0f;
    private @Getter float maximumHeight = -1.0f;

    public void onInvalidate(Runnable callback) {
        this.onInvalidateCallback = callback;
    }

    protected void invalidate() {
        if (onInvalidateCallback != null) onInvalidateCallback.run();
    }

    public T widthMode(SizeMode mode) {
        this.widthMode = mode;
        invalidate();
        return (T) this;
    }

    public T heightMode(SizeMode mode) {
        this.heightMode = mode;
        invalidate();
        return (T) this;
    }

    public T fixedWidth(float width) {
        this.widthMode = SizeMode.FIXED;
        this.fixedWidth = width;
        invalidate();
        return (T) this;
    }

    public T fixedHeight(float height) {
        this.heightMode = SizeMode.FIXED;
        this.fixedHeight = height;
        invalidate();
        return (T) this;
    }

    public T growWeightHorizontal(float weight) {
        this.growWeightHorizontal = weight;
        invalidate();
        return (T) this;
    }

    public T growWeightVertical(float weight) {
        this.growWeightVertical = weight;
        invalidate();
        return (T) this;
    }

    public T alignSelf(AlignSelf alignSelf) {
        this.alignSelf = alignSelf;
        invalidate();
        return (T) this;
    }

    public T grow() {
        this.widthMode = SizeMode.GROW;
        this.heightMode = SizeMode.GROW;
        invalidate();
        return (T) this;
    }

    public T growX() {
        this.widthMode = SizeMode.GROW;
        invalidate();
        return (T) this;
    }

    public T growY() {
        this.heightMode = SizeMode.GROW;
        invalidate();
        return (T) this;
    }

    public T width(float width) {
        return fixedWidth(width);
    }

    public T height(float height) {
        return fixedHeight(height);
    }

    public T padding(float all) {
        return padding(all, all, all, all);
    }

    public T padding(float vertical, float horizontal) {
        return padding(vertical, horizontal, vertical, horizontal);
    }

    public T padding(float top, float right, float bottom, float left) {
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
        this.paddingLeft = left;
        invalidate();
        return (T) this;
    }

    public T paddingTop(float padding) {
        this.paddingTop = padding;
        invalidate();
        return (T) this;
    }

    public T paddingBottom(float padding) {
        this.paddingBottom = padding;
        invalidate();
        return (T) this;
    }

    public T paddingLeft(float padding) {
        this.paddingLeft = padding;
        invalidate();
        return (T) this;
    }

    public T paddingRight(float padding) {
        this.paddingRight = padding;
        invalidate();
        return (T) this;
    }

    public T minimumWidth(float width) {
        this.minimumWidth = width;
        invalidate();
        return (T) this;
    }

    public T maximumWidth(float width) {
        this.maximumWidth = width;
        invalidate();
        return (T) this;
    }

    public T minimumHeight(float height) {
        this.minimumHeight = height;
        invalidate();
        return (T) this;
    }

    public T maximumHeight(float height) {
        this.maximumHeight = height;
        invalidate();
        return (T) this;
    }

    public T reset(boolean invalidate) {
        widthMode = SizeMode.WRAP;
        heightMode = SizeMode.WRAP;
        fixedWidth = 0f;
        fixedHeight = 0f;
        growWeightHorizontal = 1f;
        growWeightVertical = 1f;
        alignSelf = AlignSelf.AUTO;
        paddingTop = 0f;
        paddingBottom = 0f;
        paddingLeft = 0f;
        paddingRight = 0f;
        minimumWidth = -1f;
        maximumWidth = -1f;
        minimumHeight = -1f;
        maximumHeight = -1f;

        if (invalidate) invalidate();
        return (T) this;
    }

    public float getHorizontalPadding() {
        return paddingLeft + paddingRight;
    }

    public float getVerticalPadding() {
        return paddingTop + paddingBottom;
    }

    public float constrainWidth(float value) {
        if (minimumWidth >= 0.0f && value < minimumWidth) {
            value = minimumWidth;
        }
        if (maximumWidth >= 0.0f && value > maximumWidth) {
            value = maximumWidth;
        }
        return value;
    }

    public float constrainHeight(float value) {
        if (minimumHeight >= 0.0f && value < minimumHeight) {
            value = minimumHeight;
        }
        if (maximumHeight >= 0.0f && value > maximumHeight) {
            value = maximumHeight;
        }
        return value;
    }
}
