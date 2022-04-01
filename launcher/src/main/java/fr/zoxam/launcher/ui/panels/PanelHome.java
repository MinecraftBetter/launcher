package fr.zoxam.launcher.ui.panels;

import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import fr.zoxam.launcher.Main;
import fr.zoxam.launcher.ui.PanelManager;
import fr.zoxam.launcher.ui.panel.Panel;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.Cursor;
import javafx.scene.control.Label;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class PanelHome extends Panel {
    public PanelManager panelManager;
    public MinecraftProfile account;
    private GridPane centerPane;

    public PanelHome(MinecraftProfile account){
        this.account = account;
    }

    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);
        this.panelManager = panelManager;
        panelManager.SetBackground("/minecraftbetter/images/background.jpg");

        //Fond griser
        GridPane BackGray = new GridPane();
        BackGray.setStyle("-fx-background-color: #151516;-fx-opacity: 33%;");
        layout.getChildren().add(BackGray);

        /*GridPane pagePanel = new GridPane();
        layout.getChildren().add(pagePanel);
        pagePanel.setMinWidth(600);
        pagePanel.setMaxWidth(600);
        pagePanel.setMinHeight(450);
        pagePanel.setMaxHeight(450);
        GridPane.setVgrow(pagePanel, Priority.ALWAYS);
        GridPane.setHgrow(pagePanel, Priority.ALWAYS);
        GridPane.setValignment(pagePanel, VPos.CENTER);
        GridPane.setHalignment(pagePanel, HPos.CENTER);
        pagePanel.setStyle("-fx-background-color: #181818;");

        Label username = new Label("Welcome " + account.getName());
        username.setStyle("-fx-text-fill: white;");
        pagePanel.getChildren().add(username);*/
        // Panneau pour les réseaux sociaux.
        GridPane Social = new GridPane();
        layout.getChildren().add(Social);
        Social.setMinWidth(300);
        Social.setMaxWidth(200);
        Social.setMinHeight(100);
        Social.setMaxHeight(100);
        GridPane.setVgrow(Social, Priority.ALWAYS);
        GridPane.setHgrow(Social, Priority.ALWAYS);
        GridPane.setValignment(Social, VPos.CENTER);
        GridPane.setHalignment(Social, HPos.CENTER);
        Social.setTranslateX(400);
        Social.setStyle("-fx-background-color: #202021 ;-fx-border-radius: 10 10 10 10;-fx-background-radius: 10 10 10 10;");
        // Text réseaux sociaux
        Label SuivezNous = new Label("Suivre MinecraftBetter");
        GridPane.setVgrow(SuivezNous, Priority.ALWAYS);
        GridPane.setHgrow(SuivezNous, Priority.ALWAYS);
        GridPane.setHalignment(SuivezNous, HPos.CENTER);
        GridPane.setValignment(SuivezNous, VPos.CENTER);
        SuivezNous.setStyle("-fx-text-fill: #FFFF; -fx-font-size: 18px;-fx-opacity: 100%;-fx-font-weight: bold;");
        SuivezNous.setTranslateX(400);
        SuivezNous.setTranslateY(-36);
        layout.getChildren().add(SuivezNous);
        // Icon réseaux sociaux
        Image siteWebImage = new Image(Main.class.getResource("/minecraftbetter/images/Minecraft_Better_Logo_64x.png").toExternalForm());
        ImageView siteWebImageView = new ImageView(siteWebImage);
        GridPane.setVgrow(siteWebImageView, Priority.ALWAYS);
        GridPane.setHgrow(siteWebImageView, Priority.ALWAYS);
        GridPane.setHalignment(siteWebImageView, HPos.CENTER);
        GridPane.setValignment(siteWebImageView, VPos.CENTER);
        siteWebImageView.setTranslateX(285);
        siteWebImageView.setTranslateY(15);
        layout.getChildren().add(siteWebImageView);
        Button website = new Button("");
        GridPane.setVgrow(website, Priority.ALWAYS);
        GridPane.setHgrow(website, Priority.ALWAYS);
        GridPane.setHalignment(website, HPos.CENTER);
        GridPane.setValignment(website, VPos.CENTER);
        website.setTranslateX(285);
        website.setTranslateY(15);
        website.setMinWidth(50);
        website.setMaxWidth(50);
        website.setMinHeight(50);
        website.setMaxHeight(50);
        website.setStyle("-fx-background-color: #181818; -fx-text-fill: #5e5e5e; -fx-font-size: 14px;-fx-opacity: 00%;");
        website.setOnMouseEntered(e->this.layout.setCursor(Cursor.HAND));
        website.setOnMouseExited(event -> this.layout.setCursor(Cursor.DEFAULT));
        website.setOnMouseClicked(event -> {
            openUrl("http://minecraftbetter.fr");
        });
        layout.getChildren().add(website);

        Image discordImage = new Image(Main.class.getResource("/minecraftbetter/images/discord.png").toExternalForm());
        ImageView discordImageView = new ImageView(discordImage);
        GridPane.setVgrow(discordImageView, Priority.ALWAYS);
        GridPane.setHgrow(discordImageView, Priority.ALWAYS);
        GridPane.setHalignment(discordImageView, HPos.CENTER);
        GridPane.setValignment(discordImageView, VPos.CENTER);
        discordImageView.setTranslateX(358.3);
        discordImageView.setTranslateY(15);
        layout.getChildren().add(discordImageView);
        Button discord = new Button("");
        GridPane.setVgrow(discord, Priority.ALWAYS);
        GridPane.setHgrow(discord, Priority.ALWAYS);
        GridPane.setHalignment(discord, HPos.CENTER);
        GridPane.setValignment(discord, VPos.CENTER);
        discord.setTranslateX(358.3);
        discord.setTranslateY(15);
        discord.setMinWidth(50);
        discord.setMaxWidth(50);
        discord.setMinHeight(50);
        discord.setMaxHeight(50);
        discord.setStyle("-fx-background-color: #181818; -fx-text-fill: #5e5e5e; -fx-font-size: 14px;-fx-opacity: 00%;");
        discord.setOnMouseEntered(e->this.layout.setCursor(Cursor.HAND));
        discord.setOnMouseExited(event -> this.layout.setCursor(Cursor.DEFAULT));
        discord.setOnMouseClicked(event -> {
            openUrl("https://discord.com/invite/4TC5eNEkE5");
        });
        layout.getChildren().add(discord);

        Image twitterImage = new Image(Main.class.getResource("/minecraftbetter/images/twitter.png").toExternalForm());
        ImageView twitterImageView = new ImageView(twitterImage);
        GridPane.setVgrow(twitterImageView, Priority.ALWAYS);
        GridPane.setHgrow(twitterImageView, Priority.ALWAYS);
        GridPane.setHalignment(twitterImageView, HPos.CENTER);
        GridPane.setValignment(twitterImageView, VPos.CENTER);
        twitterImageView.setTranslateX(431.6);
        twitterImageView.setTranslateY(15);
        layout.getChildren().add(twitterImageView);
        Button twitter = new Button("");
        GridPane.setVgrow(twitter, Priority.ALWAYS);
        GridPane.setHgrow(twitter, Priority.ALWAYS);
        GridPane.setHalignment(twitter, HPos.CENTER);
        GridPane.setValignment(twitter, VPos.CENTER);
        twitter.setTranslateX(431.6);
        twitter.setTranslateY(15);
        twitter.setMinWidth(50);
        twitter.setMaxWidth(50);
        twitter.setMinHeight(50);
        twitter.setMaxHeight(50);
        twitter.setStyle("-fx-background-color: #181818; -fx-text-fill: #5e5e5e; -fx-font-size: 14px;-fx-opacity: 00%;");
        twitter.setOnMouseEntered(e->this.layout.setCursor(Cursor.HAND));
        twitter.setOnMouseExited(event -> this.layout.setCursor(Cursor.DEFAULT));
        twitter.setOnMouseClicked(event -> {
            openUrl("https://twitter.com/Minecraftbetter");
        });
        layout.getChildren().add(twitter);

        Image youtubeImage = new Image(Main.class.getResource("/minecraftbetter/images/youtube.png").toExternalForm());
        ImageView youtubeImageView = new ImageView(youtubeImage);
        GridPane.setVgrow(youtubeImageView, Priority.ALWAYS);
        GridPane.setHgrow(youtubeImageView, Priority.ALWAYS);
        GridPane.setHalignment(youtubeImageView, HPos.CENTER);
        GridPane.setValignment(youtubeImageView, VPos.CENTER);
        youtubeImageView.setTranslateX(505);
        youtubeImageView.setTranslateY(15);
        layout.getChildren().add(youtubeImageView);
        Button youtube = new Button("");
        GridPane.setVgrow(youtube, Priority.ALWAYS);
        GridPane.setHgrow(youtube, Priority.ALWAYS);
        GridPane.setHalignment(youtube, HPos.CENTER);
        GridPane.setValignment(youtube, VPos.CENTER);
        youtube.setTranslateX(505);
        youtube.setTranslateY(15);
        youtube.setMinWidth(50);
        youtube.setMaxWidth(50);
        youtube.setMinHeight(50);
        youtube.setMaxHeight(50);
        youtube.setStyle("-fx-background-color: #181818; -fx-text-fill: #5e5e5e; -fx-font-size: 14px;-fx-opacity: 00%;");
        youtube.setOnMouseEntered(e->this.layout.setCursor(Cursor.HAND));
        youtube.setOnMouseExited(event -> this.layout.setCursor(Cursor.DEFAULT));
        youtube.setOnMouseClicked(event -> {
            openUrl("https://www.youtube.com/channel/Minecraftbetter");
        });
        layout.getChildren().add(youtube);

        //Bouton Jouer
        Image playImage = new Image(Main.class.getResource("/minecraftbetter/images/play.png").toExternalForm());
        ImageView ViewPlayImage = new ImageView(playImage);
        Button Play = new Button("JOUER");
        GridPane.setVgrow(Play, Priority.ALWAYS);
        GridPane.setHgrow(Play, Priority.ALWAYS);
        GridPane.setHalignment(Play, HPos.LEFT);
        GridPane.setValignment(Play, VPos.TOP);
        Play.setTranslateX(140);
        Play.setTranslateY(200);
        Play.setMinHeight(50);
        Play.setMaxHeight(50);
        Play.setMinWidth(200);
        Play.setMaxWidth(200);
        Play.setStyle("-fx-background-color:#fd000f; -fx-text-fill: #FFFF; -fx-font-size: 14px;-fx-font-weight: bold;-fx-border-radius: 45 45 45 45;-fx-background-radius: 45 45 45 45;");
        Play.setGraphic(ViewPlayImage);
        layout.getChildren().add(Play);

        // Panneau pour les Infos Serveur.
        GridPane Info = new GridPane();
        layout.getChildren().add(Info);
        Info.setMinWidth(300);
        Info.setMaxWidth(200);
        Info.setMinHeight(200);
        Info.setMaxHeight(200);
        GridPane.setVgrow(Info, Priority.ALWAYS);
        GridPane.setHgrow(Info, Priority.ALWAYS);
        GridPane.setValignment(Info, VPos.CENTER);
        GridPane.setHalignment(Info, HPos.CENTER);
        Info.setTranslateX(400);
        Info.setTranslateY(200);
        Info.setStyle("-fx-background-color: #202021 ;-fx-border-radius: 10 10 10 10;-fx-background-radius: 10 10 10 10;");
        // Texte Info
        Separator infoSeparator = new Separator();
        GridPane.setVgrow(infoSeparator, Priority.ALWAYS);
        GridPane.setHgrow(infoSeparator, Priority.ALWAYS);
        GridPane.setHalignment(infoSeparator, HPos.CENTER);
        GridPane.setValignment(infoSeparator, VPos.CENTER);
        infoSeparator.setMinWidth(250);
        infoSeparator.setMaxWidth(250);
        infoSeparator.setTranslateX(399);
        infoSeparator.setTranslateY(130);
        infoSeparator.setStyle("-fx-opacity: 30%;");
        layout.getChildren().add(infoSeparator);
        Button Infotext = new Button("Serveur");
        GridPane.setVgrow(Infotext, Priority.ALWAYS);
        GridPane.setHgrow(Infotext, Priority.ALWAYS);
        GridPane.setHalignment(Infotext, HPos.CENTER);
        GridPane.setValignment(Infotext, VPos.CENTER);
        Infotext.setStyle("-fx-background-color:#202021;-fx-text-fill: #FFFF; -fx-font-size: 18px;-fx-opacity: 100%;-fx-font-weight: bold;-fx-: #d4cfd0;");
        Infotext.setTranslateX(399);
        Infotext.setTranslateY(126);
        layout.getChildren().add(Infotext);

        //Panneau de News
        GridPane News = new GridPane();
        layout.getChildren().add(News);
        News.setMinWidth(600);
        News.setMaxWidth(600);
        News.setMinHeight(400);
        News.setMaxHeight(200);
        GridPane.setVgrow(News, Priority.ALWAYS);
        GridPane.setHgrow(News, Priority.ALWAYS);
        GridPane.setValignment(News, VPos.CENTER);
        GridPane.setHalignment(News, HPos.LEFT);
        News.setTranslateX(140);
        News.setTranslateY(125);
        News.setStyle("-fx-background-color: #202021 ;-fx-border-radius: 10 10 10 10;-fx-background-radius: 10 10 10 10;");
        //text News
        Separator newsSeparator = new Separator();
        GridPane.setVgrow(newsSeparator, Priority.ALWAYS);
        GridPane.setHgrow(newsSeparator, Priority.ALWAYS);
        GridPane.setHalignment(newsSeparator, HPos.LEFT);
        GridPane.setValignment(newsSeparator, VPos.CENTER);
        newsSeparator.setMinWidth(500);
        newsSeparator.setMaxWidth(500);
        newsSeparator.setTranslateX(185);
        newsSeparator.setTranslateY(-30);
        newsSeparator.setStyle("-fx-opacity: 30%;");
        layout.getChildren().add(newsSeparator);
        Label newText = new Label("News");
        GridPane.setVgrow(newText, Priority.ALWAYS);
        GridPane.setHgrow(newText, Priority.ALWAYS);
        GridPane.setHalignment(newText, HPos.LEFT);
        GridPane.setValignment(newText, VPos.CENTER);
        newText.setStyle("-fx-text-fill: #FFFF; -fx-font-size: 25px;-fx-opacity: 100%;-fx-font-weight: bold;-fx-: #d4cfd0;");
        newText.setTranslateX(420);
        newText.setTranslateY(-53);
        layout.getChildren().add(newText);
        //cologne réglage
        /*ColumnConstraints reglage = new ColumnConstraints();
        reglage.setHalignment(HPos.LEFT);
        reglage.setMinWidth(133);
        reglage.setMaxWidth(133);
        this.layout.getColumnConstraints().addAll(reglage, new ColumnConstraints());
        //bouton reglage
        Image reglageImage = new Image("/minecraftbetter/images/reglage.png");
        ImageView ViewReglageImage = new ImageView(reglageImage);
        Button reglageButton = new Button();
        GridPane.setVgrow(reglageButton, Priority.ALWAYS);
        GridPane.setHgrow(reglageButton, Priority.ALWAYS);
        GridPane.setHalignment(reglageButton, HPos.LEFT);
        GridPane.setValignment(reglageButton, VPos.CENTER);
        reglageButton.setTranslateX(20);
        reglageButton.setTranslateY(0);
        reglageButton.setMinHeight(75);
        reglageButton.setMaxHeight(75);
        reglageButton.setMinWidth(75);
        reglageButton.setMaxWidth(75);
        reglageButton.setStyle("-fx-background-color:#202021;-fx-font-size: 14px;-fx-border-radius: 25 25 25 25;-fx-background-radius: 25 25 25 25;");
        reglageButton.setGraphic(ViewReglageImage);
        layout.getChildren().add(reglageButton);*/


    }

    private void openUrl(String url){
        try{
            Desktop.getDesktop().browse(new URI(url));
        }catch (IOException | URISyntaxException e){
            Main.logger.warn(e.getMessage());
        }
    }
}
