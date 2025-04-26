package dk.easv.belman.PL;

import dk.easv.belman.Main;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private Boolean isOrdersWin = true;
    private Image userSel = new Image(getClass().getResourceAsStream("/dk/easv/belman/Images/user.png"));
    private Image ordersSel = new Image(Main.class.getResourceAsStream("/dk/easv/belman/Images/orders.png"));
    private Image userDefault = new Image(Main.class.getResourceAsStream("/dk/easv/belman/Images/userDef.png"));
    private Image ordersDefault = new Image(Main.class.getResourceAsStream("/dk/easv/belman/Images/ordersDef.png"));

    @FXML
    private void initialize()
    {
        ordersPane.getChildren().add(addCardToFlowPane("0123456789", new Image(Main.class.getResourceAsStream("Images/belman.png"))));
        ordersPane.getChildren().add(addCardToFlowPane("0123456789", new Image(Main.class.getResourceAsStream("Images/belman.png"))));
        ordersPane.getChildren().add(addCardToFlowPane("0123456789", new Image(Main.class.getResourceAsStream("Images/belman.png"))));
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

    private VBox addCardToFlowPane(String orderNumber, Image image) {
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
}
