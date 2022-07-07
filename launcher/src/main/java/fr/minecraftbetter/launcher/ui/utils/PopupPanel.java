package fr.minecraftbetter.launcher.ui.utils;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;

public class PopupPanel extends StackPane {
    private Pane parent;
    private AnchorPane anchorPane;

    public void init(Pane parent) {
        this.parent = parent;
        anchorPane = new AnchorPane();
        parent.getChildren().add(anchorPane);

        Button exitPanel = new Button();
        anchorPane.getChildren().add(exitPanel);
        AnchorPane.setTopAnchor(exitPanel, 0d);
        AnchorPane.setBottomAnchor(exitPanel, 0d);
        AnchorPane.setLeftAnchor(exitPanel, 0d);
        AnchorPane.setRightAnchor(exitPanel, 0d);
        exitPanel.setOnMouseClicked(e -> {
            if (closeOnOutsideClick) dismiss();
            if (onExit != null) Platform.runLater(onExit);
        });
        exitPanel.setStyle("-fx-background-color: rgba(0,0,0,0.4);");

        StackPane pagePanel = new StackPane();
        AnchorPane.setTopAnchor(pagePanel, 0d);
        AnchorPane.setBottomAnchor(pagePanel, 0d);
        AnchorPane.setLeftAnchor(pagePanel, 0d);
        AnchorPane.setRightAnchor(pagePanel, 0d);
        pagePanel.setPickOnBounds(false);
        anchorPane.getChildren().add(pagePanel);

        pagePanel.getChildren().add(this);
        this.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(this, Pos.CENTER);
        this.setTranslateX(0);
        this.setTranslateY(0);
        this.setStyle("-fx-background-color: #202021; -fx-border-radius: 10; -fx-background-radius: 10;");
    }

    public PopupPanel(Pane parent) {init(parent);}

    public PopupPanel(Pane parent, String title) {
        init(parent);
        StackPane titlePanel = UiUtils.panelTitle(title);
        StackPane.setAlignment(titlePanel, Pos.TOP_CENTER);
        titlePanel.maxWidthProperty().bind(widthProperty().subtract(30));
        getChildren().add(titlePanel);
    }

    Runnable onExit;
    boolean closeOnOutsideClick = true;

    public void setOnExit(Runnable onExit) {this.onExit = onExit;}

    public void setCloseOnOutsideClick(boolean closeOnOutsideClick) {this.closeOnOutsideClick = closeOnOutsideClick;}

    public void dismiss() {parent.getChildren().remove(anchorPane);}
}
