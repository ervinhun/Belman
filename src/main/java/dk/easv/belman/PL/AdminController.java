package dk.easv.belman.PL;

import dk.easv.belman.Main;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.io.IOException;

public class AdminController {

    @FXML private FlowPane contentPane;
    @FXML private Label currentP;
    @FXML private Button newUser;
    @FXML private ScrollPane scrollP;
    @FXML private Button sideBtnSelected;
    @FXML private Button sideBtnNotSelected;
    @FXML private ImageView usersImage;
    @FXML private ImageView ordersImage;
    private FlowPane usersPane;

    private boolean isOrdersWin = true;
    private Node ordersRoot;
    private Parent newUserView;

    private final Image userSel = new Image(getClass().getResourceAsStream("/dk/easv/belman/Images/user.png"));
    private final Image ordersSel = new Image(Main.class.getResourceAsStream("/dk/easv/belman/Images/orders.png"));
    private final Image userDefault = new Image(Main.class.getResourceAsStream("/dk/easv/belman/Images/userDef.png"));
    private final Image ordersDefault = new Image(Main.class.getResourceAsStream("/dk/easv/belman/Images/ordersDef.png"));

    private final ObservableList<HBox> users = FXCollections.observableArrayList();
    private final BLLManager bllManager = new BLLManager();

    @FXML
    private void initialize() {
        ordersRoot = scrollP.getContent();
        try {
            newUserView = FXMLLoader.load(Main.class.getResource("FXML/newUser.fxml"));
        } catch (IOException ex) { ex.printStackTrace(); }

        sideBtnNotSelected.setOnMouseEntered(e -> usersImage.setImage(userSel));
        sideBtnNotSelected.setOnMouseExited(e -> usersImage.setImage(userDefault));

        scrollP.setContent(ordersRoot);
        // contentPane.getChildren().add(createCard("0123456789", new Image(Main.class.getResourceAsStream("Images/belman.png"))));
        // contentPane.getChildren().add(createCard("0123456789", new Image(Main.class.getResourceAsStream("Images/belman.png"))));
        // contentPane.getChildren().add(createCard("0123456789", new Image(Main.class.getResourceAsStream("Images/belman.png"))));

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
    private void newUserTab() {
        scrollP.setContent(newUserView);
        currentP.setText("Create user");
        newUser.setVisible(false);
        newUser.setDisable(true);
    }

    private void initUsers() {
        users.clear();
        contentPane.getChildren().clear();
        for (User u : bllManager.getAllUsers()) users.add(createUserCard(u));
    }

    private void loadUsers() {
        contentPane.getChildren().setAll(users);
    }

    @FXML
    private void applySearch() {}

    private VBox createCard(String orderNumber, Image img) {
        ImageView iv = new ImageView(img);
        iv.setFitWidth(100);
        iv.setFitHeight(100);
        Rectangle clip = new Rectangle(100, 100);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        iv.setClip(clip);
        Label lbl = new Label("Order: " + orderNumber);
        VBox card = new VBox(10, iv, lbl);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setId("orderCard");
        card.setPrefHeight(160);
        return card;
    }

    private void editUser(User u) {
        newUserTab();
    }

    private HBox createUserCard(User u) {
        Label name = new Label(u.getFullName());
        name.setId("cardTitle");
        Label role = new Label("Role: " + u.getRole());
        role.setId("cardText");
        VBox details = new VBox(5, name, role);
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
        card.setId("usersCard");
        card.setUserData(u);
        return card;
    }
}
