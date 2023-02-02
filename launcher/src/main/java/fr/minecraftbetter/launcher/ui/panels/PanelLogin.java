package fr.minecraftbetter.launcher.ui.panels;

import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.api.launcher.LauncherInfo;
import fr.minecraftbetter.launcher.ui.PanelManager;
import fr.minecraftbetter.launcher.ui.panel.Panel;
import fr.minecraftbetter.launcher.ui.utils.PopupPanel;
import fr.minecraftbetter.launcher.ui.utils.UiUtils;
import fr.minecraftbetter.launcher.utils.Resources;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.minecraft_authenticator.login.User;
import org.jasypt.util.text.BasicTextEncryptor;
import org.kordamp.ikonli.fluentui.FluentUiFilledAL;
import org.kordamp.ikonli.fluentui.FluentUiFilledMZ;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.minecraftbetter.launcher.ui.utils.UiUtils.setupButton;


public class PanelLogin extends Panel {
    static final BasicTextEncryptor textEncryptor = new BasicTextEncryptor();

    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);
        Main.logger.info("AppData path: " + Main.AppData);

        try {
            MediaPlayer mediaPlayer = new MediaPlayer(Resources.getMedia("/minecraftbetter/images/intro.mp4"));
            MediaView mediaView = new MediaView(mediaPlayer);
            layout.getChildren().add(mediaView);
            mediaView.fitWidthProperty().bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
            mediaView.fitHeightProperty().bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
            mediaView.setPreserveRatio(true);
            mediaPlayer.play();
        } catch (MediaException e) {
            Main.logger.log(Level.WARNING, "Couldn''t load intro video", e);
        }

        Thread login = new Thread(() -> {
            textEncryptor.setPassword("uFw722H8$@2R");

            // Check if we saved auth data
            try {
                Path authDataPath = Main.AppData.resolve(Paths.get("token.txt"));
                if (Files.exists(authDataPath)) {
                    String encryptedAuthData = Files.readString(authDataPath);
                    String decryptedAuthData = textEncryptor.decrypt(encryptedAuthData);
                    authFileConnect(decryptedAuthData);
                    return;
                } else Main.logger.info("No auth data is saved");
            } catch (IOException e) {
                Main.logger.log(Level.WARNING, "Couldn''t read the auth data", e);
            }

            Platform.runLater(this::webConnect); // Open a login pop-up
        }); // Makes the requests asynchronously, so it doesn't freeze the app

        new Thread(() -> checkVersion(login)).start();
    }

    private void checkVersion(Thread continueFct) {
        LauncherInfo info = LauncherInfo.tryGet();
        Platform.runLater(() -> {
            if (info == null) {
                Button retry = setupButton(layout, "Ressayer", "#00C410", FluentUiFilledAL.ARROW_CLOCKWISE_24);
                Button ignore = setupButton(layout, "Ignorer", "#fd000f", FluentUiFilledAL.DISMISS_24);
                PopupPanel errorPopup = openPopup("Erreur de communication avec le serveur", "Le launcher pourrait ne pas fonctionner correctement", retry, ignore);
                retry.setOnMouseClicked(event -> {
                    errorPopup.dismiss();
                    new Thread(() -> checkVersion(continueFct)).start();
                });
                ignore.setOnMouseClicked(event -> {
                    errorPopup.dismiss();
                    continueFct.start();
                });
            } else if (!info.isUpToDate()) {
                Button download = setupButton(layout, "Télécharger", "#00C410", FluentUiFilledAL.ARROW_DOWNLOAD_24);
                Button ignore = setupButton(layout, "Ignorer", "#fd000f", FluentUiFilledAL.DISMISS_24);
                PopupPanel errorPopup = openPopup("Une version plus récente est disponible !",
                        "La version " + info.latest_version().version_number() + " est disponible. (Vous avez " + (Main.getBuildVersion() == null ? "unknown" : Main.getBuildVersion()) + ")"
                                + "\nTéléchargez-là dès maintenant pour profiter des dernières fonctionnalités et corrections", download, ignore);

                download.setOnMouseClicked(event -> UiUtils.openUrl(info.latest_version().url()));
                ignore.setOnMouseClicked(event -> {
                    errorPopup.dismiss();
                    continueFct.start();
                });
            } else continueFct.start();
        });
    }

    /**
     * Try to connect using a web pop-up
     */
    private void webConnect() {
        Main.logger.info("Logging in with a webview");

        WebView webView = new WebView();
        layout.getChildren().add(webView);
        webView.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
            Main.logger.finest("GET request to " + newValue);
            if (newValue.contains(Authenticator.microsoftLoginRedirect())) {
                final Matcher regex = Pattern.compile("([?&])code=(?<code>.+?)($|&)").matcher(newValue);

                layout.getChildren().remove(webView);
                if (!regex.find()) {
                    Main.logger.warning("The connection has been successful but we couldn't retrieve the token");
                    Button retry = setupButton(layout, "Ressayer", "#00C410", FluentUiFilledAL.ARROW_CLOCKWISE_24);
                    Button close = setupButton(layout, "Quitter", "#fd000f", FluentUiFilledAL.DISMISS_24);

                    PopupPanel errorPopup = openPopup("La connexion a réussi mais nous n'avons pu la traiter",
                            "Si le problème persiste merci de contacter un responsable", retry, close);

                    retry.setOnMouseClicked(event -> {
                        errorPopup.dismiss();
                        webConnect();
                    });
                    close.setOnMouseClicked(event -> System.exit(0));
                    return;
                }

                Main.logger.info("A token has been generated");
                connect(Authenticator.ofMicrosoft(regex.group("code")), true);
            }
            webView.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
            webView.getEngine().load(Authenticator.microsoftLogin().toString());
        });
    }

    /**
     * Try to connect using a token
     */
    private void authFileConnect(String authData) throws IOException {
        Main.logger.info("Logging in with local data");
        connect(Authenticator.of(AuthenticationFile.readString(authData)), false);

    }

    /**
     * Connect using the given method
     */
    private void connect(Authenticator.Builder builder, boolean interactive) {
        try {
            Authenticator authenticator = builder.shouldAuthenticate().build();
            authenticator.run(); // Run authentication
            connected(authenticator);
        } catch (Exception e) {
            Main.logger.log(Level.WARNING, "Connection using token failed", e);
            Platform.runLater(() -> {
                Button retry = setupButton(layout, "Ressayer", "#00C410", FluentUiFilledAL.ARROW_CLOCKWISE_24);
                Button manual = setupButton(layout, "Connexion manuelle", "#0065D8", FluentUiFilledMZ.PERSON_ARROW_RIGHT_24);
                Button close = setupButton(layout, "Quitter", "#fd000f", FluentUiFilledAL.DISMISS_24);

                PopupPanel errorPopup = interactive
                        ? openErrorPopup("La connexion guidée a échouée", e, retry, close)
                        : openErrorPopup("La connexion automatique a échouée", e, retry, manual, close);

                retry.setOnMouseClicked(event -> {
                    errorPopup.dismiss();
                    new Thread(() -> connect(builder, interactive)).start();
                });
                manual.setOnMouseClicked(event -> {
                    errorPopup.dismiss();
                    Platform.runLater(this::webConnect);
                });
                close.setOnMouseClicked(event -> System.exit(0));
            });
        }
    }

    private void connected(Authenticator result) {
        // Save the token
        try {
            String encryptedToken = textEncryptor.encrypt(result.getResultFile().writeString());
            if (!Files.exists(Main.AppData)) Files.createDirectory(Main.AppData);
            Path file = Files.writeString(Main.AppData.resolve(Paths.get("token.txt")), encryptedToken);
            Main.logger.info(() -> MessageFormat.format("Written encrypted token to {0}", file));
        } catch (IOException e) {
            Main.logger.log(Level.WARNING, "Couldn''t write the token", e);
        }

        var user = result.getUser();
        if (user.isEmpty()) {
            //Show popup
            return;
        }
        User account = user.get();


        Main.logger.info(() -> MessageFormat.format("Connected as {0}", account.name()));
        Platform.runLater(() -> panelManager.showPanel(new PanelHome(account)));
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
}
