package org.mindustrytool.libs.ui.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutEnginePrefSizeTest extends LayoutTestBase {

    // ===== PREFERRED SIZE: ROW =====

    @Test
    public void rowPrefWidthSetsChildWidth() {
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 25);
        a.set("prefW", 25f); a.set("prefH", 20f);
        layout(row().gap(0), 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(25f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(25f, b.xPosition)
        );
    }

    @Test
    public void rowPrefSizeForWrapNode() {
        MockNode a = nodeWithSizing("a", sizing(NodeSpec.SizeMode.FIXED, 40, 20));
        MockNode b = nodeWithSizing("b", sizing(NodeSpec.SizeMode.FIXED, 60, 30));
        layout(row().gap(0), 120, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(40f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(40f, b.xPosition), () -> assertEquals(60f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void columnPrefSizeSetsChildHeight() {
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        a.set("prefH", 25f);
        b.set("prefH", 35f);
        layout(column().gap(0), 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(75f, a.yPosition), () -> assertEquals(25f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(40f, b.width),
            () -> assertEquals(40f, b.yPosition), () -> assertEquals(35f, b.height)
        );
    }

    // ===== PREFERRED SIZE WITH GAP =====

    @Test
    public void rowPrefSizeWithGap() {
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 25);
        a.set("prefW", 35f);
        layout(row().gap(10), 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(35f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(15f, a.height),
            () -> assertEquals(45f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(25f, b.height)
        );
    }

    @Test
    public void columnPrefSizeWithGap() {
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        a.set("prefH", 25f);
        layout(column().gap(8), 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(75f, a.yPosition), () -> assertEquals(25f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(40f, b.width),
            () -> assertEquals(37f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    // ===== PREFERRED SIZE WITH CONSTRAINTS =====

    @Test
    public void prefSizeClampedByMinWidth() {
        MockNode a = node("a", 20, 15);
        a.set("prefW", 10f);
        a.getSizingObj().minimumWidth(25);
        layout(row().gap(0), 100, 50, a);
        assertEquals(25f, a.width);
        assertEquals(0f, a.xPosition);
    }

    @Test
    public void prefSizeClampedByMaxWidth() {
        MockNode a = node("a", 20, 15);
        a.set("prefW", 50f);
        a.getSizingObj().maximumWidth(30);
        layout(row().gap(0), 100, 50, a);
        assertEquals(30f, a.width);
    }

    // ===== PREFERRED SIZE WITH PADDING =====

    @Test
    public void prefSizeWithChildPadding() {
        MockNode a = node("a", 20, 15);
        a.set("prefW", 30f);
        a.getSizingObj().padding(5);
        layout(row().gap(0), 100, 50, a);
        assertEquals(0f, a.xPosition);
        assertEquals(30f, a.width);
        assertEquals(0f, a.yPosition);
        assertEquals(15f, a.height);
    }

    // ===== PREFERRED SIZE WITH INVISIBLE CHILDREN =====

    @Test
    public void invisibleChildPrefSizeIgnored() {
        LayoutSpec spec = new LayoutSpec().row().gap(0);
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 25);
        MockNode c = node("c", 10, 10);
        a.set("visible", false);
        a.set("prefW", 100f);
        layout(spec, 100, 50, a, b, c);
        assertAll(
            () -> assertEquals(-9999f, a.xPosition), () -> assertEquals(-1f, a.width),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(30f, c.xPosition)
        );
    }

    // ===== PREFERRED SIZE: FIXED CONTAINER =====

    @Test
    public void prefSizeInFixedContainer() {
        LayoutSpec spec = new LayoutSpec().row().gap(0);
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 25);
        a.set("prefW", 50f); a.set("prefH", 30f);
        layout(spec, 100, 60, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(50f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(60f, a.height),
            () -> assertEquals(50f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(60f, b.height)
        );
    }

    // ===== PREFERRED SIZE: NO CHILDREN =====

    @Test
    public void prefSizeWithNoChildren() {
        LayoutSpec spec = new LayoutSpec().row().gap(0);
        layout(spec, 100, 50);
    }

    // ===== PREFERRED SIZE WITH NULL SIZING =====

    @Test
    public void nullSizingUsesPrefSize() {
        MockNode a = node(null, 0, 0);
        a.set("prefW", 35f); a.set("prefH", 20f);
        a.set("sizing", null);
        layout(row().gap(0), 100, 50, a);
        assertEquals(0f, a.xPosition);
        assertEquals(35f, a.width);
        assertEquals(0f, a.yPosition);
        assertEquals(20f, a.height);
    }

    // ===== PREFERRED SIZE IN COLUMN WITH PADDING =====

    @Test
    public void columnPrefSizeWithContainerPadding() {
        LayoutSpec spec = new LayoutSpec().column().padding(10).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        a.set("prefH", 15f);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(85f, a.yPosition), () -> assertEquals(15f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(55f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    // ===== PREFERRED SIZE EXACT MATCH =====

    @Test
    public void prefSizeExactMatch() {
        MockNode a = node("a", 20, 15);
        a.set("prefW", 20f); a.set("prefH", 15f);
        layout(row().gap(0), 100, 50, a);
        assertEquals(0f, a.xPosition);
        assertEquals(20f, a.width);
        assertEquals(0f, a.yPosition);
        assertEquals(15f, a.height);
    }

    // ===== PREF SIZE ACROSS ALL SIZING MODES =====

    static Stream<Arguments> prefSizeSizingModes() {
        return Stream.of(
            Arguments.of(NodeSpec.SizeMode.FIXED),
            Arguments.of(NodeSpec.SizeMode.WRAP),
            Arguments.of(NodeSpec.SizeMode.GROW)
        );
    }

    @ParameterizedTest
    @MethodSource("prefSizeSizingModes")
    public void rowPrefSizeAcrossModes(NodeSpec.SizeMode mode) {
        LayoutSpec spec = new LayoutSpec().row().gap(0);
        MockNode a = nodeWithSizing("a", sizing(mode, 0, 0).minimumWidth(0).maximumWidth(500));
        MockNode b = nodeWithSizing("b", sizing(mode, 0, 0).minimumWidth(0).maximumWidth(500));
        a.set("prefW", 30f); a.set("prefH", 15f);
        b.set("prefW", 40f); b.set("prefH", 20f);
        if (mode == NodeSpec.SizeMode.GROW) {
            a.getSizingObj().growWeightHorizontal(1);
            b.getSizingObj().growWeightHorizontal(1);
        }
        layout(spec, 100, 50, a, b);
    }

    // ===== PREFERRED SIZE WITH MULTIPLE CHILDREN OVERFLOW =====

    @Test
    public void prefSizeOverflowLeftToRight() {
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 25);
        MockNode c = node("c", 15, 10);
        a.set("prefW", 60f);
        layout(row().gap(0), 100, 50, a, b, c);
        assertEquals(0f, a.xPosition);
        assertEquals(60f, a.width);
        assertEquals(60f, b.xPosition);
        assertEquals(30f, b.width);
        assertEquals(90f, c.xPosition);
        assertEquals(15f, c.width);
    }

    // ===== PREFERRED SIZE INVARIANTS =====

    @Test
    public void rowPrefSizeInvariants() {
        LayoutSpec spec = new LayoutSpec().row().gap(3);
        MockNode a = node("a", 15, 10);
        MockNode b = node("b", 20, 15);
        MockNode c = node("c", 25, 20);
        a.set("prefW", 18f); c.set("prefH", 18f);
        layout(spec, 100, 50, a, b, c);
        assertInvariants(spec, list(a, b, c), 0, 0, 100, 50);
    }

    @Test
    public void columnPrefSizeInvariants() {
        LayoutSpec spec = new LayoutSpec().column().gap(4);
        MockNode a = node("a", 30, 12);
        MockNode b = node("b", 40, 18);
        MockNode c = node("c", 50, 24);
        a.set("prefH", 15f); b.set("prefW", 35f);
        layout(spec, 100, 100, a, b, c);
        assertInvariants(spec, list(a, b, c), 0, 0, 100, 100);
    }
}
