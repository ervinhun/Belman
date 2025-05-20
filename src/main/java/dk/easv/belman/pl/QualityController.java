package dk.easv.belman.pl;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.User;
import dk.easv.belman.exceptions.BelmanException;
import dk.easv.belman.pl.model.QualityModel;
import dk.easv.belman.dal.FilePaths;
import dk.easv.belman.dal.OpenFile;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class QualityController extends AbstractOrderController {
    @FXML
    private FlowPane ordersPane;
    @FXML
    private TextField search;
    @FXML
    Button btnSign;
    @FXML
    private TextField txtemail;
    @FXML
    private CheckBox cbSendingEmail;

    private ImageView selectedImageView;

    private final String[] states = {"Images Needed", "Pending", "Signed ✅"};
    private final String placeholderUrl =
            getClass().getResource("/dk/easv/belman/Images/belman.png")
                    .toExternalForm();

    private QualityModel model;
    private String orderNumberToSign;
    private static final String REPORT_PDF = "/report.pdf";
    private static final String OPEN_DOCUMENT = "Open\nDocument";
    private User loggedInUserQc;
    private VBox openedOrder;

    @FXML
    private void initialize() {
        if (model == null)
            model = new QualityModel();
        refreshContent();
        this.loggedInUserQc = getLoggedInUserFromBaseController();

        search.textProperty().addListener((obs, old, txt) -> {
            model.setSearchQuery(txt);
            model.applySearch();
            refreshContent();
        });

        setupImageClickHandlers();
    }

    private void setupImageClickHandlers() {
        ImageView[] views = {topImage, leftImage, rightImage, frontImage, backImage, additionalImage};

        for (ImageView iv : views) {
            if (iv == null) continue;

            iv.getStyleClass().removeAll("clickable-image", "image-selected");
            iv.getStyleClass().add("clickable-image");

            iv.setOnMouseClicked(e -> {
                if (iv.equals(selectedImageView)) {
                    iv.getStyleClass().remove("image-selected");
                    selectedImageView = null;
                } else {
                    clearImageSelectionBorders();
                    selectedImageView = iv;
                    iv.getStyleClass().add("image-selected");
                }
            });
        }
    }

    private void clearImageSelectionBorders() {
        ImageView[] views = {topImage, leftImage, rightImage, frontImage, backImage, additionalImage};
        for (ImageView iv : views) {
            if (iv != null)
                iv.getStyleClass().remove("image-selected");
        }
    }

    private void refreshContent() {
        ordersPane.getChildren().clear();
        for (Order o : model.getFilteredOrders()) {
            ordersPane.getChildren().add(createCard(o));
        }
    }

    @Override
    @FXML
    public void cancel() {
        borderPane.setCenter(rightBox);
    }


    @FXML
    private void openImage() {
        if (selectedImageView == null) return;
        openedOrder = (VBox) borderPane.getCenter();
        List<ImageView> views = List.of(topImage, leftImage, rightImage, frontImage, backImage, additionalImage);
        String[] angles = {"Top", "Left", "Right", "Front", "Back", "Additional"};

        List<Photo> photos = model.getPhotosForOrder(orderNumberToSign);
        if (photos.isEmpty()) return;

        String angle = angles[views.indexOf(selectedImageView)];
        int idx = -1;
        for(int i = 0; i < photos.size(); i++)
        {
            if(angle.equals(photos.get(i).getAngle()))
            {
                idx = i;
            }
        }
        if (idx < 0) idx = 0;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/belman/FXML/photoPreview.fxml")
            );
            Parent root = loader.load();

            PhotoPreviewController ctrl = loader.getController();
            ctrl.setPhotos(photos, idx, this);

            borderPane.setCenter(root);
        } catch (IOException e) {
            throw new BelmanException("Failed to load photoPreview.fxml " + e);
        }
    }

    @FXML
    private void sendBackToOperator() {
        model.sendBackToOperator(orderNumberToSign);
        cancel();
        refreshContent();
    }

    @FXML
    private void deleteImage() { /* move logic to model and call from here */ }

    private void setPlaceholder(ImageView iv) {
        iv.setImage(new Image(placeholderUrl));
    }

    public void returnToOrder()
    {
        borderPane.setCenter(openedOrder);
    }


    @FXML
    private void signOrder() {

        // Check if order number is valid
        if (orderNumberToSign == null || orderNumberToSign.isEmpty()) {

            return;
        }
        if (model.signOrder(orderNumberToSign, cbSendingEmail.isSelected(), txtemail.getText(), loggedInUserQc)) {
            btnSign.setText(OPEN_DOCUMENT);
            btnSign.setOnAction(e -> {
                if (cbSendingEmail.isSelected())
                    new OpenFile(FilePaths.REPORT_DIRECTORY + orderNumberToSign + REPORT_PDF, true, txtemail.getText());
                else
                    new OpenFile(FilePaths.REPORT_DIRECTORY + orderNumberToSign + REPORT_PDF);
            });

            cancel(); // This resets the view to the previous screen
        }
    }

    private VBox createCard(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/belman/FXML/OrderCard.fxml"));
            VBox card = loader.load();
            OrderCardController controller = loader.getController();
            controller.setOrder(order);

            card.setOnMouseClicked(_ -> openOrderDetail("FXML/orderQuality.fxml", order.getOrderNumber(), Boolean.FALSE));

            return card;
        } catch (IOException e) {
            e.printStackTrace();
            return new VBox();
        }
    }
    @FXML
    private void cbSendingEmailClicked() {
        txtemail.setVisible(!txtemail.isVisible());

        if (btnSign.getText().equals(OPEN_DOCUMENT)) {
            btnSign.setOnAction(e -> {
                if (cbSendingEmail.isSelected())
                    new OpenFile(FilePaths.REPORT_DIRECTORY + orderNumberToSign + REPORT_PDF, true, txtemail.getText());
                else
                    new OpenFile(FilePaths.REPORT_DIRECTORY + orderNumberToSign + REPORT_PDF);
            });
        }
        else {
            btnSign.setOnAction(e -> signOrder());
        }

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
        this.orderNumberToSign = orderNumber;
        if (model.isDocumentExists(orderNumber)) {
            btnSign.setText(OPEN_DOCUMENT);
            btnSign.setOnAction(e -> {
                if (cbSendingEmail.isSelected()) {
                    new OpenFile(FilePaths.REPORT_DIRECTORY + orderNumber + REPORT_PDF, true, txtemail.getText());
                } else {
                    new OpenFile(FilePaths.REPORT_DIRECTORY + orderNumber + REPORT_PDF);
                }
            });

        } else {
            btnSign.setText("Sign\nOrder");
            btnSign.setOnAction(e -> signOrder());
        }
    }
}
