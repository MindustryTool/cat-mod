package org.mindustrytool.libs.ui.widget;

import arc.scene.Element;
import org.junit.jupiter.api.Test;
import org.mindustrytool.libs.ui.layout.LayoutSpec;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class StatelessWidgetTest {

    static class TestState {
        final AtomicInteger mounts = new AtomicInteger(0);
        final AtomicInteger updates = new AtomicInteger(0);
        final AtomicInteger disposes = new AtomicInteger(0);
    }

    static class LeafWidget implements Widget {
        final String value;
        final TestState state;

        LeafWidget(String value, TestState state) {
            this.value = value;
            this.state = state;
        }

        @Override
        public ElementNode createElement() {
            return new ElementNode(this) {
                {
                    this.arcElement = new Element();
                }

                @Override
                public LayoutSpec sizing() {
                    return LayoutSpec.defaultSpec();
                }

                @Override
                public void mount(ElementNode parent) {
                    state.mounts.incrementAndGet();
                }

                @Override
                public void update(Widget newWidget) {
                    super.update(newWidget);
                    state.updates.incrementAndGet();
                }

                @Override
                public void dispose() {
                    state.disposes.incrementAndGet();
                    super.dispose();
                }
            };
        }
    }

    static class WrapperWidget extends StatelessWidget {
        final Widget child;

        WrapperWidget(Widget child) {
            this.child = child;
        }

        @Override
        public Widget build() {
            return child;
        }
    }

    @Test
    void testStatelessWidgetLifecycle() {
        TestState state = new TestState();
        LeafWidget leaf = new LeafWidget("hello", state);
        WrapperWidget wrapper = new WrapperWidget(leaf);

        // 1. Mount
        ElementNode elementNode = wrapper.createElement();
        assertNull(elementNode.getArcElement(), "arcElement should be null before mount");
        
        elementNode.mount(null);
        assertNotNull(elementNode.getArcElement(), "arcElement should be resolved after mount");
        assertEquals(1, state.mounts.get(), "Child widget should be mounted once");

        // 2. Update with compatible child widget
        LeafWidget leaf2 = new LeafWidget("world", state);
        WrapperWidget wrapper2 = new WrapperWidget(leaf2);
        elementNode.update(wrapper2);

        assertEquals(1, state.mounts.get(), "Child should not be remounted");
        assertEquals(1, state.updates.get(), "Child should be updated");
        assertEquals(0, state.disposes.get(), "Child should not be disposed");

        // 3. Dispose
        elementNode.dispose();
        assertEquals(1, state.disposes.get(), "Child should be disposed");
    }

    @Test
    void testStatelessWidgetRecreatesChildOnIncompatibleType() {
        TestState state1 = new TestState();
        LeafWidget leaf = new LeafWidget("hello", state1);
        WrapperWidget wrapper = new WrapperWidget(leaf);

        ElementNode elementNode = wrapper.createElement();
        elementNode.mount(null);

        // Update with incompatible widget type
        TestState state2 = new TestState();
        LeafWidget leaf2 = new LeafWidget("world", state2);
        Widget dummyIncompatible = new Widget() {
            @Override
            public ElementNode createElement() {
                return new ElementNode(this) {
                    { this.arcElement = new Element(); }
                    @Override public LayoutSpec sizing() { return LayoutSpec.defaultSpec(); }
                    @Override public void mount(ElementNode parent) { state2.mounts.incrementAndGet(); }
                };
            }
        };

        WrapperWidget wrapper2 = new WrapperWidget(dummyIncompatible);
        elementNode.update(wrapper2);

        assertEquals(1, state1.disposes.get(), "Old child should be disposed due to incompatibility");
        assertEquals(1, state2.mounts.get(), "New child should be mounted");
    }
}
