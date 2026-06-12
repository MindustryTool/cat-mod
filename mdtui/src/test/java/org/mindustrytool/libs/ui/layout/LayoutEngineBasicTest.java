package org.mindustrytool.libs.ui.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutEngineBasicTest extends LayoutTestBase {

    // ===== ROW LAYOUT: SINGLE CHILD =====

    @Test
    public void rowSingleChildStretch() {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.STRETCH);
        MockNode a = node("a", 50, 30);
        layout(spec, 0, 0, 200, 100, a);
        assertAll(
            () -> assertEquals(0f, a.xPosition),
            () -> assertEquals(0f, a.yPosition),
            () -> assertEquals(50f, a.width),
            () -> assertEquals(100f, a.height)
        );
    }

    @Test
    public void rowSingleChildStart() {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.START);
        MockNode a = node("a", 50, 30);
        layout(spec, 0, 0, 200, 100, a);
        assertAll(
            () -> assertEquals(0f, a.xPosition),
            () -> assertEquals(0f, a.yPosition),
            () -> assertEquals(50f, a.width),
            () -> assertEquals(30f, a.height)
        );
    }

    @Test
    public void rowSingleChildCenter() {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.CENTER);
        MockNode a = node("a", 50, 30);
        layout(spec, 0, 0, 200, 100, a);
        assertEquals(35f, a.yPosition, EPS);
    }

    @Test
    public void rowSingleChildEnd() {
        LayoutSpec spec = new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.END);
        MockNode a = node("a", 50, 30);
        layout(spec, 0, 0, 200, 100, a);
        assertEquals(70f, a.yPosition);
    }

    // ===== ROW LAYOUT: TWO CHILDREN =====

    @Test
    public void rowTwoChildrenNoGap() {
        LayoutSpec spec = new LayoutSpec().row().gap(0).alignItems(LayoutSpec.AlignItems.STRETCH);
        MockNode a = node("a", 40, 20);
        MockNode b = node("b", 60, 30);
        layout(spec, 200, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(40f, a.width),
            () -> assertEquals(40f, b.xPosition), () -> assertEquals(60f, b.width),
            () -> assertEquals(100f, a.height), () -> assertEquals(100f, b.height)
        );
    }

    @Test
    public void rowTwoChildrenWithGap() {
        LayoutSpec spec = new LayoutSpec().row().gap(10).alignItems(LayoutSpec.AlignItems.STRETCH);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 25);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(40f, b.xPosition), () -> assertEquals(40f, b.width),
            () -> assertEquals(50f, a.height), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void rowTwoChildrenDifferentHeights() {
        LayoutSpec spec = new LayoutSpec().row().gap(5).alignItems(LayoutSpec.AlignItems.STRETCH);
        MockNode a = node("a", 30, 15);
        MockNode b = node("b", 40, 30);
        layout(spec, 200, 60, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition),
            () -> assertEquals(35f, b.xPosition),
            () -> assertEquals(60f, a.height),
            () -> assertEquals(60f, b.height)
        );
    }

    // ===== ROW LAYOUT: THREE CHILDREN =====

    @Test
    public void rowThreeChildrenWithGap() {
        LayoutSpec spec = new LayoutSpec().row().gap(5);
        MockNode a = node("a", 20, 10);
        MockNode b = node("b", 30, 10);
        MockNode c = node("c", 25, 10);
        layout(spec, 200, 50, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(25f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(60f, c.xPosition), () -> assertEquals(25f, c.width)
        );
    }

    @Test
    public void rowThreeChildrenZeroGap() {
        LayoutSpec spec = new LayoutSpec().row().gap(0);
        MockNode a = node("a", 15, 10);
        MockNode b = node("b", 20, 10);
        MockNode c = node("c", 25, 10);
        layout(spec, 200, 50, a, b, c);
        assertEquals(15f, b.xPosition);
        assertEquals(35f, c.xPosition);
    }

    @Test
    public void rowThreeChildrenLargeGap() {
        LayoutSpec spec = new LayoutSpec().row().gap(20);
        MockNode a = node("a", 10, 10);
        MockNode b = node("b", 10, 10);
        MockNode c = node("c", 10, 10);
        layout(spec, 200, 50, a, b, c);
        assertEquals(30f, b.xPosition);
        assertEquals(60f, c.xPosition);
    }

    // ===== ROW LAYOUT: FIVE CHILDREN =====

    @Test
    public void rowFiveChildren() {
        LayoutSpec spec = new LayoutSpec().row().gap(4).alignItems(LayoutSpec.AlignItems.STRETCH);
        MockNode[] ns = new MockNode[5];
        for (int i = 0; i < 5; i++) ns[i] = node("n" + i, 20, 15);
        layout(spec, 200, 60, ns);
        for (int i = 0; i < 5; i++) {
            assertEquals(60f, ns[i].height);
            assertEquals(i * 24f, ns[i].xPosition, EPS);
        }
    }

    @Test
    public void rowFiveChildrenVaryingSizes() {
        LayoutSpec spec = new LayoutSpec().row().gap(3);
        MockNode a = node("a", 10, 10);
        MockNode b = node("b", 20, 10);
        MockNode c = node("c", 30, 10);
        MockNode d = node("d", 40, 10);
        MockNode e = node("e", 50, 10);
        layout(spec, 500, 60, a, b, c, d, e);
        assertEquals(0f, a.xPosition);
        assertEquals(13f, b.xPosition);
        assertEquals(36f, c.xPosition);
        assertEquals(69f, d.xPosition);
        assertEquals(112f, e.xPosition);
    }

    // ===== COLUMN LAYOUT: SINGLE CHILD =====

    @Test
    public void columnSingleChildStretch() {
        LayoutSpec spec = new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.STRETCH);
        MockNode a = node("a", 50, 30);
        layout(spec, 200, 100, a);
        assertAll(
            () -> assertEquals(0f, a.xPosition),
            () -> assertEquals(200f, a.width),
            () -> assertEquals(70f, a.yPosition),
            () -> assertEquals(30f, a.height)
        );
    }

    @Test
    public void columnSingleChildStart() {
        LayoutSpec spec = new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.START);
        MockNode a = node("a", 50, 30);
        layout(spec, 200, 100, a);
        assertEquals(0f, a.xPosition);
        assertEquals(50f, a.width);
    }

    @Test
    public void columnSingleChildCenter() {
        LayoutSpec spec = new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.CENTER);
        MockNode a = node("a", 50, 30);
        layout(spec, 200, 100, a);
        assertEquals(75f, a.xPosition);
    }

    @Test
    public void columnSingleChildEnd() {
        LayoutSpec spec = new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.END);
        MockNode a = node("a", 50, 30);
        layout(spec, 200, 100, a);
        assertEquals(150f, a.xPosition);
    }

    // ===== COLUMN LAYOUT: TWO CHILDREN =====

    @Test
    public void columnTwoChildrenNoGap() {
        LayoutSpec spec = new LayoutSpec().column().gap(0).alignItems(LayoutSpec.AlignItems.STRETCH);
        MockNode a = node("a", 40, 20);
        MockNode b = node("b", 60, 30);
        layout(spec, 200, 100, a, b);
        assertAll(
            () -> assertEquals(80f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(50f, b.yPosition), () -> assertEquals(30f, b.height),
            () -> assertEquals(200f, a.width), () -> assertEquals(200f, b.width)
        );
    }

    @Test
    public void columnTwoChildrenWithGap() {
        LayoutSpec spec = new LayoutSpec().column().gap(10).alignItems(LayoutSpec.AlignItems.STRETCH);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 25);
        layout(spec, 100, 80, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(60f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(25f, b.yPosition), () -> assertEquals(25f, b.height)
        );
    }

    @Test
    public void columnTwoChildrenDifferentWidths() {
        LayoutSpec spec = new LayoutSpec().column().gap(5).alignItems(LayoutSpec.AlignItems.START);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 50, 25);
        layout(spec, 100, 80, a, b);
        assertAll(
            () -> assertEquals(30f, a.width),
            () -> assertEquals(50f, b.width),
            () -> assertEquals(60f, a.yPosition),
            () -> assertEquals(30f, b.yPosition)
        );
    }

    // ===== COLUMN LAYOUT: THREE CHILDREN =====

    @Test
    public void columnThreeChildrenWithGap() {
        LayoutSpec spec = new LayoutSpec().column().gap(5);
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 20);
        MockNode c = node("c", 25, 10);
        layout(spec, 100, 80, a, b, c);
        assertAll(
            () -> assertEquals(65f, a.yPosition), () -> assertEquals(15f, a.height),
            () -> assertEquals(40f, b.yPosition), () -> assertEquals(20f, b.height),
            () -> assertEquals(25f, c.yPosition), () -> assertEquals(10f, c.height)
        );
    }

    // ===== CONTAINER AT NON-ZERO OFFSET =====

    @Test
    public void rowWithOffset() {
        LayoutSpec spec = new LayoutSpec().row().gap(10);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 20);
        layout(spec, list(a, b), 50, 30, 200, 100);
        assertAll(
            () -> assertEquals(50f, a.xPosition),
            () -> assertEquals(30f, a.yPosition),
            () -> assertEquals(90f, b.xPosition),
            () -> assertEquals(30f, b.yPosition)
        );
    }

    @Test
    public void columnWithOffset() {
        LayoutSpec spec = new LayoutSpec().column().gap(10);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 25);
        layout(spec, list(a, b), 50, 30, 200, 100);
        assertAll(
            () -> assertEquals(50f, a.xPosition),
            () -> assertEquals(110f, a.yPosition),
            () -> assertEquals(50f, b.xPosition),
            () -> assertEquals(75f, b.yPosition)
        );
    }

    // ===== INVISIBLE CHILDREN =====

    @Test
    public void invisibleChildSkipped() {
        LayoutSpec spec = new LayoutSpec().row().gap(10);
        MockNode a = node("a", 30, 20);
        MockNode b = invisible("b", 40, 20);
        MockNode c = node("c", 50, 20);
        layout(spec, 200, 50, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition),
            () -> assertEquals(40f, c.xPosition),
            () -> assertEquals(-9999f, b.xPosition)
        );
    }

    @Test
    public void allInvisibleNoCrash() {
        LayoutSpec spec = new LayoutSpec().row().gap(10);
        MockNode a = invisible("a", 30, 20);
        MockNode b = invisible("b", 40, 20);
        assertDoesNotThrow(() -> layout(spec, 100, 50, a, b));
        assertFalse(a.boundsSet);
        assertFalse(b.boundsSet);
    }

    @Test
    public void invisibleInMiddleOfRow() {
        LayoutSpec spec = new LayoutSpec().row().gap(5);
        MockNode a = node("a", 20, 10);
        MockNode b = invisible("b", 30, 10);
        MockNode c = node("c", 40, 10);
        layout(spec, 200, 50, a, b, c);
        assertEquals(0f, a.xPosition);
        assertEquals(25f, c.xPosition);
    }

    // ===== EXACT FIT / OVERFLOW =====

    @Test
    public void rowExactFit() {
        LayoutSpec spec = new LayoutSpec().row().gap(10);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 60, 20);
        layout(spec, 100, 50, a, b);
        assertEquals(0f, a.xPosition);
        assertEquals(40f, b.xPosition);
    }

    @Test
    public void rowContentLargerThanContainer() {
        LayoutSpec spec = new LayoutSpec().row().gap(5);
        MockNode a = node("a", 80, 20);
        MockNode b = node("b", 80, 20);
        layout(spec, 100, 50, a, b);
        assertTrue(a.boundsSet);
        assertTrue(b.boundsSet);
    }

    @Test
    public void columnContentLargerThanContainer() {
        LayoutSpec spec = new LayoutSpec().column().gap(5);
        MockNode a = node("a", 30, 60);
        MockNode b = node("b", 30, 60);
        layout(spec, 100, 80, a, b);
        assertTrue(a.boundsSet);
        assertTrue(b.boundsSet);
    }

    // ===== ZERO PREFERRED SIZE =====

    @Test
    public void rowChildWithZeroWidth() {
        LayoutSpec spec = new LayoutSpec().row().gap(10);
        MockNode a = node("a", 0, 20);
        MockNode b = node("b", 30, 20);
        layout(spec, 100, 50, a, b);
        assertEquals(0f, a.xPosition);
        assertEquals(0f, a.width);
        assertEquals(10f, b.xPosition);
    }

    @Test
    public void columnChildWithZeroHeight() {
        LayoutSpec spec = new LayoutSpec().column().gap(10);
        MockNode a = node("a", 30, 0);
        MockNode b = node("b", 30, 20);
        layout(spec, 100, 50, a, b);
        assertEquals(50f, a.yPosition);
        assertEquals(0f, a.height);
        assertEquals(20f, b.yPosition);
    }

    @Test
    public void allChildrenZeroSize() {
        LayoutSpec spec = new LayoutSpec().row().gap(5);
        MockNode a = node("a", 0, 0);
        MockNode b = node("b", 0, 0);
        MockNode c = node("c", 0, 0);
        layout(spec, 100, 50, a, b, c);
        assertEquals(0f, a.xPosition);
        assertEquals(5f, b.xPosition);
        assertEquals(10f, c.xPosition);
    }

    // ===== DIFFERENT GAP VALUES =====

    @Test
    public void rowGapZero() {
        LayoutSpec spec = new LayoutSpec().row().gap(0);
        MockNode a = node("a", 20, 10);
        MockNode b = node("b", 30, 10);
        layout(spec, 100, 50, a, b);
        assertEquals(20f, b.xPosition);
    }

    @Test
    public void rowGapNegative() {
        LayoutSpec spec = new LayoutSpec().row().gap(-5);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 20);
        layout(spec, 100, 50, a, b);
        assertEquals(25f, b.xPosition);
    }

    @Test
    public void columnGapNegative() {
        LayoutSpec spec = new LayoutSpec().column().gap(-5);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 25);
        layout(spec, 100, 80, a, b);
        assertEquals(40f, b.yPosition);
    }

    @Test
    public void rowGapVeryLarge() {
        LayoutSpec spec = new LayoutSpec().row().gap(200);
        MockNode a = node("a", 10, 10);
        MockNode b = node("b", 10, 10);
        layout(spec, 500, 50, a, b);
        assertEquals(210f, b.xPosition);
    }

    // ===== TEN CHILDREN STRESS =====

    @Test
    public void rowTenChildren() {
        LayoutSpec spec = new LayoutSpec().row().gap(2).alignItems(LayoutSpec.AlignItems.STRETCH);
        MockNode[] ns = new MockNode[10];
        for (int i = 0; i < 10; i++) ns[i] = node("n" + i, 10, 10);
        layout(spec, 500, 30, ns);
        for (int i = 0; i < 10; i++) {
            assertEquals(i * 12f, ns[i].xPosition, EPS);
            assertEquals(30f, ns[i].height);
        }
    }

    @Test
    public void columnTenChildren() {
        LayoutSpec spec = new LayoutSpec().column().gap(3);
        MockNode[] ns = new MockNode[10];
        for (int i = 0; i < 10; i++) ns[i] = node("n" + i, 20, 8);
        layout(spec, 100, 500, ns);
        for (int i = 0; i < 10; i++) {
            assertEquals(492f - i * 11f, ns[i].yPosition, EPS);
        }
    }

    // ===== PARAMETERIZED: ROW GAP COMBOS =====

    static Stream<Arguments> rowGapCombos() {
        return Stream.of(
            Arguments.of(0f, 100f, new float[]{20, 30}, new float[]{0f, 20f}),
            Arguments.of(5f, 100f, new float[]{20, 30}, new float[]{0f, 25f}),
            Arguments.of(10f, 100f, new float[]{20, 30}, new float[]{0f, 30f}),
            Arguments.of(0f, 50f, new float[]{10, 15, 20}, new float[]{0f, 10f, 25f}),
            Arguments.of(3f, 80f, new float[]{10, 15, 20}, new float[]{0f, 13f, 31f}),
            Arguments.of(7f, 200f, new float[]{25, 35, 45, 55}, new float[]{0f, 32f, 74f, 126f})
        );
    }

    @ParameterizedTest
    @MethodSource("rowGapCombos")
    public void rowParameterizedGap(float gap, float cw, float[] sizes, float[] expectedX) {
        LayoutSpec spec = new LayoutSpec().row().gap(gap);
        MockNode[] ns = new MockNode[sizes.length];
        for (int i = 0; i < sizes.length; i++) ns[i] = node("n" + i, sizes[i], 20);
        layout(spec, 0, 0, cw, 50, ns);
        for (int i = 0; i < sizes.length; i++) {
            final int idx = i;
            assertEquals(expectedX[idx], ns[idx].xPosition, EPS, () -> "x mismatch child " + idx);
        }
    }

    // ===== PARAMETERIZED: COLUMN GAP COMBOS =====

    static Stream<Arguments> columnGapCombos() {
        return Stream.of(
            Arguments.of(0f, 100f, new float[]{20, 30}, new float[]{80f, 50f}),
            Arguments.of(5f, 100f, new float[]{20, 30}, new float[]{80f, 45f}),
            Arguments.of(10f, 100f, new float[]{20, 30}, new float[]{80f, 40f}),
            Arguments.of(0f, 80f, new float[]{10, 15, 20}, new float[]{70f, 55f, 35f}),
            Arguments.of(3f, 80f, new float[]{10, 15, 20}, new float[]{70f, 52f, 29f})
        );
    }

    @ParameterizedTest
    @MethodSource("columnGapCombos")
    public void columnParameterizedGap(float gap, float ch, float[] sizes, float[] expectedY) {
        LayoutSpec spec = new LayoutSpec().column().gap(gap);
        MockNode[] ns = new MockNode[sizes.length];
        for (int i = 0; i < sizes.length; i++) ns[i] = node("n" + i, 30, sizes[i]);
        layout(spec, 0, 0, 100, ch, ns);
        for (int i = 0; i < sizes.length; i++) {
            final int idx = i;
            assertEquals(expectedY[idx], ns[idx].yPosition, EPS, () -> "y mismatch child " + idx);
        }
    }

    // ===== INVARIANT-BASED COMBINATORIAL TESTS =====

    static Stream<Arguments> rowDirectionBasic() {
        return Stream.of(
            Arguments.of(new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.STRETCH), 0f),
            Arguments.of(new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.STRETCH), 5f),
            Arguments.of(new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.STRETCH), 10f),
            Arguments.of(new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.START), 0f),
            Arguments.of(new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.CENTER), 0f),
            Arguments.of(new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.END), 0f)
        );
    }

    @ParameterizedTest
    @MethodSource("rowDirectionBasic")
    public void rowInvariants(LayoutSpec spec, float gap) {
        spec.gap(gap);
        MockNode[] ns = new MockNode[]{node("a", 20, 15), node("b", 30, 25), node("c", 25, 20)};
        layout(spec, 0, 0, 200, 80, ns);
        assertInvariants(spec, list(ns), 0, 0, 200, 80);
    }

    static Stream<Arguments> columnDirectionBasic() {
        return Stream.of(
            Arguments.of(new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.STRETCH), 0f),
            Arguments.of(new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.STRETCH), 5f),
            Arguments.of(new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.START), 0f),
            Arguments.of(new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.CENTER), 0f),
            Arguments.of(new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.END), 0f)
        );
    }

    @ParameterizedTest
    @MethodSource("columnDirectionBasic")
    public void columnInvariants(LayoutSpec spec, float gap) {
        spec.gap(gap);
        MockNode[] ns = new MockNode[]{node("a", 20, 15), node("b", 30, 25), node("c", 25, 20)};
        layout(spec, 0, 0, 100, 100, ns);
        assertInvariants(spec, list(ns), 0, 0, 100, 100);
    }
}
