package org.mindustrytool.mdtui.screen;

import arc.graphics.Color;

import lombok.With;

import org.mindustrytool.libs.ui.layout.LayoutSpec;

@With
public record AppState(
    int activeTab,
    int bgMode,
    Color fillColor,
    Color gradColor1,
    Color gradColor2,
    float gradAngle,
    String activeUrl,
    float radiusTL,
    float radiusTR,
    float radiusBR,
    float radiusBL,
    boolean borderOn,
    float borderWidth,
    Color borderColor,
    boolean dashOn,
    float dashLength,
    float dashRatio,
    float innerShadowSpread,
    float innerShadowBlur,
    Color innerShadowColor,
    float glowSpread,
    Color glowColor,
    float opacity,
    int filterType,
    float filterAmount,
    float noiseAmount,
    boolean backdropOn,
    float backdropBlend,
    float backdropWeight,
    float backdropIterations,
    boolean sandboxIsColumn,
    boolean sandboxIsWrap,
    float sandboxGap,
    LayoutSpec.JustifyContent sandboxJustify,
    LayoutSpec.AlignItems sandboxAlign
) {
    public static AppState initial() {
        return new AppState(
            0,
            0,
            Color.valueOf("303052"),
            Color.valueOf("6c8ebf"),
            Color.valueOf("ff79c6"),
            45f,
            "https://github.com/Anuken.png",
            12f, 12f, 12f, 12f,
            true,
            2f,
            Color.white,
            false,
            10f,
            0.5f,
            0f,
            0f,
            Color.valueOf("00000066"),
            0f,
            Color.valueOf("ff79c644"),
            1f,
            0,
            0.5f,
            0f,
            false,
            0.8f,
            0.8f,
            4f,
            false,
            false,
            12f,
            LayoutSpec.JustifyContent.START,
            LayoutSpec.AlignItems.STRETCH
        );
    }
}
