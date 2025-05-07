package dk.easv.belman.PL;

import dk.easv.belman.Main;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
    @FXML private TextField search;
    private VBox newUserWindow;
    private boolean isOrdersWin = true;
    private VBox rightBox;
    private Image userSel = new Image(getClass().getResourceAsStream("/dk/easv/belman/Images/user.png"));
    private Image ordersSel = new Image(Main.class.getResourceAsStream("/dk/easv/belman/Images/orders.png"));
    private Image userDefault = new Image(Main.class.getResourceAsStream("/dk/easv/belman/Images/userDef.png"));
    private Image ordersDefault = new Image(Main.class.getResourceAsStream("/dk/easv/belman/Images/ordersDef.png"));
    private ObservableList<VBox> orders = FXCollections.observableArrayList();
    private ObservableList<HBox> users = FXCollections.observableArrayList();
    private FilteredList<VBox> filteredOrders = new FilteredList<>(orders);
    private FilteredList<HBox> filteredUsers = new FilteredList<>(users);
    private String[] states = {"Images Needed", "Pending", "Signed ✅"};
    private final BLLManager bllManager = new BLLManager();
    private User loggedinUser;
    private UserController userController;

    @FXML
    private void initialize()
    {
        addOrderCards();
        for (User u : bllManager.getAllUsers()) users.add(createUserCard(u));
        loggedinUser = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/newUser.fxml"));
            newUserWindow = fxmlLoader.load();
            userController = fxmlLoader.getController();
            userController.setAdminController(this);
        } catch (IOException ex) { ex.printStackTrace(); }
        sideBtnNotSelected.setOnMouseEntered(e -> usersImage.setImage(userSel));
        sideBtnNotSelected.setOnMouseExited(e -> usersImage.setImage(userDefault));
    }

    private void addOrderCards()
    {
        for(Order o : bllManager.getOrders(null))
        {
            orders.add(createOrderCard(o));
        }

        contentPane.getChildren().addAll(filteredOrders);
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
            search.setText("");
            applySearch();
            contentPane.getChildren().clear();
            contentPane.getChildren().setAll(filteredUsers);
            newUser.setVisible(true);
            newUser.setDisable(false);
            sideBtnNotSelected.setOnMouseEntered(_ -> {});
            sideBtnNotSelected.setOnMouseExited(_ -> {});
            sideBtnSelected.setOnMouseEntered(_ -> ordersImage.setImage(ordersSel));
            sideBtnSelected.setOnMouseExited(_ -> ordersImage.setImage(ordersDefault));
        }
    }

    @FXML
    private void ordersTab() {
        if (!isOrdersWin) {
            sideBtnNotSelected.setId("sideBtnNotSelected");
            sideBtnSelected.setId("sideBtnSelected");
            usersImage.setImage(userDefault);
            ordersImage.setImage(ordersSel);
            isOrdersWin = true;
            currentP.setText("Orders");
            newUser.setVisible(false);
            newUser.setDisable(true);
            search.setText("");
            applySearch();
            contentPane.getChildren().clear();
            contentPane.getChildren().addAll(filteredOrders);
            sideBtnNotSelected.setOnMouseEntered(_ -> usersImage.setImage(userSel));
            sideBtnNotSelected.setOnMouseExited(_ -> usersImage.setImage(userDefault));
            sideBtnSelected.setOnMouseEntered(_ -> {});
            sideBtnSelected.setOnMouseExited(_ -> {});
        }
    }

    @FXML
    private void newUserTab() {
        rightBox = (VBox) borderPane.getCenter();
        userController.getRightBox(rightBox);
        borderPane.setCenter(newUserWindow);
        currentP.setText("Create user");
        newUser.setVisible(false);
        newUser.setDisable(true);
    }

    @FXML
    private void applySearch()
    {
        contentPane.getChildren().clear();
        if(search.getText().isEmpty())
        {
            filteredOrders.setPredicate(_ -> true);
            filteredUsers.setPredicate(_ -> true);
        }
        else {
            filteredOrders.setPredicate(order -> {
                String orderNum = (String) order.getProperties().get("orderNum");
                return orderNum.toLowerCase().contains(search.getText().toLowerCase());
            });
            filteredUsers.setPredicate(user -> {
                String username = (String) user.getProperties().get("username");
                return username.toLowerCase().contains(search.getText().toLowerCase());
            });
        }

        if(isOrdersWin)
        {
            contentPane.getChildren().addAll(filteredOrders);
        }
        else
        {
            contentPane.getChildren().addAll(filteredUsers);
        }
    }

    private VBox createOrderCard(Order order) {
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
        card.getProperties().put("orderNum", order.getOrderNumber());

        return card;
    }
    public void addUserCard(User u) {
        HBox card = createUserCard(u);
        users.add(card);
        filteredUsers.setPredicate(_ -> true);
        if (!isOrdersWin) {
            contentPane.getChildren().clear();
            contentPane.getChildren().addAll(filteredUsers);
        }
    }


    private void editUser(User user) {
        newUserTab();
        userController.setEditingUser(user);
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
            contentPane.getChildren().removeIf(b -> b.getUserData() == u);
        });

        HBox controls = new HBox(5, edit, del);
        controls.setAlignment(Pos.CENTER_RIGHT);

        HBox card = new HBox(20, details, controls);
        card.setId("userCard");
        card.setUserData(u);
        card.getProperties().put("username", u.getFullName());

        return card;
    }
    public void refreshUsers() {
        users.clear();
        for (User u : bllManager.getAllUsers()) {
            users.add(createUserCard(u));
        }
        applySearch();
    }


    public void setLoggedinUser(User loggedinUser) {
        if (loggedinUser != null) {
            this.loggedinUser = loggedinUser;
            System.out.println("LoggedinUser: " + loggedinUser);
        } else
            System.out.println("No user is set who logged in");
    }

}
