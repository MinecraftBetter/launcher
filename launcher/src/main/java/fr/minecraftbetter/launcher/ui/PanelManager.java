package fr.minecraftbetter.launcher.ui;

import fr.minecraftbetter.launcher.Main;
import fr.minecraftbetter.launcher.MinecraftBetterLauncher;
import fr.minecraftbetter.launcher.ui.panel.IPanel;
import fr.minecraftbetter.launcher.ui.panels.includes.TopPanel;
import fr.minecraftbetter.launcher.utils.ResizeHelper;
import fr.minecraftbetter.launcher.utils.Resources;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.util.logging.Level;

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
        stage.setTitle("Launcher Better");
        stage.getIcons().add(Resources.getImage("/minecraftbetter/images/icon.png"));
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

        try {
            if (Taskbar.isTaskbarSupported()) {
                var taskbar = Taskbar.getTaskbar();
                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    var icon = getClass().getResource("/minecraftbetter/images/icon.png");
                    if (icon != null) taskbar.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(icon.getFile()));
                }
            }
        }
        catch (Exception e){
            Main.logger.log(Level.WARNING, "Error setting dock icon", e);
        }

        layout = new GridPane();
        layout.getStylesheets().add(Resources.getResource("/minecraftbetter/stylesheets/root.css"));
        setBackground(new Color(0.2, 0.2, 0.2, 1));

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


    public Boolean setBackground(Color color) {
        layout.setBackground(new Background(new BackgroundFill(color, null, null)));
        return true;
    }

    public Boolean setBackground(String resource) {
        layout.setBackground(Resources.getBackground(resource));
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
