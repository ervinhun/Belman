package dk.easv.belman.PL.model;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;

public class QualityModel {
    private final BLLManager      bllManager   = new BLLManager();
    private final ObservableList<Order> orders    = FXCollections.observableArrayList();
    private final FilteredList<Order>   filtered   = new FilteredList<>(orders, o -> true);
    private final StringProperty        searchQuery = new SimpleStringProperty("");
    private final ObjectProperty<User>  loggedInUser = new SimpleObjectProperty<>();

    public QualityModel() {
        loadOrders();
    }

    public void loadOrders() {
        List<Order> all = bllManager.getOrders(null);
        orders.setAll(all);
        applySearch();
    }

    public void applySearch() {
        String q = searchQuery.get().trim().toLowerCase();
        if (q.isEmpty()) {
            filtered.setPredicate(o -> true);
        } else {
            filtered.setPredicate(o ->
                    o.getOrderNumber().toLowerCase().contains(q)
            );
        }
    }

    public void openFullImage(File file) {
        if (file == null || !file.exists()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No image selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a valid image.");
            alert.showAndWait();
            return;
        }

        Image fullImage = new Image(file.toURI().toString());
        ImageView fullImageView = new ImageView(fullImage);
        fullImageView.setPreserveRatio(true);
        fullImageView.setFitWidth(800);

        VBox container = new VBox(fullImageView);
        container.setPadding(new javafx.geometry.Insets(10));

        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Image Preview");
        stage.setScene(new javafx.scene.Scene(container));
        stage.show();
    }

    public void setSearchQuery(String q) {
        searchQuery.set(q);
    }

    public FilteredList<Order> getFilteredOrders() {
        return filtered;
    }

    public void setLoggedInUser(User u) {
        loggedInUser.set(u);
        System.out.println("Logged in user: " + u.getUsername());
    }

    public void logout() {
        bllManager.logout(loggedInUser.get());
        loggedInUser.set(null);
    }

    public ObjectProperty<User> loggedInUserProperty() {
        return loggedInUser;
    }

    public boolean signOrder(String orderNumber) {
        return bllManager.signOrder(orderNumber, loggedInUser.get().getId());
    }

    public boolean isDocumentExists(String orderNumber) {
        return bllManager.isDocumentExists(orderNumber);
    }

    public List<Photo> getPhotosForOrder(String orderNumber) {
        return bllManager.getPhotosForOrder(orderNumber);
    }

}
