package org.mindustrytool.libs.ui.layout;

import arc.scene.Element;

import org.mindustrytool.libs.ui.components.Component;
import org.mindustrytool.libs.ui.layout.NodeSpec.SizeMode;
import org.mindustrytool.libs.ui.layout.LayoutSpec.AlignItems;
import org.mindustrytool.libs.ui.layout.LayoutSpec.JustifyContent;

/**
 * LayoutEngine is the core layout calculation processor.
 * It distributes positions and dimensions to child nodes based on flexbox rules.
 *
 * <p>=== ARCHITECTURAL RULES & CHANGELOG ===</p>
 *
 * <p>Rules for Modification:</p>
 * <ul>
 *   <li><b>1. Zero Allocation Principle:</b> To maintain smooth UI rendering in Mindustry,
 *       avoid unnecessary object creation in performance-sensitive layout loops. The current wrapping algorithm
 *       allocates temporary lines; if GC pressure becomes an issue, migrate to object pools or static buffers.</li>
 *   <li><b>2. Single-Threaded Constraints:</b> This layout engine is designed for single-threaded UI execution loops.
 *       Do not add volatile qualifiers, synchronized locks, or ThreadLocal storage, as they degrade performance.</li>
 *   <li><b>3. Backward Compatibility:</b> Public API overloads targeting `arc.scene.Element` must remain unchanged
 *       and delegate directly to `ELEMENT_ACCESSOR`.</li>
 *   <li><b>4. Axis Orientation Math:</b> In Arc UI, the coordinates start at the bottom-left corner.
 *       Therefore, along the vertical axis (Y), coordinate values increase upwards.
 *       - Cursors moving down the vertical axis must subtract coordinate values.
 *       - AlignItems.START aligns to the bottom of the line (minimum coordinate).
 *       - AlignItems.END aligns to the top of the line (maximum coordinate).</li>
 * </ul>
 *
 * <p>Changelog:</p>
 * <ul>
 *   <li><b>2026-06-09:</b> Replaced the old Sizing interface with direct NodeSpec references.
 *       Renamed all abbreviated variables and methods to full semantic terms (e.g. x -> xPosition, h -> height).</li>
 *   <li><b>2026-06-09:</b> Implemented wrapping layout logic (FlexWrap), individual cross alignment overrides (AlignSelf),
 *       reverse layout direction (Reverse), and Space Evenly alignment.</li>
 *   <li><b>2026-06-09:</b> Decoupled layout math from the Arc UI framework using LayoutAccessor to allow clean unit testing.</li>
 *   <li><b>2026-06-09:</b> Overrode setScene in Layout's WidgetGroup to intercept detachment from Scene and force component
 *   cleanup/disposal.</li>
 * </ul>
 */
public class LayoutEngine {

    /**
     * Default LayoutAccessor implementation for the standard Arc UI Scene Graph Element.
     */
    public static final LayoutAccessor<Element> ELEMENT_ACCESSOR = new LayoutAccessor<>() {
        @Override
        public boolean isVisible(Element node) {
            return node.visible;
        }

        @Override
        public float getPreferredWidth(Element node) {
            return node.getPrefWidth();
        }

        @Override
        public float getPreferredHeight(Element node) {
            return node.getPrefHeight();
        }

        @Override
        public void setBounds(Element node, float xPosition, float yPosition, float width, float height) {
            node.setBounds(xPosition, yPosition, width, height);
        }

        @Override
        public NodeSpec getSizing(Element node) {
            return LayoutEngine.sizingOf(node);
        }
    };

    /**
     * Represents abstract operations specific to layout axis dimensions.
     */
    private interface Axis {
        <T> float getPreferred(T node, LayoutAccessor<T> accessor);

        float getFixed(NodeSpec sizing);

        SizeMode getMode(NodeSpec sizing);

        float getGrowWeight(NodeSpec sizing);

        float getPadding(NodeSpec sizing);

        float constrain(NodeSpec sizing, float value);
    }

    private static final Axis AXIS_X = new Axis() {
        @Override
        public <T> float getPreferred(T node, LayoutAccessor<T> accessor) {
            return accessor.getPreferredWidth(node);
        }

        @Override
        public float getFixed(NodeSpec s) {
            return s.getFixedWidth();
        }

        @Override
        public SizeMode getMode(NodeSpec s) {
            return s.getWidthMode();
        }

        @Override
        public float getGrowWeight(NodeSpec s) {
            return s.getGrowWeightHorizontal();
        }

        @Override
        public float getPadding(NodeSpec s) {
            return s.getHorizontalPadding();
        }

        @Override
        public float constrain(NodeSpec s, float value) {
            return s.constrainWidth(value);
        }
    };

