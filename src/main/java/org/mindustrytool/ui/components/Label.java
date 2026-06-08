package org.mindustrytool.ui.components;

import arc.graphics.Color;
import arc.scene.Element;
import arc.struct.Seq;

import org.mindustrytool.signal.ReadOnlySignal;
import org.mindustrytool.ui.layout.NodeSizing;
import org.mindustrytool.ui.layout.Sizing;

import arc.func.Cons;

import static arc.Core.scene;

public class Label implements Component {
    public static class Builder {
        private String text;
        private ReadOnlySignal<String> textSignal;
        private Cons<Style> styleFn;
        private Cons<NodeSizing> sizeFn;

        public Builder text(String v) { text = v; textSignal = null; return this; }
        public Builder text(ReadOnlySignal<String> v) { textSignal = v; text = null; return this; }
        public Builder style(Cons<Style> fn) { styleFn = fn; return this; }
        public Builder size(Cons<NodeSizing> fn) { sizeFn = fn; return this; }

        public Label build() {
            Label l = textSignal != null ? Label.of(textSignal) : Label.of(text != null ? text : "");
            if (styleFn != null) l.style(styleFn);
            if (sizeFn != null) l.size(sizeFn);
            return l;
        }
    }

    public class Style {
        public final arc.scene.ui.Label.LabelStyle ls;
        Color textColor;
        int textAlign;
        float fontScale = 1f;
        boolean wrap;

        public Style(arc.scene.ui.Label.LabelStyle ls) { this.ls = ls; }

        public Style textColor(Color v) { textColor = v; ls.fontColor = v; return this; }
        public Style textAlign(int v) { textAlign = v; element.setAlignment(v); return this; }
        public Style fontScale(float v) { fontScale = v; element.setFontScale(v); return this; }
        public Style wrap(boolean v) { wrap = v; element.setWrap(v); return this; }
    }

    private final arc.scene.ui.Label element;
    public final Style style;
    public final NodeSizing sizing = new NodeSizing();
    private final Seq<Runnable> subscriptions = new Seq<>();

    private Label(String text) {
        var arcStyle = new arc.scene.ui.Label.LabelStyle(scene.getStyle(arc.scene.ui.Label.LabelStyle.class));
        this.element = new arc.scene.ui.Label(text, arcStyle);
        element.userObject = this;
        this.style = new Style(arcStyle);
        sizing.onInvalidate(() -> { apply(); element.invalidateHierarchy(); });
    }

    public static Builder build() { return new Builder(); }
    public static Label of(String text) { return new Label(text); }
    public static Label of(ReadOnlySignal<String> signal) {
        Label l = new Label(signal.get());
        l.subscriptions.add(signal.onChange(l.element::setText));
        return l;
    }

    public Label text(String text) { element.setText(text); return this; }
    public Label text(ReadOnlySignal<String> signal) {
        element.setText(signal.get());
        subscriptions.add(signal.onChange(element::setText));
        return this;
    }

    public Label style(Cons<Style> fn) { fn.get(style); return this; }
    public Label size(Cons<NodeSizing> fn) { fn.get(sizing); element.invalidateHierarchy(); return this; }

    private void apply() {
        if (style.textColor != null) style.ls.fontColor = style.textColor;
        element.setAlignment(style.textAlign);
        element.setFontScale(style.fontScale);
        element.setWrap(style.wrap);
    }

    @Override public Element element() { return element; }
    @Override public Sizing sizing() { return sizing; }

    @Override
    public void dispose() { subscriptions.each(Runnable::run); subscriptions.clear(); }
}
