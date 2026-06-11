package org.mindustrytool.libs.ui.core;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Gl;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.Shader;
import arc.util.Disposable;

import org.mindustrytool.util.Resources;

/**
 * Low-level rendering engine for custom UI elements.
 *
 * <p>Uses custom OpenGL shaders and SDF (Signed Distance Fields) to draw premium
 * visual effects: rounded corners, gradients, borders, inner shadows, glow, and
 * frosted-glass blur.
 *
 * <p><b>Lifecycle:</b> shaders and the screen-capture framebuffer are shared across all
 * instances ({@code static}). Per-instance framebuffers are used for the glass blur effect
 * and are released via {@link #dispose()}.
 */
public class CustomDraw implements Disposable {

    // ─── Shared (static) resources ───

    private static Shader mainShader;
    private static Shader blurShader;
    private static boolean shadersLoaded;


    private static void ensureShaders() {
        if (shadersLoaded) return;
        mainShader = new Shader(
            Resources.readString(Resources.SHADER_CUSTOM_ELEMENT_VERT),
            Resources.readString(Resources.SHADER_CUSTOM_ELEMENT_FRAG)
        );
        mainShader.bind();
        mainShader.setUniformi("u_gradientTex", 1);
        mainShader.setUniformi("u_fillTexture", 2);
        mainShader.setUniformi("u_blurTexture", 3);

        blurShader = new Shader(
            Resources.readString(Resources.SHADER_BLUR_VERT),
            Resources.readString(Resources.SHADER_BLUR_FRAG)
        );
        shadersLoaded = true;
    }


    // ─── Screen capture (shared) ───

    private static FrameBuffer captureFrameBuffer;


    public static Texture captureScreen() {
        int width = Math.max(Core.graphics.getWidth() / 2, 2);
        int height = Math.max(Core.graphics.getHeight() / 2, 2);

        if (captureFrameBuffer == null
            || captureFrameBuffer.getWidth() != width
            || captureFrameBuffer.getHeight() != height) {
            if (captureFrameBuffer != null) captureFrameBuffer.dispose();
            captureFrameBuffer = new FrameBuffer(width, height);
        }

        Draw.flush();
        Gl.bindTexture(Gl.texture2d, captureFrameBuffer.getTexture().getTextureObjectHandle());
        Gl.copyTexSubImage2D(Gl.texture2d, 0, 0, 0, 0, 0, width, height);
        Gl.bindTexture(Gl.texture2d, 0);
        return captureFrameBuffer.getTexture();
    }


    public static void disposeCapture() {
        if (captureFrameBuffer == null) return;
        captureFrameBuffer.dispose();
        captureFrameBuffer = null;
    }


    // ─── Per-instance framebuffers (glass blur) ───

    private FrameBuffer blurFrameBufferA;
    private FrameBuffer blurFrameBufferB;


    // ─── Render state ───

    private int fillMode;
    private final Color fillColor = new Color(Color.darkGray);
    private float topLeftRadius;
    private float topRightRadius;
    private float bottomRightRadius;
    private float bottomLeftRadius;

    private Gradient currentGradient;

    private Texture fillTexture;
    private float uvScaleX = 1f;
    private float uvScaleY = 1f;
    private float uvOffsetX;
    private float uvOffsetY;

    private float borderWidth;
    private final Color borderColor = new Color(Color.white);
    private int borderStyle;
    private float dashLength = 10f;
    private float dashRatio = 0.5f;

    private final Color innerShadowColor = new Color(0, 0, 0, 0);
    private float innerShadowSpread;
    private float innerShadowBlur;

    private final Color outerGlowColor = new Color(0, 0, 0, 0);
    private float outerGlowSpread;

    private float opacity = 1f;
    private int filterMode;
    private float filterAmount;
    private float noiseAmount;

    private Texture blurTexture;
    private float elementBoundsX;
    private float elementBoundsY;
    private float elementBoundsWidth;
    private float elementBoundsHeight;
    private float blendMode;


    // ─── State reset ───

    /**
     * Resets all render-state fields to safe defaults.
     */
    private void resetState() {
        fillMode = 0;
        fillColor.set(Color.darkGray);
        topLeftRadius = topRightRadius = bottomRightRadius = bottomLeftRadius = 0f;
        currentGradient = null;
        fillTexture = null;
        uvScaleX = uvScaleY = 1f;
        uvOffsetX = uvOffsetY = 0f;
        borderWidth = 0f;
        borderColor.set(Color.white);
        borderStyle = 0;
        dashLength = 10f;
        dashRatio = 0.5f;
        innerShadowColor.set(0, 0, 0, 0);
        innerShadowSpread = innerShadowBlur = 0f;
        outerGlowColor.set(0, 0, 0, 0);
        outerGlowSpread = 0f;
        opacity = 1f;
        filterMode = 0;
        filterAmount = noiseAmount = 0f;
        blurTexture = null;
        elementBoundsX = elementBoundsY = elementBoundsWidth = elementBoundsHeight = 0f;
        blendMode = 0f;
    }


