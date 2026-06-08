package com.neko.libs.ui;

import arc.scene.Element;
import com.neko.libs.signal.Signal;
import com.neko.libs.ui.style.StyleSpec;
import com.neko.libs.ui.widget.NBtn;
import com.neko.libs.ui.widget.NBtn.BtnConfig;
import com.neko.libs.ui.widget.NLabel;
import com.neko.libs.ui.widget.NLabel.LabelConfig;

public final class N {
    private N() {}

    public static El el(String style, Element... children) {
        El e = new El(style);
        for (Element c : children) e.addChild(c);
        return e;
    }

    public static El el(StyleSpec spec, Element... children) {
        El e = new El(spec);
        for (Element c : children) e.addChild(c);
        return e;
    }

    public static El el(Signal<String> variant, String[] variantDefs, Element... children) {
        El e = new El(variant, variantDefs);
        for (Element c : children) e.addChild(c);
        return e;
    }

    public static El spacer() {
        return new El("grow");
    }

    public static NLabel label(LabelConfig cfg) {
        return new NLabel(cfg);
    }

    public static NBtn btn(BtnConfig cfg) {
        return new NBtn(cfg);
    }

    public static String[] variants(String... defs) {
        return defs;
    }
}
