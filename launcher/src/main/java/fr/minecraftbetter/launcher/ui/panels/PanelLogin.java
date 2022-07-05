package fr.minecraftbetter.launcher.ui.panels;

import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.ui.PanelManager;
import fr.minecraftbetter.launcher.ui.panel.Panel;
import fr.minecraftbetter.launcher.ui.utils.PopupPanel;
import fr.minecraftbetter.launcher.utils.Resources;
import fr.minecraftbetter.launcher.utils.installer.MCBetterInstaller;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jasypt.util.text.BasicTextEncryptor;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fluentui.FluentUiFilledAL;
import org.kordamp.ikonli.fluentui.FluentUiFilledMZ;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
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
                    tokenConnect(decryptedToken);
                    return;
                } else Main.logger.info("No token is saved");
            } catch (IOException e) {
                Main.logger.log(Level.WARNING, "Couldn''t read the token", e);
            }

            webConnect(); // Open a login pop-up
        }); // Makes the requests asynchronously, so it doesn't freeze the app

        new Thread(() -> {
            if (!MCBetterInstaller.isUpToDate()) Platform.runLater(() -> {
                PopupPanel popup = new PopupPanel(layout);
                popup.setPrefSize(400, 300);
                popup.setOnExit(login::start);
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
    private void webConnect() {
        try {
            Main.logger.info("Logging in with a web pop-up");
            connected(authenticator.loginWithWebview());
        } catch (MicrosoftAuthenticationException e) {
            Main.logger.log(Level.WARNING, "Connection using a webview failed", e);
            Platform.runLater(() -> {
                Button retry = setupButton("Ressayer", "#00C410", FluentUiFilledAL.ARROW_CLOCKWISE_24);
                Button close = setupButton("Quitter", "#fd000f", FluentUiFilledAL.DISMISS_24);
                PopupPanel errorPopup = openErrorPopup("La connexion guidée a échouée", e, retry, close);

                retry.setOnMouseClicked(event -> {
                    errorPopup.dismiss();
                    new Thread(this::webConnect).start();
                });
                close.setOnMouseClicked(event -> System.exit(0));
            });
        }
    }

    /**
     * Try to connect using a token
     */
    private void tokenConnect(String token) {
        try {
            Main.logger.info("Logging in with a token");
            connected(authenticator.loginWithRefreshToken(token));
        } catch (MicrosoftAuthenticationException e) {
            Main.logger.log(Level.WARNING, "Connection using token failed", e);
            Platform.runLater(() -> {
                Button retry = setupButton("Ressayer", "#00C410", FluentUiFilledAL.ARROW_CLOCKWISE_24);
                Button manual = setupButton("Connexion manuelle", "#0065D8", FluentUiFilledMZ.TEXT_FIELD_24);
                Button close = setupButton("Quitter", "#fd000f", FluentUiFilledAL.DISMISS_24);
                PopupPanel errorPopup = openErrorPopup("La connexion automatique a échouée", e, retry, manual, close);

                retry.setOnMouseClicked(event -> {
                    errorPopup.dismiss();
                    new Thread(() -> tokenConnect(token)).start();
                });
                manual.setOnMouseClicked(event -> {
                    errorPopup.dismiss();
                    new Thread(this::webConnect).start();
                });
                close.setOnMouseClicked(event -> System.exit(0));
            });
        }
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


    private PopupPanel openErrorPopup(String title, Exception e, Button... buttons) {
        Throwable thrown = e;
        ArrayList<String> traces = new ArrayList<>();
        while (thrown != null) {
            traces.add(0, thrown.getLocalizedMessage());
            thrown = thrown.getCause();
        }
        return openPopup(title, String.join("\n → ", traces), buttons);
    }

    private PopupPanel openPopup(String title, String content, Button... buttons) {
        PopupPanel errorPopup = new PopupPanel(layout, title);
        errorPopup.setPrefSize(500, 400);
        errorPopup.setCloseOnOutsideClick(false);

        Label body = new Label(content);
        errorPopup.getChildren().add(body);
        body.setTextAlignment(TextAlignment.CENTER);
        body.setWrapText(true);

        HBox footer = new HBox(35);
        errorPopup.getChildren().add(footer);
        footer.setAlignment(Pos.BOTTOM_CENTER);
        footer.setPadding(new Insets(25, 25, 25, 25));
        footer.getChildren().addAll(buttons);
        return errorPopup;
    }

    private Button setupButton(String text, String color, Ikon icon) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(24);
        fontIcon.setFill(new Color(1, 1, 1, 1));
        Button btn = new Button(text, fontIcon);
        btn.setStyle("-fx-background-color:" + color + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-radius: 10; -fx-background-radius: 10;");
        return btn;
    }
}
