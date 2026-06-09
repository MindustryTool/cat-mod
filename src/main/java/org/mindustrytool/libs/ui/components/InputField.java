package org.mindustrytool.ui.components;

import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.event.ChangeListener;

import org.mindustrytool.ui.kernel.AbstractComponent;
import org.mindustrytool.ui.style.ComponentStyle;
import org.mindustrytool.ui.layout.NodeSizing;

import arc.func.Cons;

import static arc.Core.scene;

public class InputField extends AbstractComponent {
    public class Style extends ComponentStyle {
        public final arc.scene.ui.TextField.TextFieldStyle ls;

        Style(NodeSizing sizing, arc.scene.ui.TextField.TextFieldStyle ls) { super(sizing); this.ls = ls; }

        public Style text(String v) { element.setText(v); return this; }
        public Style placeholder(String v) { element.setMessageText(v); return this; }
        public Style textColor(Color v) { ls.fontColor = v; element.setStyle(ls); return this; }
        public Style size(Cons<NodeSizing> fn) { fn.get(sizing); return this; }
    }

    public class ListenerBuilder {
        public ListenerBuilder changed(Cons<String> fn) {
            element.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Element actor) { fn.get(element.getText()); }
            });
            return this;
        }
    }

    private final arc.scene.ui.TextField element;
    public final Style style;

    private InputField() {
        var base = scene.getStyle(arc.scene.ui.TextField.TextFieldStyle.class);
        var st = new arc.scene.ui.TextField.TextFieldStyle(base);
        this.element = new arc.scene.ui.TextField("", st);
        element.userObject = this;
        this.style = new Style(sizing, st);
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    public static InputField of() { return new InputField(); }

    public InputField style(Cons<Style> fn) { fn.get(style); element.invalidateHierarchy(); return this; }
    public InputField size(Cons<NodeSizing> fn) { fn.get(sizing); element.invalidateHierarchy(); return this; }
    public InputField listener(Cons<ListenerBuilder> fn) { fn.get(new ListenerBuilder()); return this; }

    @Override public Element element() { return element; }
}
