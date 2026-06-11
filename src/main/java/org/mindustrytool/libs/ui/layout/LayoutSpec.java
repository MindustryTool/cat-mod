package org.mindustrytool.libs.ui.layout;

import lombok.Getter;

/**
 * LayoutSpec specifies the layout configuration properties for a container element.
 * It configures alignment, direction, spacing, and wraps properties, matching CSS Flexbox specifications.
 * It extends {@link NodeSpec} directly to combine sizing, padding, and layout configurations.
 * All properties are fully detailed and written without abbreviations.
 */
public class LayoutSpec extends NodeSpec {

    /**
     * JustifyContent defines how the layout items are distributed along the main axis (justify-content).
     */
    public enum JustifyContent {
        /**
         * Pack items toward the start boundary of the main axis.
         */
        START,
        /**
         * Pack items toward the center of the main axis.
         */
        CENTER,
        /**
         * Pack items toward the end boundary of the main axis.
         */
        END,
        /**
         * Distribute items evenly: spacing between elements is equal, boundary margins are zero.
         */
        SPACE_BETWEEN,
        /**
         * Distribute items evenly: boundary margins are half the spacing between elements.
         */
        SPACE_AROUND,
        /**
         * Distribute items evenly: spacing to boundaries and between elements is identical.
         */
        SPACE_EVENLY
    }

    /**
     * AlignItems defines how the layout items are aligned along the cross axis (align-items).
     */
    public enum AlignItems {
        /**
         * Align items to the start boundary of the cross axis.
         */
        START,
        /**
         * Align items to the center of the cross axis.
         */
        CENTER,
        /**
         * Align items to the end boundary of the cross axis.
         */
        END,
        /**
         * Stretch items to fill the entire cross-axis limit.
         */
        STRETCH
    }

    private @Getter boolean isColumn = false;
    private @Getter boolean isWrap = false;
    private @Getter boolean isReverse = false;
    private @Getter float gap = 0.0f;
    private @Getter JustifyContent justifyContent = JustifyContent.START;
    private @Getter AlignItems alignItems = AlignItems.STRETCH;

    // --- Fluent Setters ---

    /**
     * Configures the layout direction to be vertical (column).
     *
     * @return this instance for chaining
     */
    public LayoutSpec column() {
        this.isColumn = true;
        invalidate();
        return this;
    }

    /**
     * Configures the layout direction to be horizontal (row).
     *
     * @return this instance for chaining
     */
    public LayoutSpec row() {
        this.isColumn = false;
        invalidate();
        return this;
    }

    /**
     * Enables wrapping of layout lines when elements exceed the container bounds.
     *
     * @return this instance for chaining
     */
    public LayoutSpec wrap() {
        this.isWrap = true;
        invalidate();
        return this;
    }

    /**
     * Disables wrapping of layout lines (nowrap).
     *
     * @return this instance for chaining
     */
    public LayoutSpec noWrap() {
        this.isWrap = false;
        invalidate();
        return this;
    }

    /**
     * Enables reverse layout ordering along the main axis.
     *
     * @return this instance for chaining
     */
    public LayoutSpec reverse() {
        this.isReverse = true;
        invalidate();
        return this;
    }

    /**
     * Sets the reverse layout ordering flag.
     *
     * @param reverse true to reverse layout ordering, false otherwise
     * @return this instance for chaining
     */
    public LayoutSpec reverse(boolean reverse) {
        this.isReverse = reverse;
        invalidate();
        return this;
    }

    /**
     * Sets the spacing gap between elements.
     *
     * @param value the spacing coordinate value
     * @return this instance for chaining
     */
    public LayoutSpec gap(float value) {
        this.gap = value;
        invalidate();
        return this;
    }

    /**
     * Sets the main-axis alignment policy.
     *
     * @param justifyContent the justify content alignment mode
     * @return this instance for chaining
     */
    public LayoutSpec justifyContent(JustifyContent justifyContent) {
        this.justifyContent = justifyContent;
        invalidate();
        return this;
    }

