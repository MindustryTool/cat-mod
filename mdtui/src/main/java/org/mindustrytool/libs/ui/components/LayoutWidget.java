package org.mindustrytool.libs.ui.components;

import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.event.InputEvent.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.WidgetGroup;
import arc.scene.utils.*;
import arc.struct.Seq;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import org.mindustrytool.libs.ui.widget.ElementNode;
import org.mindustrytool.libs.ui.widget.Widget;
import org.mindustrytool.libs.ui.layout.LayoutEngine;
import org.mindustrytool.libs.ui.layout.LayoutSpec;

import java.util.HashMap;
import java.util.HashSet;

import static arc.Core.*;

/**
 * A declarative, immutable layout widget that organizes, lays out, and optionally
 * scrolls its list of child widgets using flexbox-like flow arrangement.
 *
 * @param layoutSpec      the layout spec rules and sizing constraints.
 * @param scrollX         true if horizontal scrolling is enabled.
 * @param scrollY         true if vertical scrolling is enabled.
 * @param fadeScrollBars  true if scrollbars should fade when inactive.
 * @param smoothScrolling true if scroll adjustments should use smooth interpolation.
 * @param clip            true if children drawing should be clipped to the layout bounds.
 * @param background      an optional background Widget.
 * @param children        the sequence of child Widget configurations.
 * @param onClick         an optional runnable listener executed when clicked.
 */
@Builder(toBuilder = true)
public record LayoutWidget(
    LayoutSpec layoutSpec,
    boolean scrollX,
    boolean scrollY,
    boolean fadeScrollBars,
    boolean smoothScrolling,
    boolean clip,
    Widget background,
    Seq<Widget> children,
    Runnable onClick
) implements Widget {

    /**
     * Lombok builder class helper that defines the default properties for a LayoutWidget builder.
     */
    public static class LayoutWidgetBuilder {
        private LayoutSpec layoutSpec = LayoutSpec.defaultSpec();
        private boolean scrollX = false;
        private boolean scrollY = false;
        private boolean fadeScrollBars = true;
        private boolean smoothScrolling = true;
        private boolean clip = false;
        private Seq<Widget> children = new Seq<>();

        /**
         * Adds a single child widget to this layout.
         *
         * @param child the child widget to add.
         * @return this builder instance.
         */
        public LayoutWidgetBuilder child(Widget child) {
            if (this.children == null) {
                this.children = new Seq<>();
            }
            this.children.add(child);
            return this;
        }

        /**
         * Adds multiple child widgets to this layout.
         *
         * @param children the child widgets to add.
         * @return this builder instance.
         */
        public LayoutWidgetBuilder children(Widget... children) {
            if (this.children == null) {
                this.children = new Seq<>();
            }
            this.children.addAll(children);
            return this;
        }

        /**
         * Adds an iterable sequence of child widgets to this layout.
         *
         * @param children the collection of child widgets to add.
         * @return this builder instance.
         */
        public LayoutWidgetBuilder children(Iterable<? extends Widget> children) {
            if (this.children == null) {
                this.children = new Seq<>();
            }
            for (Widget child : children) {
                this.children.add(child);
            }
            return this;
        }
    }

    /**
     * Default constructor creating a default LayoutWidget.
     */
    public LayoutWidget() {
        this(LayoutSpec.defaultSpec(), false, false, true, true, false, null, new Seq<>(), null);
    }

    @Override
    public ElementNode createElement() {
        return new LayoutElementNode(this);
    }
}

/**
 * Backing ElementNode that integrates layout engine calculations and child reconciliation.
 */
class LayoutElementNode extends ElementNode {

    private final ScrollElement group;
    private final WidgetGroup contentGroup;
    private ElementNode backgroundNode;
    private final Seq<EventListener> eventListeners = new Seq<>();
    private Seq<ElementNode> children = new Seq<>();

