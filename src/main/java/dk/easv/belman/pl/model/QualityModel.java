package dk.easv.belman.pl.model;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import dk.easv.belman.exceptions.BelmanException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;
import java.util.UUID;

public class QualityModel {
    private static final int MIN_PHOTOS_FOR_SIGNING = 5;
    private final BLLManager      bllManager   = new BLLManager();
    private final ObservableList<Order> orders    = FXCollections.observableArrayList();
    private final FilteredList<Order>   filtered   = new FilteredList<>(orders, _ -> true);
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
            filtered.setPredicate(_ -> true);
        } else {
            filtered.setPredicate(o ->
                    o.getOrderNumber().toLowerCase().contains(q)
            );
        }
    }

    public void setSearchQuery(String q) {
        searchQuery.set(q);
    }

    public FilteredList<Order> getFilteredOrders() {
        return filtered;
    }

    public void setLoggedInUser(User u) {
        loggedInUser.set(u);
    }

    public void logout() {
        bllManager.logout(loggedInUser.get());
        loggedInUser.set(null);
    }

    public ObjectProperty<User> loggedInUserProperty() {
        return loggedInUser;
    }

    public boolean signOrder(String orderNumber, boolean isSendingEmail, String email, User whoSignsIt) {
        try {
            return bllManager.signOrder(orderNumber, whoSignsIt.getId(), isSendingEmail, email);
        } catch (Exception e) {
            throw new BelmanException("Error in Model while signing order" + e);
        }
    }

    public boolean isDocumentExists(String orderNumber) {
        return bllManager.isDocumentExists(orderNumber);
    }

    public List<Photo> getPhotosForOrder(String orderNumber) {
        return bllManager.getPhotosForOrder(orderNumber);
    }

    public void sendBackToOperator(String orderNumber, UUID userId, String angle) {
        bllManager.sendBackToOperator(orderNumber, userId, angle);
    }

    public boolean isOrderReadyForSigning(String orderNumberToSign) {
        return bllManager.getPhotosNumbersforOrder(orderNumberToSign) >= MIN_PHOTOS_FOR_SIGNING;
    }
}
