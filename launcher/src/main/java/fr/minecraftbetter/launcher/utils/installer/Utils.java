package fr.minecraftbetter.launcher.utils.installer;

import fr.minecraftbetter.launcher.Main;
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
}
