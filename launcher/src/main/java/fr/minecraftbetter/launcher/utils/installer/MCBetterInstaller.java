package fr.minecraftbetter.launcher.utils.installer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.utils.http.HTTP;

import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

public class MCBetterInstaller {
    public static final String MINECRAFTBETTER_GAMEASSETS_API = "https://api.minecraftbetter.com/minecraftbetter/launcher/gameassets";
    public static final String MINECRAFTBETTER_INFO_API = "https://api.minecraftbetter.com/minecraftbetter/launcher/info";

    private final MinecraftManager minecraftManager;

    final Path minecraftPath;

    public MCBetterInstaller(MinecraftManager minecraftManager) {
        this.minecraftManager = minecraftManager;
        minecraftPath = minecraftManager.minecraftPath;
    }

    public void installMods() {
        JsonObject apiResponse = HTTP.getAsJSONObject(MINECRAFTBETTER_GAMEASSETS_API);
        if (apiResponse == null || apiResponse.get("code").getAsInt() != 200) {
            Main.logger.warning("Game assets API error");
            return;
        }
        JsonObject gameAssets = apiResponse.get("results").getAsJsonObject();

        int fi = -1;
        for (Map.Entry<String, JsonElement> folderE : gameAssets.entrySet()) {
            fi += 1;
            Path assetFolderPath = minecraftPath.resolve(folderE.getKey());
            if (!Utils.tryCreateFolder(assetFolderPath)) return;

            JsonObject folder = folderE.getValue().getAsJsonObject();
            int i = -1;
            for (Map.Entry<String, JsonElement> assetE : folder.entrySet()) {
                i += 1;
                JsonObject asset = assetE.getValue().getAsJsonObject();
                String assetHash = asset.get("hash").getAsString();
                File assetFile = assetFolderPath.resolve(asset.get("path").getAsString()).toFile();

                int finalI = i;
                int finalFi = fi;
                DoubleUnaryOperator assetProgress = (double p) -> ((finalI + p) / folder.size() + finalFi) / gameAssets.size();
                minecraftManager.progression(assetProgress.applyAsDouble(0), assetE.getKey());
                if (Utils.checkIntegrity(assetFile, assetHash) || !Utils.tryCreateFolder(assetFile.getParentFile().toPath())) continue;
                else if ((assetFile.exists() && !asset.get("override").getAsBoolean())) {
                    Main.logger.fine("The overwrite instruction is set to false, skipping");
                    continue;
                }

                HTTP.downloadFile(asset.get("url").getAsString(),
                        assetFile,
                        p -> minecraftManager.progression(assetProgress.applyAsDouble(p.getPercentage()), MessageFormat.format("{0} - {1}", assetE.getKey(), p)));
            }
        }
    }

    public static boolean isUpToDate() {
        String currentVersion = Main.getBuildVersion();
        if (currentVersion == null) // This is a test build, and we aren't able to tell if there is a newer version.
        {
            Main.logger.warning(() -> "Version check has been skipped (unknown build version)");
            return true; // We consider (arbitrarily) that this build is more recent.
        }

        JsonObject response = HTTP.getAsJSONObject(MINECRAFTBETTER_INFO_API);
        assert response != null;
        JsonObject latestVersionInfo = response.getAsJsonObject("results").getAsJsonObject("latest_version");
        String latestVersion = latestVersionInfo.get("version_number").getAsString();

        boolean upToDate = Objects.equals(currentVersion, latestVersion); // That *could* be wrong but that's good enough
        if (upToDate) Main.logger.fine("This build is up to date");
        else Main.logger.warning(() -> "A newer build is available (" + currentVersion + " -> " + latestVersion + ")");
        return upToDate;
    }
}
