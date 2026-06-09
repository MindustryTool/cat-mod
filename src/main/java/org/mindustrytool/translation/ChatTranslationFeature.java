package org.mindustrytool.translation;

import javax.inject.Inject;
import javax.inject.Singleton;

import arc.Core;
import arc.func.Cons;
import arc.util.Log;
import arc.util.Strings;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.core.NetClient;
import mindustry.gen.SendMessageCallPacket;
import mindustry.gen.SendMessageCallPacket2;
import java.util.concurrent.CompletableFuture;

@Singleton
public class ChatTranslationFeature {
    private static ChatTranslationFeature instance;

    private final Seq<TranslationProvider> providers = new Seq<>();
    private final TranslationProvider defaultTranslationProvider;
    private String lastError = null;
    private TranslationProvider currentProvider;

    public static ChatTranslationFeature getInstance() {
        return instance;
    }

    @Inject
    public ChatTranslationFeature() {
        instance = this;
        this.defaultTranslationProvider = new MindustryToolTranslationProvider();
        this.currentProvider = defaultTranslationProvider;
    }

    public void init() {
        // Register packet replacements
        try {
            Seq<arc.func.Prov<? extends mindustry.net.Packet>> packetProvs = arc.util.Reflect.get(Vars.net, "packetProvs");
            packetProvs.replace(packet -> {
                Class<?> clazz = packet.get().getClass();
                if (clazz == SendMessageCallPacket.class) {
                    Log.info("Replacing packet SendMessageCallPacket for translation");
                    return SendTranslatedMessageCallPacket::new;
                } else if (clazz == SendMessageCallPacket2.class) {
                    Log.info("Replacing packet SendMessageCallPacket2 for translation");
                    return SendTranslatedMessageCallPacket2::new;
                }
                return packet;
            });
        } catch (Exception e) {
            Log.err("Failed to register packet replacement for translation", e);
        }

        providers.add(defaultTranslationProvider);
        providers.add(new GeminiTranslationProvider());
        providers.add(new DeepLTranslationProvider());

        providers.each(TranslationProvider::init);

        loadProvider();
    }

    public boolean isEnabled() {
        return Core.settings.getBool("mindustrytool.chat-translation.enabled", false);
    }

    public void setEnabled(boolean enabled) {
        Core.settings.put("mindustrytool.chat-translation.enabled", enabled);
    }

    public void loadProvider() {
        String id = ChatTranslationConfig.getProviderId();
        TranslationProvider found = providers.find(p -> p.getId().equals(id));
        if (found != null) {
            currentProvider = found;
        } else {
            currentProvider = defaultTranslationProvider;
        }
    }

    public void handleMessage(String message, Cons<String> cons) {
        if (!isEnabled()) {
            cons.get(message);
            return;
        }

        translateContent(message)
                .thenAccept(translated -> {
                    boolean showOriginal = Core.settings.getBool(ChatTranslationConfig.SHOW_ORIGINAL, true);
                    if (showOriginal) {
                        cons.get(Strings.format("@ [gold](@)[white]", message, translated));
                    } else {
                        cons.get(translated);
                    }
                })
                .exceptionally(e -> {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    lastError = cause.getMessage();

                    String formatted = Strings.format("@\n\n[scarlet]@[white]\n\n", message,
                            Core.bundle.get("chat-translation.error.prefix", "Translation Error: ") + cause.getMessage());

                    cons.get(formatted);
                    Log.err("Translation error", cause);
                    return null;
                });
    }

    public CompletableFuture<String> translateContent(String message) {
        if (!isEnabled()) {
            throw new IllegalArgumentException("ChatTranslationFeature is not enabled");
        }

        return currentProvider.translate(Strings.stripColors(message))
                .whenComplete((translated, error) -> {
                    if (error != null) {
                        Throwable cause = error.getCause() != null ? error.getCause() : error;
                        lastError = cause.getMessage();
                    }
                });
    }

    public Seq<TranslationProvider> getProviders() {
        return providers;
    }

    public TranslationProvider getCurrentProvider() {
        return currentProvider;
    }

    public void setCurrentProvider(TranslationProvider provider) {
        this.currentProvider = provider;
        ChatTranslationConfig.setProviderId(provider.getId());
    }

    public String getLastError() {
        return lastError;
    }

    public static class SendTranslatedMessageCallPacket extends SendMessageCallPacket {
        @Override
        public void handleClient() {
            if (instance != null) {
                instance.handleMessage(this.message, translated -> {
                    NetClient.sendMessage(translated);
                });
            } else {
                NetClient.sendMessage(this.message);
            }
        }
    }

    public static class SendTranslatedMessageCallPacket2 extends SendMessageCallPacket2 {
        @Override
        public void handleClient() {
            if (instance != null) {
                if (Vars.player != this.playersender) {
                    instance.handleMessage(this.message, translated -> {
                        NetClient.sendMessage(translated, this.unformatted, this.playersender);
                    });
                } else {
                    NetClient.sendMessage(this.message, this.unformatted, this.playersender);
                }
            } else {
                NetClient.sendMessage(this.message, this.unformatted, this.playersender);
            }
        }
    }
}
