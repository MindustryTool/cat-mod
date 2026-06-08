package com.neko.libs.simpleui.components;

import arc.scene.Element;

import com.neko.libs.simpleui.layout.Sizing;

public interface UIComponent {

    Element element();

    Sizing sizing();

    default void onDestroy() {

    }
}
