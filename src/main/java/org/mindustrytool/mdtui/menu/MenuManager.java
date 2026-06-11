package org.mindustrytool.mdtui.menu;

import lombok.RequiredArgsConstructor;

import org.mindustrytool.libs.ui.components.Layout;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MenuManager {
    private Layout root = Layout.of();

    public void init() {

    }
}
