package fr.minecraftbetter.launcher.utils.installer;

public class Installation {
    String profileName;
    int version;

    final String wantedMinecraftVersion;
    final String wantedJavaVersion;

    public Installation(String wantedMinecraftVersion, String wantedJavaVersion)
    {
        this.wantedMinecraftVersion = wantedMinecraftVersion;
        this.wantedJavaVersion = wantedJavaVersion;

        this.profileName = wantedMinecraftVersion;
        this.version = 0;
    }

    public Installation setProfileName(String profileName){ this.profileName = profileName; return this; }
    public Installation setVersion(int version){ this.version = version; return this; }
}
