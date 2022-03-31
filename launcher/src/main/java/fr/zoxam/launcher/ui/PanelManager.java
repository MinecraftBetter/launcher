package fr.zoxam.launcher.ui;

import fr.arinonia.arilibfx.AriLibFX;
import fr.arinonia.arilibfx.ui.utils.ResizeHelper;
import fr.zoxam.launcher.Main;
import fr.zoxam.launcher.MinecraftBetterLauncher;
import fr.zoxam.launcher.ui.panel.IPanel;
import fr.zoxam.launcher.ui.panels.includes.TopPanel;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.Console;
import java.io.InputStream;

public class PanelManager {

    private final MinecraftBetterLauncher minecraftBetterLauncher;
    private final Stage stage;
    private final TopPanel topPanel = new TopPanel();
    private final GridPane centerPanel = new GridPane();

    public PanelManager(MinecraftBetterLauncher minecraftBetterLauncher, Stage stage) {
        this.minecraftBetterLauncher = minecraftBetterLauncher;
        this.stage = stage;
    }

    public void init() {
        stage.setTitle("MinecraftBetter");
        InputStream icon = Main.class.getResourceAsStream("/icon.png");
        if (icon != null) stage.getIcons().add(new Image(icon));
        stage.setMinWidth(1280);
        stage.setWidth(1280);
        stage.setMinHeight(720);
        stage.setHeight(720);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.centerOnScreen();
        stage.show();

        GridPane layout = new GridPane();
        Background background = GetBackground("/background.jpg");
        if (background != null) layout.setBackground(background);

        stage.setScene(new Scene(layout));

        RowConstraints topPanelConstraints = new RowConstraints();
        topPanelConstraints.setValignment(VPos.TOP);
        topPanelConstraints.setMinHeight(25);
        topPanelConstraints.setMaxHeight(25);
        layout.getRowConstraints().addAll(topPanelConstraints, new RowConstraints());
        layout.add(topPanel.getLayout(), 0, 0);
        topPanel.init(this);

        layout.add(centerPanel, 0, 1);
        GridPane.setVgrow(centerPanel, Priority.ALWAYS);
        GridPane.setHgrow(centerPanel, Priority.ALWAYS);
        ResizeHelper.addResizeListener(stage);

    }

    public void showPanel(IPanel panel) {
        this.centerPanel.getChildren().clear();
        this.centerPanel.getChildren().add(panel.getLayout());
        panel.init(this);
        panel.onShow();
    }

    public static Background GetBackground(String resource){
        InputStream stream = Main.class.getResourceAsStream(resource);
        if (stream == null) return null;
        BackgroundImage background = new BackgroundImage(new Image(stream),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, false, true));
        return new Background(background);
    }

    public Stage getStage() {
        return stage;
    }

    public MinecraftBetterLauncher getMinecraftBetterLauncher() {
        return minecraftBetterLauncher;
    }

    public TopPanel getTopPanel() {
        return topPanel;
    }
}
