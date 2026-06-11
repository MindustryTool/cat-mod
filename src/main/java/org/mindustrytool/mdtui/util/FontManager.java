package org.mindustrytool.mdtui.util;

import arc.freetype.FreeTypeFontGenerator;
import arc.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import arc.graphics.g2d.Font;

import lombok.Getter;

import mindustry.Vars;

import org.mindustrytool.util.Resources;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class FontManager {
    private final @Getter Font jetbrainsMono;

    @Inject
    public FontManager() {
        jetbrainsMono = new FreeTypeFontGenerator(Vars.tree.get(Resources.FONT_JETBRAINS_MONO))
            .generateFont(new FreeTypeFontParameter() {{
                size = 32;
                incremental = true;
            }});

        jetbrainsMono.getData().markupEnabled = true;
    }
}
