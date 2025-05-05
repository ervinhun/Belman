package dk.easv.belman.PL;

import dk.easv.belman.Main;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.w3c.dom.Text;

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
    @FXML
    private TextField search;
    private ObservableList<VBox> orders = FXCollections.observableArrayList();
    private FilteredList<VBox> filteredOrders = new FilteredList<>(orders);
    private String[] states = {"Images Needed", "Pending", "Signed ✅"};
    private User loggedinUser;
    private BLLManager bllManager = new BLLManager();

    @FXML
    private void initialize()
    {
        loggedinUser = null;
        ordersPane.getChildren().clear();
        orders.clear();
        addOrderCards();
    }

    private void addOrderCards()
    {
        for(Order o : bllManager.getOrders(null))
        {
            orders.add(createCard(o));
        }

        ordersPane.getChildren().addAll(filteredOrders);
    }

    @FXML
    private void applySearch()
    {
        ordersPane.getChildren().clear();
        filteredOrders.setPredicate(order -> {
            String orderNum = (String) order.getProperties().get("orderNum");
            return orderNum.toLowerCase().contains(search.getText().toLowerCase());
        });
        ordersPane.getChildren().addAll(filteredOrders);
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

    private VBox createCard(Order order) {
        ImageView imageView = new ImageView();
        Label statusLabel = new Label();
        if(order.getPhotos().isEmpty())
        {
            imageView.setImage(new Image(Main.class.getResourceAsStream("Images/belman.png")));
            statusLabel.setText("Status: "+states[0]);
        }
        else
        {
            imageView.setImage(new Image(order.getPhotos().getFirst().getImagePath()));
            if(order.getIsSigned())
            {
                statusLabel.setText("Status: "+states[2]);
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
        card.getProperties().put("orderNum", order.getOrderNumber());

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
