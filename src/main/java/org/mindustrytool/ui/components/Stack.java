package org.mindustrytool.ui.components;

import arc.scene.Element;
import arc.struct.Seq;

import org.mindustrytool.ui.kernel.AbstractComponent;
import org.mindustrytool.ui.style.ComponentStyle;
import org.mindustrytool.ui.layout.NodeSizing;

import arc.func.Cons;

public class Stack extends AbstractComponent {
    public class Style extends ComponentStyle {
        Style(NodeSizing sizing) { super(sizing); }
        public Style size(Cons<NodeSizing> fn) { fn.get(sizing); return this; }
    }

    public final Style style;
    private final arc.scene.ui.layout.Stack element;
    private final Seq<Component> children = new Seq<>();

    private Stack() {
        this.style = new Style(sizing);
        element = new arc.scene.ui.layout.Stack();
        element.userObject = this;
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    public static Stack of() { return new Stack(); }

    public Stack child(Component c) {
        children.add(c);
        element.add(c.element());
        return this;
    }

    public Stack style(Cons<Style> fn) { fn.get(style); element.invalidateHierarchy(); return this; }
    public Stack size(Cons<NodeSizing> fn) { fn.get(sizing); element.invalidateHierarchy(); return this; }

    @Override public Element element() { return element; }

    @Override
    public void dispose() {
        for (Component c : children) c.dispose();
    }
}
