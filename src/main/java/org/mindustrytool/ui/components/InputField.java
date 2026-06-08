package org.mindustrytool.ui.components;

import arc.func.Cons;
import arc.scene.Element;
import arc.scene.event.ChangeListener;
import arc.struct.Seq;

import org.mindustrytool.signal.Signal;
import org.mindustrytool.ui.layout.NodeSizing;
import org.mindustrytool.ui.layout.Sizing;

import static arc.Core.scene;

public class InputField implements Component {
    public static class Builder {
        private String placeholder;
        private Signal<String> signal;
        private Cons<NodeSizing> sizeFn;

        public Builder placeholder(String v) { placeholder = v; return this; }
        public Builder bind(Signal<String> v) { signal = v; return this; }
        public Builder size(Cons<NodeSizing> fn) { sizeFn = fn; return this; }

        public InputField build() {
            var f = new InputField(placeholder, signal);
            if (sizeFn != null) sizeFn.get(f.sizing);
            return f;
        }
    }

    private final arc.scene.ui.TextField element;
    public final NodeSizing sizing = new NodeSizing();
    private final Seq<Runnable> subscriptions = new Seq<>();

    private InputField(String placeholder, Signal<String> signal) {
        var base = scene.getStyle(arc.scene.ui.TextField.TextFieldStyle.class);
        var st = new arc.scene.ui.TextField.TextFieldStyle(base);
        element = new arc.scene.ui.TextField(placeholder != null ? placeholder : "", st);
        element.userObject = this;
        if (signal != null) {
            element.setText(signal.get());
            element.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Element actor) {
                    signal.set(element.getText());
                }
            });
            subscriptions.add(signal.onChange(element::setText));
        }
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    public static Builder build() { return new Builder(); }

    @Override public Element element() { return element; }
    @Override public Sizing sizing() { return sizing; }

    @Override
    public void dispose() { subscriptions.each(Runnable::run); subscriptions.clear(); }
}
