package com.neko.libs.simpleui.components;

import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.Label;
import arc.struct.Seq;

import com.neko.libs.signal.Signal;
import com.neko.libs.simpleui.layout.NodeSizing;
import com.neko.libs.simpleui.layout.Sizing;

import java.util.function.Consumer;

import static arc.Core.scene;

public class CLabel implements UIComponent {
    public class LabelStyle {
        public final Label.LabelStyle ls;
        Color textColor;
        int textAlign;
        float fontScale = 1f;
        boolean wrap;

        public LabelStyle(Label.LabelStyle ls) {
            this.ls = ls;
        }

        public LabelStyle textColor(Color v) {
            textColor = v;
            ls.fontColor = v;
            return this;
        }

        public LabelStyle textColor(Signal<Color> s) {
            textColor = s.get();
            ls.fontColor = textColor;
            subs.add(s.onChange(v -> { textColor = v; ls.fontColor = v; }));
            return this;
        }

        public LabelStyle textAlign(int v) {
            textAlign = v;
            element.setAlignment(v);
            return this;
        }

        public LabelStyle fontScale(float v) {
            fontScale = v;
            element.setFontScale(v);
            return this;
        }

        public LabelStyle fontScale(Signal<Float> s) {
            fontScale = s.get();
            element.setFontScale(fontScale);
            subs.add(s.onChange(v -> { fontScale = v; element.setFontScale(v); }));
            return this;
        }

        public LabelStyle wrap(boolean v) {
            wrap = v;
            element.setWrap(v);
            return this;
        }

        public LabelStyle wrap(Signal<Boolean> s) {
            wrap = s.get();
            element.setWrap(wrap);
            subs.add(s.onChange(v -> { wrap = v; element.setWrap(v); }));
            return this;
        }
    }

    private final Label element;
    public final LabelStyle style;
    public final NodeSizing sizing = new NodeSizing();
    private final Seq<Runnable> subs = new Seq<>();

    private CLabel(String text) {
        var arcStyle = new Label.LabelStyle(scene.getStyle(Label.LabelStyle.class));
        this.element = new Label(text, arcStyle);
        element.userObject = this;
        this.style = new LabelStyle(arcStyle);
        sizing.onInvalidate(() -> {
            apply();
            element.invalidateHierarchy();
        });
    }

    public static CLabel of(String text) {
        return new CLabel(text);
    }

    public static CLabel of(Signal<String> signal) {
        CLabel l = new CLabel(signal.get());
        l.subs.add(signal.onChange(l.element::setText));
        return l;
    }

    public CLabel text(String text) {
        element.setText(text);
        return this;
    }

    public CLabel text(Signal<String> signal) {
        element.setText(signal.get());
        subs.add(signal.onChange(element::setText));
        return this;
    }

    public CLabel style(Consumer<LabelStyle> fn) {
        fn.accept(style);
        return this;
    }

    public CLabel size(Consumer<NodeSizing> fn) {
        fn.accept(sizing);
        element.invalidateHierarchy();
        return this;
    }

    private void apply() {
        if (style.textColor != null) style.ls.fontColor = style.textColor;
        element.setAlignment(style.textAlign);
        element.setFontScale(style.fontScale);
        element.setWrap(style.wrap);
    }

    @Override
    public Element element() { return element; }

    @Override
    public Sizing sizing() { return sizing; }

    @Override
    public void onDestroy() {
        subs.each(Runnable::run);
        subs.clear();
    }
}
