package org.mindustrytool.libs.ui.component;

import arc.struct.Seq;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.ui.layout.NodeSizing;

public abstract class AbstractComponent implements Component {
    protected final NodeSizing sizing = new NodeSizing();
    protected final Seq<Effect> subscriptions = new Seq<>();

    @Override
    public NodeSizing sizing() {
        return sizing;
    }

    @Override
    public void dispose() {
        subscriptions.each(Effect::dispose);
        subscriptions.clear();
    }
}
