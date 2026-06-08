package com.neko.libs.ui.style;

import com.neko.libs.ui.layout.LayoutCtx;
import java.util.HashMap;
import java.util.Map;

/**
 * Global registry for named styles.
 *
 * <p>Styles can be registered as stand-alone strings or with an optional
 * parent name for inheritance.  When a style has a parent, the parent
 * spec is parsed first and the child tokens are applied on top.
 *
 * <p>Style strings in {@link StyleParser} may reference a registered name
 * via the {@code @name} syntax, which triggers a lookup here.
 */
public final class StyleRegistry {

    public static final StyleRegistry INSTANCE = new StyleRegistry();

    private final Map<String, String> raw   = new HashMap<>();
    private final Map<String, String> resolved = new HashMap<>();
    private final Map<String, String> parents = new HashMap<>();

    private StyleRegistry() {}

    // ── Registration ──────────────────────────────────────────────────────

    /** Register a named style (stand-alone, no parent). */
    public void register(String name, String styleSpec) {
        raw.put(name, styleSpec);
        resolved.remove(name);
        parents.remove(name);
    }

    /** Register a named style that inherits from a parent. */
    public void register(String name, String parent, String styleSpec) {
        raw.put(name, styleSpec);
        resolved.remove(name);
        parents.put(name, parent);
    }

    // ── Lookup ────────────────────────────────────────────────────────────

    /**
     * Return the fully-resolved style string for {@code name} (parent tokens
     * concatenated with child tokens, separated by semicolon).
     */
    public String resolvedString(String name) {
        String cached = resolved.get(name);
        if (cached != null) return cached;
        String r = raw.get(name);
        if (r == null) return null;
        String p = parents.get(name);
        if (p != null) {
            String pr = raw.get(p);
            if (pr != null) {
                r = pr + ";" + r;
            }
        }
        resolved.put(name, r);
        return r;
    }

    /** Resolve a named style to its {@link StyleSpec}. */
    public StyleSpec get(String name) {
        String str = resolvedString(name);
        return str != null ? StyleParser.parse(str, LayoutCtx.INSTANCE) : new StyleSpec();
    }

    /** Check if a name is registered. */
    public boolean has(String name) {
        return raw.containsKey(name);
    }
}
