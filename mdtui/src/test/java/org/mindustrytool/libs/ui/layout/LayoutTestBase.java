package org.mindustrytool.libs.ui.layout;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

public abstract class LayoutTestBase {

    protected static final float EPS = 0.001f;

    protected static class MockNode {
        final String name;
        LayoutSpec sizing = LayoutSpec.defaultSpec();
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

        LayoutSpec getSizingObj() { return sizing; }

        MockNode minimumWidth(float w) { sizing = sizing.toBuilder().minimumWidth(w).build(); return this; }
        MockNode maximumWidth(float w) { sizing = sizing.toBuilder().maximumWidth(w).build(); return this; }
        MockNode minimumHeight(float h) { sizing = sizing.toBuilder().minimumHeight(h).build(); return this; }
        MockNode maximumHeight(float h) { sizing = sizing.toBuilder().maximumHeight(h).build(); return this; }
        MockNode padding(float p) { sizing = sizing.toBuilder().padding(p).build(); return this; }
        MockNode padding(float top, float right, float bottom, float left) { sizing = sizing.toBuilder().padding(top, right, bottom, left).build(); return this; }
        MockNode growWeightHorizontal(float w) { sizing = sizing.toBuilder().growWeightHorizontal(w).build(); return this; }
        MockNode growWeightVertical(float w) { sizing = sizing.toBuilder().growWeightVertical(w).build(); return this; }
        MockNode growX() { sizing = sizing.toBuilder().growX().build(); return this; }
        MockNode growY() { sizing = sizing.toBuilder().growY().build(); return this; }
        MockNode grow() { sizing = sizing.toBuilder().grow().build(); return this; }
        MockNode alignSelf(LayoutSpec.AlignSelf a) { sizing = sizing.toBuilder().alignSelf(a).build(); return this; }

        void set(String key, Object value) {
            switch (key) {
                case "prefW": preferredWidth = (Float) value; break;
                case "prefH": preferredHeight = (Float) value; break;
                case "visible": visible = (Boolean) value; break;
                case "minW": sizing = sizing.toBuilder().minimumWidth((Float) value).build(); break;
                case "maxW": sizing = sizing.toBuilder().maximumWidth((Float) value).build(); break;
                case "minH": sizing = sizing.toBuilder().minimumHeight((Float) value).build(); break;
                case "maxH": sizing = sizing.toBuilder().maximumHeight((Float) value).build(); break;
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
            c.sizing = sizing;
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
        @Override public LayoutSpec getSizing(MockNode node) { return node.returnNullSizing ? null : node.sizing; }
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
        return LayoutEngine.prefWidth(spec, spec.isColumn(), spec.gap(), Arrays.asList(children), ACCESSOR);
    }

    protected static float prefH(LayoutSpec spec, MockNode... children) {
        return LayoutEngine.prefHeight(spec, spec.isColumn(), spec.gap(), Arrays.asList(children), ACCESSOR);
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
            if (visibleCount > 1) totalMain += spec.gap() * (visibleCount - 1);
            final float totalMainFinal = totalMain;
            org.junit.jupiter.api.Assertions.assertTrue(totalMainFinal <= mainLimit + EPS,
                () -> "Total main size " + totalMainFinal + " > limit " + mainLimit);
        }

        assertNoOverlap(children, isColumn);
    }

    // Convenience builders
    protected static LayoutSpec row() { return LayoutSpec.builder().isColumn(false).alignItems(LayoutSpec.AlignItems.START).build(); }
    protected static LayoutSpec row(LayoutSpec.AlignItems ai) { return LayoutSpec.builder().isColumn(false).alignItems(ai).build(); }
    protected static LayoutSpec row(LayoutSpec.AlignItems ai, LayoutSpec.JustifyContent jc) {
        return LayoutSpec.builder().isColumn(false).alignItems(ai).justifyContent(jc).build();
    }
    protected static LayoutSpec column() { return LayoutSpec.builder().isColumn(true).alignItems(LayoutSpec.AlignItems.START).build(); }
    protected static LayoutSpec column(LayoutSpec.AlignItems ai) { return LayoutSpec.builder().isColumn(true).alignItems(ai).build(); }
    protected static LayoutSpec column(LayoutSpec.AlignItems ai, LayoutSpec.JustifyContent jc) {
        return LayoutSpec.builder().isColumn(true).alignItems(ai).justifyContent(jc).build();
    }

    protected static LayoutSpec sizing(LayoutSpec.SizeMode mode, float pw, float ph) {
        var builder = LayoutSpec.builder().fixedWidth(pw).fixedHeight(ph);
        switch (mode) {
            case FIXED:
                builder.widthMode(LayoutSpec.SizeMode.FIXED).heightMode(LayoutSpec.SizeMode.FIXED);
                break;
            case GROW:
                builder.widthMode(LayoutSpec.SizeMode.GROW).heightMode(LayoutSpec.SizeMode.GROW);
                break;
            default:
                builder.widthMode(LayoutSpec.SizeMode.WRAP).heightMode(LayoutSpec.SizeMode.WRAP);
                break;
        }
        return builder.build();
    }

    protected static MockNode nodeWithSizing(String name, LayoutSpec customSizing) {
        MockNode n = new MockNode(name, customSizing.fixedWidth(), customSizing.fixedHeight());
        n.sizing = customSizing;
        return n;
    }

    // Combinatorial source: all justify x align x sizing pairs
    protected static Stream<Arguments> comboSource() {
        return justifyModes().flatMap(j ->
            alignModes().flatMap(a ->
                Stream.of(LayoutSpec.SizeMode.values())
                    .flatMap(smA ->
                        Stream.of(LayoutSpec.SizeMode.values())
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
            Arguments.of(LayoutSpec.AlignSelf.AUTO),
            Arguments.of(LayoutSpec.AlignSelf.START),
            Arguments.of(LayoutSpec.AlignSelf.CENTER),
            Arguments.of(LayoutSpec.AlignSelf.END),
            Arguments.of(LayoutSpec.AlignSelf.STRETCH)
        );
    }
}
