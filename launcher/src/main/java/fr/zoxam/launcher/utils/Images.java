package fr.zoxam.launcher.utils;

import fr.zoxam.launcher.Main;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class Images {
    private Images() { throw new IllegalStateException("Utility class"); }
    public static Image getImageFromRessources(String path){ return new Image(Objects.requireNonNull(Main.class.getResource(path)).toExternalForm()); }
    public static ImageView getImageViewFromRessources(String path){ return new ImageView(getImageFromRessources(path)); }
}
