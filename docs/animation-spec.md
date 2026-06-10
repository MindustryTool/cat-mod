# Animation Spec — Tổng thể

## Kiến trúc 3 lớp

```
┌─────────────────────────────────────────────────────┐
│                   Lớp 1: CustomElement               │
│               Stateless Drawing Engine               │
│  draw(x, y, w, h, spec) — pure function             │
│  Không biết gì về animation, component, lifecycle    │
│  Chỉ: flush → bind shader → set uniforms → draw quad │
├─────────────────────────────────────────────────────┤
│                   Lớp 2: CustomUIComponent            │
│            Stateful + Reactive + Animated            │
│  ┌─────────────────────────────────────────────┐     │
│  │  Style (mutable state bag)                  │     │
│  │  ├── fillColor: Color                       │     │
│  │  ├── cornerRadii: vec4 (tl,tr,br,bl)       │     │
│  │  ├── borderWidth: float                     │     │
│  │  ├── borderColor: Color                     │     │
│  │  ├── borderStyle: int (0/1/2)              │     │
│  │  ├── dashLength: float                      │     │
│  │  ├── dashRatio: float                       │     │
│  │  ├── opacity: float                         │     │
│  │  ├── fillMode: int (0=solid,1=grad,2=tex)  │     │
│  │  ├── gradient: Gradient                     │     │
│  │  ├── fillTexture: Texture                   │     │
│  │  ├── shadowSpread: float                    │     │
│  │  ├── shadowBlur: float                      │     │
│  │  ├── shadowColor: Color                     │     │
│  │  ├── glowColor: Color                       │     │
│  │  ├── glowSpread: float                      │     │
│  │  └── ...                                    │     │
│  └─────────────────────────────────────────────┘     │
│  ┌─────────────────────────────────────────────┐     │
│  │  Animator (transition manager)              │     │
│  │  ├── Transition current                     │     │
│  │  ├── Queue<Transition> pending              │     │
│  │  └── update(delta) → lerp Style props       │     │
│  └─────────────────────────────────────────────┘     │
│  ┌─────────────────────────────────────────────┐     │
│  │  Element (Arc scene2d)                      │     │
│  │  ├── draw(): read Style → CustomElement     │     │
│  │  └── act(delta): Animator.update(delta)    │     │
│  └─────────────────────────────────────────────┘     │
├─────────────────────────────────────────────────────┤
│                   Lớp 3: API người dùng              │
│  CustomUIComponent.of()                              │
│    .style(s -> s.fill(RED).radius(8))                │ ← instant
│    .anim(400, Ease.outQuad, s -> s.fill(BLUE))       │ ← animated
│    .size(sz -> sz.growX().height(50))                │
├─────────────────────────────────────────────────────┤
│                   Lớp phụ: Gradient                   │
│  Gradient.linear(90, 0,RED, .5f,BLUE, 1,GREEN)      │
│  .repeat(3).hardStop()                              │
│  → bake ra 256×1 Texture                            │
└─────────────────────────────────────────────────────┘
```

## AnimatedValue<T>

```java
public class AnimatedValue<T> {
    private T value;       // current (được lerp)
    private T from, to;    // range
    private float elapsed, duration;
    private Ease ease;
    private boolean active;

    // Interpolation strategy — mỗi kiểu T cần 1 interpolator
    public interface Interpolator<T> {
        T lerp(T a, T b, float t);
    }

    public static AnimatedValue<Float> of(float initial);
    public static AnimatedValue<Color> of(Color initial);

    public void animateTo(T target, long ms, Ease ease);
    public boolean update(float delta); // returns true nếu còn chạy
    public T get();
    public boolean isActive();
    public void finish(); // jump to target ngay
    public void set(T value); // set instant, cancel animation
}

// Easing functions
public enum Ease {
    linear, quadIn, quadOut, quadInOut,
    cubicIn, cubicOut, cubicInOut,
    expoIn, expoOut, expoInOut,
    bounceOut,
    // ...
}
```

## Transition

Một Transition là snapshot cặp `(from → to)` cho toàn bộ Style properties:

```java
class Transition {
    // Snapshots
    private final Style from; // deep copy at start
    private final Style to;   // target values
    private final long duration;
    private final Ease ease;
    private final Style live; // reference đến Style thật
    private float elapsed;
    private boolean done;

    boolean update(float delta) {
        elapsed += delta;
        float t = min(elapsed / duration, 1f);
        float pt = ease.apply(t);

        // Lerp từng property
        live.fillColor.lerp(from.fillColor, to.fillColor, pt);
        live.cornerRadius = lerp(from.cornerRadius, to.cornerRadius, pt);
        live.borderWidth = lerp(from.borderWidth, to.borderWidth, pt);
        live.borderColor.lerp(from.borderColor, to.borderColor, pt);
        live.opacity = lerp(from.opacity, to.opacity, pt);
        live.shadowSpread = lerp(from.shadowSpread, to.shadowSpread, pt);
        // ... mỗi property thêm 1 dòng lerp

        if (t >= 1f) {
            // Snap to exact target
            copyProps(to, live);
            done = true;
        }
        return !done;
    }
}
```

