package org.mindustrytool.libs.ui.components;

import org.mindustrytool.libs.ui.layout.LayoutSpec;
import org.mindustrytool.libs.ui.layout.LayoutSpec.JustifyContent;
import org.mindustrytool.libs.ui.layout.LayoutSpec.AlignItems;

/**
 * ContainerStyle is an auxiliary base style builder for layout-based elements.
 * It encapsulates flexbox-like container sizing, alignment, and gap options.
 *
 * @param <S> the concrete style subclass type
 */
@SuppressWarnings("unchecked")
public abstract class ContainerStyle<S extends ContainerStyle<S>> extends ComponentStyle<S> {

    /**
     * Returns the internal {@link LayoutSpec} used to configure container properties.
     */
    protected abstract LayoutSpec layoutSpec();

    /**
     * Configures the layout direction to be vertical (column).
     *
     * @return this style builder instance
     */
    public S column() {
        layoutSpec().column();
        return (S) this;
    }

    /**
     * Configures the layout direction to be horizontal (row).
     *
     * @return this style builder instance
     */
    public S row() {
        layoutSpec().row();
        return (S) this;
    }

    /**
     * Enables wrapping of elements when they exceed the container bounds.
     *
     * @return this style builder instance
     */
    public S wrap() {
        layoutSpec().wrap();
        return (S) this;
    }

    /**
     * Disables wrapping of layout lines (nowrap).
     *
     * @return this style builder instance
     */
    public S noWrap() {
        layoutSpec().noWrap();
        return (S) this;
    }

    /**
     * Enables reverse layout ordering along the main axis.
     *
     * @return this style builder instance
     */
    public S reverse() {
        layoutSpec().reverse();
        return (S) this;
    }

    /**
     * Sets the reverse layout ordering flag.
     *
     * @param reverse true to reverse layout ordering, false otherwise
     * @return this style builder instance
     */
    public S reverse(boolean reverse) {
        layoutSpec().reverse(reverse);
        return (S) this;
    }

    /**
     * Sets the spacing gap between elements.
     *
     * @param value the spacing coordinate value
     * @return this style builder instance
     */
    public S gap(float value) {
        layoutSpec().gap(value);
        return (S) this;
    }

    /**
     * Sets the main-axis alignment policy.
     *
     * @param justifyContent the justify content alignment mode
     * @return this style builder instance
     */
    public S justifyContent(JustifyContent justifyContent) {
        layoutSpec().justifyContent(justifyContent);
        return (S) this;
    }

    /**
     * Sets the cross-axis alignment policy.
     *
     * @param alignItems the align items mode
     * @return this style builder instance
     */
    public S alignItems(AlignItems alignItems) {
        layoutSpec().alignItems(alignItems);
        return (S) this;
    }
}
