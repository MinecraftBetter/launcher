package fr.minecraftbetter.launcher.ui.utils;

import fr.minecraftbetter.launcher.Main;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class UiUtils {
    private UiUtils() {}

    public static StackPane panelTitle(String text) {
        StackPane panel = new StackPane();

        Separator line = new Separator();
        line.prefWidthProperty().bind(panel.prefWidthProperty().subtract(30));
        line.setOpacity(0.3); // 30%
        line.setTranslateY(20);
        StackPane.setAlignment(line, Pos.TOP_CENTER);
        panel.getChildren().add(line);

        Label label = new Label(text);
        StackPane.setAlignment(label, Pos.TOP_CENTER);
        label.setStyle("-fx-background-color:#202021; -fx-font-size: 18px; -fx-opacity: 100%; -fx-font-weight: bold; -fx-: #d4cfd0;");
        label.setTranslateY(5);
        label.setPadding(new Insets(0, 10, 0, 10));
        panel.getChildren().add(label);

        return panel;
    }

    public static void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            Main.logger.warning(e.getMessage());
        }
    }

    public static javafx.scene.control.Button setupButton(Pane layout, String text, String color, Ikon icon) {return setupButton(layout, text, color, icon, 10);}

    public static javafx.scene.control.Button setupButton(Pane layout, String text, String color, Ikon icon, int borderRadius) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(24);
        fontIcon.setFill(new Color(1, 1, 1, 1));
        javafx.scene.control.Button btn = new Button(text, fontIcon);
        btn.setStyle("-fx-background-color:" + color + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-radius: " + borderRadius + "; -fx-background-radius: " + borderRadius + ";");
        btn.setOnMouseEntered(e -> layout.setCursor(javafx.scene.Cursor.HAND));
        btn.setOnMouseExited(event -> layout.setCursor(Cursor.DEFAULT));
        return btn;
    }
}
