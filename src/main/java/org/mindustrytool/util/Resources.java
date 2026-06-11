package org.mindustrytool.util;

import arc.files.Fi;

import lombok.experimental.UtilityClass;

import mindustry.Vars;

@UtilityClass
public class Resources {
    public static final Fi IMAGE_CACHE_DIR = Vars.tmpDirectory.child("neko-image-caches");

    public static final String FONT_JETBRAINS_MONO = "fonts/JetBrainsMono-Regular.ttf";

    public static final String SHADER_CUSTOM_ELEMENT_VERT = "shaders/custom_element.vert";
    public static final String SHADER_CUSTOM_ELEMENT_FRAG = "shaders/custom_element.frag";
    public static final String SHADER_BLUR_VERT = "shaders/blur.vert";
    public static final String SHADER_BLUR_FRAG = "shaders/blur.frag";

    public static final String BUNDLE = "bundles/bundle";
    public static final String ICON_MOD = "icons/mod.png";

    /**
     * Reads a resource text file from the mod's tree.
     *
     * @param path the resource path
     * @return the text contents of the resource file
     */
    public static String readString(String path) {
        return Vars.tree.get(path).readString();
    }
}
