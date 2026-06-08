package org.mindustrytool.ui.components;

import arc.scene.Element;
import arc.scene.event.ChangeListener;
import arc.struct.Seq;

import org.mindustrytool.signal.Signal;
import org.mindustrytool.ui.layout.NodeSizing;
import org.mindustrytool.ui.layout.Sizing;

public class Checkbox implements Component {
    public static class Builder {
        private String text;
        private Signal<Boolean> signal;

        public Builder text(String v) { text = v; return this; }
        public Builder bind(Signal<Boolean> v) { signal = v; return this; }

        public Checkbox build() {
            return new Checkbox(text, signal);
        }
    }

    private final arc.scene.ui.CheckBox element;
    public final NodeSizing sizing = new NodeSizing();
    private final Seq<Runnable> subscriptions = new Seq<>();

    private Checkbox(String text, Signal<Boolean> signal) {
        element = new arc.scene.ui.CheckBox(text != null ? text : "");
        element.userObject = this;
        if (signal != null) {
            element.setChecked(signal.get());
            element.changed(() -> signal.set(element.isChecked()));
            subscriptions.add(signal.onChange(element::setChecked));
        }
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    public static Builder build() { return new Builder(); }

    @Override public Element element() { return element; }
    @Override public Sizing sizing() { return sizing; }

    @Override
    public void dispose() { subscriptions.each(Runnable::run); subscriptions.clear(); }
}
