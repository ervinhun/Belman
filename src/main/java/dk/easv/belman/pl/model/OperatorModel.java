package dk.easv.belman.pl.model;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OperatorModel {
    private final BLLManager bllManager = new BLLManager();
    private final ObservableList<Order> orders = FXCollections.observableArrayList();

    private final ObjectProperty<User> loggedInUser = new SimpleObjectProperty<>();

    public OperatorModel() {
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

    public void savePhotos(List<ImageView> imageViews, List<String> angles, String orderNumber)
    {
        List<Photo> photos = new ArrayList<>();
        User user = loggedInUser.get();

        for(int i = 0; i < imageViews.size(); i++)
        {
            if(imageViews.get(i).getImage() != null)
            {
                Image image = imageViews.get(i).getImage();

                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                String imagePath = "src/main/resources/dk/easv/belman/SavedImages/" + orderNumber + "/" + angles.get(i) + ".png";

                try {
                    File file = new File(imagePath);
                    ImageIO.write(bufferedImage, "png", file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                photos.add(new Photo(null, user.getId(), imagePath, angles.get(i), LocalDateTime.now(), false));
            }
        }

        bllManager.savePhotos(photos, orderNumber);
    }

}
