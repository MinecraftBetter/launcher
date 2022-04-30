package fr.minecraftbetter.launcher.utils.installer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.utils.http.HTTP;
import javafx.application.Platform;
import javafx.util.Pair;
import org.apache.commons.text.StringSubstitutor;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Installs Minecraft and it's dependencies **/
public class MinecraftManager {
    public static final String MINECRAFT_VERSION_MANIFEST_API = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    public static final String WANTED_MINECRAFT_VERSION = "1.18.2";
    public static final String JAVA_EXECUTABLE = "C:\\Users\\evan\\.jdks\\openjdk-18.0.1\\bin\\java.exe";

    private final MinecraftProfile account;
    private final String accessToken;

    private final Path installationPath;
    private final Path profilesPath;
    private final File minecraftFile;
    private final Path libsPath;
    private final Path assetsPath;
    private final Path nativesPath;
    ArrayList<Pair<Runnable, String>> actions;

    public MinecraftManager(Path installationPath, MinecraftProfile account, String accessToken) {
        this.account = account;
        this.accessToken = accessToken;

        this.installationPath = installationPath;
        minecraftFile = installationPath.resolve("minecraft.jar").toFile();
        profilesPath = installationPath.resolve("profiles/");
        libsPath = installationPath.resolve("libraries/");
        assetsPath = installationPath.resolve("assets/");
        nativesPath = installationPath.resolve("natives/");

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

    private static boolean checked = false;

    public boolean verifyInstall() {
        if (!checked) {
            // TODO: Really check
            checked = true;
            return false;
        } else return true;
    }

    public enum StartStatus {STARTED, ERROR, INCOMPLETE_INSTALL}

    public StartStatus startGame() {
        if (!verifyInstall()) return StartStatus.INCOMPLETE_INSTALL;

        if (versionProfile == null) getVersionProfile(); // Used to get launch arguments of Minecraft
        JsonObject arguments = versionProfile.get("arguments").getAsJsonObject();

        ProcessBuilder builder = new ProcessBuilder();


        StringBuilder libsToLoad = new StringBuilder();
        try (Stream<Path> libs = Files.find(libsPath, 25, (f, a) -> f.toFile().getName().endsWith(".jar"))) {
            for (Path lib : libs.collect(Collectors.toList())) {
                libsToLoad.append(lib).append(";");
            }
        } catch (IOException e) {
            Main.logger.log(Level.SEVERE, "Error while reading libraries", e);
            return StartStatus.ERROR;
        }
        libsToLoad.append(minecraftFile.toPath());

        ArrayList<String> commands = new ArrayList<>();
        commands.add(JAVA_EXECUTABLE);
        commands.addAll(compileArguments(arguments.get("jvm").getAsJsonArray(), libsToLoad.toString()));
        commands.add(versionProfile.get("mainClass").getAsString());
        commands.addAll(compileArguments(arguments.get("game").getAsJsonArray(), libsToLoad.toString()));

        builder.directory(minecraftFile.getParentFile());
        builder.command(commands);

        StringBuilder entireCommand = new StringBuilder();
        for (String command : commands)
            entireCommand.append(command).append(" ");

        Main.logger.info("Launching Minecraft");
        Main.logger.fine(() -> "Arguments: " + entireCommand);
        try {builder.start();} catch (IOException e) {
            Main.logger.log(Level.SEVERE, "Error starting Minecraft", e);
        }

        return StartStatus.STARTED;
    }

    public List<String> compileArguments(JsonArray argsJson, String classpath) {
        Map<String, String> values = new HashMap<>();
        // Game
        values.put("auth_player_name", account.getName());
        values.put("version_name", versionProfile.get("id").getAsString());
        values.put("game_directory", installationPath.toString());
        values.put("assets_root", assetsPath.toString());
        values.put("assets_index_name", versionProfile.getAsJsonObject("assetIndex").get("id").getAsString());
        values.put("auth_uuid", account.getId());
        values.put("auth_access_token", accessToken);
        values.put("clientid", "0"); //TODO
        values.put("auth_xuid", "0"); //TODO
        values.put("user_type", "microsoft"); //TODO
        values.put("version_type", "java"); //TODO
        values.put("resolution_width", "1280");
        values.put("resolution_height", "720");
        //JWM
        values.put("natives_directory", nativesPath.toString());
        values.put("launcher_name", "MinecraftBetter");
        values.put("launcher_version", "1.0");
        values.put("classpath", classpath);


        List<String> args = new ArrayList<>();
        for (JsonElement arg : argsJson) {

            JsonArray argValues = new JsonArray();
            if (arg.isJsonPrimitive()) argValues.add(arg.getAsString());
            else if (arg.isJsonObject()) {
                JsonObject argData = arg.getAsJsonObject();
                if (!checkRule(argData)) continue;
                JsonElement argValuesE = argData.get("value");
                if (argValuesE.isJsonArray()) argValues = argValuesE.getAsJsonArray();
                else argValues.add(argValuesE.getAsString());
            }

            for (JsonElement argValE : argValues) {
                StringSubstitutor sub = new StringSubstitutor(values);
                args.add(sub.replace(argValE.getAsString()));
            }
        }
        return args;
    }

    public boolean checkRule(JsonObject node) {
        Map<String, Boolean> features = new HashMap<>();
        features.put("is_demo_user", false); // Not a demo
        features.put("has_custom_resolution", true);

        if (!node.has("rules")) return true;
        boolean ruleUnmatched = false;
        for (JsonElement ruleE : node.get("rules").getAsJsonArray()) {
            JsonObject rule = ruleE.getAsJsonObject();
            boolean allow = Objects.equals(rule.get("action").getAsString(), "allow");

            if (rule.has("os")) {
                for (Map.Entry<String, JsonElement> osRule : rule.get("os").getAsJsonObject().entrySet())
                    if (System.getProperty("os." + osRule.getKey()).matches(osRule.getValue().getAsString())) ruleUnmatched = !allow;
                    else if (allow) ruleUnmatched = true;
            } else if (rule.has("features")) {
                for (Map.Entry<String, JsonElement> featureRule : rule.get("features").getAsJsonObject().entrySet())
                    if (features.containsKey(featureRule.getKey()) && Boolean.TRUE.equals(features.get(featureRule.getKey()))) ruleUnmatched = !allow;
                    else if (allow) ruleUnmatched = true;
            } else ruleUnmatched = !allow;
            if (ruleUnmatched) break;
        }
        return !ruleUnmatched;
    }


    private JsonObject versionProfile;

    private void getVersionProfile() {
        Path profile = profilesPath.resolve(WANTED_MINECRAFT_VERSION + ".json");
        try {
            Files.createDirectories(profilesPath);
            if (Files.exists(profile)) {
                versionProfile = JsonParser.parseReader(new FileReader(profile.toFile())).getAsJsonObject();
                return;
            }
        } catch (IOException e) {Main.logger.log(Level.WARNING, "Error while reading profile", e);}

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
            try {Files.write(profile, versionProfile.toString().getBytes());} catch (IOException e) {Main.logger.log(Level.WARNING, "Error while writing profile", e);}
            return;
        }
    }

