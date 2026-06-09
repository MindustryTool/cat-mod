package org.mindustrytool;

import arc.util.Log;
import mindustry.mod.Mod;
import org.codejargon.feather.Feather;

public class NekoMod extends Mod {

    private Feather feather;

    public NekoMod() {
        Log.info("NekoMod initialized.");
    }

    @Override
    public void init() {
        Log.info("Bootstrapping NekoMod with Feather DI...");
        feather = Feather.with(new NekoModule());
        feather.instance(NekoUiManager.class).init();
    }
}
