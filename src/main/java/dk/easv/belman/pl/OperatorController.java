package dk.easv.belman.pl;

import com.github.sarxos.webcam.Webcam;
import dk.easv.belman.Main;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.User;
import dk.easv.belman.exceptions.BelmanException;
import dk.easv.belman.pl.model.OperatorModel;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OperatorController {
    @FXML private BorderPane borderPane;
    @FXML private VBox rightBox;
    @FXML private FlowPane ordersPane;
    @FXML private Label orderLabel;
    @FXML private ImageView frontImage;
    @FXML private ImageView topImage;
    @FXML private ImageView backImage;
    @FXML private ImageView rightImage;
    @FXML private ImageView leftImage;
    @FXML private ImageView additionalImage;
    @FXML private GridPane gridPane;
    @FXML private ChoiceBox<String> user;
    @FXML private ImageView cameraImage;
    @FXML private Button deleteBtn;
    @FXML private Button doneBtn;
    private HBox selectMethod;

    private final String[] states = {"Images Needed", "Pending", "Signed ✅"};
    private final String placeholderUrl =
            getClass().getResource("/dk/easv/belman/Images/belman.png").toExternalForm();
    private final String addPhoto = getClass().getResource("/dk/easv/belman/Images/addPhoto.png").toExternalForm();
    private VBox previousVBox = null;
    private ImageView previousImageView = null;
    private Webcam webcam;
    private ScheduledExecutorService executor;
    private VBox prevOrderView;
    private List<ImageView> imageViews;
    private List<String> angles = List.of("Front", "Back", "Left", "Right", "Top", "Additional");
    private static final Logger logger = LoggerFactory.getLogger(OperatorController.class);


    private OperatorModel model;


    private void refreshOrders() {
        ordersPane.getChildren().clear();
        for (Order o : model.getOrders()) {
            ordersPane.getChildren().add(createCard(o));
        }
    }

    @FXML
    private void cancel() {
        borderPane.setCenter(rightBox);
    }

    @FXML
    private void confirmImages() {
        for (int i = 0; i < imageViews.size(); i++)
        {
            if(Objects.equals(imageViews.get(i).getImage().getUrl(), addPhoto) && i < imageViews.size() - 1)
            {
                throw new BelmanException("At least 5 image is required");
            }
            else if(Objects.equals(imageViews.get(i).getImage().getUrl(), addPhoto))
            {
                imageViews.get(i).setImage(null);
            }
        }
        model.savePhotos(imageViews, angles, orderLabel.getText());
        borderPane.setCenter(rightBox);
    }

    private void showSelectMethod(ImageView clickedImage) {
        VBox currentVBox = (VBox) clickedImage.getParent();

        if (previousVBox != null && previousImageView != null) {
            previousVBox.getChildren().remove(selectMethod);
            if (!previousVBox.getChildren().contains(previousImageView)) {
                previousVBox.getChildren().add(previousImageView);
            }
        }

        currentVBox.getChildren().remove(clickedImage);
        currentVBox.getChildren().add(selectMethod);

        previousVBox = currentVBox;
        previousImageView = clickedImage;
    }

    @FXML
    private void photoUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Image Files (*.png, *.jpg, *.jpeg)", "*.jpg", "*.png", "*.jpeg");
        fileChooser.getExtensionFilters().add(filter);
        Stage stage = (Stage) borderPane.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if(selectedFile != null)
        {
            previousVBox.getChildren().remove(selectMethod);
            previousVBox.getChildren().add(previousImageView);
            previousImageView.setImage(new Image(selectedFile.toURI().toString()));
            previousVBox = null;
            previousImageView.setOnMouseClicked(_ -> {});
            previousImageView.setId(null);
            previousImageView = null;
        }
    }

    @FXML
    private void photoCamera() {
        try
        {
                prevOrderView = (VBox) borderPane.getCenter();
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("FXML/takeImage.fxml"));
                loader.setController(this);
                Parent root = loader.load();
                borderPane.setCenter(root);

                showCameraImage();
        }
        catch (IOException e)
        {
            throw new BelmanException("Failed to load FXML: takeImage.fxml " + e);
        }
    }

    @FXML
    private void showCameraImage()
    {
        deleteBtn.setVisible(false);
        deleteBtn.setDisable(true);
        doneBtn.setVisible(false);
        doneBtn.setDisable(true);
        Stage stage = (Stage) borderPane.getScene().getWindow();
        stage.setOnCloseRequest(_ -> {
            if (webcam != null)  webcam.close();
            if (executor != null) executor.shutdownNow();
        });

        webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(640, 480));
        webcam.open();

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                BufferedImage img = webcam.getImage();
                if (img != null) {
                    Platform.runLater(() ->
                            cameraImage.setImage(SwingFXUtils.toFXImage(img, null))
                    );
                }
            } catch (Exception _) {
                throw new BelmanException("Error get image from the webcam");
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    @FXML
    private void takeImage()
    {
        executor.shutdown();
        webcam.close();
        deleteBtn.setVisible(true);
        deleteBtn.setDisable(false);
        doneBtn.setVisible(true);
        doneBtn.setDisable(false);
    }

    @FXML
    private void confirmImage()
    {
        Image takenImage = cameraImage.getImage();
        borderPane.setCenter(prevOrderView);
        previousVBox.getChildren().remove(selectMethod);
        previousVBox.getChildren().add(previousImageView);
        previousImageView.setImage(takenImage);
        previousVBox = null;
        previousImageView.setOnMouseClicked(_ -> {});
        previousImageView.setId(null);
        previousImageView = null;
    }

    private void openOrder(Order order, Boolean isOpenable) {
        if(Boolean.TRUE.equals(isOpenable))
        {
            try {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("FXML/orderOperator.fxml"));
                loader.setController(this);
                Parent root = loader.load();
                borderPane.setCenter(root);
                orderLabel.setText(order.getOrderNumber());

                FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/selectMethod.fxml"));
                fxmlLoader.setController(this);
                selectMethod = fxmlLoader.load();

                imageViews = List.of(
                        frontImage,
                        backImage,
                        leftImage,
                        rightImage,
                        topImage,
                        additionalImage
                );

                for (ImageView imageView : imageViews) {
                    imageView.setOnMouseClicked(_ -> showSelectMethod(imageView));
                }
            } catch (IOException e) {
                logger.error("I/O exception in Operator controller, LN 242: {}", e);
            }
        }
        else
        {
            logger.warn("Order already sent for signing");
        }
    }

    private VBox createCard(Order order) {
        ImageView iv = new ImageView();
        Label state = new Label();
        Boolean isOpenable;
        String statusPreText = "Status: ";

        if (order.getPhotos().isEmpty()) {
            iv.setImage(new Image(placeholderUrl));
            state.setText(statusPreText + states[0]);
            isOpenable = true;
        } else {
            String raw = order.getPhotos().getFirst().getImagePath();
            File   f   = new File(raw);
            iv.setImage(f.exists()
                    ? new Image(f.toURI().toString())
                    : new Image(placeholderUrl));
            state.setText(Boolean.TRUE.equals(order.getIsSigned())
                    ? statusPreText + states[2]
                    : statusPreText + states[1]);
            isOpenable = false;
        }

        iv.setFitWidth(100);
        iv.setFitHeight(100);
        Rectangle clip = new Rectangle(100, 100);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        iv.setClip(clip);

        Label lbl = new Label("Order: " + order.getOrderNumber());

        VBox card = new VBox(10, iv, lbl, state);
        card.setAlignment(Pos.CENTER);
        card.setId("orderCard");
        card.setPrefHeight(160);
        card.setOnMouseClicked(e -> openOrder(order, isOpenable));

        return card;
    }

    public void setLoggedinUser(User u) {
        if(model == null)
        {
            model = new OperatorModel();
        }
        model.setLoggedInUser(u);
        refreshOrders();
        user.getItems().setAll(
                u.getFullName(),
                "Logout"
        );
        user.getSelectionModel().selectFirst();
        user.setOnAction(_ -> {
            if ("Logout".equals(user.getValue())) {
                model.logout();
                loggedOut();
            }
        });
    }

    private void loggedOut() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) user.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
