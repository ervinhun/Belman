package dk.easv.belman.PL;

import dk.easv.belman.Main;
import dk.easv.belman.be.Photo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class AbstractOrderController extends BaseController {
    @FXML protected BorderPane borderPane;
    @FXML protected VBox       rightBox;
    @FXML protected Label      orderLabel;
    @FXML protected ImageView  topImage, leftImage, rightImage,
            frontImage, backImage, additionalImage;

    private final String   placeholderUrl =
            getClass().getResource("/dk/easv/belman/Images/belman.png")
                    .toExternalForm();
    private final String[] states       = {"Images Needed","Pending","Signed ✅"};

    protected void openOrderDetail(String fxmlPath, String orderNumber) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
            loader.setController(this);
            Parent root = loader.load();

            orderLabel.setText(orderNumber);
            borderPane.setCenter(root);

            loadOrderImages(orderNumber);
            onDetailLoaded(orderNumber);

        } catch (IOException ex) {
            ex.printStackTrace();
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
            File f = new File(p.getImagePath());
            Image img = f.exists()
                    ? new Image(f.toURI().toString())
                    : placeholder;
            switch (p.getAngle().toUpperCase()) {
                case "TOP"        -> assign(topImage, f, img);
                case "LEFT"       -> assign(leftImage, f, img);
                case "RIGHT"      -> assign(rightImage, f, img);
                case "FRONT"       -> assign(frontImage, f, img);
                case "BACK"     -> assign(backImage, f, img);
                case "ADDITIONAL" -> assign(additionalImage, f, img);
            }
        }
    }
    private void assign(ImageView iv, File f, Image img) {
        iv.setImage(img);
        iv.setUserData(f);
    }

    @FXML
    protected void cancel() {
        borderPane.setCenter(rightBox);
    }

    protected abstract List<Photo> getPhotosForOrder(String orderNumber);

    protected abstract void onDetailLoaded(String orderNumber);
}
