package com.neko;

import arc.Core;
import arc.scene.event.Touchable;
import arc.util.Log;
import mindustry.mod.Mod;
import com.neko.libs.state.State;
import com.neko.libs.ui.N;
import com.neko.libs.ui.NPanel;

import static com.neko.libs.ui.N.*;

public class NekoMod extends Mod {

    public NekoMod() {
        Log.info("NekoMod initialized.");
    }

    @Override
    public void init() {
        // ── Reactive state ─────────────────────────────────────────────────────
        State<String> clickState = new State<>("Clicked 0 times");
        final int[] count = {0};

        // ── UI tree ────────────────────────────────────────────────────────────
        NPanel panel = mount(
            el("col gap:8 p:16 bg:surface",
                label("Neko UI Test", "color:bright font:title grow-x"),
                divider(),

                label(clickState, "color:secondary"),

                el("row gap:8 justify:end",
                    btn("Reset", () -> {
                        count[0] = 0;
                        clickState.setValue("Clicked 0 times");
                    }, "ghost"),
                    btn("Click me!", () -> {
                        count[0]++;
                        clickState.setValue("Clicked " + count[0] + " times");
                        Log.info("Clicked: " + count[0]);
                    }, "primary")
                )
            )
        );

        panel.setSize(300f, 200f);
        panel.setPosition(50f, 200f);
        panel.touchable = Touchable.enabled;

        Core.scene.add(panel);
        Log.info("NekoMod panel added.");
    }
}
