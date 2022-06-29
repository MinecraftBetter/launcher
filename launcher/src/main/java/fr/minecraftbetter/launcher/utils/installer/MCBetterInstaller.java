package fr.minecraftbetter.launcher.utils.installer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.utils.http.HTTP;

import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

public class MCBetterInstaller {
    public static final String MINECRAFTBETTER_GAMEASSETS_API = "https://api.minecraftbetter.com/minecraftbetter/launcher/gameassets";

    private final MinecraftManager minecraftManager;

    final Path minecraftPath;

    public MCBetterInstaller(MinecraftManager minecraftManager) {
        this.minecraftManager = minecraftManager;
        minecraftPath = minecraftManager.minecraftPath;
    }

    public void installMods() {
        JsonObject apiResponse = HTTP.getAsJSONObject(MINECRAFTBETTER_GAMEASSETS_API);
        if(apiResponse == null || apiResponse.get("code").getAsInt() != 200) {Main.logger.warning("Game assets API error"); return; }
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
                else if ((assetFile.exists() && !asset.get("override").getAsBoolean()))
                {
                    Main.logger.fine("The overwrite instruction is set to false, skipping");
                    continue;
                }

                HTTP.downloadFile(asset.get("url").getAsString(),
                        assetFile,
                        p -> minecraftManager.progression(assetProgress.applyAsDouble(p.getPercentage()), MessageFormat.format("{0} - {1}", assetE.getKey(), p)));
            }
        }
    }
}
