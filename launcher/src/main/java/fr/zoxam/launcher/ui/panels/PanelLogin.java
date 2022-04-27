package fr.zoxam.launcher.ui.panels;

import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import fr.zoxam.launcher.ui.PanelManager;
import fr.zoxam.launcher.ui.panel.Panel;
import fr.zoxam.launcher.utils.Resources;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import net.harawata.appdirs.AppDirsFactory;
import org.jasypt.util.text.BasicTextEncryptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class PanelLogin extends Panel {

    public MicrosoftAuthenticator authenticator;
    public BasicTextEncryptor textEncryptor;
    public static final Path AppData = Paths.get(AppDirsFactory.getInstance().getUserDataDir("MinecraftBetter", null, null, true));

    public PanelManager panelManager;

    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);
        this.panelManager = panelManager;
        System.out.printf("AppData path: %1$s%n", AppData);

        MediaPlayer mediaPlayer = new MediaPlayer(Resources.getMedia("/minecraftbetter/images/intro.mp4"));
        MediaView mediaView = new MediaView(mediaPlayer);
        layout.getChildren().add(mediaView);
        mediaView.fitWidthProperty().bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
        mediaView.fitHeightProperty().bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
        mediaView.setPreserveRatio(true);
        mediaPlayer.play();

        new Thread(() -> {
            authenticator = new MicrosoftAuthenticator();
            textEncryptor = new BasicTextEncryptor();
            textEncryptor.setPassword("uFw722H8$@2R");

            // Check if we saved a token
            try {
                Path tokenPath = AppData.resolve(Paths.get("token.txt"));
                if (Files.exists(tokenPath)) {
                    String encryptedToken = new String(Files.readAllBytes(tokenPath), StandardCharsets.UTF_8);
                    String decryptedToken = textEncryptor.decrypt(encryptedToken);
                    if (tokenConnect(decryptedToken)) {
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            webConnect(); // Open a login pop-up
        }).start(); // Makes the requests asynchronously, so it doesn't freeze the app
    }

    /**
     * Try to connect using a web pop-up
     */
    private Boolean webConnect() {
        try {
            connected(authenticator.loginWithWebview());
            return true;
        } catch (MicrosoftAuthenticationException e) {
            e.printStackTrace(); /* Error (user closed the popup, ...) TODO: Handle this */
        }
        return false;
    }

    /**
     * Try to connect using a token
     */
    private Boolean tokenConnect(String token) {
        try {
            connected(authenticator.loginWithRefreshToken(token));
            return true;
        } catch (MicrosoftAuthenticationException e) {
            e.printStackTrace(); /* Unknown error TODO: Handle this */
        }
        return false;
    }

    private void connected(MicrosoftAuthResult result) {
        // Save the token
        try {
            String encryptedToken = textEncryptor.encrypt(result.getRefreshToken());
            if (!Files.exists(AppData)) Files.createDirectory(AppData);
            Path file = Files.write(AppData.resolve(Paths.get("token.txt")), encryptedToken.getBytes(StandardCharsets.UTF_8));
            System.out.printf("Written encrypted token to %1$s%n", file);
        } catch (IOException e) {
            e.printStackTrace(); // Couldn't write
        }

        MinecraftProfile account = result.getProfile();
        System.out.printf("Connected as %1$s%n", account.getName());
        Platform.runLater(() -> panelManager.showPanel(new PanelHome(account)));
    }
}
