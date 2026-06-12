package org.mindustrytool.libs.ui.layout;

import lombok.Builder;

/**
 * LayoutSpec defines the size constraints, margins, and flex-layout rules of a node.
 * It is completely immutable and owned by widgets.
 */
@Builder(toBuilder = true)
public record LayoutSpec(
    SizeMode widthMode,
    SizeMode heightMode,
    float fixedWidth,
    float fixedHeight,
    float growWeightHorizontal,
    float growWeightVertical,
    AlignSelf alignSelf,
    float paddingTop,
    float paddingBottom,
    float paddingLeft,
    float paddingRight,
    float minimumWidth,
    float maximumWidth,
    float minimumHeight,
    float maximumHeight,
    boolean isColumn,
    boolean isWrap,
    boolean isReverse,
    float gap,
    JustifyContent justifyContent,
    AlignItems alignItems
) {

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

    public enum JustifyContent {
        START,
        CENTER,
        END,
        SPACE_BETWEEN,
        SPACE_AROUND,
        SPACE_EVENLY
    }

    public enum AlignItems {
        START,
        CENTER,
        END,
        STRETCH
    }

    // --- Custom Builder to support Default Values ---
    public static class LayoutSpecBuilder {
        private SizeMode widthMode = SizeMode.WRAP;
        private SizeMode heightMode = SizeMode.WRAP;
        private float growWeightHorizontal = 1.0f;
        private float growWeightVertical = 1.0f;
        private AlignSelf alignSelf = AlignSelf.AUTO;
        private float minimumWidth = -1.0f;
        private float maximumWidth = -1.0f;
        private float minimumHeight = -1.0f;
        private float maximumHeight = -1.0f;
        private JustifyContent justifyContent = JustifyContent.START;
        private AlignItems alignItems = AlignItems.STRETCH;
    }

    // --- No-args Constructor for defaults ---
    public LayoutSpec() {
        this(SizeMode.WRAP, SizeMode.WRAP, 0.0f, 0.0f, 1.0f, 1.0f, AlignSelf.AUTO,
             0.0f, 0.0f, 0.0f, 0.0f, -1.0f, -1.0f, -1.0f, -1.0f,
             false, false, false, 0.0f, JustifyContent.START, AlignItems.STRETCH);
    }

    // --- Compatibility Getters ---
    public SizeMode getWidthMode() { return widthMode; }
    public SizeMode getHeightMode() { return heightMode; }
    public float getFixedWidth() { return fixedWidth; }
    public float getFixedHeight() { return fixedHeight; }
    public float getGrowWeightHorizontal() { return growWeightHorizontal; }
    public float getGrowWeightVertical() { return growWeightVertical; }
    public AlignSelf getAlignSelf() { return alignSelf; }
    public float getPaddingTop() { return paddingTop; }
    public float getPaddingBottom() { return paddingBottom; }
    public float getPaddingLeft() { return paddingLeft; }
    public float getPaddingRight() { return paddingRight; }
    public float getMinimumWidth() { return minimumWidth; }
    public float getMaximumWidth() { return maximumWidth; }
    public float getMinimumHeight() { return minimumHeight; }
    public float getMaximumHeight() { return maximumHeight; }
    public boolean isColumn() { return isColumn; }
    public boolean isWrap() { return isWrap; }
    public boolean isReverse() { return isReverse; }
    public float getGap() { return gap; }
    public JustifyContent getJustifyContent() { return justifyContent; }
    public AlignItems getAlignItems() { return alignItems; }

    // --- Fluent Wither-style Setters for Test and Configuration Compatibility ---
    public LayoutSpec widthMode(SizeMode widthMode) { return toBuilder().widthMode(widthMode).build(); }
    public LayoutSpec heightMode(SizeMode heightMode) { return toBuilder().heightMode(heightMode).build(); }
    public LayoutSpec fixedWidth(float fixedWidth) { return toBuilder().fixedWidth(fixedWidth).build(); }
    public LayoutSpec fixedHeight(float fixedHeight) { return toBuilder().fixedHeight(fixedHeight).build(); }
    public LayoutSpec growWeightHorizontal(float growWeightHorizontal) { return toBuilder().growWeightHorizontal(growWeightHorizontal).build(); }
    public LayoutSpec growWeightVertical(float growWeightVertical) { return toBuilder().growWeightVertical(growWeightVertical).build(); }
    public LayoutSpec alignSelf(AlignSelf alignSelf) { return toBuilder().alignSelf(alignSelf).build(); }
    public LayoutSpec paddingTop(float paddingTop) { return toBuilder().paddingTop(paddingTop).build(); }
    public LayoutSpec paddingBottom(float paddingBottom) { return toBuilder().paddingBottom(paddingBottom).build(); }
    public LayoutSpec paddingLeft(float paddingLeft) { return toBuilder().paddingLeft(paddingLeft).build(); }
    public LayoutSpec paddingRight(float paddingRight) { return toBuilder().paddingRight(paddingRight).build(); }
    public LayoutSpec minimumWidth(float minimumWidth) { return toBuilder().minimumWidth(minimumWidth).build(); }
    public LayoutSpec maximumWidth(float maximumWidth) { return toBuilder().maximumWidth(maximumWidth).build(); }
    public LayoutSpec minimumHeight(float minimumHeight) { return toBuilder().minimumHeight(minimumHeight).build(); }
    public LayoutSpec maximumHeight(float maximumHeight) { return toBuilder().maximumHeight(maximumHeight).build(); }
    public LayoutSpec isColumn(boolean isColumn) { return toBuilder().isColumn(isColumn).build(); }
    public LayoutSpec isWrap(boolean isWrap) { return toBuilder().isWrap(isWrap).build(); }
    public LayoutSpec isReverse(boolean isReverse) { return toBuilder().isReverse(isReverse).build(); }
    public LayoutSpec gap(float gap) { return toBuilder().gap(gap).build(); }
    public LayoutSpec justifyContent(JustifyContent justifyContent) { return toBuilder().justifyContent(justifyContent).build(); }
    public LayoutSpec alignItems(AlignItems alignItems) { return toBuilder().alignItems(alignItems).build(); }

    // --- Fluent Builders for Unit Test Compatibility ---
    public LayoutSpec row() { return isColumn(false); }
    public LayoutSpec column() { return isColumn(true); }
    public LayoutSpec wrap() { return isWrap(true); }
    public LayoutSpec reverse() { return isReverse(true); }
    public LayoutSpec wrap(boolean wrap) { return isWrap(wrap); }
    public LayoutSpec reverse(boolean reverse) { return isReverse(reverse); }
    public LayoutSpec padding(float all) {
        return toBuilder().paddingTop(all).paddingBottom(all).paddingLeft(all).paddingRight(all).build();
    }
    public LayoutSpec padding(float top, float right, float bottom, float left) {
        return toBuilder().paddingTop(top).paddingRight(right).paddingBottom(bottom).paddingLeft(left).build();
    }
    public LayoutSpec growX() { return toBuilder().widthMode(SizeMode.GROW).build(); }
    public LayoutSpec growY() { return toBuilder().heightMode(SizeMode.GROW).build(); }
    public LayoutSpec grow() { return toBuilder().widthMode(SizeMode.GROW).heightMode(SizeMode.GROW).build(); }
    public LayoutSpec noWrap() { return isWrap(false); }

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

    public static LayoutSpec defaultSpec() {
        return LayoutSpec.builder().build();
    }
}
