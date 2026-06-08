package com.neko.libs.ui.widget;

import arc.scene.ui.Label;

import com.neko.libs.signal.Signal;
import com.neko.libs.ui.style.StyleSpec;
import com.neko.libs.ui.style.StyleSpec.SizeMode;
import lombok.Setter;
import lombok.experimental.Accessors;

import static arc.Core.scene;

public class NLabel extends Label {

    @Setter @Accessors(fluent = true, chain = true)
    public static class LabelConfig {
        public StyleSpec spec = new StyleSpec();
        Signal<String> text;
        String textFallback = "";
    }

    public final StyleSpec spec;
    private final String textFallback;

    public NLabel(LabelConfig cfg) {
        super(cfg.text != null ? cfg.text.get() : cfg.textFallback,
              new LabelStyle(scene.getStyle(LabelStyle.class)));
        this.spec = cfg.spec;
        this.textFallback = cfg.textFallback;
        applySpec();
        if (cfg.text != null) cfg.text.onChange(v -> setText(v));
    }

    private void applySpec() {
        style.fontColor = spec.textColor();
        setAlignment(spec.textAlign());
        setWrap(spec.wrap());
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
        applySpec();
        super.draw();
    }
}
