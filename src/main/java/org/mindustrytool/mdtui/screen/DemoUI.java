package org.mindustrytool.mdtui.screen;

import arc.graphics.Color;
import arc.struct.Seq;
import arc.util.Align;

import mindustry.ui.dialogs.BaseDialog;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.signal.Signal;
import org.mindustrytool.libs.ui.core.CustomComponent;
import org.mindustrytool.libs.ui.components.Layout;
import org.mindustrytool.libs.ui.components.Text;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
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

    private final Signal<Boolean> borderEnabled = new Signal<>(false);
    private final Signal<Integer> colorIndex = new Signal<>(0);
    private final Signal<Integer> opacityStep = new Signal<>(10);

    public void show() {
        var preview = CustomComponent.of().style(s -> s.radius(12f).background(Color.valueOf("1c1c22")));

        var previewBinding = Effect.ofMain(() -> {
            var border = borderEnabled.get();
            var color = PALETTE[colorIndex.get() % PALETTE.length];
            var alpha = opacityStep.get() / 10f;

            preview.style(s -> {
                s.radius(12f).background(color).opacity(alpha);
                if (border) s.border(2f, Color.white);
                else s.borderWidth = 0f;
            });
        });

        var settings = Layout.of()
            .style(s -> s.column().gap(8f).padding(16f))
            .children(() -> {
                var borderLabel = Text.of().style(s -> s.text("Border: OFF").size(0.9f).labelAlign(Align.left));
                CustomComponent borderBtn = CustomComponent.of()
                    .style(s -> s.radius(6f).background(Color.valueOf("55556a")).fixedWidth(60f).fixedHeight(32f));
                borderBtn.onClick(() -> {
                    borderEnabled.set(!borderEnabled.get());
                    borderLabel.style.text(borderEnabled.get() ? "Border: ON" : "Border: OFF");
                    borderBtn.style.background(borderEnabled.get() ? Color.valueOf("5cb85c") : Color.valueOf("55556a"));
                });

                var colorLabel = Text.of().style(s -> s.text("Color: " + PALETTE[0]).size(0.9f).labelAlign(Align.left));
                CustomComponent colorSwatch = CustomComponent.of()
                    .style(s -> s.radius(6f).background(PALETTE[0]).fixedWidth(60f).fixedHeight(32f));
                colorSwatch.onClick(() -> {
                    var next = (colorIndex.get() + 1) % PALETTE.length;
                    colorIndex.set(next);
                    colorLabel.style.text("Color: " + PALETTE[next]);
                    colorSwatch.style.background(PALETTE[next]);
                });

                var opacityLabel = Text.of().style(s -> s.text("Opacity: 100%").size(0.9f).labelAlign(Align.left));
                CustomComponent minusBtn = CustomComponent.of()
                    .style(s -> s.radius(6f).background(Color.valueOf("303052")).fixedWidth(40f).fixedHeight(32f));
                minusBtn.onClick(() -> {
                    var val = Math.max(0, opacityStep.get() - 1);
                    opacityStep.set(val);
                    opacityLabel.style.text("Opacity: " + (val * 10) + "%");
                });
                CustomComponent plusBtn = CustomComponent.of()
                    .style(s -> s.radius(6f).background(Color.valueOf("303052")).fixedWidth(40f).fixedHeight(32f));
                plusBtn.onClick(() -> {
                    var val = Math.min(10, opacityStep.get() + 1);
                    opacityStep.set(val);
                    opacityLabel.style.text("Opacity: " + (val * 10) + "%");
                });

                return Seq.with(
                    Text.of().style(s -> s.text("Preview Demo").size(1.4f)),
                    CustomComponent.of().style(s -> s.opacity(0f).fixedHeight(8f)),
                    Text.of().style(s -> s.text("Border").size(0.8f).labelAlign(Align.left)),
                    borderLabel, borderBtn,
                    CustomComponent.of().style(s -> s.opacity(0f).fixedHeight(8f)),
                    Text.of().style(s -> s.text("Color").size(0.8f).labelAlign(Align.left)),
                    colorLabel, colorSwatch,
                    CustomComponent.of().style(s -> s.opacity(0f).fixedHeight(8f)),
                    Text.of().style(s -> s.text("Opacity").size(0.8f).labelAlign(Align.left)),
                    opacityLabel,
                    Layout.of().style(s -> s.row().gap(8f).padding(0))
                        .children(() -> Seq.with(minusBtn, plusBtn))
                );
            });

        var root = Layout.of()
            .style(s -> s.row().gap(0f).padding(0f))
            .children(() -> Seq.with(settings, preview));

        var dialog = new BaseDialog("Demo UI");
        dialog.cont.add(root.element()).grow();
        dialog.buttons.button("@close", () -> {
            previewBinding.dispose();
            root.dispose();
            dialog.hide();
        }).size(130f, 50f);
        dialog.show();
    }
}
