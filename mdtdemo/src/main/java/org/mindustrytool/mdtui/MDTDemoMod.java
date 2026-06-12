package org.mindustrytool.mdtui;

import arc.Core;
import arc.Events;
import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.mod.Mod;

import org.mindustrytool.mdtui.screen.DemoUI;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MDTDemoMod extends Mod {

    @Override
    public void init() {
        log.info("Bootstrapping MDT Demo Mod...");

        var demoUI = new DemoUI();
        Events.on(ClientLoadEvent.class, e -> {
            Core.app.post(() -> {
                Vars.ui.menufrag.addButton(
                    "MDT Demo",
                    Icon.book,
                    demoUI::show
                );
            });
        });
    }
}
