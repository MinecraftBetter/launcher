package fr.zoxam.launcher.ui.panel;

import fr.zoxam.launcher.ui.PanelManager;
import javafx.scene.layout.GridPane;

public interface IPanel {

    void init(PanelManager panelManager);
    GridPane getLayout();
    void onShow();

}
