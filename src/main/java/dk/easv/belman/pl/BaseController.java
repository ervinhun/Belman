package dk.easv.belman.pl;

import dk.easv.belman.Main;
import dk.easv.belman.be.User;
import dk.easv.belman.dal.GenerateReport;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class BaseController {
    @FXML protected ChoiceBox<String> user;
    protected User loggedInUser;


    public void setLoggedinUser(User u) {
        user.getItems().setAll(
                u.getFullName(),
                "Logout"
        );
        user.getSelectionModel().selectFirst();
        user.setOnAction(evt -> {
            if ("Logout".equals(user.getValue())) {
                loggedOut();
            }
        });
        this.loggedInUser = u;
    }

    protected void loggedOut() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("FXML/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) user.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void onUserLogout();

    public User getLoggedInUserFromBaseController() {
        return this.loggedInUser;
    }
}
