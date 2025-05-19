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

public class OperatorController extends AbstractOrderController {
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
            ordersPane.getChildren().add(createCard(o));
        }
    }

    @Override
    @FXML
    public void cancel() {
        borderPane.setCenter(rightBox);
        resizeWindow(rightBox);
    }

    @FXML
    private void confirmImages() throws IOException {
        for (int i = 0; i < imageViews.size(); i++)
        {
            if(Objects.equals(imageViews.get(i).getImage().getUrl(), addPhoto) && i < imageViews.size() - 1)
            {
                logger.warn("At least 5 images are required");
            }
            else if(Objects.equals(imageViews.get(i).getImage().getUrl(), addPhoto))
            {
                imageViews.get(i).setImage(null);
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

    private VBox createCard(Order order) {
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
                card.setOnMouseClicked(_ -> logger.warn("Order already sent for signing"));
            }

            return card;
        } catch (IOException e) {
            e.printStackTrace();
            return new VBox();
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
    protected void onDetailLoaded(String orderNumber) { openOrder(); }

    @Override
    protected void onUserLogout() {
        model.logout();
    }
}
