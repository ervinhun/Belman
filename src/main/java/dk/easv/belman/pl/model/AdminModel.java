package dk.easv.belman.pl.model;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;
import java.util.UUID;

public class AdminModel {
    private final BLLManager bllManager = new BLLManager();

    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    private final ObservableList<User>  users  = FXCollections.observableArrayList();

    private final FilteredList<Order> filteredOrders = new FilteredList<>(orders, _ -> true);
    private final FilteredList<User>  filteredUsers  = new FilteredList<>(users, _ -> true);

    private final BooleanProperty showingOrders = new SimpleBooleanProperty(true);
    private final StringProperty  currentPage    = new SimpleStringProperty("Orders");
    private final StringProperty  searchQuery    = new SimpleStringProperty("");

    private final ObjectProperty<User> loggedInUser = new SimpleObjectProperty<>();

    public AdminModel() {
        loadOrders();
    }

    public void loadOrders() {
        List<Order> all = bllManager.getOrders(null);
        orders.setAll(all);
        showingOrders.set(true);
        currentPage.set("Orders");
        searchQuery.set("");
        applySearch();
    }

    public void loadUsers() {
        List<User> all = bllManager.getAllUsers();
        users.setAll(all);
        showingOrders.set(false);
        currentPage.set("Users");
        searchQuery.set("");
        applySearch();
    }

    public void applySearch() {
        String q = searchQuery.get().trim().toLowerCase();
        if (q.isEmpty()) {
            filteredOrders.setPredicate(_ -> true);
            filteredUsers.setPredicate(_ -> true);
        } else {
            filteredOrders.setPredicate(o ->
                    o.getOrderNumber().toLowerCase().contains(q)
            );
            filteredUsers.setPredicate(u ->
                    u.getUsername().toLowerCase().contains(q) ||
                            u.getFullName().toLowerCase().contains(q)
            );
        }
    }

    public void deleteUser(User u) {
        bllManager.deleteUser(u.getId());
        users.remove(u);
    }

    public void setLoggedInUser(User u) {
        loggedInUser.set(u);
    }

    public void logout() {
        bllManager.logout(loggedInUser.get());
        loggedInUser.set(null);
    }

    public FilteredList<Order> getFilteredOrders()   { return filteredOrders;   }
    public FilteredList<User>  getFilteredUsers()    { return filteredUsers;    }
    public BooleanProperty     showingOrdersProperty() { return showingOrders; }
    public StringProperty      currentPageProperty()   { return currentPage;   }
    public StringProperty      searchQueryProperty()   { return searchQuery;   }
    public ObjectProperty<User> loggedInUserProperty() { return loggedInUser;  }

    public List<Photo> getPhotosForOrder(String orderNumber) {
        return bllManager.getPhotosForOrder(orderNumber);
    }

    public User getUserById(UUID id) {
        return bllManager.getUserById(id);
    }
}
