package org.mindustrytool.ui.components;

import arc.scene.Element;
import arc.struct.Seq;

import org.mindustrytool.ui.layout.NodeSizing;
import org.mindustrytool.ui.layout.Sizing;

import arc.func.Cons;

public class Stack implements Component {
    public static class Builder {
        private final Seq<Component> children = new Seq<>();
        private Cons<NodeSizing> sizeFn;

        public Builder child(Component v) { children.add(v); return this; }
        public Builder children(Component... v) { for (Component c : v) children.add(c); return this; }
        public Builder size(Cons<NodeSizing> fn) { sizeFn = fn; return this; }

        public Stack build() {
            Stack s = Stack.of(children.toArray());
            if (sizeFn != null) s.size(sizeFn);
            return s;
        }
    }

    private final arc.scene.ui.layout.Stack element;
    public final NodeSizing sizing = new NodeSizing();
    private final Seq<Component> children = new Seq<>();

    private Stack(Component... children) {
        element = new arc.scene.ui.layout.Stack();
        for (Component c : children) {
            this.children.add(c);
            element.add(c.element());
        }
        element.userObject = this;
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    public static Builder build() { return new Builder(); }
    public static Stack of(Component... children) { return new Stack(children); }

    public Stack size(Cons<NodeSizing> fn) { fn.get(sizing); element.invalidateHierarchy(); return this; }

    @Override public Element element() { return element; }
    @Override public Sizing sizing() { return sizing; }

    @Override
    public void dispose() { for (Component c : children) c.dispose(); }
}
