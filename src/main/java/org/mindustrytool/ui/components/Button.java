package org.mindustrytool.ui.components;

import arc.scene.Element;
import arc.scene.ui.Button.ButtonStyle;
import arc.struct.Seq;

import org.mindustrytool.ui.layout.NodeSizing;
import org.mindustrytool.ui.layout.Sizing;

import arc.func.Cons;
import arc.func.Func;

import static arc.Core.scene;

public class Button implements Component {
    public static class Builder {
        private Component child;
        private Runnable clickedFn;
        private Cons<Style> styleFn;
        private Cons<NodeSizing> sizeFn;
        private Func<Button, Runnable> bindFn;

        public Builder child(Component v) { child = v; return this; }
        public Builder clicked(Runnable r) { clickedFn = r; return this; }
        public Builder style(Cons<Style> fn) { styleFn = fn; return this; }
        public Builder size(Cons<NodeSizing> fn) { sizeFn = fn; return this; }
        public Builder bind(Func<Button, Runnable> fn) { bindFn = fn; return this; }

        public Button build() {
            Button b = Button.of(child);
            if (clickedFn != null) b.clicked(clickedFn);
            if (styleFn != null) b.style(styleFn);
            if (sizeFn != null) b.size(sizeFn);
            if (bindFn != null) b.bind(bindFn);
            return b;
        }
    }

    public class Style {
        public final ButtonStyle tbs;

        Style(ButtonStyle tbs) { this.tbs = tbs; }

        private void applySkin() {
            ButtonStyle skin = scene.getStyle(ButtonStyle.class);
            tbs.up = skin.up;
            tbs.down = skin.down;
            tbs.over = skin.over;
            tbs.checked = skin.checked;
        }

        public Style ghostVariant() {
            tbs.up = tbs.down = tbs.over = tbs.checked = null;
            return this;
        }

        public Style primaryVariant() {
            applySkin();
            return this;
        }

        public Style dangerVariant() {
            applySkin();
            return this;
        }

        public Style defaultVariant() {
            applySkin();
            return this;
        }
    }

    private final arc.scene.ui.Button element;
    public final Style style;
    public final NodeSizing sizing = new NodeSizing();
    private final Seq<Runnable> subscriptions = new Seq<>();
    private final Component child;

    private Button(Component child) {
        this.child = child;
        ButtonStyle arcStyle = new ButtonStyle(scene.getStyle(ButtonStyle.class));
        this.element = new arc.scene.ui.Button(arcStyle);
        element.add(child.element());
        element.userObject = this;
        this.style = new Style(arcStyle);
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    public static Builder build() { return new Builder(); }
    public static Button of(Component child) { return new Button(child); }

    public Button clicked(Runnable r) { element.changed(r); return this; }
    public Button style(Cons<Style> fn) { fn.get(style); element.setStyle(style.tbs); return this; }
    public Button size(Cons<NodeSizing> fn) { fn.get(sizing); element.invalidateHierarchy(); return this; }

    public Button ghostVariant() { style.ghostVariant(); element.setStyle(style.tbs); return this; }
    public Button primaryVariant() { style.primaryVariant(); element.setStyle(style.tbs); return this; }
    public Button dangerVariant() { style.dangerVariant(); element.setStyle(style.tbs); return this; }
    public Button defaultVariant() { style.defaultVariant(); element.setStyle(style.tbs); return this; }

    public Button bind(Func<Button, Runnable> fn) {
        Runnable cleanup = fn.get(this);
        if (cleanup != null) subscriptions.add(cleanup);
        return this;
    }

    @Override public Element element() { return element; }
    @Override public Sizing sizing() { return sizing; }

    @Override
    public void dispose() { subscriptions.each(Runnable::run); subscriptions.clear(); child.dispose(); }
}
