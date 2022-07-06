package fr.minecraftbetter.launcher.ui.panels;

import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.api.launcher.News;
import fr.minecraftbetter.launcher.api.server.Player;
import fr.minecraftbetter.launcher.api.server.ServerInfo;
import fr.minecraftbetter.launcher.ui.PanelManager;
import fr.minecraftbetter.launcher.ui.panel.Panel;
import fr.minecraftbetter.launcher.ui.utils.PopupPanel;
import fr.minecraftbetter.launcher.ui.utils.ProgressBarWithStatus;
import fr.minecraftbetter.launcher.ui.utils.UiUtils;
import fr.minecraftbetter.launcher.utils.Resources;
import fr.minecraftbetter.launcher.utils.installer.MinecraftInstance;
import fr.minecraftbetter.launcher.utils.installer.MinecraftManager;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.kordamp.ikonli.fluentui.FluentUiFilledAL;
import org.kordamp.ikonli.fluentui.FluentUiFilledMZ;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import static fr.minecraftbetter.launcher.ui.utils.UiUtils.openUrl;

public class PanelHome extends Panel {
    public static final String NEWS_API = "https://api.minecraftbetter.com/minecraftbetter/launcher/news";

    final MinecraftProfile account;
    final String accessToken;
    final MinecraftManager minecraftManager;

    public PanelHome(MinecraftProfile account, String accessToken) {
        this.account = account;
        this.accessToken = accessToken;
        minecraftManager = new MinecraftManager(Main.AppData, account, accessToken);
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

        // News
        StackPane news = setupPanel(600, 400, -175, 125, "Nouvelles", panel);
        StackPane newsContent = panelContent(news);
        newsPanel(newsContent);
        //endregion

        double rightWidth = 300;
        double rightX = -news.getTranslateX() + (news.getMinWidth() - rightWidth) / 2;

        // Social
        StackPane social = setupPanel(rightWidth, 100, rightX, news.getTranslateY() - (news.getMinHeight() - 100) / 2, "Suivez-nous", panel);
        StackPane socialContent = panelContent(social);
        socialPanel(socialContent);

        // Server information
        StackPane server = setupPanel(rightWidth, 250, rightX, news.getTranslateY() + (news.getMinHeight() - 250) / 2, "Serveur", panel);
        StackPane serverContent = panelContent(server);
        serverPanel(serverContent, rightWidth);

        // Settings
        Button settingsBtn = UiUtils.setupButton(layout, "", "#202021", FluentUiFilledMZ.SETTINGS_28);
        panel.getChildren().add(settingsBtn);
        StackPane.setAlignment(settingsBtn, Pos.TOP_LEFT);
        settingsBtn.setTranslateX(10);
        settingsBtn.setTranslateY(10);
        settingsBtn.setOnMouseClicked(event -> settingPopup(layout));

        // Install/Launch Btn
        boolean installed = Files.exists(minecraftManager.getMinecraftPath());
        Button play = UiUtils.setupButton(layout,
                installed ? "VERIFIER" : "INSTALLER", "#fd000f",
                installed ? FluentUiFilledAL.DOCUMENT_AUTOSAVE_24 : FluentUiFilledAL.ARROW_DOWNLOAD_24, 45);
        news.getChildren().add(play);
        StackPane.setAlignment(play, Pos.TOP_LEFT);
        play.setPrefSize(200, 50);
        play.setTranslateX(0);
        play.setTranslateY(-play.getPrefHeight() - 15);

        DoubleBinding progressLeftX = news.translateXProperty().subtract(news.widthProperty().divide(2)).add(play.translateXProperty()).add(play.widthProperty()).add(15);
        DoubleBinding progressRightX = social.translateXProperty().add(social.widthProperty().divide(2));
        DoubleBinding progressY = play.translateYProperty().add(play.heightProperty().divide(2)).add(news.translateYProperty()).subtract(news.heightProperty().divide(2));
        play.setOnMouseClicked(event -> playBtnClicked(play, panel, progressLeftX, progressRightX, progressY));
    }

    //region Home Panels
    private StackPane setupPanel(double w, double h, double x, double y, String text, Pane parent) {
        StackPane panel = new StackPane();
        parent.getChildren().add(panel);
        panel.setMinSize(w, h);
        panel.setMaxSize(w, h);
        panel.setPrefSize(w, h);
        StackPane.setAlignment(panel, Pos.CENTER);
        panel.setTranslateX(x);
        panel.setTranslateY(y);
        panel.setStyle("-fx-background-color: #202021; -fx-border-radius: 10; -fx-background-radius: 10;");

        StackPane title = UiUtils.panelTitle(text);
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        title.maxWidthProperty().bind(panel.widthProperty().subtract(30));
        panel.getChildren().add(title);
        return panel;
    }

