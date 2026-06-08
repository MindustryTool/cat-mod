package com.neko;

import arc.Core;
import arc.util.Log;

import mindustry.mod.Mod;

import com.neko.libs.signal.Signal;
import com.neko.libs.simpleui.Layout;
import com.neko.libs.simpleui.components.*;
import com.neko.libs.simpleui.spec.LayoutSpec.Justify;
import com.neko.libs.ui.style.Theme;

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

        var root = Layout.column(

            // ── Header ──
            Layout.row(
                Label.of("Settings").style(s -> s.textColor(Theme.textBright)),
                TextButton.of().text("✕").onClick(() -> Core.app.exit()).ghostVariant()
            ).style(s -> s.justify(Justify.BETWEEN).p(0, 0, 16, 0)),

            // ── Account ──
            Layout.column(
                Label.of("ACCOUNT").style(s -> s.textColor(Theme.textSecondary)),
                labelRow("Display Name", "John Doe"),
                labelRow("Email", "john@example.com"),
                labelRow("Member Since", "Jan 2026"),
                TextButton.of().text("✎ Edit Profile").onClick(() -> dirty.set(true)).ghostVariant()
            ).style(s -> s.gap(10).p(16)),

            // ── Preferences ──
            Layout.column(
                Label.of("PREFERENCES").style(s -> s.textColor(Theme.textSecondary)),
                toggledRow("Dark Mode", darkMode),
                toggledRow("Compact View", compact),
                Layout.row(
                    Label.of("Language").style(s -> s.textColor(Theme.textPrimary)),
                    TextButton.of().text(langDisplay).onClick(() -> {
                        langIndex.set((langIndex.get() + 1) % languages.length);
                        dirty.set(true);
                    }).ghostVariant()
                ).style(s -> s.gap(8).justify(Justify.BETWEEN)),
                toggledRow("Auto-save", autoSave)
            ).style(s -> s.gap(10).p(16)),

            // ── Notifications ──
            Layout.column(
                Label.of("NOTIFICATIONS").style(s -> s.textColor(Theme.textSecondary)),
                toggledRow("Email alerts", emailAlerts),
                toggledRow("Push notifications", pushNotify),
                toggledRow("Sound", soundOn)
            ).style(s -> s.gap(10).p(16)),

            // ── Danger zone ──
            Layout.column(
                Label.of("DANGER ZONE").style(s -> s.textColor(Theme.accentRed)),
                Label.of("These actions cannot be undone.").style(s -> s.textColor(Theme.textSecondary)),
                TextButton.of().text("☠ Delete Account").onClick(() -> Log.info("Account deleted!"))
                    .dangerVariant()
            ).style(s -> s.gap(8).p(16)),

            // ── Footer ──
            Layout.row(
                Label.of("v2.4.1").style(s -> s.textColor(Theme.textGhost)),
                Layout.row(
                    TextButton.of().text("Discard").onClick(() -> {
                            dirty.set(false);
                            Log.info("Changes discarded.");
                        })
                        .ghostVariant(),
                    TextButton.of().text(saveLabel).onClick(() -> {
                            dirty.set(false);
                            Log.info("Settings saved.");
                        })
                        .primaryVariant()
                ).style(s -> s.gap(8))
            ).style(s -> s.justify(Justify.BETWEEN).pt(8))

        ).style(s -> s.gap(20).p(24));

        var pane = ScrollPane.of(root)
            .style(s -> s.disableX(true).fadeScrollBars(true).scrollBarsOnTop(true))
            .size(s -> s.w(480f).h(620f));

        var el = pane.element();
        el.setPosition(
            (Core.graphics.getWidth() - 480f) / 2f,
            (Core.graphics.getHeight() - 620f) / 2f
        );
        Core.scene.add(el);
        Log.info("Settings panel rendered.");
    }

    private static Layout toggledRow(String label, Signal<Boolean> state) {
        return Layout.row(
            Label.of(label).style(s -> s.textColor(Theme.textPrimary)),
            TextButton.of()
                .onClick(() -> state.set(!state.get()))
                .bind(self -> state.onChange(v -> {
                    if (v) self.primaryVariant();
                    else self.ghostVariant();
                }))
                .size(s -> s.fixedWidth(64f))
                .text(Signal.computed(() -> state.get() ? "On" : "Off"))
        ).style(s -> s.gap(8).justify(Justify.BETWEEN));
    }

    private static Layout labelRow(String key, String value) {
        return Layout.row(
            Label.of(key).style(s -> s.textColor(Theme.textSecondary)),
            Label.of(value).style(s -> s.textColor(Theme.textBright))
        ).style(s -> s.gap(8).justify(Justify.BETWEEN));
    }
}
