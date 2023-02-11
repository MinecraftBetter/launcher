package fr.minecraftbetter.launcher.utils.installer;

import java.util.ArrayList;
import java.util.Objects;

public class Installation {
    String profileName;
    int version;
    String wantedMinecraftVersion;
    String wantedJavaVersion;
    final ArrayList<Loader> modLoaders = new ArrayList<>();

    public static Installation get(String profileName){
        return new Installation(profileName);
    }

    public Installation(String wantedMinecraftVersion) {
        this.profileName = wantedMinecraftVersion;
        this.wantedMinecraftVersion = wantedMinecraftVersion;
        this.wantedJavaVersion = "18";
        this.version = 0;

        if(Objects.equals(this.wantedMinecraftVersion, "1.19.3")) modLoaders.add(Loader.FABRIC);
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

    public Installation setVersion(int version) {
        this.version = version;
        return this;
    }

    public Installation addModLoader(Loader loarder) {
        modLoaders.add(loarder);
        return this;
    }
}
