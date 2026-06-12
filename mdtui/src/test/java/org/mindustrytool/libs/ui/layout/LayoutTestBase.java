package org.mindustrytool.libs.ui.layout;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

public abstract class LayoutTestBase {

    protected static final float EPS = 0.001f;

    protected static class MockNode {
        final String name;
        final NodeSpec<?> sizing = new NodeSpec<>();
        boolean visible = true;
        boolean returnNullSizing = false;
        float preferredWidth;
        float preferredHeight;
        float xPosition = -9999f;
        float yPosition = -9999f;
        float width = -1f;
        float height = -1f;
        boolean boundsSet = false;

        MockNode(String name, float preferredWidth, float preferredHeight) {
            this.name = name;
            this.preferredWidth = preferredWidth;
            this.preferredHeight = preferredHeight;
        }

        NodeSpec<?> getSizingObj() { return sizing; }

        void set(String key, Object value) {
            switch (key) {
                case "prefW": preferredWidth = (Float) value; break;
                case "prefH": preferredHeight = (Float) value; break;
                case "visible": visible = (Boolean) value; break;
                case "minW": sizing.minimumWidth((Float) value); break;
                case "maxW": sizing.maximumWidth((Float) value); break;
                case "minH": sizing.minimumHeight((Float) value); break;
                case "maxH": sizing.maximumHeight((Float) value); break;
                case "sizing": returnNullSizing = value == null; break;
                default: break;
            }
        }

        MockNode copy() {
            MockNode c = new MockNode(name, preferredWidth, preferredHeight);
            c.visible = visible;
            c.returnNullSizing = returnNullSizing;
            c.xPosition = xPosition; c.yPosition = yPosition;
            c.width = width; c.height = height; c.boundsSet = boundsSet;
            NodeSpec<?> t = c.sizing;
            NodeSpec<?> s = sizing;
            t.widthMode(s.getWidthMode()); t.heightMode(s.getHeightMode());
            t.fixedWidth(s.getFixedWidth()); t.fixedHeight(s.getFixedHeight());
            t.growWeightHorizontal(s.getGrowWeightHorizontal());
            t.growWeightVertical(s.getGrowWeightVertical());
            t.alignSelf(s.getAlignSelf());
            t.padding(s.getPaddingTop(), s.getPaddingRight(), s.getPaddingBottom(), s.getPaddingLeft());
            t.minimumWidth(s.getMinimumWidth()); t.maximumWidth(s.getMaximumWidth());
            t.minimumHeight(s.getMinimumHeight()); t.maximumHeight(s.getMaximumHeight());
            return c;
        }

        void reset() {
            xPosition = -9999f; yPosition = -9999f; width = -1f; height = -1f;
            boundsSet = false;
        }
    }

    protected static final LayoutAccessor<MockNode> ACCESSOR = new LayoutAccessor<>() {
        @Override public boolean isVisible(MockNode node) { return node.visible; }
        @Override public float getPreferredWidth(MockNode node) { return node.preferredWidth; }
        @Override public float getPreferredHeight(MockNode node) { return node.preferredHeight; }
        @Override public NodeSpec<?> getSizing(MockNode node) { return node.returnNullSizing ? null : node.sizing; }
        @Override
        public void setBounds(MockNode node, float xPosition, float yPosition, float width, float height) {
            node.xPosition = xPosition; node.yPosition = yPosition; node.width = width; node.height = height;
            node.boundsSet = true;
        }
    };

    protected static MockNode node(String name, float pw, float ph) {
        return new MockNode(name, pw, ph);
    }

    protected static MockNode invisible(String name, float pw, float ph) {
        MockNode n = new MockNode(name, pw, ph);
        n.visible = false;
        return n;
    }

    protected static List<MockNode> list(MockNode... nodes) {
        return Arrays.asList(nodes);
    }

    protected static void layout(LayoutSpec spec, List<MockNode> children, float x, float y, float w, float h) {
        LayoutEngine.layout(spec, children, x, y, w, h, ACCESSOR);
    }

    protected static void layout(LayoutSpec spec, float w, float h, MockNode... children) {
        LayoutEngine.layout(spec, Arrays.asList(children), 0, 0, w, h, ACCESSOR);
    }