    // ─── Public draw API ───

    public void draw(float x, float y, float width, float height, CustomComponent.Style s) {
        if (width <= 0f || height <= 0f) return;
        ensureShaders();
        resetState();

        // Glass: setup blur pass if background is GLASS
        if (s.backgroundMode == CustomComponent.Style.BackgroundMode.GLASS && s.glassIterations > 0) {
            setupGlass(x, y, width, height, s.glassIterations, s.glassBlend);
        } else {
            blurTexture = null;
            blendMode = 0f;
        }

        // Map Style -> shader uniforms
        fillMode = switch (s.backgroundMode) {
            case GRADIENT -> 1;
            case TEXTURE  -> 2;
            default       -> 0;
        };
        fillColor.set(s.fillColor);
        currentGradient = s.gradient;
        fillTexture = s.fillTexture;
        uvScaleX = s.uvScaleX;
        uvScaleY = s.uvScaleY;
        uvOffsetX = s.uvOffsetX;
        uvOffsetY = s.uvOffsetY;

        topLeftRadius = s.topLeftRadius;
        topRightRadius = s.topRightRadius;
        bottomRightRadius = s.bottomRightRadius;
        bottomLeftRadius = s.bottomLeftRadius;

        borderWidth = s.borderWidth;
        borderColor.set(s.borderColor);
        borderStyle = s.borderStyle;
        dashLength = s.dashLength;
        dashRatio = s.dashRatio;

        innerShadowSpread = s.innerShadowSpread;
        innerShadowBlur = s.innerShadowBlur;
        innerShadowColor.set(s.innerShadowColor);

        outerGlowSpread = s.glowSpread;
        outerGlowColor.set(s.glowColor);

        opacity = s.opacity;

        render(x, y, width, height);
    }


    // ─── Lifecycle ───

    @Override
    public void dispose() {
        if (blurFrameBufferA != null) {
            blurFrameBufferA.dispose();
            blurFrameBufferA = null;
        }
        if (blurFrameBufferB != null) {
            blurFrameBufferB.dispose();
            blurFrameBufferB = null;
        }
    }


    public static void disposeShared() {
        if (mainShader != null) {
            mainShader.dispose();
            mainShader = null;
        }
        if (blurShader != null) {
            blurShader.dispose();
            blurShader = null;
        }
        shadersLoaded = false;
    }


    // ─── Internal rendering ───

    private void setupGlass(float x, float y, float width, float height, int iterations, float blend) {
        Texture captureTexture = captureScreen();
        int fboWidth = Math.max((int) (width * 0.5f), 2);
        int fboHeight = Math.max((int) (height * 0.5f), 2);
        ensureBlurFrameBuffers(fboWidth, fboHeight);

        float screenWidth = Core.graphics.getWidth();
        float screenHeight = Core.graphics.getHeight();
        float u1 = x / screenWidth;
        float v1 = y / screenHeight;
        float u2 = (x + width) / screenWidth;
        float v2 = (y + height) / screenHeight;

        blurFrameBufferA.begin();
        Draw.color(Color.white);
        float packedColor = Draw.getColor().toFloatBits();
        Fill.quad(captureTexture,
            0, 0, packedColor, u1, v1,
            fboWidth, 0, packedColor, u2, v1,
            fboWidth, fboHeight, packedColor, u2, v2,
            0, fboHeight, packedColor, u1, v2);
        Draw.flush();
        blurFrameBufferA.end();

        doBlur(fboWidth, fboHeight, iterations);

        elementBoundsX = u1;
        elementBoundsY = v1;
        elementBoundsWidth = u2 - u1;
        elementBoundsHeight = v2 - v1;
        blurTexture = blurFrameBufferA.getTexture();
        blendMode = blend;
    }


