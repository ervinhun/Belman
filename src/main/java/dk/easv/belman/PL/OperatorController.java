package dk.easv.belman.PL;

import dk.easv.belman.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.io.IOException;

public class OperatorController {
    @FXML
    private BorderPane borderPane;
    @FXML
    private VBox rightBox;
    @FXML
    private FlowPane ordersPane;
    @FXML
    private Label orderLabel;
    private ObservableList<VBox> orders = FXCollections.observableArrayList();
    private String[] states = {"Images Needed", "Pending", "Signed ✅"};

    @FXML
    private void initialize()
    {
        ordersPane.getChildren().clear();
        orders.add(createCard("0123456789", new Image(Main.class.getResourceAsStream("Images/belman.png")), states[0]));
        ordersPane.getChildren().addAll(orders);
    }

    @FXML
    private void cancel()
    {
        borderPane.setCenter(rightBox);
    }

    @FXML
    private void addImage()
    {

    }

    @FXML
    private void confirmImages()
    {

    }

    private void openOrder(String orderNumber)
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/orderOperator.fxml"));
            fxmlLoader.setController(this);
            Parent root = fxmlLoader.load();
            borderPane.setCenter(root);
            orderLabel.setText(orderNumber);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private VBox createCard(String orderNumber, Image image, String state) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        Rectangle clip = new Rectangle(100, 100);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        Label orderLabel = new Label("Order: " + orderNumber);

        Label statusLabel = new Label("Status: " + state);

        VBox card = new VBox(10, imageView, orderLabel, statusLabel);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setId("orderCard");
        card.setPrefHeight(160);

        card.setOnMouseClicked(_ -> {openOrder(orderNumber);});

        return card;
    }
}