    LayoutElementNode(LayoutWidget widget) {
        super(widget);

        contentGroup = new WidgetGroup() {
            {
                setTransform(true);
                userObject = LayoutElementNode.this;
            }

            @Override
            public float getPrefWidth() {
                LayoutSpec spec = sizing();
                
                if (spec.getWidthMode() == LayoutSpec.SizeMode.FIXED && spec.getFixedWidth() > 0f) {
                    return spec.constrainWidth(spec.getFixedWidth());
                }
                
                if (spec.getWidthMode() == LayoutSpec.SizeMode.GROW) {
                    return 0f;
                }
                
                return spec.constrainWidth(
                    LayoutEngine.prefWidth(spec, spec.isColumn(), spec.getGap(), foregroundElements()));
            }

            @Override
            public float getPrefHeight() {
                LayoutSpec spec = sizing();
                
                if (spec.getHeightMode() == LayoutSpec.SizeMode.FIXED && spec.getFixedHeight() > 0f) {
                    return spec.constrainHeight(spec.getFixedHeight());
                }
                
                if (spec.getHeightMode() == LayoutSpec.SizeMode.GROW) {
                    return 0f;
                }
                
                return spec.constrainHeight(
                    LayoutEngine.prefHeight(spec, spec.isColumn(), spec.getGap(), foregroundElements()));
            }

            @Override
            public void layout() {
                LayoutSpec spec = sizing();
                float cw = getWidth(), ch = getHeight();
                
                if (backgroundNode != null) {
                    backgroundNode.getArcElement().setSize(cw, ch);
                    backgroundNode.getArcElement().setPosition(0f, 0f);
                }
                
                float lw = Math.max(0f, cw - spec.getHorizontalPadding());
                float lh = Math.max(0f, ch - spec.getVerticalPadding());
                
                LayoutEngine.layout(spec, foregroundElements(),
                    spec.getPaddingLeft(), spec.getPaddingBottom(), lw, lh);
            }
        };

        group = new ScrollElement(contentGroup) {
            {
                userObject = LayoutElementNode.this;
            }

            @Override
            protected void setScene(Scene scene) {
                boolean hadScene = getScene() != null;
                super.setScene(scene);
                
                if (hadScene && scene == null) {
                    LayoutElementNode.this.dispose();
                }
            }
        };

        group.getX().setDisabled(true);
        group.getY().setDisabled(true);

        arcElement = group;
    }

    @Override
    public void mount(ElementNode parent) {
        LayoutWidget w = (LayoutWidget) widget;
        
        group.applyLayoutConfig(w);
        mountBackground(w);
        reconcile(w.children());
        applyListeners(w);
    }

    @Override
    public void update(Widget newWidget) {
        super.update(newWidget);
        LayoutWidget w = (LayoutWidget) newWidget;
        
        group.applyLayoutConfig(w);
        updateBackground(w);
        reconcile(w.children());
        applyListeners(w);
        
        contentGroup.invalidateHierarchy();
    }

    @Override
    public void dispose() {
        if (backgroundNode != null) {
            backgroundNode.dispose();
            backgroundNode = null;
        }
        
        for (int i = children.size - 1; i >= 0; i--) {
            children.get(i).dispose();
        }
        
        children.clear();
        super.dispose();
    }

    private void reconcile(Seq<? extends Widget> newWidgets) {
        int oldLen = children.size;
        int newLen = newWidgets.size;

        HashMap<Object, ElementNode> keyed = new HashMap<>();
        for (int i = 0; i < oldLen; i++) {
            Object k = children.get(i).getWidget().key();
            if (k != null) {
                keyed.put(k, children.get(i));
            }
        }

        HashSet<ElementNode> consumed = new HashSet<>();
        Seq<ElementNode> result = new Seq<>();

        for (int i = 0; i < newLen; i++) {
            Widget w = newWidgets.get(i);
            ElementNode match = null;

            if (w.key() != null) {
                match = keyed.get(w.key());
                if (match != null && !match.getWidget().canUpdate(w)) {
                    match.dispose();
                    keyed.remove(w.key());
                    match = null;
                }
                if (match != null) {
                    keyed.remove(w.key());
                }
            }

            if (match == null && i < oldLen) {
                ElementNode candidate = children.get(i);
                if (!consumed.contains(candidate) && candidate.getWidget().canUpdate(w)) {
                    match = candidate;
                }
            }

            if (match != null) {
                match.update(w);
                result.add(match);
                consumed.add(match);
            } else {
                ElementNode n = w.createElement();
                n.mount(this);
                contentGroup.addChild(n.getArcElement());
                result.add(n);
            }
        }

        for (int i = oldLen - 1; i >= 0; i--) {
            if (!consumed.contains(children.get(i))) {
                children.get(i).dispose();
            }
        }

        children = result;
    }

