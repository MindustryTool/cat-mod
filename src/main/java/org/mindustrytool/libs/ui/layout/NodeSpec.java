package org.mindustrytool.libs.ui.layout;

import lombok.Getter;

/**
 * NodeSpec defines the sizing policies, padding, and size constraints for a layout element (Node).
 * It provides a fluent builder-like API to configure layout options.
 * All properties are documented in detail, and abbreviations are strictly avoided.
 */
public class NodeSpec {

    /**
     * Sizing mode determines how the node's dimensions are calculated.
     */
    public enum SizeMode {
        /**
         * Size is based on the preferred size of the node's content (wrap content).
         */
        WRAP,
        /**
         * Size expands to fill the remaining available space in the parent container.
         */
        GROW,
        /**
         * Size is fixed to a specific coordinate value.
         */
        FIXED
    }

    /**
     * AlignSelf allows an individual child to override the cross-axis alignment of its parent container.
     */
    public enum AlignSelf {
        /**
         * Inherit cross-axis alignment from the parent container.
         */
        AUTO,
        /**
         * Align to the start boundary of the cross axis.
         */
        START,
        /**
         * Align to the center of the cross axis.
         */
        CENTER,
        /**
         * Align to the end boundary of the cross axis.
         */
        END,
        /**
         * Stretch to fill the entire cross-axis space of the line.
         */
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

    /**
     * Sets the callback to be triggered when any layout property changes.
     *
     * @param callback the invalidation callback runnable
     */
    public void onInvalidate(Runnable callback) {
        this.onInvalidateCallback = callback;
    }

    /**
     * Triggers the invalidation callback to notify the parent container of changes.
     */
    protected void invalidate() {
        if (onInvalidateCallback != null) onInvalidateCallback.run();
    }

    // --- Fluent Setters ---

    public NodeSpec widthMode(SizeMode mode) {
        this.widthMode = mode;
        invalidate();
        return this;
    }

    public NodeSpec heightMode(SizeMode mode) {
        this.heightMode = mode;
        invalidate();
        return this;
    }

    public NodeSpec fixedWidth(float width) {
        this.widthMode = SizeMode.FIXED;
        this.fixedWidth = width;
        invalidate();
        return this;
    }

    public NodeSpec fixedHeight(float height) {
        this.heightMode = SizeMode.FIXED;
        this.fixedHeight = height;
        invalidate();
        return this;
    }

    public NodeSpec growWeightHorizontal(float weight) {
        this.growWeightHorizontal = weight;
        invalidate();
        return this;
    }

    public NodeSpec growWeightVertical(float weight) {
        this.growWeightVertical = weight;
        invalidate();
        return this;
    }

    public NodeSpec alignSelf(AlignSelf alignSelf) {
        this.alignSelf = alignSelf;
        invalidate();
        return this;
    }

    public NodeSpec grow() {
        this.widthMode = SizeMode.GROW;
        this.heightMode = SizeMode.GROW;
        invalidate();
        return this;
    }

    public NodeSpec growX() {
        this.widthMode = SizeMode.GROW;
        invalidate();
        return this;
    }

    public NodeSpec growY() {
        this.heightMode = SizeMode.GROW;
        invalidate();
        return this;
    }

    public NodeSpec width(float width) {
        return fixedWidth(width);
    }

    public NodeSpec height(float height) {
        return fixedHeight(height);
    }

    public NodeSpec padding(float all) {
        return padding(all, all, all, all);
    }

    public NodeSpec padding(float vertical, float horizontal) {
        return padding(vertical, horizontal, vertical, horizontal);
    }

    public NodeSpec padding(float top, float right, float bottom, float left) {
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
        this.paddingLeft = left;
        invalidate();
        return this;
    }

    public NodeSpec paddingTop(float padding) {
        this.paddingTop = padding;
        invalidate();
        return this;
    }

    public NodeSpec paddingBottom(float padding) {
        this.paddingBottom = padding;
        invalidate();
        return this;
    }

    public NodeSpec paddingLeft(float padding) {
        this.paddingLeft = padding;
        invalidate();
        return this;
    }

    public NodeSpec paddingRight(float padding) {
        this.paddingRight = padding;
        invalidate();
        return this;
    }

    public NodeSpec minimumWidth(float width) {
        this.minimumWidth = width;
        invalidate();
        return this;
    }

    public NodeSpec maximumWidth(float width) {
        this.maximumWidth = width;
        invalidate();
        return this;
    }

    public NodeSpec minimumHeight(float height) {
        this.minimumHeight = height;
        invalidate();
        return this;
    }

    public NodeSpec maximumHeight(float height) {
        this.maximumHeight = height;
        invalidate();
        return this;
    }

    /**
     * Resets all properties to their default values.
     *
     * @param invalidate whether to trigger the invalidation callback after reset
     * @return this instance for chaining
     */
    public NodeSpec reset(boolean invalidate) {
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
        return this;
    }

    // --- Helpers / Utilities ---

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
