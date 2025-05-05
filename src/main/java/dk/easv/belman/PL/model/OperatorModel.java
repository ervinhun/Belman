package dk.easv.belman.PL.model;

import dk.easv.belman.be.Order;
import dk.easv.belman.bll.BLLManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class OperatorModel {
    private final BLLManager bllManager = new BLLManager();
    private final ObservableList<Order> orders = FXCollections.observableArrayList();

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
}
