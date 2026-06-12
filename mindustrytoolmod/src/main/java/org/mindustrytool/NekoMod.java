package org.mindustrytool;

import arc.Core;

import lombok.Getter;

import mindustry.Vars;
import arc.Events;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.mod.Mod;

import org.codejargon.feather.Feather;

import org.mindustrytool.mdtui.screen.DemoUI;

import lombok.extern.slf4j.Slf4j;

/**
 * Entry point for the Neko Content Mod.
 * <p>
 * Bootstraps the Feather DI container on {@link #init()} and delegates
 * further initialization to {@link ModManager}.
 */
@Slf4j
public class NekoMod extends Mod {
    private static @Getter Feather feather;

    @Override
    public void init() {
        log.info("Bootstrapping NekoMod with Feather DI...");

        feather = Feather.with(new NekoModule());
        feather.instance(ModManager.class).init();

        var demoUI = feather.instance(DemoUI.class);
        Events.on(ClientLoadEvent.class, e -> {
            Core.app.post(() -> {
                Vars.ui.menufrag.addButton(
                    "Neko Demo",
                    Icon.book,
                    demoUI::show
                );
            });
        });
    }

    /**
     * Feather DI module. Empty — all bindings use {@link javax.inject.Singleton}
     * and {@link javax.inject.Inject} annotations for auto-wiring.
     */
    public static final class NekoModule {

    }
}
