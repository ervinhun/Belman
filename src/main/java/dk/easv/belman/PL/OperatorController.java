package dk.easv.belman.PL;

import dk.easv.belman.Main;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.ArrayList;

public class OperatorController {
    @FXML
    private BorderPane borderPane;
    @FXML
    private VBox rightBox;
    @FXML
    private FlowPane ordersPane;
    @FXML
    private Label orderLabel;
    @FXML Label lblFileName;
    @FXML
    Button btnChooseFile;
    private ObservableList<VBox> orders = FXCollections.observableArrayList();
    private String[] states = {"Images Needed", "Pending", "Signed ✅"};
    private User loggedinUser;
    private BLLManager bllManager = new BLLManager();
    private String orderNumber;
    private ArrayList<String> fileNames = new ArrayList<>();

    @FXML
    private void initialize()
    {
        //loggedinUser = null;

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
    private void cancelUpload() {
        fileNames.clear();
        orderNumber = null;
        cancel();
    }



    @FXML
    private void confirmImages()
    {

    }

    @FXML
    private void confirmImagesUpload() {
        if (orderNumber != null && fileNames != null && !fileNames.isEmpty()) {
            bllManager.uploadImages(orderNumber, fileNames, loggedinUser.getId());
            orderLabel.setText("Images uploaded successfully");
            cancel();
        } else {
            orderLabel.setText(orderLabel.getText() + " - No order selected");
        }
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
            this.orderNumber = orderNumber;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    private void addImage()
    {
        System.out.println("addImage");
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/orderOperatorPhotoUpload.fxml"));
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

    @FXML private void btnChooseFileClicked(Event event) {
        fileNames = bllManager.getUploadingFileNames(btnChooseFile, orderLabel.getText());
        String labelFileNames = "";
        if (fileNames != null && !fileNames.isEmpty()) {
            for (String fileName : fileNames) {
                labelFileNames += fileName + "\n";
            }
        }
        lblFileName.setText(labelFileNames);
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
            imageView.setImage(new Image(Main.class.getResourceAsStream(order.getPhotos().getFirst().getImagePath())));
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
