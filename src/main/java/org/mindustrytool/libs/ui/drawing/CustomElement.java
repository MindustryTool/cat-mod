package org.mindustrytool.libs.ui.drawing;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Gl;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.Shader;
import arc.util.Disposable;

import org.mindustrytool.util.ImageLoader;

public class CustomElement implements Disposable {

    // ══════════════════════════════════════════════════════════════════════════════════
    //  Vertex & Fragment Shaders
    // ══════════════════════════════════════════════════════════════════════════════════

    private static final String VERTEX_SRC = """
        attribute vec4 a_position;
        attribute vec4 a_color;
        attribute vec2 a_texCoord0;
        attribute vec4 a_mix_color;
        uniform mat4 u_projTrans;
        varying vec4 v_color;
        varying vec2 v_texCoord0;
        void main() {
            v_color = a_color;
            v_color.a = v_color.a * (255.0/254.0);
            v_texCoord0 = a_texCoord0;
            gl_Position = u_projTrans * a_position;
        }
        """;

    private static final String FRAGMENT_SRC = """
        uniform vec4  u_cornerRadii;
        uniform vec2  u_size;
        uniform float u_edgeSoftness;

        uniform vec4  u_fillColor;
        uniform float u_fillMode;
        uniform sampler2D u_gradientTex;
        uniform vec4  u_gradientParams;
        uniform sampler2D u_fillTexture;
        uniform vec2  u_uvScale;
        uniform vec2  u_uvOffset;

        uniform float u_borderWidth;
        uniform vec4  u_borderColor;
        uniform float u_borderStyle;
        uniform float u_dashLength;
        uniform float u_dashRatio;

        uniform vec4  u_innerShadowColor;
        uniform float u_innerShadowSpread;
        uniform float u_innerShadowBlur;

        uniform vec4  u_outerGlowColor;
        uniform float u_outerGlowSpread;

        uniform float u_opacity;
        uniform vec4  u_colorFilter;
        uniform float u_noiseAmount;

        uniform sampler2D u_blurTexture;
        uniform vec4  u_elementBounds;
        uniform float u_blendMode;

        varying vec4 v_color;
        varying vec2 v_texCoord0;

        float roundedBoxSDF(vec2 p, vec2 s, vec4 r) {
            vec2 rSel = (p.x > 0.0) ? r.xy : r.zw;
            float radius = (p.y > 0.0) ? rSel.x : rSel.y;
            vec2 q = abs(p) - s + radius;
            return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius;
        }

        float gradientPos(vec2 uv) {
            float type = u_gradientParams.z;
            float t = uv.x;
            if (type < 0.5) {
                vec2 dir = normalize(u_gradientParams.xy);
                t = dot(uv - 0.5, dir) + 0.5;
            } else if (type < 1.5) {
                vec2 center = (u_gradientParams.xy + 1.0) * 0.5;
                t = distance(uv, center) * 1.414;
            } else {
                vec2 rel = uv - 0.5;
                t = atan(rel.y, rel.x) / 6.2832 + 0.5;
            }
            return fract(t * u_gradientParams.w);
        }

        float hash(vec2 p) {
            return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
        }

        void main() {
            vec2 halfSize = u_size * 0.5;
            vec2 pos = v_texCoord0 * u_size - halfSize;

            // ─── SDF ───
            float dist = roundedBoxSDF(pos, halfSize, u_cornerRadii);
            float outerAlpha = 1.0 - smoothstep(0.0, u_edgeSoftness, dist);
            if (outerAlpha < 0.001) { discard; return; }

            // ─── Fill ───
            vec4 baseColor = u_fillColor;
            if (u_fillMode > 0.5) {
                if (u_fillMode < 1.5) {
                    float t = gradientPos(v_texCoord0);
                    baseColor = texture2D(u_gradientTex, vec2(t, 0.5));
                } else {
                    vec2 uv = (v_texCoord0 - 0.5) * u_uvScale + 0.5 + u_uvOffset;
                    baseColor = texture2D(u_fillTexture, uv) * u_fillColor;
                }
            }

            // ─── Inner Shadow ───
            float shadowDist = dist + u_innerShadowSpread;
            float shadowAlpha = (1.0 - smoothstep(0.0, max(u_innerShadowBlur, 0.001), shadowDist));
            shadowAlpha *= 1.0 - step(0.0, shadowDist);
            baseColor = mix(baseColor, u_innerShadowColor, shadowAlpha);

            // ─── Border ───
            vec4 result = baseColor;
            float borderMix = 0.0;
            if (u_borderWidth > 0.001) {
                vec4 innerRadii = max(u_cornerRadii - u_borderWidth, 0.0);
                vec2 innerHalfSize = max(halfSize - u_borderWidth, 0.0);
                float innerDist = roundedBoxSDF(pos, innerHalfSize, innerRadii);
                float innerAlpha = 1.0 - smoothstep(0.0, u_edgeSoftness, innerDist);
                float borderAlpha = outerAlpha - innerAlpha;

                if (borderAlpha > 0.001) {
                    vec4 borderOut = u_borderColor;
                    if (u_borderStyle > 0.5) {
                        float px = v_texCoord0.x, py = v_texCoord0.y;
                        float perim;
                        if (py < 0.001) perim = px;
                        else if (px > 0.999) perim = 1.0 + py;
                        else if (py > 0.999) perim = 2.0 + (1.0 - px);
                        else perim = 3.0 + (1.0 - py);
                        perim /= 4.0;
                        float dashPos = fract(perim * u_size.x * 2.0 / max(u_dashLength, 1.0));
                        float dashMask = step(dashPos, u_dashRatio);
                        if (u_borderStyle > 1.5) {
                            dashMask *= 1.0 - step(abs(dashPos - u_dashRatio * 0.5), 0.03);
                        }
                        borderOut.a *= dashMask;
                        borderAlpha *= dashMask;
                    }
                    borderMix = borderAlpha / max(outerAlpha, 0.001);
                    result = mix(baseColor, borderOut, borderMix);
                }
            }
            float visibleAlpha = outerAlpha * mix(baseColor.a, u_borderColor.a, borderMix);

            // ─── Outer Glow ───
            if (u_outerGlowSpread > 0.001) {
                float glowDist = dist - u_outerGlowSpread;
                float glowAlpha = smoothstep(-u_edgeSoftness, 0.0, -glowDist);
                glowAlpha *= 1.0 - outerAlpha;
                if (glowAlpha > 0.001) {
                    vec4 glow = u_outerGlowColor;
                    glow.a *= glowAlpha;
                    result = result * (1.0 - glowAlpha) + glow * glowAlpha;
                    visibleAlpha = max(visibleAlpha, glowAlpha);
                }
            }

            // ─── Glass Overlay ───
            if (u_blendMode > 0.001) {
                vec2 screenUV = u_elementBounds.xy + v_texCoord0 * u_elementBounds.zw;
                vec4 blurColor = texture2D(u_blurTexture, screenUV);
                vec4 glassColor = mix(blurColor * u_fillColor, mix(blurColor, u_fillColor, u_fillColor.a), u_blendMode);
                result = mix(result, glassColor, 0.8);
                visibleAlpha = max(visibleAlpha, 0.8);
            }

            // ─── Opacity ───
            result.a = visibleAlpha * u_opacity;

            // ─── Color Filter ───
            float filter = u_colorFilter.x;
            float amount = u_colorFilter.y;
            if (filter > 0.5 && amount > 0.001) {
                if (filter < 1.5) {
                    float luma = dot(result.rgb, vec3(0.299, 0.587, 0.114));
                    result.rgb = mix(result.rgb, vec3(luma), amount);
                } else if (filter < 2.5) {
                    vec3 sepia = vec3(
                        dot(result.rgb, vec3(0.393, 0.769, 0.189)),
                        dot(result.rgb, vec3(0.349, 0.686, 0.168)),
                        dot(result.rgb, vec3(0.272, 0.534, 0.131))
                    );
                    result.rgb = mix(result.rgb, sepia, amount);
                } else if (filter < 3.5) {
                    result.rgb += amount;
                } else {
                    result.rgb = mix(result.rgb, 1.0 - result.rgb, amount);
                }
            }

            // ─── Noise ───
            if (u_noiseAmount > 0.001) {
                result.rgb += (hash(v_texCoord0 + 100.0) - 0.5) * u_noiseAmount;
            }

            gl_FragColor = result;
        }
        """;

