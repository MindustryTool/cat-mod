// ─── Custom UI Element Fragment Shader ───
// Renders premium UI shapes with rounded corners, borders, shadows, glows, and filters

// ─── Layout Uniforms ───
uniform vec4  u_cornerRadii;       // Corner radii: [x]=topLeft, [y]=topRight, [z]=bottomRight, [w]=bottomLeft
uniform vec2  u_size;              // Width and height of the element
uniform float u_edgeSoftness;      // Anti-aliasing edge softness (default 1.0)

// ─── Background Fill Uniforms ───
uniform vec4  u_fillColor;         // Solid fill color or texture tint
uniform float u_fillMode;          // Background mode: 0=solid, 1=gradient, 2=texture

// ─── Layered Gradients Uniforms ───
uniform sampler2D u_gradientTex0;  // Gradient 0 lookup texture
uniform sampler2D u_gradientTex1;  // Gradient 1 lookup texture
uniform sampler2D u_gradientTex2;  // Gradient 2 lookup texture
uniform sampler2D u_gradientTex3;  // Gradient 3 lookup texture
uniform vec4      u_gradientParams0;// [xy]=dir, [z]=type, [w]=repeat
uniform vec4      u_gradientParams1;
uniform vec4      u_gradientParams2;
uniform vec4      u_gradientParams3;
uniform float     u_gradientCount;  // Active gradient count (1 to 4)

// ─── Texture Uniforms ───
uniform sampler2D u_fillTexture;   // Background texture image
uniform vec2      u_uvScale;       // Texture UV scale
uniform vec2      u_uvOffset;      // Texture UV offset

// ─── Border Uniforms ───
uniform float u_borderWidth;       // Border thickness
uniform vec4  u_borderColor;       // Border color
uniform float u_borderStyle;       // Border style: 0=solid, 1=dashed, 2=dotted
uniform float u_dashLength;        // Length of dashes/dots
uniform float u_dashRatio;         // Ratio of dash length to gap length

// ─── Inner Shadow Uniforms ───
uniform vec4  u_innerShadowColor;  // Inner shadow color and opacity
uniform float u_innerShadowSpread; // Spread distance of the shadow
uniform float u_innerShadowBlur;   // Blur softness of the shadow

// ─── Outer Glow Uniforms ───
uniform vec4  u_outerGlowColor;    // Glow color and opacity
uniform float u_outerGlowSpread;   // Glow radius/spread

// ─── Backdrop Filter Uniforms ───
uniform sampler2D u_backdropTex;   // Blurred background copy texture
uniform vec4      u_backdropCoords;// Screen UV coordinate bounds of the element
uniform float     u_backdropBlend; // Backdrop blend mode (0.0 multiply, 1.0 normal)
uniform float     u_backdropWeight;// Backdrop blending strength (0.0 to 1.0)
uniform float     u_backdropMinAlpha;// Minimum alpha limit for the backdrop area

// ─── Color Filter & Noise Uniforms ───
uniform float u_opacity;           // Master opacity multiplier
uniform vec4  u_colorFilter;       // [x]=mode (1:grayscale, 2:sepia, 3:brightness, 4:invert), [y]=amount
uniform float u_noiseAmount;       // Noise intensity multiplier

// ─── Interpolated Varyings ───
varying vec4 v_color;
varying vec2 v_texCoord0;


/**
 * Calculates the Signed Distance Field (SDF) of a rounded box.
 * Positive values are outside the box, negative values are inside.
 */
float roundedBoxSDF(vec2 p, vec2 s, vec4 r) {
    vec2 rSel = (p.x > 0.0) ? r.xy : r.zw;
    float radius = (p.y > 0.0) ? rSel.x : rSel.y;
    vec2 q = abs(p) - s + radius;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius;
}


/**
 * Computes the normalized gradient position along the gradient line/sweep.
 */
float gradientPos(vec2 uv, vec4 params) {
    float type = params.z;
    float t = uv.x;
    
    if (type < 0.5) { // Linear gradient
        vec2 dir = normalize(params.xy);
        t = dot(uv - 0.5, dir) + 0.5;
    } else if (type < 1.5) { // Radial gradient
        vec2 center = (params.xy + 1.0) * 0.5;
        t = distance(uv, center) * 1.414;
    } else { // Conic sweep gradient
        vec2 rel = uv - 0.5;
        t = atan(rel.y, rel.x) / 6.2832 + 0.5;
    }
    
    return fract(t * params.w);
}


/**
 * Computes a pseudo-random hash value for grain/noise.
 */
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}


