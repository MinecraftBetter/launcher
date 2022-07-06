package fr.minecraftbetter.launcher.ui.utils;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class ProgressBarWithStatus extends StackPane {
    final ProgressBar installationProgress;
    final Label progressText;

    public ProgressBarWithStatus() {
        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        installationProgress = new ProgressBar();
        installationProgress.setStyle("-fx-accent: red;");
        StackPane.setAlignment(installationProgress, Pos.CENTER);
        installationProgress.prefWidthProperty().bind(prefWidthProperty());
        installationProgress.prefHeightProperty().bind(prefHeightProperty());

        progressText = new Label();
        progressText.setStyle("-fx-font-weight: bold;");
        progressText.textProperty().bind(installationProgress.progressProperty().multiply(100).asString("%02.0f%%"));
        DoubleBinding progressTextPos = installationProgress.progressProperty().multiply(prefWidthProperty());
        StackPane.setAlignment(progressText, Pos.CENTER_LEFT);
        progressText.translateXProperty().bind(Bindings.max(progressTextPos.subtract(40), 40));

        getChildren().addAll(installationProgress, progressText);
    }

    public void setProgress(double value) {installationProgress.setProgress(value);}
}
