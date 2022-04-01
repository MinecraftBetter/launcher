package fr.zoxam.launcher.ui.panels.includes;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import fr.zoxam.launcher.Main;
import fr.zoxam.launcher.ui.PanelManager;
import fr.zoxam.launcher.ui.panel.Panel;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.InputStream;

public class TopPanel extends Panel {
    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);
        layout.setStyle("-fx-background-color: rgb(31,35,37);");

        GridPane titlePanel = new GridPane();
        layout.getChildren().add(titlePanel);
        GridPane.setHgrow(titlePanel, Priority.ALWAYS);
        GridPane.setVgrow(titlePanel, Priority.ALWAYS);

        InputStream iconStream = Main.class.getResourceAsStream("/minecraftbetter/images/icon.png");

        ImageView icon = new ImageView();
        titlePanel.getChildren().add(icon);
        icon.setImage(new Image(iconStream));
        icon.setFitWidth(18);
        icon.setPreserveRatio(true);
        GridPane.setVgrow(icon, Priority.ALWAYS);
        //GridPane.setValignment(icon, VPos.CENTER);
        icon.setTranslateX(5d);

        Label title = new Label("Minecraft Better");
        titlePanel.getChildren().add(title);
        title.setTranslateX(25);
        title.setFont(Font.loadFont(Main.class.getResourceAsStream("/minecraftbetter/fonts/OpenSans-Regular.ttf"), 14.0f));
        title.setStyle("-fx-text-fill: white;");
        GridPane.setVgrow(title, Priority.ALWAYS);


        GridPane topBarButton = new GridPane();
        layout.getChildren().add(topBarButton);
        GridPane.setHgrow(topBarButton, Priority.ALWAYS);
        GridPane.setVgrow(topBarButton, Priority.ALWAYS);
        topBarButton.setMinWidth(75);
        topBarButton.setMaxWidth(75);
        GridPane.setHalignment(topBarButton, HPos.RIGHT);

        MaterialDesignIconView hide = SetupButton(
                new MaterialDesignIconView(MaterialDesignIcon.WINDOW_MINIMIZE),
                e -> this.panelManager.getStage().setIconified(true), 0);
        MaterialDesignIconView fullscreen =  SetupButton(
                new MaterialDesignIconView(MaterialDesignIcon.WINDOW_MAXIMIZE),
                e -> this.panelManager.getStage().setMaximized(!this.panelManager.getStage().isMaximized()), 1);
        MaterialDesignIconView close = SetupButton(
                new MaterialDesignIconView(MaterialDesignIcon.WINDOW_CLOSE),
                e -> System.exit(0), 2, "18px");
        topBarButton.getChildren().addAll(close, fullscreen, hide);
    }

    MaterialDesignIconView SetupButton(MaterialDesignIconView btn, EventHandler<? super MouseEvent> onclick, int order) {return SetupButton(btn, onclick, order, "16px");}

    MaterialDesignIconView SetupButton(MaterialDesignIconView btn, EventHandler<? super MouseEvent> onclick, int order, String size) {
        GridPane.setVgrow(btn, Priority.ALWAYS);
        btn.setFill(Color.WHITE);
        btn.setOpacity(0.70f);
        btn.setSize(size);
        btn.setOnMouseEntered(e -> btn.setOpacity(1.0f));
        btn.setOnMouseExited(e -> btn.setOpacity(0.70f));
        btn.setOnMouseClicked(onclick);
        btn.setTranslateX(25 * order);
        return btn;
    }
}
