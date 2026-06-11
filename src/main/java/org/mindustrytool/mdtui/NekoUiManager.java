package org.mindustrytool.mdtui;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class NekoUiManager {
    private final FontManager fontManager;

    public void init() {

    }

}
