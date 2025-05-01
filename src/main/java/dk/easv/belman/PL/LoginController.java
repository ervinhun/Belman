package dk.easv.belman.PL;

import dk.easv.belman.Main;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private Button confirm;

    @FXML
    private MFXPasswordField password;
    @FXML
    private MFXTextField username;

    private BLLManager bllManager;

    @FXML
    public void initialize() {
        try {
            bllManager = new BLLManager();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void login() {
        String uName = username.getText().trim();
        String pwd = password.getText().trim();

        if (uName.isEmpty() || pwd.isEmpty()) {
            System.out.println("Please fill in username and password!");
            return;
        }

        User foundUser = bllManager.login(uName, pwd);

        if (foundUser == null) {
            System.out.println("Invalid username or password!");
            return;
        }

        int roleID = foundUser.getRoleId();
        System.out.println("Login roleId:" + roleID);
        switch (roleID) {
            case 1:
                openAdminWindow(foundUser);
                break;
            case 2:
                openQualityControllerWindow(foundUser);
                break;
            case 3:
                openOperatorWindow(foundUser);
                break;
            default:
                System.out.println("Unknown roleID: " + roleID);
        }
    }

    private void openQualityControllerWindow(User foundUser) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/quality.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            QualityController qcController;
            qcController = fxmlLoader.getController();
            qcController.setLoggedinUser(foundUser);
            Stage stage = (Stage) username.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("Error opening Coordinator window!");
            e.printStackTrace();
        }
    }

    private void openAdminWindow(User foundUser) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/admin.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            AdminController aController;
            aController = fxmlLoader.getController();
            aController.setLoggedinUser(foundUser);
            Stage stage = (Stage) username.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("Error opening Admin window!");
            e.printStackTrace();
        }
    }

    private void openOperatorWindow(User foundUser) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/operator.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            OperatorController oController;
            oController = fxmlLoader.getController();
            oController.setLoggedinUser(foundUser);
            Stage stage = (Stage) username.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("Error opening Admin window!");
            e.printStackTrace();
        }
    }
}