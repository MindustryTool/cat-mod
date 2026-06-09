package org.mindustrytool.libs.ui.components;

import arc.graphics.Color;
import arc.scene.Element;

import org.mindustrytool.libs.ui.component.AbstractComponent;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.layout.NodeSizing;

import arc.func.Cons;

import static arc.Core.scene;

public class Checkbox extends AbstractComponent {
    public class Style extends ComponentStyle<Style> {
        public arc.scene.ui.CheckBox.CheckBoxStyle ls;

        Style(NodeSizing sizing) { super(sizing);
            var base = scene.getStyle(arc.scene.ui.CheckBox.CheckBoxStyle.class);
            this.ls = new arc.scene.ui.CheckBox.CheckBoxStyle();
            ls.font = base.font;
            ls.fontColor = base.fontColor;
            ls.checkedFontColor = base.checkedFontColor;
            ls.up = base.up;
            ls.down = base.down;
            ls.over = base.over;
            ls.checked = base.checked;
            ls.checkboxOn = base.checkboxOn;
            ls.checkboxOff = base.checkboxOff;
            ls.checkboxOver = base.checkboxOver;
        }

        private void apply() { element.setStyle(ls); }

        public Style text(String v) { element.setText(v); return this; }
        public Style textColor(Color v) { ls.fontColor = v; apply(); return this; }
        public Style checked(boolean v) { element.setChecked(v); return this; }
        public Style size(Cons<NodeSizing> fn) { fn.get(sizing); return this; }
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

    @Override public Element element() { return element; }
}
