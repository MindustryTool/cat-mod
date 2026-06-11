package org.mindustrytool;

import lombok.RequiredArgsConstructor;

import org.mindustrytool.mdtui.MDTUIManager;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ModManager {
    private final MDTUIManager mdtuiManager;

    public void init() {
        mdtuiManager.init();
    }
}
