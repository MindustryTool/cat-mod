package com.neko.libs.ui.style;

import com.neko.libs.ui.layout.LayoutCtx;

/**
 * Tiny arithmetic expression evaluator for style values.
 *
 * <p>Supports: numbers, {@code +}, {@code -}, {@code *}, {@code /},
 * parentheses, and context variables ({@code scl}, {@code sw}, {@code sh}).
 *
 * <p>Examples:
 * <pre>
 *   StyleExpr.eval("8",        ctx)  →  8f
 *   StyleExpr.eval("{8*scl}",  ctx)  →  8 * ctx.scl
 *   StyleExpr.eval("{sw-32}",  ctx)  →  ctx.sw - 32
 *   StyleExpr.eval("{sw*0.8}", ctx)  →  ctx.sw * 0.8f
 * </pre>
 */
public final class StyleExpr {

    /**
     * Evaluate a raw value string.
     * The string may be a plain number, or an expression wrapped in {@code { }}.
     */
    public static float eval(String raw, LayoutCtx ctx) {
        if (raw == null || raw.isEmpty()) return 0f;

        String s = raw.trim();

        // Strip surrounding braces: {expr} → expr
        if (s.charAt(0) == '{' && s.charAt(s.length() - 1) == '}') {
            s = s.substring(1, s.length() - 1).trim();
        }

        // Fast path: plain number
        try { return Float.parseFloat(s); } catch (NumberFormatException ignored) {}

        return new StyleExpr(s, ctx).parseExpr();
    }

    // ── Parser state ─────────────────────────────────────────────────────────

    private final String     src;
    private final LayoutCtx  ctx;
    private       int        pos;

    private StyleExpr(String src, LayoutCtx ctx) {
        this.src = src;
        this.ctx = ctx;
    }

    // expr = term (('+' | '-') term)*
    private float parseExpr() {
        float v = parseTerm();
        while (pos < src.length()) {
            skipSpaces();
            if (pos >= src.length()) break;
            char c = src.charAt(pos);
            if      (c == '+') { pos++; v += parseTerm(); }
            else if (c == '-') { pos++; v -= parseTerm(); }
            else break;
        }
        return v;
    }

    // term = factor (('*' | '/') factor)*
    private float parseTerm() {
        float v = parseFactor();
        while (pos < src.length()) {
            skipSpaces();
            if (pos >= src.length()) break;
            char c = src.charAt(pos);
            if (c == '*') { pos++; v *= parseFactor(); }
            else if (c == '/') {
                pos++;
                float d = parseFactor();
                v = (d != 0f) ? v / d : 0f;
            } else break;
        }
        return v;
    }

    // factor = '-' factor | '(' expr ')' | NUMBER | VAR
    private float parseFactor() {
        skipSpaces();
        if (pos >= src.length()) return 0f;

        char c = src.charAt(pos);

        if (c == '-') { pos++; return -parseFactor(); }

        if (c == '(') {
            pos++;
            float v = parseExpr();
            if (pos < src.length() && src.charAt(pos) == ')') pos++;
            return v;
        }

        if (Character.isDigit(c) || c == '.') return parseNumber();
        if (Character.isLetter(c) || c == '_') return parseVar();

        return 0f;
    }

    private float parseNumber() {
        int start = pos;
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (Character.isDigit(c) || c == '.') pos++;
            else break;
        }
        try { return Float.parseFloat(src.substring(start, pos)); }
        catch (NumberFormatException e) { return 0f; }
    }

    private float parseVar() {
        int start = pos;
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (Character.isLetterOrDigit(c) || c == '_') pos++;
            else break;
        }
        return ctx.resolve(src.substring(start, pos));
    }

    private void skipSpaces() {
        while (pos < src.length() && src.charAt(pos) == ' ') pos++;
    }
}
