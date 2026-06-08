package com.neko.libs.simpleui.components;

import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.struct.Seq;

import com.neko.libs.signal.Signal;
import com.neko.libs.simpleui.layout.NodeSizing;
import com.neko.libs.simpleui.layout.Sizing;

import java.util.function.Consumer;
import java.util.function.Function;

import static arc.Core.scene;

public class CTextButton implements UIComponent {
    public class ButtonStyle {
        public final TextButtonStyle tbs;

        ButtonStyle(TextButtonStyle tbs) {
            this.tbs = tbs;
        }

        public ButtonStyle ghostVariant() {
            TextButtonStyle skin = scene.getStyle(TextButtonStyle.class);
            tbs.up = tbs.down = tbs.over = tbs.checked = null;
            tbs.fontColor = Color.gray;
            tbs.downFontColor = Color.lightGray;
            tbs.overFontColor = Color.white;
            tbs.disabledFontColor = Color.darkGray;
            return this;
        }

        public ButtonStyle primaryVariant() {
            TextButtonStyle skin = scene.getStyle(TextButtonStyle.class);
            tbs.up = skin.up; tbs.down = skin.down;
            tbs.over = skin.over; tbs.checked = skin.checked;
            tbs.fontColor = Color.white;
            tbs.downFontColor = Color.lightGray;
            tbs.overFontColor = Color.white;
            tbs.disabledFontColor = Color.gray;
            return this;
        }

        public ButtonStyle dangerVariant() {
            TextButtonStyle skin = scene.getStyle(TextButtonStyle.class);
            tbs.up = skin.up; tbs.down = skin.down;
            tbs.over = skin.over; tbs.checked = skin.checked;
            tbs.fontColor = Color.scarlet;
            tbs.downFontColor = Color.red;
            tbs.overFontColor = Color.scarlet;
            tbs.disabledFontColor = Color.gray;
            return this;
        }

        public ButtonStyle defaultVariant() {
            TextButtonStyle skin = scene.getStyle(TextButtonStyle.class);
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

    private final TextButton element;
    public final ButtonStyle style;
    public final NodeSizing sizing = new NodeSizing();
    private final Seq<Runnable> subs = new Seq<>();

    private CTextButton() {
        TextButtonStyle tbs = new TextButtonStyle(scene.getStyle(TextButtonStyle.class));
        this.element = new TextButton("", tbs);
        element.getLabel().setWrap(false);
        element.userObject = this;
        this.style = new ButtonStyle(tbs);
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    public static CTextButton of() {
        return new CTextButton();
    }

    public CTextButton text(String text) {
        element.setText(text);
        return this;
    }

    public CTextButton text(Signal<String> signal) {
        element.setText(signal.get());
        subs.add(signal.onChange(element::setText));
        return this;
    }

    public CTextButton onClick(Runnable r) {
        element.changed(r);
        return this;
    }

    public CTextButton ghostVariant() {
        style.ghostVariant();
        return this;
    }

    public CTextButton primaryVariant() {
        style.primaryVariant();
        return this;
    }

    public CTextButton dangerVariant() {
        style.dangerVariant();
        return this;
    }

    public CTextButton defaultVariant() {
        style.defaultVariant();
        return this;
    }

    public CTextButton style(Consumer<ButtonStyle> fn) {
        fn.accept(style);
        return this;
    }

    public CTextButton size(Consumer<NodeSizing> fn) {
        fn.accept(sizing);
        element.invalidateHierarchy();
        return this;
    }

    public CTextButton bind(Function<CTextButton, Runnable> fn) {
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
