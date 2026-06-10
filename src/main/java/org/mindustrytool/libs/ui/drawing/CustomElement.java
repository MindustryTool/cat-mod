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
import org.mindustrytool.util.Resources;

/**
 * CustomElement is the low-level rendering engine using custom OpenGL shaders
 * and SDF (Signed Distance Fields) to draw premium visual effects.
 */
public class CustomElement implements Disposable {

    private static Shader mainShader;
    private static Shader blurShader;
    private static boolean shadersLoaded;

    private static void ensureShaders() {
        if (shadersLoaded) {
            return;
        }
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

    // ─── Screen Capture (shared across instances) ───
    private static FrameBuffer captureFrameBuffer;

    public static Texture captureScreen() {
        int width = Math.max(Core.graphics.getWidth() / 2, 2);
        int height = Math.max(Core.graphics.getHeight() / 2, 2);
        if (captureFrameBuffer == null || captureFrameBuffer.getWidth() != width || captureFrameBuffer.getHeight() != height) {
            if (captureFrameBuffer != null) {
                captureFrameBuffer.dispose();
            }
            captureFrameBuffer = new FrameBuffer(width, height);
        }
        Draw.flush();
        Gl.bindTexture(Gl.texture2d, captureFrameBuffer.getTexture().getTextureObjectHandle());
        Gl.copyTexSubImage2D(Gl.texture2d, 0, 0, 0, 0, 0, width, height);
        Gl.bindTexture(Gl.texture2d, 0);
        return captureFrameBuffer.getTexture();
    }

    public static void disposeCapture() {
        if (captureFrameBuffer != null) {
            captureFrameBuffer.dispose();
            captureFrameBuffer = null;
        }
    }

    // ─── Per-Instance FBOs (for glass blur) ───
    private FrameBuffer blurFrameBufferA;
    private FrameBuffer blurFrameBufferB;
    private int lastFboWidth;
    private int lastFboHeight;

    // ─── Render State ───
    private float topLeftRadius;
    private float topRightRadius;
    private float bottomRightRadius;
    private float bottomLeftRadius;
    private int fillMode;
    private final Color fillColor = new Color(Color.darkGray);
    private Texture gradientTexture;
    private float gradientDx;
    private float gradientDy;
    private float gradientType;
    private float gradientRepeat;
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
    private Texture fallbackTexture;

    // ─── Public API ───

    public void fill(float xPosition, float yPosition, float width, float height, float radius, Color color) {
        this.fillMode = 0;
        this.fillColor.set(color);
        this.topLeftRadius = this.topRightRadius = this.bottomRightRadius = this.bottomLeftRadius = radius;
        render(xPosition, yPosition, width, height);
    }

    public void fill(float xPosition, float yPosition, float width, float height, float topLeft, float topRight, float bottomRight, float bottomLeft, Color color) {
        this.fillMode = 0;
        this.fillColor.set(color);
        this.topLeftRadius = topLeft;
        this.topRightRadius = topRight;
        this.bottomRightRadius = bottomRight;
        this.bottomLeftRadius = bottomLeft;
        render(xPosition, yPosition, width, height);
    }

    public void fillGradient(float xPosition, float yPosition, float width, float height, float radius, Gradient gradient) {
        this.fillMode = 1;
        this.fillColor.set(Color.white);
        applyGradient(gradient);
        this.topLeftRadius = this.topRightRadius = this.bottomRightRadius = this.bottomLeftRadius = radius;
        render(xPosition, yPosition, width, height);
    }

    public void fillGradient(float xPosition, float yPosition, float width, float height, float topLeft, float topRight, float bottomRight, float bottomLeft, Gradient gradient) {
        this.fillMode = 1;
        this.fillColor.set(Color.white);
        applyGradient(gradient);
        this.topLeftRadius = topLeft;
        this.topRightRadius = topRight;
        this.bottomRightRadius = bottomRight;
        this.bottomLeftRadius = bottomLeft;
        render(xPosition, yPosition, width, height);
    }

    public void fillTexture(float xPosition, float yPosition, float width, float height, float radius, Texture texture, Color tint,
                            float scaleX, float scaleY, float offsetX, float offsetY) {
        this.fillMode = 2;
        this.fillColor.set(tint);
        this.fillTexture = texture;
        this.uvScaleX = scaleX;
        this.uvScaleY = scaleY;
        this.uvOffsetX = offsetX;
        this.uvOffsetY = offsetY;
        this.topLeftRadius = this.topRightRadius = this.bottomRightRadius = this.bottomLeftRadius = radius;
        render(xPosition, yPosition, width, height);
    }

    public void fillTexture(float xPosition, float yPosition, float width, float height,
                            float topLeft, float topRight, float bottomRight, float bottomLeft,
                            Texture texture, Color tint,
                            float scaleX, float scaleY, float offsetX, float offsetY) {
        this.fillMode = 2;
        this.fillColor.set(tint);
        this.fillTexture = texture;
        this.uvScaleX = scaleX;
        this.uvScaleY = scaleY;
        this.uvOffsetX = offsetX;
        this.uvOffsetY = offsetY;
        this.topLeftRadius = topLeft;
        this.topRightRadius = topRight;
        this.bottomRightRadius = bottomRight;
        this.bottomLeftRadius = bottomLeft;
        render(xPosition, yPosition, width, height);
    }

    public void loadImage(float xPosition, float yPosition, float width, float height, float radius, String url, Color tint,
                          float scaleX, float scaleY, float offsetX, float offsetY) {
        Texture texture = ImageLoader.get(url);
        if (texture != null) {
            fillTexture(xPosition, yPosition, width, height, radius, texture, tint, scaleX, scaleY, offsetX, offsetY);
            return;
        }
        if (fallbackTexture != null) {
            fillTexture(xPosition, yPosition, width, height, radius, fallbackTexture, tint, scaleX, scaleY, offsetX, offsetY);
        }
        ImageLoader.load(url, loadedTexture -> {
            if (loadedTexture != null) {
                fillTexture(xPosition, yPosition, width, height, radius, loadedTexture, tint, scaleX, scaleY, offsetX, offsetY);
            }
        });
    }

    public void loadImage(float xPosition, float yPosition, float width, float height, float radius, String url) {
        loadImage(xPosition, yPosition, width, height, radius, url, Color.white, 1f, 1f, 0f, 0f);
    }

    public void fallback(Texture texture) {
        this.fallbackTexture = texture;
    }

    public void fillWithBorder(float xPosition, float yPosition, float width, float height, float radius,
                               Color fill, float borderWidth, Color borderColor) {
        this.fillMode = 0;
        this.fillColor.set(fill);
        this.borderWidth = borderWidth;
        this.borderColor.set(borderColor);
        this.borderStyle = 0;
        this.topLeftRadius = this.topRightRadius = this.bottomRightRadius = this.bottomLeftRadius = radius;
        render(xPosition, yPosition, width, height);
    }

    public void fillWithBorder(float xPosition, float yPosition, float width, float height,
                               float topLeft, float topRight, float bottomRight, float bottomLeft,
                               Color fill, float borderWidth, Color borderColor) {
        this.fillMode = 0;
        this.fillColor.set(fill);
        this.borderWidth = borderWidth;
        this.borderColor.set(borderColor);
        this.borderStyle = 0;
        this.topLeftRadius = topLeft;
        this.topRightRadius = topRight;
        this.bottomRightRadius = bottomRight;
        this.bottomLeftRadius = bottomLeft;
        render(xPosition, yPosition, width, height);
    }

    public void fillGradientWithBorder(float xPosition, float yPosition, float width, float height, float radius,
                                       Gradient gradient, float borderWidth, Color borderColor) {
        this.fillMode = 1;
        this.fillColor.set(Color.white);
        applyGradient(gradient);
        this.borderWidth = borderWidth;
        this.borderColor.set(borderColor);
        this.borderStyle = 0;
        this.topLeftRadius = this.topRightRadius = this.bottomRightRadius = this.bottomLeftRadius = radius;
        render(xPosition, yPosition, width, height);
    }

    public void fillDashedBorder(float xPosition, float yPosition, float width, float height, float radius,
                                 Color fill, float borderWidth, Color borderColor,
                                 float dashLength, float dashRatio) {
        this.fillMode = 0;
        this.fillColor.set(fill);
        this.borderWidth = borderWidth;
        this.borderColor.set(borderColor);
        this.borderStyle = 1;
        this.dashLength = dashLength;
        this.dashRatio = dashRatio;
        this.topLeftRadius = this.topRightRadius = this.bottomRightRadius = this.bottomLeftRadius = radius;
        render(xPosition, yPosition, width, height);
    }

    public void fillWithShadow(float xPosition, float yPosition, float width, float height, float radius,
                               Color fill, float shadowSpread, float shadowBlur, Color shadowColor) {
        this.fillMode = 0;
        this.fillColor.set(fill);
        this.innerShadowSpread = shadowSpread;
        this.innerShadowBlur = shadowBlur;
        this.innerShadowColor.set(shadowColor);
        this.topLeftRadius = this.topRightRadius = this.bottomRightRadius = this.bottomLeftRadius = radius;
        render(xPosition, yPosition, width, height);
    }

    public void glass(float xPosition, float yPosition, float width, float height, float radius,
                      Color tint, int blurIterations,
                      float borderWidth, Color borderColor) {
        this.fillMode = 0;
        this.fillColor.set(tint);
        this.borderWidth = borderWidth;
        this.borderColor.set(borderColor);
        this.borderStyle = 0;
        this.blendMode = 0.8f;
        this.topLeftRadius = this.topRightRadius = this.bottomRightRadius = this.bottomLeftRadius = radius;

        Texture captureTexture = captureScreen();
        int fboWidth = Math.max((int) (width * 0.5f), 2);
        int fboHeight = Math.max((int) (height * 0.5f), 2);
        ensureBlurFrameBuffers(fboWidth, fboHeight);

        float screenWidth = Core.graphics.getWidth();
        float screenHeight = Core.graphics.getHeight();
        float u1 = xPosition / screenWidth;
        float v1 = yPosition / screenHeight;
        float u2 = (xPosition + width) / screenWidth;
        float v2 = (yPosition + height) / screenHeight;

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

        doBlur(fboWidth, fboHeight, blurIterations);

        this.elementBoundsX = u1;
        this.elementBoundsY = v1;
        this.elementBoundsWidth = u2 - u1;
        this.elementBoundsHeight = v2 - v1;
        this.blurTexture = blurFrameBufferA.getTexture();

        render(xPosition, yPosition, width, height);
    }

    public void glass(float xPosition, float yPosition, float width, float height, float radius,
                      Color tint, int blurIterations) {
        glass(xPosition, yPosition, width, height, radius, tint, blurIterations, 0f, Color.white);
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

    // ─── Internal ───

    private void applyGradient(Gradient gradient) {
        this.gradientTexture = gradient.getTexture();
        float[] parameters = gradient.getParams();
        this.gradientDx = parameters[0];
        this.gradientDy = parameters[1];
        this.gradientType = parameters[2];
        this.gradientRepeat = parameters[3];
    }

    private void render(float xPosition, float yPosition, float width, float height) {
        if (width <= 0f || height <= 0f) {
            return;
        }
        ensureShaders();

        Draw.flush();
        Shader previousShader = Draw.getShader();
        Draw.shader(mainShader);
        mainShader.bind();

        mainShader.setUniformf("u_cornerRadii", topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius);
        mainShader.setUniformf("u_size", width, height);
        mainShader.setUniformf("u_edgeSoftness", 1.0f);

        mainShader.setUniformf("u_fillColor", fillColor.r, fillColor.g, fillColor.b, fillColor.a);
        mainShader.setUniformf("u_fillMode", fillMode);
        mainShader.setUniformf("u_gradientParams", gradientDx, gradientDy, gradientType, gradientRepeat);
        mainShader.setUniformf("u_uvScale", uvScaleX, uvScaleY);
        mainShader.setUniformf("u_uvOffset", uvOffsetX, uvOffsetY);

        mainShader.setUniformf("u_borderWidth", Math.min(borderWidth, Math.min(width, height) * 0.5f));
        mainShader.setUniformf("u_borderColor", borderColor.r, borderColor.g, borderColor.b, borderColor.a);
        mainShader.setUniformf("u_borderStyle", borderStyle);
        mainShader.setUniformf("u_dashLength", dashLength);
        mainShader.setUniformf("u_dashRatio", dashRatio);

        mainShader.setUniformf("u_innerShadowColor", innerShadowColor.r, innerShadowColor.g, innerShadowColor.b, innerShadowColor.a);
        mainShader.setUniformf("u_innerShadowSpread", innerShadowSpread);
        mainShader.setUniformf("u_innerShadowBlur", innerShadowBlur);

        mainShader.setUniformf("u_outerGlowColor", outerGlowColor.r, outerGlowColor.g, outerGlowColor.b, outerGlowColor.a);
        mainShader.setUniformf("u_outerGlowSpread", outerGlowSpread);

        mainShader.setUniformf("u_opacity", opacity);
        mainShader.setUniformf("u_colorFilter", filterMode, filterAmount, 0f, 0f);
        mainShader.setUniformf("u_noiseAmount", noiseAmount);

        mainShader.setUniformf("u_elementBounds", elementBoundsX, elementBoundsY, elementBoundsWidth, elementBoundsHeight);
        mainShader.setUniformf("u_blendMode", blendMode);

        // Bind optional textures
        if (fillMode == 1 && gradientTexture != null) {
            gradientTexture.bind(1);
        }
        if (fillMode == 2 && fillTexture != null) {
            fillTexture.bind(2);
        }
        if (blendMode > 0.001f && blurTexture != null) {
            blurTexture.bind(3);
        }

        Draw.color(Color.white);
        float packedColor = Draw.getColor().toFloatBits();
        Fill.quad(Core.atlas.white().texture,
            xPosition, yPosition, packedColor, 0f, 0f,
            xPosition + width, yPosition, packedColor, 1f, 0f,
            xPosition + width, yPosition + height, packedColor, 1f, 1f,
            xPosition, yPosition + height, packedColor, 0f, 1f);

        Draw.flush();
        Draw.shader(previousShader);
    }

    private void ensureBlurFrameBuffers(int width, int height) {
        if (blurFrameBufferA == null || blurFrameBufferA.getWidth() != width || blurFrameBufferA.getHeight() != height) {
            if (blurFrameBufferA != null) {
                blurFrameBufferA.dispose();
            }
            if (blurFrameBufferB != null) {
                blurFrameBufferB.dispose();
            }
            blurFrameBufferA = new FrameBuffer(width, height);
            blurFrameBufferB = new FrameBuffer(width, height);
            this.lastFboWidth = width;
            this.lastFboHeight = height;
        }
    }

    private void doBlur(int fboWidth, int fboHeight, int iterations) {
        float texelSizeU = 1f / fboWidth;
        float texelSizeV = 1f / fboHeight;

        blurShader.bind();
        blurShader.setUniformf("u_texelSize", texelSizeU, texelSizeV);

        for (int i = 0; i < iterations; i++) {
            float offset = i * 0.5f + 0.5f;
            blurShader.setUniformf("u_offset", offset);

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
