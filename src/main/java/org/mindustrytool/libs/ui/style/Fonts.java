package org.mindustrytool.libs.ui.style;

import arc.freetype.FreeTypeFontGenerator;
import arc.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import arc.graphics.g2d.Font;

import lombok.experimental.UtilityClass;

import mindustry.Vars;

import org.mindustrytool.util.Resources;

@UtilityClass
public class Fonts {
    public static Font mono;

    public static void load() {
        mono = new FreeTypeFontGenerator(Vars.tree.get(Resources.FONT_MONO))
            .generateFont(new FreeTypeFontParameter() {{
                size = 32;
                incremental = true;
            }});

        mono.getData().markupEnabled = true;
    }
}
