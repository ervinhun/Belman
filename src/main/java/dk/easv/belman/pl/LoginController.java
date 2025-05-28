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
    @FXML private Button           cameraBtn;
    @FXML private Button           backBtn;
    @FXML private Label            errorLabel;
    @FXML private StackPane        stackP;
    @FXML private VBox             loginVBox;

    private LoginModel model;

    private Webcam webcam;
    private ScheduledExecutorService executor;
    private Pattern pattern = Pattern.compile("hash:\\s*(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @FXML
    public void initialize() {
        model = new LoginModel();
        errorLabel.textProperty().bind(model.errorMessageProperty());
        errorLabel.visibleProperty().bind(model.errorMessageProperty().isNotEmpty());
        model.loggedInUserProperty().addListener((obs, oldUser, newUser) -> {
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
        Stage stage = (Stage) confirm.getScene().getWindow();
        stage.setOnCloseRequest(_ -> {
            if (webcam != null)  webcam.close();
            if (executor != null) executor.shutdownNow();
        });

        cameraView.setVisible(true);
        backBtn.setVisible(true);
        backBtn.setDisable(false);
        loginVBox.setVisible(false);
        loginVBox.setDisable(true);

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
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                    try {
                        Result result = new MultiFormatReader().decode(bitmap);

                        // QR code found, shut down cam and exec
                        executor.shutdown();
                        webcam.close();
                        Platform.runLater(() -> {
                            Matcher matcher = pattern.matcher(result.getText());

                            if (matcher.find()) {
                                String hash = matcher.group(1);
                                model.login("user", hash, true);
                            }
                            else {
                                showErrorLabel("Invalid QR code format. Please try again.");
                            }
                        });

                    } catch (NotFoundException e) {
                        // no QR code - take next image
                    }
                }
            } catch (Exception e) {
                throw new BelmanException("Error during camera loop: " + e);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
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
