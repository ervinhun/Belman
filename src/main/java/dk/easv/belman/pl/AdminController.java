package dk.easv.belman.pl;

import dk.easv.belman.Main;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.QualityDocument;
import dk.easv.belman.be.User;
import dk.easv.belman.pl.model.AdminModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class AdminController extends AbstractOrderController{
    @FXML private FlowPane  contentPane;
    @FXML private Label     currentP;
    @FXML private Button    newUser;
    @FXML private Button    sideBtnSelected;
    @FXML private Button    sideBtnNotSelected;
    @FXML private ImageView usersImage;
    @FXML private ImageView ordersImage;
    @FXML private TextField search;

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
    @FXML private Label signedByText;
    @FXML private Label signedAtText;

    private VBox newUserWindow;
    private UserController userController;

    private final Image userSel     =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream("/dk/easv/belman/Images/user.png")));
    private final Image userDefault =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream("/dk/easv/belman/Images/userDef.png")));
    private final Image ordersSel   =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream("/dk/easv/belman/Images/orders.png")));
    private final Image ordersDefault =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream("/dk/easv/belman/Images/ordersDef.png")));

    private final DateTimeFormatter dtf =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Logger logger = Logger.getLogger(AdminController.class.getName());

    private final AdminModel model = new AdminModel();

    @FXML
    private void initialize() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("FXML/newUser.fxml"));
        newUserWindow   = loader.load();
        userController  = loader.getController();
        userController.setAdminController(this);

        currentP.textProperty().bind(model.currentPageProperty());

        model.showingOrdersProperty().addListener((_, _, _) -> updateTabStyles());

        search.textProperty().addListener((_, _, newText) -> {
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

    public void reloadUsers() {
        model.loadUsers();
        refreshContent();
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

            sideBtnNotSelected.setOnMouseEntered(_ -> usersImage.setImage(userSel));
            sideBtnNotSelected.setOnMouseExited(_ -> usersImage.setImage(userDefault));
            sideBtnSelected.setOnMouseEntered(_ -> {});
            sideBtnSelected.setOnMouseExited(_ -> {});

        } else {
            sideBtnNotSelected.setId("sideBtnSelected");
            sideBtnSelected  .setId("sideBtnNotSelected");
            usersImage.setImage(userSel);
            ordersImage.setImage(ordersDefault);
            newUser.setVisible(true);
            newUser.setDisable(false);

            sideBtnNotSelected.setOnMouseEntered(_ -> {});
            sideBtnNotSelected.setOnMouseExited(_ -> {});
            sideBtnSelected.setOnMouseEntered(_ -> ordersImage.setImage(ordersSel));
            sideBtnSelected.setOnMouseExited(_ -> ordersImage.setImage(ordersDefault));
        }

        refreshContent();
    }


    @FXML
    private void applySearch() {
        model.searchQueryProperty().set(search.getText());
        model.applySearch();
        refreshContent();
    }

    private void refreshContent() {
        contentPane.getChildren().clear();

        if (!model.showingOrdersProperty().get()) {
            for (User user : model.getFilteredUsers()) {
                HBox card = createUserCard(user);
                if(card != null)
                {
                    contentPane.getChildren().add(card);
                }
            }
        }
        else {
            for (Order order : model.getFilteredOrders()) {
                VBox card = createOrderCard(order);
                if(card != null)
                {
                    contentPane.getChildren().add(card);
                }
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
            logger.warning("Failed to load order card: "+ e);
            return null;
        }
    }

    private HBox createUserCard(User u) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("FXML/userCard.fxml"));
            HBox card = loader.load();

            UserCardController controller = loader.getController();

            controller.setData(u, model, this, this::refreshContent);

            return card;
        } catch (IOException e) {
            logger.warning("Failed to load user card: "+ e);
            return null;
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
        bindImages(0.6f);
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
            String fullName = user == null ? "Unknown" : user.getFullName();
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
        QualityDocument pDoc = model.getQualityDocumentForAdmin(orderNumber);
        if (pDoc != null && pDoc.getGeneratedAt() != null) {
            signedAtText.setText(signedAtText.getText() + " " + dtf.format(pDoc.getGeneratedAt()));
            signedByText.setText(signedByText.getText() + " " + pDoc.getGeneratedByName());
        } else {
            signedAtText.setText(signedAtText.getText() + " Not signed");
            signedByText.setText(signedByText.getText() + " Not signed");
        }
    }
}
