package fr.zoxam.launcher.ui.panels;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import fr.zoxam.launcher.Main;
import fr.zoxam.launcher.ui.PanelManager;
import fr.zoxam.launcher.ui.panel.Panel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class PanelHome extends Panel {
    public static final Path MinecraftDir = PanelLogin.AppData.resolve(Paths.get("minecraft"));

    PanelManager panelManager;
    MinecraftProfile account;

    public PanelHome(MinecraftProfile account) {
        this.account = account;
    }

    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);
        this.panelManager = panelManager;
        panelManager.SetBackground("/minecraftbetter/images/background.png");

        StackPane panel = new StackPane();
        GridPane.setHgrow(panel, Priority.ALWAYS);
        GridPane.setVgrow(panel, Priority.ALWAYS);
        panel.setBackground(new Background(new BackgroundFill(new Color(0.08, 0.08, 0.08, 0.33), null, null))); // Darken the background
        layout.getChildren().add(panel);

        ImageView logo = new ImageView(new Image(Objects.requireNonNull(Main.class.getResource("/minecraftbetter/images/home/banner.png")).toExternalForm()));
        StackPane.setAlignment(logo, Pos.TOP_CENTER);
        logo.setTranslateY(50);
        logo.setFitHeight(100);
        logo.setPreserveRatio(true);
        panel.getChildren().add(logo);

        //region Left side
        // News
        StackPane news = setupPanel(600, 400, -175, 125, "Nouvelles", panel);

        // Install/Launch Btn
        boolean installed = Files.exists(MinecraftDir);
        MaterialDesignIconView viewPlayImage = new MaterialDesignIconView(installed ? MaterialDesignIcon.PLAY : MaterialDesignIcon.DOWNLOAD);
        viewPlayImage.setSize("24px");
        viewPlayImage.setFill(new Color(1, 1, 1, 1));
        Button play = new Button(installed ? "JOUER" : "INSTALLER", viewPlayImage);
        news.getChildren().add(play);
        StackPane.setAlignment(play, Pos.TOP_LEFT);
        play.setPrefSize(200, 50);
        play.setTranslateX(0);
        play.setTranslateY(-play.getPrefHeight() - 15);
        play.setStyle("-fx-background-color:#fd000f; -fx-text-fill: #FFFF; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-radius: 45; -fx-background-radius: 45;");
        play.setOnMouseEntered(e -> this.layout.setCursor(Cursor.HAND));
        play.setOnMouseExited(event -> this.layout.setCursor(Cursor.DEFAULT));
        //endregion

        //region Right side
        double rightWidth = 300;
        double rightX = -news.getTranslateX() + (news.getMinWidth() - rightWidth) / 2;
        // Social
        StackPane social = panelContent(setupPanel(rightWidth, 100, rightX, news.getTranslateY() - (news.getMinHeight() - 100) / 2, "Suivez-nous", panel));
        int btnSpacing = 300 / 4; // Panel size / number of btn
        Button website = setupSocialBtn("/minecraftbetter/images/home/website.png", 0);
        website.setOnMouseClicked(event -> openUrl("https://minecraftbetter.fr"));
        social.getChildren().add(website);
        Button discord = setupSocialBtn("/minecraftbetter/images/home/discord.png", btnSpacing);
        discord.setOnMouseClicked(event -> openUrl("https://discord.com/invite/4TC5eNEkE5"));
        social.getChildren().add(discord);
        Button twitter = setupSocialBtn("/minecraftbetter/images/home/twitter.png", btnSpacing * 2d);
        twitter.setOnMouseClicked(event -> {}); //TODO
        social.getChildren().add(twitter);
        Button youtube = setupSocialBtn("/minecraftbetter/images/home/youtube.png", btnSpacing * 3d);
        youtube.setOnMouseClicked(event -> {}); // TODO
        social.getChildren().add(youtube);

        // Server information
        StackPane server = setupPanel(rightWidth, 250, rightX, news.getTranslateY() + (news.getMinHeight() - 250) / 2, "Serveur", panel);
        Label username = new Label("Connecté en tant que " + account.getName());
        username.setStyle("-fx-text-fill: white;");
        server.getChildren().add(username);
        //endregion

        // Settings
        MaterialDesignIconView settingsIcon = new MaterialDesignIconView(MaterialDesignIcon.SETTINGS);
        settingsIcon.setSize("35px");
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

        ImageView imgView = new ImageView(new Image(Objects.requireNonNull(Main.class.getResource(img)).toExternalForm()));
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
            Main.logger.warn(e.getMessage());
        }
    }
}
