package org.mindustrytool.libs.ui.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutEngineJustifyTest extends LayoutTestBase {

    // ===== ROW: ALL JUSTIFY MODES WITH 2 CHILDREN =====

    @Test
    public void rowJustifyStart() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.START).gap(0);
        MockNode a = node("a", 20, 20);
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
    public void rowJustifyCenter() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.CENTER).gap(0);
        MockNode a = node("a", 20, 20);
        MockNode b = node("b", 30, 25);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(25f, a.xPosition, EPS), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(45f, b.xPosition, EPS), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void rowJustifyEnd() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.END).gap(0);
        MockNode a = node("a", 20, 20);
        MockNode b = node("b", 30, 25);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(50f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(70f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void rowJustifySpaceBetween() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.SPACE_BETWEEN).gap(0);
        MockNode a = node("a", 20, 20);
        MockNode b = node("b", 30, 25);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(70f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void rowJustifySpaceAround() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.SPACE_AROUND).gap(0);
        MockNode a = node("a", 20, 20);
        MockNode b = node("b", 30, 25);
        layout(spec, 100, 50, a, b);
        float around = 50f / 2f;
        assertAll(
            () -> assertEquals(around, a.xPosition, EPS), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(around + 20f + around * 2f, b.xPosition, EPS), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void rowJustifySpaceEvenly() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.SPACE_EVENLY).gap(0);
        MockNode a = node("a", 20, 20);
        MockNode b = node("b", 30, 25);
        layout(spec, 100, 50, a, b);
        float evenly = 50f / 3f;
        assertAll(
            () -> assertEquals(evenly, a.xPosition, EPS), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(evenly + 20f + evenly, b.xPosition, EPS), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    // ===== COLUMN: ALL JUSTIFY MODES =====

    @Test
    public void columnJustifyStart() {
        LayoutSpec spec = new LayoutSpec().column().justifyContent(LayoutSpec.JustifyContent.START).gap(0);
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

    @Test
    public void columnJustifyCenter() {
        LayoutSpec spec = new LayoutSpec().column().justifyContent(LayoutSpec.JustifyContent.CENTER).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(55f, a.yPosition, EPS), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(25f, b.yPosition, EPS), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void columnJustifyEnd() {
        LayoutSpec spec = new LayoutSpec().column().justifyContent(LayoutSpec.JustifyContent.END).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(30f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void columnJustifySpaceBetween() {
        LayoutSpec spec = new LayoutSpec().column().justifyContent(LayoutSpec.JustifyContent.SPACE_BETWEEN).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(80f, a.yPosition), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void columnJustifySpaceAround() {
        LayoutSpec spec = new LayoutSpec().column().justifyContent(LayoutSpec.JustifyContent.SPACE_AROUND).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(55f, a.yPosition, EPS), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(-25f, b.yPosition, EPS), () -> assertEquals(30f, b.height)
        );
    }

    @Test
    public void columnJustifySpaceEvenly() {
        LayoutSpec spec = new LayoutSpec().column().justifyContent(LayoutSpec.JustifyContent.SPACE_EVENLY).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(190f / 3f, a.yPosition, EPS), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(50f / 3f, b.yPosition, EPS), () -> assertEquals(30f, b.height)
        );
    }

    // ===== JUSTIFY WITH GAP =====

    @Test
    public void rowJustifyStartWithGap() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.START).gap(10);
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 20);
        layout(spec, 120, 50, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(30f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void rowJustifyCenterWithGap() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.CENTER).gap(10);
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 20);
        layout(spec, 120, 50, a, b);
        assertAll(
            () -> assertEquals(30f, a.xPosition, EPS), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(60f, b.xPosition, EPS), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void rowJustifyEndWithGap() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.END).gap(10);
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 20);
        layout(spec, 120, 50, a, b);
        assertAll(
            () -> assertEquals(60f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(90f, b.xPosition), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void rowJustifyBetweenWithGap() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.SPACE_BETWEEN).gap(10);
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 20);
        layout(spec, 120, 50, a, b);
        float total = 20f + 10f + 30f;
        float extra = 120f - total;
        float between = extra / 1f;
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(20f + 10f + between, b.xPosition, EPS), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void rowJustifyAroundWithGap() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.SPACE_AROUND).gap(10);
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 20);
        layout(spec, 120, 50, a, b);
        float total = 20f + 10f + 30f;
        float extra = 120f - total;
        float around = extra / 2f;
        assertAll(
            () -> assertEquals(around, a.xPosition, EPS), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(around + 20f + 10f + around * 2f, b.xPosition, EPS), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void rowJustifyEvenlyWithGap() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.SPACE_EVENLY).gap(10);
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 20);
        layout(spec, 120, 50, a, b);
        float total = 20f + 10f + 30f;
        float extra = 120f - total;
        float evenly = extra / 3f;
        assertAll(
            () -> assertEquals(evenly, a.xPosition, EPS), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(evenly + 20f + 10f + evenly, b.xPosition, EPS), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    // ===== THREE CHILDREN JUSTIFY =====

    @Test
    public void rowThreeChildrenJustifyCenter() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.CENTER).gap(0);
        MockNode a = node("a", 10, 15);
        MockNode b = node("b", 15, 20);
        MockNode c = node("c", 20, 25);
        layout(spec, 100, 50, a, b, c);
        float extra = 100f - 10f - 15f - 20f;
        assertAll(
            () -> assertEquals(extra / 2f, a.xPosition, EPS), () -> assertEquals(10f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(extra / 2f + 10f, b.xPosition, EPS), () -> assertEquals(15f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height),
            () -> assertEquals(extra / 2f + 10f + 15f, c.xPosition, EPS), () -> assertEquals(20f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(50f, c.height)
        );
    }

    @Test
    public void rowThreeChildrenJustifyEnd() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.END).gap(0);
        MockNode a = node("a", 10, 15);
        MockNode b = node("b", 15, 20);
        MockNode c = node("c", 20, 25);
        layout(spec, 100, 50, a, b, c);
        assertAll(
            () -> assertEquals(55f, a.xPosition), () -> assertEquals(10f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(65f, b.xPosition), () -> assertEquals(15f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height),
            () -> assertEquals(80f, c.xPosition), () -> assertEquals(20f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void rowThreeChildrenSpaceBetween() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.SPACE_BETWEEN).gap(0);
        MockNode a = node("a", 10, 15);
        MockNode b = node("b", 15, 20);
        MockNode c = node("c", 20, 25);
        layout(spec, 100, 50, a, b, c);
        float extra = 100f - 45f;
        float between = extra / 2f;
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(10f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(10f + between, b.xPosition, EPS), () -> assertEquals(15f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height),
            () -> assertEquals(10f + between + 15f + between, c.xPosition, EPS), () -> assertEquals(20f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void rowThreeChildrenSpaceAround() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.SPACE_AROUND).gap(0);
        MockNode a = node("a", 10, 15);
        MockNode b = node("b", 15, 20);
        MockNode c = node("c", 20, 25);
        layout(spec, 100, 50, a, b, c);
        float extra = 100f - 45f;
        float around = extra / 3f;
        assertAll(
            () -> assertEquals(around, a.xPosition, EPS), () -> assertEquals(10f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(around + 10f + around * 2f, b.xPosition, EPS), () -> assertEquals(15f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height),
            () -> assertEquals(around + 10f + around * 2f + 15f + around * 2f, c.xPosition, EPS), () -> assertEquals(20f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    @Test
    public void rowThreeChildrenSpaceEvenly() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.SPACE_EVENLY).gap(0);
        MockNode a = node("a", 10, 15);
        MockNode b = node("b", 15, 20);
        MockNode c = node("c", 20, 25);
        layout(spec, 100, 50, a, b, c);
        float extra = 100f - 45f;
        float evenly = extra / 4f;
        assertAll(
            () -> assertEquals(evenly, a.xPosition, EPS), () -> assertEquals(10f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(evenly + 10f + evenly, b.xPosition, EPS), () -> assertEquals(15f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height),
            () -> assertEquals(evenly + 10f + evenly + 15f + evenly, c.xPosition, EPS), () -> assertEquals(20f, c.width),
            () -> assertEquals(0f, c.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    // ===== JUSTIFY EDGE CASES =====

    @Test
    public void singleChildJustifyCenter() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.CENTER).gap(0);
        MockNode a = node("a", 30, 20);
        layout(spec, 100, 50, a);
        assertEquals(35f, a.xPosition, EPS);
        assertEquals(30f, a.width);
        assertEquals(0f, a.yPosition);
        assertEquals(50f, a.height);
    }

    @Test
    public void singleChildJustifyEnd() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.END).gap(0);
        MockNode a = node("a", 30, 20);
        layout(spec, 100, 50, a);
        assertEquals(70f, a.xPosition);
        assertEquals(30f, a.width);
        assertEquals(0f, a.yPosition);
        assertEquals(50f, a.height);
    }

    @Test
    public void singleChildJustifyBetween() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.SPACE_BETWEEN).gap(0);
        MockNode a = node("a", 30, 20);
        layout(spec, 100, 50, a);
        assertEquals(0f, a.xPosition);
        assertEquals(30f, a.width);
    }

    @Test
    public void singleChildJustifyAround() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.SPACE_AROUND).gap(0);
        MockNode a = node("a", 30, 20);
        layout(spec, 100, 50, a);
        float extra = 100f - 30f;
        assertEquals(extra / 1f, a.xPosition, EPS);
    }

    @Test
    public void singleChildJustifyEvenly() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.SPACE_EVENLY).gap(0);
        MockNode a = node("a", 30, 20);
        layout(spec, 100, 50, a);
        assertEquals(35f, a.xPosition, EPS);
    }

    @Test
    public void exactFitJustifyCenter() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.CENTER).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 20);
        layout(spec, 70, 50, a, b);
        assertEquals(0f, a.xPosition);
        assertEquals(30f, b.xPosition);
    }

    // ===== OVERFLOW WITH JUSTIFY =====

    @Test
    public void rowJustifyEndContentOverflows() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.END).gap(0);
        MockNode a = node("a", 60, 20);
        MockNode b = node("b", 60, 20);
        layout(spec, 100, 50, a, b);
        assertEquals(-20f, a.xPosition);
        assertEquals(40f, b.xPosition);
    }

    @Test
    public void rowJustifyCenterContentOverflows() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.CENTER).gap(0);
        MockNode a = node("a", 60, 20);
        MockNode b = node("b", 60, 20);
        layout(spec, 100, 50, a, b);
        assertEquals(-10f, a.xPosition);
        assertEquals(50f, b.xPosition);
    }

    // ===== PARAMETERIZED: ALL JUSTIFY WITH 2 CHILDREN =====

    static Stream<Arguments> rowJustifyTwoChildrenParams() {
        return Stream.of(
            Arguments.of(LayoutSpec.JustifyContent.START, 0f, 20f),
            Arguments.of(LayoutSpec.JustifyContent.CENTER, 25f, 45f),
            Arguments.of(LayoutSpec.JustifyContent.END, 50f, 70f),
            Arguments.of(LayoutSpec.JustifyContent.SPACE_BETWEEN, 0f, 70f),
            Arguments.of(LayoutSpec.JustifyContent.SPACE_AROUND, 25f, 95f),
            Arguments.of(LayoutSpec.JustifyContent.SPACE_EVENLY, 50f / 3f, 50f / 3f + 20f + 50f / 3f)
        );
    }

    @ParameterizedTest
    @MethodSource("rowJustifyTwoChildrenParams")
    public void rowJustifyTwoChildren(LayoutSpec.JustifyContent jc, float exA, float exB) {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(jc).gap(0);
        MockNode a = node("a", 20, 15);
        MockNode b = node("b", 30, 20);
        layout(spec, 100, 50, a, b);
        assertAll(
            () -> assertEquals(exA, a.xPosition, EPS), () -> assertEquals(20f, a.width),
            () -> assertEquals(0f, a.yPosition), () -> assertEquals(50f, a.height),
            () -> assertEquals(exB, b.xPosition, EPS), () -> assertEquals(30f, b.width),
            () -> assertEquals(0f, b.yPosition), () -> assertEquals(50f, b.height)
        );
    }

    static Stream<Arguments> columnJustifyTwoChildrenParams() {
        return Stream.of(
            Arguments.of(LayoutSpec.JustifyContent.START, 80f, 50f),
            Arguments.of(LayoutSpec.JustifyContent.CENTER, 55f, 25f),
            Arguments.of(LayoutSpec.JustifyContent.END, 30f, 0f),
            Arguments.of(LayoutSpec.JustifyContent.SPACE_BETWEEN, 80f, 0f),
            Arguments.of(LayoutSpec.JustifyContent.SPACE_AROUND, 55f, -25f),
            Arguments.of(LayoutSpec.JustifyContent.SPACE_EVENLY, 190f / 3f, 50f / 3f)
        );
    }

    @ParameterizedTest
    @MethodSource("columnJustifyTwoChildrenParams")
    public void columnJustifyTwoChildren(LayoutSpec.JustifyContent jc, float eyA, float eyB) {
        LayoutSpec spec = new LayoutSpec().column().justifyContent(jc).gap(0);
        MockNode a = node("a", 30, 20);
        MockNode b = node("b", 40, 30);
        layout(spec, 100, 100, a, b);
        assertAll(
            () -> assertEquals(0f, a.xPosition), () -> assertEquals(100f, a.width),
            () -> assertEquals(eyA, a.yPosition, EPS), () -> assertEquals(20f, a.height),
            () -> assertEquals(0f, b.xPosition), () -> assertEquals(100f, b.width),
            () -> assertEquals(eyB, b.yPosition, EPS), () -> assertEquals(30f, b.height)
        );
    }

    // ===== FIVE CHILDREN JUSTIFY =====

    @Test
    public void fiveChildrenJustifyStart() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.START).gap(0);
        MockNode[] ns = new MockNode[5];
        for (int i = 0; i < 5; i++) ns[i] = node("n" + i, 10, 10);
        layout(spec, 100, 30, ns);
        for (int i = 0; i < 5; i++) {
            assertEquals(i * 10f, ns[i].xPosition);
            assertEquals(10f, ns[i].width);
            assertEquals(0f, ns[i].yPosition);
            assertEquals(30f, ns[i].height);
        }
    }

    @Test
    public void fiveChildrenJustifyEnd() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.END).gap(0);
        MockNode[] ns = new MockNode[5];
        for (int i = 0; i < 5; i++) ns[i] = node("n" + i, 10, 10);
        layout(spec, 100, 30, ns);
        for (int i = 0; i < 5; i++) {
            assertEquals(50f + i * 10f, ns[i].xPosition);
            assertEquals(10f, ns[i].width);
        }
    }

    @Test
    public void fiveChildrenSpaceEvenly() {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(LayoutSpec.JustifyContent.SPACE_EVENLY).gap(0);
        MockNode[] ns = new MockNode[5];
        for (int i = 0; i < 5; i++) ns[i] = node("n" + i, 10, 10);
        layout(spec, 100, 30, ns);
        float evenly = 50f / 6f;
        for (int i = 0; i < 5; i++) {
            assertEquals(evenly + i * (10f + evenly), ns[i].xPosition, EPS);
        }
    }

    // ===== PARAMETERIZED: SIX JUSTIFY MODES IN ROWS =====

    @ParameterizedTest
    @MethodSource("justifyModes")
    public void rowJustifyAllModesInvariants(LayoutSpec.JustifyContent jc) {
        LayoutSpec spec = new LayoutSpec().row().justifyContent(jc).gap(0);
        MockNode[] ns = new MockNode[]{node("a", 20, 15), node("b", 25, 20), node("c", 30, 25)};
        layout(spec, 200, 80, ns);
        assertInvariants(spec, list(ns), 0, 0, 200, 80);
    }

    @ParameterizedTest
    @MethodSource("justifyModes")
    public void columnJustifyAllModesInvariants(LayoutSpec.JustifyContent jc) {
        LayoutSpec spec = new LayoutSpec().column().justifyContent(jc).gap(0);
        MockNode[] ns = new MockNode[]{node("a", 30, 15), node("b", 40, 20), node("c", 50, 25)};
        layout(spec, 150, 150, ns);
        assertInvariants(spec, list(ns), 0, 0, 150, 150);
    }
}
