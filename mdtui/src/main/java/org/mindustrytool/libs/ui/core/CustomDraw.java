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
 * backdrop-filter blur.
 *
 * <p><b>Lifecycle:</b> Shaders and the screen-capture framebuffer are shared across all
 * instances ({@code static}) and should be released via {@link #disposeShared()} and {@link #disposeCapture()}.
 * Per-instance framebuffers are used for the backdrop blur effect and are released via {@link #dispose()}.
 */
public class CustomDraw implements Disposable {

    private static Shader mainShader;
    private static Shader blurShader;
    private static boolean shadersLoaded;
    private static FrameBuffer captureFrameBuffer;

    /**
     * Ensures that the shared custom element shader and blur shader are loaded.
     */
    private static void ensureShaders() {
        if (shadersLoaded) return;

        mainShader = new Shader(
            Resources.readString(Resources.SHADER_CUSTOM_ELEMENT_VERT),
            Resources.readString(Resources.SHADER_CUSTOM_ELEMENT_FRAG)
        );

        mainShader.bind();
        mainShader.setUniformi("u_gradientTex0", 1);
        mainShader.setUniformi("u_fillTexture", 2);
        mainShader.setUniformi("u_backdropTex", 3);
        mainShader.setUniformi("u_gradientTex1", 4);
        mainShader.setUniformi("u_gradientTex2", 5);
        mainShader.setUniformi("u_gradientTex3", 6);

        blurShader = new Shader(
            Resources.readString(Resources.SHADER_BLUR_VERT),
            Resources.readString(Resources.SHADER_BLUR_FRAG)
        );

        shadersLoaded = true;
    }

    /**
     * Captures the current screen contents to a downscaled FrameBuffer.
     * Used as a backdrop source for the glass/blur effect.
     *
     * @return the captured screen background texture
     */
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

    /**
     * Disposes the shared screen-capture framebuffer.
     */
    public static void disposeCapture() {
        if (captureFrameBuffer == null) return;
        captureFrameBuffer.dispose();
        captureFrameBuffer = null;
    }

    /**
     * Disposes all shared static shader resources.
     */
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


    private FrameBuffer blurFrameBufferA;
    private FrameBuffer blurFrameBufferB;

    /**
     * Draws the specified custom element shape and styling.
     * Sets up backdrop blur framebuffers if needed, and triggers the shader rendering.
     *
     * @param x      the X coordinate of the element
     * @param y      the Y coordinate of the element
     * @param width  the width of the element
     * @param height the height of the element
     * @param s      the visual style configuration
     */
    public void draw(float x, float y, float width, float height, CustomComponent.Style s) {
        if (width <= 0f || height <= 0f) return;
        ensureShaders();

        Texture backdropTexture = null;
        float bdX = 0f, bdY = 0f, bdW = 0f, bdH = 0f;

        if (s.backgroundMode == CustomComponent.Style.BackgroundMode.BACKDROP && s.backdropIterations > 0) {
            Texture captureTexture = captureScreen();
            int fboWidth = Math.max((int) (width * 0.5f), 2);
            int fboHeight = Math.max((int) (height * 0.5f), 2);
            ensureBlurFrameBuffers(fboWidth, fboHeight);

            float screenWidth = Core.graphics.getWidth();
            float screenHeight = Core.graphics.getHeight();
            bdX = x / screenWidth;
            bdY = y / screenHeight;
            float u2 = (x + width) / screenWidth;
            float v2 = (y + height) / screenHeight;
            bdW = u2 - bdX;
            bdH = v2 - bdY;

            blurFrameBufferA.begin();
            Draw.color(Color.white);
            float packedColor = Draw.getColor().toFloatBits();
            Fill.quad(captureTexture,
                0, 0, packedColor, bdX, bdY,
                fboWidth, 0, packedColor, u2, bdY,
                fboWidth, fboHeight, packedColor, u2, v2,
                0, fboHeight, packedColor, bdX, v2);
            Draw.flush();
            blurFrameBufferA.end();

            doBlur(fboWidth, fboHeight, s.backdropIterations);
            backdropTexture = blurFrameBufferA.getTexture();
        }

        render(x, y, width, height, s, backdropTexture, bdX, bdY, bdW, bdH);
    }

    /**
     * Disposes the per-instance temporary framebuffers.
     */
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

    /**
     * Binds uniforms to the custom element shader and renders a textured quad.
     * Uses lazy/conditional uniform binding to minimize OpenGL state changes.
     */
    private void render(float x, float y, float width, float height, CustomComponent.Style s,
                        Texture backdropTexture, float bdX, float bdY, float bdW, float bdH) {
        if (width <= 0f || height <= 0f) return;
        ensureShaders();

        Draw.flush();
        Shader previousShader = Draw.getShader();
        Draw.shader(mainShader);
        mainShader.bind();

        mainShader.setUniformf("u_size", width, height);
        mainShader.setUniformf("u_cornerRadii", s.topLeftRadius, s.topRightRadius, s.bottomRightRadius, s.bottomLeftRadius);
        mainShader.setUniformf("u_edgeSoftness", 1.0f);
        mainShader.setUniformf("u_opacity", s.opacity);

        int fillMode = switch (s.backgroundMode) {
            case GRADIENT -> 1;
            case TEXTURE  -> 2;
            default       -> 0;
        };
        mainShader.setUniformf("u_fillMode", fillMode);
        mainShader.setUniformf("u_fillColor", s.fillColor.r, s.fillColor.g, s.fillColor.b, s.fillColor.a);

        if (fillMode == 1) {
            int count = 0;
            if (s.gradient0 != null) {
                bindGradient(count, s.gradient0);
                count++;
            }
            if (s.gradient0 != null && s.gradient1 != null) {
                bindGradient(count, s.gradient1);
                count++;
            }
            if (s.gradient0 != null && s.gradient1 != null && s.gradient2 != null) {
                bindGradient(count, s.gradient2);
                count++;
            }
            if (s.gradient0 != null && s.gradient1 != null && s.gradient2 != null && s.gradient3 != null) {
                bindGradient(count, s.gradient3);
                count++;
            }
            mainShader.setUniformf("u_gradientCount", count);
        } else if (fillMode == 2 && s.fillTexture != null) {
            mainShader.setUniformf("u_uvScale", s.uvScaleX, s.uvScaleY);
            mainShader.setUniformf("u_uvOffset", s.uvOffsetX, s.uvOffsetY);
            s.fillTexture.bind(2);
        }

        mainShader.setUniformf("u_borderWidth", Math.min(s.borderWidth, Math.min(width, height) * 0.5f));
        if (s.borderWidth > 0.001f) {
            mainShader.setUniformf("u_borderColor", s.borderColor.r, s.borderColor.g, s.borderColor.b, s.borderColor.a);
            mainShader.setUniformf("u_borderStyle", s.borderStyle);
            if (s.borderStyle > 0) {
                mainShader.setUniformf("u_dashLength", s.dashLength);
                mainShader.setUniformf("u_dashRatio", s.dashRatio);
            }
        }

        if (s.innerShadowColor.a > 0.001f && (s.innerShadowSpread > 0f || s.innerShadowBlur > 0f)) {
            mainShader.setUniformf("u_innerShadowColor", s.innerShadowColor.r, s.innerShadowColor.g, s.innerShadowColor.b, s.innerShadowColor.a);
            mainShader.setUniformf("u_innerShadowSpread", s.innerShadowSpread);
            mainShader.setUniformf("u_innerShadowBlur", s.innerShadowBlur);
        } else {
            mainShader.setUniformf("u_innerShadowColor", 0f, 0f, 0f, 0f);
        }

        if (s.glowColor.a > 0.001f && s.glowSpread > 0f) {
            mainShader.setUniformf("u_outerGlowColor", s.glowColor.r, s.glowColor.g, s.glowColor.b, s.glowColor.a);
            mainShader.setUniformf("u_outerGlowSpread", s.glowSpread);
        } else {
            mainShader.setUniformf("u_outerGlowSpread", 0f);
        }

        boolean hasBackdrop = s.backgroundMode == CustomComponent.Style.BackgroundMode.BACKDROP
                           && s.backdropIterations > 0 && backdropTexture != null;
        mainShader.setUniformf("u_backdropWeight", hasBackdrop ? s.backdropWeight : 0f);
        if (hasBackdrop) {
            mainShader.setUniformf("u_backdropCoords", bdX, bdY, bdW, bdH);
            mainShader.setUniformf("u_backdropBlend", s.backdropBlend);
            mainShader.setUniformf("u_backdropMinAlpha", s.backdropMinAlpha);
            backdropTexture.bind(3);
        }

        if (s.filterMode > 0 && s.filterAmount > 0.001f) {
            mainShader.setUniformf("u_colorFilter", s.filterMode, s.filterAmount, 0f, 0f);
        } else {
            mainShader.setUniformf("u_colorFilter", 0f, 0f, 0f, 0f);
        }
        mainShader.setUniformf("u_noiseAmount", s.noiseAmount);

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

    /**
     * Ensures that the per-instance blur framebuffers are matching the requested size.
     */
    private void ensureBlurFrameBuffers(int width, int height) {
        if (blurFrameBufferA != null
            && blurFrameBufferA.getWidth() == width
            && blurFrameBufferA.getHeight() == height) return;
        if (blurFrameBufferA != null) blurFrameBufferA.dispose();
        if (blurFrameBufferB != null) blurFrameBufferB.dispose();
        blurFrameBufferA = new FrameBuffer(width, height);
        blurFrameBufferB = new FrameBuffer(width, height);
    }

    /**
     * Performs a multi-iteration ping-pong Gaussian blur horizontally and vertically
     * using the two temporary framebuffers.
     */
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

    /**
     * Binds a single gradient texture and its parameters to the active shader unit.
     */
    private void bindGradient(int index, Gradient g) {
        float[] params = g.params();
        mainShader.setUniformf("u_gradientParams" + index, params[0], params[1], params[2], params[3]);
        g.texture().bind(1 + (index == 0 ? 0 : 2 + index));
    }
}
