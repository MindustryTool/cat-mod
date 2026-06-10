package org.mindustrytool.mdtui;

import arc.Core;
import arc.Events;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;

import org.mindustrytool.auth.AuthService;
import org.mindustrytool.libs.ui.style.Fonts;
import org.mindustrytool.libs.ui.components.CustomUIComponent;
import org.mindustrytool.libs.ui.components.Label;
import org.mindustrytool.libs.ui.components.Layout;
import org.mindustrytool.libs.ui.style.Theme;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class NekoUiManager {

    private final AuthService authService;

    public void init() {
        authService.init();
        Fonts.load();

        Events.on(ClientLoadEvent.class,
            e -> Core.app.post(() -> Vars.ui.menufrag.addButton("Neko Content Mod", Icon.settings, this::showSettingsDialog)));
    }

    private void showSettingsDialog() {
        var dialog = new BaseDialog("Neko Content Mod");

        var content = Layout.of()
            .style(s -> s.column().gap(16f).padding(24f).size(sz -> sz.growX().minimumHeight(400f)))

            .child(Label.of().style(s -> s
                .text("Settings").textColor(Theme.TEXT_BRIGHT).fontScale(1.4f)))

            .child(CustomUIComponent.of().style(s -> s
                .fill(Theme.BORDER).size(sz -> sz.growX().height(1f))))

            .child(Label.of().style(s -> s
                .text("Coming soon...").textColor(Theme.TEXT_GHOST).fontScale(0.9f)));

        dialog.cont.add(content.element()).grow();
        dialog.addCloseButton();
        dialog.show();
    }
}
