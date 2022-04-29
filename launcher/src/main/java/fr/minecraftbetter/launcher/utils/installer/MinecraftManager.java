package fr.minecraftbetter.launcher.utils.installer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.utils.http.HTTP;
import javafx.application.Platform;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.logging.Level;

/** Installs Minecraft and it's dependencies **/
public class MinecraftManager {
    public static final String MINECRAFT_VERSION_MANIFEST_API = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    public static final String WANTED_MINECRAFT_VERSION = "1.18.2";

    private final Path installationPath;
    ArrayList<Pair<Runnable, String>> actions;

    public MinecraftManager(Path installationPath) {
        this.installationPath = installationPath;

        actions = new ArrayList<>();
        actions.add(new Pair<>(this::getVersionProfile, "Initializing"));
        actions.add(new Pair<>(this::installMinecraft, "Installing Minecraft"));
        actions.add(new Pair<>(this::installMinecraftLibraries, "Installing Minecraft libraries"));
        actions.add(new Pair<>(this::installMinecraftAssets, "Installing Minecraft assets"));
        actions.add(new Pair<>(this::installFabric, "Installing Fabric"));
        actions.add(new Pair<>(this::installMods, "Installing mods"));
    }

    private Consumer<Progress> progress;
    private Runnable complete = () -> {};
    private int status = 0;

    private String getStatusMessage() {return status < actions.size() ? actions.get(status).getValue() : null;}

    public void setProgress(Consumer<Progress> progress) {this.progress = progress;}

    public void setComplete(Runnable complete) {this.complete = complete;}


    private void progression() {progression(0);}

    private void progression(double percentage) {progression(percentage, null);}

    private void progression(double percentage, String detail) {
        if (progress == null) return;
        Platform.runLater(() -> progress.accept(new Progress(
                percentage,
                status,
                actions.size(),
                detail == null ? getStatusMessage() : MessageFormat.format("{0} ({1})", getStatusMessage(), detail))));
    }


    public void startInstall() {
        try {Files.createDirectories(installationPath);} catch (IOException e) {
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Couldn''t create/access installation path {0}", installationPath));
        }
        new Thread(() -> {
            for (; status < actions.size(); status++) {
                progression();
                Main.logger.info(getStatusMessage());
                actions.get(status).getKey().run();
            }
            Platform.runLater(() -> {
                progress.accept(new Progress(1, "Installation done"));
                Main.logger.info("Installation done");
                complete.run();
            });
        }).start();
    }

    public Boolean verifyInstall() {
        return false; // TODO: Really check
    }

    public Boolean startGame() {
        return verifyInstall();
        //TODO: Launch the game
    }


    private JsonObject versionProfile;

    private void getVersionProfile() {
        JsonObject manifest = HTTP.getAsJSON(MINECRAFT_VERSION_MANIFEST_API);
        assert manifest != null;
        progression(1 / 3d);

        Main.logger.fine(() -> MessageFormat.format("Processing {0} releases", manifest.getAsJsonArray("versions").size()));
        for (JsonElement vE : manifest.getAsJsonArray("versions")) {
            JsonObject versionMeta = vE.getAsJsonObject();
            if (!Objects.equals(versionMeta.get("id").getAsString(), WANTED_MINECRAFT_VERSION)) continue;

            Main.logger.fine(() -> MessageFormat.format("Found {0} metadata", WANTED_MINECRAFT_VERSION));
            Main.logger.finest(versionMeta::toString);
            progression(2 / 3d);
            versionProfile = HTTP.getAsJSON(versionMeta.get("url").getAsString());
            Main.logger.fine(() -> MessageFormat.format("Got {0} profile", WANTED_MINECRAFT_VERSION));
            Main.logger.finest(versionProfile::toString);
            assert versionProfile != null;
            return;
        }
    }

    private void installMinecraft() {
        assert versionProfile != null;
        File minecraft = installationPath.resolve("minecraft.jar").toFile();
        JsonObject client = versionProfile.get("downloads").getAsJsonObject().get("client").getAsJsonObject();

        if (checkIntegrity(minecraft, client.get("sha1").getAsString())) return;

        try {
            HTTP.getFile(
                    client.get("url").getAsString(),
                    new FileOutputStream(minecraft),
                    p -> progression(p.getPercentage(), p.toString()));
        } catch (IOException e) {
            Main.logger.log(Level.SEVERE, "Error while downloading Minecraft", e);
            return;
        }
        Main.logger.fine(() -> MessageFormat.format("Successfully downloaded Minecraft to {0}", minecraft.getAbsolutePath()));
    }

