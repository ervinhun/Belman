package dk.easv.belman.pl;

import dk.easv.belman.be.User;
import dk.easv.belman.pl.model.AdminModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;


public class UserCardController {
    @FXML private Label lblFullName;
    @FXML private Label lblRole;
    @FXML private Label lblLastLogin;
    @FXML private HBox userCard;

    private User user;
    private AdminModel model;
    private Runnable onRefresh;
    private AdminController adminController;

    @FXML
    private void onEdit() {
        adminController.editUser(user);
    }

    @FXML
    private void onDelete() {
        model.deleteUser(user);
        if (onRefresh != null) onRefresh.run();
    }

    public void setData(User user, AdminModel model, AdminController controller, Runnable onRefresh) {
        this.user = user;
        this.model = model;
        this.onRefresh = onRefresh;
        this.adminController = controller;

        lblFullName.setText(user.getFullName());
        lblRole.setText("Role: " + user.getRole());
        if (user.getLastLoginTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            lblLastLogin.setText("Last login: " + user.getLastLoginTime().format(formatter));
        } else {
            lblLastLogin.setText("Last login: never");
        }


        userCard.getProperties().put("username", user.getUsername());
    }
}
