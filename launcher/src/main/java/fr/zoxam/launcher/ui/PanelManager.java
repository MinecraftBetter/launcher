package fr.zoxam.launcher.ui;

import fr.arinonia.arilibfx.ui.utils.ResizeHelper;
import fr.zoxam.launcher.Main;
import fr.zoxam.launcher.MinecraftBetterLauncher;
import fr.zoxam.launcher.ui.panel.IPanel;
import fr.zoxam.launcher.ui.panels.includes.TopPanel;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.InputStream;

public class PanelManager {

    private final MinecraftBetterLauncher minecraftBetterLauncher;
    private final Stage stage;
    private final TopPanel topPanel = new TopPanel();
    private final GridPane centerPanel = new GridPane();

    private GridPane layout;

    public PanelManager(MinecraftBetterLauncher minecraftBetterLauncher, Stage stage) {
        this.minecraftBetterLauncher = minecraftBetterLauncher;
        this.stage = stage;
    }

    public void init() {
        stage.setTitle("MinecraftBetter");
        InputStream icon = Main.class.getResourceAsStream("/minecraftbetter/images/icon.png");
        if (icon != null) stage.getIcons().add(new Image(icon));
        stage.setMinWidth(1280);
        stage.setMaxWidth(1280);
        stage.setWidth(1280);
        stage.setMinHeight(720);
        stage.setMaxHeight(720);
        stage.setHeight(720);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.centerOnScreen();
        stage.show();

        layout = new GridPane();
        SetBackground(new Color(0.2,0.2,0.2,1));

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


    public Boolean SetBackground(Color color) {
        layout.setBackground(new Background(new BackgroundFill(color, null, null)));
        return true;
    }
    public Boolean SetBackground(String resource){
        InputStream stream = Main.class.getResourceAsStream(resource);
        if (stream == null) return false;

        BackgroundImage background = new BackgroundImage(new Image(stream),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, false, true));

        layout.setBackground(new Background(background));
        return true;
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
