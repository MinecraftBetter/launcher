package fr.minecraftbetter.launcher.utils.installer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.utils.http.HTTP;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.stream.Stream;

public class FabricInstaller {

    public static final String FABRIC_VERSIONS_API = "https://meta.fabricmc.net/v2/versions/loader/{0}";
    public static final String FABRIC_PROFILE_API = "https://meta.fabricmc.net/v2/versions/loader/{0}/{1}/profile/json";

    private JsonObject versionProfile;
    private final MinecraftManager minecraftManager;

    final Path profilesPath;
    final Path libsPath;

    public FabricInstaller(MinecraftManager minecraftManager) {
        this.minecraftManager = minecraftManager;
        profilesPath = minecraftManager.minecraftInstaller.profilesPath;
        libsPath = minecraftManager.minecraftInstaller.libsPath;
    }

    public void getProfile() {
        Path profile = profilesPath.resolve(MinecraftManager.WANTED_MINECRAFT_VERSION + "-fabric.json");

        try {
            Files.createDirectories(profilesPath);
            if (Files.exists(profile)) {
                versionProfile = JsonParser.parseReader(new FileReader(profile.toFile())).getAsJsonObject();
                return;
            }
        } catch (IOException e) {Main.logger.log(Level.WARNING, "Error while reading fabric profile", e);}

        JsonArray manifest = HTTP.getAsJSONArray(MessageFormat.format(FABRIC_VERSIONS_API, MinecraftManager.WANTED_MINECRAFT_VERSION));
        assert manifest != null && !manifest.isEmpty();
        minecraftManager.progression(1 / 3d);


        Main.logger.fine(() -> MessageFormat.format("Processing {0} fabric releases", manifest.size()));
        for (JsonElement vE : manifest) {
            JsonObject versionMeta = vE.getAsJsonObject().get("loader").getAsJsonObject();
            if (!versionMeta.get("stable").getAsBoolean()) continue;

            Main.logger.fine(() -> MessageFormat.format("Found {0} fabric metadata", MinecraftManager.WANTED_MINECRAFT_VERSION));
            Main.logger.finest(vE::toString);
            minecraftManager.progression(2 / 3d);

            String fabricVersion = versionMeta.get("version").getAsString();

            versionProfile = HTTP.getAsJSONObject(MessageFormat.format(FABRIC_PROFILE_API, MinecraftManager.WANTED_MINECRAFT_VERSION, fabricVersion));
            Main.logger.fine(() -> MessageFormat.format("Got {0} fabric profile for Minecraft {1}", fabricVersion, MinecraftManager.WANTED_MINECRAFT_VERSION));
            Main.logger.finest(versionProfile::toString);
            assert versionProfile != null;
            try {Files.write(profile, versionProfile.toString().getBytes());} catch (IOException e) {Main.logger.log(Level.WARNING, "Error while writing fabric profile", e);}
            return;
        }
    }

    public void installLibs() {
        assert versionProfile != null;
        int i = -1;
        JsonArray libs = versionProfile.get("libraries").getAsJsonArray();
        for (JsonElement libE : libs) {
            i += 1;
            JsonObject lib = libE.getAsJsonObject();
            MavenResolvedArtifact[] libDep = Maven.configureResolver()
                    .withRemoteRepo("fabricMaven", lib.get("url").getAsString(), "default")
                    .withMavenCentralRepo(true)
                    .resolve(lib.get("name").getAsString()).withTransitivity().asResolvedArtifact();
            for (MavenResolvedArtifact dep : libDep) {
                MavenCoordinate depInfo = dep.getCoordinate();
                Main.logger.fine(() -> "Installing " + depInfo.toCanonicalForm());
                minecraftManager.progression(i / (double) libs.size(), depInfo.toCanonicalForm());
                Path libDir = libsPath
                        .resolve(depInfo.getGroupId().replace('.', '/'))
                        .resolve(depInfo.getArtifactId());

                Path from = dep.asFile().toPath();
                Path to = libDir.resolve(depInfo.getVersion()).resolve(dep.asFile().getName());
                try { Files.createDirectories(to.getParent()); } catch (IOException e) {
                    Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error creating directory {0}", to.toAbsolutePath()));
                }
                try (Stream<Path> libsInDir = Files.find(libDir, 4, (p, a) -> p.toFile().getName().endsWith(".jar"))){
                    if (libsInDir.count() > 0) {
                        Main.logger.fine(() -> MessageFormat.format("An existing lib for {1} has been found at {0}, skipping", libDir, depInfo.toCanonicalForm()));
                        continue;
                    }

                    if (!Files.exists(to)) Files.copy(from, to);
                } catch (IOException e) {
                    Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error copying {0} to {1}", from.toAbsolutePath(), to.toAbsolutePath()));
                }
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
        JsonArray args = new JsonArray();
        args.add("-DFabricMcEmu=net.minecraft.client.main.Main");
        // return versionProfile.get("arguments").getAsJsonObject().get("jvm").getAsJsonArray();
        return args;
    }

    public JsonArray getGameArguments() {
        assert versionProfile != null;
        return versionProfile.get("arguments").getAsJsonObject().get("game").getAsJsonArray();
    }
}
