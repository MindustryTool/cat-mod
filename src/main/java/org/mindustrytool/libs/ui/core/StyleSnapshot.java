package org.mindustrytool.libs.ui.core;

/**
 * Immutable snapshot of all animatable visual properties in {@link CustomComponent.Style}.
 *
 * <p>Using a Java {@code record} with an all-args constructor provides compile-time safety:
 * adding a new field to this record forces every {@link #from} call site to update, preventing
 * silent drift between Style fields and the animation system.
 *
 * <p>arc's {@link arc.graphics.Color} is mutable, so colors are stored as individual float
 * components to keep this record truly immutable.
 *
 * <p>Non-animatable properties (gradient, texture, UV params) are intentionally excluded —
 * they change discretely and are not interpolated.
 */
public record StyleSnapshot(

        // fill
        float fillR, float fillG, float fillB, float fillA,

        // corners
        float topLeftRadius, float topRightRadius,
        float bottomRightRadius, float bottomLeftRadius,

        // border
        float borderWidth,
        float borderR, float borderG, float borderB, float borderA,
        int borderStyle, float dashLength, float dashRatio,

        // inner shadow
        float innerShadowSpread, float innerShadowBlur,
        float shadowR, float shadowG, float shadowB, float shadowA,

        // outer glow
        float glowSpread,
        float glowR, float glowG, float glowB, float glowA,

        // misc
        float opacity

) {

    /**
     * Captures the current animatable state of {@code style}.
     * If a new field is added to this record, this method will not compile until updated.
     */
    public static StyleSnapshot from(CustomComponent.Style style) {
        return new StyleSnapshot(
                style.fillColor.r, style.fillColor.g, style.fillColor.b, style.fillColor.a,
                style.topLeftRadius, style.topRightRadius,
                style.bottomRightRadius, style.bottomLeftRadius,
                style.borderWidth,
                style.borderColor.r, style.borderColor.g, style.borderColor.b, style.borderColor.a,
                style.borderStyle, style.dashLength, style.dashRatio,
                style.innerShadowSpread, style.innerShadowBlur,
                style.innerShadowColor.r, style.innerShadowColor.g,
                style.innerShadowColor.b, style.innerShadowColor.a,
                style.glowSpread,
                style.glowColor.r, style.glowColor.g, style.glowColor.b, style.glowColor.a,
                style.opacity
        );
    }


    /** Writes all animatable values from this snapshot back into {@code style}. */
    public void applyTo(CustomComponent.Style style) {
        style.fillColor.set(fillR, fillG, fillB, fillA);
        style.topLeftRadius = topLeftRadius;
        style.topRightRadius = topRightRadius;
        style.bottomRightRadius = bottomRightRadius;
        style.bottomLeftRadius = bottomLeftRadius;
        style.borderWidth = borderWidth;
        style.borderColor.set(borderR, borderG, borderB, borderA);
        style.borderStyle = borderStyle;
        style.dashLength = dashLength;
        style.dashRatio = dashRatio;
        style.innerShadowSpread = innerShadowSpread;
        style.innerShadowBlur = innerShadowBlur;
        style.innerShadowColor.set(shadowR, shadowG, shadowB, shadowA);
        style.glowSpread = glowSpread;
        style.glowColor.set(glowR, glowG, glowB, glowA);
        style.opacity = opacity;
    }


    /**
     * Returns a new snapshot that is the linear interpolation between {@code a} and {@code b}
     * at normalised time {@code t} (0 = a, 1 = b).
     *
     * <p>{@code borderStyle} is a discrete value and switches at the midpoint.
     */
    public static StyleSnapshot lerp(StyleSnapshot a, StyleSnapshot b, float t) {
        float u = 1f - t;
        return new StyleSnapshot(
                a.fillR * u + b.fillR * t,
                a.fillG * u + b.fillG * t,
                a.fillB * u + b.fillB * t,
                a.fillA * u + b.fillA * t,

                a.topLeftRadius * u + b.topLeftRadius * t,
                a.topRightRadius * u + b.topRightRadius * t,
                a.bottomRightRadius * u + b.bottomRightRadius * t,
                a.bottomLeftRadius * u + b.bottomLeftRadius * t,

                a.borderWidth * u + b.borderWidth * t,
                a.borderR * u + b.borderR * t,
                a.borderG * u + b.borderG * t,
                a.borderB * u + b.borderB * t,
                a.borderA * u + b.borderA * t,
                t < 0.5f ? a.borderStyle : b.borderStyle,
                a.dashLength * u + b.dashLength * t,
                a.dashRatio * u + b.dashRatio * t,

                a.innerShadowSpread * u + b.innerShadowSpread * t,
                a.innerShadowBlur * u + b.innerShadowBlur * t,
                a.shadowR * u + b.shadowR * t,
                a.shadowG * u + b.shadowG * t,
                a.shadowB * u + b.shadowB * t,
                a.shadowA * u + b.shadowA * t,

                a.glowSpread * u + b.glowSpread * t,
                a.glowR * u + b.glowR * t,
                a.glowG * u + b.glowG * t,
                a.glowB * u + b.glowB * t,
                a.glowA * u + b.glowA * t,

                a.opacity * u + b.opacity * t
        );
    }
}