    private void mountBackground(LayoutWidget w) {
        if (w.background() == null) {
            return;
        }
        
        backgroundNode = w.background().createElement();
        backgroundNode.mount(this);
        backgroundNode.getArcElement().toBack();
    }

    private void updateBackground(LayoutWidget w) {
        if (w.background() != null) {
            if (backgroundNode == null) {
                backgroundNode = w.background().createElement();
                backgroundNode.mount(this);
                backgroundNode.getArcElement().toBack();
            } else if (backgroundNode.getWidget().canUpdate(w.background())) {
                backgroundNode.update(w.background());
            } else {
                backgroundNode.dispose();
                backgroundNode = w.background().createElement();
                backgroundNode.mount(this);
                backgroundNode.getArcElement().toBack();
            }
        } else if (backgroundNode != null) {
            backgroundNode.dispose();
            backgroundNode = null;
        }
    }

    private Seq<Element> foregroundElements() {
        Seq<Element> result = new Seq<>();
        for (int i = 0; i < children.size; i++) {
            result.add(children.get(i).getArcElement());
        }
        return result;
    }

    private void applyListeners(LayoutWidget w) {
        for (EventListener l : eventListeners) {
            group.removeListener(l);
        }
        eventListeners.clear();
        
        if (w.onClick() != null) {
            ClickListener cl = new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    w.onClick().run();
                }
            };
            group.addListener(cl);
            eventListeners.add(cl);
        }
    }

    @Override
    public LayoutSpec sizing() {
        return ((LayoutWidget) widget).layoutSpec();
    }
}

/**
 * Custom scroll host group supporting flick scrolling, overscrolling, momentum flings, and fade configurations.
 */
class ScrollElement extends WidgetGroup {

    /**
     * Scroll axis details tracking bounds, knob state, and scroll progress amount.
     */
    @Getter @Setter
    public static class ScrollAxis {
        boolean disabled, forceScroll, scrolling, overscroll = true;
        boolean scrollBarOnEdge = true;
        float amount, visualAmount, max, velocity, areaSize;
        boolean touchScroll;
        Drawable bar = new RectDrawable(new arc.graphics.Color(0.2f, 0.2f, 0.2f, 0.2f), 10f, 10f);
        Drawable knob = new RectDrawable(new arc.graphics.Color(0.5f, 0.5f, 0.5f, 0.5f), 10f, 10f);
        final Rect barBounds = new Rect();
        final Rect knobBounds = new Rect();
    }

    /**
     * Fading configuration governing transparency decay.
     */
    static class FadeConfig {
        float alpha = 1f, alphaSeconds = 1f;
        float delay, delaySeconds = 1f;

        void reset() {
            alpha = alphaSeconds;
            delay = delaySeconds;
        }
    }

    @Getter final ScrollAxis x = new ScrollAxis();
    @Getter final ScrollAxis y = new ScrollAxis();
    @Getter final FadeConfig fade = new FadeConfig();

    final Vec2 lastPoint = new Vec2();
    private final Rect widgetAreaBounds = new Rect();
    private final Rect widgetCullingArea = new Rect();
    private final Rect scissorBounds = new Rect();

    boolean cancelTouchFocus = true;
    @Getter boolean flickScroll = true;
    float flingTimer;
    float flingTime = 1f;
    int draggingPointer = -1;

    @Getter Element widget;
    private ElementGestureListener flickScrollListener;
    float overscrollDistance = 50f;
    float overscrollSpeedMin = 30f;
    float overscrollSpeedMax = 200f;
    boolean clamp = true;
    boolean variableSizeKnobs = true;
    @Getter boolean scrollbarsOnTop;

