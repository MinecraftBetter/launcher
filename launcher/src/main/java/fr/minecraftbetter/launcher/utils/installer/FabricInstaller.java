package fr.minecraftbetter.launcher.utils.installer;

import com.github.codeteapot.tools.artifact.Artifact;
import com.github.codeteapot.tools.artifact.ArtifactCoordinates;
import com.github.codeteapot.tools.artifact.ArtifactRepository;
import com.github.codeteapot.tools.artifact.ArtifactRepositoryException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.utils.http.ConcurrentDownloader;
import fr.minecraftbetter.launcher.utils.http.DownloadTask;
import fr.minecraftbetter.launcher.utils.http.HTTP;
import javafx.util.Pair;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Stream;

public class FabricInstaller implements Installer {

    public static final String FABRIC_VERSIONS_API = "https://meta.fabricmc.net/v2/versions/loader/{0}";
    public static final String FABRIC_PROFILE_API = "https://meta.fabricmc.net/v2/versions/loader/{0}/{1}/profile/json";
    public static final String MAVEN_CENTRAL_REPOSITORY = "https://repo.maven.apache.org/maven2/";

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
        Path profile = profilesPath.resolve(minecraftManager.installationProfile.wantedMinecraftVersion + "-fabric.json");

        try {
            Files.createDirectories(profilesPath);
            if (Files.exists(profile)) {
                versionProfile = JsonParser.parseReader(new FileReader(profile.toFile())).getAsJsonObject();
                return;
            }
        } catch (IOException e) {Main.logger.log(Level.WARNING, "Error while reading fabric profile", e);}

        JsonArray manifest = HTTP.getAsJSONArray(MessageFormat.format(FABRIC_VERSIONS_API, minecraftManager.installationProfile.wantedMinecraftVersion));
        assert manifest != null && !manifest.isEmpty();
        minecraftManager.progression(1 / 3d);


        Main.logger.fine(() -> MessageFormat.format("Processing {0} fabric releases", manifest.size()));
        for (JsonElement vE : manifest) {
            JsonObject versionMeta = vE.getAsJsonObject().get("loader").getAsJsonObject();
            if (!versionMeta.get("stable").getAsBoolean()) continue;

            Main.logger.fine(() -> MessageFormat.format("Found {0} fabric metadata", minecraftManager.installationProfile.wantedMinecraftVersion));
            Main.logger.finest(vE::toString);
            minecraftManager.progression(2 / 3d);

            String fabricVersion = versionMeta.get("version").getAsString();

            versionProfile = HTTP.getAsJSONObject(MessageFormat.format(FABRIC_PROFILE_API, minecraftManager.installationProfile.wantedMinecraftVersion, fabricVersion));
            Main.logger.fine(() -> MessageFormat.format("Got {0} fabric profile for Minecraft {1}", fabricVersion, minecraftManager.installationProfile.wantedMinecraftVersion));
            Main.logger.finest(versionProfile::toString);
            assert versionProfile != null;
            try {Files.write(profile, versionProfile.toString().getBytes());} catch (IOException e) {Main.logger.log(Level.WARNING, "Error while writing fabric profile", e);}
            return;
        }
    }

    private final HashMap<String, Pair<Integer, DownloadTask>> libs = new HashMap<>();

    public void installLibs() {
        assert versionProfile != null;

        JsonArray libsJson = versionProfile.get("libraries").getAsJsonArray();
        for (JsonElement libE : libsJson) {
            JsonObject lib = libE.getAsJsonObject();
            String[] libNameParts = lib.get("name").getAsString().split(":");
            ArtifactCoordinates coords = new ArtifactCoordinates(libNameParts[0], libNameParts[1], libNameParts[2]);
            downloadLib(coords, lib.get("url").getAsString(), libsPath, 0);
        }

        ConcurrentDownloader downloader = new ConcurrentDownloader();
        for (Pair<Integer, DownloadTask> lib : libs.values()) downloader.addTask(lib.getValue());
        downloader.onProgress(p -> minecraftManager.progression(p.getPercentage(), p.getStatus()));
        var thread = downloader.thread();
        thread.start();
        try {thread.join();} catch (InterruptedException e) {Thread.currentThread().interrupt();}
    }

    private boolean downloadLib(ArtifactCoordinates coords, String repoURL, Path installationPath, int depth) {
        var libName = coords.getGroupId() + "." + coords.getArtifactId();
        if (libs.containsKey(libName) && depth > libs.get(libName).getKey()) {
            Main.logger.fine(() -> MessageFormat.format("Circular reference for {0} (depth {1})", libName, depth));
            return false;
        }
        Main.logger.fine(() -> MessageFormat.format("Checking {0} (depth {1})", libName, depth));

        ArtifactRepository repo;
        try {
            repo = new ArtifactRepository(new URL(repoURL));
        } catch (MalformedURLException e) {
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error parsing url {0}", repoURL));
            return false;
        }
        Artifact artifact;
        try {
            artifact = repo.get(coords);
        } catch (ArtifactRepositoryException | IOException e) {
            if (!repoURL.equals(MAVEN_CENTRAL_REPOSITORY))
                return downloadLib(coords, MAVEN_CENTRAL_REPOSITORY, installationPath, depth); // Try to download with Maven Central Repository
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error getting artifact {0}:{1}", libName, coords.getVersion()));
            return false;
        }

        Path path = installationPath.resolve(coords.getGroupId().replace('.', '/')).resolve(coords.getArtifactId());
        Path fileUrl = Paths.get(artifact.getLocation().getFile().replace(".pom", ".jar"));
        Path libFile = path.resolve(coords.getVersion()).resolve(fileUrl.getFileName());
        try {Files.createDirectories(libFile.getParent());} catch (IOException e) {
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error creating directory {0}", libFile.getParent().toAbsolutePath()));
        }

        try (Stream<Path> libsInDir = Files.find(path, 4, (p, a) -> p.toFile().getName().endsWith(".jar"))) {
            Optional<Path> match = libsInDir.findAny();
            if (match.isPresent())
                Main.logger.fine(() -> MessageFormat.format("An existing lib for {1} has been found at {0}, skipping", match.get(), coords.getArtifactId()));
            else if (!Files.exists(libFile)) {
                Main.logger.fine(() -> MessageFormat.format("Adding {0} to the download list", libName));
                libs.put(libName, new Pair<>(depth, new DownloadTask(artifact.getLocation().toString().replace(".pom", ".jar"), libFile.toFile(), coords.getArtifactId())));
            }
        } catch (IOException e) {
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error while searching for {0} in local files", coords.getArtifactId()));
        }


        for (ArtifactCoordinates dep : artifact.getDependencies()) downloadLib(dep, repoURL, installationPath, depth + 1);
        return true;
    }

    public void installAssets() { /* Nothing to install */ }

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
