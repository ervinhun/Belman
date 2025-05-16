package dk.easv.belman.pl;

import dk.easv.belman.be.Order;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.io.File;

public class OrderCardController {

    @FXML
    private ImageView orderImage;

    @FXML
    private Label lblOrderNumber;

    @FXML
    private Label lblStatus;

    private Order order;

    private final String[] states = {"Images Needed", "Pending", "Signed ✅"};

    private final String placeholderUrl = getClass()
            .getResource("/dk/easv/belman/Images/belman.png")
            .toExternalForm();

    public void setOrder(Order order) {
        this.order = order;

        if (order.getPhotos().isEmpty()) {
            orderImage.setImage(new Image(placeholderUrl));
            lblStatus.setText("Status: " + states[0]); // "Images Needed"
        } else {
            File imgFile = new File(order.getPhotos().getFirst().getImagePath());
            Image image = imgFile.exists()
                    ? new Image(imgFile.toURI().toString())
                    : new Image(placeholderUrl);
            orderImage.setImage(image);

            lblStatus.setText(order.getIsSigned()
                    ? "Status: " + states[2]
                    : "Status: " + states[1]
            );
        }

        orderImage.setFitWidth(100);
        orderImage.setFitHeight(100);
        orderImage.setPreserveRatio(false);

        Rectangle clip = new Rectangle(100, 100);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        orderImage.setClip(clip);

        lblOrderNumber.setText("Order: " + order.getOrderNumber());
    }

    public Order getOrder() {
        return order;
    }
}
