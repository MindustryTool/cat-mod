package org.mindustrytool.libs.ui.widget;

import arc.scene.Element;

/**
 * A widget that does not maintain its own state and is composed of other widgets.
 * <p>
 * This mirrors Flutter's {@code StatelessWidget} and enables building reusable
 * custom UI components via composition rather than writing custom {@link ElementNode} classes.
 */
public abstract class StatelessWidget implements Widget {

    /**
     * Builds the widget tree representing this widget.
     *
     * @return the child widget tree
     */
    public abstract Widget build();

    @Override
    public ElementNode createElement() {
        return new StatelessElementNode(this);
    }
}

class StatelessElementNode extends ElementNode {
    private ElementNode childNode;

    StatelessElementNode(StatelessWidget widget) {
        super(widget);
    }

    @Override
    public void mount(ElementNode parent) {
        Widget childWidget = ((StatelessWidget) widget).build();
        childNode = childWidget.createElement();
        childNode.mount(this);
        this.arcElement = childNode.getArcElement();
    }

    @Override
    public void update(Widget newWidget) {
        super.update(newWidget);
        Widget childWidget = ((StatelessWidget) newWidget).build();
        if (childNode.getWidget().canUpdate(childWidget)) {
            childNode.update(childWidget);
        } else {
            childNode.dispose();
            childNode = childWidget.createElement();
            childNode.mount(this);
            this.arcElement = childNode.getArcElement();
        }
    }

    @Override
    public void dispose() {
        if (childNode != null) {
            childNode.dispose();
            childNode = null;
        }
        super.dispose();
    }
}