    // ─── Kawase Blur Shader ───
    private static final String BLUR_VERTEX_SRC = """
        attribute vec4 a_position;
        attribute vec2 a_texCoord0;
        varying vec2 v_texCoord0;
        void main() {
            gl_Position = a_position;
            v_texCoord0 = a_texCoord0;
        }
        """;

    private static final String BLUR_FRAGMENT_SRC = """
        uniform sampler2D u_texture;
        uniform vec2 u_texelSize;
        uniform float u_offset;
        varying vec2 v_texCoord0;
        void main() {
            vec2 uv = v_texCoord0;
            vec2 hp = u_texelSize * u_offset;
            vec4 sum = texture2D(u_texture, uv) * 4.0;
            sum += texture2D(u_texture, uv + vec2(-hp.x, -hp.y));
            sum += texture2D(u_texture, uv + vec2( hp.x, -hp.y));
            sum += texture2D(u_texture, uv + vec2(-hp.x,  hp.y));
            sum += texture2D(u_texture, uv + vec2( hp.x,  hp.y));
            gl_FragColor = sum / 8.0;
        }
        """;

    // ══════════════════════════════════════════════════════════════════════════════════
    //  Static Resources
    // ══════════════════════════════════════════════════════════════════════════════════

    private static Shader mainShader;
    private static Shader blurShader;
    private static boolean shadersLoaded;

