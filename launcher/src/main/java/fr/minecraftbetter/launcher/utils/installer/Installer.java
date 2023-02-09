package fr.minecraftbetter.launcher.utils.installer;

import com.google.gson.JsonArray;

public interface Installer {
    String getID();
    String getMainClass();
    JsonArray getJWMArguments();
    JsonArray getGameArguments();

    void getProfile();
    void installLibs();
    void installAssets();
}
