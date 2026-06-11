package org.mindustrytool.mdtui.menu;

import lombok.RequiredArgsConstructor;

import org.mindustrytool.libs.ui.components.Layout;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Root manager for the in-game menu UI.
 * <p>
 * Builds and maintains the menu scene graph rooted at a {@link Layout}.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MenuManager {
    private Layout root = Layout.of();

    /** Initialises the menu hierarchy. Called once during mod bootstrap. */
    public void init() {

    }
}
