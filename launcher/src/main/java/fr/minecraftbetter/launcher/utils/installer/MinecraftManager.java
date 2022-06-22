package fr.minecraftbetter.launcher.utils.installer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import fr.minecraftbetter.launcher.Main;
import javafx.application.Platform;
import javafx.util.Pair;
import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;

public class MinecraftManager {
    public static final String WANTED_MINECRAFT_VERSION = "1.18.2";
    public static final String WANTED_JAVA_VERSION = "18";

    private final MinecraftProfile account;
    private final String accessToken;

    final Path javaPath;
    final Path minecraftPath;
    ArrayList<Pair<Runnable, String>> actions;

    final MinecraftInstaller minecraftInstaller;
    final FabricInstaller fabricInstaller;
    final MCBetterInstaller mcBetterInstaller;

    public MinecraftManager(Path installationPath, MinecraftProfile account, String accessToken) {
        this.account = account;
        this.accessToken = accessToken;

        javaPath = installationPath.resolve("jre/").toAbsolutePath();
        minecraftPath = installationPath.resolve("minecraft/").toAbsolutePath();

        minecraftInstaller = new MinecraftInstaller(this, minecraftPath);
        fabricInstaller = new FabricInstaller(this);
        mcBetterInstaller = new MCBetterInstaller(this);

        actions = new ArrayList<>();
        actions.add(new Pair<>(minecraftInstaller::getProfile, "Initializing"));
        actions.add(new Pair<>(() -> JavaManager.installJava(javaPath, WANTED_JAVA_VERSION, this::progression), "Installing Java " + WANTED_JAVA_VERSION));
        actions.add(new Pair<>(minecraftInstaller::installMinecraft, "Installing Minecraft"));
        actions.add(new Pair<>(minecraftInstaller::installLibs, "Installing Minecraft libraries"));
        actions.add(new Pair<>(minecraftInstaller::installAssets, "Installing Minecraft assets"));
        actions.add(new Pair<>(fabricInstaller::getProfile, "Installing Fabric profile"));
        actions.add(new Pair<>(fabricInstaller::installLibs, "Installing Fabric"));
        actions.add(new Pair<>(mcBetterInstaller::installMods, "Installing mods and config"));
    }

    private Consumer<Progress> progress;
    private Runnable complete = () -> {};
    private int status = 0;

    private String getStatusMessage() {return status < actions.size() ? actions.get(status).getValue() : null;}

    public void setProgress(Consumer<Progress> progress) {this.progress = progress;}

    public void setComplete(Runnable complete) {this.complete = complete;}

    public Path getMinecraftPath(){ return minecraftPath;}


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

    private static boolean checked = false;

    public static boolean verifyInstall() {
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
        try (Stream<Path> libs = Files.find(minecraftInstaller.libsPath, 25, (f, a) -> f.toFile().getName().endsWith(".jar"))) {
            for (Path lib : libs.toList())
                libsToLoad.append(minecraftPath.relativize(lib)).append(";");
        } catch (IOException e) {
            Main.logger.log(Level.SEVERE, "Error while reading libraries", e);
            return new MinecraftInstance(MinecraftInstance.StartStatus.ERROR);
        }
        libsToLoad.append(minecraftPath.relativize(minecraftInstaller.minecraftFile.toPath()));
        Main.logger.finest(libsToLoad::toString);

        Main.logger.finer("Building arguments");
        ArrayList<String> commands = new ArrayList<>();
        commands.add(JavaManager.getJre(javaPath));
        commands.addAll(compileArguments(minecraftInstaller.getJWMArguments(), libsToLoad.toString()));
        commands.addAll(compileArguments(fabricInstaller.getJWMArguments(), libsToLoad.toString()));
        commands.add(fabricInstaller.getMainClass());
        commands.addAll(compileArguments(minecraftInstaller.getGameArguments(), libsToLoad.toString()));
        commands.addAll(compileArguments(fabricInstaller.getGameArguments(), libsToLoad.toString()));
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
        values.put("auth_player_name", account.getName());
        values.put("version_name", fabricInstaller.getID());
        values.put("game_directory", minecraftPath.toString());
        values.put("assets_root", minecraftInstaller.assetsPath.toString());
        values.put("assets_index_name", minecraftInstaller.getAssetIndexID());
        values.put("auth_uuid", account.getId());
        values.put("auth_access_token", accessToken);
        values.put("clientid", "0"); //TODO
        values.put("auth_xuid", "0"); //TODO
        values.put("user_type", "microsoft"); //TODO
        values.put("version_type", "java"); //TODO
        values.put("resolution_width", "1280");
        values.put("resolution_height", "720");
        //JWM
        values.put("natives_directory", minecraftInstaller.libsPath.toString());
        values.put("launcher_name", "MinecraftBetter");
        values.put("launcher_version", "1.0");
        values.put("classpath", classpath);


        List<String> args = new ArrayList<>();
        for (JsonElement arg : argsJson) {
            JsonArray argValues = new JsonArray();
            if (arg.isJsonPrimitive()) argValues.add(arg.getAsString());
            else if (arg.isJsonObject()) {
                JsonObject argData = arg.getAsJsonObject();
                if (!checkRules(argData)) continue;
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

    public boolean checkRules(JsonObject node) {
        if (!node.has("rules")) return true;
        for (JsonElement ruleE : node.get("rules").getAsJsonArray()) {
            JsonObject rule = ruleE.getAsJsonObject();
            if (ruleIsUnmatched(rule)) return false;
        }
        return true;
    }

    private boolean ruleIsUnmatched(JsonObject rule) {
        Map<String, Boolean> features = new HashMap<>();
        features.put("is_demo_user", false); // Not a demo
        features.put("has_custom_resolution", true);

        boolean allow = Objects.equals(rule.get("action").getAsString(), "allow");

        if (rule.has("os")) {
            for (Map.Entry<String, JsonElement> osRule : rule.get("os").getAsJsonObject().entrySet())
                if (System.getProperty("os." + osRule.getKey()).matches(osRule.getValue().getAsString())) return !allow;
                else if (allow) return true;
        } else if (rule.has("features")) {
            for (Map.Entry<String, JsonElement> featureRule : rule.get("features").getAsJsonObject().entrySet())
                if (features.containsKey(featureRule.getKey()) && Boolean.TRUE.equals(features.get(featureRule.getKey()))) return !allow;
                else if (allow) return true;
        } else return !allow;
        return false;
    }
}