    private static final Axis AXIS_Y = new Axis() {
        @Override
        public <T> float getPreferred(T node, LayoutAccessor<T> accessor) {
            return accessor.getPreferredHeight(node);
        }

        @Override
        public float getFixed(NodeSpec s) {
            return s.getFixedHeight();
        }

        @Override
        public SizeMode getMode(NodeSpec s) {
            return s.getHeightMode();
        }

        @Override
        public float getGrowWeight(NodeSpec s) {
            return s.getGrowWeightVertical();
        }

        @Override
        public float getPadding(NodeSpec s) {
            return s.getVerticalPadding();
        }

        @Override
        public float constrain(NodeSpec s, float value) {
            return s.constrainHeight(value);
        }
    };

    // --- Backward Compatible Overloads for Element ---

    /** Computes preferred width using the default {@link #ELEMENT_ACCESSOR}. */
    public static float prefWidth(NodeSpec spec, boolean isColumn, float gap, Iterable<Element> children) {
        return prefWidth(spec, isColumn, gap, children, ELEMENT_ACCESSOR);
    }

    /** Computes preferred height using the default {@link #ELEMENT_ACCESSOR}. */
    public static float prefHeight(NodeSpec spec, boolean isColumn, float gap, Iterable<Element> children) {
        return prefHeight(spec, isColumn, gap, children, ELEMENT_ACCESSOR);
    }

    /** Lays out children using the default {@link #ELEMENT_ACCESSOR}. */
    public static void layout(LayoutSpec spec, Iterable<Element> children, float xPosition, float yPosition, float width, float height) {
        layout(spec, children, xPosition, yPosition, width, height, ELEMENT_ACCESSOR);
    }

    // --- Core Generalized Layout Algorithms ---

    /** Computes the preferred width for the given children with a custom accessor. */
    public static <T> float prefWidth(NodeSpec spec, boolean isColumn, float gap, Iterable<T> children, LayoutAccessor<T> accessor) {
        return preferredAxis(spec, isColumn, AXIS_X, gap, children, accessor);
    }

    /** Computes the preferred height for the given children with a custom accessor. */
    public static <T> float prefHeight(NodeSpec spec, boolean isColumn, float gap, Iterable<T> children, LayoutAccessor<T> accessor) {
        return preferredAxis(spec, isColumn, AXIS_Y, gap, children, accessor);
    }

    private static <T> float preferredAxis(NodeSpec spec,
                                           boolean isColumn,
                                           Axis axis,
                                           float gapSpacing,
                                           Iterable<T> children,
                                           LayoutAccessor<T> accessor) {
        if (axis.getMode(spec) == SizeMode.FIXED) return axis.constrain(spec, axis.getFixed(spec));

        float totalSize = axis.getPadding(spec);
        float maximumChildSize = 0.0f;
        int childCount = 0;
        boolean isMainAxis = (isColumn == (axis == AXIS_Y));

        for (T childNode : children) {
            if (!accessor.isVisible(childNode)) continue;
            NodeSpec childSizing = accessor.getSizing(childNode);

            float childValue = childSizing == null
                ? axis.getPreferred(childNode, accessor)
                : axis.constrain(childSizing, (axis.getMode(childSizing) == SizeMode.FIXED) ? axis.getFixed(childSizing) : axis.getPreferred(childNode, accessor));

            if (isMainAxis) totalSize += childValue;
            else maximumChildSize = Math.max(maximumChildSize, childValue);
            childCount++;
        }

        if (!isMainAxis) totalSize += maximumChildSize;
        if (childCount > 1) totalSize += gapSpacing * (childCount - 1);
        return axis.constrain(spec, totalSize);
    }

    /**
     * Internal layout item tracking calculated sizes and positions.
     */
    private static class LayoutItem<T> {
        final T node;
        float mainSize = 0.0f;
        float crossSize = 0.0f;
        float mainPosition = 0.0f;
        float crossPosition = 0.0f;

        LayoutItem(T node) {
            this.node = node;
        }
    }

    /**
     * Internal layout line representing a horizontal row or vertical column containing grouped elements.
     */
    private static class LayoutLine<T> {
        final java.util.List<LayoutItem<T>> items = new java.util.ArrayList<>();
        float mainSize = 0.0f;
        float crossSize = 0.0f;
        float crossPosition = 0.0f;
        int growCount = 0;
        float totalGrowWeight = 0.0f;
    }

