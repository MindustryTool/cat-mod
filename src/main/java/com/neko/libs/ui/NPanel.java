package com.neko.libs.ui;

import arc.input.KeyCode;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import com.neko.libs.ui.layout.LayoutCtx;

/**
 * Arc {@link Element} adapter that hosts an {@link El} tree.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Triggers layout when the El tree is dirty or bounds changed.</li>
 *   <li>Delegates Arc input events (touch, hover) to the El tree.</li>
 *   <li>Calls {@link El#draw()} each frame.</li>
 * </ul>
 *
 * <p>Add to scene: {@code Core.scene.add(panel);}
 */
public class NPanel extends Element {

    private El    root     = null;
    private El    captured = null;
    private El    hovered  = null;
    private float lastX, lastY;

    public NPanel() {
        this.touchable = Touchable.enabled;

        addListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float lx, float ly,
                                     int pointer, KeyCode button) {
                float px = x + lx, py = y + ly;
                captured = (root != null) ? root.hitAt(px, py) : null;
                lastX = px;
                lastY = py;
                if (captured != null) captured.setPressed(true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float lx, float ly,
                                int pointer, KeyCode button) {
                if (captured != null) {
                    captured.setPressed(false);
                    float px = x + lx, py = y + ly;
                    if (captured.hitTest(px, py)) captured.fireClick();
                    captured = null;
                }
            }

            @Override
            public void touchDragged(InputEvent event, float lx, float ly, int pointer) {
                // Reserved for scroll / drag widgets
            }

            @Override
            public boolean mouseMoved(InputEvent event, float lx, float ly) {
                El hit = (root != null) ? root.hitAt(x + lx, y + ly) : null;
                if (hit != hovered) {
                    if (hovered != null) hovered.setHovered(false);
                    hovered = hit;
                    if (hovered != null) hovered.setHovered(true);
                }
                return false;
            }
        });
    }

    // ── Root management ───────────────────────────────────────────────────────

    public void mount(El newRoot) {
        if (this.root != null) this.root.detach();
        this.root = newRoot;
    }

    public El getRoot() { return root; }

    // ── Arc Element overrides ─────────────────────────────────────────────────

    @Override
    public void draw() {
        super.draw();
        if (root == null) return;

        LayoutCtx.INSTANCE.refresh();

        boolean boundsChanged = root.getX() != x    || root.getY() != y
                             || root.getW() != width || root.getH() != height;

        if (root.isDirty() || boundsChanged) {
            root.layout(LayoutCtx.INSTANCE, x, y, width, height);
        }

        root.draw();
    }

    @Override
    public boolean remove() {
        if (hovered  != null) hovered.setHovered(false);
        if (captured != null) captured.setPressed(false);
        hovered  = null;
        captured = null;
        return super.remove();
    }
}
