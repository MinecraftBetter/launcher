package fr.minecraftbetter.launcher.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.minecraftbetter.launcher.Main;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;

public final class Settings {
    public static final Path SETTINGS = Main.AppData.resolve("settings.json");

    public String Xmx = "4G";

    public Settings() {}

    public Settings(String Xmx) {this.Xmx = Xmx;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Settings) obj;
        return Objects.equals(this.Xmx, that.Xmx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Xmx);
    }

    @Override
    public String toString() {
        return "Settings[" +
                "Xmx=" + Xmx + ']';
    }

    private static Settings settings;

    public static Settings getSettings() {
        if (settings != null) return settings;
        try {
            Reader reader = Files.newBufferedReader(SETTINGS);
            settings = new Gson().fromJson(reader, Settings.class);
        } catch (IOException e) {
            Main.logger.log(Level.WARNING, "Couldn't load settings", e);
            settings = new Settings();
        }
        return settings;
    }

    public void saveSettings() {
        try {Files.writeString(SETTINGS, new GsonBuilder().setPrettyPrinting().create().toJson(this));} catch (IOException e) {
            Main.logger.log(Level.WARNING, "Couldn't save settings", e);
        }
    }
}
