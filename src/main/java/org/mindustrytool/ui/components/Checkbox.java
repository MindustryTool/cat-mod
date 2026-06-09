package org.mindustrytool.ui.components;

import arc.graphics.Color;
import arc.scene.Element;

import org.mindustrytool.ui.kernel.AbstractComponent;
import org.mindustrytool.ui.style.ComponentStyle;
import org.mindustrytool.ui.layout.NodeSizing;

import arc.func.Cons;

import static arc.Core.scene;

public class Checkbox extends AbstractComponent {
    public class Style extends ComponentStyle {
        public arc.scene.ui.CheckBox.CheckBoxStyle ls;

        Style(NodeSizing sizing) { super(sizing);
            var base = scene.getStyle(arc.scene.ui.CheckBox.CheckBoxStyle.class);
            this.ls = new arc.scene.ui.CheckBox.CheckBoxStyle();
            ls.fontColor = base.fontColor;
            ls.checkedFontColor = base.checkedFontColor;
            ls.up = base.up;
            ls.down = base.down;
            ls.over = base.over;
            ls.checked = base.checked;
        }

        public Style text(String v) { element.setText(v); return this; }
        public Style textColor(Color v) { ls.fontColor = v; element.setStyle(ls); return this; }
        public Style checked(boolean v) { element.setChecked(v); return this; }
        public Style size(Cons<NodeSizing> fn) { fn.get(sizing); return this; }
    }

    public class ListenerBuilder {
        public ListenerBuilder changed(Cons<Boolean> fn) {
            element.changed(() -> fn.get(element.isChecked()));
            return this;
        }
    }

    private final arc.scene.ui.CheckBox element;
    public final Style style;

    private Checkbox() {
        var base = scene.getStyle(arc.scene.ui.CheckBox.CheckBoxStyle.class);
        this.element = new arc.scene.ui.CheckBox("");
        element.userObject = this;
        this.style = new Style(sizing);
        element.setStyle(style.ls);
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    public static Checkbox of() { return new Checkbox(); }

    public Checkbox style(Cons<Style> fn) { fn.get(style); element.invalidateHierarchy(); return this; }
    public Checkbox size(Cons<NodeSizing> fn) { fn.get(sizing); element.invalidateHierarchy(); return this; }
    public Checkbox listener(Cons<ListenerBuilder> fn) { fn.get(new ListenerBuilder()); return this; }

    @Override public Element element() { return element; }
}
