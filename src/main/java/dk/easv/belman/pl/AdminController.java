package dk.easv.belman.pl;

import dk.easv.belman.Main;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.User;
import dk.easv.belman.pl.model.AdminModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AdminController extends AbstractOrderController{
    @FXML private FlowPane  contentPane;
    @FXML private Label     currentP;
    @FXML private Button    newUser;
    @FXML private Button    sideBtnSelected;
    @FXML private Button    sideBtnNotSelected;
    @FXML private ImageView usersImage;
    @FXML private ImageView ordersImage;
    @FXML private TextField search;

    private VBox newUserWindow;
    private UserController userController;

    private final String placeholderUrl =
            Objects.requireNonNull(getClass().getResource("/dk/easv/belman/Images/belman.png"))
                    .toExternalForm();

    private final Image userSel     =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream("/dk/easv/belman/Images/user.png")));
    private final Image userDefault =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream("/dk/easv/belman/Images/userDef.png")));
    private final Image ordersSel   =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream("/dk/easv/belman/Images/orders.png")));
    private final Image ordersDefault =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream("/dk/easv/belman/Images/ordersDef.png")));

    private final String[] states = {"Images Needed", "Pending", "Signed ✅"};
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @FXML private Label uploadedByText;
    @FXML private Label uploadedAtText;
    @FXML private Label uploadedByText1;
    @FXML private Label uploadedAtText1;
    @FXML private Label uploadedByText2;
    @FXML private Label uploadedAtText2;
    @FXML private Label uploadedByText3;
    @FXML private Label uploadedAtText3;
    @FXML private Label uploadedByText4;
    @FXML private Label uploadedAtText4;
    @FXML private Label uploadedByText5;
    @FXML private Label uploadedAtText5;

    private final DateTimeFormatter dtf =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AdminModel model = new AdminModel();

    public void reloadUsers() {
        model.loadUsers();
        refreshContent();
    }

    @FXML
    private void initialize() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("FXML/newUser.fxml"));
        newUserWindow   = loader.load();
        userController  = loader.getController();
        userController.setAdminController(this);

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
        resizeWindow(newUserWindow);
    }

    private void updateTabStyles() {
        boolean showOrders = model.showingOrdersProperty().get();

        if (showOrders) {
            sideBtnNotSelected.setId("sideBtnNotSelected");
            sideBtnSelected.setId("sideBtnSelected");
            usersImage.setImage(userDefault);
            ordersImage.setImage(ordersSel);
            newUser.setVisible(false);
            newUser.setDisable(true);

            sideBtnNotSelected.setOnMouseEntered(e -> usersImage.setImage(userSel));
            sideBtnNotSelected.setOnMouseExited(e -> usersImage.setImage(userDefault));
            sideBtnSelected.setOnMouseEntered(e -> {});
            sideBtnSelected.setOnMouseExited(e -> {});

        } else {
            sideBtnNotSelected.setId("sideBtnSelected");
            sideBtnSelected  .setId("sideBtnNotSelected");
            usersImage.setImage(userSel);
            ordersImage.setImage(ordersDefault);
            newUser.setVisible(true);
            newUser.setDisable(false);

            sideBtnNotSelected.setOnMouseEntered(e -> {});
            sideBtnNotSelected.setOnMouseExited(e -> {});
            sideBtnSelected.setOnMouseEntered(e -> ordersImage.setImage(ordersSel));
            sideBtnSelected.setOnMouseExited(e -> ordersImage.setImage(ordersDefault));
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

        if (!model.showingOrdersProperty().get()) {
            for (User user : model.getFilteredUsers()) {
                HBox card = loadUserCard(user);
                contentPane.getChildren().add(card);
            }
        }
        else {
            for (Order order : model.getFilteredOrders()) {
                VBox card = createOrderCard(order);
                contentPane.getChildren().add(card);
            }
        }
    }



    private VBox createOrderCard(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/belman/FXML/OrderCard.fxml"));
            VBox card = loader.load();
            OrderCardController controller = loader.getController();
            controller.setOrder(order);
            card.setOnMouseClicked(_ -> openOrderDetail("FXML/orderAdmin.fxml", order.getOrderNumber(), Boolean.FALSE));
            return card;
        } catch (IOException e) {
            e.printStackTrace();
            return new VBox(); // fallback
        }
    }

    private HBox loadUserCard(User u) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("FXML/userCard.fxml"));
            HBox card = loader.load();

            UserCardController controller = loader.getController();

            controller.setData(u, model, this, this::refreshContent);

            return card;
        } catch (IOException e) {
            logger.error("Failed to load user card", e);
            return new HBox();
        }
    }

    public void editUser(User u) {
        newUserTab();
        userController.setEditingUser(u);
    }


    @Override
    protected List<Photo> getPhotosForOrder(String orderNumber) {
        return model.getPhotosForOrder(orderNumber);
    }

    @Override
    protected void onUserLogout() {
        model.logout();
    }

    @Override
    protected void onDetailLoaded(String orderNumber) {
        for (Label lbl : List.of(
                uploadedByText, uploadedAtText,
                uploadedByText1, uploadedAtText1,
                uploadedByText2, uploadedAtText2,
                uploadedByText3, uploadedAtText3,
                uploadedByText4, uploadedAtText4,
                uploadedByText5, uploadedAtText5
        )) {
            lbl.setText("");
        }

        List<Photo> photos = model.getPhotosForOrder(orderNumber);
        for (Photo p : photos) {
            String angle = p.getAngle().toUpperCase();
            UUID id  = p.getUploadedBy();
            User user = model.getUserById(id);
            String fullName = user.getFullName();
            String when  = dtf.format(p.getUploadedAt());

            switch (angle) {
                case "LEFT"        -> {
                    uploadedByText.setText(fullName);
                    uploadedAtText.setText(when);
                }
                case "TOP"         -> {
                    uploadedByText1.setText(fullName);
                    uploadedAtText1.setText(when);
                }
                case "RIGHT"       -> {
                    uploadedByText2.setText(fullName);
                    uploadedAtText2.setText(when);
                }
                case "BACK"        -> {
                    uploadedByText3.setText(fullName);
                    uploadedAtText3.setText(when);
                }
                case "FULL", "FRONT" -> {
                    uploadedByText4.setText(fullName);
                    uploadedAtText4.setText(when);
                }
                case "ADDITIONAL"  -> {
                    uploadedByText5.setText(fullName);
                    uploadedAtText5.setText(when);
                }
                default           -> {
                    uploadedByText.setText("Unknown angle: " + angle);
                    uploadedAtText.setText("");
                }
            }
        }
    }
}
