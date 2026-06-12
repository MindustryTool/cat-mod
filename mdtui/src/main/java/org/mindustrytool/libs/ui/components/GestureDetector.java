package org.mindustrytool.libs.ui.components;

import arc.scene.Element;
import arc.scene.event.ClickListener;
import arc.scene.event.ElementGestureListener;
import arc.scene.event.HandCursorListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.layout.WidgetGroup;
import lombok.Builder;
import org.mindustrytool.libs.ui.widget.ElementNode;
import org.mindustrytool.libs.ui.widget.Widget;
import org.mindustrytool.libs.ui.layout.LayoutSpec;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A declarative, immutable gesture detector widget that listens for touch, click, hover,
 * and key/scroll events on its child widget without adding visual overhead to the rendering graph.
 *
 * @param child       the child widget to wrap and listen to.
 * @param onTap       callback executed when the child is clicked/tapped.
 * @param onDoubleTap callback executed when the child is double tapped.
 * @param onLongPress callback executed when the child is pressed and held.
 * @param onHover     callback executed when mouse hovers over the child (passes true on enter, false on exit).
 * @param onPan       callback executed when drag/pan gestures are detected.
 * @param onScroll    callback executed when the mouse scroll wheel is rotated.
 */
@Builder(toBuilder = true)
public record GestureDetector(
    Widget child,
    Runnable onTap,
    Runnable onDoubleTap,
    Runnable onLongPress,
    Consumer<Boolean> onHover,
    BiConsumer<Float, Float> onPan,
    BiConsumer<Float, Float> onScroll
) implements Widget {

    /**
     * Lombok builder class helper that defines the default properties for a GestureDetector builder.
     */
    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    public static class GestureDetectorBuilder {
    }

    @Override
    public ElementNode createElement() {
        return new GestureElementNode(this);
    }
}

/**
 * Backing ElementNode that manages event listeners for a {@link GestureDetector}.
 */
class GestureElementNode extends ElementNode {

    private final EventGroup group = new EventGroup();
    private ElementNode childNode;

    private ClickListener clickListener;
    private HandCursorListener cursorListener;
    private InputListener hoverListener;
    private ElementGestureListener gestureListener;
    private InputListener scrollListener;

    /**
     * Constructs a gesture element node.
     *
     * @param widget the gesture detector blueprint.
     */
    GestureElementNode(GestureDetector widget) {
        super(widget);
        this.arcElement = group;
        group.userObject = this;
    }

    @Override
    public LayoutSpec sizing() {
        if (childNode == null) return LayoutSpec.defaultSpec();
        return childNode.sizing();
    }

    @Override
    public void mount(ElementNode parent) {
        GestureDetector w = (GestureDetector) widget;

        if (w.child() != null) {
            childNode = w.child().createElement();
            childNode.mount(this);
            group.setChild(childNode.getArcElement());
        }

        applyListeners();
    }

    @Override
    public void update(Widget newWidget) {
        super.update(newWidget);
        GestureDetector w = (GestureDetector) newWidget;

        if (w.child() != null) {
            if (childNode == null) {
                childNode = w.child().createElement();
                childNode.mount(this);
                group.setChild(childNode.getArcElement());
            } else if (childNode.getWidget().canUpdate(w.child())) {
                childNode.update(w.child());
            } else {
                childNode.dispose();
                childNode = w.child().createElement();
                childNode.mount(this);
                group.setChild(childNode.getArcElement());
            }
        } else if (childNode != null) {
            childNode.dispose();
            childNode = null;
            group.setChild(null);
        }

        applyListeners();
        group.invalidateHierarchy();
    }

    @Override
    public void dispose() {
        if (childNode != null) {
            childNode.dispose();
            childNode = null;
        }
        super.dispose();
    }

    private void applyListeners() {
        GestureDetector w = (GestureDetector) widget;

        if (clickListener != null) {
            group.removeListener(clickListener);
            clickListener = null;
        }

        if (cursorListener != null) {
            group.removeListener(cursorListener);
            cursorListener = null;
        }

        if (hoverListener != null) {
            group.removeListener(hoverListener);
            hoverListener = null;
        }

        if (gestureListener != null) {
            group.removeListener(gestureListener);
            gestureListener = null;
        }

        if (scrollListener != null) {
            group.removeListener(scrollListener);
            scrollListener = null;
        }

        if (w.onTap() != null || w.onDoubleTap() != null) {
            clickListener = new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (getTapCount() == 2 && w.onDoubleTap() != null) {
                        w.onDoubleTap().run();
                    } else if (getTapCount() == 1 && w.onTap() != null) {
                        w.onTap().run();
                    }
                }
            };
            group.addListener(clickListener);

            cursorListener = new HandCursorListener();
            group.addListener(cursorListener);
        }

        if (w.onHover() != null) {
            hoverListener = new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Element fromActor) {
                    w.onHover().accept(true);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Element toActor) {
                    w.onHover().accept(false);
                }
            };
            group.addListener(hoverListener);
        }

        if (w.onLongPress() != null || w.onPan() != null) {
            gestureListener = new ElementGestureListener() {
                @Override
                public boolean longPress(Element actor, float x, float y) {
                    if (w.onLongPress() != null) {
                        w.onLongPress().run();
                        return true;
                    }
                    return false;
                }

                @Override
                public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
                    if (w.onPan() != null) {
                        w.onPan().accept(deltaX, deltaY);
                    }
                }
            };
            group.addListener(gestureListener);
        }

        if (w.onScroll() != null) {
            scrollListener = new InputListener() {
                @Override
                public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                    w.onScroll().accept(amountX, amountY);
                    return true;
                }
            };
            group.addListener(scrollListener);
        }
    }
}

/**
 * A pass-through container group that delegates all layout sizing and bounds updates to its child,
 * allowing listeners to be attached at the group level.
 */
class EventGroup extends WidgetGroup {

    private Element child;

    EventGroup() {
        setTransform(false);
    }

    /**
     * Sets the active child element inside this event group.
     *
     * @param child the backing Arc UI element.
     */
    public void setChild(Element child) {
        if (this.child != null) {
            removeChild(this.child);
        }

        this.child = child;

        if (child != null) {
            addChild(child);
        }

        invalidateHierarchy();
    }

    @Override
    public float getPrefWidth() {
        return child != null ? child.getPrefWidth() : 0f;
    }

    @Override
    public float getPrefHeight() {
        return child != null ? child.getPrefHeight() : 0f;
    }

    @Override
    public float getMinWidth() {
        return child != null ? child.getMinWidth() : 0f;
    }

    @Override
    public float getMinHeight() {
        return child != null ? child.getMinHeight() : 0f;
    }

    @Override
    public void layout() {
        if (child != null) {
            child.setBounds(0f, 0f, getWidth(), getHeight());
        }
    }
}
