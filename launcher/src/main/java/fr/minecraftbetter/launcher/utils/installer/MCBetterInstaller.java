package fr.minecraftbetter.launcher.utils.installer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.utils.http.ConcurrentDownloader;
import fr.minecraftbetter.launcher.utils.http.DownloadTask;
import fr.minecraftbetter.launcher.utils.http.HTTP;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.logging.Level;

public class MCBetterInstaller {
    public static final String MINECRAFTBETTER_GAMEASSETS_API = "https://api.minecraftbetter.com/minecraftbetter/launcher/gameassets/get?from=";

    private final MinecraftManager minecraftManager;

    final Path minecraftPath;
    final File gameAssetsManifestFile;

    public MCBetterInstaller(MinecraftManager minecraftManager, Path installationPath) {
        this.minecraftManager = minecraftManager;
        minecraftPath = minecraftManager.minecraftPath;
        gameAssetsManifestFile = installationPath.resolve("assets.json").toFile();
    }

    public void installMods() {
        JsonObject apiResponse = HTTP.getAsJSONObject(MINECRAFTBETTER_GAMEASSETS_API);
        if (apiResponse == null || apiResponse.get("code").getAsInt() != 200) {
            Main.logger.warning("Game assets API error");
            return;
        }
        if (!apiResponse.get("results").isJsonObject()) return;
        JsonObject gameAssets = apiResponse.get("results").getAsJsonObject();

        int fi = -1;
        ConcurrentDownloader downloader = new ConcurrentDownloader();
        for (Map.Entry<String, JsonElement> versionE : gameAssets.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            fi += 1;
            Main.logger.fine("Processing tag n°" + versionE.getKey());

            JsonObject folder = versionE.getValue().getAsJsonObject();
            int i = -1;
            for (Map.Entry<String, JsonElement> assetE : folder.entrySet()) {
                i += 1;
                JsonObject asset = assetE.getValue().getAsJsonObject();
                File assetFile = minecraftPath.resolve(asset.get("path").getAsString()).toFile();
                if(!assetFile.toPath().startsWith(minecraftPath)) {
                    Main.logger.severe("Security issue, the current file is located outside the authorised path");
                }
                if (asset.has("delete") && asset.get("delete").getAsBoolean()) {
                    try {
                        delete(assetFile.toPath());
                    } catch (IOException e) {
                        Main.logger.log(Level.SEVERE, "Couldn't delete " + assetFile, e);
                    }
                    continue;
                }
                String assetHash = asset.get("hash").getAsString();

                int finalI = i;
                int finalFi = fi;
                DoubleUnaryOperator assetProgress = (double p) -> ((finalI + p) / folder.size() + finalFi) / gameAssets.size();
                minecraftManager.progression(assetProgress.applyAsDouble(0), assetE.getKey());
                if (Utils.checkIntegrity(assetFile, assetHash) || !Utils.tryCreateFolder(assetFile.getParentFile().toPath())) continue;
                else if ((assetFile.exists() && !asset.get("override").getAsBoolean())) {
                    Main.logger.fine("The overwrite instruction is set to false, skipping");
                    continue;
                }

                downloader.addTask(new DownloadTask(asset.get("url").getAsString(), assetFile, assetE.getKey()));
            }
        }

        downloader.onProgress(p -> minecraftManager.progression(p.getPercentage(), p.getStatus()));
        var thread = downloader.thread();
        thread.start();
        try {thread.join();} catch (InterruptedException e) {Thread.currentThread().interrupt();}
    }

    /**
     * Delete a file/directory using recursion
     */
    public static void delete(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var directory = Files.newDirectoryStream(path)) {
                for (Path file : directory) { //list all the files in directory
                    delete(file); //recursive delete
                }
            }
        }

        // We can delete it
        Files.delete(path);
        Main.logger.finest(() -> "Deleting " + path);
    }
}