    ScrollElement(Element widget) {
        setWidget(widget);
        setSize(150, 150);
        setTransform(true);
        initializeListeners();
    }

    void applyLayoutConfig(LayoutWidget w) {
        fade.delaySeconds = 0.5f;
        fade.alphaSeconds = 2.0f;
        getX().setDisabled(!w.scrollX());
        getY().setDisabled(!w.scrollY());
        
        invalidateHierarchy();
    }

    private void initializeListeners() {
        addCaptureListener(new InputListener() {
            private float handlePosition;

            @Override
            public void enter(InputEvent event, float tx, float ty, int pointer, Element fromActor) {
                requestScroll();
            }

            @Override
            public boolean touchDown(InputEvent event, float tx, float ty, int pointer, KeyCode button) {
                if (draggingPointer != -1) {
                    return false;
                }
                if (pointer == 0 && button != KeyCode.mouseLeft) {
                    return false;
                }
                
                requestScroll();

                if (!flickScroll) {
                    fade.reset();
                }

                if (fade.alpha == 0) {
                    return false;
                }

                if (x.scrolling && x.barBounds.contains(tx, ty)) {
                    event.stop();
                    fade.reset();
                    if (x.knobBounds.contains(tx, ty)) {
                        lastPoint.set(tx, ty);
                        handlePosition = x.knobBounds.x;
                        x.touchScroll = true;
                        draggingPointer = pointer;
                        return true;
                    }
                    setScrollX(x.amount + x.areaSize * (tx < x.knobBounds.x ? -1 : 1));
                    return true;
                }
                
                if (y.scrolling && y.barBounds.contains(tx, ty)) {
                    event.stop();
                    fade.reset();
                    if (y.knobBounds.contains(tx, ty)) {
                        lastPoint.set(tx, ty);
                        handlePosition = y.knobBounds.y;
                        y.touchScroll = true;
                        draggingPointer = pointer;
                        return true;
                    }
                    setScrollY(y.amount + y.areaSize * (ty < y.knobBounds.y ? 1 : -1));
                    return true;
                }
                
                return false;
            }

            @Override
            public void touchUp(InputEvent event, float tx, float ty, int pointer, KeyCode button) {
                if (pointer != draggingPointer) {
                    return;
                }
                cancel();
            }

            @Override
            public void touchDragged(InputEvent event, float tx, float ty, int pointer) {
                if (pointer != draggingPointer) {
                    return;
                }
                
                if (x.touchScroll) {
                    float delta = tx - lastPoint.x;
                    float scrollH = handlePosition + delta;
                    handlePosition = scrollH;
                    scrollH = Math.max(x.barBounds.x, scrollH);
                    scrollH = Math.min(x.barBounds.x + x.barBounds.width - x.knobBounds.width, scrollH);
                    float total = x.barBounds.width - x.knobBounds.width;
                    
                    if (total != 0) {
                        setScrollPercentX((scrollH - x.barBounds.x) / total);
                    }
                    lastPoint.set(tx, ty);
                } else if (y.touchScroll) {
                    float delta = ty - lastPoint.y;
                    float scrollV = handlePosition + delta;
                    handlePosition = scrollV;
                    scrollV = Math.max(y.barBounds.y, scrollV);
                    scrollV = Math.min(y.barBounds.y + y.barBounds.height - y.knobBounds.height, scrollV);
                    float total = y.barBounds.height - y.knobBounds.height;
                    
                    if (total != 0) {
                        setScrollPercentY(1 - ((scrollV - y.barBounds.y) / total));
                    }
                    lastPoint.set(tx, ty);
                }
            }

            @Override
            public boolean mouseMoved(InputEvent event, float tx, float ty) {
                if (!flickScroll) {
                    fade.reset();
                }
                requestScroll();
                return false;
            }
        });

        flickScrollListener = new ElementGestureListener() {
            @Override
            public void pan(InputEvent event, float tx, float ty, float deltaX, float deltaY) {
                fade.reset();
                x.amount -= deltaX;
                y.amount += deltaY;
                clamp();
                
                if (cancelTouchFocus && ((x.scrolling && deltaX != 0) || (y.scrolling && deltaY != 0))) {
                    cancelTouchFocus();
                }
            }

            @Override
            public void fling(InputEvent event, float tx, float ty, KeyCode button) {
                if (Math.abs(tx) > 150 && x.scrolling) {
                    flingTimer = flingTime;
                    x.velocity = tx;
                    if (cancelTouchFocus) {
                        cancelTouchFocus();
                    }
                }
                
                if (Math.abs(ty) > 150 && y.scrolling) {
                    flingTimer = flingTime;
                    y.velocity = -ty;
                    if (cancelTouchFocus) {
                        cancelTouchFocus();
                    }
                }
            }

            @Override
            public boolean handle(SceneEvent event) {
                if (super.handle(event)) {
                    if (((InputEvent) event).type == InputEventType.touchDown) {
                        flingTimer = 0;
                    }
                    return true;
                }
                return false;
            }
        };
        addListener(flickScrollListener);

        addListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float tx, float ty, float sx, float sy) {
                fade.reset();
                
                if (y.scrolling) {
                    setScrollY(y.amount + getMouseWheelY() * sy);
                }
                if (x.scrolling) {
                    setScrollX(x.amount + getMouseWheelX() * sx);
                }
                
                return x.scrolling || y.scrolling;
            }
        });

        addCaptureListener(new InputListener() {
            boolean on = false;

            @Override
            public boolean touchDown(InputEvent event, float tx, float ty, int pointer, KeyCode button) {
                Element actor = ScrollElement.this.hit(tx, ty, true);
                on = flickScroll;
                
                if ((actor instanceof Slider || actor instanceof TextField) && on) {
                    ScrollElement.this.setFlickScroll(false);
                    return true;
                }

                return super.touchDown(event, tx, ty, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float tx, float ty, int pointer, KeyCode button) {
                if (on) {
                    ScrollElement.this.setFlickScroll(true);
                }
                super.touchUp(event, tx, ty, pointer, button);
            }
        });
    }

    public void setWidget(Element widget) {
        if (widget == this) {
            throw new IllegalArgumentException("widget cannot be the ScrollElement.");
        }
        if (this.widget != null) {
            super.removeChild(this.widget);
        }
        
        this.widget = widget;
        if (widget != null) {
            super.addChild(widget);
        }
    }

    void setupFadeScrollBars(float delay, float seconds) {
        fade.delaySeconds = delay;
        fade.alphaSeconds = seconds;
    }

    public void setScrollbarsOnTop(boolean scrollbarsOnTop) {
        if (this.scrollbarsOnTop == scrollbarsOnTop) {
            return;
        }
        this.scrollbarsOnTop = scrollbarsOnTop;
        invalidateHierarchy();
    }

    public void setFlickScroll(boolean flickScroll) {
        if (this.flickScroll == flickScroll) {
            return;
        }
        this.flickScroll = flickScroll;
        
        if (flickScroll) {
            addListener(flickScrollListener);
        } else {
            removeListener(flickScrollListener);
        }
    }

    public void setForceScroll(boolean xForce, boolean yForce) {
        x.forceScroll = xForce;
        y.forceScroll = yForce;
        invalidateHierarchy();
    }

    public void setOverscroll(boolean overscroll) {
        x.overscroll = overscroll;
        y.overscroll = overscroll;
    }

    public void setScrollX(float pixels) {
        x.amount = Mathf.clamp(pixels, 0, x.max);
    }

    public void setScrollY(float pixels) {
        y.amount = Mathf.clamp(pixels, 0, y.max);
    }

    public void setScrollXForce(float pixels) {
        x.visualAmount = pixels;
        x.amount = pixels;
    }

    public void setScrollYForce(float pixels) {
        y.visualAmount = pixels;
        y.amount = pixels;
    }

    public void setScrollPercentX(float percentX) {
        setScrollX(x.max * Mathf.clamp(percentX, 0, 1));
    }

    public void setScrollPercentY(float percentY) {
        setScrollY(y.max * Mathf.clamp(percentY, 0, 1));
    }

    public float getScrollPercentX() {
        return safePercent(x.amount, x.max);
    }

    public float getScrollPercentY() {
        return safePercent(y.amount, y.max);
    }

    public float getVisualScrollPercentX() {
        return safePercent(x.visualAmount, x.max);
    }

    public float getVisualScrollPercentY() {
        return safePercent(y.visualAmount, y.max);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        boolean panning = flickScrollListener.getGestureDetector().isPanning();
        boolean animating = false;

        Object uo = userObject;
        boolean fadeSB = uo instanceof LayoutElementNode && ((LayoutWidget)((LayoutElementNode) uo).getWidget()).fadeScrollBars();
        
        if (fade.alpha > 0 && !panning && !x.touchScroll && !y.touchScroll) {
            fade.delay -= delta;
            if (fade.delay <= 0) {
                fade.alpha = Math.max(0, fade.alpha - delta);
            }
            animating = true;
        }

        if (flingTimer > 0) {
            fade.reset();

            float alpha = flingTimer / flingTime;
            updateFlingAxis(x, delta, alpha);
            updateFlingAxis(y, delta, alpha);
            clamp();

            flingTimer -= delta;
            if (flingTimer <= 0) {
                x.velocity = 0;
                y.velocity = 0;
            }

            animating = true;
        }

        boolean smoothScrolling = uo instanceof LayoutElementNode && ((LayoutWidget)((LayoutElementNode) uo).getWidget()).smoothScrolling();
        if (smoothScrolling && flingTimer <= 0 && !panning && !x.touchScroll && !y.touchScroll) {
            animating |= smoothApproach(x.visualAmount, x.amount, delta, val -> x.visualAmount = val);
            animating |= smoothApproach(y.visualAmount, y.amount, delta, val -> y.visualAmount = val);
        } else {
            if (x.visualAmount != x.amount) {
                x.visualAmount = x.amount;
            }
            if (y.visualAmount != y.amount) {
                y.visualAmount = y.amount;
            }
        }

        animating |= applyOverscroll(x, delta);
        animating |= applyOverscroll(y, delta);

        if (animating) {
            Scene stage = getScene();
            if (stage != null && stage.getActionsRequestRendering()) {
                graphics.requestRendering();
            }
        }
    }

    @Override
    public void layout() {
        float width = getWidth();
        float height = getHeight();

        float scrollbarHeight = scrollbarH();
        float scrollbarWidth = scrollbarW();

        x.areaSize = width;
        y.areaSize = height;

        if (widget == null) {
            return;
        }

        float widgetWidth = widget.getPrefWidth();
        float widgetHeight = widget.getPrefHeight();

        x.scrolling = x.forceScroll || (widgetWidth > x.areaSize && !x.disabled);
        y.scrolling = y.forceScroll || (widgetHeight > y.areaSize && !y.disabled);

        widgetAreaBounds.set(0, 0, x.areaSize, y.areaSize);

        widgetWidth = x.disabled ? x.areaSize : Math.max(x.areaSize, widgetWidth);
        widgetHeight = y.disabled ? y.areaSize : Math.max(y.areaSize, widgetHeight);

        x.max = widgetWidth - x.areaSize;
        y.max = widgetHeight - y.areaSize;

        setScrollX(x.amount);

        calculateBounds(x, true, width, height, widgetWidth, scrollbarWidth);
        calculateBounds(y, false, width, height, widgetHeight, scrollbarHeight);

        widget.setSize(widgetWidth, widgetHeight);
        widget.validate();
    }

    @Override
    public void draw() {
        if (widget == null) {
            return;
        }

        validate();
        applyTransform(computeTransform());

        if (x.scrolling) {
            x.knobBounds.x = x.barBounds.x + (x.barBounds.width - x.knobBounds.width) * getVisualScrollPercentX();
        }
        if (y.scrolling) {
            y.knobBounds.y = y.barBounds.y + (y.barBounds.height - y.knobBounds.height) * (1 - getVisualScrollPercentY());
        }

        float yOffset = y.scrolling ? y.visualAmount : 0;
        float xOffset = x.scrolling ? x.visualAmount : 0;

        widget.setPosition(widgetAreaBounds.x - xOffset, widgetAreaBounds.y - y.max + yOffset);

        if (widget instanceof Cullable) {
            widgetCullingArea.x = -widget.x + widgetAreaBounds.x;
            widgetCullingArea.y = -widget.y + widgetAreaBounds.y;
            widgetCullingArea.width = widgetAreaBounds.width;
            widgetCullingArea.height = widgetAreaBounds.height;
            ((Cullable) widget).setCullingArea(widgetCullingArea);
        }

        scene.calculateScissors(widgetAreaBounds, scissorBounds);

        Object uo = userObject;
        boolean clip = uo instanceof LayoutElementNode && ((LayoutWidget)((LayoutElementNode) uo).getWidget()).clip();
        
        if (clip) {
            if (ScissorStack.push(scissorBounds)) {
                drawChildren();
                ScissorStack.pop();
            }
        } else {
            drawChildren();
        }

        Draw.color(color.r, color.g, color.b, color.a * parentAlpha * Interp.fade.apply(fade.alpha / fade.alphaSeconds));

        if (x.scrolling) {
            if (x.bar != null) {
                x.bar.draw(x.barBounds.x, x.barBounds.y, x.barBounds.width, x.barBounds.height);
            }
            if (x.knob != null) {
                x.knob.draw(x.knobBounds.x, x.knobBounds.y, x.knobBounds.width, x.knobBounds.height);
            }
        }
        
        if (y.scrolling) {
            if (y.bar != null) {
                y.bar.draw(y.barBounds.x, y.barBounds.y, y.barBounds.width, y.barBounds.height);
            }
            if (y.knob != null) {
                y.knob.draw(y.knobBounds.x, y.knobBounds.y, y.knobBounds.width, y.knobBounds.height);
            }
        }

        resetTransform();
    }

    @Override
    public float getPrefWidth() {
        if (widget != null) {
            validate();
            return widget.getPrefWidth();
        }
        return 0;
    }

    @Override
    public float getPrefHeight() {
        if (widget != null) {
            validate();
            return widget.getPrefHeight();
        }
        return 0;
    }

    @Override
    public float getMinWidth() {
        return 0;
    }

    @Override
    public float getMinHeight() {
        return 0;
    }

    @Override
    public boolean removeChild(Element actor) {
        return removeChild(actor, true);
    }

    @Override
    public boolean removeChild(Element actor, boolean unfocus) {
        if (actor == null) {
            throw new IllegalArgumentException("actor cannot be null.");
        }
        
        if (actor != widget) {
            return false;
        }
        
        this.widget = null;
        return super.removeChild(actor, unfocus);
    }

    @Override
    public Element hit(float tx, float ty, boolean touchable) {
        if (tx < 0 || tx >= getWidth() || ty < 0 || ty >= getHeight()) {
            return null;
        }
        
        if ((x.scrolling && x.barBounds.contains(tx, ty)) || (y.scrolling && y.barBounds.contains(tx, ty))) {
            return this;
        }
        
        return super.hit(tx, ty, touchable);
    }

    public void cancelTouchFocus() {
        Scene stage = getScene();
        if (stage != null) {
            stage.cancelTouchFocusExcept(flickScrollListener, this);
        }
    }

    public void cancel() {
        draggingPointer = -1;
        x.touchScroll = false;
        y.touchScroll = false;
        flickScrollListener.getGestureDetector().cancel();
    }

    private void clampAxis(boolean hasOverscroll, float amount, float max, java.util.function.Consumer<Float> setter) {
        float lo = hasOverscroll ? -overscrollDistance : 0;
        float hi = hasOverscroll ? max + overscrollDistance : max;
        setter.accept(Mathf.clamp(amount, lo, hi));
    }

    void clamp() {
        if (!clamp) {
            return;
        }
        clampAxis(x.overscroll, x.amount, x.max, val -> x.amount = val);
        clampAxis(y.overscroll, y.amount, y.max, val -> y.amount = val);
    }

    private float scrollbarH() {
        float h = x.knob != null ? x.knob.getMinHeight() : 0;
        return (x.bar != null ? Math.max(h, x.bar.getMinHeight()) : h) / 2f;
    }

    private float scrollbarW() {
        float w = y.knob != null ? y.knob.getMinWidth() : 0;
        return (y.bar != null ? Math.max(w, y.bar.getMinWidth()) : w) / 2f;
    }

    private void updateFlingAxis(ScrollAxis axis, float delta, float alpha) {
        axis.amount -= axis.velocity * alpha * delta;
        if (axis.amount <= -overscrollDistance || axis.amount >= axis.max + overscrollDistance) {
            axis.velocity = 0;
        }
    }

    private void calculateBounds(ScrollAxis axis, boolean isX, float parentWidth, float parentHeight, float widgetSize, float scrollbarSize) {
        if (!axis.scrolling || axis.knob == null) {
            axis.barBounds.set(0, 0, 0, 0);
            axis.knobBounds.set(0, 0, 0, 0);
            return;
        }

        float areaWidth = parentWidth;
        float areaHeight = parentHeight;

        if (isX) {
            float barHeight = scrollbarSize;
            axis.barBounds.set(0, 0, areaWidth, barHeight);
            float knobWidth = areaWidth * (areaWidth / widgetSize);
            
            if (variableSizeKnobs) {
                knobWidth = Math.max(axis.knob.getMinWidth(), knobWidth);
            } else {
                knobWidth = axis.knob.getMinWidth();
            }
            
            knobWidth = Math.min(areaWidth, knobWidth);
            axis.knobBounds.set(0, 0, knobWidth, barHeight);
        } else {
            float barWidth = scrollbarSize;
            axis.barBounds.set(areaWidth - barWidth, 0, barWidth, areaHeight);
            float knobHeight = areaHeight * (areaHeight / widgetSize);
            
            if (variableSizeKnobs) {
                knobHeight = Math.max(axis.knob.getMinHeight(), knobHeight);
            } else {
                knobHeight = axis.knob.getMinHeight();
            }
            
            knobHeight = Math.min(areaHeight, knobHeight);
            axis.knobBounds.set(areaWidth - barWidth, 0, barWidth, knobHeight);
        }
    }

    private float safePercent(float val, float max) {
        if (max == 0) {
            return 0;
        }
        return Mathf.clamp(val / max, 0, 1);
    }

    private boolean applyOverscroll(ScrollAxis axis, float delta) {
        if (!axis.overscroll || !axis.scrolling) {
            return false;
        }
        
        if (axis.amount < 0) {
            axis.amount += (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * (-axis.amount / overscrollDistance)) * delta;
            
            if (axis.amount > 0) {
                axis.amount = 0;
            }
            return true;
        } else if (axis.amount > axis.max) {
            axis.amount -= (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * ((axis.amount - axis.max) / overscrollDistance)) * delta;
            
            if (axis.amount < axis.max) {
                axis.amount = axis.max;
            }
            return true;
        }
        
        return false;
    }

    private boolean smoothApproach(float visual, float target, float delta, java.util.function.Consumer<Float> setter) {
        if (visual == target) {
            return false;
        }
        
        float diff = target - visual;
        if (Math.abs(diff) < 0.1f) {
            setter.accept(target);
            return true;
        }
        
        setter.accept(visual + diff * (1 - (float) Math.exp(-15 * delta)));
        return true;
    }

    protected float getMouseWheelX() {
        return Math.min(x.areaSize, Math.max(x.areaSize * 0.9f, x.max * 0.1f) / 4);
    }

    protected float getMouseWheelY() {
        return Math.min(y.areaSize, Math.max(y.areaSize * 0.9f, y.max * 0.1f) / 4);
    }

    private static class RectDrawable extends BaseDrawable {
        private final arc.graphics.Color color;

        public RectDrawable(arc.graphics.Color color, float minWidth, float minHeight) {
            this.color = color;
            setMinWidth(minWidth);
            setMinHeight(minHeight);
        }

        @Override
        public void draw(float x, float y, float width, float height) {
            Draw.color(color);
            Fill.rect(x + width / 2f, y + height / 2f, width, height);
        }
    }
}
