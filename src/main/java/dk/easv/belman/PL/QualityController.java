package dk.easv.belman.PL;

import dk.easv.belman.Main;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.User;
import dk.easv.belman.PL.model.QualityModel;
import dk.easv.belman.dal.FilePaths;
import dk.easv.belman.dal.OpenFile;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class QualityController {
    @FXML
    private FlowPane ordersPane;
    @FXML
    private BorderPane borderPane;
    @FXML
    private VBox rightBox;
    @FXML
    private Label orderLabel;
    @FXML
    private TextField search;
    @FXML
    Button btnSign;

    // ImageViews for different angles
    @FXML private ImageView topImage;
    @FXML private ImageView leftImage;
    @FXML private ImageView rightImage;
    @FXML private ImageView frontImage;
    @FXML private ImageView backImage;
    @FXML private ImageView additionalImage;

    private ImageView selectedImageView;

    @FXML private ChoiceBox<String> user;

    private final String[] states = {"Images Needed", "Pending", "Signed ✅"};
    private final String placeholderUrl =
            getClass().getResource("/dk/easv/belman/Images/belman.png")
                    .toExternalForm();

    private QualityModel model;
    private String orderNumberToSign;

    @FXML
    private void initialize() {
        if (model == null)
            model = new QualityModel();
        refreshContent();

        search.textProperty().addListener((obs, old, txt) -> {
            model.setSearchQuery(txt);
            model.applySearch();
            refreshContent();
        });

        setupImageClickHandlers();
    }

    private void setupImageClickHandlers() {
        ImageView[] views = {topImage, leftImage, rightImage, frontImage, backImage, additionalImage};
        for (ImageView iv : views) {
            if (iv == null) continue;
            iv.getStyleClass().add("clickable-image");
            iv.setOnMouseClicked(e -> {
                clearImageSelectionBorders();
                selectedImageView = iv;
                iv.getStyleClass().add("image-selected");
            });
        }
    }

    private void clearImageSelectionBorders() {
        ImageView[] views = {topImage, leftImage, rightImage, frontImage, backImage, additionalImage};
        for (ImageView iv : views) {
            if (iv != null)
                iv.getStyleClass().remove("image-selected");
        }
    }

    public void setLoggedinUser(User u) {
        model.setLoggedInUser(u);
        user.getItems().setAll(
                u.getFullName(),
                "Logout"
        );
        user.getSelectionModel().selectFirst();
        user.setOnAction(evt -> {
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
            System.out.println(e.getMessage());
        }
    }

    private void refreshContent() {
        ordersPane.getChildren().clear();
        for (Order o : model.getFilteredOrders()) {
            ordersPane.getChildren().add(createCard(o));
        }
    }

    @FXML
    private void cancel() {
        borderPane.setCenter(rightBox);
    }

    @FXML
    private void openImage() {
        if (selectedImageView == null) {
            showAlert("No image selected", "Please select an image to open.");
            return;
        }

        File file = (File) selectedImageView.getUserData();
        model.openFullImage(file);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void deleteImage() { /* move logic to model and call from here */ }

    private void openOrder(String orderNumber) {
        try {

            FXMLLoader loader = new FXMLLoader(Main.class.getResource("FXML/orderQuality.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            orderNumberToSign = orderNumber;
            borderPane.setCenter(root);
            orderLabel.setText(orderNumber);

            // Load images for preview
            loadOrderImages(orderNumber);

            if(model.isDocumentExists(orderNumber)) {
                btnSign.setText("Open\nDocument");
                btnSign.setOnAction(e -> {
                    OpenFile openFile = new OpenFile(FilePaths.REPORT_DIRECTORY + orderNumber + "/report.pdf");
                });
            } else {
                btnSign.setText("Sign\nOrder");
                btnSign.setOnAction(e -> signOrder());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadOrderImages(String orderNumber) {

        List<Photo> photos = model.getPhotosForOrder(orderNumber);


        setPlaceholder(topImage);
        setPlaceholder(leftImage);
        setPlaceholder(rightImage);
        setPlaceholder(frontImage);
        setPlaceholder(backImage);
        setPlaceholder(additionalImage);

        for (Photo photo : photos) {
            String angle = photo.getAngle();
            File file = new File(photo.getImagePath());
            Image image = file.exists() ? new Image(file.toURI().toString()) : new Image(placeholderUrl);

            switch (angle.toUpperCase()) {
                case "TOP" -> {
                    topImage.setImage(image);
                    topImage.setUserData(file);
                }
                case "LEFT" -> {
                    leftImage.setImage(image);
                    leftImage.setUserData(file);
                }
                case "RIGHT" -> {
                    rightImage.setImage(image);
                    rightImage.setUserData(file);
                }
                case "FULL" -> {
                    frontImage.setImage(image);
                    frontImage.setUserData(file);
                }
                case "BOTTOM" -> {
                    backImage.setImage(image);
                    backImage.setUserData(file);
                }
                case "ADDITIONAL" -> {
                    additionalImage.setImage(image);
                    additionalImage.setUserData(file);
                }
            }

        }
    }

    private void setPlaceholder(ImageView iv) {
        iv.setImage(new Image(placeholderUrl));
    }


    @FXML
    private void signOrder() {

        System.out.println("Sign Order clicked");
        // Check if order number is valid
        if (orderNumberToSign == null || orderNumberToSign.isEmpty()) {
            // Show an alert if the order number is missing
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing Order Number");
            alert.setHeaderText(null);
            alert.setContentText("No order number selected to sign.");
            alert.showAndWait();
            return;
        }
        if (model.signOrder(orderNumberToSign)) {
            btnSign.setText("Open\nDocument");
            btnSign.setOnAction(e -> {
                OpenFile openFile = new OpenFile(FilePaths.REPORT_DIRECTORY + orderNumberToSign + "/report.pdf");
            });
            // Show confirmation to the user
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Order Signed");
            alert.setHeaderText(null);
            alert.setContentText("Order " + orderNumberToSign + " has been signed successfully.");
            alert.showAndWait();

            // Optionally return to the overview or refresh UI
            cancel(); // This resets the view to the previous screen
        }
    }

        private VBox createCard (Order order){
            ImageView iv = new ImageView();
            Label state = new Label();

            if (order.getPhotos().isEmpty()) {
                iv.setImage(new Image(placeholderUrl));
                state.setText("Status: " + states[0]);
            } else {
                String path = order.getPhotos().getFirst().getImagePath();
                File f = new File(path);
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
            card.getProperties().put("orderNum", order.getOrderNumber());
            card.setOnMouseClicked(e -> openOrder(order.getOrderNumber()));
            return card;
        }
}
