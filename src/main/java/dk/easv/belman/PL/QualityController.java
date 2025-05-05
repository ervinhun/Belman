package dk.easv.belman.PL;

import dk.easv.belman.Main;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import dk.easv.belman.dal.GenerateReport;
import dk.easv.belman.bll.BLLManager;
import dk.easv.belman.dal.OpenFile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import javafx.stage.Stage;

import org.w3c.dom.Text;


import java.io.File;
import java.io.IOException;
import java.util.UUID;

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
    private Button btnSign;
    private ObservableList<VBox> orders = FXCollections.observableArrayList();
    private FilteredList<VBox> filteredOrders = new FilteredList<>(orders);
    private String[] states = {"Images Needed", "Pending", "Signed ✅"};
    private User loggedinUser;

    @FXML
    private FlowPane imagesPane;

    private BLLManager bllManager = new BLLManager();
    private String productNumberToSign;
    private BLLManager bllManager;

    @FXML
    private void initialize()
    {
        //loggedinUser = null;
        ordersPane.getChildren().clear();

     //   orders.add(createCard("testProductNo", new Image(Main.class.getResourceAsStream("Images/belman.png")), states[2]));
        ordersPane.getChildren().addAll(orders);

        //orders.add(createCard("I524-08641", new Image(Main.class.getResourceAsStream("Images/belman.png")), states[2]));
        ordersPane.getChildren().addAll(orders);
        this.productNumberToSign = null;
        try
        {
            bllManager = new BLLManager();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
    private void openImage() {
        // Loop through all image nodes to find the selected one
        for (var node : imagesPane.getChildren()) {
            if (node.getStyle().contains("-fx-border-color")) {
                // Found the selected image
                File file = (File) node.getUserData(); // Get image file from ImageView
                if (file != null && file.exists()) {
                    // Load full-size image
                    Image fullImage = new Image(file.toURI().toString());

                    // Create an ImageView for the full-size image
                    ImageView fullImageView = new ImageView(fullImage);
                    fullImageView.setPreserveRatio(true);
                    fullImageView.setFitWidth(800); // Or adjust based on screen size

                    // Create a layout container and add the ImageView
                    VBox root = new VBox(fullImageView);
                    root.setPadding(new Insets(10));

                    // Create and show a new window
                    Stage imageStage = new Stage();
                    imageStage.setTitle("Full Image View");
                    imageStage.setScene(new Scene(root));
                    imageStage.show();
                }
                return; // Exit after opening the selected image
            }
        }

        // If no image was selected, show a warning
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No image selected");
        alert.setHeaderText(null);
        alert.setContentText("Please select an image to open.");
        alert.showAndWait();
    }

    @FXML
    private void deleteImage() {
        // Loop through all image nodes to find the selected one
        for (var node : imagesPane.getChildren()) {
            if (node.getStyle().contains("-fx-border-color")) {
                File file = (File) node.getUserData(); // Get the associated file

                if (file != null && file.exists()) {
                    boolean deleted = file.delete(); // Attempt to delete the file

                    if (deleted) {
                        System.out.println("Deleted file: " + file.getAbsolutePath());
                        imagesPane.getChildren().remove(node); // Remove from UI
                    } else {
                        System.out.println("Failed to delete file: " + file.getAbsolutePath());
                        // Show error alert if deletion failed
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Deletion Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Could not delete image.");
                        alert.showAndWait();
                    }
                } else {
                    System.out.println("Selected file does not exist.");
                }
                return; // Exit after handling the selected image
            }
        }

        // No image was selected
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Image Selected");
        alert.setHeaderText(null);
        alert.setContentText("Please select an image to delete.");
        alert.showAndWait();
    }

    @FXML
    private void signOrder()
    {
        if (productNumberToSign == null || productNumberToSign.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("No Order Selected");
            alert.setContentText("Please select an order to sign.");
            alert.showAndWait();
            return;
        }
        boolean isSuccess = bllManager.signOrder(productNumberToSign, loggedinUser.getId());
        Alert alert;
        if (isSuccess) {
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Order Signed");
            alert.setContentText("The order has been successfully signed.");
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Signing Failed");
            alert.setContentText("Failed to sign the order. Please try again.");
        }
        alert.showAndWait();
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
            boolean documentExists = bllManager.checkIfDocumentExists(orderNumber);
            if (documentExists) {
                btnSign.setText("Open\nDocument");
                btnSign.setOnAction(_ -> openDocument(orderNumber));
                btnSign.setPrefWidth(88);
            }
            else {
                btnSign.setText("Sign");
                btnSign.setAlignment(Pos.CENTER);
                btnSign.setOnAction(_ -> signOrder());
                btnSign.setPrefWidth(88);
            }

            loadImages(orderNumber);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void openDocument(String orderNumber) {
        String filePath = bllManager.getDocumentPath(orderNumber);
        if (filePath != null) {
            bllManager.openFile(filePath);

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

        card.setOnMouseClicked(_ -> {
            openOrder(orderNumber);
            setProductNumberToSign(orderNumber);
        });
        card.setOnMouseClicked(_ -> openOrder(order.getOrderNumber()));
        card.getProperties().put("orderNum", order.getOrderNumber());

        return card;
    }

    private void setProductNumberToSign(String orderNumber) {
        this.productNumberToSign = orderNumber;
    }

    public void setLoggedinUser(User loggedinUser) {
        if (loggedinUser != null) {
            this.loggedinUser = loggedinUser;
            System.out.println("LoggedinUser: " + loggedinUser);
        } else
            System.out.println("No user is set who logged in");
    }
}
