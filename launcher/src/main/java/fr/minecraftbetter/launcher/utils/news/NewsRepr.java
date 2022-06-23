package fr.minecraftbetter.launcher.utils.news;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.text.SimpleDateFormat;

public class NewsRepr extends ListCell<News> {
    @Override
    public void updateItem(News item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) return;
        VBox pane = new VBox(5);
        pane.prefWidthProperty().bind(getListView().widthProperty().subtract(22));
        pane.setMaxWidth(Region.USE_PREF_SIZE);

        HBox titlePane = new HBox();
        Label title = new Label(item.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-wrap-text: true;");
        Label date = new Label(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getDate()));
        date.setStyle("-fx-font-size: 10px; -fx-text-fill: #bdbdbd; -fx-font-weight: bold;");
        date.setPrefWidth(100);
        title.prefWidthProperty().bind(titlePane.widthProperty().subtract(date.prefWidthProperty()));
        titlePane.getChildren().addAll(title, date);

        Label desc = new Label(item.getDescription());
        desc.setWrapText(true);
        desc.setTextAlignment(TextAlignment.JUSTIFY);
        pane.getChildren().addAll(titlePane, desc);
        setGraphic(pane);

    }
}
