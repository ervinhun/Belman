package dk.easv.belman.PL;

import com.github.sarxos.webcam.Webcam;
import dk.easv.belman.Main;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.User;
import dk.easv.belman.PL.model.OperatorModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
    private HBox selectMethod;

    private final String[] states = {"Images Needed", "Pending", "Signed ✅"};
    private final String placeholderUrl =
            getClass().getResource("/dk/easv/belman/Images/belman.png").toExternalForm();
    private final String addPhoto = getClass().getResource("/dk/easv/belman/Images/addPhoto.png").toExternalForm();
    private VBox previousVBox = null;
    private ImageView previousImageView = null;

    private OperatorModel model;

    private User loggedinUser;

    @FXML
    private void initialize() {
        model = new OperatorModel();
        refreshOrders();
    }

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
        // TODO: call model / BLL to mark photos confirmed
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

    }

    private void openOrder(String orderNumber) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("FXML/orderOperator.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            borderPane.setCenter(root);
            orderLabel.setText(orderNumber);

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/selectMethod.fxml"));
            fxmlLoader.setController(this);
            selectMethod = fxmlLoader.load();

            List<ImageView> imageViews = List.of(
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
            e.printStackTrace();
        }
    }

    private VBox createCard(Order order) {
        ImageView iv = new ImageView();
        Label state = new Label();

        if (order.getPhotos().isEmpty()) {
            iv.setImage(new Image(placeholderUrl));
            state.setText("Status: " + states[0]);
        } else {
            String raw = order.getPhotos().getFirst().getImagePath();
            File   f   = new File(raw);
            iv.setImage(f.exists()
                    ? new Image(f.toURI().toString())
                    : new Image(placeholderUrl));
            state.setText(order.getIsSigned()
                    ? "Status: " + states[2]
                    : "Status: " + states[1]);
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
        card.setOnMouseClicked(e -> openOrder(order.getOrderNumber()));

        return card;
    }

    public void setLoggedinUser(User user) {
        this.loggedinUser = user;
        System.out.println("Logged in as: " + user.getUsername());
    }
}