    private static void ensureShaders() {
        if (shadersLoaded) return;
        mainShader = new Shader(VERTEX_SRC, FRAGMENT_SRC);
        mainShader.bind();
        mainShader.setUniformi("u_gradientTex", 1);
        mainShader.setUniformi("u_fillTexture", 2);
        mainShader.setUniformi("u_blurTexture", 3);
        blurShader = new Shader(BLUR_VERTEX_SRC, BLUR_FRAGMENT_SRC);
        shadersLoaded = true;
    }

    // ══════════════════════════════════════════════════════════════════════════════════
    //  Screen Capture (static, shared across instances)
    // ══════════════════════════════════════════════════════════════════════════════════

    private static FrameBuffer captureFbo;

    public static Texture captureScreen() {
        int w = Math.max(Core.graphics.getWidth() / 2, 2);
        int h = Math.max(Core.graphics.getHeight() / 2, 2);
        if (captureFbo == null || captureFbo.getWidth() != w || captureFbo.getHeight() != h) {
            if (captureFbo != null) captureFbo.dispose();
            captureFbo = new FrameBuffer(w, h);
        }
        Draw.flush();
        Gl.bindTexture(Gl.texture2d, captureFbo.getTexture().getTextureObjectHandle());
        Gl.copyTexSubImage2D(Gl.texture2d, 0, 0, 0, 0, 0, w, h);
        Gl.bindTexture(Gl.texture2d, 0);
        return captureFbo.getTexture();
    }

    public static void disposeCapture() {
        if (captureFbo != null) { captureFbo.dispose(); captureFbo = null; }
    }

    // ══════════════════════════════════════════════════════════════════════════════════
    //  Per-Instance FBOs (for glass blur)
    // ══════════════════════════════════════════════════════════════════════════════════

    private FrameBuffer blurFboA, blurFboB;
    private int lastFboW, lastFboH;

    // ══════════════════════════════════════════════════════════════════════════════════
    //  Render State
    // ══════════════════════════════════════════════════════════════════════════════════

