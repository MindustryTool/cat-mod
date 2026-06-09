package org.mindustrytool;

import arc.Core;
import arc.util.Log;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.mindustrytool.mui.widgets.ExampleWidget;
import org.mindustrytool.ui.components.Button;
import org.mindustrytool.ui.components.CustomUIComponent;
import org.mindustrytool.ui.components.Label;
import org.mindustrytool.ui.components.Layout;
import org.mindustrytool.ui.components.Stack;
import org.mindustrytool.ui.spec.LayoutSpec.Justify;
import org.mindustrytool.ui.style.Theme;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class NekoUiManager {

    private final SettingsService settingsService;
    private final ExampleWidget exampleWidget;
    private final org.mindustrytool.auth.AuthService authService;
    private final org.mindustrytool.chat.ChatService chatService;
    private final org.mindustrytool.chat.ui.ChatOverlay chatOverlay;
    private final org.mindustrytool.translation.ChatTranslationFeature chatTranslationFeature;

    private static final float pw = 1200f;
    private static final float ph = 800f;
    private arc.scene.Element rootElement;

    public void init() {
        authService.init();
        chatService.init();
        chatTranslationFeature.init();

        if (authService.isLoggedIn()) {
            chatService.connectStream();
        }

        Core.app.post(() -> {
            if (chatOverlay.parent == null) {
                Core.scene.add(chatOverlay);
            }
        });

        var body = Layout.of()
            .style(s -> s.row().gap(0).minW(pw).size(sz -> sz.grow()))
            .child(exampleWidget.build());

        var rootPanel = Stack.of()
            .child(CustomUIComponent.of().style(s -> s.color(Theme.BACKGROUND).cornerRadius(12f).size(sz -> sz.w(pw).h(ph))))
            .child(
                Layout.of()
                    .style(s -> s.col().gap(0).size(sz -> sz.w(pw).h(ph)))
                    .child(buildHeader())
                    .child(buildDivider(1f))
                    .child(body)
                    .child(buildDivider(1f))
                    .child(buildFooter())
            );

        rootElement = rootPanel.element();
        rootElement.setSize(pw, ph);
        rootElement.setPosition(
            (Core.graphics.getWidth() - pw) / 2f,
            (Core.graphics.getHeight() - ph) / 2f
        );
        Core.scene.add(rootElement);
        Log.info("NekoMod UI initialized and rendered.");
    }

    private Layout buildHeader() {
        return Layout.of()
            .style(s -> s.row().justify(Justify.BETWEEN).p(16, 20, 12, 20))
            .child(
                Label.of().style(s -> s.text("Settings").textColor(Theme.TEXT_BRIGHT).fontScale(1.4f))
            )
            .child(
                Button.of()
                    .child(Label.of().style(s -> s.text("✕")))
                    .listener(l -> l.changed(() -> { if (rootElement != null) rootElement.remove(); }))
                    .style(s -> s.ghostVariant())
            );
    }

    private Layout buildFooter() {
        return Layout.of()
            .style(s -> s.row().justify(Justify.BETWEEN).p(16, 20))
            .child(
                Label.of().style(s -> s.text("v2.4.1").textColor(Theme.TEXT_GHOST))
            )
            .child(
                Layout.of()
                    .style(s -> s.row().gap(8))
                    .child(Label.of().style(s -> s.text("").textColor(Theme.ACCENT_GOLD)))
                    .child(
                        Button.of()
                            .child(Label.of().style(s -> s.text("Reset All")))
                            .listener(l -> l.changed(() -> {
                                settingsService.resetAll();
                                settingsService.getDirty().set(true);
                            }))
                            .style(s -> s.ghostVariant())
                    )
                    .child(
                        Button.of()
                            .child(Label.of().style(s -> s.text("Save Changes")))
                            .listener(l -> l.changed(() -> {
                                settingsService.save();
                                settingsService.getDirty().set(false);
                                Log.info("Settings saved.");
                            }))
                            .style(s -> s.primaryVariant())
                    )
            );
    }

    private static Layout buildDivider(float h) {
        return Layout.of().style(s -> s.row().size(sz -> sz.h(h)));
    }
}
