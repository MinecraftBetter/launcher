package fr.zoxam.launcher.ui.panels;

import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import fr.zoxam.launcher.ui.PanelManager;
import fr.zoxam.launcher.ui.panel.Panel;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import java.awt.*;

public class PanelHome extends Panel {
    public PanelManager panelManager;
    public MinecraftProfile account;

    public PanelHome(MinecraftProfile account){
        this.account = account;
    }

    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);
        this.panelManager = panelManager;
        panelManager.SetBackground("/minecraftbetter/images/background.jpg");

        GridPane pagePanel = new GridPane();
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
        pagePanel.getChildren().add(username);
    }
}
