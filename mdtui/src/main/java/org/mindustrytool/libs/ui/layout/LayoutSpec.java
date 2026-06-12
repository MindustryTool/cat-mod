package org.mindustrytool.libs.ui.layout;

import lombok.Builder;

/**
 * A layout specification record that defines size constraints, padding, alignment,
 * and flex-box layout behaviors of a node.
 * <p>
 * It is completely immutable and owned directly by {@link org.mindustrytool.libs.ui.widget.Widget} configurations.
 *
 * @param widthMode             The horizontal sizing mode (WRAP, GROW, or FIXED).
 * @param heightMode            The vertical sizing mode (WRAP, GROW, or FIXED).
 * @param fixedWidth            The exact width of the widget when widthMode is FIXED.
 * @param fixedHeight           The exact height of the widget when heightMode is FIXED.
 * @param growWeightHorizontal  The relative weight of the widget when growing horizontally in a flex row.
 * @param growWeightVertical    The relative weight of the widget when growing vertically in a flex column.
 * @param alignSelf             The cross-axis alignment override of this specific widget in its parent container.
 * @param paddingTop            The top padding spacing inside the widget container.
 * @param paddingBottom         The bottom padding spacing inside the widget container.
 * @param paddingLeft           The left padding spacing inside the widget container.
 * @param paddingRight          The right padding spacing inside the widget container.
 * @param minimumWidth          The minimum allowed width limit for layout.
 * @param maximumWidth          The maximum allowed width limit for layout.
 * @param minimumHeight         The minimum allowed height limit for layout.
 * @param maximumHeight         The maximum allowed height limit for layout.
 * @param isColumn              True if layout children are arranged vertically, false for horizontally.
 * @param isWrap                True if children should wrap onto multiple rows/columns if space is exceeded.
 * @param isReverse             True if children flow in reverse order (e.g. bottom-to-top or right-to-left).
 * @param gap                   The spacing gap between adjacent children.
 * @param justifyContent        The alignment of children along the main flow axis.
 * @param alignItems            The alignment of children along the cross flow axis.
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

    /**
     * Size modes determining how a widget resolves its layout constraints.
     */
    public enum SizeMode {
        /** Size fits the content of the widget. */
        WRAP,
        /** Size grows to occupy available parent space. */
        GROW,
        /** Size is fixed to specific width or height values. */
        FIXED
    }

    /**
     * Cross-axis alignment overrides for individual elements.
     */
    public enum AlignSelf {
        /** Follow the parent's default items alignment. */
        AUTO,
        /** Align to the start of the cross-axis. */
        START,
        /** Align to the center of the cross-axis. */
        CENTER,
        /** Align to the end of the cross-axis. */
        END,
        /** Stretch to fill the cross-axis width/height. */
        STRETCH
    }

    /**
     * Distribution behavior of children along the main flow axis.
     */
    public enum JustifyContent {
        /** Pack items at the start of the main flow axis. */
        START,
        /** Center items along the main flow axis. */
        CENTER,
        /** Pack items at the end of the main flow axis. */
        END,
        /** Distribute space evenly; first item is at the start, last is at the end. */
        SPACE_BETWEEN,
        /** Distribute space evenly; items have a half-size space on each side. */
        SPACE_AROUND,
        /** Distribute space evenly; items have equal space on all sides and between them. */
        SPACE_EVENLY
    }

    /**
     * Alignment behavior of children along the cross flow axis.
     */
    public enum AlignItems {
        /** Align items to the start of the cross axis. */
        START,
        /** Center items along the cross axis. */
        CENTER,
        /** Align items to the end of the cross axis. */
        END,
        /** Stretch items to fill the cross axis space. */
        STRETCH
    }

    /**
     * Lombok builder class helper that defines the default properties for a LayoutSpec builder.
     */
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

    /**
     * Default constructor creating a default LayoutSpec.
     */
    public LayoutSpec() {
        this(SizeMode.WRAP, SizeMode.WRAP, 0.0f, 0.0f, 1.0f, 1.0f, AlignSelf.AUTO,
             0.0f, 0.0f, 0.0f, 0.0f, -1.0f, -1.0f, -1.0f, -1.0f,
             false, false, false, 0.0f, JustifyContent.START, AlignItems.STRETCH);
    }

    // --- Fluent Wither-style Setters for Test and Configuration Compatibility ---

    /** @param widthMode new width size mode. @return new LayoutSpec. */
    public LayoutSpec widthMode(SizeMode widthMode) { return toBuilder().widthMode(widthMode).build(); }
    /** @param heightMode new height size mode. @return new LayoutSpec. */
    public LayoutSpec heightMode(SizeMode heightMode) { return toBuilder().heightMode(heightMode).build(); }
    /** @param fixedWidth new fixed width value. @return new LayoutSpec. */
    public LayoutSpec fixedWidth(float fixedWidth) { return toBuilder().fixedWidth(fixedWidth).build(); }
    /** @param fixedHeight new fixed height value. @return new LayoutSpec. */
    public LayoutSpec fixedHeight(float fixedHeight) { return toBuilder().fixedHeight(fixedHeight).build(); }
    /** @param growWeightHorizontal new horizontal grow weight. @return new LayoutSpec. */
    public LayoutSpec growWeightHorizontal(float growWeightHorizontal) { return toBuilder().growWeightHorizontal(growWeightHorizontal).build(); }
    /** @param growWeightVertical new vertical grow weight. @return new LayoutSpec. */
    public LayoutSpec growWeightVertical(float growWeightVertical) { return toBuilder().growWeightVertical(growWeightVertical).build(); }
    /** @param alignSelf new cross axis alignment override. @return new LayoutSpec. */
    public LayoutSpec alignSelf(AlignSelf alignSelf) { return toBuilder().alignSelf(alignSelf).build(); }
    /** @param paddingTop new top padding. @return new LayoutSpec. */
    public LayoutSpec paddingTop(float paddingTop) { return toBuilder().paddingTop(paddingTop).build(); }
    /** @param paddingBottom new bottom padding. @return new LayoutSpec. */
    public LayoutSpec paddingBottom(float paddingBottom) { return toBuilder().paddingBottom(paddingBottom).build(); }
    /** @param paddingLeft new left padding. @return new LayoutSpec. */
    public LayoutSpec paddingLeft(float paddingLeft) { return toBuilder().paddingLeft(paddingLeft).build(); }
    /** @param paddingRight new right padding. @return new LayoutSpec. */
    public LayoutSpec paddingRight(float paddingRight) { return toBuilder().paddingRight(paddingRight).build(); }
    /** @param minimumWidth new minimum width constraint. @return new LayoutSpec. */
    public LayoutSpec minimumWidth(float minimumWidth) { return toBuilder().minimumWidth(minimumWidth).build(); }
    /** @param maximumWidth new maximum width constraint. @return new LayoutSpec. */
    public LayoutSpec maximumWidth(float maximumWidth) { return toBuilder().maximumWidth(maximumWidth).build(); }
    /** @param minimumHeight new minimum height constraint. @return new LayoutSpec. */
    public LayoutSpec minimumHeight(float minimumHeight) { return toBuilder().minimumHeight(minimumHeight).build(); }
    /** @param maximumHeight new maximum height constraint. @return new LayoutSpec. */
    public LayoutSpec maximumHeight(float maximumHeight) { return toBuilder().maximumHeight(maximumHeight).build(); }
    /** @param isColumn new flow layout direction. @return new LayoutSpec. */
    public LayoutSpec isColumn(boolean isColumn) { return toBuilder().isColumn(isColumn).build(); }
    /** @param isWrap new wrapping flag. @return new LayoutSpec. */
    public LayoutSpec isWrap(boolean isWrap) { return toBuilder().isWrap(isWrap).build(); }
    /** @param isReverse new reverse flow flag. @return new LayoutSpec. */
    public LayoutSpec isReverse(boolean isReverse) { return toBuilder().isReverse(isReverse).build(); }
    /** @param gap new layout spacing gap. @return new LayoutSpec. */
    public LayoutSpec gap(float gap) { return toBuilder().gap(gap).build(); }
    /** @param justifyContent new main axis alignment justification. @return new LayoutSpec. */
    public LayoutSpec justifyContent(JustifyContent justifyContent) { return toBuilder().justifyContent(justifyContent).build(); }
    /** @param alignItems new cross axis alignment behavior. @return new LayoutSpec. */
    public LayoutSpec alignItems(AlignItems alignItems) { return toBuilder().alignItems(alignItems).build(); }

    // --- Fluent Builders for Unit Test Compatibility ---

    /** @return new LayoutSpec configured as row flow. */
    public LayoutSpec row() { return isColumn(false); }
    /** @return new LayoutSpec configured as column flow. */
    public LayoutSpec column() { return isColumn(true); }
    /** @return new LayoutSpec configured with wrapping enabled. */
    public LayoutSpec wrap() { return isWrap(true); }
    /** @return new LayoutSpec configured with reverse flow enabled. */
    public LayoutSpec reverse() { return isReverse(true); }
    /** @param wrap wrapping enabled value. @return new LayoutSpec. */
    public LayoutSpec wrap(boolean wrap) { return isWrap(wrap); }
    /** @param reverse reverse flow value. @return new LayoutSpec. */
    public LayoutSpec reverse(boolean reverse) { return isReverse(reverse); }
    
    /**
     * Configures symmetric padding on all sides.
     *
     * @param all the padding space.
     * @return new LayoutSpec.
     */
    public LayoutSpec padding(float all) {
        return toBuilder().paddingTop(all).paddingBottom(all).paddingLeft(all).paddingRight(all).build();
    }
    
    /**
     * Configures padding on top, right, bottom, and left sides individually.
     *
     * @param top    the top padding space.
     * @param right  the right padding space.
     * @param bottom the bottom padding space.
     * @param left   the left padding space.
     * @return new LayoutSpec.
     */
    public LayoutSpec padding(float top, float right, float bottom, float left) {
        return toBuilder().paddingTop(top).paddingRight(right).paddingBottom(bottom).paddingLeft(left).build();
    }
    
    /** @return new LayoutSpec with width mode set to GROW. */
    public LayoutSpec growX() { return toBuilder().widthMode(SizeMode.GROW).build(); }
    /** @return new LayoutSpec with height mode set to GROW. */
    public LayoutSpec growY() { return toBuilder().heightMode(SizeMode.GROW).build(); }
    /** @return new LayoutSpec with both width and height modes set to GROW. */
    public LayoutSpec grow() { return toBuilder().widthMode(SizeMode.GROW).heightMode(SizeMode.GROW).build(); }
    /** @return new LayoutSpec with wrapping disabled. */
    public LayoutSpec noWrap() { return isWrap(false); }

    /**
     * Computes the total horizontal padding (left + right).
     *
     * @return total horizontal padding value.
     */
    public float getHorizontalPadding() {
        return paddingLeft + paddingRight;
    }

    /**
     * Computes the total vertical padding (top + bottom).
     *
     * @return total vertical padding value.
     */
    public float getVerticalPadding() {
        return paddingTop + paddingBottom;
    }

    /**
     * Constraints a width value within the minimum and maximum boundaries.
     *
     * @param value the raw width value.
     * @return the clamped width value.
     */
    public float constrainWidth(float value) {
        if (minimumWidth >= 0.0f && value < minimumWidth) {
            value = minimumWidth;
        }
        if (maximumWidth >= 0.0f && value > maximumWidth) {
            value = maximumWidth;
        }
        return value;
    }

    /**
     * Constraints a height value within the minimum and maximum boundaries.
     *
     * @param value the raw height value.
     * @return the clamped height value.
     */
    public float constrainHeight(float value) {
        if (minimumHeight >= 0.0f && value < minimumHeight) {
            value = minimumHeight;
        }
        if (maximumHeight >= 0.0f && value > maximumHeight) {
            value = maximumHeight;
        }
        return value;
    }

    /**
     * Resolves the default layout specification.
     *
     * @return the default specification object.
     */
    public static LayoutSpec defaultSpec() {
        return LayoutSpec.builder().build();
    }
}
