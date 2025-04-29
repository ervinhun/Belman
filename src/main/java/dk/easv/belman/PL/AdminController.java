package dk.easv.belman.PL;

import dk.easv.belman.Main;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class AdminController {
    @FXML
    private FlowPane ordersPane;
    @FXML
    private Label currentP;
    @FXML
    private Button newUser;
    @FXML private Button sideBtnSelected;
    @FXML private Button sideBtnNotSelected;
    @FXML private ImageView usersImage;
    @FXML private ImageView ordersImage;
    @FXML private BorderPane borderPane;
    private Boolean isOrdersWin = true;
    private Image userSel = new Image(getClass().getResourceAsStream("/dk/easv/belman/Images/user.png"));
    private Image ordersSel = new Image(Main.class.getResourceAsStream("/dk/easv/belman/Images/orders.png"));
    private Image userDefault = new Image(Main.class.getResourceAsStream("/dk/easv/belman/Images/userDef.png"));
    private Image ordersDefault = new Image(Main.class.getResourceAsStream("/dk/easv/belman/Images/ordersDef.png"));
    private ObservableList<VBox> orders = FXCollections.observableArrayList();
    private ObservableList<VBox> users = FXCollections.observableArrayList();

    @FXML
    private void initialize()
    {
        orders.add(createOrderCard("0123456789", new Image(Main.class.getResourceAsStream("Images/belman.png"))));
        users.add(createUserCard("Username", "Operator", "2025-01-01"));
        ordersPane.getChildren().addAll(orders);
        sideBtnNotSelected.setOnMouseEntered(e -> usersImage.setImage(userSel));
        sideBtnNotSelected.setOnMouseExited(e -> usersImage.setImage(userDefault));
    }

    @FXML
    private void usersTab()
    {
        if(isOrdersWin)
        {
            sideBtnNotSelected.setId("sideBtnSelected");
            sideBtnSelected.setId("sideBtnNotSelected");
            usersImage.setImage(userSel);
            ordersImage.setImage(ordersDefault);
            isOrdersWin = false;
            currentP.setText("Users");
            newUser.setVisible(true);
            newUser.setDisable(false);
            sideBtnNotSelected.setOnMouseEntered(e -> {});
            sideBtnNotSelected.setOnMouseExited(e -> {});
            sideBtnSelected.setOnMouseEntered(e -> ordersImage.setImage(ordersSel));
            sideBtnSelected.setOnMouseExited(e -> ordersImage.setImage(ordersDefault));
            ordersPane.getChildren().clear();
            ordersPane.getChildren().addAll(users);
        }
    }

    @FXML
    private void ordersTab()
    {
        if(!isOrdersWin)
        {
            sideBtnNotSelected.setId("sideBtnNotSelected");
            sideBtnSelected.setId("sideBtnSelected");
            usersImage.setImage(userDefault);
            ordersImage.setImage(ordersSel);
            isOrdersWin = true;
            currentP.setText("Orders");
            newUser.setVisible(false);
            newUser.setDisable(true);
            sideBtnNotSelected.setOnMouseEntered(e -> usersImage.setImage(userSel));
            sideBtnNotSelected.setOnMouseExited(e -> usersImage.setImage(userDefault));
            sideBtnSelected.setOnMouseEntered(e -> {});
            sideBtnSelected.setOnMouseExited(e -> {});
            ordersPane.getChildren().clear();
            ordersPane.getChildren().addAll(orders);
        }
    }

    @FXML
    private void newUserTab()
    {

    }

    @FXML
    private void applySearch()
    {

    }

    private VBox createOrderCard(String orderNumber, Image image) {
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

    public static VBox createUserCard(String username, String role, String lastLogin) {
        VBox card = new VBox();
        card.setSpacing(8);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(200);
        card.setId("userCard");

        Label user = new Label(username);
        user.setId("user");

        Label roleText = new Label(role);

        Label loginText = new Label("Last Login: " + lastLogin);

        card.getChildren().addAll(user, roleText, loginText);

        return card;
    }
}
