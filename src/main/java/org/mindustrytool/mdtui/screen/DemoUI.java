package org.mindustrytool.mdtui.screen;

import arc.graphics.Color;
import arc.struct.Seq;
import arc.util.Align;

import mindustry.ui.dialogs.BaseDialog;

import org.mindustrytool.libs.signal.Signal;
import org.mindustrytool.libs.ui.core.CustomComponent;
import org.mindustrytool.libs.ui.components.Layout;
import org.mindustrytool.libs.ui.components.Text;

import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DemoUI {

    private static final Color[] PALETTE = {
        Color.valueOf("6c8ebf"),
        Color.valueOf("f0c040"),
        Color.valueOf("d9534f"),
        Color.valueOf("5cb85c"),
        Color.valueOf("5bc0de"),
        Color.valueOf("ff79c6"),
    };

    public void show() {
        var borderEnabled = new Signal<>(false);
        var colorIndex = new Signal<>(0);
        var opacityStep = new Signal<>(10);

        var preview = CustomComponent.of().style(s -> {
            var border = borderEnabled.get();
            var color = PALETTE[colorIndex.get() % PALETTE.length];
            var alpha = opacityStep.get() / 10f;
            s.radius(12f).background(color).opacity(alpha);
            if (border) s.border(2f, Color.white);
            else s.borderWidth = 0f;
        });

        var settings = Layout.of()
            .style(s -> s.column().gap(8f).padding(16f).fixedWidth(260f))
            .children(() -> Seq.with(
                Text.of().style(s -> s.text("Preview Demo").size(1.4f).labelAlign(Align.left)),
                spacer(8f),

                section("Border"),
                Text.of().style(s -> s.text(borderEnabled.get() ? "Border: ON" : "Border: OFF").size(0.9f).labelAlign(Align.left)),
                toggle(Color.valueOf("55556a"), Color.valueOf("5cb85c"), borderEnabled).style(s -> s.fixedWidth(60f).fixedHeight(32f)),

                spacer(8f),

                section("Color"),
                Text.of().style(s -> s.text("Color: " + PALETTE[colorIndex.get() % PALETTE.length]).size(0.9f).labelAlign(Align.left)),
                swatch(colorIndex).style(s -> s.fixedWidth(60f).fixedHeight(32f)),

                spacer(8f),

                section("Opacity"),
                Text.of().style(s -> s.text("Opacity: " + (opacityStep.get() * 10) + "%").size(0.9f).labelAlign(Align.left)),
                Layout.of().style(s -> s.row().gap(8f).padding(0))
                    .children(() -> Seq.with(
                        btn("-", () -> opacityStep.set(Math.max(0, opacityStep.get() - 1))),
                        btn("+", () -> opacityStep.set(Math.min(10, opacityStep.get() + 1)))
                    ))
            ));

        var root = Layout.of()
            .style(s -> s.row().gap(0f).padding(0f))
            .children(() -> Seq.with(settings, preview));

        var dialog = new BaseDialog("Demo UI");
        dialog.cont.add(root.element()).grow();
        dialog.buttons.button("@close", () -> {
            root.dispose();
            dialog.hide();
        }).size(130f, 50f);
        dialog.show();
    }

    private static CustomComponent spacer(float h) {
        return CustomComponent.of().style(s -> s.opacity(0f).fixedHeight(h));
    }

    private static Text section(String text) {
        return Text.of().style(s -> s.text(text).size(0.8f).labelAlign(Align.left));
    }

    private static CustomComponent toggle(Color off, Color on, Signal<Boolean> signal) {
        var c = CustomComponent.of().style(s -> s.radius(6f).background(signal.get() ? on : off));
        c.onClick(() -> signal.set(!signal.get()));
        return c;
    }

    private static CustomComponent swatch(Signal<Integer> index) {
        var c = CustomComponent.of().style(s -> s.radius(6f).background(PALETTE[index.get() % PALETTE.length]));
        c.onClick(() -> index.set((index.get() + 1) % PALETTE.length));
        return c;
    }

    private static CustomComponent btn(String label, Runnable action) {
        var c = CustomComponent.of().style(s -> s.radius(6f).background(Color.valueOf("303052")).fixedWidth(40f).fixedHeight(32f));
        c.onClick(action);
        return c;
    }
}