    private void installMinecraft() {
        assert versionProfile != null;
        JsonObject client = versionProfile.get("downloads").getAsJsonObject().get("client").getAsJsonObject();

        if (checkIntegrity(minecraftFile, client.get("sha1").getAsString())) return;

        try {
            HTTP.getFile(
                    client.get("url").getAsString(),
                    new FileOutputStream(minecraftFile),
                    p -> progression(p.getPercentage(), p.toString()));
        } catch (IOException e) {
            Main.logger.log(Level.SEVERE, "Error while downloading Minecraft", e);
            return;
        }
        Main.logger.fine(() -> MessageFormat.format("Successfully downloaded Minecraft to {0}", minecraftFile.getAbsolutePath()));
    }

    private void installMinecraftLibraries() {
        assert versionProfile != null;
        if (!tryCreateFolder(libsPath)) return;

        JsonArray libs = versionProfile.get("libraries").getAsJsonArray();
        for (int i = 0; i < libs.size(); i++) {
            JsonObject lib = libs.get(i).getAsJsonObject();
            String libName = lib.get("name").getAsString();
            progression(i / (double) libs.size(), libName);
            if (!checkRule(lib)) continue;

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
        if (checkIntegrity(libPath, libInfo.get("sha1").getAsString())) return;

        try {
            Files.createDirectories(libPath.getParentFile().toPath());
            HTTP.getFile(
                    libInfo.get("url").getAsString(),
                    new FileOutputStream(libPath),
                    p -> progression((i + p.getPercentage()) / total, MessageFormat.format("{0} - {1}", libName, p)));
            Main.logger.fine(() -> MessageFormat.format("Successfully downloaded {0} to {1}", libName, libPath.getAbsolutePath()));
        } catch (IOException e) {Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error while downloading {0}", libName));}
    }

    private void installMinecraftAssets() {
        assert versionProfile != null;

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
