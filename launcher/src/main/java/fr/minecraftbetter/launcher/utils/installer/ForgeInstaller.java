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

public class ForgeInstaller implements Installer {

    public static final String FORGE_VERSIONS_API = "https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml";
    public static final String FORGE_PROFILE_API = "http://api.minecraftbetter.com/storage?path=profiles/{0}.json";
    public static final String MAVEN_CENTRAL_REPOSITORY = "https://repo.maven.apache.org/maven2/";
    public static final String MIRROR_REPOSITORY = "https://maven.creeperhost.net";
    public static final String MINECRAFT_REPOSITORY = "https://libraries.minecraft.net";

    private JsonObject versionProfile;
    private final MinecraftManager minecraftManager;

    final Path profilesPath;
    final Path libsPath;

    public ForgeInstaller(MinecraftManager minecraftManager) {
        this.minecraftManager = minecraftManager;
        profilesPath = minecraftManager.minecraftInstaller.profilesPath;
        libsPath = minecraftManager.minecraftInstaller.libsPath;
    }

    public void getProfile() {
        Path profile = profilesPath.resolve(minecraftManager.installationProfile.wantedMinecraftVersion + "-forge.json");

        try {
            Files.createDirectories(profilesPath);
            if (Files.exists(profile)) {
                versionProfile = JsonParser.parseReader(new FileReader(profile.toFile())).getAsJsonObject();
                return;
            }
        } catch (IOException e) {Main.logger.log(Level.WARNING, "Error while reading forge profile", e);}

        String forgeVersion = "1.8.9-forge1.8.9-11.15.1.2318-1.8.9";

        versionProfile = HTTP.getAsJSONObject(MessageFormat.format(FORGE_PROFILE_API, forgeVersion));
        Main.logger.fine(() -> MessageFormat.format("Got {0} forge profile for Minecraft {1}", forgeVersion, minecraftManager.installationProfile.wantedMinecraftVersion));
        Main.logger.finest(versionProfile::toString);
        assert versionProfile != null;
        try {Files.write(profile, versionProfile.toString().getBytes());} catch (IOException e) {Main.logger.log(Level.WARNING, "Error while writing forge profile", e);}
    }

    private final HashMap<String, Pair<Integer, DownloadTask>> libs = new HashMap<>();

    public void installLibs() {
        assert versionProfile != null;

        JsonArray libsJson = versionProfile.get("libraries").getAsJsonArray();
        for (JsonElement libE : libsJson) {
            JsonObject lib = libE.getAsJsonObject();
            String[] libNameParts = lib.get("name").getAsString().split(":");
            ArtifactCoordinates coords = new ArtifactCoordinates(libNameParts[0], libNameParts[1], libNameParts[2]);
            if (lib.has("serverreq") && !lib.has("clientreq")) continue;
            if (lib.has("clientreq") && !lib.get("clientreq").getAsBoolean()) continue;
            downloadLib(coords, lib.has("url") ? lib.get("url").getAsString() : MINECRAFT_REPOSITORY, libsPath, 0);
        }

        downloadLib(new ArtifactCoordinates("net.minecraft", "launchwrapper", "1.12"), MINECRAFT_REPOSITORY, libsPath, 0);
        downloadLib(new ArtifactCoordinates("org.ow2.asm", "asm-all", "5.0.3"), MINECRAFT_REPOSITORY, libsPath, 0);
        downloadLib(new ArtifactCoordinates("lzma", "lzma", "0.0.1"), MINECRAFT_REPOSITORY, libsPath, 0);

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
        return new JsonArray();
    }

    public JsonArray getGameArguments() {
        assert versionProfile != null;
        JsonArray args = new JsonArray();
        if(versionProfile.has("minecraftArguments")) {
            var array = new JsonArray();
            for (String arg: versionProfile.get("minecraftArguments").getAsString().split(" ")) array.add(arg);
            return array;
        }
        return args;
    }
}
