package fr.minecraftbetter.launcher.api.launcher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.utils.http.HTTP;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public final class LauncherInfo {
    public static final String MINECRAFTBETTER_INFO_API = "https://api.minecraftbetter.com/minecraftbetter/launcher/info";

    public static LauncherInfo tryGet() {
        JsonObject response = HTTP.getAsJSONObject(MINECRAFTBETTER_INFO_API);
        if (response == null) return null;
        JsonObject results = response.getAsJsonObject("results");
        if (results == null) return null;
        return new Gson().fromJson(results, LauncherInfo.class);
    }


    private final String name;
    private final String copyright;
    private final VersionInfo latest_version;

    public LauncherInfo(String name,
                        String copyright,
                        VersionInfo latest_version) {
        this.name = name;
        this.copyright = copyright;
        this.latest_version = latest_version;
    }

    public String name() {return name;}

    public String copyright() {return copyright;}

    public VersionInfo latest_version() {return latest_version;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LauncherInfo) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.copyright, that.copyright) &&
                Objects.equals(this.latest_version, that.latest_version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, copyright, latest_version);
    }

    @Override
    public String toString() {
        return "LauncherInfo[" +
                "name=" + name + ", " +
                "copyright=" + copyright + ", " +
                "latest_version=" + latest_version + ']';
    }

    public boolean isUpToDate() {
        String currentVersion = Main.getBuildVersion();
        if (currentVersion == null) // This is a test build, and we aren't able to tell if there is a newer version.
        {
            Main.logger.warning(() -> "Version check has been skipped (unknown build version)");
            return true; // We consider (arbitrarily) that this build is more recent.
        }

        boolean upToDate = Objects.equals(currentVersion, latest_version.version_number()); // That *could* be wrong but that's good enough
        if (upToDate) Main.logger.fine("This build is up to date");
        else Main.logger.warning(() -> "A newer build is available (" + currentVersion + " -> " + latest_version.version_number() + ")");
        return upToDate;
    }
}
