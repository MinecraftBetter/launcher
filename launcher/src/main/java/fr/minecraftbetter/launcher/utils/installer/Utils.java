package fr.minecraftbetter.launcher.utils.installer;

import fr.minecraftbetter.launcher.Main;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.lang3.SystemUtils;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.logging.Level;

public class Utils {
    private Utils() {throw new IllegalStateException("Utility class");}

    public static boolean tryCreateFolder(Path path) {
        if(Files.exists(path)) return true;
        try {Files.createDirectories(path);} catch (IOException e) {
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Couldn''t create/access folder at {0}", path.toAbsolutePath()));
            return false;
        }
        return true;
    }

    /**
     * Delete a file/directory using recursion
     */
    public static void delete(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var directory = Files.newDirectoryStream(path)) {
                for (Path file : directory) { //list all the files in directory
                    delete(file); //recursive delete
                }
            }
        }

        // We can delete it
        Files.delete(path);
        Main.logger.finest(() -> "Deleting " + path);
    }

    public static String calculateHash(File file, String hashMethod) {
        MessageDigest digest;
        try {digest = MessageDigest.getInstance(hashMethod);} catch (NoSuchAlgorithmException e) {
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

    public static boolean checkIntegrity(File file, String hash) {return checkIntegrity(file, hash, "SHA-1");}

    public static boolean checkIntegrity(File file, String hash, String method) {
        if (!file.exists()) return false;

        Main.logger.fine(() -> MessageFormat.format("Found existing installation at {0}", file.getAbsolutePath()));
        String fileSha = calculateHash(file, method);
        if (fileSha == null) return false;
        if (Objects.equals(fileSha, hash.toLowerCase())) {
            Main.logger.fine(() -> method + " matching, skipping");
            return true;
        }
        Main.logger.fine(() -> MessageFormat.format("{0} aren''t matching, found {1} expected {2}", method, fileSha, hash.toLowerCase()));
        return false;
    }

    public static String getOS() {
        if (SystemUtils.IS_OS_WINDOWS) return "windows";
        if (SystemUtils.IS_OS_MAC) return "mac";
        else if (SystemUtils.IS_OS_LINUX) return "linux";
        else if (SystemUtils.IS_OS_SOLARIS) return "solaris";
        else if (SystemUtils.IS_OS_AIX) return "aix";

        Main.logger.warning("Unknown os");
        return null;
    }

    public static String getArch() {
        if (SystemUtils.IS_OS_WINDOWS) {
            // Windows returns x86 if the program is running on 32 bits java
            String cpuArch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
            return cpuArch != null && cpuArch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") ? "x64" : "x86";
        }

        String arch = System.getProperty("os.arch");
        if (Objects.equals(arch, "amd64")) return "x64";
        if (Objects.equals(arch, "i386")) return "x86";
        return arch;
    }

    public static void unzip(File zip, Path dest) {
        try (ZipFile zipFile = new ZipFile(zip)) {
            zipFile.extractAll(dest.toString());
        } catch (IOException e) {
            Main.logger.log(Level.SEVERE, e, () -> MessageFormat.format("Error while extracting {0} to {1}", zip, dest));
        }
    }
}
