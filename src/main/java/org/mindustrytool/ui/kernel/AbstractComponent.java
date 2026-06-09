package org.mindustrytool.ui.kernel;

import arc.struct.Seq;

import org.mindustrytool.ui.components.Component;
import org.mindustrytool.ui.layout.Sizing;
import org.mindustrytool.ui.layout.NodeSizing;

public abstract class AbstractComponent implements Component {
    protected final NodeSizing sizing = new NodeSizing();
    protected final Seq<Runnable> subscriptions = new Seq<>();

    @Override
    public Sizing sizing() { return sizing; }

    @Override
    public void dispose() {
        subscriptions.each(Runnable::run);
        subscriptions.clear();
    }
}