    private float tl, tr, br, bl;
    private int fillMode;
    private final Color fillColor = new Color(Color.darkGray);
    private Texture gradientTex;
    private float gradDx, gradDy, gradType, gradRepeat;
    private Texture fillTexture;
    private float uvSx = 1f, uvSy = 1f, uvOx, uvOy;
    private float borderWidth;
    private final Color borderColor = new Color(Color.white);
    private int borderStyle;
    private float dashLen = 10f, dashRatio = 0.5f;
    private final Color innerShadowColor = new Color(0, 0, 0, 0);
    private float innerShadowSpread, innerShadowBlur;
    private final Color outerGlowColor = new Color(0, 0, 0, 0);
    private float outerGlowSpread;
    private float opacity = 1f;
    private int filterMode;
    private float filterAmount;
    private float noiseAmount;
    private Texture blurTexture;
    private float elBoundsX, elBoundsY, elBoundsW, elBoundsH;
    private float blendMode;
    private Texture fallbackTexture;

    // ══════════════════════════════════════════════════════════════════════════════════
    //  Public API
    // ══════════════════════════════════════════════════════════════════════════════════

    public void fill(float x, float y, float w, float h, float radius, Color color) {
        fillMode = 0; fillColor.set(color);
        tl = tr = br = bl = radius;
        render(x, y, w, h);
    }

    public void fill(float x, float y, float w, float h, float tl, float tr, float br, float bl, Color color) {
        fillMode = 0; fillColor.set(color);
        this.tl = tl; this.tr = tr; this.br = br; this.bl = bl;
        render(x, y, w, h);
    }

    public void fillGradient(float x, float y, float w, float h, float radius, Gradient gradient) {
        fillMode = 1; fillColor.set(Color.white);
        applyGradient(gradient);
        tl = tr = br = bl = radius;
        render(x, y, w, h);
    }

    public void fillGradient(float x, float y, float w, float h, float tl, float tr, float br, float bl, Gradient gradient) {
        fillMode = 1; fillColor.set(Color.white);
        applyGradient(gradient);
        this.tl = tl; this.tr = tr; this.br = br; this.bl = bl;
        render(x, y, w, h);
    }

    public void fillTexture(float x, float y, float w, float h, float radius, Texture texture, Color tint,
                            float scaleX, float scaleY, float offsetX, float offsetY) {
        fillMode = 2;
        fillColor.set(tint);
        fillTexture = texture;
        uvSx = scaleX; uvSy = scaleY; uvOx = offsetX; uvOy = offsetY;
        tl = tr = br = bl = radius;
        render(x, y, w, h);
    }

    public void fillTexture(float x, float y, float w, float h,
                            float tl, float tr, float br, float bl,
                            Texture texture, Color tint,
                            float scaleX, float scaleY, float offsetX, float offsetY) {
        fillMode = 2;
        fillColor.set(tint);
        fillTexture = texture;
        uvSx = scaleX; uvSy = scaleY; uvOx = offsetX; uvOy = offsetY;
        this.tl = tl; this.tr = tr; this.br = br; this.bl = bl;
        render(x, y, w, h);
    }

    public void loadImage(float x, float y, float w, float h, float radius, String url, Color tint,
                          float scaleX, float scaleY, float offsetX, float offsetY) {
        Texture tex = ImageLoader.get(url);
        if (tex != null) {
            fillTexture(x, y, w, h, radius, tex, tint, scaleX, scaleY, offsetX, offsetY);
            return;
        }
        if (fallbackTexture != null) {
            fillTexture(x, y, w, h, radius, fallbackTexture, tint, scaleX, scaleY, offsetX, offsetY);
        }
        ImageLoader.load(url, t -> {
            fillTexture(x, y, w, h, radius, t, tint, scaleX, scaleY, offsetX, offsetY);
        });
    }

    public void loadImage(float x, float y, float w, float h, float radius, String url) {
        loadImage(x, y, w, h, radius, url, Color.white, 1f, 1f, 0f, 0f);
    }

    public void fallback(Texture texture) {
        fallbackTexture = texture;
    }

    public void fillWithBorder(float x, float y, float w, float h, float radius,
                               Color fill, float borderW, Color borderC) {
        fillMode = 0; fillColor.set(fill);
        borderWidth = borderW; borderColor.set(borderC); borderStyle = 0;
        tl = tr = br = bl = radius;
        render(x, y, w, h);
    }

