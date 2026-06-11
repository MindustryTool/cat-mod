package org.mindustrytool.libs.ui.components;

import arc.graphics.g2d.Font;
import arc.scene.Element;
import arc.scene.Scene;

import org.mindustrytool.libs.ui.component.Component;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.component.EffectHost;
import org.mindustrytool.libs.ui.element.TextElement;
import org.mindustrytool.libs.ui.layout.NodeSpec;

import arc.func.Cons;

/**
 * A reactive text component that wraps and controls a {@link TextElement}.
 * Uses an {@link EffectHost} to track styling changes and dynamically update the underlying element's properties.
 *
 * <p>Supports static text string, dynamic text suppliers, color tinting, custom fonts, scaling, alignments, and wrapping.
 *
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * Text.of()
 *     .style(s -> s.text("Hello World").color(Color.white).size(1.2f))
 *     .size(sz -> sz.fixedWidth(150f));
 * }</pre>
 */
public class Text implements Component {

    /**
     * Builder-style configurator class to set properties on the underlying {@link TextElement}.
     * Executes within the reactive tracking context of {@link Text#style(Cons)}.
     */
    public class Style extends ComponentStyle<Style> {

        @Override
        protected NodeSpec sizing() {
            return sizing;
        }

        @Override
        protected Element styledElement() {
            return element;
        }

        /**
         * Sets static text to display.
         *
         * @param value the string value
         * @return this Style instance for chaining
         */
        public Style text(String value) {
            element.setText(value);
            return this;
        }

        /**
         * Sets a custom font to render the text.
         *
         * @param font the font to render with
         * @return this Style instance for chaining
         */
        public Style font(Font font) {
            element.setFont(font);
            return this;
        }

        /**
         * Sets the font scale factor relative to its native scale (1.0 = native).
         *
         * @param scale the scaling factor
         * @return this Style instance for chaining
         */
        public Style size(float scale) {
            element.setFontScale(scale);
            return this;
        }

        /**
         * Sets separate font scale factors for X and Y axes.
         *
         * @param scaleX the horizontal scaling factor
         * @param scaleY the vertical scaling factor
         * @return this Style instance for chaining
         */
        public Style size(float scaleX, float scaleY) {
            element.setFontScale(scaleX, scaleY);
            return this;
        }

        /**
         * Sets the horizontal font scale factor.
         *
         * @param scaleX the horizontal scaling factor
         * @return this Style instance for chaining
         */
        public Style sizeX(float scaleX) {
            element.setFontScaleX(scaleX);
            return this;
        }

        /**
         * Sets the vertical font scale factor.
         *
         * @param scaleY the vertical scaling factor
         * @return this Style instance for chaining
         */
        public Style sizeY(float scaleY) {
            element.setFontScaleY(scaleY);
            return this;
        }

        /**
         * Sets alignment for text within its layout box.
         *
         * @param value alignment mask (e.g. Align.left, Align.center, Align.right)
         * @return this Style instance for chaining
         */
        public Style align(int value) {
            element.setAlignment(value);
            return this;
        }

        /**
         * Sets the alignment of the text block within the element, and the alignment of lines.
         *
         * @param labelAlign alignment mask for the text block within the layout bounds
         * @param lineAlign  alignment mask for individual lines of text
         * @return this Style instance for chaining
         */
        public Style align(int labelAlign, int lineAlign) {
            element.setAlignment(labelAlign, lineAlign);
            return this;
        }

        /**
         * Sets the alignment of the text block within the element's bounds.
         *
         * @param labelAlign alignment mask for the text block (e.g. Align.center, Align.topRight)
         * @return this Style instance for chaining
         */
        public Style labelAlign(int labelAlign) {
            element.setLabelAlign(labelAlign);
            return this;
        }

        /**
         * Sets the alignment of individual lines of text.
         *
         * @param lineAlign alignment mask for text lines (e.g. Align.left, Align.center, Align.right)
         * @return this Style instance for chaining
         */
        public Style lineAlign(int lineAlign) {
            element.setLineAlign(lineAlign);
            return this;
        }

        /**
         * Sets whether the text wraps at the element's width boundaries.
         *
         * @param value true to enable wrapping
         * @return this Style instance for chaining
         */
        public Style wrap(boolean value) {
            element.setWrap(value);
            return this;
        }

        /**
         * Sets the ellipsis text when text is too long and wrapping is disabled.
         *
         * @param value the ellipsis string (e.g. "...")
         * @return this Style instance for chaining
         */
        public Style ellipsis(String value) {
            element.setEllipsis(value);
            return this;
        }

        /**
         * Enables or disables standard ellipsis ("...") truncation.
         *
         * @param value true to enable ellipsis, false to disable
         * @return this Style instance for chaining
         */
        public Style ellipsis(boolean value) {
            element.setEllipsis(value);
            return this;
        }
    }

    protected final NodeSpec sizing = new NodeSpec();
    public final Style style = new Style();

    private final EffectHost effects = new EffectHost();
    private final TextElement element = new TextElement() {

        @Override
        protected void setScene(Scene sceneInstance) {
            boolean hadScene = getScene() != null;
            super.setScene(sceneInstance);
            if (hadScene && sceneInstance == null) Text.this.dispose();
        }
    };

    private Text() {
        element.userObject = this;
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    /**
     * Creates a new reactive Text component.
     *
     * @return a new Text instance
     */
    public static Text of() {
        return new Text();
    }

    /**
     * Registers a reactive style configurator. The configurator runs
     * immediately and re-runs whenever its tracked signal dependencies change.
     */
    public Text style(Cons<Style> configurator) {
        effects.add(() -> configurator.get(style));
        return this;
    }

    @Override
    public Element element() {
        return element;
    }

    @Override
    public NodeSpec sizing() {
        return sizing;
    }

    @Override
    public void dispose() {
        effects.disposeAll();
    }
}
