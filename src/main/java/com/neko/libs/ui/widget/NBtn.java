package com.neko.libs.ui.widget;

import arc.scene.ui.TextButton;

import com.neko.libs.signal.Signal;
import com.neko.libs.ui.style.StyleSpec;
import com.neko.libs.ui.style.StyleSpec.SizeMode;
import com.neko.libs.ui.style.Theme;
import lombok.Setter;
import lombok.experimental.Accessors;

import static arc.Core.scene;

public class NBtn extends TextButton {

    @Setter @Accessors(fluent = true, chain = true)
    public static class BtnConfig {
        public StyleSpec spec = new StyleSpec();
        Signal<String> text;
        String textFallback = "";
        Runnable onClick;
    }

    public final StyleSpec spec;
    private final String textFallback;
    private StyleSpec.Variant lastVariant = null;

    public NBtn(BtnConfig cfg) {
        super(cfg.text != null ? cfg.text.get() : cfg.textFallback,
              new TextButtonStyle(scene.getStyle(TextButtonStyle.class)));
        this.spec = cfg.spec;
        this.textFallback = cfg.textFallback;
        label.setWrap(false);
        applyVariant();
        setDisabled(spec.button().disabled());
        if (cfg.text != null) cfg.text.onChange(v -> setText(v));
        if (cfg.onClick != null) clicked(cfg.onClick);
    }

    @Override
    public float getPrefWidth() {
        if (spec == null) return super.getPrefWidth();
        if (spec.widthMode() == SizeMode.FIXED) return spec.constrainW(spec.fixedWidth());
        if (spec.widthMode() == SizeMode.GROW) return 0f;
        return super.getPrefWidth();
    }

    @Override
    public float getPrefHeight() {
        if (spec == null) return super.getPrefHeight();
        if (spec.heightMode() == SizeMode.FIXED) return spec.constrainH(spec.fixedHeight());
        if (spec.heightMode() == SizeMode.GROW) return 0f;
        return super.getPrefHeight();
    }

    @Override
    public void draw() {
        applyVariant();
        super.draw();
    }

    private void applyVariant() {
        StyleSpec.Variant v = spec.button().variant();
        if (v == lastVariant) return;
        lastVariant = v;
        TextButtonStyle s = getStyle();
        switch (v) {
            case PRIMARY -> s.fontColor = Theme.textBright;
            case DANGER  -> s.fontColor = Theme.accentRed;
            case GHOST   -> { s.up = s.down = s.over = s.checked = s.checkedOver = s.disabled = null; s.fontColor = Theme.textSecondary; }
            default      -> { /* DEFAULT — skin defaults already apply */ }
        }
    }
}