    /**
     * Main layout entry point. Distributes children within the given bounds
     * according to flexbox rules (direction, wrapping, justification, alignment).
     *
     * @param spec      the layout specification
     * @param children  the child nodes to lay out
     * @param xPosition the container's left edge
     * @param yPosition the container's bottom edge (Arc coordinate system)
     * @param width     the available width
     * @param height    the available height
     * @param accessor  abstraction for accessing layout properties of each child
     */
    public static <T> void layout(LayoutSpec spec,
                                  Iterable<T> children,
                                  float xPosition,
                                  float yPosition,
                                  float width,
                                  float height,
                                  LayoutAccessor<T> accessor) {
        float gapSpacing = spec.getGap();
        boolean isColumn = spec.isColumn();
        Axis mainAxis = isColumn ? AXIS_Y : AXIS_X;
        Axis crossAxis = isColumn ? AXIS_X : AXIS_Y;
        float mainLimit = isColumn ? height : width;

        // Step 1: Partition child nodes into lines if wrapping is enabled
        java.util.List<LayoutLine<T>> layoutLines = new java.util.ArrayList<>();
        LayoutLine<T> currentLayoutLine = new LayoutLine<>();
        layoutLines.add(currentLayoutLine);

        for (T childNode : children) {
            if (!accessor.isVisible(childNode)) continue;
            NodeSpec childSizing = accessor.getSizing(childNode);

            float mainSize = childSizing == null
                ? mainAxis.getPreferred(childNode, accessor)
                : (mainAxis.getMode(childSizing) == SizeMode.GROW ? 0.0f : getChildSizeOnAxis(childNode, childSizing, mainAxis, accessor));

            float crossSize = childSizing == null
                ? crossAxis.getPreferred(childNode, accessor)
                : getChildSizeOnAxis(childNode, childSizing, crossAxis, accessor);

            // Create new line if wrapping is enabled and current line overflows
            if (spec.isWrap() && !currentLayoutLine.items.isEmpty() && currentLayoutLine.mainSize + gapSpacing + mainSize > mainLimit)
                layoutLines.add(currentLayoutLine = new LayoutLine<>());

            LayoutItem<T> item = new LayoutItem<>(childNode);
            item.mainSize = mainSize;
            item.crossSize = crossSize;
            currentLayoutLine.items.add(item);

            if (currentLayoutLine.items.size() > 1) currentLayoutLine.mainSize += gapSpacing;
            currentLayoutLine.mainSize += mainSize;

            if (childSizing != null && mainAxis.getMode(childSizing) == SizeMode.GROW) {
                currentLayoutLine.growCount++;
                currentLayoutLine.totalGrowWeight += mainAxis.getGrowWeight(childSizing);
            }
        }

        layoutLines.removeIf(line -> line.items.isEmpty());
        if (layoutLines.isEmpty()) return;

        // Step 2: Distribute extra main axis space to GROW elements and determine crossSize of each line
        for (LayoutLine<T> line : layoutLines) {
            float extraMain = mainLimit - line.mainSize;
            if (line.growCount > 0 && extraMain > 0.0f) {
                for (LayoutItem<T> item : line.items) {
                    NodeSpec childSizing = accessor.getSizing(item.node);
                    if (childSizing != null && mainAxis.getMode(childSizing) == SizeMode.GROW)
                        item.mainSize = (line.totalGrowWeight > 0.0f ? (mainAxis.getGrowWeight(childSizing) / line.totalGrowWeight) : (1.0f / line.growCount)) * extraMain;
                }
                line.mainSize = mainLimit;
            }

            float maximumCross = 0.0f;
            for (LayoutItem<T> item : line.items) maximumCross = Math.max(maximumCross, item.crossSize);
            line.crossSize = maximumCross;
        }

        // If not wrapping or only one line exists, let the single line occupy the full cross container space
        if (!spec.isWrap() || layoutLines.size() == 1)
            layoutLines.get(0).crossSize = isColumn ? width : height;

        // Step 3: Stack layout lines sequentially along the cross axis
        float currentCrossPosition = isColumn ? xPosition : yPosition + height;
        for (LayoutLine<T> line : layoutLines) {
            if (isColumn) {
                line.crossPosition = currentCrossPosition;
                currentCrossPosition += line.crossSize + gapSpacing;
            } else {
                line.crossPosition = currentCrossPosition - line.crossSize;
                currentCrossPosition -= line.crossSize + gapSpacing;
            }
        }

        // Step 4: Justify elements on the main axis and set final positions
        boolean isForwardDirection = isColumn == spec.isReverse();

        for (LayoutLine<T> line : layoutLines) {
            float extraMainSpace = mainLimit - line.mainSize;
            float[] offsets = computeJustifyOffsets(extraMainSpace, line.items.size(), gapSpacing, spec.getJustifyContent());

            float cursorPosition = isForwardDirection
                ? (isColumn ? yPosition : xPosition) + offsets[0]
                : (isColumn ? yPosition + height : xPosition + width) - offsets[0];

            int index = 0;
            for (LayoutItem<T> item : line.items) {
                NodeSpec childSizing = accessor.getSizing(item.node);
                AlignItems childAlignment = getChildAlignment(childSizing, spec.getAlignItems());

                if (childAlignment == AlignItems.STRETCH) item.crossSize = line.crossSize;

                // Determine local cross offset inside the layout line space
                item.crossPosition = line.crossPosition + calculateItemCrossPositionOffset(0.0f,
                    line.crossSize,
                    item.crossSize,
                    childAlignment);

                if (isForwardDirection) {
                    item.mainPosition = cursorPosition;
                    cursorPosition += item.mainSize + offsets[index + 1];
                } else {
                    item.mainPosition = cursorPosition - item.mainSize;
                    cursorPosition -= item.mainSize + offsets[index + 1];
                }

                if (isColumn) accessor.setBounds(item.node, item.crossPosition, item.mainPosition, item.crossSize, item.mainSize);
                else accessor.setBounds(item.node, item.mainPosition, item.crossPosition, item.mainSize, item.crossSize);
                index++;
            }
        }
    }

