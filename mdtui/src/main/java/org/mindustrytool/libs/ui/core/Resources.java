package org.mindustrytool.libs.ui.core;

import arc.files.Fi;

import lombok.experimental.UtilityClass;

import mindustry.Vars;

/**
 * Central registry for mod resource paths and directories.
 * <p>
 * All constants point into the mod's resource tree ({@code Vars.tree}) and
 * are resolved at runtime via {@link mindustry.Vars#tree}.
 */
@UtilityClass
public class Resources {
    /** Temp directory for cached downloaded images. */
    public static final Fi IMAGE_CACHE_DIR = Vars.tmpDirectory.child("neko-image-caches");

    /** Custom-element SDF vertex shader. */
    public static final String SHADER_CUSTOM_ELEMENT_VERT = "shaders/custom_element.vert";
    /** Custom-element SDF fragment shader. */
    public static final String SHADER_CUSTOM_ELEMENT_FRAG = "shaders/custom_element.frag";
    /** Gaussian blur vertex shader. */
    public static final String SHADER_BLUR_VERT = "shaders/blur.vert";
    /** Gaussian blur fragment shader. */
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
