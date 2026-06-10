# Drawing Spec — CustomElement

## 1. Corner Radius (4 corners riêng)

```
u_cornerRadii = vec4(tl, tr, br, bl)
```

**Behavior:**
- Mỗi góc có radius riêng, tính bằng pixel (world units)
- Clamp: mỗi radius bị clamp về `min(radius, min(w, h) / 2)` — không cho quá nửa cạnh ngắn nhất
- Khi radius = 0: góc vuông
- Khi tất cả radius = min(w,h)/2: hình ellipse/circle
- SDF chọn radius theo quadrant:
  ```
  r.xy = (p.x > 0) ? r.xy : r.zw
  r.x  = (p.y > 0) ? r.x  : r.y
  ```

## 2. Fill Modes

```
u_fillMode: 0 = solid | 1 = gradient | 2 = texture
```

### 2a. Solid
- `u_fillColor` = RGBA
- Output = `u_fillColor`

### 2b. Gradient
- `u_gradientTex`: sampler, texture 2D height=1, width=N (N stops, N ≥ 2)
- `u_gradientParams.xy`: direction vector (linear) hoặc center (radial/conic)
- `u_gradientParams.z`: type — 0=linear, 1=radial, 2=conic
- `u_gradientParams.w`: repeat count (1 = no repeat, >1 = repeating)

**Position compute:**

| Type | Công thức |
|------|-----------|
| Linear | `t = dot(uv - 0.5, normalize(dir)) + 0.5` |
| Radial | `t = distance(uv, center) * 2` (center mặc định 0.5,0.5) |
| Conic | `t = atan(uv.y - cy, uv.x - cx) / 6.2832 + 0.5` |

**Final:**
```
t = fract(t * repeat)
color = texture2D(u_gradientTex, vec2(t, 0.5))
```

**Color interpolation giữa các stop:** linear trong không gian RGBA.

**Hard stop:** 2 stops tại cùng position. Kết quả: không blend, sharp edge.

### 2c. Texture
- `u_fillTexture`: sampler 2D
- `u_uvTransform.xy` = scale (1,1 = no scale)
- `u_uvTransform.zw` = offset (0,0 = no offset)
- UV transform: `uv = (uv - 0.5) * scale + 0.5 + offset`
- Wrap mode: `clampToEdge` mặc định (có thể set trên texture object)

## 3. Border

```
u_borderWidth: float (pixels)
u_borderColor: vec4
u_borderStyle: 0 = solid | 1 = dashed | 2 = dotted
u_dashLength: float (pixels dọc theo perimeter)
u_dashRatio: float (0-1, tỷ lệ dash / gap)
```

**Behavior chung:**
- Border vẽ **inset** (bên trong shape)
- Inner SDF: `innerRadius = max(cornerRadius - borderWidth, 0)`
- `innerDist = roundedBoxSDF(pos, halfSize - borderWidth, innerRadii)`
- `borderAlpha = outerAlpha - innerAlpha`

### 3a. Solid
- Toàn bộ border perimeter được fill

### 3b. Dashed
- Tính vị trí dọc theo perimeter (dùng UV hoặc angle)
- `frag(perimeterPos / dashLength) < dashRatio` → vẽ border, bỏ qua phần gap
- Dash pattern đồng bộ trên cả 4 cạnh (bắt đầu từ top-center)

### 3c. Dotted
- Giống dashed nhưng dashRatio → dot ratio, hiển thị như các chấm tròn
- dot = circle thay vì rect dash

### Clamp
- `u_borderWidth` clamp về `min(width, height) / 2`
- Khi borderWidth >= min(w,h)/2: inner SDF biến mất → fill ko còn, chỉ border

## 4. Inner Shadow

```
u_innerShadowColor: vec4
u_innerShadowSpread: float (pixels, lùi SDF vào trong)
u_innerShadowBlur: float (pixels, blur SDF edge)
```

**Behavior:**
- Shadow offset từ SDF edge lùi vào: `shadowDist = dist + spread`
- Alpha: `smoothstep(0, blur, shadowDist)` → càng vào trong càng tối
- Vị trí: `shadowAlpha = 1.0 - smoothstep(0, blur, dist + spread)`
- Kết quả: `result = mix(result, shadowColor, shadowAlpha)`
- Khi spread = 0: shadow sát edge
- Khi spread > 0: shadow ăn sâu vào trong
- Khi blur > 0: edge mềm
- Khi cả spread=0 và blur=0: inner glow sát edge

