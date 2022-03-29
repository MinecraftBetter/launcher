package fr.zoxam.launcher.ui;

import fr.arinonia.arilibfx.ui.utils.ResizeHelper;
import fr.zoxam.launcher.MinecraftBetterLauncher;
import fr.zoxam.launcher.ui.panel.IPanel;
import fr.zoxam.launcher.ui.panels.includes.TopPanel;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
        this.stage.setTitle("MinecraftBetter");
        this.stage.setMinWidth(1280);
        this.stage.setWidth(1280);
        this.stage.setMinHeight(720);
        this.stage.setHeight(720);
        this.stage.initStyle(StageStyle.UNDECORATED);
        this.stage.centerOnScreen();
        this.stage.show();

        GridPane layout = new GridPane();
        //this.layout.setStyle(AriLibFX.setResponsiveBackground("http://minecraftbetter.fr/caca.png")); // TODO: Store this locally, having it remote increase the loading time (especially on low connexions)
        this.stage.setScene(new Scene(layout));

        RowConstraints topPanelConstraints = new RowConstraints();
        topPanelConstraints.setValignment(VPos.TOP);
        topPanelConstraints.setMinHeight(25);
        topPanelConstraints.setMaxHeight(25);
        layout.getRowConstraints().addAll(topPanelConstraints, new RowConstraints());
        layout.add(this.topPanel.getLayout(), 0, 0);
        this.topPanel.init(this);

        layout.add(this.centerPanel, 0, 1);
        GridPane.setVgrow(this.centerPanel, Priority.ALWAYS);
        GridPane.setHgrow(this.centerPanel, Priority.ALWAYS);
        ResizeHelper.addResizeListener(this.stage);

    }

    public void showPanel(IPanel panel) {
        this.centerPanel.getChildren().clear();
        this.centerPanel.getChildren().add(panel.getLayout());
        panel.init(this);
        panel.onShow();
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
