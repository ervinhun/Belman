package dk.easv.belman.pl.model;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import dk.easv.belman.exceptions.BelmanException;
import dk.easv.belman.pl.OperatorController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OperatorModel {
    private final BLLManager bllManager = new BLLManager();
    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    private OperatorController operatorController;

    private final ObjectProperty<User> loggedInUser = new SimpleObjectProperty<>();

    public OperatorModel(OperatorController operatorController) {
        this.operatorController = operatorController;
        loadOrders();
    }

    public void loadOrders() {
        List<Order> all = bllManager.getOrders(null);
        orders.setAll(all);
    }

    public ObservableList<Order> getOrders() {
        return orders;
    }

    public void setLoggedInUser(User u) {
        loggedInUser.set(u);
    }

    public void logout() {
        bllManager.logout(loggedInUser.get());
        loggedInUser.set(null);
    }

    /*public void savePhotos(List<ImageView> imageViews, List<String> angles, String orderNumber)
    {
        List<Photo> photos = new ArrayList<>();
        User user = operatorController.getUser();

        for(int i = 0; i < imageViews.size(); i++)
        {
            if(imageViews.get(i).getImage() != null)
            {
                Image image = imageViews.get(i).getImage();

                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                String imagePath = "src/main/resources/dk/easv/belman/SavedImages/" + orderNumber + "/" + angles.get(i) + ".png";

                try {
                    File file = new File(imagePath);
                    if (!file.exists())
                    {
                        if (!file.mkdirs()) {
                            throw new BelmanException("Failed to create folder");
                        }
                    }
                    ImageIO.write(bufferedImage, "png", file);
                } catch (IOException e) {
                    throw new BelmanException("Failed to save image: " + e.getMessage());
                }

                photos.add(new Photo(null, user.getId(), imagePath, angles.get(i), LocalDateTime.now(), false));
            }
        }

        bllManager.savePhotos(photos, orderNumber);
    }*/

    public void savePhotos(List<ImageView> imageViews,
                           List<String> angles,
                           String orderNumber) throws IOException {
        List<Photo> photos = new ArrayList<>();
        UUID userId = operatorController.getUser().getId();

        for (int i = 0; i < imageViews.size(); i++) {
            javafx.scene.image.Image fxImg = imageViews.get(i).getImage();
            if (fxImg == null) continue;

            // turn FX Image → raw PNG bytes
            BufferedImage buf = SwingFXUtils.fromFXImage(fxImg, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(buf, "png", baos);
            byte[] data = baos.toByteArray();

            photos.add(new Photo(
                    null, userId, angles.get(i), LocalDateTime.now(), false, data
            ));
        }

        bllManager.savePhotosBinary(photos, orderNumber);
    }

}
