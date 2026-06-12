package org.mindustrytool.libs.ui.layout;

import arc.scene.Element;

import org.mindustrytool.libs.ui.widget.ElementNode;
import org.mindustrytool.libs.ui.layout.LayoutSpec.SizeMode;
import org.mindustrytool.libs.ui.layout.LayoutSpec.AlignItems;
import org.mindustrytool.libs.ui.layout.LayoutSpec.JustifyContent;

/**
 * LayoutEngine is the core layout calculation processor.
 * It distributes positions and dimensions to child nodes based on flexbox rules.
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
        public LayoutSpec getSizing(Element node) {
            return LayoutEngine.sizingOf(node);
        }
    };

    /**
     * Represents abstract operations specific to layout axis dimensions.
     */
    private interface Axis {
        <T> float getPreferred(T node, LayoutAccessor<T> accessor);

        float getFixed(LayoutSpec sizing);

        SizeMode getMode(LayoutSpec sizing);

        float getGrowWeight(LayoutSpec sizing);

        float getPadding(LayoutSpec sizing);

        float constrain(LayoutSpec sizing, float value);
    }

    private static final Axis AXIS_X = new Axis() {
        @Override
        public <T> float getPreferred(T node, LayoutAccessor<T> accessor) {
            return accessor.getPreferredWidth(node);
        }

        @Override
        public float getFixed(LayoutSpec s) {
            return s.fixedWidth();
        }

        @Override
        public SizeMode getMode(LayoutSpec s) {
            return s.widthMode();
        }

        @Override
        public float getGrowWeight(LayoutSpec s) {
            return s.growWeightHorizontal();
        }

        @Override
        public float getPadding(LayoutSpec s) {
            return s.getHorizontalPadding();
        }

        @Override
        public float constrain(LayoutSpec s, float value) {
            return s.constrainWidth(value);
        }
    };

    private static final Axis AXIS_Y = new Axis() {
        @Override
        public <T> float getPreferred(T node, LayoutAccessor<T> accessor) {
            return accessor.getPreferredHeight(node);
        }

        @Override
        public float getFixed(LayoutSpec s) {
            return s.fixedHeight();
        }

        @Override
        public SizeMode getMode(LayoutSpec s) {
            return s.heightMode();
        }

        @Override
        public float getGrowWeight(LayoutSpec s) {
            return s.growWeightVertical();
        }

        @Override
        public float getPadding(LayoutSpec s) {
            return s.getVerticalPadding();
        }

        @Override
        public float constrain(LayoutSpec s, float value) {
            return s.constrainHeight(value);
        }
    };

    // --- Backward Compatible Overloads for Element ---

    /** Computes preferred width using the default {@link #ELEMENT_ACCESSOR}. */
    public static float prefWidth(LayoutSpec spec, boolean isColumn, float gap, Iterable<Element> children) {
        return prefWidth(spec, isColumn, gap, children, ELEMENT_ACCESSOR);
    }

    /** Computes preferred height using the default {@link #ELEMENT_ACCESSOR}. */
    public static float prefHeight(LayoutSpec spec, boolean isColumn, float gap, Iterable<Element> children) {
        return prefHeight(spec, isColumn, gap, children, ELEMENT_ACCESSOR);
    }

    /** Lays out children using the default {@link #ELEMENT_ACCESSOR}. */
    public static void layout(LayoutSpec spec, Iterable<Element> children, float xPosition, float yPosition, float width, float height) {
        layout(spec, children, xPosition, yPosition, width, height, ELEMENT_ACCESSOR);
    }

    // --- Core Generalized Layout Algorithms ---

    /** Computes the preferred width for the given children with a custom accessor. */
    public static <T> float prefWidth(LayoutSpec spec, boolean isColumn, float gap, Iterable<T> children, LayoutAccessor<T> accessor) {
        return preferredAxis(spec, isColumn, AXIS_X, gap, children, accessor);
    }

    /** Computes the preferred height for the given children with a custom accessor. */
    public static <T> float prefHeight(LayoutSpec spec, boolean isColumn, float gap, Iterable<T> children, LayoutAccessor<T> accessor) {
        return preferredAxis(spec, isColumn, AXIS_Y, gap, children, accessor);
    }

    private static <T> float preferredAxis(LayoutSpec spec,
                                           boolean isColumn,
                                           Axis axis,
                                           float gapSpacing,
                                           Iterable<T> children,
                                           LayoutAccessor<T> accessor) {
        if (axis.getMode(spec) == SizeMode.FIXED) {
            return axis.constrain(spec, axis.getFixed(spec));
        }

        float totalSize = axis.getPadding(spec);
        float maximumChildSize = 0.0f;
        int childCount = 0;
        boolean isMainAxis = (isColumn == (axis == AXIS_Y));

        for (T childNode : children) {
            if (!accessor.isVisible(childNode)) {
                continue;
            }
            LayoutSpec childSizing = accessor.getSizing(childNode);

            float childValue = childSizing == null
                ? axis.getPreferred(childNode, accessor)
                : axis.constrain(childSizing, (axis.getMode(childSizing) == SizeMode.FIXED) ? axis.getFixed(childSizing) : axis.getPreferred(childNode, accessor));

            if (isMainAxis) {
                totalSize += childValue;
            } else {
                maximumChildSize = Math.max(maximumChildSize, childValue);
            }
            childCount++;
        }

        if (!isMainAxis) {
            totalSize += maximumChildSize;
        }
        if (childCount > 1) {
            totalSize += gapSpacing * (childCount - 1);
        }
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
        float gapSpacing = spec.gap();
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
            LayoutSpec childSizing = accessor.getSizing(childNode);

            float mainSize = childSizing == null
                ? mainAxis.getPreferred(childNode, accessor)
                : (mainAxis.getMode(childSizing) == SizeMode.GROW ? 0.0f : getChildSizeOnAxis(childNode, childSizing, mainAxis, accessor));

            float crossSize = childSizing == null
                ? crossAxis.getPreferred(childNode, accessor)
                : getChildSizeOnAxis(childNode, childSizing, crossAxis, accessor);

            // Create new line if wrapping is enabled and current line overflows
            if (spec.isWrap() && !currentLayoutLine.items.isEmpty() && currentLayoutLine.mainSize + gapSpacing + mainSize > mainLimit) {
                layoutLines.add(currentLayoutLine = new LayoutLine<>());
            }

            LayoutItem<T> item = new LayoutItem<>(childNode);
            item.mainSize = mainSize;
            item.crossSize = crossSize;
            currentLayoutLine.items.add(item);

            if (currentLayoutLine.items.size() > 1) {
                currentLayoutLine.mainSize += gapSpacing;
            }
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
                    LayoutSpec childSizing = accessor.getSizing(item.node);
                    if (childSizing != null && mainAxis.getMode(childSizing) == SizeMode.GROW) {
                        item.mainSize = (line.totalGrowWeight > 0.0f ? (mainAxis.getGrowWeight(childSizing) / line.totalGrowWeight) : (1.0f / line.growCount)) * extraMain;
                    }
                }
                line.mainSize = mainLimit;
            }

            float maximumCross = 0.0f;
            for (LayoutItem<T> item : line.items) maximumCross = Math.max(maximumCross, item.crossSize);
            line.crossSize = maximumCross;
        }

        // If not wrapping or only one line exists, let the single line occupy the full cross container space
        if (!spec.isWrap() || layoutLines.size() == 1) {
            layoutLines.get(0).crossSize = isColumn ? width : height;
        }

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
            float[] offsets = computeJustifyOffsets(extraMainSpace, line.items.size(), gapSpacing, spec.justifyContent());

            float cursorPosition = isForwardDirection
                ? (isColumn ? yPosition : xPosition) + offsets[0]
                : (isColumn ? yPosition + height : xPosition + width) - offsets[0];

            int index = 0;
            for (LayoutItem<T> item : line.items) {
                LayoutSpec childSizing = accessor.getSizing(item.node);
                AlignItems childAlignment = getChildAlignment(childSizing, spec.alignItems());

                if (childAlignment == AlignItems.STRETCH) {
                    item.crossSize = line.crossSize;
                }

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

                if (isColumn) {
                    accessor.setBounds(item.node, item.crossPosition, item.mainPosition, item.crossSize, item.mainSize);
                } else {
                    accessor.setBounds(item.node, item.mainPosition, item.crossPosition, item.mainSize, item.crossSize);
                }
                index++;
            }
        }
    }

    private static <T> float getChildSizeOnAxis(T node, LayoutSpec sizing, Axis axis, LayoutAccessor<T> accessor) {
        float fixedValue = (axis.getMode(sizing) == SizeMode.FIXED) ? axis.getFixed(sizing) : axis.getPreferred(node, accessor);
        return axis.constrain(sizing, fixedValue);
    }

    private static AlignItems getChildAlignment(LayoutSpec sizing, AlignItems fallback) {
        if (sizing == null) return fallback;
        return switch (sizing.alignSelf()) {
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
                float halfAround = extraSpace / (count * 2);
                offsets[0] = halfAround;
                for (int i = 1; i < count; i++) offsets[i] = gapSpacing + halfAround * 2;
                if (count > 0) offsets[count] = gapSpacing + halfAround;
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

    public static LayoutSpec sizingOf(Element element) {
        Object object = element.userObject;
        if (object instanceof ElementNode) {
            return ((ElementNode) object).sizing();
        }
        return null;
    }
}
