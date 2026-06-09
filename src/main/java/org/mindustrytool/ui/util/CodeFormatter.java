package org.mindustrytool.ui.util;

import arc.graphics.Color;

public final class CodeFormatter {

    private CodeFormatter() {}

    public static String formatCode(int tabSize, boolean lineNumbers, boolean indentGuides) {
        String[] lines = {
            "package neko.mod;",
            "",
            "import mindustry.mod.Mod;",
            "import mindustry.world.Block;",
            "import mindustry.world.blocks.defense.turrets.PowerTurret;",
            "import mindustry.content.Items;",
            "import mindustry.type.Category;",
            "",
            "public class NekoContentMod extends Mod {",
            "    public static Block nekoTurret;",
            "",
            "    @Override",
            "    public void loadContent() {",
            "        nekoTurret = new PowerTurret(\"neko-turret\") {{",
            "            requirements(Category.turret, with(",
            "                Items.copper, 150,",
            "                Items.lead, 120,",
            "                Items.silicon, 80",
            "            ));",
            "            ",
            "            size = 3;",
            "            health = 1200;",
            "            reload = 45f;",
            "            range = 240f;",
            "            shootType = new LaserBulletType(60) {{",
            "                colors = new Color[]{ Color.valueOf(\"ff79c6\"), Color.valueOf(\"8be9fd\") };",
            "                width = 8f;",
            "                length = 240f;",
            "            }};",
            "        }};",
            "    }",
            "}"
        };
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) {
                if (lineNumbers) {
                    sb.append(String.format("[#6272a4]%2d │[]\n", i + 1));
                } else {
                    sb.append("\n");
                }
                continue;
            }
            int spaces = 0;
            while (spaces < line.length() && line.charAt(spaces) == ' ') {
                spaces++;
            }
            int level = spaces / 4;
            String content = line.substring(spaces);

            String highlighted = content;
            highlighted = highlighted.replace("package ", "[#ff79c6]package []");
            highlighted = highlighted.replace("import ", "[#ff79c6]import []");
            highlighted = highlighted.replace("public class ", "[#ff79c6]public class []");
            highlighted = highlighted.replace("extends ", "[#ff79c6]extends []");
            highlighted = highlighted.replace("public static ", "[#ff79c6]public static []");
            highlighted = highlighted.replace("void ", "[#ff79c6]void []");
            highlighted = highlighted.replace("new ", "[#ff79c6]new []");
            highlighted = highlighted.replace("@Override", "[#ffb86c]@Override[]");

            highlighted = highlighted.replace("Block ", "[#8be9fd]Block []");
            highlighted = highlighted.replace("Mod ", "[#8be9fd]Mod []");
            highlighted = highlighted.replace("PowerTurret", "[#8be9fd]PowerTurret[]");
            highlighted = highlighted.replace("LaserBulletType", "[#8be9fd]LaserBulletType[]");
            highlighted = highlighted.replace("Color", "[#8be9fd]Color[]");

            highlighted = highlighted.replace("\"neko-turret\"", "[#f1fa8c]\"neko-turret\"[]");
            highlighted = highlighted.replace("\"ff79c6\"", "[#f1fa8c]\"ff79c6\"[]");
            highlighted = highlighted.replace("\"8be9fd\"", "[#f1fa8c]\"8be9fd\"[]");

            highlighted = highlighted.replaceAll("(?<=\\W)(\\d+f?)(?=\\W)", "[#bd93f9]$1[]");

            StringBuilder indentSb = new StringBuilder();
            for (int lvl = 0; lvl < level; lvl++) {
                if (indentGuides) {
                    indentSb.append("[#44475a]│[]");
                    for (int s = 1; s < tabSize; s++) {
                        indentSb.append(" ");
                    }
                } else {
                    for (int s = 0; s < tabSize; s++) {
                        indentSb.append(" ");
                    }
                }
            }

            String formattedLine = indentSb.toString() + highlighted;
            if (lineNumbers) {
                sb.append(String.format("[#6272a4]%2d │[] %s\n", i + 1, formattedLine));
            } else {
                sb.append(formattedLine).append("\n");
            }
        }
        return sb.toString();
    }
}