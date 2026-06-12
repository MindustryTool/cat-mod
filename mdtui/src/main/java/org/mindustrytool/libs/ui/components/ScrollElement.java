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
import lombok.Getter;
import lombok.Setter;

import static arc.Core.*;

/**
 * A direct port of Arc's ScrollPane source code with per-axis scroll configuration.
 * Extends WidgetGroup directly, behaves as a pure-property element. Each axis (X and Y)
 * can be independently configured for scrolling, overscroll, force scroll, and scrollbar visibility.
 */
public class ScrollElement extends WidgetGroup {

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
    @Getter boolean fadeScrollBars = false;
    @Getter @Setter boolean smoothScrolling = true;
    float overscrollDistance = 50f;
    float overscrollSpeedMin = 30f;
    float overscrollSpeedMax = 200f;
    boolean clamp = true;
    boolean variableSizeKnobs = true;
    @Getter boolean scrollbarsOnTop;
    @Setter boolean clip = true;

    /**
     * Creates a scroll element wrapping the given widget.
     * @param widget the child element to make scrollable, or null
     */
    public ScrollElement(Element widget) {
        setWidget(widget);
        setSize(150, 150);
        setTransform(true);
        initializeListeners();
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
                if (draggingPointer != -1) return false;
                if (pointer == 0 && button != KeyCode.mouseLeft) return false;
                requestScroll();

                if (!flickScroll) fade.reset();

                if (fade.alpha == 0) return false;

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
                if (pointer != draggingPointer) return;
                cancel();
            }

            @Override
            public void touchDragged(InputEvent event, float tx, float ty, int pointer) {
                if (pointer != draggingPointer) return;
                if (x.touchScroll) {
                    float delta = tx - lastPoint.x;
                    float scrollH = handlePosition + delta;
                    handlePosition = scrollH;
                    scrollH = Math.max(x.barBounds.x, scrollH);
                    scrollH = Math.min(x.barBounds.x + x.barBounds.width - x.knobBounds.width, scrollH);
                    float total = x.barBounds.width - x.knobBounds.width;
                    if (total != 0) setScrollPercentX((scrollH - x.barBounds.x) / total);
                    lastPoint.set(tx, ty);
                } else if (y.touchScroll) {
                    float delta = ty - lastPoint.y;
                    float scrollV = handlePosition + delta;
                    handlePosition = scrollV;
                    scrollV = Math.max(y.barBounds.y, scrollV);
                    scrollV = Math.min(y.barBounds.y + y.barBounds.height - y.knobBounds.height, scrollV);
                    float total = y.barBounds.height - y.knobBounds.height;
                    if (total != 0) setScrollPercentY(1 - ((scrollV - y.barBounds.y) / total));
                    lastPoint.set(tx, ty);
                }
            }

            @Override
            public boolean mouseMoved(InputEvent event, float tx, float ty) {
                if (!flickScroll) fade.reset();
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
                if (cancelTouchFocus && ((x.scrolling && deltaX != 0) || (y.scrolling && deltaY != 0))) cancelTouchFocus();
            }

            @Override
            public void fling(InputEvent event, float tx, float ty, KeyCode button) {
                if (Math.abs(tx) > 150 && x.scrolling) {
                    flingTimer = flingTime;
                    x.velocity = tx;
                    if (cancelTouchFocus) cancelTouchFocus();
                }
                if (Math.abs(ty) > 150 && y.scrolling) {
                    flingTimer = flingTime;
                    y.velocity = -ty;
                    if (cancelTouchFocus) cancelTouchFocus();
                }
            }

