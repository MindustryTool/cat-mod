package org.mindustrytool.libs.ui.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutEnginePaddingConstraintTest extends LayoutTestBase {

    // ===== CONTAINER PADDING (ROW) =====

    @Test
    public void rowContainerPadding() {
        LayoutSpec spec = new LayoutSpec().row().padding(10).gap(0);
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 25);
        layout(spec, 100, 60, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(60f, a.height),
            () -> assertEquals(20f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(60f, b.height)
        );
    }

    @Test
    public void columnContainerPadding() {
        LayoutSpec spec = new LayoutSpec().column().padding(15).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(80f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(50f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    // ===== ASYMMETRIC PADDING =====

    @Test
    public void rowAsymmetricPadding() {
        LayoutSpec spec = new LayoutSpec().row().padding(5, 10, 15, 20).gap(0);
        MockNode a = node("a", 20, 15);
        layout(spec, 100, 60, a);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(60f, a.height)
        );
    }

    @Test
    public void columnAsymmetricPadding() {
        LayoutSpec spec = new LayoutSpec().column().padding(3, 8, 12, 6).gap(0);
        MockNode a = node("a", 30, 20);
        layout(spec, 100, 80, a);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(60f, a.yPosition), () -> assertEquals(20f, a.height)
        );
    }

    // ===== ZERO PADDING =====

    @Test
    public void zeroPaddingBehavesSame() {
        LayoutSpec spec1 = new LayoutSpec().row().padding(0).gap(0);
        LayoutSpec spec2 = new LayoutSpec().row().gap(0);
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 25);
        layout(spec1, 100, 50, a, b);
        float x1 = a.xPosition, w1 = a.width, y1 = a.yPosition, h1 = a.height;
        float x2 = b.xPosition, w2 = b.width, y2 = b.yPosition, h2 = b.height;
        layout(spec2, 100, 50, a.copy(), b.copy());
        assertEquals(x1, a.xPosition);
        assertEquals(y1, a.yPosition);
        assertEquals(x2, b.xPosition);
        assertEquals(y2, b.yPosition);
    }

    // ===== CHILD PADDING =====

    @Test
    public void childPaddingRow() {
        LayoutSpec spec = new LayoutSpec().row().gap(0);
        MockNode a = node("a", 20, 15);
        a.getSizingObj().padding(6);
        MockNode b = node("b", 30, 25);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(20f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void childPaddingColumn() {
        LayoutSpec spec = new LayoutSpec().column().gap(0);
        MockNode a = node("a", 30, 20);
        a.getSizingObj().padding(5, 10, 5, 10);
        MockNode b = node("b", 40, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(80f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(50f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    // ===== MINIMUM WIDTH CONSTRAINT =====

    @Test
    public void minimumWidthConstraint() {
        MockNode a = node("a", 10, 15);
        a.getSizingObj().minimumWidth(30);
        layout(row().gap(0), 100, 50, a);
        assertEquals(0f, a.xPosition);
        assertEquals(30f, a.width);
        assertEquals(0f, a.yPosition);
        assertEquals(15f, a.height);
    }

    @Test
    public void minimumWidthDoesNotExpandIfAlreadyLarger() {
        MockNode a = node("a", 40, 15);
        a.getSizingObj().minimumWidth(30);
        layout(row().gap(0), 100, 50, a);
        assertEquals(0f, a.xPosition);
        assertEquals(40f, a.width);
    }

    @Test
    public void minimumWidthWithGrow() {
        MockNode a = nodeWithSizing("a", sizing(NodeSpec.SizeMode.GROW, 0, 20).growWeightHorizontal(1));
        a.set("minW", 80f);
        MockNode b = node("b", 10, 20);
        layout(row().gap(0), 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(90f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(90f, b.xPosition), () -> assertEquals(10f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(20f, b.height)
        );
    }

    // ===== MAXIMUM WIDTH CONSTRAINT =====

    @Test
    public void maximumWidthConstraint() {
        MockNode a = node("a", 50, 15);
        a.getSizingObj().maximumWidth(30);
        layout(row().gap(0), 100, 50, a);
        assertEquals(0f, a.xPosition);
        assertEquals(30f, a.width);
    }

    @Test
    public void maximumWidthHasNoEffectIfSmaller() {
        MockNode a = node("a", 20, 15);
        a.getSizingObj().maximumWidth(40);
        layout(row().gap(0), 100, 50, a);
        assertEquals(0f, a.xPosition);
        assertEquals(20f, a.width);
    }

    @Test
    public void maximumWidthWithGrow() {
        MockNode a = nodeWithSizing("a", sizing(NodeSpec.SizeMode.GROW, 0, 20).growWeightHorizontal(1));
        a.set("maxW", 40f);
        MockNode b = nodeWithSizing("b", sizing(NodeSpec.SizeMode.GROW, 0, 20).growWeightHorizontal(1));
        layout(row().gap(0), 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(50f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(50f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(20f, b.height)
        );
    }

    // ===== MINIMUM HEIGHT CONSTRAINT (COLUMN) =====

    @Test
    public void minimumHeightConstraint() {
        MockNode a = node("a", 30, 5);
        a.getSizingObj().minimumHeight(25);
        layout(column().gap(0), 100, 100, a);
        assertEquals(0f, a.xPosition);
        assertEquals(30f, a.width);
        assertEquals(75f, a.yPosition);
        assertEquals(25f, a.height);
    }

    // ===== MAXIMUM HEIGHT CONSTRAINT (COLUMN) =====

    @Test
    public void maximumHeightConstraint() {
        MockNode a = node("a", 30, 40);
        a.getSizingObj().maximumHeight(20);
        layout(column().gap(0), 100, 100, a);
        assertEquals(0f, a.xPosition);
        assertEquals(30f, a.width);
        assertEquals(80f, a.yPosition);
        assertEquals(20f, a.height);
    }

    // ===== CONSTRAINTS WITH GAP =====

    @Test
    public void minWidthWithGap() {
        MockNode a = node("a", 10, 15);
        a.getSizingObj().minimumWidth(30);
        MockNode b = node("b", 10, 15);
        layout(row().gap(10), 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(15f, a.height),
            () -> assertEquals(40f, b.xPosition), () -> assertEquals(10f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(15f, b.height)
        );
    }

    // ===== CONSTRAINTS WITH FIXED SIZING =====

    @Test
    public void minMaxExactSame() {
        MockNode a = node("a", 20, 15);
        a.getSizingObj().minimumWidth(20).maximumWidth(20);
        layout(row().gap(0), 100, 50, a);
        assertEquals(20f, a.width);
    }

    @Test
    public void minLargerThanMaxTreatedAsMin() {
        MockNode a = node("a", 20, 15);
        a.getSizingObj().minimumWidth(50).maximumWidth(30);
        layout(row().gap(0), 100, 50, a);
        assertEquals(30f, a.width);
    }

    // ===== PADDING WITH PREFERRED SIZE COMPUTATION =====

    @Test
    public void paddingIncreasesPreferredSize() {
        LayoutSpec spec = new LayoutSpec().row().padding(10).gap(0);
        spec.padding(10);
        MockNode a = node("a", 20, 15);
        layout(spec, 100, 50, a);
        assertEquals(0f, a.xPosition);
        assertEquals(20f, a.width);
        assertEquals(0f, a.yPosition);
        assertEquals(50f, a.height);
    }

    // ===== PARAMETERIZED COMBOS =====

    static Stream<Arguments> paddingAndMinWidthParams() {
        return Stream.of(
            Arguments.of(0, 0, 20f, 0f),
            Arguments.of(5, 0, 20f, 0f),
            Arguments.of(0, 10, 20f, 0f),
            Arguments.of(5, 10, 20f, 0f),
            Arguments.of(10, 30, 30f, 0f)
        );
    }

    @ParameterizedTest
    @MethodSource("paddingAndMinWidthParams")
    public void rowPaddingAndMinWidth(int pad, int minW, float expectedW, float expectedX) {
        LayoutSpec spec = new LayoutSpec().row().padding(pad).gap(0);
        MockNode a = node("a", 20, 15);
        a.getSizingObj().minimumWidth(minW);
        layout(spec, 100, 50, a);
        assertEquals(expectedX, a.xPosition);
        assertEquals(Math.max(expectedW, minW), a.width);
    }

    // ===== PADDING + CONSTRAINTS INVARIANTS =====

    @Test
    public void rowPaddingInvariants() {
        LayoutSpec spec = new LayoutSpec().row().padding(8).gap(3);
        MockNode a = node("a", 20, 10);
        MockNode b = node("b", 25, 15);
        MockNode c = node("c", 30, 20);
        layout(spec, 120, 60, a, b, c);
        assertInvariants(spec, list(a, b, c), 0, 0, 120, 60);
    }

    @Test
    public void columnPaddingInvariants() {
        LayoutSpec spec = new LayoutSpec().column().padding(6).gap(4);
        MockNode a = node("a", 30, 15);
        MockNode b = node("b", 40, 20);
        MockNode c = node("c", 50, 25);
        layout(spec, 100, 120, a, b, c);
        assertInvariants(spec, list(a, b, c), 0, 0, 100, 120);
    }

    @Test
    public void rowConstraintsInvariants() {
        LayoutSpec spec = new LayoutSpec().row().gap(2);
        MockNode a = node("a", 15, 10);
        a.getSizingObj().minimumWidth(20);
        MockNode b = node("b", 10, 15);
        b.getSizingObj().maximumWidth(8);
        MockNode c = node("c", 25, 20);
        layout(spec, 100, 50, a, b, c);
        assertInvariants(spec, list(a, b, c), 0, 0, 100, 50);
    }

    @Test
    public void columnConstraintsInvariants() {
        LayoutSpec spec = new LayoutSpec().column().gap(2);
        MockNode a = node("a", 30, 10);
        a.getSizingObj().minimumHeight(20);
        MockNode b = node("b", 40, 20);
        b.getSizingObj().maximumHeight(10);
        MockNode c = node("c", 50, 15);
        layout(spec, 100, 100, a, b, c);
        assertInvariants(spec, list(a, b, c), 0, 0, 100, 100);
    }
}