## 5. Outer Glow

```
u_outerGlowColor: vec4
u_outerGlowSpread: float (pixels, phình SDF ra ngoài)
```

**Behavior:**
- Phình SDF ra ngoài: `glowDist = dist - spread`
- Alpha: `smoothstep(-softness, 0, glowDist)` — glow chỉ xuất hiện bên ngoài shape
- Kết quả: `result = mix(result, glowColor, glowAlpha)`
- Chỉ vẽ glow khi dist > 0 (bên ngoài shape)
- Glow không ảnh hưởng đến fill/border bên trong

## 6. Glass (backdrop-blur)

```
u_blurTexture: sampler2D (external, pre-captured + blurred)
u_blendMode: float (0 = multiply, 1 = overlay)
```

**Behavior:**
- Texture là screen capture đã blur (từ NekoUiManager)
- UV: dùng `v_texCoord0` (element-relative) → map vào screen-absolute coordinates
  - Cần biết element position trên screen → pass qua uniform hoặc tính từ x,y
  - `uv = (fragCoord - elementPos) / elementSize`
- Composite:
  ```
  blurColor = texture2D(u_blurTexture, screenUV)
  multResult = blurColor * tintColor
  overResult = mix(blurColor.rgb, tintColor.rgb, tintColor.a)
  glassColor = mix(multResult, overResult, u_blendMode)
  ```
- Áp dụng SDF mask: `result = glassColor * outerAlpha`
- Border + glass: border vẽ lên trên glass (giống các mode khác)

**NOTE:** Glass chỉ hoạt động khi NekoUiManager cung cấp blur texture. Nếu không có fallback về solid fill.

## 7. Opacity

```
u_opacity: float (0-1, default 1)
```

- `result.a *= u_opacity`
- Áp dụng cuối cùng, sau tất cả các layer khác
- 0 = invisible, 1 = fully opaque

## 8. Color Filter

```
u_colorFilter: vec4 (x = mode, y = amount, z = 0, w = 0)
```

**Modes:**

| mode | name | formula |
|------|------|---------|
| 0 | none | pass-through |
| 1 | grayscale | `luma = dot(rgb, vec3(0.299, 0.587, 0.114)); rgb = mix(rgb, luma, amount)` |
| 2 | sepia | `sepia = vec3(dot(rgb, vec3(0.393,0.769,0.189)), dot(rgb, vec3(0.349,0.686,0.168)), dot(rgb, vec3(0.272,0.534,0.131)); rgb = mix(rgb, sepia, amount)` |
| 3 | brightness | `rgb += amount` |
| 4 | invert | `rgb = mix(rgb, 1 - rgb, amount)` |

- amount: 0-1 (trừ brightness: -1 đến 1)
- Chỉ 1 filter active tại 1 thời điểm

## 9. Noise Grain

```
u_noiseAmount: float (0 = off, >0 = intensity)
```

- Simple hash noise: `hash(uv + time)`
- `rgb += (noise - 0.5) * u_noiseAmount`
- Dùng cho glass texture effect hoặc film grain

## 10. Tương tác giữa các layer (stacking order trong 1 draw call)

```
1. Fill (solid / gradient / texture)
2. Inner Shadow (on top of fill)
3. Border (on top of fill + shadow)
4. Outer Glow (on top of border)
5. Glass: thay thế fill layer, border vẽ lên trên
6. Opacity (áp dụng cuối)
7. Color Filter (áp dụng cuối)
8. Noise Grain (áp dụng cuối)
```

**Kết hợp được:**
- gradient + border
- gradient + border + inner shadow
- texture + dashed border + glow
- glass + border + opacity
- solid + border + glow + noise

**Ko kết hợp được trong 1 draw call:**
- multiple fills (layer nhiều background) → cần N draw call
- drop shadow ra ngoài → cần 1 draw call riêng (FBO hoặc draw trước)
- glass + gradient cùng lúc → glass override fill (chọn 1)

## 11. Java API

