package org.mindustrytool.libs.ui.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutEngineReverseTest extends LayoutTestBase {

    // ===== ROW REVERSE =====

    @Test
    public void rowReverse() {
        LayoutSpec spec = LayoutSpec.builder().row().reverse(true).gap(0).build();
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 25);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(80f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(50f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void columnReverse() {
        LayoutSpec spec = LayoutSpec.builder().column().reverse(true).gap(0).build();
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(20f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    // ===== REVERSE + JUSTIFY =====

    @Test
    public void rowReverseJustifyCenter() {
        LayoutSpec spec = LayoutSpec.builder().row().reverse(true).justifyContent(LayoutSpec.JustifyContent.CENTER).gap(0).build();
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 25);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(55f, a.xPosition, EPS), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(25f, b.xPosition, EPS), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void rowReverseJustifyEnd() {
        LayoutSpec spec = LayoutSpec.builder().row().reverse(true).justifyContent(LayoutSpec.JustifyContent.END).gap(0).build();
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 25);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(30f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void rowReverseJustifyStart() {
        LayoutSpec spec = LayoutSpec.builder().row().reverse(true).justifyContent(LayoutSpec.JustifyContent.START).gap(0).build();
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 25);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(80f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(50f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void columnReverseJustifyCenter() {
        LayoutSpec spec = LayoutSpec.builder().column().reverse(true).justifyContent(LayoutSpec.JustifyContent.CENTER).gap(0).build();
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(25f, a.yPosition, EPS), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(45f, b.yPosition, EPS), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void columnReverseJustifyEnd() {
        LayoutSpec spec = LayoutSpec.builder().column().reverse(true).justifyContent(LayoutSpec.JustifyContent.END).gap(0).build();
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(50f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(70f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void columnReverseJustifyStart() {
        LayoutSpec spec = LayoutSpec.builder().column().reverse(true).justifyContent(LayoutSpec.JustifyContent.START).gap(0).build();
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(20f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    // ===== REVERSE + GAP =====

    @Test
    public void rowReverseWithGap() {
        LayoutSpec spec = LayoutSpec.builder().row().reverse(true).gap(10).build();
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 25);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(80f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(40f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void columnReverseWithGap() {
        LayoutSpec spec = LayoutSpec.builder().column().reverse(true).gap(10).build();
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(30f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    // ===== REVERSE + WRAP =====

    @Test
    public void rowReverseWrap() {
        LayoutSpec spec = LayoutSpec.builder().row().reverse(true).wrap().gap(0).build();
        MockNode a = node("a", 50, 20);
        MockNode b = node("b", 50, 25);
        MockNode c = node("c", 50, 30);
        layout(spec, 100, 80, a, b, c);
        assertAll(
            () -> assertEquals(50f, a.xPosition), () -> assertEquals(50f, a.width),
            () -> assertEquals(55f, a.yPosition), () -> assertEquals(25f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(55f, b.yPosition), () -> assertEquals(25f, b.height),
            () -> assertEquals(50f, c.xPosition), () -> assertEquals(50f, c.width),
            () -> assertEquals(25f, c.yPosition), () -> assertEquals(30f, c.height)
        );
    }

    @Test
    public void columnReverseWrap() {
        LayoutSpec spec = LayoutSpec.builder().column().reverse(true).wrap().gap(0).build();
        MockNode a = node("a", 40, 40);
        MockNode b = node("b", 50, 40);
        MockNode c = node("c", 60, 40);
        layout(spec, 150, 80, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(50f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(40f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(40f, b.yPosition), () -> assertEquals(40f, b.height),
            () -> assertEquals(50f, c.xPosition), () -> assertEquals(60f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(40f, c.height)
        );
    }

    // ===== REVERSE + GROW =====

    @Test
    public void rowReverseGrow() {
        LayoutSpec spec = LayoutSpec.builder().row().reverse(true).gap(0).build();
        MockNode a = nodeWithSizing("a", sizing(LayoutSpec.SizeMode.GROW, 0, 15).toBuilder().growWeightHorizontal(1).build());
        MockNode b = nodeWithSizing("b", sizing(LayoutSpec.SizeMode.GROW, 0, 25).toBuilder().growWeightHorizontal(3).build());
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(75f, a.xPosition), () -> assertEquals(25f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(75f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void columnReverseGrow() {
        LayoutSpec spec = LayoutSpec.builder().column().reverse(true).gap(0).build();
        MockNode a = nodeWithSizing("a", sizing(LayoutSpec.SizeMode.GROW, 30, 0).toBuilder().growWeightVertical(2).build());
        MockNode b = nodeWithSizing("b", sizing(LayoutSpec.SizeMode.GROW, 40, 0).toBuilder().growWeightVertical(1).build());
        layout(spec, 100, 90, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(60f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(60f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    // ===== REVERSE WITH THREE CHILDREN =====

    @Test
    public void rowReverseThreeChildren() {
        LayoutSpec spec = LayoutSpec.builder().row().reverse(true).gap(0).build();
        MockNode a = node("a", 10, 10);
        MockNode b = node("b", 15, 15);
        MockNode c = node("c", 20, 20);
        layout(spec, 100, 40, a, b, c);
        assertAll(
            () -> assertEquals(90f, a.xPosition), () -> assertEquals(10f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(40f, a.height),
            () -> assertEquals(75f, b.xPosition), () -> assertEquals(15f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(40f, b.height),
            () -> assertEquals(55f, c.xPosition), () -> assertEquals(20f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(40f, b.height)
        );
    }

    @Test
    public void columnReverseThreeChildren() {
        LayoutSpec spec = LayoutSpec.builder().column().reverse(true).gap(0).build();
        MockNode a = node("a", 30, 10);
        MockNode b = node("b", 40, 15);
        MockNode c = node("c", 50, 20);
        layout(spec, 100, 100, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(10f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(10f, b.yPosition), () -> assertEquals(15f, b.height),
            () -> assertEquals(0f, c.xPosition), () -> assertEquals(100f, c.width),
            () -> assertEquals(25f, c.yPosition), () -> assertEquals(20f, c.height)
        );
    }

    // ===== PARAMETERIZED REVERSE =====

    static Stream<Arguments> reverseJustifyParams() {
        return Stream.of(
            Arguments.of(LayoutSpec.JustifyContent.START, 80f, 50f),
            Arguments.of(LayoutSpec.JustifyContent.CENTER, 55f, 25f),
            Arguments.of(LayoutSpec.JustifyContent.END, 30f, 0f),
            Arguments.of(LayoutSpec.JustifyContent.SPACE_BETWEEN, 80f, 0f),
            Arguments.of(LayoutSpec.JustifyContent.SPACE_AROUND, 67.5f, 12.5f),
            Arguments.of(LayoutSpec.JustifyContent.SPACE_EVENLY, 190f / 3f, 50f / 3f)
        );
    }

    static Stream<Arguments> columnReverseJustifyParams() {
        return Stream.of(
            Arguments.of(LayoutSpec.JustifyContent.START, 0f, 20f),
            Arguments.of(LayoutSpec.JustifyContent.CENTER, 25f, 45f),
            Arguments.of(LayoutSpec.JustifyContent.END, 50f, 70f),
            Arguments.of(LayoutSpec.JustifyContent.SPACE_BETWEEN, 0f, 70f),
            Arguments.of(LayoutSpec.JustifyContent.SPACE_AROUND, 12.5f, 57.5f),
            Arguments.of(LayoutSpec.JustifyContent.SPACE_EVENLY, 50f / 3f, 50f / 3f + 20f + 50f / 3f)
        );
    }

    @ParameterizedTest
    @MethodSource("reverseJustifyParams")
    public void rowReverseJustifyParam(LayoutSpec.JustifyContent jc, float exA, float exB) {
        LayoutSpec spec = LayoutSpec.builder().row().reverse(true).justifyContent(jc).gap(0).build();
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 20);
        layout(spec, 100, 50, a, b);
        assertEquals(exA, a.xPosition, EPS);
        assertEquals(20f, a.width);
        assertEquals(exB, b.xPosition, EPS);
        assertEquals(30f, b.width);
    }

    @ParameterizedTest
    @MethodSource("columnReverseJustifyParams")
    public void columnReverseJustifyParam(LayoutSpec.JustifyContent jc, float eyA, float eyB) {
        LayoutSpec spec = LayoutSpec.builder().column().reverse(true).justifyContent(jc).gap(0).build();
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(spec, 100, 100, a, b);
        assertEquals(eyA, a.yPosition, EPS);
        assertEquals(20f, a.height);
        assertEquals(eyB, b.yPosition, EPS);
        assertEquals(30f, b.height);
    }

    // ===== REVERSE INVARIANTS =====

    static Stream<Arguments> reverseJustifyModes() {
        return justifyModes();
    }

    @ParameterizedTest
    @MethodSource("reverseJustifyModes")
    public void rowReverseInvariants(LayoutSpec.JustifyContent jc) {
        LayoutSpec spec = LayoutSpec.builder().row().reverse(true).justifyContent(jc).gap(0).build();
        MockNode a = node("a", 15, 10);
        MockNode b = node("b", 20, 15);
        MockNode c = node("c", 25, 20);
        layout(spec, 120, 50, a, b, c);
        assertInvariants(spec, list(a, b, c), 0, 0, 120, 50);
    }

    @ParameterizedTest
    @MethodSource("reverseJustifyModes")
    public void columnReverseInvariants(LayoutSpec.JustifyContent jc) {
        LayoutSpec spec = LayoutSpec.builder().column().reverse(true).justifyContent(jc).gap(0).build();
        MockNode a = node("a", 30, 10);
        MockNode b = node("b", 40, 15);
        MockNode c = node("c", 50, 20);
        layout(spec, 100, 80, a, b, c);
        assertInvariants(spec, list(a, b, c), 0, 0, 100, 80);
    }
}
