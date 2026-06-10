package org.mindustrytool.libs.ui.component;

import arc.struct.Seq;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.ui.layout.NodeSpec;

/**
 * Skeletal implementation of {@link Component} that manages a shared
 * {@link NodeSpec} and a list of active {@link Effect} subscriptions.
 * <p>
 * Concrete components extend this class to reuse the sizing and lifecycle
 * machinery. Subclasses should register any {@code Effect} instances they
 * create in {@link #subscriptions} so they are automatically disposed when
 * the component is removed from the scene graph.
 * <p>
 * Example:
 * <pre>{@code
 * public class MyComponent extends AbstractComponent {
 *     public MyComponent() {
 *         var e = new Effect(() -> { ... });
 *         subscriptions.add(e);
 *     }
 *
 *     public Element element() { return myElement; }
 * }
 * }</pre>
 */
public abstract class AbstractComponent implements Component {
    protected final NodeSpec sizing = new NodeSpec();
    protected final Seq<Effect> subscriptions = new Seq<>();

    @Override
    public NodeSpec sizing() {
        return sizing;
    }

    @Override
    public void dispose() {
        subscriptions.each(Effect::dispose);
        subscriptions.clear();
    }
}
