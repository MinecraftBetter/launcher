package fr.minecraftbetter.launcher.ui.panels;

import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.ui.PanelManager;
import fr.minecraftbetter.launcher.ui.panel.Panel;
import fr.minecraftbetter.launcher.utils.news.News;
import fr.minecraftbetter.launcher.utils.Resources;
import fr.minecraftbetter.launcher.utils.installer.MinecraftInstance;
import fr.minecraftbetter.launcher.utils.installer.MinecraftManager;
import fr.minecraftbetter.launcher.utils.news.NewsRepr;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fluentui.FluentUiFilledAL;
import org.kordamp.ikonli.fluentui.FluentUiFilledMZ;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class PanelHome extends Panel {
    public static final String NEWS_API = "https://api.minecraftbetter.com/minecraftbetter/launcher/news";

    MinecraftProfile account;
    String accessToken;

    public PanelHome(MinecraftProfile account, String accessToken) {
        this.account = account;
        this.accessToken = accessToken;
    }

    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);
        this.panelManager = panelManager;
        panelManager.setBackground("/minecraftbetter/images/background.png");

        StackPane panel = new StackPane();
        GridPane.setHgrow(panel, Priority.ALWAYS);
        GridPane.setVgrow(panel, Priority.ALWAYS);
        panel.setBackground(new Background(new BackgroundFill(new Color(0.08, 0.08, 0.08, 0.33), null, null))); // Darken the background
        layout.getChildren().add(panel);

        ImageView logo = Resources.getImageView("/minecraftbetter/images/home/banner.png");
        StackPane.setAlignment(logo, Pos.TOP_CENTER);
        logo.setTranslateY(50);
        logo.setFitHeight(100);
        logo.setPreserveRatio(true);
        panel.getChildren().add(logo);

        //region Left side
        // News
        StackPane news = setupPanel(600, 400, -175, 125, "Nouvelles", panel);
        StackPane newsContent = panelContent(news);
        ListView<News> list = new ListView<>();
        ObservableList<News> data = FXCollections.observableArrayList(News.getNews(NEWS_API));
        list.setItems(data);
        list.setCellFactory(view -> new NewsRepr());
        newsContent.getChildren().add(list);

        //endregion

        //region Right side
        double rightWidth = 300;
        double rightX = -news.getTranslateX() + (news.getMinWidth() - rightWidth) / 2;
        // Social
        StackPane social = setupPanel(rightWidth, 100, rightX, news.getTranslateY() - (news.getMinHeight() - 100) / 2, "Suivez-nous", panel);
        StackPane socialContent = panelContent(social);
        int btnSpacing = 300 / 4; // Panel size / number of btn
        Button website = setupSocialBtn("/minecraftbetter/images/home/website.png", 0);
        website.setOnMouseClicked(event -> openUrl("https://minecraftbetter.com"));
        socialContent.getChildren().add(website);
        Button discord = setupSocialBtn("/minecraftbetter/images/home/discord.png", btnSpacing);
        discord.setOnMouseClicked(event -> openUrl("https://discord.com/invite/4TC5eNEkE5"));
        socialContent.getChildren().add(discord);
        Button twitter = setupSocialBtn("/minecraftbetter/images/home/twitter.png", btnSpacing * 2d);
        twitter.setOnMouseClicked(event -> openUrl("https://twitter.com/MinecraftBeuteu"));
        socialContent.getChildren().add(twitter);
        Button youtube = setupSocialBtn("/minecraftbetter/images/home/youtube.png", btnSpacing * 3d);
        youtube.setOnMouseClicked(event -> openUrl("https://www.youtube.com/channel/UCBIuyqGUDez-ksy7DPupJ6w"));
        socialContent.getChildren().add(youtube);

        // Server information
        StackPane server = setupPanel(rightWidth, 250, rightX, news.getTranslateY() + (news.getMinHeight() - 250) / 2, "Serveur", panel);
        Label username = new Label("Connecté en tant que " + account.getName());
        username.setStyle("-fx-text-fill: white;");
        server.getChildren().add(username);
        //endregion

        // Settings
        FontIcon settingsIcon = new FontIcon(FluentUiFilledMZ.SETTINGS_28);
        settingsIcon.setIconSize(35);
        settingsIcon.setIconColor(new Color(1, 1, 1, 1));
        Button settingsBtn = new Button("", settingsIcon);
        StackPane.setAlignment(settingsBtn, Pos.TOP_LEFT);
        panel.getChildren().add(settingsBtn);
        settingsBtn.setBackground(new Background(new BackgroundFill(new Color(0, 0, 0, 0), null, null)));
        settingsBtn.setTranslateX(20);
        settingsBtn.setTranslateY(20);
        settingsBtn.setMinSize(35, 35);
        settingsBtn.setMaxSize(35, 35);
        settingsBtn.setOnMouseEntered(e -> this.layout.setCursor(Cursor.HAND));
        settingsBtn.setOnMouseExited(event -> this.layout.setCursor(Cursor.DEFAULT));
        settingsBtn.setOnMouseClicked(event -> settingPopup(layout));

        // Install/Launch Btn
        MinecraftManager minecraftManager = new MinecraftManager(Main.AppData, account, accessToken);
        boolean installed = Files.exists(minecraftManager.getMinecraftPath());
        FontIcon viewPlayImage = new FontIcon(installed ? FluentUiFilledAL.DOCUMENT_AUTOSAVE_24 : FluentUiFilledAL.ARROW_DOWNLOAD_24);
        viewPlayImage.setIconSize(24);
        viewPlayImage.setFill(new Color(1, 1, 1, 1));
        Button play = new Button(installed ? "VERIFIER" : "INSTALLER", viewPlayImage);
        news.getChildren().add(play);
        StackPane.setAlignment(play, Pos.TOP_LEFT);
        play.setPrefSize(200, 50);
        play.setTranslateX(0);
        play.setTranslateY(-play.getPrefHeight() - 15);
        play.setStyle("-fx-background-color:#fd000f; -fx-text-fill: #FFFF; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-radius: 45; -fx-background-radius: 45;");
        play.setOnMouseEntered(e -> this.layout.setCursor(Cursor.HAND));
        play.setOnMouseExited(event -> this.layout.setCursor(Cursor.DEFAULT));
        play.setOnMouseClicked(event -> {
            if (installed) {
                play.setDisable(true);
                Main.logger.fine("The launch of Minecraft has been requested");
                MinecraftInstance instance = minecraftManager.startGame();
                if (instance.getStatus() == MinecraftInstance.StartStatus.ERROR) {
                    Main.logger.severe("Couldn't start Minecraft");
                    play.setDisable(false);
                }
                else if (instance.getStatus() == MinecraftInstance.StartStatus.STARTED){
                    Main.logger.fine("Minecraft has been started");
                    viewPlayImage.setIconCode(FluentUiFilledAL.CONTENT_SETTINGS_24);
                    play.setText("LANCÉ");
                    instance.onExit((error, process) -> {
                        if (Boolean.TRUE.equals(error)){
                            Main.logger.severe("An error has occurred during Minecraft execution " + process.errorReader().lines().collect(Collectors.joining("\n")));
                        }
                        else Main.logger.fine("Minecraft has been exited");
                        play.setDisable(false);
                        viewPlayImage.setIconCode(FluentUiFilledMZ.PLAY_24);
                        play.setText("JOUER");
                    });
                }
                if (instance.getStatus() != MinecraftInstance.StartStatus.INCOMPLETE_INSTALL) return;
                Main.logger.warning("The current installation of Minecraft is incomplete");
            }

            play.setDisable(true);
            viewPlayImage.setIconCode(FluentUiFilledAL.ARROW_DOWNLOAD_24);
            play.setText("INSTALLATION");

            ProgressBar installationProgress = new ProgressBar();
            installationProgress.setStyle("-fx-accent: red;");
            double progressLeftX = news.getTranslateX() - news.getWidth() / 2 + play.getTranslateX() + play.getWidth() + 15;
            double progressRightX = social.getTranslateX() + social.getWidth() / 2;
            installationProgress.setPrefSize(Math.abs(progressLeftX) + Math.abs(progressRightX), 25);
            installationProgress.setTranslateX((progressLeftX + progressRightX) / 2);
            installationProgress.setTranslateY(play.getTranslateY() + play.getHeight() / 2 + news.getTranslateY() - news.getHeight() / 2);
            Label progressText = new Label();
            progressText.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            progressText.textProperty().bind(installationProgress.progressProperty().multiply(100).asString("%02.0f%%"));
            DoubleBinding progressTextPos = installationProgress.progressProperty().multiply(progressRightX - progressLeftX).add(progressLeftX);
            progressText.translateXProperty().bind(Bindings.max(progressTextPos.subtract(30), progressLeftX + 30));
            progressText.translateYProperty().bind(installationProgress.translateYProperty());
            panel.getChildren().addAll(installationProgress, progressText);

            Label installationStatus = new Label();
            installationStatus.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            StackPane.setAlignment(installationStatus, Pos.BOTTOM_LEFT);
            installationStatus.setTranslateX(5);
            installationStatus.setTranslateY(-5);
            panel.getChildren().add(installationStatus);

            minecraftManager.setProgress(p -> {
                installationProgress.setProgress(p.getPercentage());
                installationStatus.setText(p.getStatus());
            });
            minecraftManager.setComplete(() -> {
                panel.getChildren().removeAll(installationProgress, progressText, installationStatus);

                viewPlayImage.setIconCode(FluentUiFilledMZ.PLAY_24);
                play.setText("JOUER");
                play.setDisable(false);
            });
            minecraftManager.startInstall();

        });
    }

    //region Home Panels
    private StackPane setupPanel(double w, double h, double x, double y, String text, Pane parent) {
        StackPane panel = new StackPane();
        parent.getChildren().add(panel);
        panel.setMinSize(w, h);
        panel.setMaxSize(w, h);
        StackPane.setAlignment(panel, Pos.CENTER);
        panel.setTranslateX(x);
        panel.setTranslateY(y);
        panel.setStyle("-fx-background-color: #202021 ; -fx-border-radius: 10; -fx-background-radius: 10;");

        Separator line = new Separator();
        StackPane.setAlignment(line, Pos.TOP_CENTER);
        line.setMinWidth(w - 30);
        line.setMaxWidth(w - 30);
        line.setTranslateY(20);
        line.setStyle("-fx-opacity: 30%;");
        panel.getChildren().add(line);

        Label label = new Label(text);
        StackPane.setAlignment(label, Pos.TOP_CENTER);
        label.setStyle("-fx-background-color:#202021; -fx-text-fill: #FFFF; -fx-font-size: 18px; -fx-opacity: 100%; -fx-font-weight: bold; -fx-: #d4cfd0;");
        label.setTranslateY(5);
        label.setPadding(new Insets(0, 10, 0, 10));
        panel.getChildren().add(label);

        return panel;
    }

    private StackPane panelContent(StackPane panel) {
        StackPane content = new StackPane();
        StackPane.setAlignment(content, Pos.BOTTOM_CENTER);
        content.minWidthProperty().bind(panel.minWidthProperty().subtract(30));
        content.maxWidthProperty().bind(panel.maxWidthProperty().subtract(30));
        content.minHeightProperty().bind(panel.minHeightProperty().subtract(30));
        content.maxHeightProperty().bind(panel.maxHeightProperty().subtract(30));
        panel.getChildren().add(content);

        return content;
    }
    //endregion

    private Button setupSocialBtn(String img, double x) {
        int size = 50;

        ImageView imgView = Resources.getImageView(img);
        imgView.setFitHeight(size - 15d);
        imgView.setPreserveRatio(true);

        Button btn = new Button("", imgView);
        StackPane.setAlignment(btn, Pos.CENTER_LEFT);
        btn.setTranslateX(x);
        btn.setTranslateY(0);
        btn.setMinSize(size, size);
        btn.setMaxSize(size, size);
        btn.setStyle("-fx-background-color: #2A2A2A; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10;");
        btn.setOnMouseEntered(e -> this.layout.setCursor(Cursor.HAND));
        btn.setOnMouseExited(event -> this.layout.setCursor(Cursor.DEFAULT));

        return btn;
    }


    private void settingPopup(Pane parent) {
        AnchorPane settingsPanel = new AnchorPane();
        parent.getChildren().add(settingsPanel);

        Button exitPanel = new Button();
        settingsPanel.getChildren().add(exitPanel);
        AnchorPane.setTopAnchor(exitPanel, 0d);
        AnchorPane.setBottomAnchor(exitPanel, 0d);
        AnchorPane.setLeftAnchor(exitPanel, 0d);
        AnchorPane.setRightAnchor(exitPanel, 0d);
        exitPanel.setOnMouseClicked(e -> parent.getChildren().remove(settingsPanel));
        exitPanel.setBackground(new Background(new BackgroundFill(new Color(0, 0, 0, 0.4), null, null)));

        StackPane pagePanel = new StackPane();
        AnchorPane.setTopAnchor(pagePanel, 0d);
        AnchorPane.setBottomAnchor(pagePanel, 0d);
        AnchorPane.setLeftAnchor(pagePanel, 0d);
        AnchorPane.setRightAnchor(pagePanel, 0d);
        pagePanel.setPickOnBounds(false);
        settingsPanel.getChildren().add(pagePanel);

        StackPane settingsPopup = setupPanel(900, 600, 0, 0, "Paramètres", pagePanel);

        Label username = new Label("TODO");
        username.setStyle("-fx-text-fill: white;");
        settingsPopup.getChildren().add(username);
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            Main.logger.warning(e.getMessage());
        }
    }
}
