# MDT UI Framework - Design Guideline

This document defines the visual standards, design tokens, responsive layout patterns, and micro-interaction guidelines for building consistent, modern, and beautiful user interfaces using the MDT UI Framework.

---

## 1. Visual Aesthetics & Design Tokens

To achieve a premium, state-of-the-art interface (avoiding cheap or default looks), adhere to the following design tokens:

### 1. Color Palettes
Avoid plain, saturated primary colors (red, green, blue). Use curated hex codes matching sleek dark modes:
* **Backgrounds**: Deep obsidian/navy shades:
  - Base Panel: `#1c1c22`
  - Inner Settings Card: `#303052`
  - Interactive Hover: `#55556a`
* **Accents / Status**:
  - Success (Green): `#5cb85c`
  - Info (Cyan): `#5bc0de`
  - Warning (Orange): `#f0c040`
  - Danger (Red): `#d9534f`
  - Primary Pink: `#ff79c6`

### 2. Glassmorphism & Backdrop Filters
For overlay menus, dialogs, and popups, use glassmorphic styling:
- **Opacity**: Use `0.8f` to `0.9f` to create transparency.
- **Borders**: Add thin white borders (`border(1f, Color.valueOf("ffffff15"))`) to frame the element.
- **Radii**: Use soft corners (typically `12f` for panels, `8f` for settings blocks, `6f` for buttons).

---

## 2. Layout Patterns & Hierarchy

Always structure UI containers using structural layers to establish clear visual hierarchy:

```
+-------------------------------------------------------+
|  Layout (Root Panel)                                  |
|  +-------------------------------------------------+  |
|  |  Layout (Header Section)                         |  |
|  |  [Text: Preview Demo]                            |  |
|  +-------------------------------------------------+  |
|  +------------------------+ +----------------------+  |
|  |  Layout (Sidebar)      | |  CustomComponent     |  |
|  |  - Border Settings     | |  (Reactive Preview)  |  |
|  |  - Color Picker        | |                      |  |
|  |  - Opacity Slider      | |                      |  |
|  |                        | |                      |  |
|  +------------------------+ +----------------------+  |
+-------------------------------------------------------+
```

### 1. Spacing & Margins
- **Container Padding**: Always apply standard padding of `16f` for main containers.
- **Item Gap**: Use a gap of `8f` for listing options vertically or horizontally.
- **Spacers**: Avoid empty layout groups unless specifically created as a spacer (e.g. `CustomComponent.of().style(s -> s.opacity(0f).fixedHeight(8f))`).

### 2. Sizing Constraints
- Avoid hardcoding absolute coordinate values for responsive elements. Instead, utilize `SizeMode.GROW` combined with constraints:
  - For sidebars/settings panels: use a fixed width (e.g. `fixedWidth(260f)`) and let height grow or wrap.
  - For dynamic preview blocks: use `grow()` to take all remaining viewport space.

---

## 3. Micro-Interactions & Feedback

Static layouts feel dead. Add subtle feedback loops for interaction:

### 1. Hover States (Reactive State Binding)
To maintain the declarative, nested tree structure of components (without storing references in local variables), always bind hover styles reactively using a `Signal<Boolean>` state rather than imperatively modifying the style in callbacks:

```java
Signal<Boolean> isHovered = new Signal<>(false);

CustomComponent.of()
    .style(s -> s.background(isHovered.get() ? hoverColor : normalColor))
    .onHover(() -> isHovered.set(true))
    .onExit(() -> isHovered.set(false));
```
This keeps the scene graph fully nested, fluent, and responsive.

### 2. Active States
Provide instant visual confirmation on click before completing actions (e.g., color changing, borders changing, text updating reactive feedback).

---

## 4. Typography Scale

Always use **JetBrains Mono** via the `FontManager`. Maintain consistent scale rules:
- **Headers / Titles**: Size factor `1.4f` (bold/markup titles).
- **Labels**: Size factor `0.8f` (dimmed descriptions, headings).
- **Values / Button Text**: Size factor `0.9f` (white text, high contrast).