    private void render(float x, float y, float width, float height) {
        if (width <= 0f || height <= 0f) return;
        ensureShaders();

        Draw.flush();
        Shader previousShader = Draw.getShader();
        Draw.shader(mainShader);
        mainShader.bind();

        mainShader.setUniformi("u_gradientTex", 1);
        mainShader.setUniformi("u_fillTexture", 2);
        mainShader.setUniformi("u_blurTexture", 3);

        mainShader.setUniformf("u_cornerRadii", topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius);
        mainShader.setUniformf("u_size", width, height);
        mainShader.setUniformf("u_edgeSoftness", 1.0f);

        mainShader.setUniformf("u_fillColor", fillColor.r, fillColor.g, fillColor.b, fillColor.a);
        mainShader.setUniformf("u_fillMode", fillMode);

        if (fillMode == 1 && currentGradient != null) {
            float[] gradientParams = currentGradient.getParams();
            mainShader.setUniformf("u_gradientParams",
                gradientParams[0], gradientParams[1], gradientParams[2], gradientParams[3]);
        } else {
            mainShader.setUniformf("u_gradientParams", 0f, 0f, 0f, 1f);
        }

        mainShader.setUniformf("u_uvScale", uvScaleX, uvScaleY);
        mainShader.setUniformf("u_uvOffset", uvOffsetX, uvOffsetY);

        mainShader.setUniformf("u_borderWidth", Math.min(borderWidth, Math.min(width, height) * 0.5f));
        mainShader.setUniformf("u_borderColor", borderColor.r, borderColor.g, borderColor.b, borderColor.a);
        mainShader.setUniformf("u_borderStyle", borderStyle);
        mainShader.setUniformf("u_dashLength", dashLength);
        mainShader.setUniformf("u_dashRatio", dashRatio);

        mainShader.setUniformf("u_innerShadowColor",
            innerShadowColor.r, innerShadowColor.g, innerShadowColor.b, innerShadowColor.a);
        mainShader.setUniformf("u_innerShadowSpread", innerShadowSpread);
        mainShader.setUniformf("u_innerShadowBlur", innerShadowBlur);

        mainShader.setUniformf("u_outerGlowColor",
            outerGlowColor.r, outerGlowColor.g, outerGlowColor.b, outerGlowColor.a);
        mainShader.setUniformf("u_outerGlowSpread", outerGlowSpread);

        mainShader.setUniformf("u_opacity", opacity);
        mainShader.setUniformf("u_colorFilter", filterMode, filterAmount, 0f, 0f);
        mainShader.setUniformf("u_noiseAmount", noiseAmount);

        mainShader.setUniformf("u_elementBounds",
            elementBoundsX, elementBoundsY, elementBoundsWidth, elementBoundsHeight);
        mainShader.setUniformf("u_blendMode", blendMode);

        if (fillMode == 1 && currentGradient != null) currentGradient.getTexture().bind(1);
        if (fillMode == 2 && fillTexture != null) fillTexture.bind(2);
        if (blendMode > 0.001f && blurTexture != null) blurTexture.bind(3);

        Gl.activeTexture(Gl.texture0);

        Draw.color(Color.white);
        float packedColor = Draw.getColor().toFloatBits();
        Fill.quad(Core.atlas.white().texture,
            x, y, packedColor, 0f, 0f,
            x + width, y, packedColor, 1f, 0f,
            x + width, y + height, packedColor, 1f, 1f,
            x, y + height, packedColor, 0f, 1f);

        Draw.flush();
        Draw.shader(previousShader);
    }


    private void ensureBlurFrameBuffers(int width, int height) {
        if (blurFrameBufferA != null
            && blurFrameBufferA.getWidth() == width
            && blurFrameBufferA.getHeight() == height) return;
        if (blurFrameBufferA != null) blurFrameBufferA.dispose();
        if (blurFrameBufferB != null) blurFrameBufferB.dispose();
        blurFrameBufferA = new FrameBuffer(width, height);
        blurFrameBufferB = new FrameBuffer(width, height);
    }


    private void doBlur(int fboWidth, int fboHeight, int iterations) {
        float texelSizeU = 1f / fboWidth;
        float texelSizeV = 1f / fboHeight;

        blurShader.bind();
        blurShader.setUniformf("u_texelSize", texelSizeU, texelSizeV);

        for (int i = 0; i < iterations; i++) {
            blurShader.setUniformf("u_offset", i * 0.5f + 0.5f);

            blurFrameBufferB.begin();
            Draw.color(Color.white);
            Draw.blit(blurFrameBufferA.getTexture(), blurShader);
            Draw.flush();
            blurFrameBufferB.end();

            FrameBuffer tmp = blurFrameBufferA;
            blurFrameBufferA = blurFrameBufferB;
            blurFrameBufferB = tmp;
        }
    }
}
