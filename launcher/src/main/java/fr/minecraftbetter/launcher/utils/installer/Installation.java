package fr.minecraftbetter.launcher.utils.installer;

import java.util.ArrayList;
import java.util.Objects;

public class Installation {
    String profileName;
    int version;

    final String wantedMinecraftVersion;
    final String wantedJavaVersion;
    final ArrayList<Loader> modLoaders = new ArrayList<>();

    public Installation(String wantedMinecraftVersion, String wantedJavaVersion) {
        this.wantedMinecraftVersion = wantedMinecraftVersion;
        this.wantedJavaVersion = wantedJavaVersion;

        this.profileName = wantedMinecraftVersion;
        this.version = 0;

        if(Objects.equals(this.wantedMinecraftVersion, "1.19.3")) modLoaders.add(Loader.FABRIC);
    }

    public Installation setProfileName(String profileName) {
        this.profileName = profileName;
        return this;
    }

    public Installation setVersion(int version) {
        this.version = version;
        return this;
    }
}
