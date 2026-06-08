package org.mindustrytool;

import arc.Core;
import arc.util.Log;

import mindustry.mod.Mod;

import org.mindustrytool.signal.Signal;
import org.mindustrytool.ui.components.*;
import org.mindustrytool.ui.spec.LayoutSpec.Items;
import org.mindustrytool.ui.spec.LayoutSpec.Justify;
import org.mindustrytool.ui.style.Theme;

public class NekoMod extends Mod {

    private final Signal<Integer> activeSection = Signal.of(0);
    private final Signal<Boolean> dirty = Signal.of(false);

    private static final String[] SECTION_NAMES = {
        "Profile", "General", "Editor", "Notifications", "Shortcuts", "About"
    };

    // Profile
    private final Signal<String> displayNameSg = Signal.of("John Doe");
    private final Signal<String> emailSg = Signal.of("john@example.com");
    private final Signal<String> bioSg = Signal.of("Cat enthusiast & game developer");
    private final Signal<String> memberSinceSg = Signal.of("January 2026");
    private final Signal<String> accountIdSg = Signal.of("usr_8a7b3c2f");
    private final Signal<String> lastLoginSg = Signal.of("Today at 14:32");

    // General
    private final Signal<String> languageSg = Signal.of("English");
    private final Signal<String> themeSg = Signal.of("Dark");
    private final Signal<Float> uiScaleSg = Signal.of(1.0f);
    private final Signal<Float> animSpeedSg = Signal.of(1.0f);
    private final Signal<Boolean> reduceMotionSg = Signal.of(false);
    private final Signal<Boolean> compactSg = Signal.of(false);
    private final Signal<Boolean> autoSaveSg = Signal.of(true);
    private final Signal<Boolean> telemetrySg = Signal.of(false);

    // Editor
    private final Signal<Float> fontSizeSg = Signal.of(14f);
    private final Signal<Float> tabSizeSg = Signal.of(4f);
    private final Signal<Boolean> wordWrapSg = Signal.of(true);
    private final Signal<Boolean> lineNumbersSg = Signal.of(true);
    private final Signal<Boolean> indentGuidesSg = Signal.of(true);
    private final Signal<Boolean> autoPairSg = Signal.of(true);
    private final Signal<Boolean> minimapSg = Signal.of(true);
    private final Signal<Float> undoLimitSg = Signal.of(100f);

    // Notifications
    private final Signal<Boolean> emailAlertsSg = Signal.of(true);
    private final Signal<Boolean> pushNotifySg = Signal.of(false);
    private final Signal<Boolean> soundSg = Signal.of(true);
    private final Signal<Boolean> desktopNotifySg = Signal.of(true);
    private final Signal<Boolean> digestSg = Signal.of(false);
    private final Signal<Boolean> quietHoursSg = Signal.of(false);

    public NekoMod() {
        Log.info("NekoMod initialized.");
    }

    @Override
    public void init() {
        var sidebar = buildSidebar();
        var content = buildContentStack();

        var root = Layout.build().col().style(s -> s.gap(0).p(0))
            .children(
                buildHeader(),
                buildDivider(1f),
                Layout.build().row().style(s -> s.gap(0).items(Items.STRETCH))
                    .children(
                        sidebar,
                        buildDividerV(),
                        content
                    )
                    .build(),
                buildDivider(1f),
                buildFooter()
            )
            .build();

        var pane = ScrollPane.build().child(root)
            .style(s -> s.disableX(true).fadeScrollBars(true).scrollBarsOnTop(true))
            .size(s -> s.w(640f).h(700f))
            .build();

        var el = pane.element();
        el.setPosition(
            (Core.graphics.getWidth() - 640f) / 2f,
            (Core.graphics.getHeight() - 700f) / 2f
        );
        Core.scene.add(el);
        Log.info("Settings panel rendered.");
    }

