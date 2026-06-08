package com.neko.libs.simpleui.components;

import arc.scene.Element;
import arc.struct.Seq;

import com.neko.libs.simpleui.layout.NodeSizing;
import com.neko.libs.simpleui.layout.Sizing;

import java.util.function.Consumer;

public class Stack implements Component {
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

    public static Stack of(Component... children) {
        return new Stack(children);
    }

    public Stack size(Consumer<NodeSizing> fn) {
        fn.accept(sizing);
        element.invalidateHierarchy();
        return this;
    }

    @Override
    public Element element() { return element; }

    @Override
    public Sizing sizing() { return sizing; }

    @Override
    public void onDestroy() {
        for (Component c : children) c.onDestroy();
    }
}
