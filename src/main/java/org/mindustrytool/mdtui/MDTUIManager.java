package org.mindustrytool.mdtui;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

/**
 * Manages the Mindustry Developer Toolkit UI overlay.
 * <p>
 * Handles registration of custom UI elements, menus, and HUD components.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class MDTUIManager {

    /** Initialises the MDT UI overlay. Called once during mod bootstrap. */
    public void init() {

    }
}
