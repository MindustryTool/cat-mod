package org.mindustrytool.libs.ui.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import org.mindustrytool.libs.ui.layout.LayoutSpec.AlignItems;
import org.mindustrytool.libs.ui.layout.LayoutSpec.JustifyContent;

public class LayoutEngineEdgeTest extends LayoutTestBase {

    // ===== EMPTY / NULL CHILDREN =====

    @Test
    public void noChildren() {
        LayoutSpec spec = new LayoutSpec().row().gap(0);
        layout(spec, 100, 50);
    }

    @Test
    public void allChildrenInvisible() {
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 25);
        a.set("visible", false);
        b.set("visible", false);
        layout(row().gap(0), 100, 50, a, b);
        assertEquals(-1f, a.width);
        assertEquals(-1f, b.width);
    }

    @Test
    public void singleInvisibleChild() {
        MockNode a = node("a", 20, 15);
        a.set("visible", false);
        layout(row().gap(0), 100, 50, a);
        assertEquals(-1f, a.width);
        assertEquals(-9999f, a.xPosition);
    }

    // ===== ZERO CONTAINER SIZE =====

    @Test
    public void zeroContainerSizeRow() {
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 25);
        layout(row().gap(0), 0, 0, a, b);
        assertEquals(0f, a.xPosition);
        assertEquals(20f, a.width);
        assertEquals(20f, b.xPosition);
        assertEquals(30f, b.width);
    }

    @Test
    public void zeroContainerSizeColumn() {
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(column().gap(0), 0, 0, a, b);
        assertEquals(0f, a.xPosition);
        assertEquals(30f, a.width);
        assertEquals(-20f, a.yPosition);
        assertEquals(20f, a.height);
        assertEquals(0f, b.xPosition);
        assertEquals(40f, b.width);
        assertEquals(-50f, b.yPosition);
        assertEquals(30f, b.height);
    }

    // ===== VERY LARGE VALUES =====

    @Test
    public void largeContainerNoOverflow() {
        MockNode a = node("a", 100, 50);
        MockNode b = node("b", 150, 75);
        layout(row(AlignItems.STRETCH).gap(0), 10000, 5000, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(5000f, a.height),
            () -> assertEquals(100f, b.xPosition), () -> assertEquals(150f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(5000f, b.height)
        );
    }

    @Test
    public void largeNodeSizes() {
        MockNode a = node("a", 5000, 3000);
        MockNode b = node("b", 4000, 2000);
        layout(row(AlignItems.STRETCH).gap(0), 10000, 5000, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(5000f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(5000f, a.height),
            () -> assertEquals(5000f, b.xPosition), () -> assertEquals(4000f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(5000f, b.height)
        );
    }

    // ===== SUB-PIXEL VALUES =====

    @Test
    public void subPixelValuesRow() {
        MockNode a = node("a", 10.5f, 5.25f);
        MockNode b = node("b", 20.75f, 8.33f);
        layout(row().gap(0), 100, 50, a, b);
        assertEquals(0f, a.xPosition);
        assertEquals(10.5f, a.width, EPS);
        assertEquals(0f, a.yPosition);
        assertEquals(5.25f, a.height, EPS);
        assertEquals(10.5f, b.xPosition, EPS);
        assertEquals(20.75f, b.width, EPS);
        assertEquals(0f, b.yPosition);
        assertEquals(8.33f, b.height, EPS);
    }

    @Test
    public void subPixelValuesColumn() {
        MockNode a = node("a", 15.33f, 10.67f);
        MockNode b = node("b", 22.22f, 14.44f);
        layout(column().gap(0), 100, 100, a, b);
        assertEquals(0f, a.xPosition);
        assertEquals(15.33f, a.width, EPS);
        assertEquals(89.33f, a.yPosition, EPS);
        assertEquals(10.67f, a.height, EPS);
        assertEquals(0f, b.xPosition);
        assertEquals(22.22f, b.width, EPS);
        assertEquals(74.89f, b.yPosition, EPS);
        assertEquals(14.44f, b.height, EPS);
    }

    @Test
    public void subPixelGap() {
        MockNode a = node("a", 10, 10);
        MockNode b = node("b", 15, 15);
        MockNode c = node("c", 12, 12);
        layout(row().gap(1.5f), 50, 30, a, b, c);
        assertInvariants(row().gap(1.5f), list(a, b, c), 0, 0, 50, 30);
    }

    // ===== NEGATIVE GAP / PADDING =====

    @Test
    public void negativeGapRow() {
        MockNode a = node("a", 30, 15);
        MockNode b = node("b", 30, 20);
        layout(row(AlignItems.STRETCH).gap(-10), 60, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(20f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void negativeGapColumn() {
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(column().gap(-5), 100, 60, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(40f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(40f, b.width),
            () -> assertEquals(15f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void negativePaddingRow() {
        LayoutSpec spec = new LayoutSpec().row().padding(-5).gap(0);
        MockNode a = node("a", 20, 15);
        layout(spec, 100, 50, a);
        assertEquals(0f, a.xPosition);
        assertEquals(20f, a.width);
        assertEquals(0f, a.yPosition);
        assertEquals(50f, a.height);
    }

    @Test
    public void negativePaddingColumn() {
        LayoutSpec spec = new LayoutSpec().column().padding(-3).gap(0);
        MockNode a = node("a", 30, 20);
        layout(spec, 100, 80, a);
        assertEquals(0f, a.xPosition);
        assertEquals(100f, a.width);
        assertEquals(60f, a.yPosition);
        assertEquals(20f, a.height);
    }

    // ===== MANY CHILDREN STRESS =====

    @Test
    public void oneHundredChildrenRow() {
        MockNode[] ns = new MockNode[100];
        for (int i = 0; i < 100; i++) ns[i] = node("n" + i, 5, 3);
        layout(row(AlignItems.STRETCH).gap(1), 1000, 50, ns);
        for (int i = 0; i < 100; i++) {
            assertEquals(i * 6f, ns[i].xPosition, EPS);
            assertEquals(5f, ns[i].width);
            assertEquals(0f, ns[i].yPosition);
            assertEquals(50f, ns[i].height);
        }
    }

    @Test
    public void oneHundredChildrenColumn() {
        MockNode[] ns = new MockNode[100];
        for (int i = 0; i < 100; i++) ns[i] = node("n" + i, 3, 5);
        layout(column(AlignItems.STRETCH).gap(1), 100, 1000, ns);
        for (int i = 0; i < 100; i++) {
            assertEquals(995f - i * 6f, ns[i].yPosition, EPS);
            assertEquals(5f, ns[i].height);
            assertEquals(0f, ns[i].xPosition);
            assertEquals(100f, ns[i].width);
        }
    }

    @Test
    public void fiftyChildrenWrap() {
        MockNode[] ns = new MockNode[50];
        for (int i = 0; i < 50; i++) ns[i] = node("n" + i, 40, 10);
        layout(row().wrap().gap(2), 100, 500, ns);
        assertInvariants(row().wrap().gap(2), list(ns), 0, 0, 100, 500);
    }

    @Test
    public void fiftyGrowChildren() {
        MockNode[] ns = new MockNode[50];
        for (int i = 0; i < 50; i++)
            ns[i] = nodeWithSizing("n" + i, sizing(LayoutSpec.SizeMode.GROW, 0, 5).growWeightHorizontal(1));
        layout(row().gap(0), 500, 50, ns);
        for (int i = 0; i < 50; i++) {
            assertEquals(10f, ns[i].width, EPS);
            assertEquals(i * 10f, ns[i].xPosition, EPS);
        }
    }

    // ===== MIXED VISIBILITY WITH COMPLEX =====

    @Test
    public void mixedVisibilityComplex() {
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 20);
        MockNode c = node("c", 10, 10);
        MockNode d = node("d", 25, 18);
        MockNode e = node("e", 15, 12);
        a.set("visible", false);
        c.set("visible", false);
        layout(row(AlignItems.CENTER).gap(5), 120, 50, a, b, c, d, e);
        assertAll(
            () -> assertEquals(-1f, a.width),
            () -> assertEquals(-1f, c.width),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(35f, d.xPosition), () -> assertEquals(25f, d.width),
            () -> assertEquals(65f, e.xPosition), () -> assertEquals(15f, e.width)
        );
        assertInvariants(row(AlignItems.CENTER).gap(5), list(a, b, c, d, e), 0, 0, 120, 50);
    }

    // ===== NESTED-LIKE COMPLEX COMBOS =====

    @Test
    public void allFeaturesCombinedRow() {
        LayoutSpec spec = new LayoutSpec().row()
            .justifyContent(LayoutSpec.JustifyContent.SPACE_AROUND)
            .alignItems(LayoutSpec.AlignItems.CENTER)
            .padding(5)
            .gap(3)
            .noWrap();
        MockNode a = node("a", 15, 10);
        MockNode b = nodeWithSizing("b", sizing(LayoutSpec.SizeMode.GROW, 0, 20).growWeightHorizontal(2));
        MockNode c = nodeWithSizing("c", sizing(LayoutSpec.SizeMode.WRAP, 0, 0));
        MockNode d = node("d", 20, 15);
        c.set("prefW", 25f); c.set("prefH", 15f);
        MockNode e = nodeWithSizing("e", sizing(LayoutSpec.SizeMode.GROW, 0, 25).growWeightHorizontal(1));
        a.set("visible", false);
        layout(spec, 150, 60, a, b, c, d, e);
        assertInvariants(spec, list(a, b, c, d, e), 0, 0, 150, 60);
    }

    @Test
    public void allFeaturesCombinedColumn() {
        LayoutSpec spec = new LayoutSpec().column()
            .justifyContent(LayoutSpec.JustifyContent.SPACE_EVENLY)
            .alignItems(LayoutSpec.AlignItems.CENTER)
            .padding(3, 6, 3, 6)
            .gap(2)
            .noWrap();
        MockNode a = node("a", 20, 10);
        MockNode b = nodeWithSizing("b", sizing(LayoutSpec.SizeMode.GROW, 30, 0).growWeightVertical(3));
        MockNode c = nodeWithSizing("c", sizing(LayoutSpec.SizeMode.WRAP, 0, 0));
        c.set("prefW", 35f); c.set("prefH", 18f);
        MockNode d = node("d", 25, 15);
        a.set("visible", false);
        layout(spec, 100, 150, a, b, c, d);
        assertInvariants(spec, list(a, b, c, d), 0, 0, 100, 150);
    }

    // ===== EDGE: SINGLE CHILD VARIOUS SIZING =====

    @Test
    public void singleChildGrowWeighted() {
        MockNode a = nodeWithSizing("a", sizing(LayoutSpec.SizeMode.GROW, 30, 20).growWeightHorizontal(5));
        layout(row().gap(0), 100, 50, a);
        assertEquals(0f, a.xPosition);
        assertEquals(100f, a.width);
    }

    @Test
    public void singleChildFixedStretch() {
        MockNode a = node("a", 40, 30);
        layout(row(AlignItems.STRETCH).gap(0), 100, 60, a);
        assertEquals(0f, a.xPosition);
        assertEquals(40f, a.width);
        assertEquals(0f, a.yPosition);
        assertEquals(60f, a.height);
    }

    // ===== EDGE: OVERFLOW VARIANTS =====

    @Test
    public void childrenOverflowContainerPositive() {
        MockNode a = node("a", 60, 10);
        MockNode b = node("b", 60, 10);
        layout(row().gap(0), 100, 50, a, b);
        assertEquals(0f, a.xPosition);
        assertEquals(60f, b.xPosition);
    }

    @Test
    public void childrenHugelyOverflowContainer() {
        MockNode a = node("a", 300, 10);
        MockNode b = node("b", 300, 10);
        layout(row().gap(0), 50, 50, a, b);
        assertEquals(0f, a.xPosition);
        assertEquals(300f, b.xPosition);
    }

    @Test
    public void columnChildrenOverflow() {
        MockNode a = node("a", 30, 60);
        MockNode b = node("b", 40, 60);
        layout(column().gap(0), 100, 100, a, b);
        assertEquals(40f, a.yPosition);
        assertEquals(60f, a.height);
        assertEquals(-20f, b.yPosition);
        assertEquals(60f, b.height);
    }

    // ===== EDGE: JUST END WITH OVERFLOW =====

    @Test
    public void justifyEndOverflowLeftSide() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.END).gap(0);
        MockNode a = node("a", 70, 10);
        MockNode b = node("b", 70, 10);
        layout(spec, 100, 50, a, b);
        assertEquals(-40f, a.xPosition);
        assertEquals(70f, a.width);
        assertEquals(30f, b.xPosition);
        assertEquals(70f, b.width);
    }

    // ===== EDGE: MAX/MIN EQUAL =====

    @Test
    public void minEqualsMax() {
        MockNode a = node("a", 20, 15);
        a.minimumWidth(30).maximumWidth(30);
        layout(row().gap(0), 100, 50, a);
        assertEquals(30f, a.width);
    }

    @Test
    public void minEqualsMaxColumn() {
        MockNode a = node("a", 30, 20);
        a.minimumHeight(25).maximumHeight(25);
        layout(column().gap(0), 100, 100, a);
        assertEquals(25f, a.height);
    }

    // ===== EDGE: PADDING WRAP =====

    @Test
    public void paddingWithWrap() {
        LayoutSpec spec = new LayoutSpec().row().padding(10).wrap().gap(0);
        MockNode a = node("a", 60, 20);
        MockNode b = node("b", 60, 25);
        MockNode c = node("c", 60, 30);
        layout(spec, 100, 100, a, b, c);
    }

    @Test
    public void paddingWithWrapColumn() {
        LayoutSpec spec = new LayoutSpec().column().padding(10).wrap().gap(0);
        MockNode a = node("a", 40, 40);
        MockNode b = node("b", 50, 40);
        MockNode c = node("c", 60, 40);
        layout(spec, 150, 100, a, b, c);
        assertInvariants(spec, list(a, b, c), 0, 0, 150, 100);
    }

    // ===== 100 CHILDREN WITH MIXED GROW WEIGHTS =====

    @Test
    public void oneHundredGrowChildrenWeighted() {
        MockNode[] ns = new MockNode[100];
        for (int i = 0; i < 100; i++)
            ns[i] = nodeWithSizing("n" + i, sizing(LayoutSpec.SizeMode.GROW, 0, 3).growWeightHorizontal(i + 1));
        layout(row().gap(0), 5050, 50, ns);
        float totalW = 0;
        for (int i = 0; i < 100; i++) totalW += ns[i].width;
        assertEquals(5050f, totalW, 1f);
    }

    // ===== INVARIANT-ONLY MEGA TEST =====

    @Test
    public void rowComplexInvariantMega() {
        MockNode a = node("a", 18, 12);
        MockNode b = nodeWithSizing("b", sizing(LayoutSpec.SizeMode.GROW, 0, 22).growWeightHorizontal(3));
        MockNode c = nodeWithSizing("c", sizing(LayoutSpec.SizeMode.WRAP, 0, 0));
        c.set("prefW", 28f); c.set("prefH", 16f);
        c.padding(4);
        MockNode d = node("d", 14, 10);
        d.minimumWidth(20).minimumHeight(15);
        MockNode e = node("e", 10, 8);
        e.set("visible", false);
        layout(row(AlignItems.CENTER, JustifyContent.SPACE_AROUND).gap(2).padding(3), 200, 60, a, b, c, d, e);
        assertInvariants(row(AlignItems.CENTER, JustifyContent.SPACE_AROUND).gap(2).padding(3), list(a, b, c, d, e), 0, 0, 200, 60);
    }

    @Test
    public void columnComplexInvariantMega() {
        MockNode a = node("a", 25, 14);
        MockNode b = nodeWithSizing("b", sizing(LayoutSpec.SizeMode.GROW, 30, 0).growWeightVertical(2));
        MockNode c = nodeWithSizing("c", sizing(LayoutSpec.SizeMode.WRAP, 0, 0));
        c.set("prefW", 32f); c.set("prefH", 20f);
        c.padding(3, 6, 3, 6);
        MockNode d = node("d", 20, 12);
        d.minimumWidth(28).minimumHeight(16);
        MockNode e = node("e", 12, 9);
        e.set("visible", false);
        layout(column(AlignItems.END, JustifyContent.SPACE_EVENLY).gap(3).padding(4, 2, 4, 2), 100, 200, a, b, c, d, e);
        assertInvariants(column(AlignItems.END, JustifyContent.SPACE_EVENLY).gap(3).padding(4, 2, 4, 2), list(a, b, c, d, e), 0, 0, 100, 200);
    }

    // ===== PARAMETERIZED STRESS =====

    static Stream<Arguments> stressChildCounts() {
        return Stream.of(
            Arguments.of(1), Arguments.of(2), Arguments.of(5),
            Arguments.of(10), Arguments.of(20), Arguments.of(50)
        );
    }

    @ParameterizedTest
    @MethodSource("stressChildCounts")
    public void rowStressWithCount(int count) {
        MockNode[] ns = new MockNode[count];
        for (int i = 0; i < count; i++) ns[i] = node("n" + i, 10, 5);
        layout(row(AlignItems.STRETCH).gap(1), count * 11, 30, ns);
        for (int i = 0; i < count; i++) {
            assertEquals(i * 11f, ns[i].xPosition, EPS);
            assertEquals(10f, ns[i].width);
            assertEquals(0f, ns[i].yPosition);
            assertEquals(30f, ns[i].height);
        }
    }

    @ParameterizedTest
    @MethodSource("stressChildCounts")
    public void columnStressWithCount(int count) {
        MockNode[] ns = new MockNode[count];
        for (int i = 0; i < count; i++) ns[i] = node("n" + i, 5, 10);
        layout(column(AlignItems.STRETCH).gap(1), 50, count * 11, ns);
        for (int i = 0; i < count; i++) {
            assertEquals(count * 11f - 10f - i * 11f, ns[i].yPosition, EPS);
            assertEquals(10f, ns[i].height);
            assertEquals(0f, ns[i].xPosition);
            assertEquals(50f, ns[i].width);
        }
    }

    // ===== ALL MODES COMBO GRID =====

    static Stream<Arguments> allModesCombo() {
        return comboSource();
    }

    @ParameterizedTest
    @MethodSource("allModesCombo")
    public void rowComboInvariants(LayoutSpec.JustifyContent jc, LayoutSpec.AlignItems ai, LayoutSpec.SizeMode smA, LayoutSpec.SizeMode smB) {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(jc).alignItems(ai).gap(2).padding(1);
        MockNode a = nodeWithSizing("a", sizing(smA, 15, 10));
        MockNode b = nodeWithSizing("b", sizing(smB, 25, 20));
        if (smA == LayoutSpec.SizeMode.GROW) a.growWeightHorizontal(1);
        if (smB == LayoutSpec.SizeMode.GROW) b.growWeightHorizontal(2);
        if (smA == LayoutSpec.SizeMode.WRAP) { a.set("prefW", 15f); a.set("prefH", 10f); }
        if (smB == LayoutSpec.SizeMode.WRAP) { b.set("prefW", 25f); b.set("prefH", 20f); }
        layout(spec, 100, 50, a, b);
        assertInvariants(spec, list(a, b), 0, 0, 100, 50);
    }

    @ParameterizedTest
    @MethodSource("allModesCombo")
    public void columnComboInvariants(LayoutSpec.JustifyContent jc, LayoutSpec.AlignItems ai, LayoutSpec.SizeMode smA, LayoutSpec.SizeMode smB) {
        LayoutSpec spec = new LayoutSpec().column().justifyContent(jc).alignItems(ai).gap(2).padding(1);
        MockNode a = nodeWithSizing("a", sizing(smA, 20, 12));
        MockNode b = nodeWithSizing("b", sizing(smB, 30, 18));
        if (smA == LayoutSpec.SizeMode.GROW) a.growWeightVertical(1);
        if (smB == LayoutSpec.SizeMode.GROW) b.growWeightVertical(2);
        if (smA == LayoutSpec.SizeMode.WRAP) { a.set("prefW", 20f); a.set("prefH", 12f); }
        if (smB == LayoutSpec.SizeMode.WRAP) { b.set("prefW", 30f); b.set("prefH", 18f); }
        layout(spec, 100, 80, a, b);
        assertInvariants(spec, list(a, b), 0, 0, 100, 80);
    }

    // ===== SUB-PIXEL INVARIANTS =====

    @Test
    public void subPixelAllChildren() {
        MockNode[] ns = new MockNode[7];
        for (int i = 0; i < 7; i++)
            ns[i] = node("n" + i, 7.33f + i * 1.1f, 4.44f + i * 0.77f);
        layout(row().gap(0.5f), 100, 50, ns);
        assertInvariants(row().gap(0.5f), list(ns), 0, 0, 100, 50);
    }

    // ===== NO OVERLAP: COMPLEX LAYOUTS =====

    @Test
    public void noOverlapComplexRow() {
        MockNode a = node("a", 20, 15);
        MockNode b = nodeWithSizing("b", sizing(LayoutSpec.SizeMode.GROW, 0, 20).growWeightHorizontal(1));
        MockNode c = nodeWithSizing("c", sizing(LayoutSpec.SizeMode.WRAP, 0, 0));
        c.set("prefW", 25f); c.set("prefH", 18f);
        MockNode d = node("d", 15, 12);
        layout(row(AlignItems.CENTER, JustifyContent.SPACE_BETWEEN).gap(3).padding(2), 150, 60, a, b, c, d);
        assertNoOverlap(list(a, b, c, d));
    }

    @Test
    public void noOverlapComplexColumn() {
        MockNode a = node("a", 30, 15);
        MockNode b = nodeWithSizing("b", sizing(LayoutSpec.SizeMode.GROW, 40, 0).growWeightVertical(1));
        MockNode c = nodeWithSizing("c", sizing(LayoutSpec.SizeMode.WRAP, 0, 0));
        c.set("prefW", 35f); c.set("prefH", 20f);
        MockNode d = node("d", 25, 12);
        layout(column(AlignItems.START, JustifyContent.SPACE_EVENLY).gap(2).padding(3), 100, 150, a, b, c, d);
        assertNoOverlap(list(a, b, c, d), true);
    }
}
