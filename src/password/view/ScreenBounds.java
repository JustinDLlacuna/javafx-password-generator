package password.view;

import javafx.geometry.Rectangle2D;

public interface ScreenBounds {
    Rectangle2D SCREEN = javafx.stage.Screen.getPrimary().getVisualBounds();
    double SCREEN_WIDTH = SCREEN.getWidth();
    double SCREEN_HEIGHT = SCREEN.getHeight();
}
