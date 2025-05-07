package dk.easv.belman.PL;

import dk.easv.belman.Main;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.User;
import dk.easv.belman.PL.model.AdminModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.IOException;

public class AdminController {
    @FXML private FlowPane  contentPane;
    @FXML private Label     currentP;
    @FXML private Button    newUser;
    @FXML private Button    sideBtnSelected;
    @FXML private Button    sideBtnNotSelected;
    @FXML private ImageView usersImage;
    @FXML private ImageView ordersImage;
    @FXML private BorderPane borderPane;
    @FXML private TextField search;

    private VBox newUserWindow;
    private UserController userController;

    private final String placeholderUrl =
            getClass().getResource("/dk/easv/belman/Images/belman.png")
                    .toExternalForm();

    private final Image userSel     =
            new Image(getClass().getResourceAsStream("/dk/easv/belman/Images/user.png"));
    private final Image userDefault =
            new Image(getClass().getResourceAsStream("/dk/easv/belman/Images/userDef.png"));
    private final Image ordersSel   =
            new Image(getClass().getResourceAsStream("/dk/easv/belman/Images/orders.png"));
    private final Image ordersDefault =
            new Image(getClass().getResourceAsStream("/dk/easv/belman/Images/ordersDef.png"));

    private final String[] states = {"Images Needed", "Pending", "Signed ✅"};

    private final AdminModel model = new AdminModel();

    @FXML
    private void initialize() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("FXML/newUser.fxml"));
        newUserWindow   = loader.load();
        userController  = loader.getController();

        currentP.textProperty().bind(model.currentPageProperty());

        model.showingOrdersProperty().addListener((obs, was, isNow) -> updateTabStyles());

        search.textProperty().addListener((obs, oldText, newText) -> {
            model.searchQueryProperty().set(newText);
            model.applySearch();
            refreshContent();
        });

        model.loadOrders();

        updateTabStyles();
    }

    @FXML private void ordersTab()  { model.loadOrders(); }
    @FXML private void usersTab()   { model.loadUsers();  }
    @FXML private void newUserTab() {
        userController.getRightBox((VBox)borderPane.getCenter());
        borderPane.setCenter(newUserWindow);
        model.currentPageProperty().set("Create user");
        newUser.setVisible(false);
        newUser.setDisable(true);
    }

    public void setLoggedinUser(User u) {
        model.setLoggedInUser(u);
    }

    private void updateTabStyles() {
        boolean showOrders = model.showingOrdersProperty().get();

        if (showOrders) {
            sideBtnNotSelected.setId("sideBtnNotSelected");
            sideBtnSelected  .setId("sideBtnSelected");
            usersImage.setImage(userDefault);
            ordersImage.setImage(ordersSel);
            newUser.setVisible(false);
            newUser.setDisable(true);

            sideBtnNotSelected.setOnMouseEntered(e -> usersImage.setImage(userSel));
            sideBtnNotSelected.setOnMouseExited(e -> usersImage.setImage(userDefault));
            sideBtnSelected  .setOnMouseEntered(e -> {});
            sideBtnSelected  .setOnMouseExited(e -> {});

        } else {
            sideBtnNotSelected.setId("sideBtnSelected");
            sideBtnSelected  .setId("sideBtnNotSelected");
            usersImage.setImage(userSel);
            ordersImage.setImage(ordersDefault);
            newUser.setVisible(true);
            newUser.setDisable(false);

            sideBtnNotSelected.setOnMouseEntered(e -> {});
            sideBtnNotSelected.setOnMouseExited(e -> {});
            sideBtnSelected  .setOnMouseEntered(e -> ordersImage.setImage(ordersSel));
            sideBtnSelected  .setOnMouseExited(e -> ordersImage.setImage(ordersDefault));
        }

        refreshContent();
    }

@FXML
    private void applySearch(KeyEvent event) {
        model.searchQueryProperty().set(search.getText());
        model.applySearch();
        refreshContent();
    }

    private void refreshContent() {
        contentPane.getChildren().clear();

        if (model.showingOrdersProperty().get()) {
            for (Order o : model.getFilteredOrders()) {
                contentPane.getChildren().add(createOrderCard(o));
            }
        } else {
            for (User u : model.getFilteredUsers()) {
                contentPane.getChildren().add(createUserCard(u));
            }
        }
    }

    private VBox createOrderCard(Order order) {
        ImageView imageView = new ImageView();
        Label    status    = new Label();

        if (order.getPhotos().isEmpty()) {
            imageView.setImage(new Image(placeholderUrl));
            status.setText("Status: " + states[0]);
        } else {
            String rawPath = order.getPhotos().getFirst().getImagePath();
            File imgFile   = new File(rawPath);
            if (imgFile.exists()) {
                imageView.setImage(new Image(imgFile.toURI().toString()));
            } else {
                imageView.setImage(new Image(placeholderUrl));
            }
            status.setText(order.getIsSigned()
                    ? "Status: " + states[2]
                    : "Status: " + states[1]);
        }

        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        Rectangle clip = new Rectangle(100, 100);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        Label orderLabel = new Label("Order: " + order.getOrderNumber());

        VBox card = new VBox(10, imageView, orderLabel, status);
        card.setAlignment(Pos.CENTER);
        card.setId("orderCard");
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
        Label name      = new Label(u.getFullName());   name.setId("cardTitle");
        Label role      = new Label("Role: " + u.getRole());    role.setId("cardText");
        Label lastLogin = new Label("Last login: " + u.getLastLoginTime()); lastLogin.setId("cardText");

        VBox details = new VBox(5, name, role, lastLogin);
        details.setId("cardDetails");

        Button edit = new Button("✎");
        edit.getStyleClass().add("edit_button");
        edit.setOnAction(e -> editUser(u));

        Button del = new Button("🗑");
        del.getStyleClass().add("delete_button");
        del.setOnAction(e -> {
            model.deleteUser(u);
            refreshContent();
        });

        HBox controls = new HBox(5, edit, del);
        controls.setAlignment(Pos.CENTER_RIGHT);

        HBox card = new HBox(20, details, controls);
        card.setId("userCard");
        card.getProperties().put("username", u.getUsername());
        return card;
    }
    public void refreshUsers() {
        users.clear();
        for (User u : bllManager.getAllUsers()) {
            users.add(createUserCard(u));
        }
        applySearch();
    }


    private void editUser(User u) {
        newUserTab();
    }
}
