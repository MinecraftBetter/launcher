package fr.minecraftbetter.launcher;

import fr.minecraftbetter.launcher.ui.PanelManager;
import fr.minecraftbetter.launcher.ui.panels.PanelLogin;
import javafx.stage.Stage;

public class MinecraftBetterLauncher {

    private PanelManager panelManager;

    public void init(Stage stage){
        this.panelManager = new PanelManager(this, stage);
        this.panelManager.init();
        this.panelManager.showPanel(new PanelLogin());
    }

}
