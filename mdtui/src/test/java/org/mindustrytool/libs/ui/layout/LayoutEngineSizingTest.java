package org.mindustrytool.libs.ui.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutEngineSizingTest extends LayoutTestBase {

    // ===== FIXED SIZING =====

    @Test
    public void fixedSizingRow() {
        LayoutSpec spec = row().gap(0);
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 25);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(15f, a.height),
            () -> assertEquals(20f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(25f, b.height)
        );
    }

    @Test
    public void fixedSizingColumn() {
        LayoutSpec spec = column().gap(0);
        MockNode a = node("a", 30, 15);
        MockNode b = node("b", 50, 25);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(85f, a.yPosition), () -> assertEquals(15f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(60f, b.yPosition), () -> assertEquals(25f, b.height)
        );
    }

    // ===== GROW SIZING =====

    @Test
    public void singleGrowRow() {
        LayoutSpec spec = row().gap(0);
        MockNode a = node("a", 0, 20);
        a.growX().growWeightHorizontal(1);
        layout(spec, 100, 50, a);
        assertEquals(0f, a.xPosition);
        assertEquals(100f, a.width);
        assertEquals(0f, a.yPosition);
        assertEquals(20f, a.height);
    }

    @Test
    public void singleGrowColumn() {
        LayoutSpec spec = column().gap(0);
        MockNode a = node("a", 30, 0);
        a.growY().growWeightVertical(1);
        layout(spec, 100, 80, a);
        assertEquals(0f, a.xPosition);
        assertEquals(30f, a.width);
        assertEquals(0f, a.yPosition);
        assertEquals(80f, a.height);
    }

    @Test
    public void twoEqualGrowRow() {
        LayoutSpec spec = row().gap(0);
        MockNode a = node("a", 0, 15);
        a.growX().growWeightHorizontal(1);
        MockNode b = node("b", 0, 25);
        b.growX().growWeightHorizontal(1);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(50f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(15f, a.height),
            () -> assertEquals(50f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(25f, b.height)
        );
    }

    @Test
    public void twoEqualGrowColumn() {
        LayoutSpec spec = column().gap(0);
        MockNode a = node("a", 20, 0);
        a.growY().growWeightVertical(1);
        MockNode b = node("b", 30, 0);
        b.growY().growWeightVertical(1);
        layout(spec, 100, 80, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(40f, a.yPosition), () -> assertEquals(40f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(40f, b.height)
        );
    }

    // ===== GROW WITH WEIGHT =====

    @Test
    public void weightedGrowRow() {
        LayoutSpec spec = row().gap(0);
        MockNode a = node("a", 0, 20);
        a.growX().growWeightHorizontal(1);
        MockNode b = node("b", 0, 30);
        b.growX().growWeightHorizontal(3);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(25f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(25f, b.xPosition), () -> assertEquals(75f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void weightedGrowColumn() {
        LayoutSpec spec = column().gap(0);
        MockNode a = node("a", 30, 0);
        a.growY().growWeightVertical(2);
        MockNode b = node("b", 40, 0);
        b.growY().growWeightVertical(1);
        layout(spec, 100, 90, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(30f, a.yPosition), () -> assertEquals(60f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(40f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void threeUnequalGrowRow() {
        LayoutSpec spec = row().gap(0);
        MockNode a = node("a", 0, 10);
        a.growX().growWeightHorizontal(1);
        MockNode b = node("b", 0, 20);
        b.growX().growWeightHorizontal(2);
        MockNode c = node("c", 0, 30);
        c.growX().growWeightHorizontal(3);
        layout(spec, 120, 50, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(10f, a.height),
            () -> assertEquals(20f, b.xPosition), () -> assertEquals(40f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(20f, b.height),
            () -> assertEquals(60f, c.xPosition), () -> assertEquals(60f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(30f, c.height)
        );
    }

    // ===== WRAP SIZING =====

    @Test
    public void wrapSizingRow() {
        LayoutSpec spec = row().gap(0);
        MockNode a = nodeWithSizing("a", sizing(LayoutSpec.SizeMode.FIXED, 20, 15));
        MockNode b = nodeWithSizing("b", sizing(LayoutSpec.SizeMode.FIXED, 30, 25));
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(15f, a.height),
            () -> assertEquals(20f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(25f, b.height)
        );
    }

    @Test
    public void wrapSizingColumn() {
        LayoutSpec spec = column().gap(0);
        MockNode a = nodeWithSizing("a", sizing(LayoutSpec.SizeMode.FIXED, 30, 15));
        MockNode b = nodeWithSizing("b", sizing(LayoutSpec.SizeMode.FIXED, 50, 25));
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(85f, a.yPosition), () -> assertEquals(15f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(60f, b.yPosition), () -> assertEquals(25f, b.height)
        );
    }

    // ===== MIXED FIXED + GROW + WRAP =====

    @Test
    public void mixedFixedGrowWrapRow() {
        LayoutSpec spec = row().gap(5);
        MockNode a = node("a", 15, 10);
        MockNode b = node("b", 0, 20);
        b.growX().growWeightHorizontal(1);
        MockNode c = nodeWithSizing("c", sizing(LayoutSpec.SizeMode.FIXED, 20, 15));
        layout(spec, 100, 40, a, b, c);
        float growW = 100f - 15f - 5f - 20f - 5f;
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(15f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(10f, a.height),
            () -> assertEquals(20f, b.xPosition), () -> assertEquals(growW, b.width, EPS),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(20f, b.height),
            () -> assertEquals(20f + growW + 5f, c.xPosition, EPS), () -> assertEquals(20f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(15f, c.height)
        );
    }

    @Test
    public void mixedFixedGrowWrapColumn() {
        LayoutSpec spec = column().gap(5);
        MockNode a = node("a", 30, 10);
        MockNode b = node("b", 40, 0);
        b.growY().growWeightVertical(1);
        MockNode c = nodeWithSizing("c", sizing(LayoutSpec.SizeMode.FIXED, 20, 15));
        layout(spec, 100, 100, a, b, c);
        float growH = 100f - 10f - 5f - 15f - 5f;
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(90f, a.yPosition), () -> assertEquals(10f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(40f, b.width),
            () -> assertEquals(20f, b.yPosition), () -> assertEquals(growH, b.height, EPS),
            () -> assertEquals(0f, c.xPosition), () -> assertEquals(20f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(15f, c.height)
        );
    }

    // ===== GROW WITH GAPS =====

    @Test
    public void growWithGapRow() {
        LayoutSpec spec = row().gap(10);
        MockNode a = node("a", 0, 15);
        a.growX().growWeightHorizontal(1);
        MockNode b = node("b", 0, 25);
        b.growX().growWeightHorizontal(1);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(45f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(15f, a.height),
            () -> assertEquals(55f, b.xPosition), () -> assertEquals(45f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(25f, b.height)
        );
    }

    @Test
    public void growWithGapColumn() {
        LayoutSpec spec = column().gap(8);
        MockNode a = node("a", 30, 0);
        a.growY().growWeightVertical(1);
        MockNode b = node("b", 40, 0);
        b.growY().growWeightVertical(1);
        layout(spec, 100, 80, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(44f, a.yPosition), () -> assertEquals(36f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(40f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(36f, b.height)
        );
    }

    // ===== ZERO WEIGHT GROW =====

    @Test
    public void zeroGrowWeightIsTreatedAsFixed() {
        MockNode a = node("a", 30, 20);
        a.growX().growY().growWeightHorizontal(0).growWeightVertical(0);
        layout(row().gap(0), 100, 50, a);
        assertEquals(0f, a.xPosition);
        assertEquals(100f, a.width);
        assertEquals(0f, a.yPosition);
        assertEquals(20f, a.height);
    }

    // ===== GROW WITH PREFERRED SIZE =====

    @Test
    public void growWithPrefSizeDoesNotGrowBelow() {
        LayoutSpec spec = row().gap(0);
        MockNode a = node("a", 80, 20);
        a.growX().growWeightHorizontal(1);
        MockNode b = node("b", 10, 10);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(90f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(90f, b.xPosition), () -> assertEquals(10f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(10f, b.height)
        );
    }

    // ===== NULL SIZING =====

    @Test
    public void nullSizingDefaultsToWrap() {
        LayoutSpec spec = row().gap(0);
        MockNode a = node(null, 0, 0);
        a.set("prefW", 25f); a.set("prefH", 15f);
        a.set("sizing", null);
        MockNode b = node("b", 20, 10);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(25f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(15f, a.height),
            () -> assertEquals(25f, b.xPosition), () -> assertEquals(20f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(10f, b.height)
        );
    }

    // ===== PARAMETERIZED: GROW DISTRIBUTION =====

    static Stream<Arguments> growDistributionParams() {
        return Stream.of(
            Arguments.of(1, 1, 50f, 50f),
            Arguments.of(1, 3, 25f, 75f),
            Arguments.of(3, 1, 75f, 25f),
            Arguments.of(2, 2, 50f, 50f),
            Arguments.of(4, 1, 80f, 20f)
        );
    }

    @ParameterizedTest
    @MethodSource("growDistributionParams")
    public void growDistributionRow(int w1, int w2, float expectedW1, float expectedW2) {
        LayoutSpec spec = row().gap(0);
        MockNode a = node("a", 0, 15);
        a.growX().growWeightHorizontal(w1);
        MockNode b = node("b", 0, 20);
        b.growX().growWeightHorizontal(w2);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(expectedW1, a.width, EPS),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(15f, a.height),
            () -> assertEquals(expectedW1, b.xPosition, EPS), () -> assertEquals(expectedW2, b.width, EPS),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(20f, b.height)
        );
    }

    @ParameterizedTest
    @MethodSource("growDistributionParams")
    public void growDistributionColumn(int w1, int w2, float expectedH1, float expectedH2) {
        LayoutSpec spec = column().gap(0);
        MockNode a = node("a", 20, 0);
        a.growY().growWeightVertical(w1);
        MockNode b = node("b", 30, 0);
        b.growY().growWeightVertical(w2);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(100f - expectedH1, a.yPosition, EPS), () -> assertEquals(expectedH1, a.height, EPS),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(100f - expectedH1 - expectedH2, b.yPosition, EPS), () -> assertEquals(expectedH2, b.height, EPS)
        );
    }
}