void main() {
    vec2 halfSize = u_size * 0.5;
    vec2 pos = v_texCoord0 * u_size - halfSize;

    // ─── 1. Signed Distance Field ───
    float dist = roundedBoxSDF(pos, halfSize, u_cornerRadii);
    float outerAlpha = 1.0 - smoothstep(0.0, u_edgeSoftness, dist);
    if (outerAlpha < 0.001) { discard; return; }

    // ─── 2. Background Fill (Solid / Multi-Gradient / Texture) ───
    vec4 baseColor = u_fillColor;
    if (u_fillMode > 0.5) {
        if (u_fillMode < 1.5) { // Gradient Mode
            float t0 = gradientPos(v_texCoord0, u_gradientParams0);
            baseColor = texture2D(u_gradientTex0, vec2(t0, 0.5));
            
            // Blend up to 4 layers of gradients sequentially using their alphas
            if (u_gradientCount > 1.5) {
                float t1 = gradientPos(v_texCoord0, u_gradientParams1);
                vec4 g1 = texture2D(u_gradientTex1, vec2(t1, 0.5));
                baseColor = mix(baseColor, g1, g1.a);
            }
            if (u_gradientCount > 2.5) {
                float t2 = gradientPos(v_texCoord0, u_gradientParams2);
                vec4 g2 = texture2D(u_gradientTex2, vec2(t2, 0.5));
                baseColor = mix(baseColor, g2, g2.a);
            }
            if (u_gradientCount > 3.5) {
                float t3 = gradientPos(v_texCoord0, u_gradientParams3);
                vec4 g3 = texture2D(u_gradientTex3, vec2(t3, 0.5));
                baseColor = mix(baseColor, g3, g3.a);
            }
        } else { // Texture Mode
            vec2 uv = (v_texCoord0 - 0.5) * u_uvScale + 0.5 + u_uvOffset;
            uv.y = 1.0 - uv.y; // Flip Y coordinate for texture coordinates
            baseColor = texture2D(u_fillTexture, uv) * u_fillColor;
        }
    }

    // ─── 3. Inner Shadow ───
    float shadowDist = dist + u_innerShadowSpread;
    float shadowAlpha = smoothstep(-max(u_innerShadowBlur, 0.001), 0.0, shadowDist);
    baseColor = mix(baseColor, vec4(u_innerShadowColor.rgb, 1.0), shadowAlpha * u_innerShadowColor.a);

    // ─── 4. Border Rendering ───
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
            if (u_borderStyle > 0.5) { // Dashed or Dotted style
                float px = v_texCoord0.x, py = v_texCoord0.y;
                float perim;
                // Approximate perimeter position along the quad border
                if (py < 0.001) perim = px;
                else if (px > 0.999) perim = 1.0 + py;
                else if (py > 0.999) perim = 2.0 + (1.0 - px);
                else perim = 3.0 + (1.0 - py);
                perim /= 4.0;
                
                float dashPos = fract(perim * u_size.x * 2.0 / max(u_dashLength, 1.0));
                float dashMask = step(dashPos, u_dashRatio);
                if (u_borderStyle > 1.5) { // Dotted style: add gaps within dots
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

    // ─── 5. Outer Glow ───
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

    // ─── 6. Backdrop Filter (Glass Overlay) ───
    if (u_backdropWeight > 0.001) {
        vec2 screenUV = u_backdropCoords.xy + v_texCoord0 * u_backdropCoords.zw;
        vec4 backdropColor = texture2D(u_backdropTex, screenUV);
        vec4 blendedColor = mix(backdropColor * u_fillColor, mix(backdropColor, u_fillColor, u_fillColor.a), u_backdropBlend);
        result = mix(result, blendedColor, u_backdropWeight);
        visibleAlpha = max(visibleAlpha, u_backdropMinAlpha);
    }

    // ─── 7. Opacity ───
    result.a = visibleAlpha * u_opacity;

    // ─── 8. Color Filter (Grayscale / Sepia / Brightness / Invert) ───
    float filter = u_colorFilter.x;
    float amount = u_colorFilter.y;
    if (filter > 0.5 && amount > 0.001) {
        if (filter < 1.5) { // Grayscale mode
            float luma = dot(result.rgb, vec3(0.299, 0.587, 0.114));
            result.rgb = mix(result.rgb, vec3(luma), amount);
        } else if (filter < 2.5) { // Sepia mode
            vec3 sepia = vec3(
                dot(result.rgb, vec3(0.393, 0.769, 0.189)),
                dot(result.rgb, vec3(0.349, 0.686, 0.168)),
                dot(result.rgb, vec3(0.272, 0.534, 0.131))
            );
            result.rgb = mix(result.rgb, sepia, amount);
        } else if (filter < 3.5) { // Brightness adjustment
            result.rgb += amount;
        } else { // Inversion
            result.rgb = mix(result.rgb, 1.0 - result.rgb, amount);
        }
    }

    // ─── 9. Noise Grain ───
    if (u_noiseAmount > 0.001) {
        result.rgb += (hash(v_texCoord0 + 100.0) - 0.5) * u_noiseAmount;
    }

    gl_FragColor = result;
}
