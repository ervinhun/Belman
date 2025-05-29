package dk.easv.belman.pl;

import dk.easv.belman.Main;
import dk.easv.belman.be.User;
import dk.easv.belman.exceptions.BelmanException;
import dk.easv.belman.pl.model.LoginModel;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginController {
    @FXML private MFXTextField     username;
    @FXML private MFXPasswordField password;
    @FXML private Button           confirm;
    @FXML private ImageView        cameraView;
    @FXML private Button           backBtn;
    @FXML private Label            errorLabel;
    @FXML private StackPane        stackP;
    @FXML private VBox             loginVBox;

    private LoginModel model;

    private Webcam webcam;
    private ScheduledExecutorService executor;
    private final Pattern pattern = Pattern.compile("hash:\\s*(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @FXML
    public void initialize() {
        model = new LoginModel();
        errorLabel.textProperty().bind(model.errorMessageProperty());
        errorLabel.visibleProperty().bind(model.errorMessageProperty().isNotEmpty());
        model.loggedInUserProperty().addListener((_, _, newUser) -> {
            if (newUser != null) {
                Platform.runLater(() -> openNextWindow(newUser));
            }
        });
        cameraView.fitWidthProperty().bind(stackP.widthProperty().multiply(0.8f));
        cameraView.fitHeightProperty().bind(stackP.heightProperty().multiply(0.8f));
    }

    @FXML
    private void login() {
        model.login(
                username.getText().trim(),
                password.getText().trim(),
                false
        );
    }

    private void openNextWindow(User user) {

        int role = user.getRoleId();
        try {
            FXMLLoader loader;
            Stage stage = (Stage) confirm.getScene().getWindow();
            Scene scene;

            switch (role) {
                case 1:
                    loader = new FXMLLoader(Main.class.getResource("FXML/admin.fxml"));
                    scene  = new Scene(loader.load());
                    AdminController adminController = loader.getController();
                    adminController.setLoggedinUser(user);
                    break;

                case 2:
                    loader = new FXMLLoader(Main.class.getResource("FXML/quality.fxml"));
                    scene  = new Scene(loader.load());
                    QualityController qcController = loader.getController();
                    qcController.setLoggedinUser(user);
                    break;

                case 3:
                    loader = new FXMLLoader(Main.class.getResource("FXML/operator.fxml"));
                    scene  = new Scene(loader.load());
                    OperatorController opController = loader.getController();
                    opController.setLoggedinUser(user);
                    break;

                default:
                    throw new IllegalStateException("Unknown role: " + role);
            }

            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            throw new BelmanException("Failed to load FXML: " + e);
        }
    }

    @FXML
    private void cameraLogin() {
        prepareStageCloseHandler();
        updateUIForCameraLogin();
        startWebcam();
        startCameraLoop();
    }

    private void prepareStageCloseHandler() {
        Stage stage = (Stage) confirm.getScene().getWindow();
        stage.setOnCloseRequest(_ -> {
            closeWebcam();
            shutdownExecutor();
        });
    }

    private void updateUIForCameraLogin() {
        cameraView.setVisible(true);
        backBtn.setVisible(true);
        backBtn.setDisable(false);
        loginVBox.setVisible(false);
        loginVBox.setDisable(true);
    }

    private void startWebcam() {
        webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(640, 480));
        webcam.open();
    }

    private void startCameraLoop() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::processCameraFrame, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void processCameraFrame() {
        try {
            BufferedImage img = webcam.getImage();
            if (img == null) return;

            Platform.runLater(() ->
                    cameraView.setImage(SwingFXUtils.toFXImage(img, null))
            );

            decodeQRCode(img);

        } catch (Exception e) {
            throw new BelmanException("Error during camera loop: " + e);
        }
    }

    private void decodeQRCode(BufferedImage img) {
        LuminanceSource source = new BufferedImageLuminanceSource(img);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = new MultiFormatReader().decode(bitmap);
            shutdownExecutor();
            closeWebcam();

            Platform.runLater(() -> handleQRCodeResult(result.getText()));

        } catch (NotFoundException _) {
            // No QR code found; silently ignore and wait for next frame
        }
    }

    private void handleQRCodeResult(String qrText) {
        Matcher matcher = pattern.matcher(qrText);
        if (matcher.find()) {
            String hash = matcher.group(1);
            model.login("user", hash, true);
        } else {
            showErrorLabel("Invalid QR code format. Please try again.");
        }
    }

    private void closeWebcam() {
        if (webcam != null) webcam.close();
    }

    private void shutdownExecutor() {
        if (executor != null && !executor.isShutdown()) executor.shutdownNow();
    }


    @FXML
    private void back()
    {
        if (webcam != null)  webcam.close();
        if (executor != null) executor.shutdownNow();
        cameraView.setVisible(false);
        cameraView.setDisable(true);
        backBtn.setVisible(false);
        backBtn.setDisable(true);
        loginVBox.setVisible(true);
        loginVBox.setDisable(false);
    }

    private void showErrorLabel(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setDisable(false);
    }
}
