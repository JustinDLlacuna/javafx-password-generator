package password.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import password.view.ScreenBounds;

public class Main extends Application implements ScreenBounds {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("/password/view/View.fxml"));

        primaryStage.setTitle("Password Generator");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(SCREEN_WIDTH / 2.0);
        primaryStage.setMinHeight(SCREEN_HEIGHT / 2.0);
        primaryStage.getIcons().add(new Image("/password/view/icon.png"));

        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
