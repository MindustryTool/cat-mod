package org.mindustrytool.libs.ui.layout;

import lombok.Getter;

public class LayoutSpec extends NodeSpec<LayoutSpec> {

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

    private @Getter boolean isColumn = false;
    private @Getter boolean isWrap = false;
    private @Getter boolean isReverse = false;
    private @Getter float gap = 0.0f;
    private @Getter JustifyContent justifyContent = JustifyContent.START;
    private @Getter AlignItems alignItems = AlignItems.STRETCH;

    public LayoutSpec column() {
        this.isColumn = true;
        invalidate();
        return this;
    }

    public LayoutSpec row() {
        this.isColumn = false;
        invalidate();
        return this;
    }

    public LayoutSpec wrap() {
        this.isWrap = true;
        invalidate();
        return this;
    }

    public LayoutSpec noWrap() {
        this.isWrap = false;
        invalidate();
        return this;
    }

    public LayoutSpec reverse() {
        this.isReverse = true;
        invalidate();
        return this;
    }

    public LayoutSpec reverse(boolean reverse) {
        this.isReverse = reverse;
        invalidate();
        return this;
    }

    public LayoutSpec gap(float value) {
        this.gap = value;
        invalidate();
        return this;
    }

    public LayoutSpec justifyContent(JustifyContent justifyContent) {
        this.justifyContent = justifyContent;
        invalidate();
        return this;
    }

    public LayoutSpec alignItems(AlignItems alignItems) {
        this.alignItems = alignItems;
        invalidate();
        return this;
    }

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
