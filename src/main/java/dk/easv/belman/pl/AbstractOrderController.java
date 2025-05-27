package dk.easv.belman.pl;

import dk.easv.belman.Main;
import dk.easv.belman.be.Photo;
import dk.easv.belman.exceptions.BelmanException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class AbstractOrderController extends BaseController {
    @FXML protected BorderPane borderPane;
    @FXML protected VBox       rightBox;
    @FXML protected Label      orderLabel;
    @FXML protected ImageView topImage;
    @FXML protected ImageView leftImage;
    @FXML protected ImageView rightImage;
    @FXML protected ImageView frontImage;
    @FXML protected ImageView backImage;
    @FXML protected ImageView additionalImage;

    private final String   placeholderUrl =
            getClass().getResource("/dk/easv/belman/Images/belman.png")
                    .toExternalForm();

    protected void openOrderDetail(String fxmlPath, String orderNumber, Boolean isOperator) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
            loader.setController(this);
            Parent root = loader.load();

            orderLabel.setText(orderNumber);
            borderPane.setCenter(root);
            resizeWindow(root);
            rebindUserChoiceBox(root);

            if(!isOperator)
            {
                loadOrderImages(orderNumber);
            }
            onDetailLoaded(orderNumber);

        } catch (IOException ex) {
            throw new BelmanException("Failed to load FXML: " + fxmlPath + " " + ex);
        }
    }

    protected void resizeWindow(Node root)
    {
        Stage stage = (Stage) borderPane.getScene().getWindow();
        double actualWidth = stage.getWidth() - borderPane.getLeft().getBoundsInParent().getWidth();
        double prefWidth = root.prefWidth(-1);

        double actualHeight = stage.getHeight();
        double prefHeight = root.prefHeight(-1);

        if (actualWidth < prefWidth || actualHeight < prefHeight) {
            stage.sizeToScene();
        }
    }

    private void loadOrderImages(String orderNumber) {
        Image placeholder = new Image(placeholderUrl);

        for (ImageView iv : new ImageView[]{
                topImage, leftImage, rightImage,
                frontImage, backImage, additionalImage
        }) {
            iv.setImage(placeholder);
            iv.setUserData(null);
        }

        List<Photo> photos = getPhotosForOrder(orderNumber);
        for (Photo p : photos) {
            Image img;
            byte[] data = p.getPhotoFile();
            if (data != null && data.length > 0) {
                img = new Image(new ByteArrayInputStream(data));
            } else {
                img = placeholder;
            }
            switch (p.getAngle().toUpperCase()) {
                case "TOP"        -> assign(topImage, p, img);
                case "LEFT"       -> assign(leftImage, p, img);
                case "RIGHT"      -> assign(rightImage, p, img);
                case "FRONT"       -> assign(frontImage, p, img);
                case "BACK"     -> assign(backImage, p, img);
                case "ADDITIONAL" -> assign(additionalImage, p, img);
                default           -> throw new BelmanException("Unknown angle: " + p.getAngle());
            }
        }
    }

    protected void bindImages(float multiplier)
    {
        for (ImageView iv : new ImageView[]{
                topImage, rightImage, leftImage,
                frontImage, backImage, additionalImage
        }) {
            VBox cellSize = (VBox) iv.getParent();
            iv.fitWidthProperty().bind(cellSize.widthProperty());
            iv.fitHeightProperty().bind(cellSize.heightProperty().multiply(multiplier));
        }
    }

    private void assign(ImageView iv,Photo p, Image img) {
        iv.setImage(img);
        iv.setUserData(p);
    }

    @FXML
    protected void cancel() {
        borderPane.setCenter(rightBox);
        resizeWindow(rightBox);
        rebindUserChoiceBox(rightBox);
    }

    protected abstract List<Photo> getPhotosForOrder(String orderNumber);

    protected abstract void onDetailLoaded(String orderNumber);
}
