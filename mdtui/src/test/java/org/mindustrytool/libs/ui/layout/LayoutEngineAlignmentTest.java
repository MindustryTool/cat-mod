package org.mindustrytool.libs.ui.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutEngineAlignmentTest extends LayoutTestBase {

    // ===== ROW: ALL ALIGN ITEMS =====

    @Test
    public void rowAlignItemsStart() {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.START).gap(0);
        MockNode a = node("a", 20, 20);
        MockNode b = node("b", 30, 30);
        layout(spec, 100, 60, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(20f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void rowAlignItemsCenter() {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.CENTER).gap(0);
        MockNode a = node("a", 20, 20);
        MockNode b = node("b", 30, 30);
        layout(spec, 100, 60, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(5f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(20f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void rowAlignItemsEnd() {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.END).gap(0);
        MockNode a = node("a", 20, 20);
        MockNode b = node("b", 30, 30);
        layout(spec, 100, 60, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(10f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(20f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void rowAlignItemsStretch() {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.STRETCH).gap(0);
        MockNode a = node("a", 20, 20);
        MockNode b = node("b", 30, 30);
        layout(spec, 100, 60, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(60f, a.height),
            () -> assertEquals(20f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(60f, b.height)
        );
    }

    // ===== COLUMN: ALL ALIGN ITEMS =====

    @Test
    public void columnAlignItemsStart() {
        LayoutSpec spec = new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.START).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 50, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(80f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(50f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void columnAlignItemsCenter() {
        LayoutSpec spec = new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.CENTER).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 50, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(35f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(80f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(25f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(50f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void columnAlignItemsEnd() {
        LayoutSpec spec = new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.END).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 50, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(70f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(80f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(50f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(50f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void columnAlignItemsStretch() {
        LayoutSpec spec = new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.STRETCH).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 50, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(80f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(50f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    // ===== ALIGN SELF OVERRIDE: ALL 5 MODES IN ROW =====

    static Stream<Arguments> alignSelfRowParams() {
        return Stream.of(
            Arguments.of(NodeSpec.AlignSelf.AUTO, 0f),
            Arguments.of(NodeSpec.AlignSelf.START, 0f),
            Arguments.of(NodeSpec.AlignSelf.CENTER, 15f),
            Arguments.of(NodeSpec.AlignSelf.END, 30f),
            Arguments.of(NodeSpec.AlignSelf.STRETCH, 0f)
        );
    }

    @ParameterizedTest
    @MethodSource("alignSelfRowParams")
    public void rowAlignSelfOverride(NodeSpec.AlignSelf alignSelf, float expectedY) {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.START).gap(0);
        MockNode a = node("a", 20, 20);
        MockNode b = nodeWithSizing("b", sizing(NodeSpec.SizeMode.FIXED, 20, 30).alignSelf(alignSelf));
        MockNode c = node("c", 20, 15);
        layout(spec, 100, 50, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(20f, b.xPosition), () -> assertEquals(20f, b.width),
            () -> assertEquals(expectedY, b.yPosition, EPS), () -> assertEquals(30f, b.height),
            () -> assertEquals(40f, c.xPosition), () -> assertEquals(20f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(15f, c.height)
        );
    }

    @Test
    public void rowAlignSelfStretch() {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.START).gap(0);
        MockNode a = node("a", 20, 20);
        MockNode b = nodeWithSizing("b", sizing(NodeSpec.SizeMode.FIXED, 20, 30).alignSelf(NodeSpec.AlignSelf.STRETCH));
        MockNode c = node("c", 20, 15);
        layout(spec, 100, 80, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(20f, b.xPosition), () -> assertEquals(20f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(80f, b.height),
            () -> assertEquals(40f, c.xPosition), () -> assertEquals(20f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(15f, c.height)
        );
    }

    // ===== ALIGN SELF IN CENTER CONTAINER =====

    static Stream<Arguments> alignSelfInCenterRowParams() {
        return Stream.of(
            Arguments.of(NodeSpec.AlignSelf.AUTO, 10f),
            Arguments.of(NodeSpec.AlignSelf.START, 0f),
            Arguments.of(NodeSpec.AlignSelf.CENTER, 15f),
            Arguments.of(NodeSpec.AlignSelf.END, 30f),
            Arguments.of(NodeSpec.AlignSelf.STRETCH, 0f)
        );
    }

    @ParameterizedTest
    @MethodSource("alignSelfInCenterRowParams")
    public void rowAlignSelfOverridesCenterContainer(NodeSpec.AlignSelf alignSelf, float expectedY) {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.CENTER).gap(0);
        MockNode a = nodeWithSizing("a", sizing(NodeSpec.SizeMode.FIXED, 30, 20).alignSelf(alignSelf));
        layout(spec, 100, 50, a);
        assertEquals(expectedY, a.yPosition, EPS);
        assertEquals(30f, a.width);
        assertEquals(20f, a.height);
    }

    // ===== ALIGN SELF IN END CONTAINER =====

    static Stream<Arguments> alignSelfInEndRowParams() {
        return Stream.of(
            Arguments.of(NodeSpec.AlignSelf.AUTO, 30f),
            Arguments.of(NodeSpec.AlignSelf.START, 0f),
            Arguments.of(NodeSpec.AlignSelf.CENTER, 15f),
            Arguments.of(NodeSpec.AlignSelf.END, 30f),
            Arguments.of(NodeSpec.AlignSelf.STRETCH, 0f)
        );
    }

    @ParameterizedTest
    @MethodSource("alignSelfInEndRowParams")
    public void rowAlignSelfOverridesEndContainer(NodeSpec.AlignSelf alignSelf, float expectedY) {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.END).gap(0);
        MockNode a = nodeWithSizing("a", sizing(NodeSpec.SizeMode.FIXED, 30, 20).alignSelf(alignSelf));
        layout(spec, 100, 50, a);
        assertEquals(expectedY, a.yPosition, EPS);
        assertEquals(30f, a.width);
        assertEquals(20f, a.height);
    }

    // ===== MULTIPLE CHILDREN WITH DIFFERENT ALIGN SELF =====

    @Test
    public void rowMixedAlignSelf() {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.START).gap(5);
        MockNode a = nodeWithSizing("a", sizing(NodeSpec.SizeMode.FIXED, 15, 20).alignSelf(NodeSpec.AlignSelf.START));
        MockNode b = nodeWithSizing("b", sizing(NodeSpec.SizeMode.FIXED, 15, 20).alignSelf(NodeSpec.AlignSelf.CENTER));
        MockNode c = nodeWithSizing("c", sizing(NodeSpec.SizeMode.FIXED, 15, 20).alignSelf(NodeSpec.AlignSelf.END));
        MockNode d = nodeWithSizing("d", sizing(NodeSpec.SizeMode.FIXED, 15, 20).alignSelf(NodeSpec.AlignSelf.STRETCH));
        MockNode e = nodeWithSizing("e", sizing(NodeSpec.SizeMode.FIXED, 15, 20).alignSelf(NodeSpec.AlignSelf.AUTO));
        layout(spec, 100, 60, a, b, c, d, e);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(15f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(20f, b.xPosition), () -> assertEquals(15f, b.width),
            () -> assertEquals(20f, b.yPosition, EPS), () -> assertEquals(20f, b.height),
            () -> assertEquals(40f, c.xPosition), () -> assertEquals(15f, c.width),
            () -> assertEquals(40f, c.yPosition), () -> assertEquals(20f, c.height),
            () -> assertEquals(60f, d.xPosition), () -> assertEquals(15f, d.width),
            () -> assertEquals(0f, d.yPosition), () -> assertEquals(60f, d.height),
            () -> assertEquals(80f, e.xPosition), () -> assertEquals(15f, e.width),
            () -> assertEquals(0f, e.yPosition), () -> assertEquals(20f, e.height)
        );
    }

    @Test
    public void columnMixedAlignSelf() {
        LayoutSpec spec = new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.START).gap(5);
        MockNode a = nodeWithSizing("a", sizing(NodeSpec.SizeMode.FIXED, 50, 15).alignSelf(NodeSpec.AlignSelf.START));
        MockNode b = nodeWithSizing("b", sizing(NodeSpec.SizeMode.FIXED, 50, 15).alignSelf(NodeSpec.AlignSelf.CENTER));
        MockNode c = nodeWithSizing("c", sizing(NodeSpec.SizeMode.FIXED, 50, 15).alignSelf(NodeSpec.AlignSelf.END));
        MockNode d = nodeWithSizing("d", sizing(NodeSpec.SizeMode.FIXED, 50, 15).alignSelf(NodeSpec.AlignSelf.STRETCH));
        layout(spec, 100, 100, a, b, c, d);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(50f, a.width),
            () -> assertEquals(85f, a.yPosition), () -> assertEquals(15f, a.height),
            () -> assertEquals(25f, b.xPosition, EPS), () -> assertEquals(50f, b.width),
            () -> assertEquals(65f, b.yPosition), () -> assertEquals(15f, b.height),
            () -> assertEquals(50f, c.xPosition), () -> assertEquals(50f, c.width),
            () -> assertEquals(45f, c.yPosition), () -> assertEquals(15f, c.height),
            () -> assertEquals(0f, d.xPosition), () -> assertEquals(100f, d.width),
            () -> assertEquals(25f, d.yPosition), () -> assertEquals(15f, d.height)
        );
    }

    // ===== STRETCH WITH GROW =====

    @Test
    public void rowStretchWithGrow() {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.STRETCH).gap(0);
        MockNode a = nodeWithSizing("a", sizing(NodeSpec.SizeMode.GROW, 0, 20).growWeightHorizontal(1));
        MockNode b = nodeWithSizing("b", sizing(NodeSpec.SizeMode.GROW, 0, 30).growWeightHorizontal(1));
        layout(spec, 100, 60, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(50f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(60f, a.height),
            () -> assertEquals(50f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(60f, b.height)
        );
    }

    @Test
    public void columnStretchWithGrow() {
        LayoutSpec spec = new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.STRETCH).gap(0);
        MockNode a = nodeWithSizing("a", sizing(NodeSpec.SizeMode.GROW, 30, 0).growWeightVertical(1));
        MockNode b = nodeWithSizing("b", sizing(NodeSpec.SizeMode.GROW, 40, 0).growWeightVertical(1));
        layout(spec, 100, 80, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(40f, a.yPosition), () -> assertEquals(40f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(40f, b.height)
        );
    }

    // ===== STRETCH WITH PREFERRED HEIGHT =====

    @Test
    public void rowStretchRespectsPrefHeight() {
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 25);
        layout(row().alignItems(LayoutSpec.AlignItems.STRETCH).gap(0), 100, 60, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(60f, a.height),
            () -> assertEquals(30f, b.xPosition), () -> assertEquals(40f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(60f, b.height)
        );
    }

    @Test
    public void columnStretchRespectsPrefWidth() {
        MockNode a = node("a", 40, 20);
        MockNode b = node("b", 60, 30);
        layout(column().alignItems(LayoutSpec.AlignItems.STRETCH).gap(0), 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(80f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(50f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    // ===== FULL ASSERTION: ALL 4 ALIGN ITEMS ROW =====

    @ParameterizedTest
    @MethodSource("alignModes")
    public void rowAlignItemsAllModes(LayoutSpec.AlignItems ai) {
        LayoutSpec spec = new LayoutSpec().row().alignItems(ai).gap(0);
        MockNode a = node("a", 20, 20);
        MockNode b = node("b", 30, 30);
        layout(spec, 100, 50, a, b);
        assertInvariants(spec, list(a, b), 0, 0, 100, 50);
    }

    @ParameterizedTest
    @MethodSource("alignModes")
    public void columnAlignItemsAllModes(LayoutSpec.AlignItems ai) {
        LayoutSpec spec = new LayoutSpec().column().alignItems(ai).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(spec, 100, 100, a, b);
        assertInvariants(spec, list(a, b), 0, 0, 100, 100);
    }

    // ===== FULL ASSERTION: ALL ALIGN SELF IN ROW =====

    @ParameterizedTest
    @MethodSource("alignSelfModes")
    public void rowAlignSelfAllModes(NodeSpec.AlignSelf alignSelf) {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.START).gap(0);
        MockNode a = node("a", 10, 10);
        MockNode b = nodeWithSizing("b", sizing(NodeSpec.SizeMode.FIXED, 20, 20).alignSelf(alignSelf));
        MockNode c = node("c", 10, 10);
        layout(spec, 100, 40, a, b, c);
        assertInvariants(spec, list(a, b, c), 0, 0, 100, 40);
    }

    @ParameterizedTest
    @MethodSource("alignSelfModes")
    public void columnAlignSelfAllModes(NodeSpec.AlignSelf alignSelf) {
        LayoutSpec spec = new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.START).gap(0);
        MockNode a = node("a", 10, 10);
        MockNode b = nodeWithSizing("b", sizing(NodeSpec.SizeMode.FIXED, 20, 20).alignSelf(alignSelf));
        MockNode c = node("c", 10, 10);
        layout(spec, 100, 100, a, b, c);
        assertInvariants(spec, list(a, b, c), 0, 0, 100, 100);
    }

    // ===== ALIGN ITEMS + ALIGN SELF COMBINATIONS =====

    @ParameterizedTest
    @MethodSource("alignModes")
    public void rowAlignItemsWithAlignSelfCenter(LayoutSpec.AlignItems ai) {
        LayoutSpec spec = new LayoutSpec().row().alignItems(ai).gap(0);
        MockNode a = node("a", 15, 15);
        MockNode b = nodeWithSizing("b", sizing(NodeSpec.SizeMode.FIXED, 20, 25).alignSelf(NodeSpec.AlignSelf.CENTER));
        MockNode c = node("c", 15, 15);
        layout(spec, 100, 60, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(15f, a.width),
            () -> assertEquals(15f, c.xPosition), () -> assertEquals(15f, c.width),
            () -> assertEquals(30f, b.xPosition), () -> assertEquals(20f, b.width)
        );
    }

    @ParameterizedTest
    @MethodSource("alignModes")
    public void rowAlignItemsWithAlignSelfEnd(LayoutSpec.AlignItems ai) {
        LayoutSpec spec = new LayoutSpec().row().alignItems(ai).gap(0);
        MockNode a = node("a", 15, 15);
        MockNode b = nodeWithSizing("b", sizing(NodeSpec.SizeMode.FIXED, 20, 25).alignSelf(NodeSpec.AlignSelf.END));
        MockNode c = node("c", 15, 15);
        layout(spec, 100, 60, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(15f, a.width),
            () -> assertEquals(15f, c.xPosition), () -> assertEquals(15f, c.width),
            () -> assertEquals(30f, b.xPosition), () -> assertEquals(20f, b.width)
        );
    }

    // ===== STRETCH WITH MIXED SIZING =====

    @Test
    public void rowStretchWithMixedFixedAndGrow() {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.STRETCH).gap(0);
        MockNode a = node("a", 30, 15);
        MockNode b = nodeWithSizing("b", sizing(NodeSpec.SizeMode.GROW, 0, 0).growWeightHorizontal(1));
        MockNode c = node("c", 20, 20);
        layout(spec, 100, 50, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(30f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height),
            () -> assertEquals(80f, c.xPosition), () -> assertEquals(20f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(50f, c.height)
        );
    }
}
