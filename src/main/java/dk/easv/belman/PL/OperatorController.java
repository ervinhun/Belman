package dk.easv.belman.PL;

import dk.easv.belman.Main;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

public class OperatorController {
    @FXML
    private HBox coord;
    @FXML
    private FlowPane ordersPane;

    @FXML
    private void initialize()
    {
        ordersPane.getChildren().add(addCardToFlowPane("0123456789", new Image(Main.class.getResourceAsStream("Images/belman.png"))));
    }

    private VBox addCardToFlowPane(String orderNumber, Image image) {
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

        return card;
    }
}
