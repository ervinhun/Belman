package dk.easv.belman.PL;

import dk.easv.belman.Main;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.io.IOException;

public class AdminController {
    @FXML
    private FlowPane contentPane;
    @FXML
    private Label currentP;
    @FXML
    private Button newUser;
    @FXML private Button sideBtnSelected;
    @FXML private Button sideBtnNotSelected;
    @FXML private ImageView usersImage;
    @FXML private ImageView ordersImage;
    @FXML private BorderPane borderPane;
    @FXML private ScrollPane scrollP;
    private Node ordersRoot;
    private Parent newUserView;
    private FlowPane usersPane;
    private boolean isOrdersWin = true;
    private Image userSel = new Image(getClass().getResourceAsStream("/dk/easv/belman/Images/user.png"));
    private Image ordersSel = new Image(Main.class.getResourceAsStream("/dk/easv/belman/Images/orders.png"));
    private Image userDefault = new Image(Main.class.getResourceAsStream("/dk/easv/belman/Images/userDef.png"));
    private Image ordersDefault = new Image(Main.class.getResourceAsStream("/dk/easv/belman/Images/ordersDef.png"));
    private ObservableList<VBox> orders = FXCollections.observableArrayList();
    private ObservableList<VBox> users = FXCollections.observableArrayList();
    private String[] states = {"Images Needed", "Pending", "Signed ✅"};
    private final BLLManager bllManager = new BLLManager();
    private User loggedinUser;
  
    @FXML
    private void initialize()
    {
        orders.add(createOrderCard("0123456789", new Image(Main.class.getResourceAsStream("Images/belman.png")), states[1]));
        users.add(createUserCard("Username", "Operator", "2025-01-01"));
        ordersPane.getChildren().addAll(orders);
        loggedinUser = null;
        ordersRoot = scrollP.getContent();
        try {
            newUserView = FXMLLoader.load(Main.class.getResource("FXML/newUser.fxml"));
        } catch (IOException ex) { ex.printStackTrace(); }
        sideBtnNotSelected.setOnMouseEntered(e -> usersImage.setImage(userSel));
        sideBtnNotSelected.setOnMouseExited(e -> usersImage.setImage(userDefault));
        scrollP.setContent(ordersRoot);
    }

    @FXML
    private void usersTab() {
        if (isOrdersWin) {
            sideBtnNotSelected.setId("sideBtnSelected");
            sideBtnSelected.setId("sideBtnNotSelected");
            usersImage.setImage(userSel);
            ordersImage.setImage(ordersDefault);
            isOrdersWin = false;
            currentP.setText("Users");

            usersPane = new FlowPane(10,10);
            usersPane.setPadding(contentPane.getPadding());

            users.clear();
            for (User u : bllManager.getAllUsers()) users.add(createUserCard(u));
            usersPane.getChildren().setAll(users);
            scrollP.setContent(usersPane);
            newUser.setVisible(true); newUser.setDisable(false);
        }
    }

    @FXML
    private void ordersTab() {
        if (!isOrdersWin) {
            scrollP.setContent(ordersRoot);
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
        }
    }

    @FXML
    private void newUserTab() {
        scrollP.setContent(newUserView);
        currentP.setText("Create user");
        newUser.setVisible(false);
        newUser.setDisable(true);
    }

    @FXML
    private void applySearch()
    {

    }

    private VBox createOrderCard(String orderNumber, Image image, String state) {
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

        return card;
    }

    private void editUser(User user) {
        newUserTab();
    }

    private HBox createUserCard(User u) {
        Label name = new Label(u.getFullName());
        name.setId("cardTitle");

        Label role = new Label("Role: " + u.getRole());
        role.setId("cardText");

        Label lastLogin = new Label("Last login: " + u.getLastLoginTime());
        lastLogin.setId("cardText");

        VBox details = new VBox(5, name, role, lastLogin);
        details.setId("cardDetails");

        Button edit = new Button("\uD83D\uDD89");
        edit.getStyleClass().add("edit_button");
        edit.setPrefSize(35, 35);
        edit.setOnAction(e -> editUser(u));

        Button del = new Button("\uD83D\uDDD1");
        del.getStyleClass().add("delete_button");
        del.setPrefSize(35, 35);
        del.setOnAction(e -> {
            bllManager.deleteUser(u.getId());
            users.removeIf(b -> b.getUserData() == u);
            usersPane.getChildren().removeIf(b -> b.getUserData() == u);
        });

        HBox controls = new HBox(5, edit, del);
        controls.setAlignment(Pos.CENTER_RIGHT);

        HBox card = new HBox(20, details, controls);
        card.setId("userCard");
        card.setUserData(u);


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