    /**
     * Sets the cross-axis alignment policy.
     *
     * @param alignItems the align items mode
     * @return this instance for chaining
     */
    public LayoutSpec alignItems(AlignItems alignItems) {
        this.alignItems = alignItems;
        invalidate();
        return this;
    }

    // --- Covariant overrides of NodeSpec builder methods ---

    @Override
    public LayoutSpec widthMode(SizeMode mode) {
        super.widthMode(mode);
        return this;
    }

    @Override
    public LayoutSpec heightMode(SizeMode mode) {
        super.heightMode(mode);
        return this;
    }

    @Override
    public LayoutSpec fixedWidth(float width) {
        super.fixedWidth(width);
        return this;
    }

    @Override
    public LayoutSpec fixedHeight(float height) {
        super.fixedHeight(height);
        return this;
    }

    @Override
    public LayoutSpec growWeightHorizontal(float weight) {
        super.growWeightHorizontal(weight);
        return this;
    }

    @Override
    public LayoutSpec growWeightVertical(float weight) {
        super.growWeightVertical(weight);
        return this;
    }

    @Override
    public LayoutSpec alignSelf(AlignSelf alignSelf) {
        super.alignSelf(alignSelf);
        return this;
    }

    @Override
    public LayoutSpec grow() {
        super.grow();
        return this;
    }

    @Override
    public LayoutSpec growX() {
        super.growX();
        return this;
    }

    @Override
    public LayoutSpec growY() {
        super.growY();
        return this;
    }

    @Override
    public LayoutSpec width(float width) {
        super.width(width);
        return this;
    }

    @Override
    public LayoutSpec height(float height) {
        super.height(height);
        return this;
    }

    @Override
    public LayoutSpec padding(float all) {
        super.padding(all);
        return this;
    }

    @Override
    public LayoutSpec padding(float vertical, float horizontal) {
        super.padding(vertical, horizontal);
        return this;
    }

    @Override
    public LayoutSpec padding(float top, float right, float bottom, float left) {
        super.padding(top, right, bottom, left);
        return this;
    }

    @Override
    public LayoutSpec paddingTop(float padding) {
        super.paddingTop(padding);
        return this;
    }

    @Override
    public LayoutSpec paddingBottom(float padding) {
        super.paddingBottom(padding);
        return this;
    }

    @Override
    public LayoutSpec paddingLeft(float padding) {
        super.paddingLeft(padding);
        return this;
    }

    @Override
    public LayoutSpec paddingRight(float padding) {
        super.paddingRight(padding);
        return this;
    }

    @Override
    public LayoutSpec minimumWidth(float width) {
        super.minimumWidth(width);
        return this;
    }

    @Override
    public LayoutSpec maximumWidth(float width) {
        super.maximumWidth(width);
        return this;
    }

    @Override
    public LayoutSpec minimumHeight(float height) {
        super.minimumHeight(height);
        return this;
    }

    @Override
    public LayoutSpec maximumHeight(float height) {
        super.maximumHeight(height);
        return this;
    }

    @Override
    public void onInvalidate(Runnable callback) {
        super.onInvalidate(callback);
    }

    // --- Reset ---

    /**
     * Resets all layout properties — both {@link NodeSpec} sizing fields and the
     * LayoutSpec-specific direction, alignment, and gap fields — to their defaults.
     *
     * <p>Call this at the start of a reactive style configurator so that re-running
     * the effect produces the same result regardless of previous state.
     *
     * @return this instance for chaining
     */
    @Override
    public LayoutSpec reset(boolean invalidate) {
        super.reset(false);
        isColumn = false;
        isWrap = false;
        isReverse = false;
        gap = 0f;
        justifyContent = JustifyContent.START;
        alignItems = AlignItems.STRETCH;

        if (invalidate) invalidate();
        return this;
    }
}