    protected static void layout(LayoutSpec spec, float x, float y, float w, float h, MockNode... children) {
        LayoutEngine.layout(spec, Arrays.asList(children), x, y, w, h, ACCESSOR);
    }

    protected static float prefW(LayoutSpec spec, MockNode... children) {
        return LayoutEngine.prefWidth(spec, spec.isColumn(), spec.getGap(), Arrays.asList(children), ACCESSOR);
    }

    protected static float prefH(LayoutSpec spec, MockNode... children) {
        return LayoutEngine.prefHeight(spec, spec.isColumn(), spec.getGap(), Arrays.asList(children), ACCESSOR);
    }

    protected static void assertNoOverlap(List<MockNode> children) {
        assertNoOverlap(children, false);
    }

    protected static void assertNoOverlap(List<MockNode> children, boolean isColumn) {
        for (int i = 1; i < children.size(); i++) {
            MockNode prev = children.get(i - 1);
            MockNode curr = children.get(i);
            if (prev.visible && curr.visible) {
                if (isColumn) {
                    float prevEnd = prev.yPosition + prev.height;
                    float currStart = curr.yPosition;
                    float currEnd = curr.yPosition + curr.height;
                    float prevStart = prev.yPosition;
                    org.junit.jupiter.api.Assertions.assertTrue(prevEnd <= currStart + EPS || currEnd <= prevStart + EPS,
                        () -> "Overlap: " + prev.name + " bottom=" + prevEnd + " vs " + curr.name + " top=" + currStart);
                } else {
                    float prevEnd = prev.xPosition + prev.width;
                    float currStart = curr.xPosition;
                    float currEnd = curr.xPosition + curr.width;
                    float prevStart = prev.xPosition;
                    org.junit.jupiter.api.Assertions.assertTrue(prevEnd <= currStart + EPS || currEnd <= prevStart + EPS,
                        () -> "Overlap: " + prev.name + " end=" + prevEnd + " vs " + curr.name + " start=" + currStart);
                }
            }
        }
    }

    protected static void assertInvariants(LayoutSpec spec, List<MockNode> children, float x, float y, float w, float h) {
        boolean isColumn = spec.isColumn();
        float mainLimit = isColumn ? h : w;

        for (MockNode child : children) {
            if (!child.visible) continue;
            org.junit.jupiter.api.Assertions.assertTrue(child.width >= 0, () -> child.name + " negative width: " + child.width);
            org.junit.jupiter.api.Assertions.assertTrue(child.height >= 0, () -> child.name + " negative height: " + child.height);

            float crossPos = isColumn ? child.xPosition : child.yPosition;
            float crossSize = isColumn ? child.width : child.height;
            org.junit.jupiter.api.Assertions.assertTrue(crossPos >= 0 - EPS,
                () -> child.name + " cross pos < 0: " + crossPos);
            float crossLimit = isColumn ? w : h;
            org.junit.jupiter.api.Assertions.assertTrue(crossPos + crossSize <= crossLimit + EPS,
                () -> child.name + " cross overflow: " + (crossPos + crossSize) + " > " + crossLimit);
        }

        if (!spec.isWrap()) {
            float totalMain = 0;
            int visibleCount = 0;
            for (MockNode c : children) {
                if (!c.visible) continue;
                visibleCount++;
                totalMain += (isColumn ? c.height : c.width);
            }
            if (visibleCount > 1) totalMain += spec.getGap() * (visibleCount - 1);
            final float totalMainFinal = totalMain;
            org.junit.jupiter.api.Assertions.assertTrue(totalMainFinal <= mainLimit + EPS,
                () -> "Total main size " + totalMainFinal + " > limit " + mainLimit);
        }

        assertNoOverlap(children, isColumn);
    }

