package org.mindustrytool.libs.ui.component;

import arc.scene.Element;
import arc.scene.event.Touchable;
import org.mindustrytool.libs.ui.layout.NodeSpec;
import org.mindustrytool.libs.ui.layout.NodeSpec.AlignSelf;
import org.mindustrytool.libs.ui.layout.NodeSpec.SizeMode;

/**
 * ComponentStyle serves as the base class for component-specific styling builders.
 * It uses the Curiously Recurring Template Pattern (CRTP) to allow fluent method chaining
 * across inheritance hierarchies, exposing all of {@link NodeSpec}'s sizing and padding properties,
 * as well as all common {@link Element} visual and interactive configuration properties.
 *
 * @param <S> the concrete style subclass type
 */
@SuppressWarnings("unchecked")
public abstract class ComponentStyle<S extends ComponentStyle<S>> {

    /**
     * Returns the {@link NodeSpec} to which configuration methods delegate.
     * Implementations return the owning component's sizing instance.
     */
    protected abstract NodeSpec sizing();

    /**
     * Returns the arc {@link Element} whose visual properties (visibility,
     * touchable, name) are mutated by the inherited builder methods.
     */
    protected abstract Element styledElement();

    protected ComponentStyle() {

    }

    /**
     * Resets the sizing specification to default values.
     *
     * @param invalidate whether to trigger the invalidation callback after reset
     * @return this style builder instance
     */
    public S reset(boolean invalidate) {
        sizing().reset(invalidate);
        return (S) this;
    }

    // --- Core Element Configuration Builders ---

    /**
     * Sets the visibility status of the element.
     *
     * @param visible true to show, false to hide
     * @return this style builder instance
     */
    public S visible(boolean visible) {
        styledElement().visible = visible;
        return (S) this;
    }

    /**
     * Sets the touchable behavior of the element.
     *
     * @param touchable the touchable mode
     * @return this style builder instance
     */
    public S touchable(Touchable touchable) {
        styledElement().touchable = touchable;
        return (S) this;
    }

    /**
     * Sets the debug or lookup name.
     *
     * @param name the name of the element
     * @return this style builder instance
     */
    public S name(String name) {
        styledElement().name = name;
        return (S) this;
    }

    // --- NodeSpec Sizing & Padding Builders ---

    /**
     * Sets the width sizing mode.
     *
     * @param mode the size mode
     * @return this style builder instance
     */
    public S widthMode(SizeMode mode) {
        sizing().widthMode(mode);
        return (S) this;
    }

    /**
     * Sets the height sizing mode.
     *
     * @param mode the size mode
     * @return this style builder instance
     */
    public S heightMode(SizeMode mode) {
        sizing().heightMode(mode);
        return (S) this;
    }

    /**
     * Configures a fixed width for the element.
     *
     * @param width the width value
     * @return this style builder instance
     */
    public S fixedWidth(float width) {
        sizing().fixedWidth(width);
        return (S) this;
    }

    /**
     * Configures a fixed height for the element.
     *
     * @param height the height value
     * @return this style builder instance
     */
    public S fixedHeight(float height) {
        sizing().fixedHeight(height);
        return (S) this;
    }

    /**
     * Sets the horizontal grow weight.
     *
     * @param weight the grow weight
     * @return this style builder instance
     */
    public S growWeightHorizontal(float weight) {
        sizing().growWeightHorizontal(weight);
        return (S) this;
    }

    /**
     * Sets the vertical grow weight.
     *
     * @param weight the grow weight
     * @return this style builder instance
     */
    public S growWeightVertical(float weight) {
        sizing().growWeightVertical(weight);
        return (S) this;
    }

    /**
     * Configures individual cross-axis alignment.
     *
     * @param alignSelf the align self mode
     * @return this style builder instance
     */
    public S alignSelf(AlignSelf alignSelf) {
        sizing().alignSelf(alignSelf);
        return (S) this;
    }

    /**
     * Enables horizontal and vertical grow modes.
     *
     * @return this style builder instance
     */
    public S grow() {
        sizing().grow();
        return (S) this;
    }

    /**
     * Enables horizontal grow mode.
     *
     * @return this style builder instance
     */
    public S growX() {
        sizing().growX();
        return (S) this;
    }

    /**
     * Enables vertical grow mode.
     *
     * @return this style builder instance
     */
    public S growY() {
        sizing().growY();
        return (S) this;
    }

    /**
     * Configures a fixed width.
     *
     * @param width the width value
     * @return this style builder instance
     */
    public S width(float width) {
        sizing().width(width);
        return (S) this;
    }

    /**
     * Configures a fixed height.
     *
     * @param height the height value
     * @return this style builder instance
     */
    public S height(float height) {
        sizing().height(height);
        return (S) this;
    }

    /**
     * Configures uniform padding on all sides.
     *
     * @param all the padding value
     * @return this style builder instance
     */
    public S padding(float all) {
        sizing().padding(all);
        return (S) this;
    }

    /**
     * Configures vertical and horizontal padding.
     *
     * @param vertical the vertical padding
     * @param horizontal the horizontal padding
     * @return this style builder instance
     */
    public S padding(float vertical, float horizontal) {
        sizing().padding(vertical, horizontal);
        return (S) this;
    }

    /**
     * Configures padding individually for all sides.
     *
     * @param top the top padding
     * @param right the right padding
     * @param bottom the bottom padding
     * @param left the left padding
     * @return this style builder instance
     */
    public S padding(float top, float right, float bottom, float left) {
        sizing().padding(top, right, bottom, left);
        return (S) this;
    }

    /**
     * Sets the top padding.
     *
     * @param padding the padding value
     * @return this style builder instance
     */
    public S paddingTop(float padding) {
        sizing().paddingTop(padding);
        return (S) this;
    }

    /**
     * Sets the bottom padding.
     *
     * @param padding the padding value
     * @return this style builder instance
     */
    public S paddingBottom(float padding) {
        sizing().paddingBottom(padding);
        return (S) this;
    }

    /**
     * Sets the left padding.
     *
     * @param padding the padding value
     * @return this style builder instance
     */
    public S paddingLeft(float padding) {
        sizing().paddingLeft(padding);
        return (S) this;
    }

    /**
     * Sets the right padding.
     *
     * @param padding the padding value
     * @return this style builder instance
     */
    public S paddingRight(float padding) {
        sizing().paddingRight(padding);
        return (S) this;
    }

    /**
     * Configures the minimum width constraint.
     *
     * @param width the minimum width
     * @return this style builder instance
     */
    public S minimumWidth(float width) {
        sizing().minimumWidth(width);
        return (S) this;
    }

    /**
     * Configures the maximum width constraint.
     *
     * @param width the maximum width
     * @return this style builder instance
     */
    public S maximumWidth(float width) {
        sizing().maximumWidth(width);
        return (S) this;
    }

    /**
     * Configures the minimum height constraint.
     *
     * @param height the minimum height
     * @return this style builder instance
     */
    public S minimumHeight(float height) {
        sizing().minimumHeight(height);
        return (S) this;
    }

    /**
     * Configures the maximum height constraint.
     *
     * @param height the maximum height
     * @return this style builder instance
     */
    public S maximumHeight(float height) {
        sizing().maximumHeight(height);
        return (S) this;
    }
}
