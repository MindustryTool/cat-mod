package org.mindustrytool;

import lombok.Getter;

import lombok.extern.slf4j.Slf4j;

import mindustry.mod.Mod;

import org.codejargon.feather.Feather;

@Slf4j
public class NekoMod extends Mod {
    private static @Getter Feather feather;

    @Override
    public void init() {
        log.info("Bootstrapping NekoMod with Feather DI...");

        feather = Feather.with(new NekoModule());
        feather.instance(ModManager.class).init();
    }

    public static final class NekoModule {

    }
}
