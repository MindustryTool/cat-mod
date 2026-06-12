package org.mindustrytool.mdtui.screen;

import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.Scene;
import arc.struct.Seq;
import arc.util.Align;

import mindustry.ui.dialogs.BaseDialog;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.signal.Signal;
import org.mindustrytool.libs.ui.components.Component;
import org.mindustrytool.libs.ui.components.Layout;
import org.mindustrytool.libs.ui.components.Text;
import org.mindustrytool.libs.ui.core.CustomComponent;
import org.mindustrytool.libs.ui.core.Gradient;
import org.mindustrytool.libs.ui.layout.LayoutSpec;
import org.mindustrytool.libs.ui.layout.LayoutSpec.AlignItems;
import org.mindustrytool.libs.ui.layout.LayoutSpec.JustifyContent;
import org.mindustrytool.libs.ui.layout.NodeSpec;
import org.mindustrytool.libs.ui.layout.NodeSpec.SizeMode;

import lombok.RequiredArgsConstructor;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * MDT Developer Playground Overlay UI.
 * Demonstrates 100% reactive state bindings and showcases all CustomComponent,
 * Layout, and Text rendering features of the framework in a complex UI.
 */
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

    // --- State Signals ---
    private final Signal<Integer> activeTab = new Signal<>(0); // 0=Shape, 1=Shadows, 2=Filters, 3=Layout

    // Shape & Color Signals
    private final Signal<Integer> bgMode = new Signal<>(0); // 0=Solid, 1=Gradient, 2=Image URL
    private final Signal<Color> fillColor = new Signal<>(Color.valueOf("303052"));
    private final Signal<Color> gradColor1 = new Signal<>(Color.valueOf("6c8ebf"));
    private final Signal<Color> gradColor2 = new Signal<>(Color.valueOf("ff79c6"));
    private final Signal<Float> gradAngle = new Signal<>(45f);
    private final Signal<String> activeUrl = new Signal<>(IMAGE_URLS[0]);

    private final Signal<Float> radiusTL = new Signal<>(12f);
    private final Signal<Float> radiusTR = new Signal<>(12f);
    private final Signal<Float> radiusBR = new Signal<>(12f);
    private final Signal<Float> radiusBL = new Signal<>(12f);

    private final Signal<Boolean> borderOn = new Signal<>(true);
    private final Signal<Float> borderWidth = new Signal<>(2f);
    private final Signal<Color> borderColor = new Signal<>(Color.white);
    private final Signal<Boolean> dashOn = new Signal<>(false);
    private final Signal<Float> dashLength = new Signal<>(10f);
    private final Signal<Float> dashRatio = new Signal<>(0.5f);

    // Shadows & Glow Signals
    private final Signal<Float> innerShadowSpread = new Signal<>(0f);
    private final Signal<Float> innerShadowBlur = new Signal<>(0f);
    private final Signal<Color> innerShadowColor = new Signal<>(Color.valueOf("00000066"));
    private final Signal<Float> glowSpread = new Signal<>(0f);
    private final Signal<Color> glowColor = new Signal<>(Color.valueOf("ff79c644"));

    // Filters & Opacity Signals
    private final Signal<Float> opacity = new Signal<>(1.0f);
    private final Signal<Integer> filterType = new Signal<>(0); // 0=None, 1=Grayscale, 2=Sepia, 3=Brightness, 4=Invert
    private final Signal<Float> filterAmount = new Signal<>(0.5f);
    private final Signal<Float> noiseAmount = new Signal<>(0.0f);

    private final Signal<Boolean> backdropOn = new Signal<>(false);
    private final Signal<Float> backdropBlend = new Signal<>(0.8f);
    private final Signal<Float> backdropWeight = new Signal<>(0.8f);
    private final Signal<Float> backdropIterations = new Signal<>(4f);

    // Layout Sandbox Signals
    private final Signal<Boolean> sandboxIsColumn = new Signal<>(false);
    private final Signal<Boolean> sandboxIsWrap = new Signal<>(false);
    private final Signal<Float> sandboxGap = new Signal<>(12f);
    private final Signal<JustifyContent> sandboxJustify = new Signal<>(JustifyContent.START);
    private final Signal<AlignItems> sandboxAlign = new Signal<>(AlignItems.STRETCH);

    public void show() {
        // --- 1. PREVIEW CARD & CONTAINER ---
        CustomComponent previewCard = CustomComponent.of();
        
        // Reactive style binding on the preview widget
        previewCard.style(s -> {
            s.radius(radiusTL.get(), radiusTR.get(), radiusBR.get(), radiusBL.get());
            s.opacity(opacity.get());

            // Background configuration
            if (backdropOn.get()) {
                s.backgroundMode = CustomComponent.Style.BackgroundMode.BACKDROP;
                s.backdropIterations = backdropIterations.get().intValue();
                s.backdropBlend = backdropBlend.get();
                s.backdropWeight = backdropWeight.get();
            } else if (bgMode.get() == 0) {
                s.background(fillColor.get());
            } else if (bgMode.get() == 1) {
                s.background(Gradient.linear(gradAngle.get(), Gradient.stop(0f, gradColor1.get()), Gradient.stop(1f, gradColor2.get())));
            } else {
                // Image URL mode - loadImage handles internal textures
                s.background(fillColor.get());
            }

            // Border configuration
            if (borderOn.get()) {
                s.borderWidth = borderWidth.get();
                s.borderColor.set(borderColor.get());
                s.borderStyle = dashOn.get() ? 1 : 0;
                s.dashLength = dashLength.get();
                s.dashRatio = dashRatio.get();
            } else {
                s.borderWidth = 0f;
            }

            // Shadows & Glow
            s.innerShadowSpread = innerShadowSpread.get();
            s.innerShadowBlur = innerShadowBlur.get();
            s.innerShadowColor.set(innerShadowColor.get());

            s.glowSpread = glowSpread.get();
            s.glowColor.set(glowColor.get());

            // Filters
            s.noise(noiseAmount.get());
            if (filterType.get() == 1) s.grayscale(filterAmount.get());
            else if (filterType.get() == 2) s.sepia(filterAmount.get());
            else if (filterType.get() == 3) s.brightness(filterAmount.get());
            else if (filterType.get() == 4) s.invert(filterAmount.get());
            else s.filterMode = 0;
        });

        // Trigger loadImage on previewCard reactively
        Effect.of(() -> {
            if (bgMode.get() == 2 && !backdropOn.get()) {
                previewCard.loadImage(activeUrl.get());
            }
        });

        // Preview card container of fixed size 200x200 centered inside previewWrapper
        Layout previewCardContainer = Layout.of()
            .background(previewCard)
            .style(s -> s.alignItems(AlignItems.CENTER).justifyContent(JustifyContent.CENTER).fixedWidth(200f).fixedHeight(200f))
            .children(() -> Seq.with(
                Text.of().style(t -> t.text("[ff79c6]Preview Element[]").size(1.2f).wrap(true))
            ));

        // Background layout wrapper to give text and center alignment to the preview card
        Layout previewWrapper = Layout.of()
            .style(s -> s.alignItems(AlignItems.CENTER).justifyContent(JustifyContent.CENTER).grow())
            .children(() -> Seq.with(
                previewCardContainer
            ));

        // --- 2. LAYOUT SANDBOX PREVIEW PANEL ---
        CustomComponent itemA = CustomComponent.of().style(s -> s.radius(8f).background(Color.valueOf("ff79c6")).width(50f).height(50f));
        CustomComponent itemB = CustomComponent.of().style(s -> s.radius(8f).background(Color.valueOf("6c8ebf")).width(70f).height(60f));
        CustomComponent itemC = CustomComponent.of().style(s -> s.radius(8f).background(Color.valueOf("f0c040")).width(60f).height(40f));

        Layout sandboxWrapper = Layout.of()
            .background(CustomComponent.of().style(s -> s.radius(12f).background(Color.valueOf("14141a")).border(1f, Color.valueOf("ffffff10"))))
            .style(s -> {
                if (sandboxIsColumn.get()) s.column(); else s.row();
                if (sandboxIsWrap.get()) s.wrap(); else s.noWrap();
                s.gap(sandboxGap.get())
                 .justifyContent(sandboxJustify.get())
                 .alignItems(sandboxAlign.get())
                 .grow()
                 .padding(16f);
            })
            .children(() -> Seq.with(itemA, itemB, itemC));

        // Right Main Content view that reactively switches depending on activeTab
        Layout mainPreviewArea = Layout.of()
            .background(CustomComponent.of().style(bg -> bg.background(Color.valueOf("101014"))))
            .style(s -> s.column().grow().padding(24f).alignItems(AlignItems.STRETCH).justifyContent(JustifyContent.CENTER))
            .children(() -> {
                if (activeTab.get() == 3) {
                    return Seq.with(
                        Text.of().style(t -> t.text("Flexbox Layout Sandbox").size(1.1f).padding(0f, 0f, 12f, 0f)),
                        sandboxWrapper
                    );
                } else {
                    return Seq.with(previewWrapper);
                }
            });

        // --- 3. SIDEBAR CONTROLS ---
        Layout sidebar = Layout.of()
            .background(CustomComponent.of().style(s -> s.radius(0f).background(Color.valueOf("1c1c24"))))
            .style(s -> s.column().fixedWidth(320f).growY().padding(12f).scrollY(true).fadeScrollBars(true).gap(16f))
            .children(() -> Seq.with(
                Text.of().style(t -> t.text("[ff79c6]MDT Playground[]").size(1.3f).labelAlign(Align.left)),
                buildTabBar(),
                buildTabControls()
            ));

        // --- 4. ROOT LAYOUT & DIALOG ---
        Layout root = Layout.of()
            .style(s -> s.row().grow().gap(0f).padding(0f))
            .children(() -> Seq.with(sidebar, mainPreviewArea));

        BaseDialog dialog = new BaseDialog("Developer Playground");
        dialog.cont.add(root.element()).grow();
        dialog.buttons.button("@close", () -> {
            root.dispose();
            dialog.hide();
        }).size(140f, 50f);
        dialog.show();
    }

    // --- Helper Component Builders ---

    private Layout buildTabBar() {
        return Layout.of()
            .style(s -> s.row().gap(4f).padding(0f).fixedHeight(32f).widthMode(SizeMode.GROW))
            .children(() -> Seq.with(
                tabButton("Shape", 0),
                tabButton("Shadow", 1),
                tabButton("Filter", 2),
                tabButton("Layout", 3)
            ));
    }

    private Component tabButton(String title, int tabIndex) {
        Signal<Boolean> isHovered = new Signal<>(false);
        CustomComponent bg = CustomComponent.of();
        bg.style(s -> {
            boolean active = activeTab.get() == tabIndex;
            s.radius(6f);
            if (active) {
                s.background(Color.valueOf("ff79c6"));
            } else {
                s.background(isHovered.get() ? Color.valueOf("44445c") : Color.valueOf("303042"));
            }
        });

        return Layout.of()
            .background(bg)
            .style(s -> s.alignItems(AlignItems.CENTER).justifyContent(JustifyContent.CENTER).fixedHeight(28f).growX())
            .children(() -> Seq.with(
                Text.of().style(t -> t.text(title).size(0.8f).color(activeTab.get() == tabIndex ? Color.white : Color.lightGray))
            ))
            .onHover(() -> isHovered.set(true))
            .onExit(() -> isHovered.set(false))
            .onClick(() -> activeTab.set(tabIndex));
    }

    private Layout buildTabControls() {
        return Layout.of()
            .style(s -> s.column().gap(12f).widthMode(SizeMode.GROW))
            .children(() -> {
                int current = activeTab.get();
                if (current == 0) return buildShapeControls();
                if (current == 1) return buildShadowControls();
                if (current == 2) return buildFilterControls();
                return buildLayoutControls();
            });
    }

    private Seq<Component> buildShapeControls() {
        return Seq.with(
            sectionHeader("Fill Options"),
            selector("Fill Mode", new Integer[]{ 0, 1, 2 }, new String[]{ "Solid", "Gradient", "Image" }, bgMode),
            
            // Solid color picker
            colorPicker("Solid Color", PRESET_COLORS, fillColor),

            // Gradient inputs
            colorPicker("Gradient Start", PRESET_COLORS, gradColor1),
            colorPicker("Gradient End", PRESET_COLORS, gradColor2),
            stepper("Gradient Angle", -360f, 360f, 15f, gradAngle, "%.0f°"),

            // Image URL lists
            selector("Sample Image", IMAGE_URLS, IMAGE_NAMES, activeUrl),

            sectionHeader("Corner Radii"),
            stepper("Top Left", 0f, 40f, 2f, radiusTL, "%.0f px"),
            stepper("Top Right", 0f, 40f, 2f, radiusTR, "%.0f px"),
            stepper("Bottom Right", 0f, 40f, 2f, radiusBR, "%.0f px"),
            stepper("Bottom Left", 0f, 40f, 2f, radiusBL, "%.0f px"),

            sectionHeader("Border Styling"),
            toggle("Enable Border", borderOn),
            stepper("Border Width", 1f, 8f, 0.5f, borderWidth, "%.1f px"),
            colorPicker("Border Color", PRESET_COLORS, borderColor),
            toggle("Dashed Border", dashOn),
            stepper("Dash Length", 2f, 30f, 2f, dashLength, "%.0f px"),
            stepper("Dash Ratio", 0.1f, 0.9f, 0.05f, dashRatio, "%.2f")
        );
    }

    private Seq<Component> buildShadowControls() {
        return Seq.with(
            sectionHeader("Inner Shadow"),
            stepper("Spread Size", 0f, 30f, 2f, innerShadowSpread, "%.0f px"),
            stepper("Blur Size", 0f, 30f, 2f, innerShadowBlur, "%.0f px"),
            colorPicker("Shadow Color", new Color[]{
                Color.valueOf("00000066"), Color.valueOf("000000aa"), Color.valueOf("d9534f66"), Color.valueOf("ff79c666")
            }, innerShadowColor),

            sectionHeader("Outer Glow"),
            stepper("Glow Spread", 0f, 40f, 2f, glowSpread, "%.0f px"),
            colorPicker("Glow Color", new Color[]{
                Color.valueOf("ff79c633"), Color.valueOf("6c8ebf33"), Color.valueOf("5cb85c33"), Color.valueOf("f0c04033")
            }, glowColor)
        );
    }

    private Seq<Component> buildFilterControls() {
        return Seq.with(
            sectionHeader("Backdrop Blur (Glass)"),
            toggle("Enable Glassmorphism", backdropOn),
            stepper("Iterations", 1f, 8f, 1f, backdropIterations, "%.0f times"),
            stepper("Blur Blend", 0.0f, 1.0f, 0.1f, backdropBlend, "%.1f"),
            stepper("Blur Weight", 0.0f, 1.0f, 0.1f, backdropWeight, "%.1f"),

            sectionHeader("Color Filters"),
            stepper("Overall Opacity", 0.1f, 1.0f, 0.05f, opacity, "%.2f"),
            selector("Filter Mode", new Integer[]{ 0, 1, 2, 3, 4 }, new String[]{ "None", "Gray", "Sepia", "Bright", "Invert" }, filterType),
            stepper("Filter Amount", 0.0f, 1.0f, 0.1f, filterAmount, "%.1f"),
            stepper("Noise Grain", 0.0f, 1.0f, 0.05f, noiseAmount, "%.2f")
        );
    }

    private Seq<Component> buildLayoutControls() {
        return Seq.with(
            sectionHeader("Flow Orientation"),
            toggle("Column Direction", sandboxIsColumn),
            toggle("Enable Wrapping", sandboxIsWrap),
            stepper("Gap Spacing", 0f, 32f, 4f, sandboxGap, "%.0f px"),

            sectionHeader("Main Axis Justification"),
            selector("Justify Content",
                new JustifyContent[]{ JustifyContent.START, JustifyContent.CENTER, JustifyContent.END, JustifyContent.SPACE_BETWEEN, JustifyContent.SPACE_AROUND, JustifyContent.SPACE_EVENLY },
                new String[]{ "Start", "Center", "End", "Between", "Around", "Evenly" },
                sandboxJustify
            ),

            sectionHeader("Cross Axis Alignment"),
            selector("Align Items",
                new AlignItems[]{ AlignItems.START, AlignItems.CENTER, AlignItems.END, AlignItems.STRETCH },
                new String[]{ "Start", "Center", "End", "Stretch" },
                sandboxAlign
            )
        );
    }

    private Text sectionHeader(String text) {
        return Text.of().style(t -> t.text("[ff79c6]-- " + text + " --[]").size(0.85f).padding(8f, 0f, 4f, 0f).labelAlign(Align.center));
    }

    // --- Core Reactive Widgets ---

    private Layout toggle(String label, Signal<Boolean> signal) {
        Signal<Boolean> isHovered = new Signal<>(false);
        CustomComponent bg = CustomComponent.of();
        bg.style(s -> {
            if (signal.get()) {
                s.background(isHovered.get() ? Color.valueOf("4cae4c") : Color.valueOf("5cb85c"));
            } else {
                s.background(isHovered.get() ? Color.valueOf("44445c") : Color.valueOf("303042"));
            }
            s.radius(8f);
        });

        return Layout.of()
            .style(s -> s.row().gap(8f).alignItems(AlignItems.CENTER).widthMode(SizeMode.GROW).fixedHeight(32f))
            .children(() -> Seq.with(
                Text.of().style(s -> s.text(label).size(0.8f).growX().labelAlign(Align.left)),
                Layout.of()
                    .background(bg)
                    .style(s -> s.alignItems(AlignItems.CENTER).justifyContent(JustifyContent.CENTER).fixedWidth(45f).fixedHeight(20f))
                    .children(() -> Seq.with(
                        Text.of().style(t -> t.text(signal.get() ? "ON" : "OFF").size(0.7f).color(Color.white))
                    ))
                    .onHover(() -> isHovered.set(true))
                    .onExit(() -> isHovered.set(false))
                    .onClick(() -> signal.set(!signal.get()))
            ));
    }

    private Layout stepper(String label, float min, float max, float step, Signal<Float> signal, String format) {
        Signal<Boolean> minusHovered = new Signal<>(false);
        Signal<Boolean> plusHovered = new Signal<>(false);

        CustomComponent minusBg = CustomComponent.of();
        minusBg.style(s -> s.radius(4f).background(minusHovered.get() ? Color.valueOf("55556a") : Color.valueOf("303042")));

        CustomComponent plusBg = CustomComponent.of();
        plusBg.style(s -> s.radius(4f).background(plusHovered.get() ? Color.valueOf("55556a") : Color.valueOf("303042")));

        return Layout.of()
            .style(s -> s.row().gap(6f).alignItems(AlignItems.CENTER).widthMode(SizeMode.GROW).fixedHeight(32f))
            .children(() -> Seq.with(
                Text.of().style(s -> s.text(label).size(0.8f).growX().labelAlign(Align.left)),
                Layout.of()
                    .background(minusBg)
                    .style(s -> s.alignItems(AlignItems.CENTER).justifyContent(JustifyContent.CENTER).fixedWidth(28f).fixedHeight(22f))
                    .children(() -> Seq.with(Text.of().style(t -> t.text("-").size(0.85f))))
                    .onHover(() -> minusHovered.set(true))
                    .onExit(() -> minusHovered.set(false))
                    .onClick(() -> signal.set(Math.max(min, signal.get() - step))),
                Text.of().style(s -> s.text(String.format(format, signal.get())).size(0.8f).fixedWidth(50f).labelAlign(Align.center)),
                Layout.of()
                    .background(plusBg)
                    .style(s -> s.alignItems(AlignItems.CENTER).justifyContent(JustifyContent.CENTER).fixedWidth(28f).fixedHeight(22f))
                    .children(() -> Seq.with(Text.of().style(t -> t.text("+").size(0.85f))))
                    .onHover(() -> plusHovered.set(true))
                    .onExit(() -> plusHovered.set(false))
                    .onClick(() -> signal.set(Math.min(max, signal.get() + step)))
            ));
    }

    private Layout colorPicker(String label, Color[] colors, Signal<Color> signal) {
        return Layout.of()
            .style(s -> s.column().gap(4f).widthMode(SizeMode.GROW))
            .children(() -> Seq.with(
                Text.of().style(s -> s.text(label).size(0.75f).labelAlign(Align.left)),
                Layout.of()
                    .style(s -> s.row().gap(6f).wrap().fixedHeight(22f))
                    .children(() -> {
                        Seq<Component> swatches = new Seq<>();
                        for (Color c : colors) {
                            swatches.add(colorSwatch(c, signal));
                        }
                        return swatches;
                    })
            ));
    }

    private CustomComponent colorSwatch(Color color, Signal<Color> signal) {
        Signal<Boolean> isHovered = new Signal<>(false);
        CustomComponent swatch = CustomComponent.of();
        swatch.style(s -> {
            s.background(color).radius(4f).fixedWidth(18f).fixedHeight(18f);
            boolean active = signal.get().equals(color);
            if (active) {
                s.border(1.5f, Color.white);
            } else if (isHovered.get()) {
                s.border(1f, Color.valueOf("ffffff99"));
            } else {
                s.borderWidth = 0f;
            }
        });
        swatch.onHover(() -> isHovered.set(true))
              .onExit(() -> isHovered.set(false))
              .onClick(() -> signal.set(color));
        return swatch;
    }

    private <T> Layout selector(String label, T[] options, String[] displayNames, Signal<T> signal) {
        return Layout.of()
            .style(s -> s.column().gap(4f).widthMode(SizeMode.GROW))
            .children(() -> {
                Seq<Component> rowChildren = new Seq<>();
                for (int i = 0; i < options.length; i++) {
                    rowChildren.add(optionButton(options[i], displayNames[i], signal));
                }
                return Seq.with(
                    Text.of().style(s -> s.text(label).size(0.75f).labelAlign(Align.left)),
                    Layout.of()
                        .style(s -> s.row().gap(4f).wrap())
                        .children(() -> rowChildren)
                );
            });
    }

    private <T> Component optionButton(T value, String displayName, Signal<T> signal) {
        Signal<Boolean> isHovered = new Signal<>(false);
        CustomComponent bg = CustomComponent.of();
        bg.style(s -> {
            boolean active = signal.get() == value;
            s.radius(4f);
            if (active) {
                s.background(Color.valueOf("ff79c6"));
            } else {
                s.background(isHovered.get() ? Color.valueOf("44445c") : Color.valueOf("303042"));
            }
        });

        return Layout.of()
            .background(bg)
            .style(s -> s.alignItems(AlignItems.CENTER).justifyContent(JustifyContent.CENTER).fixedHeight(22f).growX())
            .children(() -> Seq.with(
                Text.of().style(t -> t.text(displayName).size(0.7f).color(signal.get() == value ? Color.white : Color.lightGray))
            ))
            .onHover(() -> isHovered.set(true))
            .onExit(() -> isHovered.set(false))
            .onClick(() -> signal.set(value));
    }
}
