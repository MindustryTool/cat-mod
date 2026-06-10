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
            uv.y = 1.0 - uv.y;
            baseColor = texture2D(u_fillTexture, uv) * u_fillColor;
        }
    }

    // ─── Inner Shadow ───
    float shadowDist = dist + u_innerShadowSpread;
    float shadowAlpha = smoothstep(-max(u_innerShadowBlur, 0.001), 0.0, shadowDist);
    baseColor = mix(baseColor, vec4(u_innerShadowColor.rgb, 1.0), shadowAlpha * u_innerShadowColor.a);

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
