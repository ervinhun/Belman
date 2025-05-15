package dk.easv.belman.pl;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.User;
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

        List<Photo> photos = model.getPhotosForOrder(orderNumberToSign);
        List<File> files = photos.stream()
                .map(p -> new File(p.getImagePath()))
                .filter(File::exists)
                .toList();

        if (files.isEmpty()) return;

        File selected = (File) selectedImageView.getUserData();
        int selectedIndex = files.indexOf(selected);
        if (selectedIndex == -1) selectedIndex = 0;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/belman/FXML/photoPreview.fxml"));
            Parent previewRoot = loader.load();
            PhotoPreviewController controller = loader.getController();
            controller.setPhotos(files, selectedIndex, this);

            borderPane.setCenter(previewRoot);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void deleteImage() { /* move logic to model and call from here */ }

    private void openFullImage(File file) {
        Image image = model.getFullImage(file);
        if (image == null) {
            return;
        }

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(800);

        VBox container = new VBox(imageView);
        container.setPadding(new Insets(10));

        Stage stage = new Stage();
        stage.setTitle("Image Preview");
        stage.setScene(new Scene(container));
        stage.show();
    }

    private void setPlaceholder(ImageView iv) {
        iv.setImage(new Image(placeholderUrl));
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

    private VBox createCard (Order order){
        ImageView iv = new ImageView();
        Label state = new Label();
        String statusPreText = "Status: ";

        if (order.getPhotos().isEmpty()) {
            iv.setImage(new Image(placeholderUrl));
            state.setText(statusPreText + states[0]);
        } else {
            String path = order.getPhotos().getFirst().getImagePath();
            File f = new File(path);
            iv.setImage(f.exists()
                    ? new Image(f.toURI().toString())
                    : new Image(placeholderUrl));
            state.setText(Boolean.TRUE.equals(order.getIsSigned())
                    ? statusPreText + states[2]
                    : statusPreText + states[1]);
        }

        iv.setFitWidth(100);
        iv.setFitHeight(100);
        Rectangle clip = new Rectangle(100, 100);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        iv.setClip(clip);

        Label lbl = new Label("Order: " + order.getOrderNumber());

        VBox card = new VBox(10, iv, lbl, state);
        card.setAlignment(Pos.CENTER);
        card.setId("orderCard");
        card.setPrefHeight(160);
        card.getProperties().put("orderNum", order.getOrderNumber());
        card.setOnMouseClicked(e ->
                openOrderDetail("FXML/orderQuality.fxml", order.getOrderNumber())
        );
        return card;
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
        this.orderNumberToSign = orderNumber;//
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
