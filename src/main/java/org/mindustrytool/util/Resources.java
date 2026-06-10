package org.mindustrytool.util;

import mindustry.Vars;

public class Resources {
    public static final String FONT_MONO = "fonts/JetBrainsMono-Regular.ttf";
    public static final String BUNDLE = "bundles/bundle";
    public static final String ICON_MOD = "icons/mod.png";

    public static final String SHADER_CUSTOM_ELEMENT_VERT = "shaders/custom_element.vert";
    public static final String SHADER_CUSTOM_ELEMENT_FRAG = "shaders/custom_element.frag";
    public static final String SHADER_BLUR_VERT = "shaders/blur.vert";
    public static final String SHADER_BLUR_FRAG = "shaders/blur.frag";

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
