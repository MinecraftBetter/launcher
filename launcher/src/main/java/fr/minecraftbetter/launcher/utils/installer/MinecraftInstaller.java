package fr.minecraftbetter.launcher.utils.installer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.utils.http.ConcurrentDownloader;
import fr.minecraftbetter.launcher.utils.http.DownloadTask;
import fr.minecraftbetter.launcher.utils.http.HTTP;
import org.apache.commons.text.StringSubstitutor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import java.util.logging.Level;

public class MinecraftInstaller implements Installer {
    public static final String MINECRAFT_VERSION_MANIFEST_API = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    private JsonObject versionProfile;
    private final MinecraftManager minecraftManager;

    final Path javaPath;
    final Path profilesPath;
    final File minecraftFile;
    final Path nativeLibsPath;
    final Path libsPath;
    final Path assetsPath;

    public MinecraftInstaller(MinecraftManager minecraftManager) {
        this.minecraftManager = minecraftManager;

        javaPath = minecraftManager.javaPath;
        minecraftFile = minecraftManager.minecraftPath.resolve("minecraft.jar").toFile();
        profilesPath = minecraftManager.minecraftPath.resolve("profiles/");
        nativeLibsPath = minecraftManager.minecraftPath.resolve(".natives/");
        libsPath = minecraftManager.minecraftPath.resolve("libraries/");
        assetsPath = minecraftManager.minecraftPath.resolve("assets/");
    }

    public void getProfile() {
        Path profile = profilesPath.resolve(minecraftManager.installationProfile.wantedMinecraftVersion + ".json");
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
            if (!Objects.equals(versionMeta.get("id").getAsString(), minecraftManager.installationProfile.wantedMinecraftVersion)) continue;

            Main.logger.fine(() -> MessageFormat.format("Found {0} metadata", minecraftManager.installationProfile.wantedMinecraftVersion));
            Main.logger.finest(versionMeta::toString);
            minecraftManager.progression(2 / 3d);
            versionProfile = HTTP.getAsJSONObject(versionMeta.get("url").getAsString());
            Main.logger.fine(() -> MessageFormat.format("Got {0} profile", minecraftManager.installationProfile.wantedMinecraftVersion));
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
        ConcurrentDownloader downloader = new ConcurrentDownloader();
        for (int i = 0; i < libs.size(); i++) {
            JsonObject lib = libs.get(i).getAsJsonObject();
            String libName = lib.get("name").getAsString();
            minecraftManager.progression(i / (double) libs.size(), libName);
            if (minecraftManager.rulesAreUnmatched(lib)) continue;

            var downloads = lib.get("downloads").getAsJsonObject();
            if(downloads.has("artifact")) {
                var task = downloadLib(downloads.get("artifact").getAsJsonObject(), libName);
                if (task != null) downloader.addTask(task);
            }
            if (lib.has("natives")) {
                var classifiers = downloads.get("classifiers").getAsJsonObject();
                for (Map.Entry<String, JsonElement> nativeCat : lib.get("natives").getAsJsonObject().entrySet()) {
                    if (!System.getProperty("os.name").toLowerCase().replace(" ","").contains(nativeCat.getKey())) continue;

                    Map<String, String> values = new HashMap<>();
                    values.put("arch", Utils.getArch().replace("x", ""));
                    var nativeKey = new StringSubstitutor(values).replace(nativeCat.getValue().getAsString());
                    JsonObject nativeInfo = classifiers.get(nativeKey).getAsJsonObject();

                    var task = downloadLib(nativeInfo, libName);
                    if (task != null) downloader.addTask(task);
                }
            }
        }
        downloader.onProgress(p -> minecraftManager.progression(p.getPercentage(), p.getStatus()));
        var thread = downloader.thread();
        thread.start();
        try {thread.join();} catch (InterruptedException e) {Thread.currentThread().interrupt();}
    }

    private DownloadTask downloadLib(JsonObject libInfo, String libName) {
        File libPath = libsPath.resolve(libInfo.get("path").getAsString()).toFile();
        if (Utils.checkIntegrity(libPath, libInfo.get("sha1").getAsString())) return null;
        if (!Utils.tryCreateFolder(libPath.getParentFile().toPath())) return null;

        return new DownloadTask(libInfo.get("url").getAsString(), libPath, libName);
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
        ConcurrentDownloader downloader = new ConcurrentDownloader();
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

                downloader.addTask(new DownloadTask(
                        "https://resources.download.minecraft.net/" + assetRelativePath,
                        assetFile,
                        assetE.getKey()));
            }
        }

        downloader.onProgress(p -> minecraftManager.progression(p.getPercentage(), p.getStatus()));
        var thread = downloader.thread();
        thread.start();
        try {thread.join();} catch (InterruptedException e) {Thread.currentThread().interrupt();}
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
        if(versionProfile.has("arguments")) return versionProfile.get("arguments").getAsJsonObject().get("jvm").getAsJsonArray();
        else {
            var array = new JsonArray();
            array.add("-Djava.library.path=${natives_directory}");
            array.add("-Dminecraft.launcher.brand=${launcher_name}");
            array.add("-Dminecraft.launcher.version=${launcher_version}");
            array.add("-cp");
            array.add("${classpath}");
            return array;
        }
    }

    public JsonArray getGameArguments() {
        assert versionProfile != null;
        if(versionProfile.has("arguments")) return versionProfile.get("arguments").getAsJsonObject().get("game").getAsJsonArray();
        else if(versionProfile.has("minecraftArguments")) {
            var array = new JsonArray();
            for (String arg: versionProfile.get("minecraftArguments").getAsString().split(" ")) array.add(arg);
            return array;
        }
        return null;
    }

    public String getAssetIndexID() {
        return versionProfile.getAsJsonObject("assetIndex").get("id").getAsString();
    }
}
