package dk.easv.belman.PL;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;

import dk.easv.belman.Main;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;

import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;

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
    private MFXPasswordField password;
    @FXML
    private MFXTextField username;

    private BLLManager bllManager;

    @FXML
    public void initialize() {
        try {
            bllManager = new BLLManager();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void login() {
        String uName = username.getText().trim();
        String pwd = password.getText().trim();

        if (uName.isEmpty() || pwd.isEmpty()) {
            System.out.println("Please fill in username and password!");
            return;
        }

        User foundUser = bllManager.login(uName, pwd);

        if (foundUser == null) {
            System.out.println("Invalid username or password!");
            return;
        }

        int roleID = foundUser.getRoleId();
        System.out.println("Login roleId:" + roleID);
        switch (roleID) {
            case 1:
                openAdminWindow(foundUser);
                break;
            case 2:
                openQualityControllerWindow(foundUser);
                break;
            case 3:
                openOperatorWindow(foundUser);
                break;
            default:
                System.out.println("Unknown roleID: " + roleID);
        }
    }

    private void openQualityControllerWindow(User foundUser) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/quality.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            QualityController qcController;
            qcController = fxmlLoader.getController();
            qcController.setLoggedinUser(foundUser);
            Stage stage = (Stage) username.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("Error opening Coordinator window!");
            e.printStackTrace();
        }
    }

    private void openAdminWindow(User foundUser) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/admin.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            AdminController aController;
            aController = fxmlLoader.getController();
            aController.setLoggedinUser(foundUser);
            Stage stage = (Stage) username.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("Error opening Admin window!");
            e.printStackTrace();
        }
    }

    private void openOperatorWindow(User foundUser) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/operator.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            OperatorController oController;
            oController = fxmlLoader.getController();
            oController.setLoggedinUser(foundUser);
            Stage stage = (Stage) username.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("Error opening Admin window!");
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