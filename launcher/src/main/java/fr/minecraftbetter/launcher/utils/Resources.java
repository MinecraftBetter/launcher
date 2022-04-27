package fr.minecraftbetter.launcher.utils;

import fr.minecraftbetter.launcher.Main;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;

import java.util.Objects;

public class Resources {
    private Resources() { throw new IllegalStateException("Utility class"); }
    public static String getResource(String path){ return Objects.requireNonNull(Main.class.getResource(path)).toExternalForm(); }
    public static Image getImage(String path){ return new Image(getResource(path)); }
    public static ImageView getImageView(String path){ return new ImageView(getImage(path)); }
    public static Background getBackground(String path){
        return new Background(new BackgroundImage(getImage(path),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(100, 100, true, true, false, true)));
    }
    public static Media getMedia(String path){ return new Media(getResource(path)); }
}
