package fr.minecraftbetter.launcher.ui.panels;

import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.ui.PanelManager;
import fr.minecraftbetter.launcher.ui.panel.Panel;
import fr.minecraftbetter.launcher.utils.Resources;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.jasypt.util.text.BasicTextEncryptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.logging.Level;


public class PanelLogin extends Panel {

    static final MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
    static final BasicTextEncryptor textEncryptor = new BasicTextEncryptor();

    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);
        Main.logger.info("AppData path: " + Main.AppData);

        MediaPlayer mediaPlayer = new MediaPlayer(Resources.getMedia("/minecraftbetter/images/intro.mp4"));
        MediaView mediaView = new MediaView(mediaPlayer);
        layout.getChildren().add(mediaView);
        mediaView.fitWidthProperty().bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
        mediaView.fitHeightProperty().bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
        mediaView.setPreserveRatio(true);
        mediaPlayer.play();

        new Thread(() -> {
            textEncryptor.setPassword("uFw722H8$@2R");

            // Check if we saved a token
            try {
                Path tokenPath = Main.AppData.resolve(Paths.get("token.txt"));
                if (Files.exists(tokenPath)) {
                    String encryptedToken = Files.readString(tokenPath);
                    String decryptedToken = textEncryptor.decrypt(encryptedToken);
                    if (Boolean.TRUE.equals(tokenConnect(decryptedToken))) {
                        return;
                    }
                }
                else Main.logger.info("No token is saved");
            } catch (IOException e) {
                Main.logger.log(Level.WARNING, "Couldn''t read the token", e);
            }

            webConnect(); // Open a login pop-up
        }).start(); // Makes the requests asynchronously, so it doesn't freeze the app
    }

    /**
     * Try to connect using a web pop-up
     */
    private Boolean webConnect() {
        try {
            Main.logger.info("Logging in with a web pop-up");
            connected(authenticator.loginWithWebview());
            return true;
        } catch (MicrosoftAuthenticationException e) {
            Main.logger.log(Level.WARNING, "Connection using a webview failed", e); /* Error (user closed the popup, ...) TODO: Handle this */
        }
        return false;
    }

    /**
     * Try to connect using a token
     */
    private Boolean tokenConnect(String token) {
        try {
            Main.logger.info("Logging in with a token");
            connected(authenticator.loginWithRefreshToken(token));
            return true;
        }
        catch (MicrosoftAuthenticationException e) {
            Main.logger.log(Level.WARNING, "Connection using token failed", e); /* Unknown error TODO: Handle this */
        }
        return false;
    }

    private void connected(MicrosoftAuthResult result) {
        // Save the token
        try {
            String encryptedToken = textEncryptor.encrypt(result.getRefreshToken());
            if (!Files.exists(Main.AppData)) Files.createDirectory(Main.AppData);
            Path file = Files.writeString(Main.AppData.resolve(Paths.get("token.txt")), encryptedToken);
            Main.logger.info(() -> MessageFormat.format( "Written encrypted token to {0}", file));
        } catch (IOException e) {
            Main.logger.log(Level.WARNING, "Couldn''t write the token", e);
        }

        MinecraftProfile account = result.getProfile();
        Main.logger.info(() -> MessageFormat.format("Connected as {0}", account.getName()));
        Platform.runLater(() -> panelManager.showPanel(new PanelHome(account, result.getAccessToken())));
    }
}
