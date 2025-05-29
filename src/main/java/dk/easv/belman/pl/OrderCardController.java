package dk.easv.belman.pl;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Objects;

public class OrderCardController {

    @FXML
    private ImageView orderImage;

    @FXML
    private Label lblOrderNumber;

    @FXML
    private Label lblStatus;

    private Order order;

    private final String[] states = {"Images Needed", "Pending", "Signed ✅"};

    private final String placeholderUrl = Objects.requireNonNull(getClass()
                    .getResource("/dk/easv/belman/Images/belman.png"))
            .toExternalForm();

    public void setOrder(Order order) {
        List<Photo> photos = order.getPhotos();
        Image img;
        String status;
        this.order = order;

        if (photos.isEmpty() || photos.getFirst().getPhotoFile() == null) {
            img = new Image(placeholderUrl);
            status = states[0];
        } else {
            Photo p = photos.getFirst();
            img = new Image(new ByteArrayInputStream(p.getPhotoFile()));
            status = order.getIsSigned() ? states[2] : states[1];
        }

        orderImage.setImage(img);
        orderImage.setFitWidth(100);
        orderImage.setFitHeight(100);
        orderImage.setPreserveRatio(false);

        Rectangle clip = new Rectangle(100, 100);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        orderImage.setClip(clip);

        lblStatus.setText("Status: " + status);
        lblOrderNumber.setText("Order: " + order.getOrderNumber());
    }

    public Order getOrder() {
        return order;
    }
}
