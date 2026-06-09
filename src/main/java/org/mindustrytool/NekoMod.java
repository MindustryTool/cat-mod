package org.mindustrytool;

import arc.util.Log;

import lombok.Getter;

import mindustry.mod.Mod;

import org.codejargon.feather.Feather;

public class NekoMod extends Mod {
    private static @Getter Feather feather;

    @Override
    public void init() {
        Log.info("Bootstrapping NekoMod with Feather DI...");

        feather = Feather.with(new NekoModule());
        feather.instance(NekoUiManager.class).init();
    }

    public static final class NekoModule {

    }
}
