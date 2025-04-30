package dk.easv.belman.PL;

import dk.easv.belman.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.io.IOException;

public class QualityController {
    @FXML
    private FlowPane ordersPane;
    @FXML
    private BorderPane borderPane;
    @FXML
    private VBox rightBox;
    @FXML
    private Label orderLabel;
    private ObservableList<VBox> orders = FXCollections.observableArrayList();

    @FXML
    private void initialize()
    {
        ordersPane.getChildren().clear();
        orders.add(createCard("0123456789", new Image(Main.class.getResourceAsStream("Images/belman.png"))));
        ordersPane.getChildren().addAll(orders);
    }

    @FXML
    private void applySearch()
    {

    }

    @FXML
    private void cancel()
    {
        borderPane.setCenter(rightBox);
    }

    @FXML
    private void openImage()
    {

    }

    @FXML
    private void deleteImage()
    {

    }

    @FXML
    private void signOrder()
    {

    }

    private void openOrder(String orderNumber)
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/orderQuality.fxml"));
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

    private VBox createCard(String orderNumber, Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        Rectangle clip = new Rectangle(100, 100);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        Label orderLabel = new Label("Order: " + orderNumber);

        VBox card = new VBox(10, imageView, orderLabel);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setId("orderCard");
        card.setPrefHeight(160);

        card.setOnMouseClicked(_ -> {openOrder(orderNumber);});

        return card;
    }
}
