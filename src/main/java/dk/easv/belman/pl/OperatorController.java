package dk.easv.belman.pl;

import com.github.sarxos.webcam.Webcam;
import dk.easv.belman.Main;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.User;
import dk.easv.belman.exceptions.BelmanException;
import dk.easv.belman.pl.model.OperatorModel;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class OperatorController extends AbstractOrderController {
    @FXML private FlowPane ordersPane;
    @FXML private ImageView cameraImage;
    @FXML private Button deleteBtn;
    @FXML private Button doneBtn;
    @FXML private VBox cameraVbox;
    private HBox selectMethod;


    private final String addPhoto = Objects.requireNonNull(
                                    getClass().getResource("/dk/easv/belman/Images/addPhoto.png"))
                                    .toExternalForm();
    private VBox previousVBox = null;
    private ImageView previousImageView = null;
    private Webcam webcam;
    private ScheduledExecutorService executor;
    private VBox prevOrderView;
    private List<ImageView> imageViews;
    private final List<String> angles = List.of("Front", "Back", "Left", "Right", "Top", "Additional");
    private static final Logger logger = Logger.getLogger(OperatorController.class.getName());


    private OperatorModel model;

    @FXML
    public void initialize()
    {
        if(model == null)
        {
            model = new OperatorModel(this);
        }
        refreshOrders();
    }

    private void refreshOrders() {
        ordersPane.getChildren().clear();
        for (Order o : model.getOrders()) {
            if(o != null)
            {
                ordersPane.getChildren().add(createOrderCard(o));
            }
        }
    }

    @FXML
    private void confirmImages() throws IOException {
        int validImageCount = 0;
        for (ImageView imageView : imageViews) {
            Image img = imageView.getImage();
            if (img != null && !Objects.equals(img.getUrl(), addPhoto)) {
                validImageCount++;
            }
        }
        if (validImageCount < 5) {
            logger.warning("At least 5 real images (not placeholders) are required.");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Insufficient number of Images");
            alert.setContentText("At least 5 images are required to confirm the order.");
            alert.showAndWait();
            return;
        }
        else {  //If the photo is the placeholder, then it sets it to null
            for (ImageView imageView : imageViews) {
                if (Objects.equals(imageView.getImage().getUrl(), addPhoto)) {
                    imageView.setImage(null);
                }
            }
        }
        model.savePhotos(imageViews, angles, orderLabel.getText());
        borderPane.setCenter(rightBox);
        resizeWindow(rightBox);
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
            resizeWindow(root);

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
        cameraImage.fitWidthProperty().bind(cameraVbox.widthProperty().multiply(0.9f));
        cameraImage.fitHeightProperty().bind(cameraVbox.heightProperty().multiply(0.9f));
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
        resizeWindow(prevOrderView);
        previousVBox.getChildren().remove(selectMethod);
        previousVBox.getChildren().add(previousImageView);
        previousImageView.setImage(takenImage);
        previousVBox = null;
        previousImageView.setOnMouseClicked(_ -> {});
        previousImageView.setId(null);
        previousImageView = null;
    }

    private void openOrder() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/selectMethod.fxml"));
            fxmlLoader.setController(this);
            selectMethod = fxmlLoader.load();
            configureUserChoiceBox();

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
            throw new BelmanException("Failed to open order: " + e.getMessage());
        }
    }

    private VBox createOrderCard(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/belman/FXML/OrderCard.fxml"));
            VBox card = loader.load();
            OrderCardController controller = loader.getController();
            controller.setOrder(order);

            boolean isOpenable = order.getPhotos().isEmpty();
            if (isOpenable) {
                card.setOnMouseClicked(_ -> openOrderDetail("FXML/orderOperator.fxml", order.getOrderNumber(), Boolean.TRUE));
            }
            else
            {
                card.setOnMouseClicked(_ -> logger.warning("Order already sent for signing"));
            }

            return card;
        } catch (IOException e) {
            logger.warning("Failed to load order card: "+ e);
            return null;
        }
    }

    public User getUser()
    {
        return loggedInUser;
    }

    @Override
    protected List<Photo> getPhotosForOrder(String orderNumber) {
        return List.of();
    }

    @Override
    @FXML
    public void cancel() {
        borderPane.setCenter(rightBox);
        resizeWindow(rightBox);
        rebindUserChoiceBox(rightBox);
    }

    @Override
    protected void onDetailLoaded(String orderNumber) {
        openOrder();
        bindImages(0.9f);
    }

    @Override
    protected void onUserLogout() {
        model.logout();
    }
}
