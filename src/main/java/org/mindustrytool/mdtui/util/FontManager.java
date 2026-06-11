package org.mindustrytool.mdtui;

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
    private final @Getter Font mono;

    @Inject
    public FontManager() {
        mono = new FreeTypeFontGenerator(Vars.tree.get(Resources.FONT_MONO))
            .generateFont(new FreeTypeFontParameter() {{
                size = 32;
                incremental = true;
            }});

        mono.getData().markupEnabled = true;
    }
}
