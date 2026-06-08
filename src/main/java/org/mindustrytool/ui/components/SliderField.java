package org.mindustrytool.ui.components;

import arc.func.Cons;
import arc.scene.Element;
import arc.scene.event.ChangeListener;
import arc.struct.Seq;

import org.mindustrytool.signal.Signal;
import org.mindustrytool.ui.layout.NodeSizing;
import org.mindustrytool.ui.layout.Sizing;

public class SliderField implements Component {
    public static class Builder {
        private float min, max, step = 1f;
        private Signal<Float> signal;
        private boolean vertical;
        private Cons<NodeSizing> sizeFn;

        public Builder range(float min, float max, float step) {
            this.min = min; this.max = max; this.step = step; return this;
        }
        public Builder bind(Signal<Float> v) { signal = v; return this; }
        public Builder vertical(boolean v) { vertical = v; return this; }
        public Builder size(Cons<NodeSizing> fn) { sizeFn = fn; return this; }

        public SliderField build() {
            var f = new SliderField(min, max, step, vertical, signal);
            if (sizeFn != null) sizeFn.get(f.sizing);
            return f;
        }
    }

    private final arc.scene.ui.Slider element;
    public final NodeSizing sizing = new NodeSizing();
    private final Seq<Runnable> subscriptions = new Seq<>();

    private SliderField(float min, float max, float step, boolean vertical, Signal<Float> signal) {
        element = new arc.scene.ui.Slider(min, max, step, vertical);
        element.userObject = this;
        if (signal != null) {
            element.setValue(signal.get());
            element.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Element actor) {
                    signal.set(element.getValue());
                }
            });
            subscriptions.add(signal.onChange(element::setValue));
        }
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    public static Builder build() { return new Builder(); }

    @Override public Element element() { return element; }
    @Override public Sizing sizing() { return sizing; }

    @Override
    public void dispose() { subscriptions.each(Runnable::run); subscriptions.clear(); }
}
