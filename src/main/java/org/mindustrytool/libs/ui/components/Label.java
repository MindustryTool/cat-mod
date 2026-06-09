package org.mindustrytool.libs.ui.components;

import arc.graphics.Color;
import arc.scene.Element;

import org.mindustrytool.libs.ui.component.AbstractComponent;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.layout.NodeSizing;

import arc.func.Cons;

import static arc.Core.scene;

public class Label extends AbstractComponent {
    public class Style extends ComponentStyle<Style> {
        public final arc.scene.ui.Label.LabelStyle ls;
        Color textColor;
        int textAlign;
        float fontScale = 1f;
        boolean wrap;

        public Style(NodeSizing sizing, arc.scene.ui.Label.LabelStyle ls) { super(sizing); this.ls = ls; }

        public Style text(String v) { element.setText(v); return this; }
        public Style textColor(Color v) { textColor = v; ls.fontColor = v; return this; }
        public Style textAlign(int v) { textAlign = v; element.setAlignment(v); return this; }
        public Style fontScale(float v) { fontScale = v; element.setFontScale(v); return this; }
        public Style wrap(boolean v) { wrap = v; element.setWrap(v); return this; }
        public Style size(Cons<NodeSizing> fn) { fn.get(sizing); return this; }
    }


    private final arc.scene.ui.Label element;
    public final Style style;

    private Label() {
        var arcStyle = new arc.scene.ui.Label.LabelStyle(scene.getStyle(arc.scene.ui.Label.LabelStyle.class));
        this.element = new arc.scene.ui.Label("", arcStyle);
        element.userObject = this;
        this.style = new Style(sizing, arcStyle);
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    public static Label of() { return new Label(); }

    public Label style(Cons<Style> fn) { fn.get(style); element.invalidateHierarchy(); return this; }
    public Label size(Cons<NodeSizing> fn) { fn.get(sizing); element.invalidateHierarchy(); return this; }

    @Override public Element element() { return element; }
}
