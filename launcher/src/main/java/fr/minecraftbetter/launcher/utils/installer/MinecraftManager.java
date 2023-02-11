package fr.minecraftbetter.launcher.utils.installer;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.utils.Settings;
import javafx.application.Platform;
import javafx.util.Pair;
import net.hycrafthd.minecraft_authenticator.login.User;
import org.apache.commons.text.StringSubstitutor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.stream.Stream;

public class MinecraftManager {
    private static final Path INSTALLATION_PATH = Main.AppData;

    private final User account;
    final Installation installationProfile;

    final Path javaPath;
    final Path minecraftPath;
    ArrayList<Pair<Runnable, String>> actions;

    final EnumMap<Loader, Installer> installers = new EnumMap<>(Loader.class);
    final MinecraftInstaller minecraftInstaller;
    final MCBetterInstaller mcBetterInstaller;

    public MinecraftManager(Installation installationProfile, User account) {
        this.account = account;
        this.installationProfile = installationProfile;

        javaPath = INSTALLATION_PATH.resolve("jre/").toAbsolutePath();
        minecraftPath = INSTALLATION_PATH.resolve("minecraft/").resolve(installationProfile.profileName).toAbsolutePath();

        minecraftInstaller = new MinecraftInstaller(this);
        mcBetterInstaller = new MCBetterInstaller(this);

        installers.put(Loader.MINECRAFT, minecraftInstaller);
        //installers.put(Loader.MINECRAFT_BETTER, mcBetterInstaller);
        if (installationProfile.modLoaders.contains(Loader.FABRIC)) installers.put(Loader.FABRIC, new FabricInstaller(this));

        actions = new ArrayList<>();
        actions.add(new Pair<>(minecraftInstaller::getProfile, "Initializing"));
        actions.add(new Pair<>(mcBetterInstaller::installMods, "Installing mods and config"));
        actions.add(new Pair<>(() -> JavaManager.installJava(javaPath, installationProfile.wantedJavaVersion, this::progression), "Installing Java " + installationProfile.wantedJavaVersion));
        actions.add(new Pair<>(minecraftInstaller::installMinecraft, "Installing Minecraft"));
        actions.add(new Pair<>(minecraftInstaller::installAssets, "Installing Minecraft assets"));
        actions.add(new Pair<>(minecraftInstaller::installLibs, "Installing Minecraft libraries"));

        if (installers.containsKey(Loader.FABRIC)) {
            actions.add(new Pair<>(installers.get(Loader.FABRIC)::getProfile, "Installing Fabric profile"));
            actions.add(new Pair<>(installers.get(Loader.FABRIC)::installLibs, "Installing Fabric"));
        }

        try {
            Files.writeString(minecraftInstaller.profilesPath.resolve("profile.json"), new GsonBuilder().setPrettyPrinting().create().toJson(installationProfile));
        } catch (IOException e) {
            Main.logger.log(Level.WARNING, "Couldn't save settings", e);
        }
    }

    private Consumer<Progress> progress;
    private Runnable complete = () -> {};
    private int status = 0;

    private String getStatusMessage() {return status < actions.size() ? actions.get(status).getValue() : null;}

    public void setProgress(Consumer<Progress> progress) {this.progress = progress;}

    public void setComplete(Runnable complete) {this.complete = complete;}

    public Path getMinecraftPath() {return minecraftPath;}


    void progression() {progression(0);}

    void progression(double percentage) {progression(percentage, null);}

    void progression(double percentage, String detail) {
        if (progress == null) return;
        Platform.runLater(() -> progress.accept(new Progress(
                percentage,
                status,
                actions.size(),
                detail == null ? getStatusMessage() : MessageFormat.format("{0} ({1})", getStatusMessage(), detail))));
    }