    // Convenience builders
    protected static LayoutSpec row() { return new LayoutSpec().row().alignItems(LayoutSpec.AlignItems.START); }
    protected static LayoutSpec row(LayoutSpec.AlignItems ai) { return new LayoutSpec().row().alignItems(ai); }
    protected static LayoutSpec row(LayoutSpec.AlignItems ai, LayoutSpec.JustifyContent jc) {
        return new LayoutSpec().row().alignItems(ai).justifyContent(jc);
    }
    protected static LayoutSpec column() { return new LayoutSpec().column().alignItems(LayoutSpec.AlignItems.START); }
    protected static LayoutSpec column(LayoutSpec.AlignItems ai) { return new LayoutSpec().column().alignItems(ai); }
    protected static LayoutSpec column(LayoutSpec.AlignItems ai, LayoutSpec.JustifyContent jc) {
        return new LayoutSpec().column().alignItems(ai).justifyContent(jc);
    }

    protected static NodeSpec<?> sizing(NodeSpec.SizeMode mode, float pw, float ph) {
        NodeSpec<?> s = new NodeSpec<>();
        switch (mode) {
            case FIXED:
                s.fixedWidth(pw).fixedHeight(ph).widthMode(NodeSpec.SizeMode.FIXED).heightMode(NodeSpec.SizeMode.FIXED);
                break;
            case GROW:
                s.fixedWidth(pw).fixedHeight(ph).growX().growY();
                break;
            default:
                s.fixedWidth(pw).fixedHeight(ph);
                break;
        }
        return s;
    }

    protected static MockNode nodeWithSizing(String name, NodeSpec<?> customSizing) {
        MockNode n = new MockNode(name, customSizing.getFixedWidth(), customSizing.getFixedHeight());
        NodeSpec<?> target = n.sizing;
        target.fixedWidth(customSizing.getFixedWidth());
        target.fixedHeight(customSizing.getFixedHeight());
        target.widthMode(customSizing.getWidthMode());
        target.heightMode(customSizing.getHeightMode());
        target.growWeightHorizontal(customSizing.getGrowWeightHorizontal());
        target.growWeightVertical(customSizing.getGrowWeightVertical());
        target.alignSelf(customSizing.getAlignSelf());
        target.padding(customSizing.getPaddingTop(), customSizing.getPaddingRight(), customSizing.getPaddingBottom(), customSizing.getPaddingLeft());
        target.minimumWidth(customSizing.getMinimumWidth());
        target.maximumWidth(customSizing.getMaximumWidth());
        target.minimumHeight(customSizing.getMinimumHeight());
        target.maximumHeight(customSizing.getMaximumHeight());
        return n;
    }

    // Combinatorial source: all justify x align x sizing pairs
    protected static Stream<Arguments> comboSource() {
        return justifyModes().flatMap(j ->
            alignModes().flatMap(a ->
                Stream.of(NodeSpec.SizeMode.values())
                    .flatMap(smA ->
                        Stream.of(NodeSpec.SizeMode.values())
                            .map(smB -> Arguments.of(j.get()[0], a.get()[0], smA, smB))
                    )
            )
        );
    }

    // Common parameter sources for parameterized tests
    protected static Stream<Arguments> justifyModes() {
        return Stream.of(
            Arguments.of(LayoutSpec.JustifyContent.START),
            Arguments.of(LayoutSpec.JustifyContent.CENTER),
            Arguments.of(LayoutSpec.JustifyContent.END),
            Arguments.of(LayoutSpec.JustifyContent.SPACE_BETWEEN),
            Arguments.of(LayoutSpec.JustifyContent.SPACE_AROUND),
            Arguments.of(LayoutSpec.JustifyContent.SPACE_EVENLY)
        );
    }

    protected static Stream<Arguments> alignModes() {
        return Stream.of(
            Arguments.of(LayoutSpec.AlignItems.START),
            Arguments.of(LayoutSpec.AlignItems.CENTER),
            Arguments.of(LayoutSpec.AlignItems.END),
            Arguments.of(LayoutSpec.AlignItems.STRETCH)
        );
    }

    protected static Stream<Arguments> alignSelfModes() {
        return Stream.of(
            Arguments.of(NodeSpec.AlignSelf.AUTO),
            Arguments.of(NodeSpec.AlignSelf.START),
            Arguments.of(NodeSpec.AlignSelf.CENTER),
            Arguments.of(NodeSpec.AlignSelf.END),
            Arguments.of(NodeSpec.AlignSelf.STRETCH)
        );
    }
}
