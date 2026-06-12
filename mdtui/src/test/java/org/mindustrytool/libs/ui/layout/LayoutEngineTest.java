package org.mindustrytool.libs.ui.layout;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutEngineTest extends LayoutTestBase {

    @Test
    public void testBasicRowLayout() {
        LayoutSpec spec = LayoutSpec.builder().row().gap(10f).build();
        MockNode node1 = node("node1", 30f, 20f);
        MockNode node2 = node("node2", 40f, 25f);
        List<MockNode> children = Arrays.asList(node1, node2);
        LayoutEngine.layout(spec, children, 0f, 0f, 100f, 50f, ACCESSOR);
        assertEquals(0f, node1.xPosition);
        assertEquals(30f, node1.width);
        assertEquals(40f, node2.xPosition);
        assertEquals(40f, node2.width);
        assertEquals(0f, node1.yPosition);
        assertEquals(50f, node1.height);
        assertEquals(0f, node2.yPosition);
        assertEquals(50f, node2.height);
    }

    @Test
    public void testAlignSelfOverride() {
        LayoutSpec spec = LayoutSpec.builder().row().alignItems(LayoutSpec.AlignItems.START).build();
        MockNode node1 = node("node1", 30f, 20f);
        MockNode node2 = node("node2", 40f, 10f);
        node2.alignSelf(LayoutSpec.AlignSelf.END);
        List<MockNode> children = Arrays.asList(node1, node2);
        LayoutEngine.layout(spec, children, 0f, 0f, 100f, 50f, ACCESSOR);
        assertEquals(0f, node1.yPosition);
        assertEquals(20f, node1.height);
        assertEquals(40f, node2.yPosition);
        assertEquals(10f, node2.height);
    }

    @Test
    public void testJustifyEvenly() {
        LayoutSpec spec = LayoutSpec.builder().row().justifyContent(LayoutSpec.JustifyContent.SPACE_EVENLY).gap(0f).build();
        MockNode node1 = node("node1", 20f, 20f);
        MockNode node2 = node("node2", 30f, 20f);
        List<MockNode> children = Arrays.asList(node1, node2);
        LayoutEngine.layout(spec, children, 0f, 0f, 100f, 50f, ACCESSOR);
        float expectedSpace = 50f / 3f;
        assertEquals(expectedSpace, node1.xPosition, 0.001f);
        assertEquals(expectedSpace + 20f + expectedSpace, node2.xPosition, 0.001f);
    }

    @Test
    public void testReverseRowLayout() {
        LayoutSpec spec = LayoutSpec.builder().row().reverse().gap(10f).build();
        MockNode node1 = node("node1", 30f, 20f);
        MockNode node2 = node("node2", 40f, 20f);
        List<MockNode> children = Arrays.asList(node1, node2);
        LayoutEngine.layout(spec, children, 0f, 0f, 100f, 50f, ACCESSOR);
        assertEquals(70f, node1.xPosition);
        assertEquals(20f, node2.xPosition);
    }

    @Test
    public void testFlexWrapMultiLine() {
        LayoutSpec spec = LayoutSpec.builder().row().wrap().gap(10f).alignItems(LayoutSpec.AlignItems.START).build();
        MockNode node1 = node("node1", 30f, 20f);
        MockNode node2 = node("node2", 30f, 15f);
        MockNode node3 = node("node3", 30f, 25f);
        MockNode node4 = node("node4", 30f, 10f);
        List<MockNode> children = Arrays.asList(node1, node2, node3, node4);
        LayoutEngine.layout(spec, children, 0f, 0f, 100f, 80f, ACCESSOR);
        assertEquals(0f, node1.xPosition);
        assertEquals(40f, node2.xPosition);
        assertEquals(0f, node3.xPosition);
        assertEquals(40f, node4.xPosition);
        assertEquals(60f, node1.yPosition);
        assertEquals(60f, node2.yPosition);
        assertEquals(25f, node3.yPosition);
        assertEquals(25f, node4.yPosition);
    }
}
