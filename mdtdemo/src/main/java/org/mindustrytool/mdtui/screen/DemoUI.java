package org.mindustrytool.mdtui.screen;

import arc.graphics.Color;
import arc.struct.Seq;
import arc.util.Align;

import mindustry.ui.dialogs.BaseDialog;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.signal.Signal;
import org.mindustrytool.libs.ui.components.Gradient;
import org.mindustrytool.libs.ui.widget.Widget;
import org.mindustrytool.libs.ui.components.CustomWidget;
import org.mindustrytool.libs.ui.components.LayoutWidget;
import org.mindustrytool.libs.ui.components.TextWidget;
import org.mindustrytool.libs.ui.components.GestureDetector;
import org.mindustrytool.libs.ui.widget.ElementNode;
import org.mindustrytool.libs.ui.layout.LayoutSpec;

import lombok.RequiredArgsConstructor;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DemoUI {

    private static final Color[] PRESET_COLORS = {
        Color.valueOf("1c1c22"),
        Color.valueOf("303052"),
        Color.valueOf("6c8ebf"),
        Color.valueOf("ff79c6"),
        Color.valueOf("5cb85c"),
        Color.valueOf("d9534f"),
        Color.valueOf("f0c040")
    };

    private static final String[] IMAGE_URLS = {
        "https://github.com/Anuken.png",
        "https://github.com/github.png",
        "https://upload.wikimedia.org/wikipedia/commons/4/4e/Mindustry_Icon.png"
    };

    private static final String[] IMAGE_NAMES = { "Anuken", "GitHub", "Mindustry" };

    private final Signal<AppState> state = Signal.of(AppState.initial());
    private ElementNode rootNode;

    public void show() {
        AppState s = state.get();
        Widget rootWidget = build(s);
        rootNode = rootWidget.createElement();
        rootNode.mount(null);

        BaseDialog dialog = new BaseDialog("Developer Playground");
        dialog.cont.add(rootNode.getArcElement()).grow();
        dialog.buttons.button("@close", () -> {
            rootNode.dispose();
            dialog.hide();
        }).size(140f, 50f);
        dialog.show();

        Effect.of(() -> {
            AppState next = state.get();
            rootNode.update(build(next));
        });
    }

    private Widget build(AppState s) {
        return LayoutWidget.builder()
            .layoutSpec(LayoutSpec.builder()
                .isColumn(false).gap(0f)
                .widthMode(LayoutSpec.SizeMode.GROW).heightMode(LayoutSpec.SizeMode.GROW)
                .build())
            .children(Seq.with(
                buildSidebar(s),
                buildMainPreviewArea(s)
            ))
            .build();
    }

    private Widget buildSidebar(AppState s) {
        return LayoutWidget.builder()
            .scrollY(true).fadeScrollBars(true)
            .layoutSpec(LayoutSpec.builder()
                .isColumn(true).gap(16f)
                .paddingTop(12f).paddingRight(12f).paddingBottom(12f).paddingLeft(12f)
                .widthMode(LayoutSpec.SizeMode.FIXED).fixedWidth(320f).heightMode(LayoutSpec.SizeMode.GROW)
                .build())
            .children(Seq.with(
                TextWidget.builder().text("[ff79c6]MDT Playground[]").fontScale(1.3f).labelAlign(Align.left).build(),
                buildTabBar(s),
                buildTabControls(s)
            ))
            .build();
    }

    private Widget buildTabBar(AppState s) {
        return LayoutWidget.builder()
            .layoutSpec(LayoutSpec.builder()
                .isColumn(false).gap(4f).fixedHeight(32f).widthMode(LayoutSpec.SizeMode.GROW)
                .build())
            .children(Seq.with(
                tabButton(s, "Shape", 0),
                tabButton(s, "Shadow", 1),
                tabButton(s, "Filter", 2),
                tabButton(s, "Layout", 3)
            ))
            .build();
    }

    private Widget tabButton(AppState s, String title, int tabIndex) {
        boolean isActive = s.activeTab() == tabIndex;
        Color bg = isActive ? Color.valueOf("ff79c6") : Color.valueOf("303042");
        Color textColor = isActive ? Color.white : Color.lightGray;
        return GestureDetector.builder()
            .onTap(() -> state.set(state.get().withActiveTab(tabIndex)))
            .child(LayoutWidget.builder()
                .background(CustomWidget.builder().backgroundMode(CustomWidget.BackgroundMode.SOLID)
                    .fillColor(bg).topLeftRadius(6f).topRightRadius(6f).bottomRightRadius(6f).bottomLeftRadius(6f).build())
                .layoutSpec(LayoutSpec.builder()
                    .alignItems(LayoutSpec.AlignItems.CENTER).justifyContent(LayoutSpec.JustifyContent.CENTER)
                    .fixedHeight(28f).widthMode(LayoutSpec.SizeMode.GROW)
                    .build())
                .children(Seq.with(
                    TextWidget.builder().text(title).fontScale(0.8f).color(textColor).build()
                ))
                .build())
            .build();
    }

    private Widget buildTabControls(AppState s) {
        return LayoutWidget.builder()
            .layoutSpec(LayoutSpec.builder()
                .isColumn(true).gap(12f).widthMode(LayoutSpec.SizeMode.GROW)
                .build())
            .children(buildControlsForTab(s))
            .build();
    }

    private Seq<Widget> buildControlsForTab(AppState s) {
        return switch (s.activeTab()) {
            case 0 -> buildShapeControls(s);
            case 1 -> buildShadowControls(s);
            case 2 -> buildFilterControls(s);
            case 3 -> buildLayoutControls(s);
            default -> new Seq<>();
        };
    }

    private Widget buildMainPreviewArea(AppState s) {
        Widget content;
        if (s.activeTab() == 3) {
            content = buildSandboxPreview(s);
        } else {
            content = buildPreviewCard(s);
        }
        return LayoutWidget.builder()
            .background(CustomWidget.builder().backgroundMode(CustomWidget.BackgroundMode.SOLID)
                .fillColor(Color.valueOf("101014")).build())
            .layoutSpec(LayoutSpec.builder()
                .isColumn(true).paddingTop(24f).paddingRight(24f).paddingBottom(24f).paddingLeft(24f)
                .widthMode(LayoutSpec.SizeMode.GROW).heightMode(LayoutSpec.SizeMode.GROW)
                .alignItems(LayoutSpec.AlignItems.STRETCH).justifyContent(LayoutSpec.JustifyContent.CENTER)
                .build())
            .children(Seq.with(
                LayoutWidget.builder()
                    .layoutSpec(LayoutSpec.builder()
                        .alignItems(LayoutSpec.AlignItems.CENTER).justifyContent(LayoutSpec.JustifyContent.CENTER)
                        .widthMode(LayoutSpec.SizeMode.GROW).heightMode(LayoutSpec.SizeMode.GROW)
                        .build())
                    .children(Seq.with(content))
                    .build()
            ))
            .build();
    }

    private Widget buildPreviewCard(AppState s) {
        return LayoutWidget.builder()
            .background(buildPreviewCustom(s))
            .layoutSpec(LayoutSpec.builder()
                .alignItems(LayoutSpec.AlignItems.CENTER).justifyContent(LayoutSpec.JustifyContent.CENTER)
                .widthMode(LayoutSpec.SizeMode.FIXED).fixedWidth(200f).heightMode(LayoutSpec.SizeMode.FIXED).fixedHeight(200f)
                .build())
            .children(Seq.with(
                TextWidget.builder().text("[ff79c6]Preview Element[]").fontScale(1.2f).wrap(true).build()
            ))
            .build();
    }

    private Widget buildPreviewCustom(AppState s) {
        CustomWidget.CustomWidgetBuilder b = CustomWidget.builder()
            .backgroundMode(CustomWidget.BackgroundMode.SOLID)
            .topLeftRadius(s.radiusTL()).topRightRadius(s.radiusTR())
            .bottomRightRadius(s.radiusBR()).bottomLeftRadius(s.radiusBL())
            .opacity(s.opacity());

        if (s.backdropOn()) {
            b.backgroundMode(CustomWidget.BackgroundMode.BACKDROP)
                .backdropIterations((int)s.backdropIterations())
                .backdropBlend(s.backdropBlend()).backdropWeight(s.backdropWeight());
        } else if (s.bgMode() == 0) {
            b.fillColor(s.fillColor());
        } else if (s.bgMode() == 1) {
            b.backgroundMode(CustomWidget.BackgroundMode.GRADIENT)
                .gradient0(Gradient.linear(s.gradAngle(),
                    Gradient.stop(0f, s.gradColor1()), Gradient.stop(1f, s.gradColor2())));
        } else {
            b.fillColor(s.fillColor());
        }

        if (s.borderOn()) {
            b.borderWidth(s.borderWidth()).borderColor(s.borderColor());
            b.borderStyle(s.dashOn() ? 1 : 0);
            b.dashLength(s.dashLength()).dashRatio(s.dashRatio());
        }

        b.innerShadowSpread(s.innerShadowSpread()).innerShadowBlur(s.innerShadowBlur())
            .innerShadowColor(s.innerShadowColor());
        b.glowSpread(s.glowSpread()).glowColor(s.glowColor());

        if (s.filterType() == 1) { b.filterMode(1).filterAmount(s.filterAmount()); }
        else if (s.filterType() == 2) { b.filterMode(2).filterAmount(s.filterAmount()); }
        else if (s.filterType() == 3) { b.filterMode(3).filterAmount(s.filterAmount()); }
        else if (s.filterType() == 4) { b.filterMode(4).filterAmount(s.filterAmount()); }
        b.noiseAmount(s.noiseAmount());

        return b.build();
    }

    private Widget buildSandboxPreview(AppState s) {
        return LayoutWidget.builder()
            .background(CustomWidget.builder().backgroundMode(CustomWidget.BackgroundMode.SOLID)
                .fillColor(Color.valueOf("14141a")).topLeftRadius(12f).topRightRadius(12f)
                .bottomRightRadius(12f).bottomLeftRadius(12f)
                .borderWidth(1f).borderColor(Color.valueOf("ffffff10")).build())
            .layoutSpec(LayoutSpec.builder()
                .alignItems(s.sandboxAlign()).justifyContent(s.sandboxJustify())
                .isColumn(s.sandboxIsColumn()).isWrap(s.sandboxIsWrap()).gap(s.sandboxGap())
                .paddingTop(16f).paddingRight(16f).paddingBottom(16f).paddingLeft(16f)
                .widthMode(LayoutSpec.SizeMode.GROW).heightMode(LayoutSpec.SizeMode.GROW)
                .build())
            .children(Seq.with(
                CustomWidget.builder().backgroundMode(CustomWidget.BackgroundMode.SOLID)
                    .fillColor(Color.valueOf("ff79c6")).topLeftRadius(8f).topRightRadius(8f)
                    .bottomRightRadius(8f).bottomLeftRadius(8f)
                    .layoutSpec(LayoutSpec.builder().fixedWidth(50f).fixedHeight(50f).build())
                    .build(),
                CustomWidget.builder().backgroundMode(CustomWidget.BackgroundMode.SOLID)
                    .fillColor(Color.valueOf("6c8ebf")).topLeftRadius(8f).topRightRadius(8f)
                    .bottomRightRadius(8f).bottomLeftRadius(8f)
                    .layoutSpec(LayoutSpec.builder().fixedWidth(70f).fixedHeight(60f).build())
                    .build(),
                CustomWidget.builder().backgroundMode(CustomWidget.BackgroundMode.SOLID)
                    .fillColor(Color.valueOf("f0c040")).topLeftRadius(8f).topRightRadius(8f)
                    .bottomRightRadius(8f).bottomLeftRadius(8f)
                    .layoutSpec(LayoutSpec.builder().fixedWidth(60f).fixedHeight(40f).build())
                    .build()
            ))
            .build();
    }

    private Seq<Widget> buildShapeControls(AppState s) {
        return Seq.with(
            sectionHeader("Fill Options"),
            selector(s, "Fill Mode", new Integer[]{0, 1, 2}, new String[]{"Solid", "Gradient", "Image"}, s.bgMode(), v -> state.set(state.get().withBgMode(v))),
            colorPicker(s, "Solid Color", PRESET_COLORS, s.fillColor(), v -> state.set(state.get().withFillColor(v))),
            colorPicker(s, "Gradient Start", PRESET_COLORS, s.gradColor1(), v -> state.set(state.get().withGradColor1(v))),
            colorPicker(s, "Gradient End", PRESET_COLORS, s.gradColor2(), v -> state.set(state.get().withGradColor2(v))),
            stepper(s, "Gradient Angle", -360f, 360f, 15f, s.gradAngle(), v -> state.set(state.get().withGradAngle(v)), "%.0f\u00b0"),
            selector(s, "Sample Image", IMAGE_URLS, IMAGE_NAMES, s.activeUrl(), v -> state.set(state.get().withActiveUrl(v))),
            sectionHeader("Corner Radii"),
            stepper(s, "Top Left", 0f, 40f, 2f, s.radiusTL(), v -> state.set(state.get().withRadiusTL(v)), "%.0f px"),
            stepper(s, "Top Right", 0f, 40f, 2f, s.radiusTR(), v -> state.set(state.get().withRadiusTR(v)), "%.0f px"),
            stepper(s, "Bottom Right", 0f, 40f, 2f, s.radiusBR(), v -> state.set(state.get().withRadiusBR(v)), "%.0f px"),
            stepper(s, "Bottom Left", 0f, 40f, 2f, s.radiusBL(), v -> state.set(state.get().withRadiusBL(v)), "%.0f px"),
            sectionHeader("Border Styling"),
            toggle(s, "Enable Border", s.borderOn(), v -> state.set(state.get().withBorderOn(v))),
            stepper(s, "Border Width", 1f, 8f, 0.5f, s.borderWidth(), v -> state.set(state.get().withBorderWidth(v)), "%.1f px"),
            colorPicker(s, "Border Color", PRESET_COLORS, s.borderColor(), v -> state.set(state.get().withBorderColor(v))),
            toggle(s, "Dashed Border", s.dashOn(), v -> state.set(state.get().withDashOn(v))),
            stepper(s, "Dash Length", 2f, 30f, 2f, s.dashLength(), v -> state.set(state.get().withDashLength(v)), "%.0f px"),
            stepper(s, "Dash Ratio", 0.1f, 0.9f, 0.05f, s.dashRatio(), v -> state.set(state.get().withDashRatio(v)), "%.2f")
        );
    }

    private Seq<Widget> buildShadowControls(AppState s) {
        return Seq.with(
            sectionHeader("Inner Shadow"),
            stepper(s, "Spread Size", 0f, 30f, 2f, s.innerShadowSpread(), v -> state.set(state.get().withInnerShadowSpread(v)), "%.0f px"),
            stepper(s, "Blur Size", 0f, 30f, 2f, s.innerShadowBlur(), v -> state.set(state.get().withInnerShadowBlur(v)), "%.0f px"),
            colorPicker(s, "Shadow Color", new Color[]{
                Color.valueOf("00000066"), Color.valueOf("000000aa"), Color.valueOf("d9534f66"), Color.valueOf("ff79c666")
            }, s.innerShadowColor(), v -> state.set(state.get().withInnerShadowColor(v))),
            sectionHeader("Outer Glow"),
            stepper(s, "Glow Spread", 0f, 40f, 2f, s.glowSpread(), v -> state.set(state.get().withGlowSpread(v)), "%.0f px"),
            colorPicker(s, "Glow Color", new Color[]{
                Color.valueOf("ff79c633"), Color.valueOf("6c8ebf33"), Color.valueOf("5cb85c33"), Color.valueOf("f0c04033")
            }, s.glowColor(), v -> state.set(state.get().withGlowColor(v)))
        );
    }

    private Seq<Widget> buildFilterControls(AppState s) {
        return Seq.with(
            sectionHeader("Backdrop Blur (Glass)"),
            toggle(s, "Enable Glassmorphism", s.backdropOn(), v -> state.set(state.get().withBackdropOn(v))),
            stepper(s, "Iterations", 1f, 8f, 1f, s.backdropIterations(), v -> state.set(state.get().withBackdropIterations(v)), "%.0f times"),
            stepper(s, "Blur Blend", 0.0f, 1.0f, 0.1f, s.backdropBlend(), v -> state.set(state.get().withBackdropBlend(v)), "%.1f"),
            stepper(s, "Blur Weight", 0.0f, 1.0f, 0.1f, s.backdropWeight(), v -> state.set(state.get().withBackdropWeight(v)), "%.1f"),
            sectionHeader("Color Filters"),
            stepper(s, "Overall Opacity", 0.1f, 1.0f, 0.05f, s.opacity(), v -> state.set(state.get().withOpacity(v)), "%.2f"),
            selector(s, "Filter Mode", new Integer[]{0, 1, 2, 3, 4}, new String[]{"None", "Gray", "Sepia", "Bright", "Invert"}, s.filterType(), v -> state.set(state.get().withFilterType(v))),
            stepper(s, "Filter Amount", 0.0f, 1.0f, 0.1f, s.filterAmount(), v -> state.set(state.get().withFilterAmount(v)), "%.1f"),
            stepper(s, "Noise Grain", 0.0f, 1.0f, 0.05f, s.noiseAmount(), v -> state.set(state.get().withNoiseAmount(v)), "%.2f")
        );
    }

    private Seq<Widget> buildLayoutControls(AppState s) {
        return Seq.with(
            sectionHeader("Flow Orientation"),
            toggle(s, "Column Direction", s.sandboxIsColumn(), v -> state.set(state.get().withSandboxIsColumn(v))),
            toggle(s, "Enable Wrapping", s.sandboxIsWrap(), v -> state.set(state.get().withSandboxIsWrap(v))),
            stepper(s, "Gap Spacing", 0f, 32f, 4f, s.sandboxGap(), v -> state.set(state.get().withSandboxGap(v)), "%.0f px"),
            sectionHeader("Main Axis Justification"),
            selector(s, "Justify Content",
                new LayoutSpec.JustifyContent[]{
                    LayoutSpec.JustifyContent.START, LayoutSpec.JustifyContent.CENTER,
                    LayoutSpec.JustifyContent.END, LayoutSpec.JustifyContent.SPACE_BETWEEN,
                    LayoutSpec.JustifyContent.SPACE_AROUND, LayoutSpec.JustifyContent.SPACE_EVENLY
                },
                new String[]{"Start", "Center", "End", "Between", "Around", "Evenly"},
                s.sandboxJustify(), v -> state.set(state.get().withSandboxJustify(v))),
            sectionHeader("Cross Axis Alignment"),
            selector(s, "Align Items",
                new LayoutSpec.AlignItems[]{
                    LayoutSpec.AlignItems.START, LayoutSpec.AlignItems.CENTER,
                    LayoutSpec.AlignItems.END, LayoutSpec.AlignItems.STRETCH
                },
                new String[]{"Start", "Center", "End", "Stretch"},
                s.sandboxAlign(), v -> state.set(state.get().withSandboxAlign(v)))
        );
    }

    private Widget sectionHeader(String text) {
        return TextWidget.builder()
            .text("[ff79c6]-- " + text + " --[]")
            .fontScale(0.85f).labelAlign(Align.center)
            .build();
    }

    private Widget toggle(AppState s, String label, boolean value, java.util.function.Consumer<Boolean> onChange) {
        Color bgColor = value ? Color.valueOf("5cb85c") : Color.valueOf("303042");
        return LayoutWidget.builder()
            .layoutSpec(LayoutSpec.builder()
                .isColumn(false).gap(8f).alignItems(LayoutSpec.AlignItems.CENTER)
                .widthMode(LayoutSpec.SizeMode.GROW).fixedHeight(32f)
                .build())
            .children(Seq.with(
                LayoutWidget.builder().layoutSpec(LayoutSpec.builder().widthMode(LayoutSpec.SizeMode.GROW).build()).children(Seq.with(
                    TextWidget.builder().text(label).fontScale(0.8f).labelAlign(Align.left).build()
                )).build(),
                GestureDetector.builder()
                    .onTap(() -> onChange.accept(!value))
                    .child(LayoutWidget.builder()
                        .background(CustomWidget.builder().backgroundMode(CustomWidget.BackgroundMode.SOLID)
                            .fillColor(bgColor).topLeftRadius(8f).topRightRadius(8f)
                            .bottomRightRadius(8f).bottomLeftRadius(8f).build())
                        .layoutSpec(LayoutSpec.builder()
                            .alignItems(LayoutSpec.AlignItems.CENTER).justifyContent(LayoutSpec.JustifyContent.CENTER)
                            .widthMode(LayoutSpec.SizeMode.FIXED).fixedWidth(45f).heightMode(LayoutSpec.SizeMode.FIXED).fixedHeight(20f)
                            .build())
                        .children(Seq.with(
                            TextWidget.builder().text(value ? "ON" : "OFF").fontScale(0.7f).color(Color.white).build()
                        ))
                        .build())
                    .build()
            ))
            .build();
    }

    private Widget stepper(AppState s, String label, float min, float max, float step, float value, java.util.function.Consumer<Float> onChange, String format) {
        return LayoutWidget.builder()
            .layoutSpec(LayoutSpec.builder()
                .isColumn(false).gap(6f).alignItems(LayoutSpec.AlignItems.CENTER)
                .widthMode(LayoutSpec.SizeMode.GROW).fixedHeight(32f)
                .build())
            .children(Seq.with(
                LayoutWidget.builder().layoutSpec(LayoutSpec.builder().widthMode(LayoutSpec.SizeMode.GROW).build()).children(Seq.with(
                    TextWidget.builder().text(label).fontScale(0.8f).labelAlign(Align.left).build()
                )).build(),
                minusButton(min, step, value, onChange),
                LayoutWidget.builder()
                    .layoutSpec(LayoutSpec.builder()
                        .widthMode(LayoutSpec.SizeMode.FIXED).fixedWidth(50f)
                        .alignItems(LayoutSpec.AlignItems.CENTER).justifyContent(LayoutSpec.JustifyContent.CENTER)
                        .build())
                    .children(Seq.with(
                        TextWidget.builder().text(String.format(format, value)).fontScale(0.8f).build()
                    ))
                    .build(),
                plusButton(max, step, value, onChange)
            ))
            .build();
    }

    private Widget minusButton(float min, float step, float value, java.util.function.Consumer<Float> onChange) {
        return GestureDetector.builder()
            .onTap(() -> onChange.accept(Math.max(min, value - step)))
            .child(LayoutWidget.builder()
                .background(CustomWidget.builder().backgroundMode(CustomWidget.BackgroundMode.SOLID)
                    .fillColor(Color.valueOf("303042")).topLeftRadius(4f).topRightRadius(4f)
                    .bottomRightRadius(4f).bottomLeftRadius(4f).build())
                .layoutSpec(LayoutSpec.builder()
                    .alignItems(LayoutSpec.AlignItems.CENTER).justifyContent(LayoutSpec.JustifyContent.CENTER)
                    .widthMode(LayoutSpec.SizeMode.FIXED).fixedWidth(28f).heightMode(LayoutSpec.SizeMode.FIXED).fixedHeight(22f)
                    .build())
                .children(Seq.with(TextWidget.builder().text("-").fontScale(0.85f).build()))
                .build())
            .build();
    }

    private Widget plusButton(float max, float step, float value, java.util.function.Consumer<Float> onChange) {
        return GestureDetector.builder()
            .onTap(() -> onChange.accept(Math.min(max, value + step)))
            .child(LayoutWidget.builder()
                .background(CustomWidget.builder().backgroundMode(CustomWidget.BackgroundMode.SOLID)
                    .fillColor(Color.valueOf("303042")).topLeftRadius(4f).topRightRadius(4f)
                    .bottomRightRadius(4f).bottomLeftRadius(4f).build())
                .layoutSpec(LayoutSpec.builder()
                    .alignItems(LayoutSpec.AlignItems.CENTER).justifyContent(LayoutSpec.JustifyContent.CENTER)
                    .widthMode(LayoutSpec.SizeMode.FIXED).fixedWidth(28f).heightMode(LayoutSpec.SizeMode.FIXED).fixedHeight(22f)
                    .build())
                .children(Seq.with(TextWidget.builder().text("+").fontScale(0.85f).build()))
                .build())
            .build();
    }

    private Widget colorPicker(AppState s, String label, Color[] colors, Color current, java.util.function.Consumer<Color> onChange) {
        Seq<Widget> swatches = new Seq<>();
        for (Color c : colors) {
            swatches.add(colorSwatch(c, current, onChange));
        }
        return LayoutWidget.builder()
            .layoutSpec(LayoutSpec.builder()
                .isColumn(true).gap(4f).widthMode(LayoutSpec.SizeMode.GROW)
                .build())
            .children(Seq.with(
                TextWidget.builder().text(label).fontScale(0.75f).labelAlign(Align.left).build(),
                LayoutWidget.builder()
                    .layoutSpec(LayoutSpec.builder()
                        .isColumn(false).gap(6f).isWrap(true).fixedHeight(22f)
                        .build())
                    .children(swatches)
                    .build()
            ))
            .build();
    }

    private Widget colorSwatch(Color color, Color current, java.util.function.Consumer<Color> onChange) {
        boolean isActive = current.equals(color);
        float border = isActive ? 1.5f : 0f;
        Color borderC = isActive ? Color.white : Color.clear;
        return GestureDetector.builder()
            .onTap(() -> onChange.accept(color))
            .child(LayoutWidget.builder()
                .layoutSpec(LayoutSpec.builder()
                    .widthMode(LayoutSpec.SizeMode.FIXED).fixedWidth(18f).heightMode(LayoutSpec.SizeMode.FIXED).fixedHeight(18f)
                    .build())
                .background(CustomWidget.builder().backgroundMode(CustomWidget.BackgroundMode.SOLID)
                    .fillColor(color).topLeftRadius(4f).topRightRadius(4f).bottomRightRadius(4f).bottomLeftRadius(4f)
                    .borderWidth(border).borderColor(borderC).build())
                .build())
            .build();
    }

    @SuppressWarnings("unchecked")
    private <T> Widget selector(AppState s, String label, T[] options, String[] displayNames, T current, java.util.function.Consumer<T> onChange) {
        Seq<Widget> rowChildren = new Seq<>();
        for (int i = 0; i < options.length; i++) {
            rowChildren.add(optionButton(options[i], displayNames[i], current, onChange));
        }
        return LayoutWidget.builder()
            .layoutSpec(LayoutSpec.builder()
                .isColumn(true).gap(4f).widthMode(LayoutSpec.SizeMode.GROW)
                .build())
            .children(Seq.with(
                TextWidget.builder().text(label).fontScale(0.75f).labelAlign(Align.left).build(),
                LayoutWidget.builder()
                    .layoutSpec(LayoutSpec.builder()
                        .isColumn(false).gap(4f).isWrap(true)
                        .build())
                    .children(rowChildren)
                    .build()
            ))
            .build();
    }

    private <T> Widget optionButton(T option, String displayName, T current, java.util.function.Consumer<T> onChange) {
        boolean isActive = current.equals(option);
        Color bg = isActive ? Color.valueOf("ff79c6") : Color.valueOf("303042");
        Color textColor = isActive ? Color.white : Color.lightGray;
        return GestureDetector.builder()
            .onTap(() -> onChange.accept(option))
            .child(LayoutWidget.builder()
                .background(CustomWidget.builder().backgroundMode(CustomWidget.BackgroundMode.SOLID)
                    .fillColor(bg).topLeftRadius(4f).topRightRadius(4f).bottomRightRadius(4f).bottomLeftRadius(4f).build())
                .layoutSpec(LayoutSpec.builder()
                    .alignItems(LayoutSpec.AlignItems.CENTER).justifyContent(LayoutSpec.JustifyContent.CENTER)
                    .fixedHeight(22f).widthMode(LayoutSpec.SizeMode.GROW)
                    .build())
                .children(Seq.with(
                    TextWidget.builder().text(displayName).fontScale(0.7f).color(textColor).build()
                ))
                .build())
            .build();
    }
}
