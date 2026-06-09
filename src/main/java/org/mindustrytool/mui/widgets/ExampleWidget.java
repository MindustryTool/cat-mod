package org.mindustrytool.mui.widgets;

import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Align;
import arc.input.KeyCode;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.mindustrytool.SettingsService;
import org.mindustrytool.signal.Effect;
import org.mindustrytool.signal.Signal;
import org.mindustrytool.ui.components.Button;
import org.mindustrytool.ui.components.Checkbox;
import org.mindustrytool.ui.components.Component;
import org.mindustrytool.ui.components.CustomUIComponent;
import org.mindustrytool.ui.components.InputField;
import org.mindustrytool.ui.components.Label;
import org.mindustrytool.ui.components.Layout;
import org.mindustrytool.ui.components.ScrollPane;
import org.mindustrytool.ui.components.SliderField;
import org.mindustrytool.ui.components.Stack;
import org.mindustrytool.ui.spec.LayoutSpec.Justify;
import org.mindustrytool.ui.spec.LayoutSpec.Items;
import org.mindustrytool.ui.style.Theme;
import org.mindustrytool.ui.util.CodeFormatter;

@Singleton
public class ExampleWidget {

    private final SettingsService settings;
    private final Signal<Integer> activeTab = Signal.of(0);
    private final Seq<Runnable> subs = new Seq<>();
    private Layout rootLayout;
    private static final float pw = 1200f;

    @Inject
    public ExampleWidget(SettingsService settings) {
        this.settings = settings;
    }

