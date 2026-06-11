package org.mindustrytool.mdtui.util;

import arc.freetype.FreeTypeFontGenerator;
import arc.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import arc.graphics.g2d.Font;

import lombok.Getter;

import mindustry.Vars;

import org.mindustrytool.util.Resources;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provides pre-configured fonts used across the mod UI.
 * <p>
 * Fonts are generated once at construction time and cached for the lifetime
 * of the mod. Injected by Feather as a singleton.
 */
@Singleton
public final class FontManager {
    /** JetBrains Mono regular 32px, with color markup enabled. */
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
