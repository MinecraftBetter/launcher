package fr.minecraftbetter.launcher;

import fr.minecraftbetter.launcher.utils.logging.LogFormatter;
import fr.minecraftbetter.launcher.utils.logging.OutConsoleHandler;
import javafx.application.Application;
import net.harawata.appdirs.AppDirsFactory;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;


public class Main {

    public static final Logger logger = Logger.getLogger("fr.minecraftbetter.launcher");
    public static final Path AppData = Paths.get(AppDirsFactory.getInstance().getUserDataDir("MinecraftBetter", null, null, true));

    public static void main(String[] args) {
        logger.setUseParentHandlers(false);
        Formatter loggingFormat = new LogFormatter();
        OutConsoleHandler consoleHandler = new OutConsoleHandler(loggingFormat);
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);

        try {
            FileHandler fileHandler = new FileHandler(AppData.resolve("logs.txt").toString());
            fileHandler.setFormatter(loggingFormat);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
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
}
