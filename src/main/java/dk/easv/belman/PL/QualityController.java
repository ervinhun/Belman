package dk.easv.belman.PL;

import dk.easv.belman.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.io.IOException;

public class QualityController {
    @FXML
    private FlowPane ordersPane;

    @FXML
    private void initialize()
    {
        ordersPane.getChildren().add(createCard("0123456789", new Image(Main.class.getResourceAsStream("Images/belman.png"))));
    }

    @FXML
    private void applySearch()
    {

    }

    private void openOrder(String orderNumber)
    {

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
