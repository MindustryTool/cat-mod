package org.mindustrytool;

import arc.Core;
import arc.util.Log;

import mindustry.mod.Mod;

import org.mindustrytool.signal.Signal;
import org.mindustrytool.ui.components.*;
import org.mindustrytool.ui.spec.LayoutSpec.Justify;
import org.mindustrytool.ui.style.Theme;

public class NekoMod extends Mod {

    public NekoMod() {
        Log.info("NekoMod initialized.");
    }

    @Override
    public void init() {
        var darkMode = Signal.of(false);
        var compact = Signal.of(false);
        var autoSave = Signal.of(true);
        var emailAlerts = Signal.of(true);
        var pushNotify = Signal.of(false);
        var soundOn = Signal.of(true);
        var langIndex = Signal.of(0);
        var dirty = Signal.of(false);

        String[] languages = {"English", "Tiếng Việt", "日本語", "中文"};
        var langDisplay = Signal.computed(() -> languages[langIndex.get()]);
        var saveLabel = Signal.computed(() -> dirty.get() ? "● Save Changes" : "Save Changes");

        var root = Layout.build().col().style(s -> s.gap(20).p(24))
            .children(

                Layout.build().row()
                    .style(s -> s.justify(Justify.BETWEEN).p(0, 0, 16, 0))
                    .children(
                        Label.build().text("Settings").style(s -> s.textColor(Theme.TEXT_BRIGHT)).build(),
                        Button.build()
                            .child(Label.build().text("✕").build())
                            .clicked(() -> Core.app.exit())
                            .style(Button.Style::ghostVariant)
                            .build()
                    )
                    .build(),

                Layout.build().col().style(s -> s.gap(10).p(16))
                    .children(
                        Label.build().text("ACCOUNT").style(s -> s.textColor(Theme.TEXT_SECONDARY)).build(),
                        labelRow("Display Name", "John Doe"),
                        labelRow("Email", "john@example.com"),
                        labelRow("Member Since", "Jan 2026"),
                        Button.build()
                            .child(Label.build().text("✎ Edit Profile").build())
                            .clicked(() -> dirty.set(true))
                            .style(s -> s.ghostVariant())
                            .build()
                    )
                    .build(),

                Layout.build().col().style(s -> s.gap(10).p(16))
                    .children(
                        Label.build().text("PREFERENCES").style(s -> s.textColor(Theme.TEXT_SECONDARY)).build(),
                        toggledRow("Dark Mode", darkMode),
                        toggledRow("Compact View", compact),
                        Layout.build().row().style(s -> s.gap(8).justify(Justify.BETWEEN))
                            .children(
                                Label.build().text("Language").style(s -> s.textColor(Theme.TEXT_PRIMARY)).build(),
                                Button.build()
                                    .child(Label.build().text(langDisplay).build())
                                    .clicked(() -> {
                                        langIndex.set((langIndex.get() + 1) % languages.length);
                                        dirty.set(true);
                                    })
                                    .style(s -> s.ghostVariant())
                                    .build()
                            )
                            .build(),
                        toggledRow("Auto-save", autoSave)
                    )
                    .build(),

                Layout.build().col().style(s -> s.gap(10).p(16))
                    .children(
                        Label.build().text("NOTIFICATIONS").style(s -> s.textColor(Theme.TEXT_SECONDARY)).build(),
                        toggledRow("Email alerts", emailAlerts),
                        toggledRow("Push notifications", pushNotify),
                        toggledRow("Sound", soundOn)
                    )
                    .build(),

                Layout.build().col().style(s -> s.gap(8).p(16))
                    .children(
                        Label.build().text("DANGER ZONE").style(s -> s.textColor(Theme.ACCENT_RED)).build(),
                        Label.build().text("These actions cannot be undone.").style(s -> s.textColor(Theme.TEXT_SECONDARY)).build(),
                        Button.build()
                            .child(Label.build().text("☠ Delete Account").build())
                            .clicked(() -> Log.info("Account deleted!"))
                            .style(s -> s.dangerVariant())
                            .build()
                    )
                    .build(),

                Layout.build().row()
                    .style(s -> s.justify(Justify.BETWEEN).pt(8))
                    .children(
                        Label.build().text("v2.4.1").style(s -> s.textColor(Theme.TEXT_GHOST)).build(),
                        Layout.build().row().style(s -> s.gap(8))
                            .children(
                                Button.build()
                                    .child(Label.build().text("Discard").build())
                                    .clicked(() -> { dirty.set(false); Log.info("Changes discarded."); })
                                    .style(s -> s.ghostVariant())
                                    .build(),
                                Button.build()
                                    .child(Label.build().text(saveLabel).build())
                                    .clicked(() -> { dirty.set(false); Log.info("Settings saved."); })
                                    .style(s -> s.primaryVariant())
                                    .build()
                            )
                            .build()
                    )
                    .build()

            )
            .build();

        var pane = ScrollPane.build()
            .child(root)
            .style(s -> s.disableX(true).fadeScrollBars(true).scrollBarsOnTop(true))
            .size(s -> s.w(480f).h(620f))
            .build();

        var el = pane.element();
        el.setPosition(
            (Core.graphics.getWidth() - 480f) / 2f,
            (Core.graphics.getHeight() - 620f) / 2f
        );
        Core.scene.add(el);
        Log.info("Settings panel rendered.");
    }

    private static Layout toggledRow(String label, Signal<Boolean> state) {
        return Layout.build().row().style(s -> s.gap(8).justify(Justify.BETWEEN))
            .children(
                Label.build().text(label).style(s -> s.textColor(Theme.TEXT_PRIMARY)).build(),
                Button.build()
                    .child(Label.build().text(Signal.computed(() -> state.get() ? "On" : "Off")).build())
                    .clicked(() -> state.set(!state.get()))
                    .bind(self -> state.onChange(v -> {
                        if (v) self.primaryVariant();
                        else self.ghostVariant();
                    }))
                    .size(s -> s.fixedWidth(64f))
                    .build()
            )
            .build();
    }

    private static Layout labelRow(String key, String value) {
        return Layout.build().row().style(s -> s.gap(8).justify(Justify.BETWEEN))
            .children(
                Label.build().text(key).style(s -> s.textColor(Theme.TEXT_SECONDARY)).build(),
                Label.build().text(value).style(s -> s.textColor(Theme.TEXT_BRIGHT)).build()
            )
            .build();
    }
}
