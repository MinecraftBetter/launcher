package fr.minecraftbetter.launcher.utils.installer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.utils.http.HTTP;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import java.util.logging.Level;

public class MinecraftInstaller {
    public static final String MINECRAFT_VERSION_MANIFEST_API = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    private JsonObject versionProfile;
    private final MinecraftManager minecraftManager;

    final Path javaPath;
    final Path profilesPath;
    final File minecraftFile;
    final Path libsPath;
    final Path assetsPath;

    public MinecraftInstaller(MinecraftManager minecraftManager, Path installationPath) {
        this.minecraftManager = minecraftManager;

        javaPath = installationPath.resolve("../jre/").toAbsolutePath();
        minecraftFile = installationPath.resolve("minecraft.jar").toFile();
        profilesPath = installationPath.resolve("profiles/");
        libsPath = installationPath.resolve("libraries/");
        assetsPath = installationPath.resolve("assets/");
    }

    public void getProfile() {
        Path profile = profilesPath.resolve(MinecraftManager.WANTED_MINECRAFT_VERSION + ".json");
        try {
            Files.createDirectories(profilesPath);
            if (Files.exists(profile)) {
                versionProfile = JsonParser.parseReader(new FileReader(profile.toFile())).getAsJsonObject();
                return;
            }
        } catch (IOException e) {Main.logger.log(Level.WARNING, "Error while reading profile", e);}

        JsonObject manifest = HTTP.getAsJSONObject(MINECRAFT_VERSION_MANIFEST_API);
        assert manifest != null;
        minecraftManager.progression(1 / 3d);

        Main.logger.fine(() -> MessageFormat.format("Processing {0} releases", manifest.getAsJsonArray("versions").size()));
        for (JsonElement vE : manifest.getAsJsonArray("versions")) {
            JsonObject versionMeta = vE.getAsJsonObject();
            if (!Objects.equals(versionMeta.get("id").getAsString(), MinecraftManager.WANTED_MINECRAFT_VERSION)) continue;

            Main.logger.fine(() -> MessageFormat.format("Found {0} metadata", MinecraftManager.WANTED_MINECRAFT_VERSION));
            Main.logger.finest(versionMeta::toString);
            minecraftManager.progression(2 / 3d);
            versionProfile = HTTP.getAsJSONObject(versionMeta.get("url").getAsString());
            Main.logger.fine(() -> MessageFormat.format("Got {0} profile", MinecraftManager.WANTED_MINECRAFT_VERSION));
            Main.logger.finest(versionProfile::toString);
            assert versionProfile != null;
            try {Files.write(profile, versionProfile.toString().getBytes());} catch (IOException e) {Main.logger.log(Level.WARNING, "Error while writing profile", e);}
            return;
        }
    }

    public void installMinecraft() {
        assert versionProfile != null;
        JsonObject client = versionProfile.get("downloads").getAsJsonObject().get("client").getAsJsonObject();
        if (Utils.checkIntegrity(minecraftFile, client.get("sha1").getAsString())) return;
        HTTP.downloadFile(client.get("url").getAsString(), minecraftFile, p -> minecraftManager.progression(p.getPercentage(), p.toString()));
        Main.logger.fine(() -> MessageFormat.format("Successfully downloaded Minecraft to {0}", minecraftFile.getAbsolutePath()));
    }

    public void installLibs() {
        assert versionProfile != null;
        if (!Utils.tryCreateFolder(libsPath)) return;

        JsonArray libs = versionProfile.get("libraries").getAsJsonArray();
        for (int i = 0; i < libs.size(); i++) {
            JsonObject lib = libs.get(i).getAsJsonObject();
            String libName = lib.get("name").getAsString();
            minecraftManager.progression(i / (double) libs.size(), libName);
            if (!minecraftManager.checkRules(lib)) continue;

            downloadLib(lib.get("downloads").getAsJsonObject().get("artifact").getAsJsonObject(), i, libs.size(), libName);
            if (lib.has("natives")) {
                for (Map.Entry<String, JsonElement> nativeCat : lib.get("natives").getAsJsonObject().entrySet()) {
                    if (!System.getProperty("os.name").toLowerCase().contains(nativeCat.getKey())) continue;
                    JsonObject nativeInfo = lib.get("downloads").getAsJsonObject().get("classifiers").getAsJsonObject().get(nativeCat.getValue().getAsString()).getAsJsonObject();
                    downloadLib(nativeInfo, i, libs.size(), libName);
                }
            }
        }
    }

