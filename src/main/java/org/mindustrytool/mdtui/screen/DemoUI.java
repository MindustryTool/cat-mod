package org.mindustrytool.mdtui.screen;

import arc.graphics.Color;
import arc.scene.Element;
import arc.struct.Seq;
import arc.util.Align;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.signal.Signal;
import org.mindustrytool.libs.ui.core.CustomComponent;
import org.mindustrytool.libs.ui.components.Layout;
import org.mindustrytool.libs.ui.components.Text;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Slf4j
public class DemoUI {
    private final Layout root;
    private final Signal<Boolean> borderEnabled = new Signal<>(false);
    private final Signal<Integer> colorIndex = new Signal<>(0);
    private final Signal<Integer> opacityStep = new Signal<>(10);

    private static final Color[] PALETTE = {
        Color.valueOf("6c8ebf"),
        Color.valueOf("f0c040"),
        Color.valueOf("d9534f"),
        Color.valueOf("5cb85c"),
        Color.valueOf("5bc0de"),
        Color.valueOf("ff79c6"),
    };

    @Inject
    public DemoUI() {
        var preview = CustomComponent.of().style(s -> s.radius(12f).background(Color.valueOf("1c1c22")));

        Effect.ofMain(() -> {
            var border = borderEnabled.get();
            var color = PALETTE[colorIndex.get() % PALETTE.length];
            var alpha = opacityStep.get() / 10f;

            preview.style(s -> {
                s.radius(12f).background(color).opacity(alpha);
                if (border) s.border(2f, Color.white);
                else s.borderWidth = 0f;
            });
        });

        var borderLabel = label("Border: OFF");
        var borderBtn = toggleBtn(false).onClick(() -> {
            borderEnabled.set(!borderEnabled.get());
            borderLabel.style.text(borderEnabled.get() ? "Border: ON" : "Border: OFF");
        });

        var colorLabel = label("Color: " + PALETTE[0]);
        CustomComponent colorSwatch = swatch(PALETTE[0]);
        colorSwatch.onClick(() -> {
            var next = (colorIndex.get() + 1) % PALETTE.length;
            colorIndex.set(next);
            colorLabel.style.text("Color: " + PALETTE[next]);
            colorSwatch.style.background(PALETTE[next]);
        });

        var opacityLabel = label("Opacity: 100%");
        var minusBtn = btn("-").onClick(() -> {
            var val = Math.max(0, opacityStep.get() - 1);
            opacityStep.set(val);
            opacityLabel.style.text("Opacity: " + (val * 10) + "%");
        });
        var plusBtn = btn("+").onClick(() -> {
            var val = Math.min(10, opacityStep.get() + 1);
            opacityStep.set(val);
            opacityLabel.style.text("Opacity: " + (val * 10) + "%");
        });

        var settings = Layout.of()
            .style(s -> s.column().gap(8f).padding(16f))
            .children(() -> Seq.with(
                title("Preview Demo"),
                spacer(8f),
                section("Border"),
                borderLabel, borderBtn,
                spacer(8f),
                section("Color"),
                colorLabel, colorSwatch,
                spacer(8f),
                section("Opacity"),
                opacityLabel,
                Layout.of().style(s -> s.row().gap(8f).padding(0))
                    .children(() -> Seq.with(minusBtn, plusBtn))
            ));

        root = Layout.of()
            .style(s -> s.row().gap(0f).padding(0f))
            .children(() -> Seq.with(settings, preview));

        log.info("DemoUI initialised");
    }

    public Element element() {
        return root.element();
    }

    private static Text title(String text) {
        return Text.of().style(s -> s.text(text).size(1.4f));
    }

    private static Text section(String text) {
        return Text.of().style(s -> s.text(text).size(0.8f)
            .labelAlign(Align.left).lineAlign(Align.left));
    }

    private static Text label(String text) {
        return Text.of().style(s ->
            s.text(text).size(0.9f).labelAlign(Align.left).lineAlign(Align.left));
    }

    private static CustomComponent spacer(float h) {
        return CustomComponent.of().style(s -> s.opacity(0f).fixedHeight(h));
    }

    private static CustomComponent btn(String text) {
        return CustomComponent.of()
            .style(s -> s.radius(6f).background(Color.valueOf("303052"))
                .fixedWidth(40f).fixedHeight(32f));
    }

    private static CustomComponent toggleBtn(boolean on) {
        return CustomComponent.of()
            .style(s -> s.radius(6f)
                .background(on ? Color.valueOf("5cb85c") : Color.valueOf("55556a"))
                .fixedWidth(60f).fixedHeight(32f));
    }

    private static CustomComponent swatch(Color c) {
        return CustomComponent.of()
            .style(s -> s.radius(6f).background(c).fixedWidth(60f).fixedHeight(32f));
    }
}
