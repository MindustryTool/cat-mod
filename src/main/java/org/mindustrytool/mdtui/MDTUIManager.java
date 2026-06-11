package org.mindustrytool.mdtui;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

import org.mindustrytool.mdtui.screen.DemoUI;

/**
 * Manages the Mindustry Developer Toolkit UI overlay.
 * <p>
 * Handles registration of custom UI elements, menus, and HUD components.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class MDTUIManager {
    private final DemoUI demoUI;

    /** Initialises the MDT UI overlay. Called once during mod bootstrap. */
    public void init() {
        // DemoUI is eagerly created by Feather injection — its constructor
        // builds the full reactive scene graph. The root element can be
        // retrieved via demoUI.element() for mounting.
    }
}
