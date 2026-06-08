package com.neko.libs.ui.style;

import arc.graphics.Color;
import arc.math.Interp;
import com.neko.libs.ui.style.StyleSpec.Align.Items;
import com.neko.libs.ui.style.StyleSpec.Align.Justify;
import com.neko.libs.ui.style.Theme;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StyleSpec {

    // ── Reactive State ────────────────────────────────────────────────────

    public static final class State<T> {
        private final List<Runnable> onChangeListeners = new ArrayList<>(1);

        public State() {}
        public State(T value) { this.value = value; }

        @Getter
        private T value;

        public void setValue(T newValue)   { this.value = newValue; notifyChange(); }
        public void onChange(Runnable r)   { onChangeListeners.add(r); }
        private void notifyChange() {
            for (Runnable r : onChangeListeners) r.run();
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Core — always present, always read by layout engine
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    public boolean isColumn = true;
    public State<Boolean> isColumnState;
    public boolean isColumn() { return isColumnState != null ? isColumnState.getValue() : isColumn; }

    public float gap = 0f;
    public State<Float> gapState;
    public float gap() { return gapState != null ? gapState.getValue() : gap; }

    public boolean hidden = false;
    public State<Boolean> hiddenState;
    public boolean hidden() { return hiddenState != null ? hiddenState.getValue() : hidden; }

    // ── Widget-specific properties (used when this spec is attached to a widget) ──

    public Color textColor = Theme.textPrimary;
    public State<Color> textColorState;
    public Color textColor() { return textColorState != null ? textColorState.getValue() : textColor; }

    public int textAlign = arc.util.Align.left;
    public State<Integer> textAlignState;
    public int textAlign() { return textAlignState != null ? textAlignState.getValue() : textAlign; }

    public boolean wrap = false;
    public State<Boolean> wrapState;
    public boolean wrap() { return wrapState != null ? wrapState.getValue() : wrap; }

    public ButtonStyle button;

    // ── Self sizing ───────────────────────────────────────────────────────

    public enum SizeMode { WRAP, GROW, FIXED }

    public SizeMode widthMode  = SizeMode.WRAP;
    public State<SizeMode> widthModeState;
    public SizeMode widthMode() { return widthModeState != null ? widthModeState.getValue() : widthMode; }

    public SizeMode heightMode = SizeMode.WRAP;
    public State<SizeMode> heightModeState;
    public SizeMode heightMode() { return heightModeState != null ? heightModeState.getValue() : heightMode; }

    public float fixedWidth  = 0f;
    public State<Float> fixedWidthState;
    public float fixedWidth() { return fixedWidthState != null ? fixedWidthState.getValue() : fixedWidth; }

    public float fixedHeight = 0f;
    public State<Float> fixedHeightState;
    public float fixedHeight() { return fixedHeightState != null ? fixedHeightState.getValue() : fixedHeight; }

    public float growWeightX = 1f;
    public State<Float> growWeightXState;
    public float growWeightX() { return growWeightXState != null ? growWeightXState.getValue() : growWeightX; }

    public float growWeightY = 1f;
    public State<Float> growWeightYState;
    public float growWeightY() { return growWeightYState != null ? growWeightYState.getValue() : growWeightY; }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Lazy sub-objects — null when not used
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    public Pad        pad;
    public Align      align;
    public Visual     visual;
    public Constraint constraint;
    public Transition transition;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Sub-object classes (each property also has optional State<T>)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    public static final class Pad {
        public float top, right, bottom, left;
        public State<Float> topState, rightState, bottomState, leftState;
        public float top()    { return topState    != null ? topState.getValue()    : top; }
        public float right()  { return rightState  != null ? rightState.getValue()  : right; }
        public float bottom() { return bottomState != null ? bottomState.getValue() : bottom; }
        public float left()   { return leftState   != null ? leftState.getValue()   : left; }
    }

    public static final class Align {
        public enum Justify { START, CENTER, END, BETWEEN }
        public enum Items   { START, CENTER, END, STRETCH }
        public Justify justify = Justify.START;
        public Items   items   = Items.STRETCH;
        public State<Justify> justifyState;
        public State<Items>   itemsState;
        public Justify justify() { return justifyState != null ? justifyState.getValue() : justify; }
        public Items   items()   { return itemsState   != null ? itemsState.getValue()   : items; }
    }

    public static final class Visual {
        public Color  background  = null;
        public Color  borderColor = null;
        public float  borderWidth = 1f;
        public float  opacity     = 1f;
        public State<Color> backgroundState;
        public State<Color> borderColorState;
        public State<Float> borderWidthState;
        public State<Float> opacityState;
        public Color  background()  { return backgroundState  != null ? backgroundState.getValue()  : background; }
        public Color  borderColor() { return borderColorState != null ? borderColorState.getValue() : borderColor; }
        public float  borderWidth() { return borderWidthState != null ? borderWidthState.getValue() : borderWidth; }
        public float  opacity()     { return opacityState     != null ? opacityState.getValue()     : opacity; }
    }

    public static final class Constraint {
        public float minWidth = -1f, maxWidth  = -1f;
        public float minHeight = -1f, maxHeight = -1f;
        public State<Float> minWidthState, maxWidthState, minHeightState, maxHeightState;
        public float minWidth()  { return minWidthState  != null ? minWidthState.getValue()  : minWidth; }
        public float maxWidth()  { return maxWidthState  != null ? maxWidthState.getValue()  : maxWidth; }
        public float minHeight() { return minHeightState != null ? minHeightState.getValue() : minHeight; }
        public float maxHeight() { return maxHeightState != null ? maxHeightState.getValue() : maxHeight; }
    }

    public static final class Transition {
        public float  duration = 150f;
        public Interp ease     = Interp.smooth;
        public State<Float> durationState;
        public State<Interp> easeState;
        public float  transitionDuration() { return durationState != null ? durationState.getValue() : duration; }
        public Interp transitionEase()     { return easeState     != null ? easeState.getValue()     : ease; }
    }

    public enum Variant { DEFAULT, PRIMARY, DANGER, GHOST }

    public static final class ButtonStyle {
        public Variant variant = Variant.DEFAULT;
        public boolean disabled = false;
        public State<Variant> variantState;
        public State<Boolean> disabledState;
        public Variant variant()   { return variantState   != null ? variantState.getValue()   : variant; }
        public boolean disabled()  { return disabledState  != null ? disabledState.getValue()  : disabled; }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Lazy init helpers (used by StyleParser)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    public Pad        pad()        { return pad        != null ? pad        : (pad        = new Pad());        }
    public Align      align()      { return align      != null ? align      : (align      = new Align());      }
    public Visual     visual()     { return visual     != null ? visual     : (visual     = new Visual());     }
    public Constraint constraint() { return constraint != null ? constraint : (constraint = new Constraint()); }
    public Transition transition() { return transition != null ? transition : (transition = new Transition()); }
    public ButtonStyle button()    { return button    != null ? button    : (button    = new ButtonStyle());    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Safe accessors — return defaults when sub-object is null
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    // Padding
    public float padH()      { return pad != null ? pad.left() + pad.right()  : 0f; }
    public float padV()      { return pad != null ? pad.top()  + pad.bottom() : 0f; }
    public float padLeft()   { return pad != null ? pad.left()   : 0f; }
    public float padRight()  { return pad != null ? pad.right()  : 0f; }
    public float padTop()    { return pad != null ? pad.top()    : 0f; }
    public float padBottom() { return pad != null ? pad.bottom() : 0f; }

    // Alignment
    public Align.Justify justify() {
        return align != null ? align.justify() : Align.Justify.START;
    }
    public Align.Items items() {
        return align != null ? align.items() : Align.Items.STRETCH;
    }

    // Visual (delegates to Visual methods which check State)
    public Color  background()  { return visual != null ? visual.background()  : null;  }
    public Color  borderColor() { return visual != null ? visual.borderColor() : null;  }
    public float  borderWidth() { return visual != null ? visual.borderWidth() : 1f;    }
    public float  opacity()     { return visual != null ? visual.opacity()     : 1f;    }

    // Constraints
    public float minWidth()  { return constraint != null ? constraint.minWidth()  : -1f; }
    public float maxWidth()  { return constraint != null ? constraint.maxWidth()  : -1f; }
    public float minHeight() { return constraint != null ? constraint.minHeight() : -1f; }
    public float maxHeight() { return constraint != null ? constraint.maxHeight() : -1f; }

    // Transition
    public float  transitionDuration() { return transition != null ? transition.transitionDuration() : 0f;           }
    public Interp transitionEase()     { return transition != null ? transition.transitionEase()     : Interp.linear; }

    // ── Constrain helpers ─────────────────────────────────────────────────

    public float constrain(float v, float min, float max) {
        if (min >= 0f && v < min) v = min;
        if (max >= 0f && v > max) v = max;
        return v;
    }

    public float constrainW(float v) { return constrain(v, minWidth(),  maxWidth());  }
    public float constrainH(float v) { return constrain(v, minHeight(), maxHeight()); }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Fluent setters
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    public StyleSpec col()       { isColumn = true;         return this; }
    public StyleSpec row()       { isColumn = false;        return this; }
    public StyleSpec gap(float v)    { gap = v;                 return this; }
    public StyleSpec hidden(boolean v) { hidden = v;               return this; }

    public StyleSpec textColor(Color v)  { textColor = v;            return this; }
    public StyleSpec textAlign(int v)    { textAlign = v;            return this; }
    public StyleSpec wrap(boolean v)     { wrap = v;                 return this; }
    public StyleSpec widthMode(SizeMode v)  { widthMode = v;            return this; }
    public StyleSpec heightMode(SizeMode v) { heightMode = v;           return this; }
    public StyleSpec fixedWidth(float v)   { fixedWidth = v;           return this; }
    public StyleSpec fixedHeight(float v)  { fixedHeight = v;          return this; }
    public StyleSpec growWeightX(float v)  { growWeightX = v;          return this; }
    public StyleSpec growWeightY(float v)  { growWeightY = v;          return this; }

    public StyleSpec grow()    { widthMode = SizeMode.GROW; heightMode = SizeMode.GROW; return this; }
    public StyleSpec growX()   { widthMode = SizeMode.GROW;                               return this; }
    public StyleSpec growY()   { heightMode = SizeMode.GROW;                              return this; }
    public StyleSpec w(float v)  { widthMode = SizeMode.FIXED;  fixedWidth  = v; return this; }
    public StyleSpec h(float v)  { heightMode = SizeMode.FIXED; fixedHeight = v; return this; }

    public StyleSpec p(float all)      { pad().top = pad().right = pad().bottom = pad().left = all; return this; }
    public StyleSpec px(float v)       { pad().left = pad().right = v;                              return this; }
    public StyleSpec py(float v)       { pad().top  = pad().bottom = v;                             return this; }
    public StyleSpec pt(float v)       { pad().top    = v; return this; }
    public StyleSpec pb(float v)       { pad().bottom = v; return this; }
    public StyleSpec pl(float v)       { pad().left   = v; return this; }
    public StyleSpec pr(float v)       { pad().right  = v; return this; }

    public StyleSpec justify(Justify v) { align().justify = v; return this; }
    public StyleSpec items(Items v)     { align().items   = v; return this; }

    public StyleSpec bg(Color v)       { visual().background  = v; return this; }
    public StyleSpec border(Color v)   { visual().borderColor = v; return this; }
    public StyleSpec borderWidth(float v) { visual().borderWidth = v; return this; }
    public StyleSpec opacity(float v)  { visual().opacity     = v; return this; }

    public StyleSpec minW(float v)  { constraint().minWidth  = v; return this; }
    public StyleSpec maxW(float v)  { constraint().maxWidth  = v; return this; }
    public StyleSpec minH(float v)  { constraint().minHeight = v; return this; }
    public StyleSpec maxH(float v)  { constraint().maxHeight = v; return this; }

    public StyleSpec duration(float v) { transition().duration = v; return this; }
    public StyleSpec ease(Interp v)    { transition().ease     = v; return this; }

    public StyleSpec variant(Variant v)  { button().variant   = v; return this; }
    public StyleSpec disabled(boolean v) { button().disabled  = v; return this; }
}
