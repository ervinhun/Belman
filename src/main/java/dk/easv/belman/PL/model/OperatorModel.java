package dk.easv.belman.PL.model;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
        System.out.println("Logged in user: " + u.getUsername());
    }

    public void logout() {
        bllManager.logout(loggedInUser.get());
        loggedInUser.set(null);
    }

}
