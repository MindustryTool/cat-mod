package org.mindustrytool.libs.ui.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutEngineWrapTest extends LayoutTestBase {

    // ===== ROW WRAP: TWO LINES =====

    @Test
    public void rowWrapTwoLines() {
        LayoutSpec spec = row().wrap().gap(0);
        MockNode a = node("a", 40, 20);
        MockNode b = node("b", 40, 30);
        MockNode c = node("c", 40, 25);
        layout(spec, 80, 80, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(40f, a.width),
            () -> assertEquals(50f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(40f, b.xPosition), () -> assertEquals(40f, b.width),
            () -> assertEquals(50f, b.yPosition), () -> assertEquals(30f, b.height),
            () -> assertEquals(0f, c.xPosition), () -> assertEquals(40f, c.width),
            () -> assertEquals(25f, c.yPosition), () -> assertEquals(25f, c.height)
        );
    }

    @Test
    public void columnWrapTwoLines() {
        LayoutSpec spec = column().wrap().gap(0);
        MockNode a = node("a", 40, 30);
        MockNode b = node("b", 60, 30);
        MockNode c = node("c", 50, 30);
        layout(spec, 100, 60, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(40f, a.width),
            () -> assertEquals(30f, a.yPosition), () -> assertEquals(30f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(60f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(30f, b.height),
            () -> assertEquals(60f, c.xPosition), () -> assertEquals(50f, c.width),
            () -> assertEquals(30f, c.yPosition), () -> assertEquals(30f, c.height)
        );
    }

    // ===== ROW WRAP THREE LINES =====

    @Test
    public void rowWrapThreeLines() {
        LayoutSpec spec = row().wrap().gap(0);
        MockNode a = node("a", 60, 15);
        MockNode b = node("b", 60, 20);
        MockNode c = node("c", 60, 25);
        MockNode d = node("d", 60, 30);
        layout(spec, 100, 100, a, b, c, d);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(60f, a.width),
            () -> assertEquals(85f, a.yPosition), () -> assertEquals(15f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(60f, b.width),
            () -> assertEquals(65f, b.yPosition), () -> assertEquals(20f, b.height),
            () -> assertEquals(0f, c.xPosition), () -> assertEquals(60f, c.width),
            () -> assertEquals(40f, c.yPosition), () -> assertEquals(25f, c.height),
            () -> assertEquals(0f, d.xPosition), () -> assertEquals(60f, d.width),
            () -> assertEquals(10f, d.yPosition), () -> assertEquals(30f, d.height)
        );
    }

    @Test
    public void columnWrapThreeLines() {
        LayoutSpec spec = column().wrap().gap(0);
        MockNode a = node("a", 40, 50);
        MockNode b = node("b", 50, 50);
        MockNode c = node("c", 60, 50);
        MockNode d = node("d", 70, 50);
        layout(spec, 150, 100, a, b, c, d);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(40f, a.width),
            () -> assertEquals(50f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height),
            () -> assertEquals(50f, c.xPosition), () -> assertEquals(60f, c.width),
            () -> assertEquals(50f, c.yPosition), () -> assertEquals(50f, c.height),
            () -> assertEquals(50f, d.xPosition), () -> assertEquals(70f, d.width),
            () -> assertEquals(0f, d.yPosition), () -> assertEquals(50f, d.height)
        );
    }

    // ===== WRAP + GAP =====

    @Test
    public void rowWrapWithGap() {
        LayoutSpec spec = row().wrap().gap(10);
        MockNode a = node("a", 35, 20);
        MockNode b = node("b", 35, 25);
        MockNode c = node("c", 35, 30);
        layout(spec, 80, 100, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(35f, a.width),
            () -> assertEquals(75f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(45f, b.xPosition), () -> assertEquals(35f, b.width),
            () -> assertEquals(75f, b.yPosition), () -> assertEquals(25f, b.height),
            () -> assertEquals(0f, c.xPosition), () -> assertEquals(35f, c.width),
            () -> assertEquals(35f, c.yPosition), () -> assertEquals(30f, c.height)
        );
    }

    @Test
    public void columnWrapWithGap() {
        LayoutSpec spec = column().wrap().gap(10);
        MockNode a = node("a", 30, 35);
        MockNode b = node("b", 40, 35);
        MockNode c = node("c", 50, 35);
        layout(spec, 100, 80, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(45f, a.yPosition), () -> assertEquals(35f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(40f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(35f, b.height),
            () -> assertEquals(50f, c.xPosition), () -> assertEquals(50f, c.width),
            () -> assertEquals(45f, c.yPosition), () -> assertEquals(35f, c.height)
        );
    }

    // ===== WRAP + JUSTIFY =====

    @Test
    public void rowWrapJustifyCenter() {
        LayoutSpec spec = row().wrap().justifyContent(LayoutSpec.JustifyContent.CENTER).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 30, 25);
        MockNode c = node("c", 30, 30);
        layout(spec, 80, 100, a, b, c);
        assertAll(
            () -> assertEquals(10f, a.xPosition, EPS), () -> assertEquals(30f, a.width),
            () -> assertEquals(75f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(40f, b.xPosition, EPS), () -> assertEquals(30f, b.width),
            () -> assertEquals(75f, b.yPosition), () -> assertEquals(25f, b.height),
            () -> assertEquals(25f, c.xPosition, EPS), () -> assertEquals(30f, c.width),
            () -> assertEquals(45f, c.yPosition), () -> assertEquals(30f, c.height)
        );
    }

    @Test
    public void rowWrapJustifyEnd() {
        LayoutSpec spec = row().wrap().justifyContent(LayoutSpec.JustifyContent.END).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 30, 25);
        MockNode c = node("c", 30, 30);
        layout(spec, 80, 100, a, b, c);
        assertAll(
            () -> assertEquals(20f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(75f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(50f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(75f, b.yPosition), () -> assertEquals(25f, b.height),
            () -> assertEquals(50f, c.xPosition), () -> assertEquals(30f, c.width),
            () -> assertEquals(45f, c.yPosition), () -> assertEquals(30f, c.height)
        );
    }

    @Test
    public void rowWrapJustifyBetween() {
        LayoutSpec spec = row().wrap().justifyContent(LayoutSpec.JustifyContent.SPACE_BETWEEN).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 30, 25);
        MockNode c = node("c", 30, 30);
        layout(spec, 80, 100, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(75f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(50f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(75f, b.yPosition), () -> assertEquals(25f, b.height),
            () -> assertEquals(0f, c.xPosition, EPS), () -> assertEquals(30f, c.width),
            () -> assertEquals(45f, c.yPosition), () -> assertEquals(30f, c.height)
        );
    }

    @Test
    public void columnWrapJustifyCenter() {
        LayoutSpec spec = column().wrap().justifyContent(LayoutSpec.JustifyContent.CENTER).gap(0);
        MockNode a = node("a", 40, 25);
        MockNode b = node("b", 50, 25);
        MockNode c = node("c", 60, 25);
        layout(spec, 120, 60, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(40f, a.width),
            () -> assertEquals(30f, a.yPosition, EPS), () -> assertEquals(25f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(5f, b.yPosition, EPS), () -> assertEquals(25f, b.height),
            () -> assertEquals(50f, c.xPosition, EPS), () -> assertEquals(60f, c.width),
            () -> assertEquals(17.5f, c.yPosition, EPS), () -> assertEquals(25f, c.height)
        );
    }

    // ===== WRAP + ALIGN ITEMS =====

    @Test
    public void rowWrapAlignItemsEnd() {
        LayoutSpec spec = row().wrap().alignItems(LayoutSpec.AlignItems.END).gap(0);
        MockNode a = node("a", 35, 20);
        MockNode b = node("b", 35, 30);
        MockNode c = node("c", 35, 25);
        layout(spec, 70, 80, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(35f, a.width),
            () -> assertEquals(60f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(35f, b.xPosition), () -> assertEquals(35f, b.width),
            () -> assertEquals(50f, b.yPosition), () -> assertEquals(30f, b.height),
            () -> assertEquals(0f, c.xPosition), () -> assertEquals(35f, c.width),
            () -> assertEquals(25f, c.yPosition), () -> assertEquals(25f, c.height)
        );
    }

    @Test
    public void rowWrapAlignItemsCenter() {
        LayoutSpec spec = row().wrap().alignItems(LayoutSpec.AlignItems.CENTER).gap(0);
        MockNode a = node("a", 35, 20);
        MockNode b = node("b", 35, 30);
        MockNode c = node("c", 35, 25);
        layout(spec, 70, 80, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(35f, a.width),
            () -> assertEquals(55f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(35f, b.xPosition), () -> assertEquals(35f, b.width),
            () -> assertEquals(50f, b.yPosition), () -> assertEquals(30f, b.height),
            () -> assertEquals(0f, c.xPosition), () -> assertEquals(35f, c.width),
            () -> assertEquals(25f, c.yPosition), () -> assertEquals(25f, c.height)
        );
    }

    @Test
    public void columnWrapAlignItemsCenter() {
        LayoutSpec spec = column().wrap().alignItems(LayoutSpec.AlignItems.CENTER).gap(0);
        MockNode a = node("a", 40, 25);
        MockNode b = node("b", 50, 25);
        MockNode c = node("c", 60, 25);
        layout(spec, 120, 60, a, b, c);
        assertAll(
            () -> assertEquals(5f, a.xPosition, EPS), () -> assertEquals(40f, a.width),
            () -> assertEquals(35f, a.yPosition), () -> assertEquals(25f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(10f, b.yPosition), () -> assertEquals(25f, b.height),
            () -> assertEquals(50f, c.xPosition, EPS), () -> assertEquals(60f, c.width),
            () -> assertEquals(35f, c.yPosition), () -> assertEquals(25f, c.height)
        );
    }

    // ===== WRAP + REVERSE =====

    @Test
    public void rowWrapReverse() {
        LayoutSpec spec = row().wrap().reverse(true).gap(0);
        MockNode a = node("a", 40, 20);
        MockNode b = node("b", 40, 25);
        MockNode c = node("c", 40, 30);
        layout(spec, 80, 80, a, b, c);
        assertAll(
            () -> assertEquals(40f, a.xPosition), () -> assertEquals(40f, a.width),
            () -> assertEquals(55f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(40f, b.width),
            () -> assertEquals(55f, b.yPosition), () -> assertEquals(25f, b.height),
            () -> assertEquals(40f, c.xPosition), () -> assertEquals(40f, c.width),
            () -> assertEquals(25f, c.yPosition), () -> assertEquals(30f, c.height)
        );
    }

    @Test
    public void columnWrapReverse() {
        LayoutSpec spec = column().wrap().reverse(true).gap(0);
        MockNode a = node("a", 40, 30);
        MockNode b = node("b", 50, 30);
        MockNode c = node("c", 60, 30);
        layout(spec, 120, 60, a, b, c);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(40f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(30f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(30f, b.yPosition), () -> assertEquals(30f, b.height),
            () -> assertEquals(50f, c.xPosition), () -> assertEquals(60f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(30f, c.height)
        );
    }

    // ===== WRAP + GROW =====

    @Test
    public void rowWrapWithGrow() {
        LayoutSpec spec = row().wrap().gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = nodeWithSizing("b", sizing(NodeSpec.SizeMode.GROW, 0, 25).growWeightHorizontal(1));
        MockNode c = node("c", 30, 30);
        layout(spec, 100, 80, a, b, c);
        float growW = 100f - 30f - 30f;
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(30f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(30f, b.xPosition), () -> assertEquals(growW, b.width, EPS),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(25f, b.height),
            () -> assertEquals(70f, c.xPosition), () -> assertEquals(30f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(30f, c.height)
        );
    }

    @Test
    public void columnWrapWithGrow() {
        LayoutSpec spec = column().wrap().gap(0);
        MockNode a = node("a", 40, 30);
        MockNode b = nodeWithSizing("b", sizing(NodeSpec.SizeMode.GROW, 50, 0).growWeightVertical(1));
        MockNode c = node("c", 60, 30);
        layout(spec, 150, 80, a, b, c);
        float growH = 80f - 30f - 30f;
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(40f, a.width),
            () -> assertEquals(50f, a.yPosition), () -> assertEquals(30f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(50f, b.width),
            () -> assertEquals(30f, b.yPosition), () -> assertEquals(growH, b.height, EPS),
            () -> assertEquals(0f, c.xPosition), () -> assertEquals(60f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(30f, c.height)
        );
    }

    // ===== WRAP EDGE CASES =====

    @Test
    public void noWrapDoesNotWrap() {
        LayoutSpec spec = row().noWrap().gap(0);
        MockNode a = node("a", 60, 20);
        MockNode b = node("b", 60, 25);
        layout(spec, 80, 50, a, b);
        assertEquals(0f, a.xPosition);
        assertEquals(60f, a.width);
        assertEquals(60f, b.xPosition);
        assertEquals(60f, b.width);
    }

    @Test
    public void singleChildWrapping() {
        LayoutSpec spec = row().wrap().gap(0);
        MockNode a = node("a", 30, 20);
        layout(spec, 50, 50, a);
        assertEquals(0f, a.xPosition);
        assertEquals(30f, a.width);
        assertEquals(0f, a.yPosition);
        assertEquals(20f, a.height);
    }

    @Test
    public void allChildrenExactlyFitNoWrap() {
        LayoutSpec spec = row().wrap().gap(0);
        MockNode a = node("a", 40, 20);
        MockNode b = node("b", 40, 25);
        layout(spec, 80, 60, a, b);
        assertEquals(0f, a.xPosition);
        assertEquals(40f, b.xPosition);
        assertEquals(0f, b.yPosition);
    }

    @Test
    public void wrapWithZeroChildren() {
        LayoutSpec spec = row().wrap().gap(0);
        layout(spec, 100, 50);
    }

    @Test
    public void rowWrapInvariants() {
        LayoutSpec spec = row().wrap().gap(5);
        MockNode a = node("a", 30, 15);
        MockNode b = node("b", 30, 20);
        MockNode c = node("c", 30, 25);
        MockNode d = node("d", 30, 30);
        layout(spec, 70, 100, a, b, c, d);
        assertInvariants(spec, list(a, b, c, d), 0, 0, 70, 100);
    }

    @Test
    public void columnWrapInvariants() {
        LayoutSpec spec = column().wrap().gap(5);
        MockNode a = node("a", 40, 25);
        MockNode b = node("b", 50, 25);
        MockNode c = node("c", 60, 25);
        MockNode d = node("d", 70, 25);
        layout(spec, 150, 60, a, b, c, d);
        assertInvariants(spec, list(a, b, c, d), 0, 0, 150, 60);
    }
}
