# MDT UI Framework - Design Guideline

This document defines the visual standards, design tokens, responsive layout patterns, and micro-interaction guidelines for building consistent, modern, and beautiful user interfaces using the MDT UI Framework.

---

## 1. Visual Aesthetics & Design Tokens

To achieve a premium, modern user interface (avoiding simple or default styling), adhere to the following design tokens:

### 1. Color Palettes
Avoid using overly saturated primary colors (pure red, green, blue). Use curated hex codes tailored for dark interfaces:
* **Backgrounds**: Dark charcoal / slate blue tones:
  - Main Panel (Base Panel): `#1c1c22`
  - Settings Card: `#303052`
  - Interactive Hover/Active States: `#55556a`
* **Accents / Status**:
  - Success (Green): `#5cb85c`
  - Info (Blue): `#5bc0de`
  - Warning (Gold/Yellow): `#f0c040`
  - Danger (Red): `#d9534f`
  - Primary Accent (Primary Pink): `#ff79c6`

### 2. Glassmorphism & Backdrop Filters
For overlay menus, dialogs, and popups, use a frosted glass look:
- **Opacity**: Use values between `0.8f` and `0.9f` to create subtle translucency.
- **Borders**: Add a thin, semi-transparent white border (`borderWidth(1f).borderColor(Color.valueOf("ffffff15"))`) to frame the container.
- **Corner Radii**: Use soft corner rounding (typically `12f` for panels, `8f` for settings blocks, `6f` for buttons).

---

## 2. Layout & Visual Hierarchy

Always organize UI containers into clear layers to establish visual hierarchy:

```
+-------------------------------------------------------+
|  LayoutWidget (Root Panel)                            |
|  +-------------------------------------------------+  |
|  |  LayoutWidget (Header Section)                  |  |
|  |  [TextWidget: Preview Demo]                     |  |
|  +-------------------------------------------------+  |
|  +------------------------+ +----------------------+  |
|  |  LayoutWidget (Sidebar)| |  CustomWidget        |  |
|  |  - Border Settings     | |  (Reactive Preview)  |  |
|  |  - Color Picker        | |                      |  |
|  |  - Opacity Slider      | |                      |  |
|  |                        | |                      |  |
|  +------------------------+ +----------------------+  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

### 1. Spacing & Margins
- **Container Padding**: Always apply standard padding of `12f` to `16f` for parent containers.
- **Element Spacing (Gap)**: Use gaps of `8f` to `12f` when listing options vertically or horizontally.
- **Spacers**: Avoid empty layout groups. If you need fixed spacing, use empty widgets:
  ```java
  LayoutWidget.builder().fixedHeight(8f).build()
  ```

### 2. Sizing Constraints
- Avoid hardcoding absolute coordinates for elements that need to adapt. Instead, use `NodeSpec.SizeMode.GROW` combined with constraints:
  - For toolbars/sidebars: use a fixed width (e.g. `fixedWidth(320f)`) and allow the height to scale automatically.
  - For dynamic preview blocks: use `widthMode(NodeSpec.SizeMode.GROW).heightMode(NodeSpec.SizeMode.GROW)` to occupy all remaining space.

---

## 3. Micro-Interactions & State-driven Styling

To keep the declarative, nested structure of immutable widget trees clean, all visual updates must be driven by application state (`Signal<AppState>`).

### Active States
Instead of imperatively modifying styles inline, compute colors and border properties based on current state parameters:

```java
boolean isActive = s.activeTab() == tabIndex;
Color bg = isActive ? Color.valueOf("ff79c6") : Color.valueOf("303042");

return LayoutWidget.builder()
    .background(CustomWidget.builder().fillColor(bg).build())
    .onClick(() -> state.set(state.get().withActiveTab(tabIndex)))
    .children(...)
    .build();
```
When clicked, the updated signal value triggers a root rebuild, automatically updating compatible widgets via the reconciliation engine.

---

## 4. Typography Scale

Ensure a consistent typographic hierarchy using `TextWidget`:
- **Main Headers**: Font scale `1.3f` (supports Mindustry markup tags like `[ff79c6]`).
- **Description Labels**: Font scale `0.8f` (using muted text colors like `Color.lightGray`).
- **Interactive Values / Button Text**: Font scale `0.9f` (white text with high contrast).
