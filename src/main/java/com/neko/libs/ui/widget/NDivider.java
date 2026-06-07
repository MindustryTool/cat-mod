package com.neko.libs.ui.widget;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import com.neko.libs.ui.El;
import com.neko.libs.ui.layout.LayoutCtx;
import com.neko.libs.ui.style.StyleSpec;
import com.neko.libs.ui.style.Theme;

/**
 * 1px divider line.
 *
 * <p>Defaults to horizontal ({@code grow-x h:1}).
 * Pass {@code "vertical"} or use the factory to get a vertical divider.
 */
public class NDivider extends El {

    private final boolean vertical;
    private Color color = Theme.borderSubtle;

    public NDivider() {
        this(false);
    }

    public NDivider(boolean vertical) {
        super(vertical ? "grow-y w:1" : "grow-x h:1");
        this.vertical = vertical;
    }

    public NDivider color(Color c) { this.color = c; return this; }

    @Override
    protected float contentPrefWidth(LayoutCtx ctx, float innerW, StyleSpec s) {
        return vertical ? 1f : Math.max(1f, innerW);
    }

    @Override
    protected float contentPrefHeight(LayoutCtx ctx, float innerW, float availH, StyleSpec s) {
        return vertical ? Math.max(1f, availH) : 1f;
    }

    @Override
    protected void drawContent(float alpha) {
        Color prev = Draw.getColor().cpy();
        Draw.color(color, alpha);
        Fill.rect(getX() + getW() / 2f, getY() + getH() / 2f, getW(), getH());
        Draw.color(prev);
    }
}