    private void downloadLib(JsonObject libInfo, int i, double total, String libName) {
        File libPath = libsPath.resolve(libInfo.get("path").getAsString()).toFile();
        if (Utils.checkIntegrity(libPath, libInfo.get("sha1").getAsString())) return;
        if (!Utils.tryCreateFolder(libPath.getParentFile().toPath())) return;

        HTTP.downloadFile(libInfo.get("url").getAsString(), libPath,
                p -> minecraftManager.progression((i + p.getPercentage()) / total, MessageFormat.format("{0} - {1}", libName, p)));
    }

    public void installAssets() {

        assert versionProfile != null;

        JsonObject assetIndexInfo = versionProfile.get("assetIndex").getAsJsonObject();
        Path assetIndexes = assetsPath.resolve("indexes");
        if (!Utils.tryCreateFolder(assetIndexes)) return;
        File index = assetIndexes.resolve(assetIndexInfo.get("id").getAsString() + ".json").toFile();
        if (!Utils.checkIntegrity(index, assetIndexInfo.get("sha1").getAsString()) && !HTTP.downloadFile(assetIndexInfo.get("url").getAsString(), index, null)) return;
        JsonObject assetIndex;
        try {assetIndex = JsonParser.parseReader(new FileReader(index)).getAsJsonObject();} catch (IOException e) {
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error while reading {0}", index));
            return;
        }

        int fi = -1;
        for (Map.Entry<String, JsonElement> folderE : assetIndex.entrySet()) {
            fi += 1;
            Path assetFolderPath = assetsPath.resolve(folderE.getKey());
            if (!Utils.tryCreateFolder(assetFolderPath)) return;

            JsonObject folder = folderE.getValue().getAsJsonObject();
            int i = -1;
            for (Map.Entry<String, JsonElement> assetE : folder.entrySet()) {
                i += 1;
                JsonObject asset = assetE.getValue().getAsJsonObject();
                String assetHash = asset.get("hash").getAsString();
                Path assetRelativePath = Paths.get(assetHash.substring(0, 2), assetHash);
                File assetFile = assetFolderPath.resolve(assetRelativePath).toFile();

                int finalI = i;
                int finalFi = fi;
                DoubleUnaryOperator assetProgress = (double p) -> ((finalI + p) / folder.size() + finalFi) / assetIndex.size();
                minecraftManager.progression(assetProgress.applyAsDouble(0), assetE.getKey());
                if (Utils.checkIntegrity(assetFile, assetHash) || !Utils.tryCreateFolder(assetFile.getParentFile().toPath())) continue;

                HTTP.downloadFile(
                        "https://resources.download.minecraft.net/" + assetRelativePath,
                        assetFile,
                        p -> minecraftManager.progression(assetProgress.applyAsDouble(p.getPercentage()), MessageFormat.format("{0} - {1}", assetE.getKey(), p)));
            }
        }
    }


    public String getID() {
        assert versionProfile != null;
        return versionProfile.get("id").getAsString();
    }

    public String getMainClass() {
        assert versionProfile != null;
        return versionProfile.get("mainClass").getAsString();
    }

    public JsonArray getJWMArguments() {
        assert versionProfile != null;
        return versionProfile.get("arguments").getAsJsonObject().get("jvm").getAsJsonArray();
    }

    public JsonArray getGameArguments() {
        assert versionProfile != null;
        return versionProfile.get("arguments").getAsJsonObject().get("game").getAsJsonArray();
    }

    public String getAssetIndexID(){
        return versionProfile.getAsJsonObject("assetIndex").get("id").getAsString();
    }
}
