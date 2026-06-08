package com.neko.libs.simpleui.style;

import arc.graphics.Color;

public final class Theme {

    private Theme() {}

    public static final Color bgVoid    = Color.valueOf("0d0d0f");
    public static final Color bgBase    = Color.valueOf("141418");
    public static final Color bgSurface = Color.valueOf("1c1c22");
    public static final Color bgRaised  = Color.valueOf("26262f");
    public static final Color bgInput   = Color.valueOf("2a2a35");

    public static final Color borderSubtle  = Color.valueOf("2e2e3a");
    public static final Color borderDefault = Color.valueOf("44445a");
    public static final Color borderActive  = Color.valueOf("7a7aaa");

    public static final float BORDER_W = 1f;

    public static final Color accentPrimary = Color.valueOf("6c8ebf");
    public static final Color accentGold    = Color.valueOf("f0c040");
    public static final Color accentRed     = Color.valueOf("d9534f");
    public static final Color accentGreen   = Color.valueOf("5cb85c");
    public static final Color accentBlue    = Color.valueOf("5bc0de");

    public static final Color textBright    = Color.valueOf("f2f2f5");
    public static final Color textPrimary   = Color.valueOf("c8c8d8");
    public static final Color textSecondary = Color.valueOf("8888aa");
    public static final Color textGhost     = Color.valueOf("55556a");
    public static final Color textAccent    = accentGold;

}
