package fr.minecraftbetter.launcher.ui.panels;

import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.ui.PanelManager;
import fr.minecraftbetter.launcher.ui.panel.Panel;
import fr.minecraftbetter.launcher.utils.Resources;
import fr.minecraftbetter.launcher.utils.installer.MCBetterInstaller;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
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

        Thread login = new Thread(() -> {
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
                } else Main.logger.info("No token is saved");
            } catch (IOException e) {
                Main.logger.log(Level.WARNING, "Couldn''t read the token", e);
            }

            webConnect(); // Open a login pop-up
        }); // Makes the requests asynchronously, so it doesn't freeze the app

        new Thread(() -> {
            if (!MCBetterInstaller.isUpToDate()) Platform.runLater(() -> {
                StackPane popup = setupPopup(layout, login::start);
                VBox content = new VBox(15);
                popup.getChildren().add(content);
                content.setAlignment(Pos.CENTER);

                Label title = new Label("Une version plus récente est disponible !");
                Label desc = new Label("Téléchargez-là dès maintenant depuis le site minecraftbetter.com");
                content.getChildren().addAll(title, desc);
            });
            else login.start();
        }).start();
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
        } catch (MicrosoftAuthenticationException e) {
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
            Main.logger.info(() -> MessageFormat.format("Written encrypted token to {0}", file));
        } catch (IOException e) {
            Main.logger.log(Level.WARNING, "Couldn''t write the token", e);
        }

        MinecraftProfile account = result.getProfile();
        Main.logger.info(() -> MessageFormat.format("Connected as {0}", account.getName()));
        Platform.runLater(() -> panelManager.showPanel(new PanelHome(account, result.getAccessToken())));
    }

    private StackPane setupPopup(Pane parent, Runnable onExit) {
        AnchorPane anchorPane = new AnchorPane();
        parent.getChildren().add(anchorPane);

        Button exitPanel = new Button();
        anchorPane.getChildren().add(exitPanel);
        AnchorPane.setTopAnchor(exitPanel, 0d);
        AnchorPane.setBottomAnchor(exitPanel, 0d);
        AnchorPane.setLeftAnchor(exitPanel, 0d);
        AnchorPane.setRightAnchor(exitPanel, 0d);
        exitPanel.setOnMouseClicked(e -> {
            parent.getChildren().remove(anchorPane);
            Platform.runLater(onExit);
        });
        exitPanel.setBackground(new Background(new BackgroundFill(new Color(0, 0, 0, 0.4), null, null)));

        StackPane pagePanel = new StackPane();
        AnchorPane.setTopAnchor(pagePanel, 0d);
        AnchorPane.setBottomAnchor(pagePanel, 0d);
        AnchorPane.setLeftAnchor(pagePanel, 0d);
        AnchorPane.setRightAnchor(pagePanel, 0d);
        pagePanel.setPickOnBounds(false);
        anchorPane.getChildren().add(pagePanel);


        StackPane contentPanel = new StackPane();
        pagePanel.getChildren().add(contentPanel);
        contentPanel.setMinSize(400, 300);
        contentPanel.setMaxSize(400, 300);
        StackPane.setAlignment(contentPanel, Pos.CENTER);
        contentPanel.setTranslateX(0);
        contentPanel.setTranslateY(0);
        contentPanel.setStyle("-fx-background-color: #202021; -fx-border-radius: 10; -fx-background-radius: 10;");
        return contentPanel;
    }
}
