package fr.minecraftbetter.launcher.ui.panel;

import fr.minecraftbetter.launcher.ui.PanelManager;
import javafx.scene.layout.GridPane;

public interface IPanel {

    void init(PanelManager panelManager);
    GridPane getLayout();
    void onShow();

}
