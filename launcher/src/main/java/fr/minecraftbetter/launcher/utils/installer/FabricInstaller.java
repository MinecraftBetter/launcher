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
import fr.minecraftbetter.launcher.utils.http.DownloadProgress;
import fr.minecraftbetter.launcher.utils.http.HTTP;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;

public class FabricInstaller {

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
            String[] libNameParts = lib.get("name").getAsString().split(":");
            ArtifactCoordinates coords = new ArtifactCoordinates(libNameParts[0], libNameParts[1], libNameParts[2]);
            int finalI = i;
            downloadLib(coords, lib.get("url").getAsString(), libsPath,
                    progress -> minecraftManager.progression(finalI / (double) libs.size(), coords.getArtifactId() + " - " + progress));
        }
    }

    private boolean downloadLib(ArtifactCoordinates coords, String repoURL, Path installationPath, Consumer<DownloadProgress> progress) {
        Main.logger.fine(() -> MessageFormat.format("Installing {0}", coords.getArtifactId()));
        progress.accept(new DownloadProgress());

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
                return downloadLib(coords, MAVEN_CENTRAL_REPOSITORY, installationPath, progress); // Try to download with Maven Central Repository
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error getting artifact {0}", coords.getArtifactId()));
            return false;
        }

        Path path = installationPath.resolve(coords.getGroupId().replace('.', '/')).resolve(coords.getArtifactId());
        Path fileUrl = Paths.get(artifact.getLocation().getFile());
        Path libFile = path.resolve(coords.getVersion()).resolve(fileUrl.getFileName());
        try {Files.createDirectories(libFile.getParent());} catch (IOException e) {
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error creating directory {0}", libFile.getParent().toAbsolutePath()));
        }

        try (Stream<Path> libsInDir = Files.find(path, 4, (p, a) -> p.toFile().getName().endsWith(".jar"))) {
            Optional<Path> match = libsInDir.findAny();
            if (match.isPresent())
                Main.logger.fine(() -> MessageFormat.format("An existing lib for {1} has been found at {0}, skipping", match.get(), coords.getArtifactId()));
            else if (!Files.exists(libFile))
                HTTP.downloadFile(artifact.getLocation().toString(), libFile.toFile(), progress);
        } catch (IOException e) {
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error while search for {0} in local files", coords.getArtifactId()));
        }


        for (ArtifactCoordinates dep : artifact.getDependencies()) downloadLib(dep, repoURL, installationPath, progress);
        return true;
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
