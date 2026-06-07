package com.neko.libs.ui;

import com.neko.libs.state.State;
import com.neko.libs.ui.widget.NBtn;
import com.neko.libs.ui.widget.NDivider;
import com.neko.libs.ui.widget.NLabel;

/**
 * Static entry point for the NekoUI system.
 *
 * <h3>Usage</h3>
 * <pre>
 * import static com.neko.libs.ui.N.*;
 *
 * NPanel panel = panel();
 * panel.mount(
 *   el("col gap:8 p:16 bg:surface",
 *     label("Settings", "color:bright font:title grow-x"),
 *     divider(),
 *     el("row gap:8 grow-x",
 *       label("Volume", "color:secondary grow-x"),
 *       label(volState)
 *     ),
 *     spacer(),
 *     el("row gap:8 justify:end",
 *       btn("Cancel", this::close, "ghost"),
 *       btn("Apply",  this::save,  "primary")
 *     )
 *   )
 * );
 * Core.scene.add(panel);
 * </pre>
 */
public final class N {
    private N() {}

    // ── Containers ────────────────────────────────────────────────────────────

    /** Generic element (container). Children define its content. */
    public static El el(String style, El... children) {
        return new El(style).add(children);
    }

    /** Element with named variants driven by a {@link State}. */
    public static El el(State<String> variant, String[] variantDefs, El... children) {
        return new El(variant, variantDefs).add(children);
    }

    /**
     * Invisible spacer that grows to fill remaining space.
     * Use inside a row/col to push siblings apart.
     */
    public static El spacer() {
        return new El("grow");
    }

    // ── Labels ────────────────────────────────────────────────────────────────

    public static NLabel label(String text, String style) {
        return new NLabel(text, style);
    }

    public static NLabel label(String text) {
        return new NLabel(text);
    }

    public static NLabel label(State<String> state, String style) {
        return new NLabel(state, style);
    }

    public static NLabel label(State<String> state) {
        return new NLabel(state);
    }

    // ── Buttons ───────────────────────────────────────────────────────────────

    public static NBtn btn(String text, Runnable onClick, String style) {
        return new NBtn(text, style).onClick(onClick);
    }

    public static NBtn btn(String text, Runnable onClick) {
        return new NBtn(text).onClick(onClick);
    }

    public static NBtn btn(String text, String style) {
        return new NBtn(text, style);
    }

    // ── Dividers ──────────────────────────────────────────────────────────────

    /** Horizontal divider, grows to fill width. */
    public static NDivider divider() {
        return new NDivider(false);
    }

    /** Vertical divider, grows to fill height. */
    public static NDivider vdivider() {
        return new NDivider(true);
    }

    // ── Scene helpers ─────────────────────────────────────────────────────────

    public static NPanel panel() {
        return new NPanel();
    }

    /**
     * Shortcut: create a panel, mount root, and return the panel.
     * <pre>
     * Core.scene.add(N.mount(el("col gap:8", ...)));
     * </pre>
     */
    public static NPanel mount(El root) {
        NPanel p = new NPanel();
        p.mount(root);
        return p;
    }

    // ── Variant helper ────────────────────────────────────────────────────────

    /**
     * Convenience to build a variantDefs array inline without casting:
     * <pre>
     * el(v, variants("desktop: col gap:8", "mobile: col gap:4"), child1, child2)
     * </pre>
     */
    public static String[] variants(String... defs) {
        return defs;
    }
}