            @Override
            public boolean handle(SceneEvent event) {
                if (super.handle(event)) {
                    if (((InputEvent) event).type == InputEventType.touchDown) flingTimer = 0;
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
                if (y.scrolling) setScrollY(y.amount + getMouseWheelY() * sy);
                if (x.scrolling) setScrollX(x.amount + getMouseWheelX() * sx);
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

    // ==========================================
    // Public Setters & Getters (Properties)
    // ==========================================

    /**
     * Sets the widget to be made scrollable. Replaces any existing widget.
     * @param widget the child element to wrap, or null to clear
     * @throws IllegalArgumentException if widget is this ScrollElement
     */
    public void setWidget(Element widget) {
        if (widget == this) throw new IllegalArgumentException("widget cannot be the ScrollElement.");
        if (this.widget != null) super.removeChild(this.widget);
        this.widget = widget;
        if (widget != null) super.addChild(widget);
    }

    /**
     * Sets whether scroll bars fade out when not in use.
     * When enabled, fade delay and speed default to 0.5s and 2.0s respectively.
     * @param fadeScrollBars true to enable fade effect
     */
    public void setFadeScrollBars(boolean fadeScrollBars) {
        if (this.fadeScrollBars == fadeScrollBars) return;
        this.fadeScrollBars = fadeScrollBars;
        if (fadeScrollBars) {
            setupFadeScrollBars(0.5f, 2.0f);
        }
        invalidateHierarchy();
    }

    /**
     * Configures the delay and duration for scroll bar fading.
     * @param delay  seconds before fade starts after last interaction
     * @param seconds duration of the fade transition
     */
    public void setupFadeScrollBars(float delay, float seconds) {
        fade.delaySeconds = delay;
        fade.alphaSeconds = seconds;
    }

    /**
     * Sets whether scroll bars are drawn on top of the content rather than beside it.
     * Enabling this disables fade scroll bars if active.
     * @param scrollbarsOnTop true to draw scroll bars over content
     */
    public void setScrollbarsOnTop(boolean scrollbarsOnTop) {
        if (this.scrollbarsOnTop == scrollbarsOnTop) return;
        this.scrollbarsOnTop = scrollbarsOnTop;
        if (scrollbarsOnTop && fadeScrollBars) {
            setFadeScrollBars(false);
        }
        invalidateHierarchy();
    }

    /**
     * Sets whether flick/gesture scrolling is enabled.
     * When enabled, the user can drag to scroll and fling to continue momentum.
     * @param flickScroll true to enable gesture-based scrolling
     */
    public void setFlickScroll(boolean flickScroll) {
        if (this.flickScroll == flickScroll) return;
        this.flickScroll = flickScroll;
        if (flickScroll) {
            addListener(flickScrollListener);
        } else {
            removeListener(flickScrollListener);
        }
    }

    /**
     * Forces scroll bars to always be shown for the specified axes,
     * even when content does not exceed the viewport.
     * @param xForce true to force X scroll bar
     * @param yForce true to force Y scroll bar
     */
    public void setForceScroll(boolean xForce, boolean yForce) {
        x.forceScroll = xForce;
        y.forceScroll = yForce;
        invalidateHierarchy();
    }

    /**
     * Enables or disables overscroll (bounce-back effect) for both axes.
     * @param overscroll true to allow overscrolling past content bounds
     */
    public void setOverscroll(boolean overscroll) {
        x.overscroll = overscroll;
        y.overscroll = overscroll;
    }

    /**
     * Sets the X scroll position in pixels, clamped to valid range.
     * @param pixels the scroll offset along the X axis
     */
    public void setScrollX(float pixels) {
        x.amount = Mathf.clamp(pixels, 0, x.max);
    }

    /**
     * Sets the Y scroll position in pixels, clamped to valid range.
     * @param pixels the scroll offset along the Y axis
     */
    public void setScrollY(float pixels) {
        y.amount = Mathf.clamp(pixels, 0, y.max);
    }

    /**
     * Sets the X scroll position immediately, bypassing smooth scrolling interpolation.
     * @param pixels the absolute scroll offset along the X axis
     */
    public void setScrollXForce(float pixels) {
        x.visualAmount = pixels;
        x.amount = pixels;
    }

    /**
     * Sets the Y scroll position immediately, bypassing smooth scrolling interpolation.
     * @param pixels the absolute scroll offset along the Y axis
     */
    public void setScrollYForce(float pixels) {
        y.visualAmount = pixels;
        y.amount = pixels;
    }

    /**
     * Sets the X scroll position as a percentage of the total scroll range.
     * @param percentX scroll percentage from 0 to 1
     */
    public void setScrollPercentX(float percentX) {
        setScrollX(x.max * Mathf.clamp(percentX, 0, 1));
    }

    /**
     * Sets the Y scroll position as a percentage of the total scroll range.
     * @param percentY scroll percentage from 0 to 1
     */
    public void setScrollPercentY(float percentY) {
        setScrollY(y.max * Mathf.clamp(percentY, 0, 1));
    }

    /**
     * @return the current X scroll position as a percentage (0 to 1)
     */
    public float getScrollPercentX() {
        return safePercent(x.amount, x.max);
    }

    /**
     * @return the current Y scroll position as a percentage (0 to 1)
     */
    public float getScrollPercentY() {
        return safePercent(y.amount, y.max);
    }

    /**
     * @return the visual (animated) X scroll percentage (0 to 1)
     */
    public float getVisualScrollPercentX() {
        return safePercent(x.visualAmount, x.max);
    }

    /**
     * @return the visual (animated) Y scroll percentage (0 to 1)
     */
    public float getVisualScrollPercentY() {
        return safePercent(y.visualAmount, y.max);
    }

    // ==========================================
    // Arc Element Lifecycle Overrides
    // ==========================================

    /**
     * Advances the scroll element by one frame. Handles fade animation of scroll bars,
     * fling momentum, smooth scrolling interpolation, and overscroll bounce-back.
     * @param delta time in seconds since the last frame
     */
    @Override
    public void act(float delta) {
        super.act(delta);

        boolean panning = flickScrollListener.getGestureDetector().isPanning();
        boolean animating = false;

        if (fade.alpha > 0 && fadeScrollBars && !panning && !x.touchScroll && !y.touchScroll) {
            fade.delay -= delta;
            if (fade.delay <= 0) fade.alpha = Math.max(0, fade.alpha - delta);
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

        if (smoothScrolling && flingTimer <= 0 && !panning && !x.touchScroll && !y.touchScroll) {
            animating |= smoothApproach(x.visualAmount, x.amount, delta, val -> x.visualAmount = val);
            animating |= smoothApproach(y.visualAmount, y.amount, delta, val -> y.visualAmount = val);
        } else {
            if (x.visualAmount != x.amount) x.visualAmount = x.amount;
            if (y.visualAmount != y.amount) y.visualAmount = y.amount;
        }

        animating |= applyOverscroll(x, delta);
        animating |= applyOverscroll(y, delta);

        if (animating) {
            Scene stage = getScene();
            if (stage != null && stage.getActionsRequestRendering()) graphics.requestRendering();
        }
    }

    /**
     * Computes layout for the widget and scroll bars. Determines scroll ranges,
     * calculates bar and knob bounds, and sizes the widget to its preferred dimensions.
     */
    @Override
    public void layout() {
        float width = getWidth();
        float height = getHeight();

        float scrollbarHeight = scrollbarH();
        float scrollbarWidth = scrollbarW();

        x.areaSize = width;
        y.areaSize = height;

        if (widget == null) return;

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

    /**
     * Draws the scroll element: applies transform, positions the widget,
     * clips to the viewport area, and draws scroll bars with current fade alpha.
     */
    @Override
    public void draw() {
        if (widget == null) return;

        validate();

        applyTransform(computeTransform());

        if (x.scrolling)
            x.knobBounds.x = x.barBounds.x + (x.barBounds.width - x.knobBounds.width) * getVisualScrollPercentX();
        if (y.scrolling)
            y.knobBounds.y = y.barBounds.y + (y.barBounds.height - y.knobBounds.height) * (1 - getVisualScrollPercentY());

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
            if (x.bar != null)
                x.bar.draw(x.barBounds.x, x.barBounds.y, x.barBounds.width, x.barBounds.height);
            if (x.knob != null)
                x.knob.draw(x.knobBounds.x, x.knobBounds.y, x.knobBounds.width, x.knobBounds.height);
        }
        if (y.scrolling) {
            if (y.bar != null)
                y.bar.draw(y.barBounds.x, y.barBounds.y, y.barBounds.width, y.barBounds.height);
            if (y.knob != null)
                y.knob.draw(y.knobBounds.x, y.knobBounds.y, y.knobBounds.width, y.knobBounds.height);
        }

        resetTransform();
    }

    /**
     * @return the preferred width of the wrapped widget, or 0 if none
     */
    @Override
    public float getPrefWidth() {
        if (widget != null) {
            validate();
            return widget.getPrefWidth();
        }
        return 0;
    }

    /**
     * @return the preferred height of the wrapped widget, or 0 if none
     */
    @Override
    public float getPrefHeight() {
        if (widget != null) {
            validate();
            return widget.getPrefHeight();
        }
        return 0;
    }

    /**
     * @return minimum width, always 0 (scroll element can shrink to nothing)
     */
    @Override
    public float getMinWidth() {
        return 0;
    }

    /**
     * @return minimum height, always 0 (scroll element can shrink to nothing)
     */
    @Override
    public float getMinHeight() {
        return 0;
    }

    /**
     * Removes a child element. Delegates to the two-argument overload with unfocus=true.
     * @param actor the child to remove
     * @return true if the actor was removed
     */
    @Override
    public boolean removeChild(Element actor) {
        return removeChild(actor, true);
    }

    /**
     * Removes a child element. Only succeeds if the actor is the current widget.
     * @param actor  the child to remove
     * @param unfocus whether to unfocus the actor
     * @return true if the actor was removed
     */
    @Override
    public boolean removeChild(Element actor, boolean unfocus) {
        if (actor == null) throw new IllegalArgumentException("actor cannot be null.");
        if (actor != widget) return false;
        this.widget = null;
        return super.removeChild(actor, unfocus);
    }

    /**
     * Returns the topmost element at the given coordinates.
     * Scroll bar regions return this ScrollElement rather than the widget.
     * @param tx the X coordinate in the element's local space
     * @param ty the Y coordinate in the element's local space
     * @param touchable whether to only consider touchable elements
     * @return the hit element, or null if outside bounds
     */
    @Override
    public Element hit(float tx, float ty, boolean touchable) {
        if (tx < 0 || tx >= getWidth() || ty < 0 || ty >= getHeight()) return null;
        if ((x.scrolling && x.barBounds.contains(tx, ty)) || (y.scrolling && y.barBounds.contains(tx, ty))) return this;
        return super.hit(tx, ty, touchable);
    }

    /**
     * Cancels touch focus for all listeners except the flick scroll gesture listener.
     * Prevents other inputs from interfering with scrolling.
     */
    public void cancelTouchFocus() {
        Scene stage = getScene();
        if (stage != null) stage.cancelTouchFocusExcept(flickScrollListener, this);
    }

    /**
     * Cancels any active scroll drag or fling on all axes.
     * Resets dragging pointer and touch scroll state.
     */
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
        if (!clamp) return;
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

        if (isX) {
            float hHeight = scrollbarSize;
            float boundsX = y.scrollBarOnEdge ? 0 : scrollbarW();
            float boundsY = axis.scrollBarOnEdge ? 0 : parentHeight - hHeight;
            axis.barBounds.set(boundsX, boundsY, axis.areaSize - (y.scrolling ? scrollbarW() : 0), hHeight);

            axis.knobBounds.width = variableSizeKnobs ?
                Math.max(axis.knob.getMinWidth(), axis.barBounds.width * axis.areaSize / widgetSize) : axis.knob.getMinWidth();
            axis.knobBounds.height = hHeight;
            axis.knobBounds.x = axis.barBounds.x + (axis.barBounds.width - axis.knobBounds.width) * getScrollPercentX();
            axis.knobBounds.y = axis.barBounds.y;
        } else {
            float vWidth = scrollbarSize;
            float boundsX = axis.scrollBarOnEdge ? parentWidth - vWidth : 0;
            float boundsY = x.scrollBarOnEdge ? scrollbarH() : 0;
            axis.barBounds.set(boundsX, boundsY, vWidth, axis.areaSize - (x.scrolling ? scrollbarH() : 0));

            axis.knobBounds.width = vWidth;
            axis.knobBounds.height = variableSizeKnobs ?
                Math.max(axis.knob.getMinHeight(), axis.barBounds.height * axis.areaSize / widgetSize) : axis.knob.getMinHeight();
            axis.knobBounds.x = boundsX;
            axis.knobBounds.y = axis.barBounds.y + (axis.barBounds.height - axis.knobBounds.height) * (1 - getScrollPercentY());
        }
    }

    private boolean applyOverscroll(ScrollAxis axis, float delta) {
        if (!axis.overscroll || !axis.scrolling) return false;
        if (axis.amount < 0) {
            fade.reset();
            axis.amount += (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * -axis.amount / overscrollDistance) * delta;
            if (axis.amount > 0) axis.amount = 0;
            return true;
        } else if (axis.amount > axis.max) {
            fade.reset();
            axis.amount -= (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * -(axis.max - axis.amount) / overscrollDistance) * delta;
            if (axis.amount < axis.max) axis.amount = axis.max;
            return true;
        }
        return false;
    }

    private boolean smoothApproach(float visual, float target, float delta, java.util.function.Consumer<Float> setter) {
        if (visual == target) return false;
        float diff = target - visual;
        float step = Math.copySign(Math.max(200f * delta, Math.abs(diff) * 7f * delta), diff);
        setter.accept(Mathf.clamp(visual + step, Math.min(visual, target), Math.max(visual, target)));
        return true;
    }

    protected float getMouseWheelX() {
        return Math.min(x.areaSize, Math.max(x.areaSize * 0.9f, x.max * 0.1f) / 4);
    }

    protected float getMouseWheelY() {
        return Math.min(y.areaSize, Math.max(y.areaSize * 0.9f, y.max * 0.1f) / 4);
    }

    private static float safePercent(float amount, float max) {
        return max == 0 ? 1f : Mathf.clamp(amount / max, 0, 1);
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
