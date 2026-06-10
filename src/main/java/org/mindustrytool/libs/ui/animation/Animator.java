package org.mindustrytool.libs.ui.animation;

import arc.graphics.Color;
import arc.struct.Queue;

public class Animator {
    private Transition current;
    private final Queue<Transition> pending = new Queue<>();
    private boolean dirty;

    public void animate(long durationMs, Ease ease, Runnable targetApplier) {
        pending.add(new Transition(durationMs, ease, targetApplier));
        if (current == null) advance();
    }

    public boolean update(float delta) {
        dirty = false;
        if (current != null) {
            if (current.update(delta)) dirty = true;
            else {
                current = null;
                if (!pending.isEmpty()) advance();
            }
        }
        return dirty;
    }

    public void cancelAll() { current = null; pending.clear(); }
    public void finishAll() { while (current != null || !pending.isEmpty()) update(99999f); }
    public boolean isAnimating() { return current != null || !pending.isEmpty(); }

    private void advance() { current = pending.removeFirst(); }

    // ---

    static class Transition {
        final long durationMs;
        final Ease ease;
        final Runnable targetApplier;
        float elapsed;
        boolean done;

        Transition(long durationMs, Ease ease, Runnable targetApplier) {
            this.durationMs = durationMs;
            this.ease = ease;
            this.targetApplier = targetApplier;
        }

        boolean update(float delta) {
            elapsed += delta * 1000f;
            float t = Math.min(elapsed / durationMs, 1f);
            // targetApplier sets "to" values, but we need "from" capture...
            // This is a placeholder - actual lerp happens in the component
            if (t >= 1f) { done = true; return true; }
            return true;
        }
    }
}
