package password.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import password.view.ScreenBounds;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class Main extends Application implements ScreenBounds {
    static RandomAccessFile fileLock;

    @Override
    public void start(Stage primaryStage) throws Exception{
        if(appNotLocked()) {
            Parent root = FXMLLoader.load(getClass().getResource("/password/view/View.fxml"));

            primaryStage.setTitle("Password Generator");
            primaryStage.setScene(new Scene(root));
            primaryStage.setMinWidth(SCREEN_WIDTH / 2.0);
            primaryStage.setMinHeight(SCREEN_HEIGHT / 2.0);
            primaryStage.getIcons().add(new Image("/password/view/icon.png"));

            primaryStage.show();
        }
    }

    @Override
    public void stop() throws Exception {
        fileLock.close();
    }

    public static void main(String[] args) { launch(args); }

    private boolean appNotLocked(){
        try{
            File file = new File("app_lock");
            file.deleteOnExit();
            fileLock = new RandomAccessFile(file,"rw");
            FileChannel channel = fileLock.getChannel();

            if(channel.tryLock() == null) {
                Platform.exit();
                return false;
            }
        }catch(Exception e) {
            System.out.println(e);
        }
        return true;
    }
}
