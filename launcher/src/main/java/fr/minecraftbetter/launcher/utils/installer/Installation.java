package fr.minecraftbetter.launcher.utils.installer;

import com.google.gson.GsonBuilder;
import fr.minecraftbetter.launcher.Main;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;

public class Installation {
    String profileName;
    int gameAssetVersion;
    String wantedMinecraftVersion;
    String wantedJavaVersion;
    final ArrayList<Loader> modLoaders = new ArrayList<>();


    public static Installation get(String profileName) {
        var data = read(profileName);
        return data == null ? new Installation(profileName) : data;
    }

    public Installation(String wantedMinecraftVersion) {
        setProfileName(wantedMinecraftVersion);
        setWantedMinecraftVersion(wantedMinecraftVersion);
        setWantedJavaVersion("18");
    }

    public String getProfileName() {
        return this.profileName;
    }

    public Installation setProfileName(String profileName) {
        this.profileName = profileName;
        return this;
    }

    public Installation setWantedMinecraftVersion(String wantedMinecraftVersion) {
        this.wantedMinecraftVersion = wantedMinecraftVersion;
        return this;
    }

    public Installation setWantedJavaVersion(String wantedJavaVersion) {
        this.wantedJavaVersion = wantedJavaVersion;
        return this;
    }

    public int getGameAssetVersion() {return this.gameAssetVersion;}

    public Installation setGameAssetVersion(int version) {
        this.gameAssetVersion = version;
        save();
        return this;
    }

    public Installation addModLoader(Loader loader) {
        if (!modLoaders.contains(loader)) modLoaders.add(loader);
        return this;
    }

    public Path getInstallationPath() {return getInstallationPath(profileName);}
    public static Path getInstallationPath(String profileName) {
        return MinecraftManager.INSTALLATION_PATH.resolve("minecraft/").resolve(profileName).toAbsolutePath();
    }

    static Installation read(String profileName) {
        var profilePath = getInstallationPath(profileName).resolve("profiles/profile.json");
        if(!Files.exists(profilePath)) return null;
        try {
            return new GsonBuilder().create().fromJson(new FileReader(profilePath.toFile()), Installation.class);
        } catch (Exception e) {
            Main.logger.log(Level.WARNING, "Error reading profile at " + profilePath, e);
            return null;
        }
    }

    public void save() {
        try {
            var profilePath = getInstallationPath().resolve("profiles/");
            Utils.tryCreateFolder(profilePath);
            Files.writeString(profilePath.resolve("profile.json"), new GsonBuilder().setPrettyPrinting().create().toJson(this));
        } catch (IOException e) {
            Main.logger.log(Level.WARNING, "Couldn't save settings", e);
        }
    }
}
