package org.mindustrytool.ui.components;

import arc.scene.Element;
import arc.scene.event.ChangeListener;
import arc.scene.event.ChangeListener.ChangeEvent;

import org.mindustrytool.ui.kernel.AbstractComponent;
import org.mindustrytool.ui.style.ComponentStyle;
import org.mindustrytool.ui.layout.NodeSizing;

import arc.func.Cons;

import static arc.Core.scene;

public class SliderField extends AbstractComponent {
    public class Style extends ComponentStyle {
        public final arc.scene.ui.Slider.SliderStyle ls;

        Style(NodeSizing sizing, arc.scene.ui.Slider.SliderStyle ls) { super(sizing); this.ls = ls; }

        public Style range(float min, float max) { element.setRange(min, max); return this; }
        public Style step(float v) { element.setStepSize(v); return this; }
        public Style value(float v) { element.setValue(v); return this; }
        public Style size(Cons<NodeSizing> fn) { fn.get(sizing); return this; }
    }

    public class ListenerBuilder {
        public ListenerBuilder changed(Cons<Float> fn) {
            element.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Element actor) { fn.get(element.getValue()); }
            });
            return this;
        }
    }

    private final arc.scene.ui.Slider element;
    public final Style style;

    private SliderField() {
        var base = scene.getStyle(arc.scene.ui.Slider.SliderStyle.class);
        var st = new arc.scene.ui.Slider.SliderStyle();
        st.knob = base.knob;
        st.knobDown = base.knobDown;
        st.knobOver = base.knobOver;
        st.background = base.background;
        st.disabledKnob = base.disabledKnob;
        st.disabledBackground = base.disabledBackground;
        this.element = new arc.scene.ui.Slider(0f, 100f, 1f, false);
        element.setStyle(st);
        element.userObject = this;
        this.style = new Style(sizing, st);
        sizing.onInvalidate(element::invalidateHierarchy);
    }

    public static SliderField of() { return new SliderField(); }

    public SliderField style(Cons<Style> fn) { fn.get(style); element.invalidateHierarchy(); return this; }
    public SliderField size(Cons<NodeSizing> fn) { fn.get(sizing); element.invalidateHierarchy(); return this; }
    public SliderField listener(Cons<ListenerBuilder> fn) { fn.get(new ListenerBuilder()); return this; }

    @Override public Element element() { return element; }
}