    public Layout build() {
        if (rootLayout != null) return rootLayout;

        var tabBackgrounds = new CustomUIComponent[3];
        var tabLabels = new Label[3];
        String[] tabNames = {"Preferences", "Code Formatter", "Chat & Translation"};
        for (int i = 0; i < 3; i++) {
            int idx = i;
            tabBackgrounds[i] = CustomUIComponent.of().style(s -> s.color(Theme.SURFACE).cornerRadius(6f));
            tabLabels[i] = Label.of().style(s -> s.text(tabNames[idx]).fontScale(1.05f));
        }

        var tabs = new Stack[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            tabs[i] = Stack.of()
                .child(tabBackgrounds[i])
                .child(Layout.of().style(s -> s.col().items(Items.CENTER).justify(Justify.CENTER))
                    .child(tabLabels[idx]));
            tabs[i].element().addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                    activeTab.set(idx);
                    return true;
                }
            });
        }

        var tabRow = Layout.of()
            .style(s -> s.row().gap(4).p(4).size(sz -> sz.w(750f).h(40f)))
            .child(Layout.of().style(s -> s.col().size(sz -> sz.grow())).child(tabs[0]))
            .child(Layout.of().style(s -> s.col().size(sz -> sz.grow())).child(tabs[1]))
            .child(Layout.of().style(s -> s.col().size(sz -> sz.grow())).child(tabs[2]));

        var segmentedControl = Stack.of()
            .style(s -> s.size(sz -> sz.w(750f).h(40f)))
            .child(CustomUIComponent.of().style(s -> s.color(Theme.BACKGROUND).cornerRadius(8f)))
            .child(tabRow);

        var tabHeaderBar = Layout.of()
            .style(s -> s.row().justify(Justify.CENTER).p(8, 16))
            .child(segmentedControl);

        var prefContent = buildPrefContent();
        var formatterContent = buildFormatterContent();
        var chatContent = buildChatContent();

        var preferencesTab = ScrollPane.of()
            .style(s -> s.disableX(true).fadeScrollBars(true).scrollBarsOnTop(true))
            .child(prefContent);

        var formatterTab = ScrollPane.of()
            .style(s -> s.disableX(true).fadeScrollBars(true).scrollBarsOnTop(true))
            .child(formatterContent);

        var chatTab = ScrollPane.of()
            .style(s -> s.disableX(true).fadeScrollBars(true).scrollBarsOnTop(true))
            .child(chatContent);

        var tabPanes = Seq.with(preferencesTab, formatterTab, chatTab);

        var contentStack = Stack.of()
            .child(preferencesTab)
            .child(formatterTab)
            .child(chatTab);

        subs.add(new Effect(() -> {
            int currentTab = activeTab.get();
            for (int i = 0; i < 3; i++) {
                tabBackgrounds[i].element().visible = (i == currentTab);
                tabPanes.get(i).element().visible = (i == currentTab);
            }
            for (int i = 0; i < 3; i++) {
                int idx = i;
                tabLabels[idx].style(s -> s.textColor(idx == currentTab ? Theme.TEXT_BRIGHT : Theme.TEXT_GHOST));
            }
            WidgetGroup stackGroup = (WidgetGroup) contentStack.element();
            stackGroup.invalidateHierarchy();
        })::dispose);

        this.rootLayout = Layout.of()
            .style(s -> s.col().gap(0))
            .child(tabHeaderBar)
            .child(divider(1f))
            .child(contentStack);

        return rootLayout;
    }

    public void dispose() {
        subs.each(Runnable::run);
        subs.clear();
        if (rootLayout != null) rootLayout.dispose();
    }

    private Layout buildPrefContent() {
        return Layout.of().style(s -> s.col().gap(16).p(16).minW(pw - 40))
            .child(Layout.of().style(s -> s.row().gap(16))
                .child(profileSection().size(sz -> sz.w(562f)))
                .child(generalSection().size(sz -> sz.w(562f))))
            .child(Layout.of().style(s -> s.row().gap(16))
                .child(notificationsSection().size(sz -> sz.w(562f)))
                .child(shortcutsSection().size(sz -> sz.w(562f))))
            .child(Layout.of().style(s -> s.row().gap(16))
                .child(aboutSection().size(sz -> sz.w(1140f))));
    }

    private Layout buildFormatterContent() {
        return Layout.of().style(s -> s.col().gap(12).p(16).minW(pw - 40))
            .child(Layout.of().style(s -> s.row().gap(20))
                .child(editorSection().size(sz -> sz.w(540f)))
                .child(buildCodePreviewSection().size(sz -> sz.w(580f))));
    }

    private Layout buildChatContent() {
        return Layout.of().style(s -> s.col().gap(16).p(16).minW(pw - 40))
            .child(Layout.of().style(s -> s.row().gap(16))
                .child(chatOverlaySection().size(sz -> sz.w(562f)))
                .child(translationSection().size(sz -> sz.w(562f))))
            .child(Layout.of().style(s -> s.row().gap(16))
                .child(geminiSection().size(sz -> sz.w(562f)))
                .child(deepLSection().size(sz -> sz.w(562f))))
            .child(Layout.of().style(s -> s.row().gap(16))
                .child(mindustryToolSection().size(sz -> sz.w(1140f))));
    }

    private Stack sectionPanel(String title, Component... fields) {
        var content = Layout.of().style(s -> s.col().gap(10).p(16));
        content.child(Label.of().style(s -> s.text(title).textColor(Theme.TEXT_BRIGHT).fontScale(0.7f).textAlign(Align.left)));
        content.child(divider(1f));
        for (Component f : fields) content.child(f);
        return Stack.of()
            .child(CustomUIComponent.of().style(s -> s.color(Theme.SURFACE).cornerRadius(10f)))
            .child(content);
    }

    // Section content builders

    private Stack profileSection() {
        return sectionPanel("PROFILE DETAILS",
            inputRow("Display Name", "Your public profile nickname displayed in servers", settings.getDisplayNameSg()),
            inputRow("Email Address", "Email associated with your developer profile", settings.getEmailSg()),
            inputRow("Biography", "Brief description about yourself or your modding career", settings.getBioSg()),
            displayRow("Member Since", "The date you registered with the Neko community", settings.getMemberSinceSg()),
            displayRow("Account ID", "Unique cryptographic identifier of your profile", settings.getAccountIdSg()),
            displayRow("Last Login Time", "Timestamp of your most recent authenticated login", settings.getLastLoginSg())
        );
    }

    private Stack generalSection() {
        return sectionPanel("GENERAL PREFERENCES",
            buttonRow("Language", "Select your preferred user interface localization", settings.getLanguageSg(), new String[]{"English", "Tiếng Việt", "日本語", "中文"}),
            buttonRow("Theme Mode", "Set preferred window styling and visual design mode", settings.getThemeSg(), new String[]{"Dark", "Light", "System"}),
            sliderRow("UI Scale", "Adjust the overall scale of interface components", settings.getUiScaleSg(), 0.5f, 2f, 0.25f),
            sliderRow("Animation Speed", "Multiplier for visual transitions and micro-animations", settings.getAnimSpeedSg(), 0f, 2f, 0.25f),
            toggleRow("Reduce Motion", "Disable complex slide animations to save device resources", settings.getReduceMotionSg()),
            toggleRow("Compact Layout", "Compress padding and list row heights for higher density", settings.getCompactSg()),
            toggleRow("Auto-save Config", "Periodically save changed settings automatically", settings.getAutoSaveSg()),
            toggleRow("Telemetry Stats", "Anonymously share crash reports to help resolve bugs", settings.getTelemetrySg())
        );
    }

    private Stack notificationsSection() {
        return sectionPanel("NOTIFICATIONS",
            toggleRow("Email Alerts", "Receive security updates and newsletters via email", settings.getEmailAlertsSg()),
            toggleRow("Push Notifications", "Allow system-level desktop push alerts for events", settings.getPushNotifySg()),
            toggleRow("Sound Effects", "Play audial alerts when receiving notifications", settings.getSoundSg()),
            toggleRow("Desktop Popups", "Show floating overlay messages on the desktop", settings.getDesktopNotifySg()),
            toggleRow("Digest Mode", "Group alerts into a single daily summary email", settings.getDigestSg()),
            toggleRow("Quiet Hours", "Mute all alert sounds during designated hours", settings.getQuietHoursSg())
        );
    }

    private Stack shortcutsSection() {
        return sectionPanel("SHORTCUTS",
            shortcutRow("Save Configuration", "Ctrl + S"),
            shortcutRow("Undo Action", "Ctrl + Z"),
            shortcutRow("Redo Action", "Ctrl + Shift + Z"),
            shortcutRow("Find Content", "Ctrl + F"),
            shortcutRow("Replace Content", "Ctrl + H"),
            shortcutRow("New File", "Ctrl + N"),
            shortcutRow("Open Document", "Ctrl + O"),
            shortcutRow("Open Preferences", "Ctrl + ,")
        );
    }

    private Stack aboutSection() {
        return sectionPanel("ABOUT MOD",
            displayRow("Mod Version", "Currently installed release of Neko mod", Signal.of("2.4.1")),
            displayRow("Build Number", "Timestamp compilation value of this release", Signal.of("2026.06.08")),
            displayRow("Developer", "Main author and maintainer of the neko codebase", Signal.of("meohexa1a")),
            displayRow("Code License", "Legal open-source license governing distribution", Signal.of("MIT License")),
            displayRow("Game Engine", "Underlying Mindustry framework version dependency", Signal.of("Mindustry v158")),
            Button.of()
                .child(Label.of().style(s -> s.text("☠ Delete Developer Account")))
                .listener(l -> l.changed(() -> Log.info("Account deleted!")))
                .style(s -> s.dangerVariant())
        );
    }

    // Chat/Translation section builders

    private Stack chatOverlaySection() {
        return sectionPanel("GLOBAL CHAT OVERLAY",
            sliderRow("Overlay Scale", "Scale factor of the overlay UI elements", settings.getChatScaleSg(), 0.5f, 2f, 0.25f),
            sliderRow("Overlay Opacity", "Transparency of the chat HUD container", settings.getChatOpacitySg(), 0.1f, 1f, 0.1f),
            sliderRow("Overlay Width", "Width ratio relative to the game window size", settings.getChatWidthSg(), 0.2f, 1f, 0.1f),
            sliderRow("Overlay Height", "Height ratio relative to the game window size", settings.getChatHeightSg(), 0.2f, 1f, 0.1f),
            toggleRow("Status Syncing", "Synchronize overlay status/collapsed state with game UI state", settings.getChatStatusSg())
        );
    }

    private Stack translationSection() {
        return sectionPanel("CHAT TRANSLATION CORE",
            toggleRow("Enable Translation", "Intercept incoming game chat and translate client-side", settings.getChatTranslationEnabledSg()),
            toggleRow("Show Original Message", "Render original untranslated message alongside translation", settings.getShowOriginalSg()),
            buttonRow("Active Provider", "Select the translation service backend provider", settings.getTranslationProviderSg(), new String[]{"noop", "mindustrytool", "gemini", "deepl"})
        );
    }

    private Stack geminiSection() {
        return sectionPanel("GOOGLE GEMINI TRANSLATOR",
            inputRow("Gemini API Key", "API key from Google AI Studio (starts with AIza)", settings.getGeminiApiKeySg()),
            buttonRow("Gemini Model", "Model version to use for translation generation", settings.getGeminiModelSg(), new String[]{"gemini-2.5-flash", "gemini-2.5-flash-lite", "gemini-2.5-pro", "gemini-2.0-flash", "gemini-2.0-flash-lite", "gemini-3-pro-preview", "gemini-3-flash-preview"}),
            sliderRow("Request Timeout", "API request timeout in seconds", settings.getGeminiTimeoutSg(), 5f, 20f, 1f),
            sliderRow("Context History", "Number of previous chat messages to send as history", settings.getGeminiMaxHistorySg(), 0f, 10f, 1f)
        );
    }

    private Stack deepLSection() {
        return sectionPanel("DEEPL TRANSLATOR",
            inputRow("DeepL API Key", "Authentication key for DeepL translation service", settings.getDeeplApiKeySg()),
            sliderRow("Request Timeout", "API request timeout in seconds", settings.getDeeplTimeoutSg(), 2f, 20f, 1f)
        );
    }

    private Stack mindustryToolSection() {
        return sectionPanel("MINDUSTRY TOOL OFFICIAL TRANSLATOR",
            sliderRow("Request Timeout", "Backend API request timeout in seconds", settings.getMindustryToolTimeoutSg(), 30f, 120f, 10f)
        );
    }

    // Editor + Code Preview sections

    private Stack editorSection() {
        return sectionPanel("EDITOR CONFIGURATION",
            sliderRow("Font Size", "Adjust editor line text character dimension size", settings.getFontSizeSg(), 8f, 32f, 2f),
            sliderRow("Tab Size", "Specify number of spaces added by indent tabs", settings.getTabSizeSg(), 2f, 8f, 2f),
            toggleRow("Word Wrap", "Force long lines of code to break onto new lines", settings.getWordWrapSg()),
            toggleRow("Line Numbers", "Prepend absolute line numbers to text lines", settings.getLineNumbersSg()),
            toggleRow("Indent Guides", "Render vertical guide bars matching indent spaces", settings.getIndentGuidesSg()),
            toggleRow("Auto-pair Brackets", "Automatically insert matching closing brackets", settings.getAutoPairSg()),
            toggleRow("Minimap Preview", "Show high-level visual code preview sidebar", settings.getMinimapSg()),
            sliderRow("Undo Limit", "Limit total saved action steps history size", settings.getUndoLimitSg(), 50f, 500f, 50f)
        );
    }

    private Layout buildCodePreviewSection() {
        int tabSize = Math.round(settings.getTabSizeSg().get());
        boolean lineNumbers = settings.getLineNumbersSg().get();
        boolean indentGuides = settings.getIndentGuidesSg().get();
        var previewText = CodeFormatter.formatCode(tabSize, lineNumbers, indentGuides);

        var macHeader = Layout.of()
            .style(s -> s.row().justify(Justify.BETWEEN).p(8, 12))
            .child(Label.of().style(s -> s.text("[#ff5555]●[] [#ffb86c]●[] [#50fa7b]●[]").fontScale(0.85f)))
            .child(Label.of().style(s -> s.text("NekoContentMod.java ⬤").textColor(Theme.TEXT_SECONDARY).fontScale(0.8f)))
            .child(Label.of().style(s -> s.text("       ")));

        var previewLabel = Label.of()
            .style(s -> s.text(previewText).textColor(Theme.TEXT_BRIGHT).fontScale(0.75f).wrap(false).textAlign(Align.topLeft));

        var codeBoxContent = Layout.of()
            .style(s -> s.col().p(16).size(sz -> sz.minW(548f).minH(500f)))
            .child(previewLabel);

        var editorBody = Layout.of()
            .style(s -> s.col().gap(0))
            .child(macHeader)
            .child(divider(1f))
            .child(codeBoxContent);

        var codeBox = Stack.of()
            .child(CustomUIComponent.of().style(s -> s.color(Theme.DRACULA_BG).cornerRadius(8f)))
            .child(editorBody);

        return Layout.of()
            .style(s -> s.col().gap(10).p(0, 16).size(sz -> sz.minH(550f)))
            .child(Label.of().style(s -> s.text("LIVE PLAYGROUND CODE PREVIEW").textColor(Theme.TEXT_BRIGHT).fontScale(0.7f).textAlign(Align.left)))
            .child(divider(1f))
            .child(codeBox);
    }

    // Field row generators

    private Layout toggleRow(String title, String desc, Signal<Boolean> signal) {
        return formRow(title, desc,
            Checkbox.of()
                .style(s -> s.checked(signal.get()))
                .listener(l -> l.changed(v -> signal.set(v)))
        );
    }

    private Layout sliderRow(String label, String desc, Signal<Float> signal, float min, float max, float step) {
        return formRow(label, desc,
            SliderField.of()
                .style(s -> s.range(min, max).step(step).value(signal.get()))
                .listener(l -> l.changed(v -> signal.set(v)))
        );
    }

    private Layout inputRow(String label, String desc, Signal<String> signal) {
        return formRow(label, desc,
            InputField.of()
                .style(s -> s.text(signal.get()))
                .listener(l -> l.changed(v -> signal.set(v)))
        );
    }

    private Layout displayRow(String label, String desc, Signal<String> value) {
        return formRow(label, desc,
            Label.of().style(s -> s.text(value.get()).textColor(Theme.TEXT_BRIGHT))
        );
    }

    private Layout buttonRow(String label, String desc, Signal<String> signal, String[] options) {
        return formRow(label, desc,
            Button.of()
                .child(Label.of().style(s -> s.text(signal.get() + "  1/" + options.length)))
                .listener(l -> l.changed(() -> {
                    String cur = signal.get();
                    for (int i = 0; i < options.length; i++) {
                        if (options[i].equals(cur)) {
                            signal.set(options[(i + 1) % options.length]);
                            return;
                        }
                    }
                    signal.set(options[0]);
                }))
                .style(s -> s.ghostVariant().size(sz -> sz.w(160f)))
        );
    }

    private Layout shortcutRow(String label, String keys) {
        return Layout.of()
            .style(s -> s.row().gap(8).justify(Justify.BETWEEN))
            .child(Label.of().style(s -> s.text(label).textColor(Theme.TEXT_PRIMARY).textAlign(Align.left)))
            .child(
                Button.of()
                    .child(Label.of().style(s -> s.text(keys).textColor(Theme.TEXT_GHOST)))
                    .listener(l -> l.changed(() -> Log.info("Shortcut: " + label)))
                    .style(s -> s.ghostVariant().size(sz -> sz.w(160f)))
            );
    }

    // Helpers

    private static Layout formRow(String title, String desc, Component control) {
        return Layout.of()
            .style(s -> s.row().gap(8).justify(Justify.BETWEEN).p(4, 0).size(sz -> sz.growX()))
            .child(
                Layout.of()
                    .style(s -> s.col().gap(4).size(sz -> sz.growX()))
                    .child(Label.of().style(s -> s.text(title).textColor(Theme.TEXT_PRIMARY).fontScale(0.95f).wrap(true).textAlign(Align.left)))
                    .child(Label.of().style(s -> s.text(desc).textColor(Theme.TEXT_SECONDARY).fontScale(0.75f).wrap(true).textAlign(Align.left)))
            )
            .child(control);
    }

    private static Component divider(float h) {
        return CustomUIComponent.of().style(s -> s.color(Theme.BORDER).size(sz -> sz.h(h)));
    }
}
