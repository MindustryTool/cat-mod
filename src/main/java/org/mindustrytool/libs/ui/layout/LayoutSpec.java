package org.mindustrytool.libs.ui.layout;

import arc.func.Cons;

/**
 * LayoutSpec specifies the layout configuration properties for a container element.
 * It configures alignment, direction, spacing, and wraps properties, matching CSS Flexbox specifications.
 * All properties are fully detailed and written without abbreviations.
 */
public class LayoutSpec {

    /**
     * JustifyContent defines how the layout items are distributed along the main axis (justify-content).
     */
    public enum JustifyContent {
        /** Pack items toward the start boundary of the main axis. */
        START,
        /** Pack items toward the center of the main axis. */
        CENTER,
        /** Pack items toward the end boundary of the main axis. */
        END,
        /** Distribute items evenly: spacing between elements is equal, boundary margins are zero. */
        SPACE_BETWEEN,
        /** Distribute items evenly: boundary margins are half the spacing between elements. */
        SPACE_AROUND,
        /** Distribute items evenly: spacing to boundaries and between elements is identical. */
        SPACE_EVENLY
    }

    /**
     * AlignItems defines how the layout items are aligned along the cross axis (align-items).
     */
    public enum AlignItems {
        /** Align items to the start boundary of the cross axis. */
        START,
        /** Align items to the center of the cross axis. */
        CENTER,
        /** Align items to the end boundary of the cross axis. */
        END,
        /** Stretch items to fill the entire cross-axis limit. */
        STRETCH
    }

    public final NodeSizing sizing = new NodeSizing();
    private boolean isColumn = false;
    private boolean isWrap = false;
    private boolean isReverse = false;
    private float gap = 0.0f;
    private JustifyContent justifyContent = JustifyContent.START;
    private AlignItems alignItems = AlignItems.STRETCH;

    /**
     * Retrieves the sizing specification for this layout container.
     *
     * @return the node sizing instance
     */
    public NodeSizing sizing() {
        return sizing;
    }

    /**
     * Configures the layout direction to be vertical (column).
     *
     * @return this instance for chaining
     */
    public LayoutSpec column() {
        this.isColumn = true;
        return this;
    }

    /**
     * Configures the layout direction to be horizontal (row).
     *
     * @return this instance for chaining
     */
    public LayoutSpec row() {
        this.isColumn = false;
        return this;
    }

    /**
     * Enables wrapping of layout lines when elements exceed the container bounds.
     *
     * @return this instance for chaining
     */
    public LayoutSpec wrap() {
        this.isWrap = true;
        return this;
    }

    /**
     * Disables wrapping of layout lines (nowrap).
     *
     * @return this instance for chaining
     */
    public LayoutSpec noWrap() {
        this.isWrap = false;
        return this;
    }

    /**
     * Enables reverse layout ordering along the main axis.
     *
     * @return this instance for chaining
     */
    public LayoutSpec reverse() {
        this.isReverse = true;
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
        return this;
    }

    // --- Delegate sizing methods to keep fluent builder API clean ---

    public LayoutSpec padding(float all) {
        sizing.padding(all);
        return this;
    }

    public LayoutSpec padding(float vertical, float horizontal) {
        sizing.padding(vertical, horizontal);
        return this;
    }

    public LayoutSpec padding(float top, float right, float bottom, float left) {
        sizing.padding(top, right, bottom, left);
        return this;
    }

    public LayoutSpec minimumWidth(float width) {
        sizing.minimumWidth(width);
        return this;
    }

    public LayoutSpec maximumWidth(float width) {
        sizing.maximumWidth(width);
        return this;
    }

    public LayoutSpec minimumHeight(float height) {
        sizing.minimumHeight(height);
        return this;
    }

    public LayoutSpec maximumHeight(float height) {
        sizing.maximumHeight(height);
        return this;
    }

    public LayoutSpec size(Cons<NodeSizing> configurator) {
        configurator.get(sizing);
        return this;
    }

    public LayoutSpec onInvalidate(Runnable callback) {
        sizing.onInvalidate(callback);
        return this;
    }

    // --- Getters ---

    public boolean isColumn() { return isColumn; }
    public boolean isWrap() { return isWrap; }
    public boolean isReverse() { return isReverse; }
    public float gap() { return gap; }
    public JustifyContent justifyContent() { return justifyContent; }
    public AlignItems alignItems() { return alignItems; }
}
