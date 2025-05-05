package dk.easv.belman.PL.model;

import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

public class LoginModel {
    private final BLLManager bllManager;
    private final ObjectProperty<User>  loggedInUser = new SimpleObjectProperty<>();
    private final StringProperty        errorMessage  = new SimpleStringProperty();

    public LoginModel() {
        bllManager = new BLLManager();
    }

    public void login(String username, String password) {
        errorMessage.set("");
        if (username.isEmpty() || password.isEmpty()) {
            errorMessage.set("Please fill in username and password!");
            return;
        }

        User found = bllManager.login(username, password);
        if (found == null) {
            errorMessage.set("Invalid username or password!");
        } else {
            loggedInUser.set(found);
        }
    }

    public ObjectProperty<User> loggedInUserProperty() {
        return loggedInUser;
    }

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }
}
