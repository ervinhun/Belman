package dk.easv.belman.PL;

import dk.easv.belman.Main;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
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
import java.util.List;

public class OperatorController {
    @FXML
    private BorderPane borderPane;
    @FXML
    private VBox rightBox;
    @FXML
    private FlowPane ordersPane;
    @FXML
    private Label orderLabel;
    @FXML
    private ImageView frontImage;
    @FXML
    private ImageView topImage;
    @FXML
    private ImageView backImage;
    @FXML
    private ImageView rightImage;
    @FXML
    private ImageView leftImage;
    @FXML
    private ImageView additionalImage;

    private ObservableList<VBox> orders = FXCollections.observableArrayList();
    private String[] states = {"Images Needed", "Pending", "Signed ✅"};
    private User loggedinUser;
    private BLLManager bllManager = new BLLManager();

    @FXML
    private void initialize()
    {
        loggedinUser = null;
        orders.clear();
        ordersPane.getChildren().clear();
        addOrderCards();
    }

    private void addOrderCards()
    {
        for(Order o : bllManager.getOrders(null))
        {
            orders.add(createCard(o));
        }

        ordersPane.getChildren().addAll(orders);
    }

    @FXML
    private void cancel()
    {
        borderPane.setCenter(rightBox);
    }

    @FXML
    private void confirmImages()
    {

    }

    private void addImage(ImageView imageView)
    {
        imageView.setImage(new Image("dk/easv/belman/images/noImg.jpg"));
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
            List<ImageView> imageViews = List.of(
                    frontImage,
                    backImage,
                    leftImage,
                    rightImage,
                    topImage,
                    additionalImage
            );

            for (ImageView imageView : imageViews) {
                imageView.setOnMouseClicked(_ -> addImage(imageView));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private VBox createCard(Order order) {
        ImageView imageView = new ImageView();
        Label statusLabel = new Label();
        if(order.getPhotos().isEmpty())
        {
            imageView.setImage(new Image("dk/easv/belman/images/belman.png"));
            statusLabel.setText("Status: "+states[0]);
        }
        else
        {
            try
            {
                imageView.setImage(new Image(Main.class.getResourceAsStream(order.getPhotos().getFirst().getImagePath())));
            }
            catch (Exception e)
            {
                imageView.setImage(new Image("dk/easv/belman/images/belman.png"));
            }

            if(order.getIsSigned())
            {
                statusLabel.setText("Status: "+states[2]);
            }
            else if(order.getPhotos().isEmpty())
            {
                statusLabel.setText("Status: "+states[0]);
            }
            else
            {
                statusLabel.setText("Status: "+states[1]);
            }
        }
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        Rectangle clip = new Rectangle(100, 100);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        Label orderLabel = new Label("Order: " + order.getOrderNumber());

        VBox card = new VBox(10, imageView, orderLabel, statusLabel);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setId("orderCard");
        card.setPrefHeight(160);
        card.setOnMouseClicked(_ -> openOrder(order.getOrderNumber()));

        return card;
    }

    public void setLoggedinUser(User loggedinUser) {
        if (loggedinUser != null) {
            this.loggedinUser = loggedinUser;
            System.out.println("LoggedinUser: " + loggedinUser);
        } else
            System.out.println("No user is set who logged in");
    }
}
