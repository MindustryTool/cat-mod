package com.neko.libs.simpleui.components;

import arc.graphics.Color;
import arc.scene.Element;
import arc.struct.Seq;

import com.neko.libs.signal.Signal;
import com.neko.libs.simpleui.layout.NodeSizing;
import com.neko.libs.simpleui.layout.Sizing;

import java.util.function.Consumer;

import static arc.Core.scene;

public class Label implements Component {
    public class Style {
        public final arc.scene.ui.Label.LabelStyle ls;
        Color textColor;
        int textAlign;
        float fontScale = 1f;
        boolean wrap;

        public Style(arc.scene.ui.Label.LabelStyle ls) {
            this.ls = ls;
        }

        public Style textColor(Color v) {
            textColor = v;
            ls.fontColor = v;
            return this;
        }

        public Style textColor(Signal<Color> s) {
            textColor = s.get();
            ls.fontColor = textColor;
            subs.add(s.onChange(v -> { textColor = v; ls.fontColor = v; }));
            return this;
        }

        public Style textAlign(int v) {
            textAlign = v;
            element.setAlignment(v);
            return this;
        }

        public Style fontScale(float v) {
            fontScale = v;
            element.setFontScale(v);
            return this;
        }

        public Style fontScale(Signal<Float> s) {
            fontScale = s.get();
            element.setFontScale(fontScale);
            subs.add(s.onChange(v -> { fontScale = v; element.setFontScale(v); }));
            return this;
        }

        public Style wrap(boolean v) {
            wrap = v;
            element.setWrap(v);
            return this;
        }

        public Style wrap(Signal<Boolean> s) {
            wrap = s.get();
            element.setWrap(wrap);
            subs.add(s.onChange(v -> { wrap = v; element.setWrap(v); }));
            return this;
        }
    }

    private final arc.scene.ui.Label element;
    public final Style style;
    public final NodeSizing sizing = new NodeSizing();
    private final Seq<Runnable> subs = new Seq<>();

    private Label(String text) {
        var arcStyle = new arc.scene.ui.Label.LabelStyle(scene.getStyle(arc.scene.ui.Label.LabelStyle.class));
        this.element = new arc.scene.ui.Label(text, arcStyle);
        element.userObject = this;
        this.style = new Style(arcStyle);
        sizing.onInvalidate(() -> {
            apply();
            element.invalidateHierarchy();
        });
    }

    public static Label of(String text) {
        return new Label(text);
    }

    public static Label of(Signal<String> signal) {
        Label l = new Label(signal.get());
        l.subs.add(signal.onChange(l.element::setText));
        return l;
    }

    public Label text(String text) {
        element.setText(text);
        return this;
    }

    public Label text(Signal<String> signal) {
        element.setText(signal.get());
        subs.add(signal.onChange(element::setText));
        return this;
    }

    public Label style(Consumer<Style> fn) {
        fn.accept(style);
        return this;
    }

    public Label size(Consumer<NodeSizing> fn) {
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
