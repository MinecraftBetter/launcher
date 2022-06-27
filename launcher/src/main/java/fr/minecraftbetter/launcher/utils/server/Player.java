package fr.minecraftbetter.launcher.utils.server;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public final class Player extends ListCell<Player> {
    public final String name;
    public final String head;

    public Player() {
        name = "";
        head = "";
    }

    public Player(String name, String head) {
        this.name = name;
        this.head = head;
    }

    @Override
    public void updateItem(Player item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) return;
        HBox pane = new HBox(5);
        pane.prefWidthProperty().bind(getListView().widthProperty().subtract(22));
        pane.setMaxHeight(25);
        pane.setFillHeight(true);
        pane.setAlignment(Pos.CENTER_LEFT);

        ImageView desc = new ImageView(item.head);
        desc.fitHeightProperty().bind(pane.maxHeightProperty());
        desc.setPreserveRatio(true);
        Label title = new Label(item.name);
        pane.getChildren().addAll(desc, title);
        setGraphic(pane);

    }
}
