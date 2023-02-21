package fr.minecraftbetter.launcher.utils.installer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.utils.http.HTTP;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public final class JavaManager {
    private JavaManager() {throw new IllegalStateException("Utility class");}

    public static final String JRE_API = "https://api.adoptium.net/v3/assets/latest/{0}/hotspot?architecture={1}&image_type=jre&os={2}";
    public static final String JAVA_EXECUTABLE = SystemUtils.IS_OS_WINDOWS ? "bin/java.exe" : "bin/java";

    public static String getJre(Path javaPath) {
        File[] files = javaPath.toFile().listFiles();
        if (files != null)
            for (File elem : files)
                if (elem.isDirectory())
                    return elem.toPath().resolve(JAVA_EXECUTABLE).toString();
        return "java";
    }

    public static void installJava(Path javaPath, String javaVersion, BiConsumer<Double, String> progression) {
        if (!Utils.tryCreateFolder(javaPath)) return;
        File jreZip = javaPath.resolve("jre.zip").toFile();
        Path jreMetadata = javaPath.resolve("jre.json");
        if (checkInstalledJava(javaPath, jreZip, jreMetadata)) return;

        JsonArray jreMatches = HTTP.getAsJSONArray(MessageFormat.format(JRE_API, javaVersion, Utils.getArch(), Utils.getOS()));
        if (jreMatches == null || jreMatches.size() <= 0) return;
        JsonObject jreInfo = jreMatches.get(0).getAsJsonObject();
        JsonObject jrePackage = jreInfo.get("binary").getAsJsonObject().get("package").getAsJsonObject();

        HTTP.downloadFile(jrePackage.get("link").getAsString(), jreZip, p -> progression.accept(p.getPercentage(), p.toString()));
        Utils.unzip(jreZip, javaPath);

        HTTP.downloadFile(jrePackage.get("metadata_link").getAsString(), jreMetadata.toFile(), null);
    }

    private static boolean checkInstalledJava(Path javaPath, File jreZip, Path jreMetadata) {
        if (Files.exists(jreMetadata)) {
            try {
                JsonObject meta = JsonParser.parseReader(new FileReader(jreMetadata.toFile())).getAsJsonObject();
                if (Utils.checkIntegrity(jreZip, meta.get("sha256").getAsString(), "SHA-256")) return true;
                else {
                    for (File elem : Objects.requireNonNull(javaPath.toFile().listFiles()))
                        if (elem.isDirectory()) Files.delete(elem.toPath());
                }
            } catch (FileNotFoundException e) {
                Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error while reading {0}", jreMetadata));
            } catch (IOException e) {
                Main.logger.log(Level.SEVERE, "Couldn't delete the old jre", e);
            }
        }
        return false;
    }
}
