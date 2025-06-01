package dk.easv.belman.pl;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.User;
import dk.easv.belman.exceptions.BelmanException;
import dk.easv.belman.pl.model.QualityModel;
import dk.easv.belman.bll.OpenSendPdf;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class QualityController extends AbstractOrderController {
    @FXML private FlowPane ordersPane;
    @FXML private TextField search;
    @FXML Button btnSign;
    @FXML private TextField txtemail;
    @FXML private CheckBox cbSendingEmail;
    @FXML private Button btnSendBack;
    @FXML private Button btnDeleteImage;

    private ImageView selectedImageView;

    private final String placeholderUrl =   Objects.requireNonNull(
                                            getClass().getResource("/dk/easv/belman/Images/belman.png"))
                                            .toExternalForm();

    private QualityModel model;
    private static final Logger logger = Logger.getLogger(QualityController.class.getName());

    private String orderNumberToSign;
    private static final String OPEN_DOCUMENT = "Open\nDocument";
    private static final String SIGN_ORDER = "Sign\nOrder";
    private User loggedInUserQc;
    private VBox openedOrder;
    private static final String IMAGE_SELECTED = "image-selected";

    @FXML
    private void initialize() {
        if (model == null)
            model = new QualityModel();
        refreshContent();
        this.loggedInUserQc = getLoggedInUserFromBaseController();

        search.textProperty().addListener((_, _, txt) -> {
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

            iv.getStyleClass().removeAll("clickable-image", IMAGE_SELECTED);
            iv.getStyleClass().add("clickable-image");

            iv.setOnMouseClicked(_ -> {
                if (iv.equals(selectedImageView)) {
                    iv.getStyleClass().remove(IMAGE_SELECTED);
                    selectedImageView = null;
                } else {
                    clearImageSelectionBorders();
                    selectedImageView = iv;
                    iv.getStyleClass().add(IMAGE_SELECTED);
                }
            });
        }
    }

    private void clearImageSelectionBorders() {
        ImageView[] views = {topImage, leftImage, rightImage, frontImage, backImage, additionalImage};
        for (ImageView iv : views) {
            if (iv != null)
                iv.getStyleClass().remove(IMAGE_SELECTED);
        }
    }

    private void refreshContent() {
        ordersPane.getChildren().clear();
        for (Order o : model.getFilteredOrders()) {
            VBox orderCard = createOrderCard(o);
            if(orderCard != null)
            {
                ordersPane.getChildren().add(orderCard);
            }
        }
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
        if (selectedImageView == null)
            return;
        List<ImageView> views = List.of(topImage, leftImage, rightImage, frontImage, backImage, additionalImage);
        String[] angles = {"Top", "Left", "Right", "Front", "Back", "Additional"};
        if (!views.contains(selectedImageView)) {
            return;
        }
        String angle = angles[views.indexOf(selectedImageView)];
            if (selectedImageView.getImage().getUrl() != null && selectedImageView.getImage().getUrl().equals(placeholderUrl)) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Selected image is empty. Please select a valid image.");
                alert.showAndWait();
                return;
            }
        selectedImageView.setImage(new Image(placeholderUrl));
        model.sendBackToOperator(orderNumberToSign, loggedInUserQc.getId(), angle);
        refreshContent();
    }

    @FXML
    private void deleteImage() { /* move logic to model and call from here */ }

    public void returnToOrder()
    {
        borderPane.setCenter(openedOrder);
    }


    @FXML
    private void signOrder() {
        if (orderNumberToSign == null || orderNumberToSign.isEmpty()) {

            return;
        }
        if (!model.isOrderReadyForSigning(orderNumberToSign)) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Order is not ready for signing. Please ensure all images are uploaded and the order is complete.");
            alert.showAndWait();
            return;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(() -> {
                boolean success = model.signOrder(orderNumberToSign, cbSendingEmail.isSelected(), txtemail.getText(), loggedInUserQc);
                Platform.runLater(() -> {
                    if (success) {
                        disableButtonsForImages();
                        cancel();
                        refreshContent();
                    } else {
                        logger.warning("Failed to sign order: " + orderNumberToSign);
                    }
                });
            });
        }
        catch (BelmanException e) {
            logger.warning("Error signing order: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to sign order: " + e.getMessage());
            alert.showAndWait();
        } finally {
            executor.shutdown();
        }
    }

    @FXML
    private void cbSendingEmailClicked() {
        txtemail.setVisible(!txtemail.isVisible());

        if (btnSign.getText().equals(OPEN_DOCUMENT)) {
            disableButtonsForImages();
        }
        else {
            btnSign.setOnAction(_ -> signOrder());
        }
    }

    private VBox createOrderCard(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/belman/FXML/OrderCard.fxml"));
            VBox card = loader.load();
            OrderCardController controller = loader.getController();
            controller.setOrder(order);

            card.setOnMouseClicked(_ -> openOrderDetail("FXML/orderQuality.fxml", order.getOrderNumber(), Boolean.FALSE));

            return card;
        } catch (IOException e) {
            logger.warning("Failed to create card: " + e);
            return null;
        }
    }

    private void disableButtonsForImages() {
        btnSign.setText(OPEN_DOCUMENT);
        btnSign.setOnAction(_ ->
                new OpenSendPdf(orderNumberToSign, cbSendingEmail.isSelected(), txtemail.getText())
        );
        btnSendBack.setDisable(true);
        btnDeleteImage.setDisable(true);
    }

    @Override
    @FXML
    public void cancel() {
        borderPane.setCenter(rightBox);
        rebindUserChoiceBox(rightBox);
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
        bindImages(0.9f);
        this.orderNumberToSign = orderNumber;
        if (model.isDocumentExists(orderNumber)) {
            disableButtonsForImages();
        } else {
            btnSign.setText(SIGN_ORDER);
            btnSign.setOnAction(_ -> signOrder());
        }
    }
}
