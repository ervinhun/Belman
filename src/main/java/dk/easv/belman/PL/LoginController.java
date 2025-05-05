package dk.easv.belman.PL;

import dk.easv.belman.Main;
import dk.easv.belman.be.User;
import dk.easv.belman.PL.model.LoginModel;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import javafx.scene.input.KeyEvent;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoginController {
    @FXML private MFXTextField     username;
    @FXML private MFXPasswordField password;
    @FXML private Button           confirm;
    @FXML private ImageView        cameraView;

    private LoginModel model;

    private Webcam webcam;
    private ScheduledExecutorService executor;

    @FXML
    public void initialize() {
        model = new LoginModel();

        model.loggedInUserProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                Platform.runLater(() -> openNextWindow(newUser));
            }
        });
    }

    @FXML
    private void login() {
        model.login(
                username.getText().trim(),
                password.getText().trim()
        );
    }

    private void openNextWindow(User user) {
        int role = user.getRoleId();
        try {
            FXMLLoader loader;
            switch (role) {
                case 1 -> loader = new FXMLLoader(Main.class.getResource("FXML/admin.fxml"));
                case 2 -> loader = new FXMLLoader(Main.class.getResource("FXML/quality.fxml"));
                case 3 -> loader = new FXMLLoader(Main.class.getResource("FXML/operator.fxml"));
                default -> throw new IllegalStateException("Unknown role: " + role);
            }
            Scene scene = new Scene(loader.load());
            Object controller = loader.getController();
            controller.getClass()
                    .getMethod("setLoggedinUser", User.class)
                    .invoke(controller, user);

            Stage stage = (Stage) confirm.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cameraLogin() {
        Stage stage = (Stage) confirm.getScene().getWindow();
        stage.setOnCloseRequest(_ -> {
            if (webcam != null)  webcam.close();
            if (executor != null) executor.shutdownNow();
        });

        cameraView.setVisible(true);
        webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(640, 480));
        webcam.open();

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                BufferedImage img = webcam.getImage();
                if (img != null) {
                    Platform.runLater(() ->
                            cameraView.setImage(SwingFXUtils.toFXImage(img, null))
                    );

                    // try decoding a QR code
                    LuminanceSource source = new BufferedImageLuminanceSource(img);
                    BinaryBitmap bitmap    = new BinaryBitmap(new HybridBinarizer(source));
                    Result result          = new MultiFormatReader().decode(bitmap);

                    // on success, shut down camera and invoke login
                    executor.shutdown();
                    webcam.close();
                    Platform.runLater(() -> {
                        cameraView.setVisible(false);
                        // assume QR text == username, empty password
                        model.login(result.getText(), "");
                    });
                }
            } catch (NotFoundException e) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }
}