    private void installMinecraftLibraries() {
        assert versionProfile != null;
        Path libsPath = installationPath.resolve("libraries/");
        if (!tryCreateFolder(libsPath)) return;

        JsonArray libs = versionProfile.get("libraries").getAsJsonArray();
        for (int i = 0; i < libs.size(); i++) {
            JsonObject lib = libs.get(i).getAsJsonObject();
            String libName = lib.get("name").getAsString();
            double libProgress = i / (double) libs.size();
            progression(libProgress, libName);
            JsonObject libInfo = lib.get("downloads").getAsJsonObject().get("artifact").getAsJsonObject();
            File libPath = libsPath.resolve(libInfo.get("path").getAsString()).toFile();
            if (checkIntegrity(libPath, libInfo.get("sha1").getAsString())) continue;

            try {
                Files.createDirectories(libPath.getParentFile().toPath());
                HTTP.getFile(
                        libInfo.get("url").getAsString(),
                        new FileOutputStream(libPath),
                        p -> progression(libProgress + p.getPercentage() / libs.size(), MessageFormat.format("{0} - {1}", libName, p)));
                Main.logger.fine(() -> MessageFormat.format("Successfully downloaded {0} to {1}", libName, libPath.getAbsolutePath()));
            } catch (IOException e) {Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error while downloading {0}", libName));}
        }
    }

    private void installMinecraftAssets() {
        assert versionProfile != null;
        Path assetsPath = installationPath.resolve("assets/");

        JsonObject assetIndexInfo = versionProfile.get("assetIndex").getAsJsonObject();
        Path assetIndexes = assetsPath.resolve("indexes");
        if (!tryCreateFolder(assetIndexes)) return;
        File index = assetIndexes.resolve(assetIndexInfo.get("id").getAsString() + ".json").toFile();
        if (!checkIntegrity(index, assetIndexInfo.get("sha1").getAsString()) && !HTTP.downloadFile(assetIndexInfo.get("url").getAsString(), index, null)) return;
        JsonObject assetIndex;
        try {assetIndex = JsonParser.parseReader(new FileReader(index)).getAsJsonObject();} catch (IOException e) {
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error while reading {0}", index));
            return;
        }

        int fi = -1;
        for (Map.Entry<String, JsonElement> folderE : assetIndex.entrySet()) {
            fi += 1;
            Path assetFolderPath = assetsPath.resolve(folderE.getKey());
            if (!tryCreateFolder(assetFolderPath)) return;

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
                progression(assetProgress.applyAsDouble(0), assetE.getKey());
                if (checkIntegrity(assetFile, assetHash) || !tryCreateFolder(assetFile.getParentFile().toPath())) continue;

                HTTP.downloadFile(
                            "https://resources.download.minecraft.net/" + assetRelativePath,
                            assetFile,
                            p -> progression(assetProgress.applyAsDouble(p.getPercentage()), MessageFormat.format("{0} - {1}", assetE.getKey(), p)));
            }
        }
    }

    private void installFabric() {
        //TODO
    }

    private void installMods() {
        //TODO
    }

    @NotNull
    private Boolean tryCreateFolder(Path path) {
        try {Files.createDirectories(path);} catch (IOException e) {
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Couldn''t create/access folder at {0}", path.toAbsolutePath()));
            return false;
        }
        return true;
    }

    private String calculateSha1(File file) {
        MessageDigest digest;
        try {digest = MessageDigest.getInstance("SHA-1");} catch (NoSuchAlgorithmException e) {
            Main.logger.log(Level.SEVERE, "Error getting SHA1 algorithm", e);
            return null;
        }
        try (InputStream fis = new FileInputStream(file)) {
            int n = 0;
            byte[] buffer = new byte[8192];
            while (n != -1) {
                n = fis.read(buffer);
                if (n > 0) digest.update(buffer, 0, n);
            }
            return new HexBinaryAdapter().marshal(digest.digest()).toLowerCase();
        } catch (FileNotFoundException e) {
            Main.logger.log(Level.SEVERE, "Error opening file", e);
            return null;
        } catch (IOException e) {
            Main.logger.log(Level.WARNING, "IO error", e);
            return null;
        }
    }

    @NotNull
    private Boolean checkIntegrity(File file, String sha) {
        if (!file.exists()) return false;

        Main.logger.fine(() -> MessageFormat.format("Found existing installation at {0}", file.getAbsolutePath()));
        String fileSha = calculateSha1(file);
        if (fileSha == null) return false;
        if (Objects.equals(fileSha, sha.toLowerCase())) {
            Main.logger.fine("SHA-1 matching, skipping");
            return true;
        }
        Main.logger.fine(() -> MessageFormat.format("SHA-1 aren''t matching, found {0} expected {1}", fileSha, sha.toLowerCase()));
        return false;
    }
}
