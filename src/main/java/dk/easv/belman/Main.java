package dk.easv.belman;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Order System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}