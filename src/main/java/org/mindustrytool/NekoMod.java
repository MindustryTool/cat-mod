package org.mindustrytool;

import lombok.Getter;

import lombok.extern.slf4j.Slf4j;

import mindustry.mod.Mod;

import org.codejargon.feather.Feather;

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
    }

    /**
     * Feather DI module. Empty — all bindings use {@link javax.inject.Singleton}
     * and {@link javax.inject.Inject} annotations for auto-wiring.
     */
    public static final class NekoModule {

    }
}
