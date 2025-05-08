package dk.easv.belman.PL;

import dk.easv.belman.Main;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.User;
import dk.easv.belman.PL.model.QualityModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.IOException;

public class QualityController {
    @FXML private FlowPane  ordersPane;
    @FXML private BorderPane borderPane;
    @FXML private VBox rightBox;
    @FXML private Label      orderLabel;
    @FXML private TextField  search;

    private final String[] states        = {"Images Needed", "Pending", "Signed ✅"};
    private final String   placeholderUrl =
            getClass().getResource("/dk/easv/belman/Images/belman.png")
                    .toExternalForm();

    private QualityModel model;

    @FXML
    private void initialize() {
        model = new QualityModel();

        refreshContent();

        search.textProperty().addListener((obs, old, txt) -> {
            model.setSearchQuery(txt);
            model.applySearch();
            refreshContent();
        });
    }

    public void setLoggedinUser(User user) {
        model.setLoggedInUser(user);
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

    @FXML private void openImage()   { /* move logic to model and call from here */ }
    @FXML private void deleteImage() { /* move logic to model and call from here */ }

    private void openOrder(String orderNumber) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("FXML/orderQuality.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            borderPane.setCenter(root);
            orderLabel.setText(orderNumber);
        } catch (IOException e) {
          e.printStackTrace();
        }
    }


    @FXML
    private void signOrder() {
        // Get the order number from the label
        String orderNumber = orderLabel.getText();

        // Check if order number is valid
        if (orderNumber == null || orderNumber.isEmpty()) {
            // Show an alert if the order number is missing
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing Order Number");
            alert.setHeaderText(null);
            alert.setContentText("No order number selected to sign.");
            alert.showAndWait();
            return;
        }

        // Print debug info to console
        System.out.println("Signing order: " + orderNumber);

        // TODO: Add logic to update the order status in the database or mark it as signed

        // Show confirmation to the user
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Signed");
        alert.setHeaderText(null);
        alert.setContentText("Order " + orderNumber + " has been signed successfully.");
        alert.showAndWait();

        // Optionally return to the overview or refresh UI
        cancel(); // This resets the view to the previous screen
    }

    private VBox createCard(Order order) {
        ImageView iv    = new ImageView();
        Label     state = new Label();

        if (order.getPhotos().isEmpty()) {
            iv.setImage(new Image(placeholderUrl));
            state.setText("Status: " + states[0]);
        } else {
            String path = order.getPhotos().getFirst().getImagePath();
            File f      = new File(path);
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