    private static <T> float getChildSizeOnAxis(T node, NodeSpec sizing, Axis axis, LayoutAccessor<T> accessor) {
        float fixedValue = (axis.getMode(sizing) == SizeMode.FIXED) ? axis.getFixed(sizing) : axis.getPreferred(node, accessor);
        return axis.constrain(sizing, fixedValue);
    }

    private static AlignItems getChildAlignment(NodeSpec sizing, AlignItems fallback) {
        if (sizing == null) return fallback;
        return switch (sizing.getAlignSelf()) {
            case START -> AlignItems.START;
            case CENTER -> AlignItems.CENTER;
            case END -> AlignItems.END;
            case STRETCH -> AlignItems.STRETCH;
            default -> fallback;
        };
    }

    private static float[] computeJustifyOffsets(float extraSpace, int count, float gapSpacing, JustifyContent justifyContent) {
        float[] offsets = new float[count + 1];
        switch (justifyContent) {
            case END -> {
                offsets[0] = extraSpace;
                for (int i = 1; i <= count; i++) offsets[i] = gapSpacing;
            }
            case CENTER -> {
                float half = extraSpace / 2.0f;
                offsets[0] = half;
                for (int i = 1; i <= count; i++) offsets[i] = gapSpacing;
            }
            case SPACE_BETWEEN -> {
                float between = (count > 1) ? extraSpace / (count - 1) : 0.0f;
                offsets[0] = 0.0f;
                for (int i = 1; i <= count; i++) offsets[i] = gapSpacing + between;
            }
            case SPACE_AROUND -> {
                float around = extraSpace / count;
                offsets[0] = around;
                for (int i = 1; i <= count; i++) offsets[i] = gapSpacing + around * 2.0f;
            }
            case SPACE_EVENLY -> {
                float evenly = extraSpace / (count + 1);
                offsets[0] = evenly;
                for (int i = 1; i <= count; i++) offsets[i] = gapSpacing + evenly;
            }
            default -> {
                offsets[0] = 0.0f;
                for (int i = 1; i <= count; i++) offsets[i] = gapSpacing;
            }
        }
        return offsets;
    }

    private static float calculateItemCrossPositionOffset(float start, float contentSize, float itemSize, AlignItems alignment) {
        return switch (alignment) {
            case CENTER -> start + (contentSize - itemSize) / 2.0f;
            case END -> start + (contentSize - itemSize);
            default -> start;
        };
    }

    /**
     * Extracts the {@link NodeSpec} from an element's user object if it implements {@link Component}.
     *
     * @param element the element to inspect
     * @return the associated NodeSpec, or null if the element has no component
     */
    public static NodeSpec sizingOf(Element element) {
        Object object = element.userObject;
        return object instanceof Component ? ((Component) object).sizing() : null;
    }
}
