package org.mindustrytool.libs.ui.components;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Gl;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.Shader;
import arc.util.Disposable;

import mindustry.Vars;

public class CustomDraw implements Disposable {

    private static Shader mainShader;
    private static Shader blurShader;
    private static boolean shadersLoaded;
    private static FrameBuffer captureFrameBuffer;

    private static void ensureShaders() {
        if (shadersLoaded) return;

        mainShader = new Shader(
            Vars.tree.get("shaders/custom_element.vert").readString(),
            Vars.tree.get("shaders/custom_element.frag").readString()
        );

        mainShader.bind();
        mainShader.setUniformi("u_gradientTex0", 1);
        mainShader.setUniformi("u_fillTexture", 2);
        mainShader.setUniformi("u_backdropTex", 3);
        mainShader.setUniformi("u_gradientTex1", 4);
        mainShader.setUniformi("u_gradientTex2", 5);
        mainShader.setUniformi("u_gradientTex3", 6);

        blurShader = new Shader(
            Vars.tree.get("shaders/blur.vert").readString(),
            Vars.tree.get("shaders/blur.frag").readString()
        );

        shadersLoaded = true;
    }

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

    public void draw(float x, float y, float width, float height, CustomWidget w) {
        if (width <= 0f || height <= 0f) return;
        ensureShaders();

        Texture backdropTexture = null;
        float bdX = 0f, bdY = 0f, bdW = 0f, bdH = 0f;

        if (w.backgroundMode() == CustomWidget.BackgroundMode.BACKDROP && w.backdropIterations() > 0) {
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

            doBlur(fboWidth, fboHeight, w.backdropIterations());
            backdropTexture = blurFrameBufferA.getTexture();
        }

        render(x, y, width, height, w, backdropTexture, bdX, bdY, bdW, bdH);
    }

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

    private void render(float x, float y, float width, float height, CustomWidget w,
                        Texture backdropTexture, float bdX, float bdY, float bdW, float bdH) {
        if (width <= 0f || height <= 0f) return;
        ensureShaders();

        Draw.flush();
        Shader previousShader = Draw.getShader();
        Draw.shader(mainShader);
        mainShader.bind();

        Color fc = w.fillColor() != null ? w.fillColor() : Color.darkGray;

        mainShader.setUniformf("u_size", width, height);
        mainShader.setUniformf("u_cornerRadii", w.topLeftRadius(), w.topRightRadius(), w.bottomRightRadius(), w.bottomLeftRadius());
        mainShader.setUniformf("u_edgeSoftness", 1.0f);
        mainShader.setUniformf("u_opacity", w.opacity());

        int fillMode = switch (w.backgroundMode()) {
            case GRADIENT -> 1;
            case TEXTURE  -> 2;
            default       -> 0;
        };
        mainShader.setUniformf("u_fillMode", fillMode);
        mainShader.setUniformf("u_fillColor", fc.r, fc.g, fc.b, fc.a);

        if (fillMode == 1) {
            int count = 0;
            if (w.gradient0() != null) {
                bindGradient(count, w.gradient0());
                count++;
            }
            if (w.gradient0() != null && w.gradient1() != null) {
                bindGradient(count, w.gradient1());
                count++;
            }
            if (w.gradient0() != null && w.gradient1() != null && w.gradient2() != null) {
                bindGradient(count, w.gradient2());
                count++;
            }
            if (w.gradient0() != null && w.gradient1() != null && w.gradient2() != null && w.gradient3() != null) {
                bindGradient(count, w.gradient3());
                count++;
            }
            mainShader.setUniformf("u_gradientCount", count);
        } else if (fillMode == 2 && w.fillTexture() != null) {
            mainShader.setUniformf("u_uvScale", w.uvScaleX(), w.uvScaleY());
            mainShader.setUniformf("u_uvOffset", w.uvOffsetX(), w.uvOffsetY());
            w.fillTexture().bind(2);
        }

        mainShader.setUniformf("u_borderWidth", Math.min(w.borderWidth(), Math.min(width, height) * 0.5f));
        if (w.borderWidth() > 0.001f) {
            Color bc = w.borderColor() != null ? w.borderColor() : Color.white;
            mainShader.setUniformf("u_borderColor", bc.r, bc.g, bc.b, bc.a);
            mainShader.setUniformf("u_borderStyle", w.borderStyle());
            if (w.borderStyle() > 0) {
                mainShader.setUniformf("u_dashLength", w.dashLength());
                mainShader.setUniformf("u_dashRatio", w.dashRatio());
            }
        }

        Color isc = w.innerShadowColor() != null ? w.innerShadowColor() : Color.clear;
        if (isc.a > 0.001f && (w.innerShadowSpread() > 0f || w.innerShadowBlur() > 0f)) {
            mainShader.setUniformf("u_innerShadowColor", isc.r, isc.g, isc.b, isc.a);
            mainShader.setUniformf("u_innerShadowSpread", w.innerShadowSpread());
            mainShader.setUniformf("u_innerShadowBlur", w.innerShadowBlur());
        } else {
            mainShader.setUniformf("u_innerShadowColor", 0f, 0f, 0f, 0f);
        }

        Color gc = w.glowColor() != null ? w.glowColor() : Color.clear;
        if (gc.a > 0.001f && w.glowSpread() > 0f) {
            mainShader.setUniformf("u_outerGlowColor", gc.r, gc.g, gc.b, gc.a);
            mainShader.setUniformf("u_outerGlowSpread", w.glowSpread());
        } else {
            mainShader.setUniformf("u_outerGlowSpread", 0f);
        }

        boolean hasBackdrop = w.backgroundMode() == CustomWidget.BackgroundMode.BACKDROP
                           && w.backdropIterations() > 0 && backdropTexture != null;
        mainShader.setUniformf("u_backdropWeight", hasBackdrop ? w.backdropWeight() : 0f);
        if (hasBackdrop) {
            mainShader.setUniformf("u_backdropCoords", bdX, bdY, bdW, bdH);
            mainShader.setUniformf("u_backdropBlend", w.backdropBlend());
            mainShader.setUniformf("u_backdropMinAlpha", w.backdropMinAlpha());
            backdropTexture.bind(3);
        }

        if (w.filterMode() > 0 && w.filterAmount() > 0.001f) {
            mainShader.setUniformf("u_colorFilter", w.filterMode(), w.filterAmount(), 0f, 0f);
        } else {
            mainShader.setUniformf("u_colorFilter", 0f, 0f, 0f, 0f);
        }
        mainShader.setUniformf("u_noiseAmount", w.noiseAmount());

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

    private void bindGradient(int index, Gradient g) {
        float[] params = g.params();
        mainShader.setUniformf("u_gradientParams" + index, params[0], params[1], params[2], params[3]);
        g.texture().bind(1 + (index == 0 ? 0 : 2 + index));
    }
}