```java
public class CustomElement implements Disposable {

    // ─── Solid ───
    void fill(float x, float y, float w, float h,
              float tl, float tr, float br, float bl, Color color);

    void fill(float x, float y, float w, float h,
              float radius, Color color);

    // ─── Gradient ───
    void fillGradient(float x, float y, float w, float h,
                      float tl, float tr, float br, float bl,
                      Gradient gradient);

    // ─── Texture ───
    void fillTexture(float x, float y, float w, float h,
                     float tl, float tr, float br, float bl,
                     Texture texture, Color tint,
                     float scaleX, float scaleY,
                     float offsetX, float offsetY);

    // ─── With Border ───
    void fillWithBorder(float x, float y, float w, float h,
                        float tl, float tr, float br, float bl,
                        Color fillColor,
                        float borderWidth, Color borderColor);

    void fillGradientWithBorder(float x, float y, float w, float h,
                                float tl, float tr, float br, float bl,
                                Gradient gradient,
                                float borderWidth, Color borderColor,
                                int borderStyle /* 0=solid,1=dashed,2=dotted */);

    // ─── Inner Shadow ───
    void fillWithShadow(float x, float y, float w, float h,
                        float tl, float tr, float br, float bl,
                        Color fillColor,
                        float shadowSpread, float shadowBlur, Color shadowColor);

    // ─── Glass ───
    void glass(float x, float y, float w, float h,
               float tl, float tr, float br, float bl,
               Texture blurSource, Color tint,
               float borderWidth, Color borderColor);

    // ─── Full draw ───
    void draw(float x, float y, float w, float h, DrawSpec spec);

    // ─── Static dispose ───
    static void disposeShared();
    void dispose();
}
```

## 12. DrawSpec Builder

```java
DrawSpec spec = DrawSpec.builder()
    .fill(Color.RED)
    .border(2f, Color.WHITE, BorderStyle.DASHED)
    .innerShadow(4f, 0.3f, new Color(0,0,0,0.5f))
    .opacity(0.9f)
    .build();

element.draw(x, y, w, h, spec);
```

## 13. Gradient class

```java
public class Gradient implements Disposable {

    // Factory
    static Gradient linear(float angle, Object... stops); // stops: pos1,color1, pos2,color2, ...
    static Gradient radial(float cx, float cy, Object... stops);
    static Gradient conic(float cx, float cy, Object... stops);

    // Configure
    Gradient repeat(int count);
    Gradient hardStop(); // sharp transitions

    // Internal
    Texture getTexture(); // baked 256x1 texture
    int getType(); // 0=linear,1=radial,2=conic
    float[] getParams(); // [dx, dy/info, type, repeat]

    void dispose();
}
```

## 14. SDF Implementation (GLSL)

```glsl
// Per-corner rounded box SDF
float roundedBoxSDF(vec2 p, vec2 halfSize, vec4 radii) {
    // Select radius based on quadrant
    vec2 rSel = (p.x > 0.0) ? radii.xy : radii.zw;
    float r = (p.y > 0.0) ? rSel.x : rSel.y;

    vec2 q = abs(p) - halfSize + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}
```

## 15. Glass: Screen Capture Flow

```
NekoUiManager:
  1. Before stage.draw():
     - captureFbo.resize(screenW/2, screenH/2)
     - Gl.copyTexSubImage2D → captureFbo texture
     - Blur captureFbo (Kawase, 4 passes) → blurFboA/B ping-pong
     - Set shared blurTexture = blurFboA.getTexture()
  2. Glass element reads shared blurTexture
  3. Trong Element.draw():
     - sample blurTexture với screen-space UV
     - composite với tint + SDF mask
```

**UV mapping cho glass:**
```
screenUV = (fragCoord - elementScreenPos) / elementScreenSize
// Trong Element.draw(), fragCoord = (this.x + v_texCoord0.x * width, this.y + v_texCoord0.y * height)
// screenUV = (this.x + uv.x * width) / screenW, (this.y + uv.y * height) / screenH
// Shader nhận elementScreenPos và elementScreenSize làm uniforms
```

## 16. Non-goals (phase sau)

- Multiple backgrounds (N layer composite)
- Drop shadow outward (cần FBO riêng)
- Conic gradient với nhiều vòng (repeating conic)
- Gradient dạng hình học (diamond, etc.)
- Animation (sẽ có reactive system riêng)
- border-image (9-patch texture)
- mix-blend-mode (screen, multiply, etc.)
