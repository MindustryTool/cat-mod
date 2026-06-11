# Assistant Developer Guide (claude.md)

This file is a guide for AI coding assistants (like Claude, Gemini, etc.) working on this codebase. It provides reference paths and helpful context about the project dependencies.

---

## 🌐 Mindustry Engine Source Code / Mã nguồn Mindustry

For reference, the decompiled/source code of the **Mindustry engine** and **Arc framework** has been cloned and placed locally at:
* **Path**: `C:\Users\meohexa1a\project\neko-content-mod\temp\mindustry-source`

> [!TIP]
> Assistants should refer to this local directory to search for Arc UI components (like `ScrollPane`, `WidgetGroup`, `Element`, `Font`, etc.) or Mindustry specific APIs (like `Vars`, `BaseDialog`, etc.) to understand their native behaviors and contracts.

---

## 🛠️ Project Structure / Cấu trúc dự án

- **UI Components & Core**: Located under `src/main/java/org/mindustrytool/libs/ui/`.
- **Signal System (Reactivity)**: Located under `src/main/java/org/mindustrytool/libs/signal/`.
- **Demo / Screener Overlay**: Located under `src/main/java/org/mindustrytool/mdtui/screen/DemoUI.java`.
- **Unit Tests**: Located under `src/test/java/org/mindustrytool/libs/signal/`.

---

## ⚠️ Important Development Notes / Lưu ý quan trọng

1. **Arc setScene(null) Lifecycle Contract**:
   Arc calls `setScene(null)` both when an element is removed from the scene and when it is added to a parent container that is not yet attached to a scene.
   To avoid premature garbage collection or unsubscriptions, always check `boolean hadScene = getScene() != null;` and only dispose when `hadScene && scene == null`.

2. **Font Scaling System**:
   When scaling font dynamically via `Text` styling (`size(...)`), always multiply by the font's base scale (retrieved via `baseFontScaleX` / `baseFontScaleY`) because fonts like JetBrains Mono are generated at a larger size (`48`) and scaled down globally via `FontManager` (`0.4f`) to improve crispness. Overriding scale directly without multiplying will cause extreme rendering distortions.
