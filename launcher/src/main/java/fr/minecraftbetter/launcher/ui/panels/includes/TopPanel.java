package fr.minecraftbetter.launcher.ui.panels.includes;

import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.ui.PanelManager;
import fr.minecraftbetter.launcher.ui.panel.Panel;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fluentui.FluentUiFilledAL;
import org.kordamp.ikonli.fluentui.FluentUiFilledMZ;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.InputStream;

public class TopPanel extends Panel {
    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);
        layout.setStyle("-fx-background-color: rgb(31,35,37);");

        GridPane topBar = new GridPane();
        layout.getChildren().add(topBar);
        GridPane.setHgrow(topBar, Priority.ALWAYS);
        GridPane.setVgrow(topBar, Priority.ALWAYS);

        GridPane titlePanel = new GridPane();
        topBar.getChildren().add(titlePanel);
        GridPane.setHgrow(titlePanel, Priority.ALWAYS);
        GridPane.setVgrow(titlePanel, Priority.ALWAYS);

        InputStream iconStream = Main.class.getResourceAsStream("/minecraftbetter/images/icon.png");
        if(iconStream != null) {
            ImageView icon = new ImageView();
            titlePanel.getChildren().add(icon);
            icon.setImage(new Image(iconStream));
            icon.setFitWidth(18);
            icon.setPreserveRatio(true);
            GridPane.setVgrow(icon, Priority.ALWAYS);
            icon.setTranslateX(5d);
        }

        Label title = new Label("Minecraft Better");
        titlePanel.getChildren().add(title);
        title.setTranslateX(25);
        title.setStyle("-fx-text-fill: white;");
        GridPane.setVgrow(title, Priority.ALWAYS);


        GridPane topBarButton = new GridPane();
        topBar.getChildren().add(topBarButton);
        GridPane.setHgrow(topBarButton, Priority.ALWAYS);
        GridPane.setVgrow(topBarButton, Priority.ALWAYS);
        topBarButton.setMinWidth(75);
        topBarButton.setMaxWidth(75);
        GridPane.setHalignment(topBarButton, HPos.RIGHT);

        FontIcon hide = setupButton(
                new FontIcon(FluentUiFilledMZ.MINIMIZE_28),
                e -> this.panelManager.getStage().setIconified(true), 1);
        FontIcon close = setupButton(
                new FontIcon(FluentUiFilledAL.DISMISS_28),
                e -> System.exit(0), 2, 18);
        topBarButton.getChildren().addAll(close, hide);
    }

    FontIcon setupButton(FontIcon btn, EventHandler<? super MouseEvent> onclick, int order) {return setupButton(btn, onclick, order, 16);}

    FontIcon setupButton(FontIcon btn, EventHandler<? super MouseEvent> onclick, int order, int size) {
        GridPane.setVgrow(btn, Priority.ALWAYS);
        btn.setFill(Color.WHITE);
        btn.setOpacity(0.70f);
        btn.setIconSize(size);
        btn.setOnMouseEntered(e -> btn.setOpacity(1.0f));
        btn.setOnMouseExited(e -> btn.setOpacity(0.70f));
        btn.setOnMouseClicked(onclick);
        btn.setTranslateX(25d * order);
        return btn;
    }
}
