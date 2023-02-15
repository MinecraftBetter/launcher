package fr.minecraftbetter.launcher.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.minecraftbetter.launcher.Main;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public final class Settings {
    public static final Path SETTINGS_PATH = Main.AppData.resolve("settings.json");

    public String Xmx = "4G";
    public int profile = 0;
    public int concurrentDownloads = 5;

    private static Settings settings;

    public static Settings getSettings() {
        if (settings != null) return settings;
        if (!Files.exists(SETTINGS_PATH)) settings = new Settings();
        else {
            try {
                Reader reader = Files.newBufferedReader(SETTINGS_PATH);
                settings = new Gson().fromJson(reader, Settings.class);
            } catch (IOException e) {
                Main.logger.log(Level.WARNING, "Couldn't load settings", e);
                settings = new Settings();
            }
        }
        return settings;
    }

    public void saveSettings() {
        try {Files.writeString(SETTINGS_PATH, new GsonBuilder().setPrettyPrinting().create().toJson(this));} catch (IOException e) {
            Main.logger.log(Level.WARNING, "Couldn't save settings", e);
        }
    }
}
