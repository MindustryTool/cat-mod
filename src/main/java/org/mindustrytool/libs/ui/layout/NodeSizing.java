package org.mindustrytool.libs.ui.layout;

/**
 * NodeSizing defines the sizing policies, padding, and size constraints for a layout element (Node).
 * It provides a fluent builder-like API to configure layout options.
 * All properties are documented in detail, and abbreviations are strictly avoided.
 */
public class NodeSizing {

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

    private SizeMode widthMode = SizeMode.WRAP;
    private SizeMode heightMode = SizeMode.WRAP;
    private float fixedWidth = 0.0f;
    private float fixedHeight = 0.0f;
    private float growWeightHorizontal = 1.0f;
    private float growWeightVertical = 1.0f;
    private AlignSelf alignSelf = AlignSelf.AUTO;

    private float paddingTop = 0.0f;
    private float paddingBottom = 0.0f;
    private float paddingLeft = 0.0f;
    private float paddingRight = 0.0f;

    private float minimumWidth = -1.0f;
    private float maximumWidth = -1.0f;
    private float minimumHeight = -1.0f;
    private float maximumHeight = -1.0f;

    /**
     * Sets the callback to be triggered when any layout property changes.
     *
     * @param callback the invalidation callback runnable
     * @return this instance for chaining
     */
    public NodeSizing onInvalidate(Runnable callback) {
        this.onInvalidateCallback = callback;
        return this;
    }

    /**
     * Triggers the invalidation callback to notify the parent container of changes.
     */
    protected void invalidate() {
        if (onInvalidateCallback != null) {
            onInvalidateCallback.run();
        }
    }

    // --- Getters ---

    public SizeMode getWidthMode() {
        return widthMode;
    }

    public SizeMode getHeightMode() {
        return heightMode;
    }

    public float getFixedWidth() {
        return fixedWidth;
    }

    public float getFixedHeight() {
        return fixedHeight;
    }

    public float getGrowWeightHorizontal() {
        return growWeightHorizontal;
    }

    public float getGrowWeightVertical() {
        return growWeightVertical;
    }

    public AlignSelf getAlignSelf() {
        return alignSelf;
    }

    public float getPaddingTop() {
        return paddingTop;
    }

    public float getPaddingBottom() {
        return paddingBottom;
    }

    public float getPaddingLeft() {
        return paddingLeft;
    }

    public float getPaddingRight() {
        return paddingRight;
    }

    public float getMinimumWidth() {
        return minimumWidth;
    }

    public float getMaximumWidth() {
        return maximumWidth;
    }

    public float getMinimumHeight() {
        return minimumHeight;
    }

    public float getMaximumHeight() {
        return maximumHeight;
    }

    // --- Fluent Setters ---

    public NodeSizing widthMode(SizeMode mode) {
        this.widthMode = mode;
        invalidate();
        return this;
    }

    public NodeSizing heightMode(SizeMode mode) {
        this.heightMode = mode;
        invalidate();
        return this;
    }

    public NodeSizing fixedWidth(float width) {
        this.widthMode = SizeMode.FIXED;
        this.fixedWidth = width;
        invalidate();
        return this;
    }

    public NodeSizing fixedHeight(float height) {
        this.heightMode = SizeMode.FIXED;
        this.fixedHeight = height;
        invalidate();
        return this;
    }

    public NodeSizing growWeightHorizontal(float weight) {
        this.growWeightHorizontal = weight;
        invalidate();
        return this;
    }

    public NodeSizing growWeightVertical(float weight) {
        this.growWeightVertical = weight;
        invalidate();
        return this;
    }

    public NodeSizing alignSelf(AlignSelf alignSelf) {
        this.alignSelf = alignSelf;
        invalidate();
        return this;
    }

    public NodeSizing grow() {
        this.widthMode = SizeMode.GROW;
        this.heightMode = SizeMode.GROW;
        invalidate();
        return this;
    }

    public NodeSizing growX() {
        this.widthMode = SizeMode.GROW;
        invalidate();
        return this;
    }

    public NodeSizing growY() {
        this.heightMode = SizeMode.GROW;
        invalidate();
        return this;
    }

    public NodeSizing width(float width) {
        return fixedWidth(width);
    }

    public NodeSizing height(float height) {
        return fixedHeight(height);
    }

    public NodeSizing padding(float all) {
        return padding(all, all, all, all);
    }

    public NodeSizing padding(float vertical, float horizontal) {
        return padding(vertical, horizontal, vertical, horizontal);
    }

    public NodeSizing padding(float top, float right, float bottom, float left) {
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
        this.paddingLeft = left;
        invalidate();
        return this;
    }

    public NodeSizing paddingTop(float padding) {
        this.paddingTop = padding;
        invalidate();
        return this;
    }

    public NodeSizing paddingBottom(float padding) {
        this.paddingBottom = padding;
        invalidate();
        return this;
    }

    public NodeSizing paddingLeft(float padding) {
        this.paddingLeft = padding;
        invalidate();
        return this;
    }

    public NodeSizing paddingRight(float padding) {
        this.paddingRight = padding;
        invalidate();
        return this;
    }

    public NodeSizing minimumWidth(float width) {
        this.minimumWidth = width;
        invalidate();
        return this;
    }

    public NodeSizing maximumWidth(float width) {
        this.maximumWidth = width;
        invalidate();
        return this;
    }

    public NodeSizing minimumHeight(float height) {
        this.minimumHeight = height;
        invalidate();
        return this;
    }

    public NodeSizing maximumHeight(float height) {
        this.maximumHeight = height;
        invalidate();
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
