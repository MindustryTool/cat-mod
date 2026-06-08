package com.neko.libs.ui.style;

import arc.graphics.Color;
import arc.math.Interp;
import com.neko.libs.ui.layout.LayoutCtx;
import com.neko.libs.ui.style.Theme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses a style string into a {@link StyleSpec}.
 * Tokenization is cached; expression evaluation runs each layout pass.
 *
 * <h3>Lazy allocation</h3>
 * Sub-objects ({@code Pad}, {@code Align}, {@code Visual}, etc.) are
 * created only when a relevant token is encountered — via the
 * {@code spec.pad()}, {@code spec.visual()}, … lazy-init helpers.
 */
public final class StyleParser {

    private static final Map<String, List<String[]>> TOKEN_CACHE = new HashMap<>();

    // ── Public API ────────────────────────────────────────────────────────────

    public static StyleSpec parse(String style, LayoutCtx ctx) {
        StyleSpec spec = new StyleSpec();
        if (style == null || style.isBlank()) return spec;
        for (String[] tok : tokenize(style)) apply(spec, tok, ctx);
        return spec;
    }

    // ── Tokenizer (result cached per unique style string) ─────────────────────

    private static List<String[]> tokenize(String style) {
        return TOKEN_CACHE.computeIfAbsent(style, s -> {
            List<String[]> out = new ArrayList<>();
            int i = 0, len = s.length();
            while (i < len) {
                while (i < len && s.charAt(i) == ' ') i++;
                if (i >= len) break;

                int start = i, depth = 0;
                while (i < len) {
                    char c = s.charAt(i);
                    if      (c == '{') depth++;
                    else if (c == '}') depth--;
                    else if (c == ' ' && depth == 0) break;
                    i++;
                }

                String token = s.substring(start, i).trim();
                if (token.isEmpty()) continue;

                int colon = colonOutside(token);
                out.add(colon >= 0
                    ? new String[]{ token.substring(0, colon), token.substring(colon + 1) }
                    : new String[]{ token });
            }
            return out;
        });
    }

    private static int colonOutside(String s) {
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if      (c == '{') depth++;
            else if (c == '}') depth--;
            else if (c == ':' && depth == 0) return i;
        }
        return -1;
    }

    // ── Token application ─────────────────────────────────────────────────────

    private static void apply(StyleSpec s, String[] tok, LayoutCtx ctx) {
        String key = tok[0];
        String val = tok.length > 1 ? tok[1] : null;
        float  fv  = val != null ? StyleExpr.eval(val, ctx) : 0f;

        // @name syntax — reference a registered style from StyleRegistry
        if (key.startsWith("@")) {
            String ref = key.substring(1);
            String resolved = StyleRegistry.INSTANCE.resolvedString(ref);
            if (resolved != null) {
                for (String[] t : tokenize(resolved)) apply(s, t, ctx);
            }
            return;
        }

        switch (key) {

            // ── Core ──────────────────────────────────────────────────────────
            case "col"    -> s.isColumn = true;
            case "row"    -> s.isColumn = false;
            case "hidden" -> s.hidden   = true;
            case "gap"    -> s.gap      = fv;

            // ── Sizing (inline) ───────────────────────────────────────────────
            case "grow"   -> { s.widthMode  = StyleSpec.SizeMode.GROW;
                                s.heightMode = StyleSpec.SizeMode.GROW; }
            case "grow-x" ->   s.widthMode  = StyleSpec.SizeMode.GROW;
            case "grow-y" ->   s.heightMode = StyleSpec.SizeMode.GROW;

            case "w" -> {
                if ("full".equals(val)) s.widthMode = StyleSpec.SizeMode.GROW;
                else { s.widthMode = StyleSpec.SizeMode.FIXED; s.fixedWidth = fv; }
            }
            case "h" -> {
                if ("full".equals(val)) s.heightMode = StyleSpec.SizeMode.GROW;
                else { s.heightMode = StyleSpec.SizeMode.FIXED; s.fixedHeight = fv; }
            }

            // ── Constraints (lazy Constraint) ─────────────────────────────────
            case "min-w" -> s.constraint().minWidth  = fv;
            case "max-w" -> s.constraint().maxWidth  = fv;
            case "min-h" -> s.constraint().minHeight = fv;
            case "max-h" -> s.constraint().maxHeight = fv;

            // ── Padding (lazy Pad) ────────────────────────────────────────────
            case "p"  -> { s.pad().top = s.pad().right = s.pad().bottom = s.pad().left = fv; }
            case "px" -> { s.pad().left  = s.pad().right  = fv; }
            case "py" -> { s.pad().top   = s.pad().bottom = fv; }
            case "pt" ->   s.pad().top    = fv;
            case "pb" ->   s.pad().bottom = fv;
            case "pl" ->   s.pad().left   = fv;
            case "pr" ->   s.pad().right  = fv;

            // ── Alignment (lazy Align) ────────────────────────────────────────
            case "justify" -> s.align().justify = switch (val != null ? val : "") {
                case "center"  -> StyleSpec.Align.Justify.CENTER;
                case "end"     -> StyleSpec.Align.Justify.END;
                case "between" -> StyleSpec.Align.Justify.BETWEEN;
                default        -> StyleSpec.Align.Justify.START;
            };
            case "items" -> s.align().items = switch (val != null ? val : "") {
                case "center"  -> StyleSpec.Align.Items.CENTER;
                case "end"     -> StyleSpec.Align.Items.END;
                case "start"   -> StyleSpec.Align.Items.START;
                default        -> StyleSpec.Align.Items.STRETCH;
            };

            // ── Visual (lazy Visual) ──────────────────────────────────────────
            case "bg"       -> s.visual().background  = color(val);
            case "border"   -> s.visual().borderColor = color(val);
            case "border-w" -> s.visual().borderWidth = fv;
            case "opacity"  -> s.visual().opacity     = fv;

            // ── Transition (lazy Transition) ──────────────────────────────────
            case "transition" -> {
                if (!"none".equals(val)) s.transition().duration = 150f;
            }
            case "duration" -> s.transition().duration = fv;
            case "ease" -> s.transition().ease = switch (val != null ? val : "") {
                case "smooth" -> Interp.smooth;
                case "out"    -> Interp.pow2Out;
                case "in"     -> Interp.pow2In;
                default       -> Interp.linear;
            };

            // Unknown tokens silently ignored — widget subclasses may read them
        }
    }

    // ── Color resolution ──────────────────────────────────────────────────────

    private static Color color(String val) {
        if (val == null) return null;
        return switch (val) {
            case "surface"  -> Theme.bgSurface;
            case "raised"   -> Theme.bgRaised;
            case "input"    -> Theme.bgInput;
            case "base"     -> Theme.bgBase;
            case "void"     -> Theme.bgVoid;
            case "subtle"   -> Theme.borderSubtle;
            case "default"  -> Theme.borderDefault;
            case "active"   -> Theme.borderActive;
            case "accent"   -> Theme.accentPrimary;
            case "red"      -> Theme.accentRed;
            case "green"    -> Theme.accentGreen;
            case "blue"     -> Theme.accentBlue;
            default -> { try { yield Color.valueOf(val); } catch (Exception e) { yield null; } }
        };
    }
}