    private Layout buildHeader() {
        return Layout.build().row().style(s -> s.justify(Justify.BETWEEN).p(16, 20, 12, 20))
            .children(
                Label.build().text("Settings").style(s -> s.textColor(Theme.TEXT_BRIGHT).fontScale(1.4f)).build(),
                Button.build()
                    .child(Label.build().text("✕").build())
                    .clicked(Core.app::exit)
                    .style(Button.Style::ghostVariant)
                    .build()
            )
            .build();
    }

    private Layout buildFooter() {
        var saveLabel = Signal.computed(() -> dirty.get() ? "● Save Changes" : "Save Changes");
        return Layout.build().row().style(s -> s.justify(Justify.BETWEEN).p(16, 20))
            .children(
                Label.build().text("v2.4.1").style(s -> s.textColor(Theme.TEXT_GHOST)).build(),
                Layout.build().row().style(s -> s.gap(8))
                    .children(
                        Label.build().text("").style(s -> s.textColor(Theme.ACCENT_GOLD)).build(),
                        Button.build()
                            .child(Label.build().text("Reset All").build())
                            .clicked(() -> { resetAll(); dirty.set(true); })
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
            .build();
    }

    private Layout buildSidebar() {
        int n = SECTION_NAMES.length;
        var buttons = new Button[n];
        var rows = new Component[n + 1];
        rows[0] = Label.build().text("SECTIONS").style(s -> s.textColor(Theme.TEXT_GHOST).fontScale(0.7f)).build();
        for (int i = 0; i < n; i++) {
            int idx = i;
            var label = Label.build().text(Signal.computed(() ->
                (idx == activeSection.get() ? "\u25CF " : "\u25CB ") + SECTION_NAMES[idx]
            )).style(s -> {
                if (idx == 0) s.textColor(Theme.ACCENT_PRIMARY);
                else s.textColor(Theme.TEXT_PRIMARY);
            }).build();
            buttons[i] = Button.build()
                .child(label)
                .clicked(() -> activeSection.set(idx))
                .style(s -> { if (idx == 0) s.primaryVariant(); else s.ghostVariant(); })
                .size(s -> s.w(170f))
                .build();
            rows[i + 1] = buttons[i];
        }
        activeSection.onChange(s -> {
            for (int i = 0; i < n; i++) {
                if (i == s) buttons[i].primaryVariant();
                else buttons[i].ghostVariant();
            }
        });
        return Layout.build().col().style(s -> s.gap(2).p(16, 0, 12, 16))
            .children(rows)
            .build();
    }

    private Layout buildContentStack() {
        var sections = new Layout[]{
            buildProfileSection(),
            buildGeneralSection(),
            buildEditorSection(),
            buildNotificationsSection(),
            buildShortcutsSection(),
            buildAboutSection(),
        };
        var stack = Layout.build().col().style(s -> s.gap(12).p(16)).build();
        var stackGroup = (arc.scene.ui.layout.WidgetGroup) stack.element();
        for (int i = 0; i < sections.length; i++) {
            stackGroup.addChild(sections[i].element());
            sections[i].element().visible = i == 0;
        }
        activeSection.onChange(s -> {
            for (int i = 0; i < sections.length; i++) {
                sections[i].element().visible = i == s;
            }
        });
        return stack;
    }

    private Layout buildProfileSection() {
        return sectionPanel("PROFILE", 6,
            inputRow("Display Name", displayNameSg),
            inputRow("Email", emailSg),
            inputRow("Bio", bioSg),
            displayRow("Member Since", memberSinceSg),
            displayRow("Account ID", accountIdSg),
            displayRow("Last Login", lastLoginSg)
        );
    }

    private Layout buildGeneralSection() {
        return sectionPanel("GENERAL", 8,
            buttonRow("Language", languageSg, new String[]{"English", "Tiếng Việt", "日本語", "中文"}),
            buttonRow("Theme", themeSg, new String[]{"Dark", "Light", "System"}),
            sliderRow("UI Scale", uiScaleSg, 0.5f, 2f, 0.25f),
            sliderRow("Animation Speed", animSpeedSg, 0f, 2f, 0.25f),
            toggleRow("Reduce Motion", reduceMotionSg),
            toggleRow("Compact Mode", compactSg),
            toggleRow("Auto-save", autoSaveSg),
            toggleRow("Telemetry", telemetrySg)
        );
    }

    private Layout buildEditorSection() {
        return sectionPanel("EDITOR", 8,
            sliderRow("Font Size", fontSizeSg, 8f, 32f, 1f),
            sliderRow("Tab Size", tabSizeSg, 1f, 8f, 1f),
            toggleRow("Word Wrap", wordWrapSg),
            toggleRow("Line Numbers", lineNumbersSg),
            toggleRow("Indent Guides", indentGuidesSg),
            toggleRow("Auto-pair Brackets", autoPairSg),
            toggleRow("Minimap", minimapSg),
            sliderRow("Undo Limit", undoLimitSg, 10f, 500f, 10f)
        );
    }

    private Layout buildNotificationsSection() {
        return sectionPanel("NOTIFICATIONS", 5,
            toggleRow("Email Alerts", emailAlertsSg),
            toggleRow("Push Notifications", pushNotifySg),
            toggleRow("Sound", soundSg),
            toggleRow("Desktop Notifications", desktopNotifySg),
            toggleRow("Digest Mode", digestSg),
            toggleRow("Quiet Hours", quietHoursSg)
        );
    }

    private Layout buildShortcutsSection() {
        String[][] keys = {
            {"Save", "Ctrl+S"}, {"Undo", "Ctrl+Z"}, {"Redo", "Ctrl+Shift+Z"},
            {"Find", "Ctrl+F"}, {"Replace", "Ctrl+H"}, {"New File", "Ctrl+N"},
            {"Open", "Ctrl+O"}, {"Preferences", "Ctrl+,"},
        };
        var rows = new Component[keys.length];
        for (int i = 0; i < keys.length; i++) {
            int idx = i;
            rows[i] = Layout.build().row().style(s -> s.gap(8).justify(Justify.BETWEEN))
                .children(
                    Label.build().text(keys[i][0]).style(s -> s.textColor(Theme.TEXT_PRIMARY)).build(),
                    Button.build()
                        .child(Label.build().text(keys[i][1]).style(s -> s.textColor(Theme.TEXT_GHOST)).build())
                        .clicked(() -> Log.info("Shortcut: " + keys[idx][0]))
                        .style(s -> s.ghostVariant())
                        .size(s -> s.w(160f))
                        .build()
                )
                .build();
        }
        return sectionPanel("SHORTCUTS", keys.length, rows);
    }

    private Layout buildAboutSection() {
        return sectionPanel("ABOUT", 6,
            displayRow("Version", Signal.of("2.4.1")),
            displayRow("Build", Signal.of("2026.06.08")),
            displayRow("Author", Signal.of("meohexa1a")),
            displayRow("License", Signal.of("MIT License")),
            displayRow("Engine", Signal.of("Mindustry v158")),
            Button.build()
                .child(Label.build().text("☠ Delete Account").build())
                .clicked(() -> Log.info("Account deleted!"))
                .style(s -> s.dangerVariant())
                .build()
        );
    }

    private void resetAll() {
        displayNameSg.set("John Doe");
        emailSg.set("john@example.com");
        bioSg.set("Cat enthusiast & game developer");
        languageSg.set("English");
        themeSg.set("Dark");
        uiScaleSg.set(1.0f);
        animSpeedSg.set(1.0f);
        reduceMotionSg.set(false);
        compactSg.set(false);
        autoSaveSg.set(true);
        telemetrySg.set(false);
        fontSizeSg.set(14f);
        tabSizeSg.set(4f);
        wordWrapSg.set(true);
        lineNumbersSg.set(true);
        indentGuidesSg.set(true);
        autoPairSg.set(true);
        minimapSg.set(true);
        undoLimitSg.set(100f);
        emailAlertsSg.set(true);
        pushNotifySg.set(false);
        soundSg.set(true);
        desktopNotifySg.set(true);
        digestSg.set(false);
        quietHoursSg.set(false);
    }

    private static Layout sectionPanel(String title, int count, Component... fields) {
        var items = new Component[count + 2];
        items[0] = Label.build().text(title).style(s -> s.textColor(Theme.TEXT_BRIGHT).fontScale(0.7f)).build();
        items[1] = Layout.build().row().size(sz -> sz.h(1f)).build();
        System.arraycopy(fields, 0, items, 2, count);
        var content = Layout.build().col().style(s -> s.gap(10).p(16))
            .children(items)
            .build();
        return Layout.build().col()
            .add(Stack.build()
                .child(SDFRoundedBox.build().color(Theme.SURFACE).cornerRadius(10f).build())
                .child(content)
                .build())
            .build();
    }

    private static Layout toggleRow(String label, Signal<Boolean> signal) {
        return Layout.build().row().style(s -> s.gap(8).justify(Justify.BETWEEN))
            .children(
                Label.build().text(label).style(s -> s.textColor(Theme.TEXT_PRIMARY)).build(),
                Checkbox.build().bind(signal).build()
            )
            .build();
    }

    private static Layout sliderRow(String label, Signal<Float> signal, float min, float max, float step) {
        return Layout.build().row().style(s -> s.gap(8).justify(Justify.BETWEEN))
            .children(
                Label.build().text(label).style(s -> s.textColor(Theme.TEXT_PRIMARY)).build(),
                Layout.build().row().style(s -> s.gap(8))
                    .children(
                        SliderField.build().range(min, max, step).bind(signal).size(sz -> sz.w(120f)).build(),
                        Label.build().text(Signal.computed(() -> String.valueOf(Math.round(signal.get() * 10f) / 10f)))
                            .style(s -> s.textColor(Theme.TEXT_GHOST)).size(sz -> sz.w(32f)).build()
                    )
                    .build()
            )
            .build();
    }

    private static Layout inputRow(String label, Signal<String> signal) {
        return Layout.build().row().style(s -> s.gap(8).justify(Justify.BETWEEN))
            .children(
                Label.build().text(label).style(s -> s.textColor(Theme.TEXT_PRIMARY)).build(),
                InputField.build().bind(signal).size(sz -> sz.w(200f)).build()
            )
            .build();
    }

    private static Layout displayRow(String label, Signal<String> value) {
        return Layout.build().row().style(s -> s.gap(8).justify(Justify.BETWEEN))
            .children(
                Label.build().text(label).style(s -> s.textColor(Theme.TEXT_SECONDARY)).build(),
                Label.build().text(value).style(s -> s.textColor(Theme.TEXT_BRIGHT)).build()
            )
            .build();
    }

    private static Layout buttonRow(String label, Signal<String> signal, String[] options) {
        return Layout.build().row().style(s -> s.gap(8).justify(Justify.BETWEEN))
            .children(
                Label.build().text(label).style(s -> s.textColor(Theme.TEXT_PRIMARY)).build(),
                Button.build()
                    .child(Label.build().text(Signal.computed(() -> {
                        int i = java.util.Arrays.asList(options).indexOf(signal.get());
                        return signal.get() + (i >= 0 ? "  " + (i + 1) + "/" + options.length : "");
                    })).build())
                    .clicked(() -> {
                        String cur = signal.get();
                        for (int i = 0; i < options.length; i++) {
                            if (options[i].equals(cur)) {
                                signal.set(options[(i + 1) % options.length]);
                                return;
                            }
                        }
                        signal.set(options[0]);
                    })
                    .style(s -> s.ghostVariant())
                    .size(sz -> sz.w(160f))
                    .build()
            )
            .build();
    }

    private static Layout buildDivider(float h) {
        return Layout.build().row().size(s -> s.h(h)).build();
    }

    private static Layout buildDividerV() {
        return Layout.build().col().size(s -> s.w(1f)).build();
    }
}
