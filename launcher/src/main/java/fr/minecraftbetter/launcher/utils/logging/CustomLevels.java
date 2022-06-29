package fr.minecraftbetter.launcher.utils.logging;

import java.util.logging.Level;

public class CustomLevels extends Level {
    public static final Level NoFormatting = new CustomLevels("No formatting", 999);

    protected CustomLevels(String name, int value) {super(name, value);}
}
