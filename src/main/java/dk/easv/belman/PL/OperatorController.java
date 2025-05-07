package dk.easv.belman.PL;

import dk.easv.belman.Main;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.User;
import dk.easv.belman.PL.model.OperatorModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.IOException;

public class OperatorController {
    @FXML private BorderPane borderPane;
    @FXML private VBox       rightBox;
    @FXML private FlowPane   ordersPane;
    @FXML private Label      orderLabel;

    private final String[] states        = {"Images Needed", "Pending", "Signed ✅"};
    private final String   placeholderUrl =
            getClass().getResource("/dk/easv/belman/Images/belman.png").toExternalForm();

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
    private void addImage() {
        // TODO: invoke file chooser / webcam / model
    }

    @FXML
    private void confirmImages() {
        // TODO: call model / BLL to mark photos confirmed
    }

    private void openOrder(String orderNumber) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("FXML/orderOperator.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            borderPane.setCenter(root);
            orderLabel.setText(orderNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private VBox createCard(Order order) {
        ImageView iv    = new ImageView();
        Label     state = new Label();

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
