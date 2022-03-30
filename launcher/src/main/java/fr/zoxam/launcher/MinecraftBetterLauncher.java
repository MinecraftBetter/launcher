package fr.zoxam.launcher;

import fr.zoxam.launcher.ui.PanelManager;
import fr.zoxam.launcher.ui.panels.PanelLogin;
import javafx.stage.Stage;

public class MinecraftBetterLauncher {

    private PanelManager panelManager;

    public void init(Stage stage){
        this.panelManager = new PanelManager(this, stage);
        this.panelManager.init();
        this.panelManager.showPanel(new PanelLogin());
    }

}
