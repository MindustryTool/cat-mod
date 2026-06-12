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
    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    public static class LayoutSpecBuilder {
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
        private boolean isColumn = false;
        private boolean isWrap = false;
        private boolean isReverse = false;
        private float gap = 0.0f;
        private JustifyContent justifyContent = JustifyContent.START;
        private AlignItems alignItems = AlignItems.STRETCH;

        /** @return row flow config */
        public LayoutSpecBuilder row() {
            this.isColumn = false;
            return this;
        }

        /** @return column flow config */
        public LayoutSpecBuilder column() {
            this.isColumn = true;
            return this;
        }

        /** @return wrap flow config */
        public LayoutSpecBuilder wrap() {
            this.isWrap = true;
            return this;
        }

        /** @return no-wrap config */
        public LayoutSpecBuilder noWrap() {
            this.isWrap = false;
            return this;
        }

        /** @return reverse config */
        public LayoutSpecBuilder reverse() {
            this.isReverse = true;
            return this;
        }

        /** @param wrap wrapping value */
        public LayoutSpecBuilder wrap(boolean wrap) {
            this.isWrap = wrap;
            return this;
        }

        /** @param reverse reverse value */
        public LayoutSpecBuilder reverse(boolean reverse) {
            this.isReverse = reverse;
            return this;
        }

        /** @return grow both directions */
        public LayoutSpecBuilder grow() {
            this.widthMode = SizeMode.GROW;
            this.heightMode = SizeMode.GROW;
            return this;
        }

        /** @return grow horizontally */
        public LayoutSpecBuilder growX() {
            this.widthMode = SizeMode.GROW;
            return this;
        }

        /** @return grow vertically */
        public LayoutSpecBuilder growY() {
            this.heightMode = SizeMode.GROW;
            return this;
        }

        /** @param all padding space */
        public LayoutSpecBuilder padding(float all) {
            this.paddingTop = all;
            this.paddingBottom = all;
            this.paddingLeft = all;
            this.paddingRight = all;
            return this;
        }

        /** @param top top padding @param right right padding @param bottom bottom padding @param left left padding */
        public LayoutSpecBuilder padding(float top, float right, float bottom, float left) {
            this.paddingTop = top;
            this.paddingRight = right;
            this.paddingBottom = bottom;
            this.paddingLeft = left;
            return this;
        }
    }

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
