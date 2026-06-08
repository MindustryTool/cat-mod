package com.neko.libs.simpleui.components;

import arc.graphics.Color;
import arc.scene.Element;
import arc.struct.Seq;

import com.neko.libs.signal.Signal;
import com.neko.libs.simpleui.layout.NodeSizing;
import com.neko.libs.simpleui.layout.Sizing;

import java.util.function.Consumer;
import java.util.function.Function;

import static arc.Core.scene;

public class TextButton implements UIComponent {
    public class ButtonStyle {
        public final arc.scene.ui.TextButton.TextButtonStyle tbs;

        ButtonStyle(arc.scene.ui.TextButton.TextButtonStyle tbs) {
            this.tbs = tbs;
        }

        public ButtonStyle ghostVariant() {
            arc.scene.ui.TextButton.TextButtonStyle skin = scene.getStyle(arc.scene.ui.TextButton.TextButtonStyle.class);
            tbs.up = tbs.down = tbs.over = tbs.checked = null;
            tbs.fontColor = Color.gray;
            tbs.downFontColor = Color.lightGray;
            tbs.overFontColor = Color.white;
            tbs.disabledFontColor = Color.darkGray;
            return this;
        }

        public ButtonStyle primaryVariant() {
            arc.scene.ui.TextButton.TextButtonStyle skin = scene.getStyle(arc.scene.ui.TextButton.TextButtonStyle.class);
            tbs.up = skin.up; tbs.down = skin.down;
            tbs.over = skin.over; tbs.checked = skin.checked;
            tbs.fontColor = Color.white;
            tbs.downFontColor = Color.lightGray;
            tbs.overFontColor = Color.white;
            tbs.disabledFontColor = Color.gray;
            return this;
        }

        public ButtonStyle dangerVariant() {
            arc.scene.ui.TextButton.TextButtonStyle skin = scene.getStyle(arc.scene.ui.TextButton.TextButtonStyle.class);
            tbs.up = skin.up; tbs.down = skin.down;
            tbs.over = skin.over; tbs.checked = skin.checked;
            tbs.fontColor = Color.scarlet;
            tbs.downFontColor = Color.red;
            tbs.overFontColor = Color.scarlet;
            tbs.disabledFontColor = Color.gray;
            return this;
        }

        public ButtonStyle defaultVariant() {
            arc.scene.ui.TextButton.TextButtonStyle skin = scene.getStyle(arc.scene.ui.TextButton.TextButtonStyle.class);
            tbs.up = skin.up; tbs.down = skin.down;
            tbs.over = skin.over; tbs.checked = skin.checked;
            tbs.fontColor = Color.lightGray;
            tbs.downFontColor = Color.gray;
            tbs.overFontColor = Color.white;
            tbs.disabledFontColor = Color.gray;
            return this;
        }

        public ButtonStyle disabled(boolean v) {
            element.setDisabled(v);
            return this;
        }

        public ButtonStyle disabled(Signal<Boolean> s) {
            element.setDisabled(s.get());
            subs.add(s.onChange(element::setDisabled));
            return this;
        }

        public ButtonStyle checked(boolean v) {
            element.setChecked(v);
            return this;
        }

        public ButtonStyle checked(Signal<Boolean> s) {
            element.setChecked(s.get());
            subs.add(s.onChange(element::setChecked));
            return this;
        }

        public void toggle() {
            element.toggle();
        }
    }

    private final arc.scene.ui.TextButton element;
    public final ButtonStyle style;
    public final NodeSizing sizing = new NodeSizing();
    private final Seq<Runnable> subs = new Seq<>();

    private TextButton() {
        arc.scene.ui.TextButton.TextButtonStyle tbs = new arc.scene.ui.TextButton.TextButtonStyle(scene.getStyle(arc.scene.ui.TextButton.TextButtonStyle.class));
        this.element = new arc.scene.ui.TextButton("", tbs);
        element.getLabel().setWrap(false);
        element.userObject = this;
        this.style = new ButtonStyle(tbs);
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    public static TextButton of() {
        return new TextButton();
    }

    public TextButton text(String text) {
        element.setText(text);
        return this;
    }

    public TextButton text(Signal<String> signal) {
        element.setText(signal.get());
        subs.add(signal.onChange(element::setText));
        return this;
    }

    public TextButton onClick(Runnable r) {
        element.changed(r);
        return this;
    }

    public TextButton ghostVariant() {
        style.ghostVariant();
        return this;
    }

    public TextButton primaryVariant() {
        style.primaryVariant();
        return this;
    }

    public TextButton dangerVariant() {
        style.dangerVariant();
        return this;
    }

    public TextButton defaultVariant() {
        style.defaultVariant();
        return this;
    }

    public TextButton style(Consumer<ButtonStyle> fn) {
        fn.accept(style);
        return this;
    }

    public TextButton size(Consumer<NodeSizing> fn) {
        fn.accept(sizing);
        element.invalidateHierarchy();
        return this;
    }

    public TextButton bind(Function<TextButton, Runnable> fn) {
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
    }
}
