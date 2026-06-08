package com.neko.libs.simpleui.components;

import arc.scene.Element;
import arc.struct.Seq;

import arc.scene.ui.Button.ButtonStyle;

import com.neko.libs.simpleui.layout.NodeSizing;
import com.neko.libs.simpleui.layout.Sizing;

import java.util.function.Consumer;
import java.util.function.Function;

import static arc.Core.scene;

public class Button implements Component {
    public class Style {
        public final ButtonStyle tbs;

        Style(ButtonStyle tbs) {
            this.tbs = tbs;
        }

        public Style ghostVariant() {
            ButtonStyle skin = scene.getStyle(ButtonStyle.class);
            tbs.up = tbs.down = tbs.over = tbs.checked = null;
            return this;
        }

        public Style primaryVariant() {
            ButtonStyle skin = scene.getStyle(ButtonStyle.class);
            tbs.up = skin.up; tbs.down = skin.down;
            tbs.over = skin.over; tbs.checked = skin.checked;
            return this;
        }

        public Style dangerVariant() {
            ButtonStyle skin = scene.getStyle(ButtonStyle.class);
            tbs.up = skin.up; tbs.down = skin.down;
            tbs.over = skin.over; tbs.checked = skin.checked;
            return this;
        }

        public Style defaultVariant() {
            ButtonStyle skin = scene.getStyle(ButtonStyle.class);
            tbs.up = skin.up; tbs.down = skin.down;
            tbs.over = skin.over; tbs.checked = skin.checked;
            return this;
        }
    }

    private final arc.scene.ui.Button element;
    public final Style style;
    public final NodeSizing sizing = new NodeSizing();
    private final Seq<Runnable> subs = new Seq<>();
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

    public static Button of(Component child) {
        return new Button(child);
    }

    public Button onClick(Runnable r) {
        element.changed(r);
        return this;
    }

    public Button style(Consumer<Style> fn) {
        fn.accept(style);
        return this;
    }

    public Button size(Consumer<NodeSizing> fn) {
        fn.accept(sizing);
        element.invalidateHierarchy();
        return this;
    }

    public Button ghostVariant() {
        style.ghostVariant();
        return this;
    }

    public Button primaryVariant() {
        style.primaryVariant();
        return this;
    }

    public Button dangerVariant() {
        style.dangerVariant();
        return this;
    }

    public Button defaultVariant() {
        style.defaultVariant();
        return this;
    }

    public Button bind(Function<Button, Runnable> fn) {
        Runnable cleanup = fn.apply(this);
        if (cleanup != null) subs.add(cleanup);
        return this;
    }

    @Override
    public Element element() { return element; }

    @Override
    public Sizing sizing() { return sizing; }

    @Override
    public void onDestroy() {
        subs.each(Runnable::run);
        subs.clear();
        child.onDestroy();
    }
}