## Animator (trong CustomUIComponent)

```java
class Animator {
    private Transition current;
    private final Queue<Transition> pending = new Queue<>();
    private final Style style; // reference
    private boolean dirty;

    void animate(long ms, Ease ease, Consumer<Style> targetConfig) {
        Style target = new Style(); // fresh
        targetConfig.accept(target); // apply target values

        Transition t = new Transition(style.snapshot(), target, ms, ease, style);
        pending.add(t);

        if (current == null) advance();
    }

    boolean update(float delta) {
        dirty = false;
        if (current != null) {
            if (current.update(delta)) dirty = true;
            else {
                current = null;
                if (!pending.isEmpty()) advance();
            }
        }
        return dirty; // caller calls element.invalidateHierarchy() nếu cần
    }

    void advance() {
        current = pending.removeFirst();
    }

    void cancelAll() { current = null; pending.clear(); }
    void finishAll() { while (current != null || !pending.isEmpty()) update(99999f); }
}
```

## Luồng dữ liệu lúc animate

```
Frame 1:
  User: .anim(400ms, outQuad, s -> s.fill(BLUE).radius(20))
    → Animator tạo Transition(from=current, to={BLUE, 20})
  act(16ms):
    → Transition.update(16ms) → t=0.04, ease=0.008
    → style.fillColor = lerp(RED, BLUE, 0.008) → #FF0202 (gần RED)
    → style.cornerRadius = lerp(8, 20, 0.008) → 8.1
    → return dirty=true
  draw():
    → CustomElement.fill(x,y,w,h, style.cornerRadius, style.fillColor)
    → Vẽ gần RED, radius gần 8

Frame 30 (480ms):
  act(16ms):
    → Transition.update(16ms) → t=1.0 → done
    → style.fillColor = BLUE (snap)
    → style.cornerRadius = 20 (snap)
    → return dirty=true
  draw():
    → Vẽ BLUE, radius 20

Animation done → không dirty → không invalidate → không redraw
```

## Property trạng thái bình thường vs animated

| Property | Kiểu | Lerp được? | Ghi chú |
|----------|------|-----------|---------|
| fillColor | Color | ✓ | lerp RGBA |
| cornerRadii | vec4 | ✓ | lerp từng radius |
| borderWidth | float | ✓ | |
| borderColor | Color | ✓ | |
| borderStyle | int (0/1/2) | ✗ | discrete — jump |
| dashLength | float | ✓ | |
| dashRatio | float | ✓ | |
| opacity | float | ✓ | |
| shadowSpread | float | ✓ | |
| shadowBlur | float | ✓ | |
| shadowColor | Color | ✓ | |
| glowColor | Color | ✓ | |
| glowSpread | float | ✓ | |
| fillMode | int | ✗ | discrete — jump |
| gradient | Gradient | ✗ | swap texture |
| fillTexture | Texture | ✗ | swap texture |

**Discrete properties**: khi detect change trong Transition, set target value ngay, không lerp.

## Integration với CustomElement

CustomElement không thay đổi. Nó nhận spec/params đã được resolve:

```java
// CustomUIComponent.Element.draw():
void draw() {
    float x = this.x, y = this.y, w = getWidth(), h = getHeight();
    if (w <= 0 || h <= 0) return;

    // Read current animated values from Style
    DrawSpec spec = DrawSpec.builder()
        .cornerRadii(style.tl, style.tr, style.br, style.bl)
        .fill(style.fillColor)
        .border(style.borderWidth, style.borderColor, style.borderStyle)
        .dash(style.dashLength, style.dashRatio)
        .opacity(style.opacity)
        .innerShadow(style.shadowSpread, style.shadowBlur, style.shadowColor)
        .outerGlow(style.glowSpread, style.glowColor)
        .build();

    element.draw(x, y, w, h, spec);
}
```

Hoặc gọi thẳng các method:
```java
element.draw(x, y, w, h, style);
Style implements DrawSpec { ... }
```

Hoặc đơn giản nhất:
```java
private final float[] uniforms = new float[40]; // pre-allocated
// update uniforms from Style
// call Fill.quad with shader
```

## Tổng số file

| File | Vai trò | ~LOC |
|------|---------|------|
| `CustomElement.java` | Stateless drawing engine | 250 |
| `CustomUIComponent.java` | Stateful component + Element | 200 |
| `Gradient.java` | Gradient texture từ stops | 100 |
| `AnimatedValue.java` | Generic interpolatable value | 60 |
| `Animator.java` | Transition manager | 80 |
| `Ease.java` | Easing functions | 80 |
| `Style.java` | Property bag (tách từ inner class) | 100 |