    public void startInstall() {
        try {Files.createDirectories(minecraftPath);} catch (IOException e) {
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Couldn''t create/access installation path {0}", minecraftPath));
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

    private boolean checked = false;

    public boolean verifyInstall() {
        if (!checked) {
            // TODO: Really check
            checked = true;
            return false;
        } else return true;
    }

    public MinecraftInstance startGame() {
        Main.logger.fine("Verifying Minecraft installation");
        if (!verifyInstall()) return new MinecraftInstance(MinecraftInstance.StartStatus.INCOMPLETE_INSTALL);

        Main.logger.fine("Building the Minecraft execution command");
        ProcessBuilder builder = new ProcessBuilder();


        Main.logger.finer("Building dependencies");
        StringBuilder libsToLoad = new StringBuilder();
        Utils.tryCreateFolder(minecraftInstaller.nativeLibsPath);
        try (Stream<Path> libs = Files.find(minecraftInstaller.libsPath, 25, (f, a) -> f.toFile().getName().endsWith(".jar"))) {
            for (Path lib : libs.toList()) {
                if (lib.toString().contains("natives")) {
                    try (JarInputStream jarInputStream = new JarInputStream(new FileInputStream(lib.toFile()))) {
                        JarEntry entry;
                        while ((entry = jarInputStream.getNextJarEntry()) != null) {
                            String name = entry.getName();
                            name = name.substring(name.lastIndexOf('\\') + 1);
                            name = name.substring(name.lastIndexOf('/') + 1);
                            if (name.endsWith(".dll")) {
                                Files.copy(jarInputStream, minecraftInstaller.nativeLibsPath.resolve(name), StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                        jarInputStream.closeEntry();
                    } catch (IOException e) {
                        Main.logger.log(Level.SEVERE, "Error reading native library " + lib, e);
                    }
                }
                libsToLoad.append(minecraftPath.relativize(lib)).append(";");
            }
        } catch (IOException e) {
            Main.logger.log(Level.SEVERE, "Error while reading libraries", e);
            return new MinecraftInstance(MinecraftInstance.StartStatus.ERROR);
        }
        libsToLoad.append(minecraftPath.relativize(minecraftInstaller.minecraftFile.toPath()));
        Main.logger.finest(libsToLoad::toString);

        Main.logger.finer("Building arguments");
        ArrayList<String> commands = new ArrayList<>();
        commands.add(JavaManager.getJre(javaPath));
        commands.add("-Xmx" + Settings.getSettings().Xmx);
        var installersInstance = installers.values().stream().toList();
        for (Installer installer : installersInstance) commands.addAll(compileArguments(installer.getJWMArguments(), libsToLoad.toString()));
        for (int i = installersInstance.size() - 1; i >= 0; i--) {
            var mainClass = installersInstance.get(i).getMainClass();
            if (mainClass != null) {
                commands.add(mainClass);
                break;
            }
        }
        for (Installer installer : installersInstance) commands.addAll(compileArguments(installer.getGameArguments(), libsToLoad.toString()));
        Main.logger.finest(() -> String.join(" ", commands));

        builder.directory(minecraftPath.toFile());
        builder.command(commands);

        Main.logger.info("Launching Minecraft");
        Main.logger.fine(() -> "Launching from directory: " + minecraftPath);
        builder.redirectOutput(Main.AppData.resolve("minecraftLogs.txt").toFile());
        builder.redirectErrorStream(true);
        try {
            return new MinecraftInstance(MinecraftInstance.StartStatus.STARTED, builder.start());
        } catch (IOException e) {
            Main.logger.log(Level.SEVERE, "Error starting Minecraft", e);
            return new MinecraftInstance(MinecraftInstance.StartStatus.ERROR);
        }
    }

    public List<String> compileArguments(JsonArray argsJson, String classpath) {
        Map<String, String> values = new HashMap<>();

        // Game
        values.put("auth_player_name", account.name());
        var installersInstance = installers.values().stream().toList();
        for (int i = installersInstance.size() - 1; i >= 0; i--) {
            var version_name = installersInstance.get(i).getID();
            if (version_name != null) {
                values.put("version_name", version_name);
                break;
            }
        }
        values.put("game_directory", minecraftPath.toString());
        values.put("assets_root", minecraftInstaller.assetsPath.toString());
        values.put("assets_index_name", minecraftInstaller.getAssetIndexID());
        values.put("auth_uuid", account.uuid());
        values.put("auth_access_token", account.accessToken());
        values.put("clientid", account.clientId());
        values.put("auth_xuid", account.xuid());
        values.put("user_type", account.type());
        values.put("user_properties", "{}");
        values.put("version_type", "java"); // TODO: Find what is expected here
        values.put("resolution_width", "1280");
        values.put("resolution_height", "720");

        //JWM
        values.put("natives_directory", minecraftInstaller.nativeLibsPath.toString());
        values.put("launcher_name", "MinecraftBetter");
        values.put("launcher_version", Main.getBuildVersion());
        values.put("classpath", classpath);


        List<String> args = new ArrayList<>();
        for (JsonElement arg : argsJson) {
            JsonArray argValues = new JsonArray();
            if (arg.isJsonPrimitive()) argValues.add(arg.getAsString());
            else if (arg.isJsonObject()) {
                JsonObject argData = arg.getAsJsonObject();
                if (rulesAreUnmatched(argData)) continue;
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

    public boolean rulesAreUnmatched(JsonObject node) {
        if (!node.has("rules")) return false;
        for (JsonElement ruleE : node.get("rules").getAsJsonArray()) {
            JsonObject rule = ruleE.getAsJsonObject();
            if (ruleIsMatched(rule)) return false;
        }
        return true;
    }

    private boolean ruleIsMatched(JsonObject rule) {
        Map<String, Boolean> features = new HashMap<>();
        features.put("is_demo_user", false); // Not a demo
        features.put("has_custom_resolution", true);

        boolean allow = Objects.equals(rule.get("action").getAsString(), "allow");

        if (rule.has("os")) {
            for (Map.Entry<String, JsonElement> osRule : rule.get("os").getAsJsonObject().entrySet()) {
                var property = System.getProperty("os." + osRule.getKey()).toLowerCase();
                var wantedProperty = osRule.getValue().getAsString().toLowerCase();
                if (!property.contains(wantedProperty)) return !allow;
            }
        } else if (rule.has("features")) {
            for (Map.Entry<String, JsonElement> featureRule : rule.get("features").getAsJsonObject().entrySet())
                if (features.containsKey(featureRule.getKey()) && Boolean.FALSE.equals(features.get(featureRule.getKey()))) return !allow;
        }
        return allow;
    }
}