    public void fillWithBorder(float x, float y, float w, float h,
                               float tl, float tr, float br, float bl,
                               Color fill, float borderW, Color borderC) {
        fillMode = 0; fillColor.set(fill);
        borderWidth = borderW; borderColor.set(borderC); borderStyle = 0;
        this.tl = tl; this.tr = tr; this.br = br; this.bl = bl;
        render(x, y, w, h);
    }

    public void fillGradientWithBorder(float x, float y, float w, float h, float radius,
                                       Gradient gradient, float borderW, Color borderC) {
        fillMode = 1; fillColor.set(Color.white);
        applyGradient(gradient);
        borderWidth = borderW; borderColor.set(borderC); borderStyle = 0;
        tl = tr = br = bl = radius;
        render(x, y, w, h);
    }

    public void fillDashedBorder(float x, float y, float w, float h, float radius,
                                 Color fill, float borderW, Color borderC,
                                 float dashLen, float dashRatio) {
        fillMode = 0; fillColor.set(fill);
        borderWidth = borderW; borderColor.set(borderC); borderStyle = 1;
        this.dashLen = dashLen; this.dashRatio = dashRatio;
        tl = tr = br = bl = radius;
        render(x, y, w, h);
    }

    public void fillWithShadow(float x, float y, float w, float h, float radius,
                               Color fill, float shadowSpread, float shadowBlur, Color shadowColor) {
        fillMode = 0; fillColor.set(fill);
        innerShadowSpread = shadowSpread; innerShadowBlur = shadowBlur;
        innerShadowColor.set(shadowColor);
        tl = tr = br = bl = radius;
        render(x, y, w, h);
    }

    public void glass(float x, float y, float w, float h, float radius,
                      Color tint, int blurIterations,
                      float borderW, Color borderC) {
        fillMode = 0; fillColor.set(tint);
        borderWidth = borderW; borderColor.set(borderC); borderStyle = 0;
        blendMode = 0.8f;
        tl = tr = br = bl = radius;

        Texture captureTex = captureScreen();
        int fw = Math.max((int)(w * 0.5f), 2);
        int fh = Math.max((int)(h * 0.5f), 2);
        ensureBlurFbos(fw, fh);

        float sw = Core.graphics.getWidth();
        float sh = Core.graphics.getHeight();
        float u1 = x / sw, v1 = y / sh;
        float u2 = (x + w) / sw, v2 = (y + h) / sh;

        blurFboA.begin();
        Draw.color(Color.white);
        float pc = Draw.getColor().toFloatBits();
        Fill.quad(captureTex,
            0, 0, pc, u1, v1,
            fw, 0, pc, u2, v1,
            fw, fh, pc, u2, v2,
            0, fh, pc, u1, v2);
        Draw.flush();
        blurFboA.end();

        doBlur(fw, fh, blurIterations);

        elBoundsX = u1; elBoundsY = v1;
        elBoundsW = u2 - u1; elBoundsH = v2 - v1;
        blurTexture = blurFboA.getTexture();

        render(x, y, w, h);
    }

    public void glass(float x, float y, float w, float h, float radius,
                      Color tint, int blurIterations) {
        glass(x, y, w, h, radius, tint, blurIterations, 0f, Color.white);
    }

    // ─── Lifecycle ───

    @Override
    public void dispose() {
        if (blurFboA != null) { blurFboA.dispose(); blurFboA = null; }
        if (blurFboB != null) { blurFboB.dispose(); blurFboB = null; }
    }

    public static void disposeShared() {
        if (mainShader != null) { mainShader.dispose(); mainShader = null; }
        if (blurShader != null) { blurShader.dispose(); blurShader = null; }
        shadersLoaded = false;
    }

    // ══════════════════════════════════════════════════════════════════════════════════
    //  Internal
    // ══════════════════════════════════════════════════════════════════════════════════

    private void applyGradient(Gradient g) {
        gradientTex = g.getTexture();
        float[] p = g.getParams();
        gradDx = p[0]; gradDy = p[1]; gradType = p[2]; gradRepeat = p[3];
    }

