package fr.minecraftbetter.launcher.ui.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.StackPane;

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
}
