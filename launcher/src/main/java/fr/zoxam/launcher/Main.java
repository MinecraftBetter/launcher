package fr.zoxam.launcher;


import fr.arinonia.arilibfx.utils.AriLogger;
import javafx.application.Application;

import javax.swing.*;


public class Main {

    public static AriLogger logger;

    public static void main(String[] args) {

        logger = new AriLogger("Minecraftbetter");
        try {
            Class.forName("javafx.application.Application");
            Application.launch(FxApplication.class, args);
        }catch (ClassNotFoundException e){
            logger.warn("JavaFX not found ");
            JOptionPane.showMessageDialog(null,"Une erreur avec java à été détectée.\n" + e.getMessage(), "Erreur Jave", JOptionPane.ERROR_MESSAGE);
        }
    }
}