    private void render(float x, float y, float w, float h) {
        if (w <= 0f || h <= 0f) return;
        ensureShaders();

        Draw.flush();
        Shader prev = Draw.getShader();
        Draw.shader(mainShader);
        mainShader.bind();

        mainShader.setUniformf("u_cornerRadii", tl, tr, br, bl);
        mainShader.setUniformf("u_size", w, h);
        mainShader.setUniformf("u_edgeSoftness", 1.0f);

        mainShader.setUniformf("u_fillColor", fillColor.r, fillColor.g, fillColor.b, fillColor.a);
        mainShader.setUniformf("u_fillMode", fillMode);
        mainShader.setUniformf("u_gradientParams", gradDx, gradDy, gradType, gradRepeat);
        mainShader.setUniformf("u_uvScale", uvSx, uvSy);
        mainShader.setUniformf("u_uvOffset", uvOx, uvOy);

        mainShader.setUniformf("u_borderWidth", Math.min(borderWidth, Math.min(w, h) * 0.5f));
        mainShader.setUniformf("u_borderColor", borderColor.r, borderColor.g, borderColor.b, borderColor.a);
        mainShader.setUniformf("u_borderStyle", borderStyle);
        mainShader.setUniformf("u_dashLength", dashLen);
        mainShader.setUniformf("u_dashRatio", dashRatio);

        mainShader.setUniformf("u_innerShadowColor", innerShadowColor.r, innerShadowColor.g, innerShadowColor.b, innerShadowColor.a);
        mainShader.setUniformf("u_innerShadowSpread", innerShadowSpread);
        mainShader.setUniformf("u_innerShadowBlur", innerShadowBlur);

        mainShader.setUniformf("u_outerGlowColor", outerGlowColor.r, outerGlowColor.g, outerGlowColor.b, outerGlowColor.a);
        mainShader.setUniformf("u_outerGlowSpread", outerGlowSpread);

        mainShader.setUniformf("u_opacity", opacity);
        mainShader.setUniformf("u_colorFilter", filterMode, filterAmount, 0f, 0f);
        mainShader.setUniformf("u_noiseAmount", noiseAmount);

        mainShader.setUniformf("u_elementBounds", elBoundsX, elBoundsY, elBoundsW, elBoundsH);
        mainShader.setUniformf("u_blendMode", blendMode);

        // Bind optional textures
        if (fillMode == 1 && gradientTex != null) {
            gradientTex.bind(1);
        }
        if (fillMode == 2 && fillTexture != null) {
            fillTexture.bind(2);
        }
        if (blendMode > 0.001f && blurTexture != null) {
            blurTexture.bind(3);
        }

        Draw.color(Color.white);
        float pc = Draw.getColor().toFloatBits();
        Fill.quad(Core.atlas.white().texture,
            x, y, pc, 0f, 0f,
            x + w, y, pc, 1f, 0f,
            x + w, y + h, pc, 1f, 1f,
            x, y + h, pc, 0f, 1f);

        Draw.flush();
        Draw.shader(prev);
    }

    private void ensureBlurFbos(int w, int h) {
        if (blurFboA == null || blurFboA.getWidth() != w || blurFboA.getHeight() != h) {
            if (blurFboA != null) blurFboA.dispose();
            if (blurFboB != null) blurFboB.dispose();
            blurFboA = new FrameBuffer(w, h);
            blurFboB = new FrameBuffer(w, h);
            lastFboW = w; lastFboH = h;
        }
    }

    private void doBlur(int fw, int fh, int iterations) {
        float tsizeU = 1f / fw, tsizeV = 1f / fh;

        blurShader.bind();
        blurShader.setUniformf("u_texelSize", tsizeU, tsizeV);

        for (int i = 0; i < iterations; i++) {
            float offset = i * 0.5f + 0.5f;
            blurShader.setUniformf("u_offset", offset);

            blurFboB.begin();
            Draw.color(Color.white);
            Draw.blit(blurFboA.getTexture(), blurShader);
            Draw.flush();
            blurFboB.end();

            FrameBuffer tmp = blurFboA;
            blurFboA = blurFboB;
            blurFboB = tmp;
        }
    }
}