    private StackPane panelContent(StackPane panel) {
        StackPane content = new StackPane();
        content.setTranslateY(30);
        StackPane.setAlignment(content, Pos.TOP_CENTER);
        content.maxWidthProperty().bind(panel.widthProperty().subtract(30));
        content.maxHeightProperty().bind(panel.heightProperty().subtract(45));
        panel.getChildren().add(content);
        return content;
    }
    //endregion

    //region Left side
    private void newsPanel(StackPane newsContent) {
        ScrollPane newsScroll = new ScrollPane();
        newsScroll.setFitToWidth(true);
        newsScroll.prefWidthProperty().bind(newsContent.widthProperty());
        VBox newsList = new VBox(5);
        newsScroll.setContent(newsList);
        newsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        newsContent.getChildren().add(newsScroll);
        new Thread(() -> {
            List<News> news = News.getNews(NEWS_API);
            Platform.runLater(() -> {
                for (News item : news) {
                    VBox pane = new VBox(5);
                    pane.prefWidthProperty().bind(newsList.widthProperty());
                    newsList.getChildren().add(pane);
                    pane.setMaxWidth(Region.USE_PREF_SIZE);

                    HBox titlePane = new HBox();
                    Label title = new Label(item.getTitle());
                    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-wrap-text: true;");
                    Label date = new Label(new SimpleDateFormat("yyyy-MM-dd").format(item.getDate()));
                    date.setStyle("-fx-font-size: 10px; -fx-text-fill: #bdbdbd; -fx-font-weight: bold;");
                    date.setAlignment(Pos.CENTER_RIGHT);
                    date.setPrefWidth(100);
                    title.prefWidthProperty().bind(titlePane.widthProperty().subtract(date.prefWidthProperty()));
                    titlePane.getChildren().addAll(title, date);

                    Label desc = new Label(item.getDescription());
                    desc.setWrapText(true);
                    desc.setTextAlignment(TextAlignment.JUSTIFY);
                    pane.getChildren().addAll(titlePane, desc);
                }
            });
        }).start();
    }
    //endregion

    //region Right side
    private void socialPanel(StackPane socialContent) {
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
    }

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
        btn.setStyle("-fx-background-color: #2A2A2A; -fx-font-size: 14px; -fx-background-radius: 10;");
        btn.setOnMouseEntered(e -> this.layout.setCursor(Cursor.HAND));
        btn.setOnMouseExited(event -> this.layout.setCursor(Cursor.DEFAULT));

