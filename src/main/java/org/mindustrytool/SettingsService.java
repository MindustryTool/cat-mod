package org.mindustrytool;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import org.mindustrytool.signal.Signal;

@Singleton
@Getter
public class SettingsService {

    private final Signal<Boolean> dirty = Signal.of(false);

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

    // Chat & Translation Settings
    private final Signal<Boolean> chatTranslationEnabledSg = Signal.of(arc.Core.settings.getBool("mindustrytool.chat-translation.enabled", false));
    private final Signal<Boolean> showOriginalSg = Signal.of(arc.Core.settings.getBool(org.mindustrytool.translation.ChatTranslationConfig.SHOW_ORIGINAL, true));
    private final Signal<String> translationProviderSg = Signal.of(arc.Core.settings.getString(org.mindustrytool.translation.ChatTranslationConfig.PROVIDER, "noop"));

    private final Signal<String> geminiApiKeySg = Signal.of(arc.Core.settings.getString(org.mindustrytool.translation.ChatTranslationConfig.GEMINI_API_KEY, ""));
    private final Signal<String> geminiModelSg = Signal.of(arc.Core.settings.getString(org.mindustrytool.translation.ChatTranslationConfig.GEMINI_MODEL, "gemini-2.5-flash"));
    private final Signal<Float> geminiTimeoutSg = Signal.of((float) arc.Core.settings.getInt(org.mindustrytool.translation.ChatTranslationConfig.GEMINI_TIMEOUT, 10));
    private final Signal<Float> geminiMaxHistorySg = Signal.of((float) arc.Core.settings.getInt(org.mindustrytool.translation.ChatTranslationConfig.GEMINI_MAX_HISTORY, 0));

    private final Signal<String> deeplApiKeySg = Signal.of(arc.Core.settings.getString(org.mindustrytool.translation.ChatTranslationConfig.DEEPL_API_KEY, ""));
    private final Signal<Float> deeplTimeoutSg = Signal.of((float) arc.Core.settings.getInt(org.mindustrytool.translation.ChatTranslationConfig.DEEPL_TIMEOUT, 10));

    private final Signal<Float> mindustryToolTimeoutSg = Signal.of((float) arc.Core.settings.getInt(org.mindustrytool.translation.ChatTranslationConfig.MINDUSTRYTOOL_TIMEOUT, 120));

    private final Signal<Float> chatScaleSg = Signal.of(org.mindustrytool.chat.ChatConfig.scale());
    private final Signal<Float> chatOpacitySg = Signal.of(org.mindustrytool.chat.ChatConfig.opacity());
    private final Signal<Float> chatWidthSg = Signal.of(org.mindustrytool.chat.ChatConfig.width());
    private final Signal<Float> chatHeightSg = Signal.of(org.mindustrytool.chat.ChatConfig.height());
    private final Signal<Boolean> chatStatusSg = Signal.of(org.mindustrytool.chat.ChatConfig.status());

    @Inject
    public SettingsService() {
    }

    public void save() {
        arc.Core.settings.put("mindustrytool.chat-translation.enabled", chatTranslationEnabledSg.get());
        arc.Core.settings.put(org.mindustrytool.translation.ChatTranslationConfig.SHOW_ORIGINAL, showOriginalSg.get());
        arc.Core.settings.put(org.mindustrytool.translation.ChatTranslationConfig.PROVIDER, translationProviderSg.get());

        arc.Core.settings.put(org.mindustrytool.translation.ChatTranslationConfig.GEMINI_API_KEY, geminiApiKeySg.get());
        arc.Core.settings.put(org.mindustrytool.translation.ChatTranslationConfig.GEMINI_MODEL, geminiModelSg.get());
        arc.Core.settings.put(org.mindustrytool.translation.ChatTranslationConfig.GEMINI_TIMEOUT, Math.round(geminiTimeoutSg.get()));
        arc.Core.settings.put(org.mindustrytool.translation.ChatTranslationConfig.GEMINI_MAX_HISTORY, Math.round(geminiMaxHistorySg.get()));

        arc.Core.settings.put(org.mindustrytool.translation.ChatTranslationConfig.DEEPL_API_KEY, deeplApiKeySg.get());
        arc.Core.settings.put(org.mindustrytool.translation.ChatTranslationConfig.DEEPL_TIMEOUT, Math.round(deeplTimeoutSg.get()));

        arc.Core.settings.put(org.mindustrytool.translation.ChatTranslationConfig.MINDUSTRYTOOL_TIMEOUT, Math.round(mindustryToolTimeoutSg.get()));

        org.mindustrytool.chat.ChatConfig.scale(chatScaleSg.get());
        org.mindustrytool.chat.ChatConfig.opacity(chatOpacitySg.get());
        org.mindustrytool.chat.ChatConfig.width(chatWidthSg.get());
        org.mindustrytool.chat.ChatConfig.height(chatHeightSg.get());
        org.mindustrytool.chat.ChatConfig.status(chatStatusSg.get());

        arc.Core.settings.forceSave();

        // Notify chat services to reload config
        if (org.mindustrytool.translation.ChatTranslationFeature.getInstance() != null) {
            org.mindustrytool.translation.ChatTranslationFeature.getInstance().loadProvider();
        }
        if (org.mindustrytool.chat.ui.ChatOverlay.getInstance() != null) {
            org.mindustrytool.chat.ui.ChatOverlay.getInstance().rebuild();
        }
    }

    public void resetAll() {
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

        chatTranslationEnabledSg.set(false);
        showOriginalSg.set(true);
        translationProviderSg.set("noop");
        geminiApiKeySg.set("");
        geminiModelSg.set("gemini-2.5-flash");
        geminiTimeoutSg.set(10f);
        geminiMaxHistorySg.set(0f);
        deeplApiKeySg.set("");
        deeplTimeoutSg.set(10f);
        mindustryToolTimeoutSg.set(120f);
        chatScaleSg.set(1f);
        chatOpacitySg.set(1f);
        chatWidthSg.set(0.7f);
        chatHeightSg.set(0.9f);
        chatStatusSg.set(true);
    }
}
