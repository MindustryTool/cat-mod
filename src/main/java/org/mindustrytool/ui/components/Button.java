package org.mindustrytool.ui.components;

import arc.scene.Element;

import org.mindustrytool.ui.kernel.AbstractComponent;
import org.mindustrytool.ui.style.ComponentStyle;
import org.mindustrytool.ui.layout.NodeSizing;

import arc.func.Cons;

import static arc.Core.scene;

public class Button extends AbstractComponent {
    public class Style extends ComponentStyle {
        public final arc.scene.ui.Button.ButtonStyle tbs;

        Style(NodeSizing sizing, arc.scene.ui.Button.ButtonStyle tbs) { super(sizing); this.tbs = tbs; }

        private void applySkin() {
            arc.scene.ui.Button.ButtonStyle skin = scene.getStyle(arc.scene.ui.Button.ButtonStyle.class);
            tbs.up = skin.up;
            tbs.down = skin.down;
            tbs.over = skin.over;
            tbs.checked = skin.checked;
        }

        public Style ghostVariant() { tbs.up = tbs.down = tbs.over = tbs.checked = null; element.setStyle(tbs); return this; }
        public Style primaryVariant() { applySkin(); element.setStyle(tbs); return this; }
        public Style dangerVariant() { applySkin(); element.setStyle(tbs); return this; }
        public Style defaultVariant() { applySkin(); element.setStyle(tbs); return this; }
        public Style size(Cons<NodeSizing> fn) { fn.get(sizing); return this; }
    }

    public class ListenerBuilder {
        public ListenerBuilder changed(Runnable r) { element.changed(r); return this; }
    }

    private final arc.scene.ui.Button element;
    public final Style style;
    private Component childComponent;

    private Button() {
        arc.scene.ui.Button.ButtonStyle arcStyle = new arc.scene.ui.Button.ButtonStyle(scene.getStyle(arc.scene.ui.Button.ButtonStyle.class));
        this.element = new arc.scene.ui.Button(arcStyle);
        element.userObject = this;
        this.style = new Style(sizing, arcStyle);
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    public static Button of() { return new Button(); }

    public Button child(Component c) {
        childComponent = c;
        element.add(c.element());
        return this;
    }

    public Button style(Cons<Style> fn) { fn.get(style); element.invalidateHierarchy(); return this; }
    public Button size(Cons<NodeSizing> fn) { fn.get(sizing); element.invalidateHierarchy(); return this; }
    public Button listener(Cons<ListenerBuilder> fn) { fn.get(new ListenerBuilder()); return this; }

    @Override public Element element() { return element; }

    @Override
    public void dispose() {
        super.dispose();
        if (childComponent != null) childComponent.dispose();
    }
}