        return btn;
    }

    private void serverPanel(StackPane serverContent, double rightWidth) {
        VBox serverBox = new VBox(10);
        serverBox.setPadding(new Insets(10, 0, 0, 0));
        serverContent.getChildren().add(serverBox);
        Label username = new Label("Connecté en tant que " + account.getName());

        Separator line = new Separator();
        line.setPrefWidth(rightWidth - 30);
        line.setOpacity(0.3);
        serverBox.getChildren().addAll(username, line);

        new Thread(() -> {
            ServerInfo serverInfo = ServerInfo.tryGet();
            Platform.runLater(() -> {
                if (serverInfo == null) {
                    serverBox.getChildren().add(new Label("Erreur de communication avec le serveur"));
                    return;
                }

                Label playerCount = new Label(serverInfo.players_online() + " joueur" + (serverInfo.players_online() > 1 ? "s" : "") + " / " + serverInfo.players_max());
                playerCount.prefWidthProperty().bind(serverContent.widthProperty());
                playerCount.setAlignment(Pos.CENTER);

                ScrollPane playerScroll = new ScrollPane();
                playerScroll.setPrefHeight(Region.USE_COMPUTED_SIZE);
                serverBox.getChildren().addAll(playerCount, playerScroll);
                playerScroll.setFitToWidth(true);
                playerScroll.prefWidthProperty().bind(serverBox.widthProperty());
                VBox playerList = new VBox(5);
                playerScroll.setContent(playerList);
                playerScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

                for (Player item : serverInfo.players()) {
                    HBox pane = new HBox(5);
                    pane.prefWidthProperty().bind(playerList.widthProperty());
                    pane.setMaxHeight(25);
                    pane.setFillHeight(true);
                    pane.setAlignment(Pos.CENTER_LEFT);

                    ImageView desc = new ImageView(item.head());
                    desc.fitHeightProperty().bind(pane.maxHeightProperty());
                    desc.setPreserveRatio(true);
                    Label title = new Label(item.name());
                    pane.getChildren().addAll(desc, title);
                    playerList.getChildren().add(pane);
                }
            });
        }).start();
    }
    //endregion

    private void playBtnClicked(Button play, StackPane panel, DoubleBinding progressLeftX, DoubleBinding progressRightX, DoubleBinding progressY) {
        if (Files.exists(minecraftManager.getMinecraftPath())) {
            play.setDisable(true);
            Main.logger.fine("The launch of Minecraft has been requested");
            MinecraftInstance instance = minecraftManager.startGame();
            if (instance.getStatus() == MinecraftInstance.StartStatus.ERROR) {
                Main.logger.severe("Couldn't start Minecraft");
                play.setDisable(false);
            } else if (instance.getStatus() == MinecraftInstance.StartStatus.STARTED) {
                Main.logger.fine("Minecraft has been started");
                ((FontIcon) play.getGraphic()).setIconCode(FluentUiFilledAL.CONTENT_SETTINGS_24);
                play.setText("LANCÉ");
                instance.onExit((error, process) -> {
                    if (Boolean.TRUE.equals(error)) {
                        Main.logger.severe("An error has occurred during Minecraft execution " + process.errorReader().lines().collect(Collectors.joining("\n")));
                    } else Main.logger.fine("Minecraft has been exited");
                    play.setDisable(false);
                    ((FontIcon) play.getGraphic()).setIconCode(FluentUiFilledMZ.PLAY_24);
                    play.setText("JOUER");
                });
            }
            if (instance.getStatus() != MinecraftInstance.StartStatus.INCOMPLETE_INSTALL) return;
            Main.logger.warning("The current installation of Minecraft is incomplete");
        }
        ((FontIcon) play.getGraphic()).setIconCode(FluentUiFilledAL.ARROW_DOWNLOAD_24);
        play.setText("INSTALLATION");

        play.setDisable(true);
        setupInstallationBar(play, panel, progressLeftX, progressRightX, progressY);
        minecraftManager.startInstall();
    }

    private void setupInstallationBar(Button play, StackPane panel, DoubleBinding progressLeftX, DoubleBinding progressRightX, DoubleBinding progressY) {
        ProgressBarWithStatus installationProgress = new ProgressBarWithStatus();
        installationProgress.prefWidthProperty().bind(progressRightX.subtract(progressLeftX));
        installationProgress.setPrefHeight(25);
        StackPane.setAlignment(installationProgress, Pos.CENTER);
        installationProgress.translateXProperty().bind(progressLeftX.add(progressRightX).divide(2));
        installationProgress.translateYProperty().bind(progressY);
        panel.getChildren().addAll(installationProgress);

        Label installationStatus = new Label();
        installationStatus.setStyle("-fx-font-weight: bold;");
        StackPane.setAlignment(installationStatus, Pos.BOTTOM_LEFT);
        installationStatus.setTranslateX(5);
        installationStatus.setTranslateY(-5);
        panel.getChildren().add(installationStatus);

        minecraftManager.setProgress(p -> {
            installationProgress.setProgress(p.getPercentage());
            installationStatus.setText(p.getStatus());
        });
        minecraftManager.setComplete(() -> {
            panel.getChildren().removeAll(installationProgress, installationStatus);

            ((FontIcon) play.getGraphic()).setIconCode(FluentUiFilledMZ.PLAY_24);
            play.setText("JOUER");
            play.setDisable(false);
        });
    }

    private void settingPopup(Pane parent) {
        PopupPanel settingsPopup = new PopupPanel(parent, "Paramètres");
        settingsPopup.setPrefSize(900, 600);
        StackPane settingsPopupContent = panelContent(settingsPopup);

        Label username = new Label("TODO");
        StackPane.setAlignment(username, Pos.CENTER);


        Label version = new Label("Version " + (Main.getBuildVersion() == null ? "unknown" : Main.getBuildVersion()));
        StackPane.setAlignment(version, Pos.BOTTOM_CENTER);
        settingsPopupContent.getChildren().addAll(username, version);
    }
}
