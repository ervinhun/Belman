package dk.easv.belman.PL;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import dk.easv.belman.Main;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;

import java.awt.*;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoginController {
    @FXML
    private Button confirm;
    @FXML
    private ImageView cameraView;
    private Webcam webcam;
    private ScheduledExecutorService executor;

    @FXML
    private void login()
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/admin.fxml"));
            Stage stage = (Stage) confirm.getScene().getWindow();
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // CAMERA DOESNT SHUT DOWN IF CLOSED BY TOP RIGHT X
    @FXML
    private void cameraLogin()
    {
        cameraView.setVisible(true);
        webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(640, 480));
        webcam.open();

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                BufferedImage image = webcam.getImage();
                if (image != null) {
                    Platform.runLater(() -> cameraView.setImage(SwingFXUtils.toFXImage(image, null)));

                    //find and read QR code
                    LuminanceSource source = new BufferedImageLuminanceSource(image);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                    //throws NotFoundException if no QR
                    Result result = new MultiFormatReader().decode(bitmap);
                    System.out.println("QR Code text: " + result.getText());

                    executor.shutdown();
                    webcam.close();
                    Platform.runLater(this::login);
                }
            } catch (NotFoundException e) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }
}