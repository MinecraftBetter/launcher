package fr.minecraftbetter.launcher;

import fr.minecraftbetter.launcher.utils.logging.CustomLevels;
import fr.minecraftbetter.launcher.utils.logging.LogFormatter;
import fr.minecraftbetter.launcher.utils.logging.OutConsoleHandler;
import javafx.application.Application;
import net.harawata.appdirs.AppDirsFactory;

import javax.swing.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {

    public static final Logger logger = Logger.getLogger("fr.minecraftbetter.launcher");
    public static final Path AppData = Paths.get(AppDirsFactory.getInstance().getUserDataDir("MinecraftBetter", null, null, true));

    public static void main(String[] args) {
        logger.setUseParentHandlers(false);
        Formatter loggingFormat = new LogFormatter();
        OutConsoleHandler consoleHandler = new OutConsoleHandler(loggingFormat);
        try {consoleHandler.setEncoding("UTF-8");} catch (UnsupportedEncodingException e) {e.printStackTrace();}
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);

        try {
            Files.createDirectories(AppData);
            FileHandler fileHandler = new FileHandler(AppData.resolve("logs.txt").toString());
            fileHandler.setFormatter(loggingFormat);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setEncoding("UTF-8");
            logger.addHandler(fileHandler);
        } catch (IOException e) {e.printStackTrace();}


        // Logs info
        String logo = """
                 __       __ __                                               ______    __          _______             __       __                      \s
                |  \\     /  \\  \\                                             /      \\  |  \\        |       \\           |  \\     |  \\                     \s
                | ▓▓\\   /  ▓▓\\▓▓_______   ______   _______  ______   ______ |  ▓▓▓▓▓▓\\_| ▓▓_       | ▓▓▓▓▓▓▓\\ ______  _| ▓▓_   _| ▓▓_    ______   ______ \s
                | ▓▓▓\\ /  ▓▓▓  \\       \\ /      \\ /       \\/      \\ |      \\| ▓▓_  \\▓▓   ▓▓ \\      | ▓▓__/ ▓▓/      \\|   ▓▓ \\ |   ▓▓ \\  /      \\ /      \\\s
                | ▓▓▓▓\\  ▓▓▓▓ ▓▓ ▓▓▓▓▓▓▓\\  ▓▓▓▓▓▓\\  ▓▓▓▓▓▓▓  ▓▓▓▓▓▓\\ \\▓▓▓▓▓▓\\ ▓▓ \\    \\▓▓▓▓▓▓      | ▓▓    ▓▓  ▓▓▓▓▓▓\\\\▓▓▓▓▓▓  \\▓▓▓▓▓▓ |  ▓▓▓▓▓▓\\  ▓▓▓▓▓▓\\
                | ▓▓\\▓▓ ▓▓ ▓▓ ▓▓ ▓▓  | ▓▓ ▓▓    ▓▓ ▓▓     | ▓▓   \\▓▓/      ▓▓ ▓▓▓▓     | ▓▓ __     | ▓▓▓▓▓▓▓\\ ▓▓    ▓▓ | ▓▓ __  | ▓▓ __| ▓▓    ▓▓ ▓▓   \\▓▓
                | ▓▓ \\▓▓▓| ▓▓ ▓▓ ▓▓  | ▓▓ ▓▓▓▓▓▓▓▓ ▓▓_____| ▓▓     |  ▓▓▓▓▓▓▓ ▓▓       | ▓▓|  \\    | ▓▓__/ ▓▓ ▓▓▓▓▓▓▓▓ | ▓▓|  \\ | ▓▓|  \\ ▓▓▓▓▓▓▓▓ ▓▓     \s
                | ▓▓  \\▓ | ▓▓ ▓▓ ▓▓  | ▓▓\\▓▓     \\\\▓▓     \\ ▓▓      \\▓▓    ▓▓ ▓▓        \\▓▓  ▓▓    | ▓▓    ▓▓\\▓▓     \\  \\▓▓  ▓▓  \\▓▓  ▓▓\\▓▓     \\ ▓▓     \s
                 \\▓▓      \\▓▓\\▓▓\\▓▓   \\▓▓ \\▓▓▓▓▓▓▓ \\▓▓▓▓▓▓▓\\▓▓       \\▓▓▓▓▓▓▓\\▓▓         \\▓▓▓▓      \\▓▓▓▓▓▓▓  \\▓▓▓▓▓▓▓   \\▓▓▓▓    \\▓▓▓▓  \\▓▓▓▓▓▓▓\\▓▓     \s
                """;
        String versionText = "Version " + getBuildVersion();
        String copyrightText = "© " + Calendar.getInstance().get(Calendar.YEAR) + " Minecraft Better";
        logger.log(CustomLevels.NoFormatting, () -> logo
                + "\n" + " ".repeat((138 - versionText.length()) / 2) + versionText
                + "\n" + " ".repeat((138 - copyrightText.length()) / 2) + copyrightText
                + "\n\n");

        if (Arrays.asList(args).contains("--debug")) {
            logger.setLevel(Level.ALL);
            logger.config("---- Debug mode ----");
        }

        try {
            Class.forName("javafx.application.Application");
            Application.launch(FxApplication.class, args);
            logger.info("Application started");
        } catch (ClassNotFoundException e) {
            logger.warning("JavaFX not found");
            JOptionPane.showMessageDialog(null, "Une erreur avec java à été détectée.\n" + e.getMessage(), "Erreur Java", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static String getBuildVersion() {return Main.class.getPackage().getImplementationVersion();}
}
