package dk.easv.belman.pl;

import dk.easv.belman.pl.model.UserModel;
import dk.easv.belman.be.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Toggle;

import java.awt.*;

public class UserController {
    @FXML private TextField    txtFullName;
    @FXML private TextField    txtUsername;
    @FXML private TextField    txtTagId;
    @FXML private CheckBox     cbTagId;
    @FXML private RadioButton  chkAdmin;
    @FXML private RadioButton  chkQualityControl;
    @FXML private RadioButton  chkOperator;
    @FXML private ToggleGroup  tgRole;
    @FXML private Button       btnSave;
    @FXML private Button       btnCancel;
    @FXML private VBox         rootVBox;
    @FXML private Label        lblError;

    private VBox      rightBox;
    private UserModel model;
    private AdminController adminController;


    @FXML
    private void initialize() {
        model = new UserModel();

        txtFullName.textProperty().bindBidirectional(model.fullNameProperty());
        txtUsername.textProperty().bindBidirectional(model.usernameProperty());
        txtTagId   .textProperty().bindBidirectional(model.tagIdProperty());

        chkAdmin       .setUserData(1);
        chkQualityControl.setUserData(2);
        chkOperator    .setUserData(3);
        lblError.textProperty().bind(model.errorMessageProperty());
        lblError.visibleProperty().bind(model.errorMessageProperty().isNotEmpty());
        tgRole.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) {
                model.roleIdProperty().set((int)newT.getUserData());
            } else {
                model.roleIdProperty().set(0);
            }
        });
        model.roleIdProperty().addListener((obs, old, val) -> {
            for (Toggle t : tgRole.getToggles()) {
                if (Integer.valueOf((int)t.getUserData()).equals(val.intValue())) {
                    tgRole.selectToggle(t);
                    break;
                }
            }
        });

        /**model.errorMessageProperty().addListener((obs, o, msg) -> {
            if (msg != null && !msg.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
            }
        });*/
        model.successMessageProperty().addListener((obs, o, msg) -> {
            if (msg != null && !msg.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
            }
        });
    }

    public void setAdminController(AdminController adminController) {
        this.adminController = adminController;
    }

    public void getRightBox(VBox rightBox) {
        this.rightBox = rightBox;
    }


    @FXML
    private void btnSaveClick() {
        txtTagId.setText(cbTagId.isSelected() ? "true" : "false");
        model.saveUser();

        String success = model.successMessageProperty().get();
        if (success != null && !success.isEmpty()) {
            adminController.reloadUsers();
            cancel();
        }
    }


    public void setEditingUser(User u) {
        cbTagId.setSelected(u.getTagId() != null);
        u.setTagId(cbTagId.isSelected() ? "true" : "false");
        txtTagId.setText(u.getTagId());
        model.setEditingUser(u);
        txtUsername.setDisable(true);
    }


    @FXML
    private void cancel() {
        model.clear();
        BorderPane bp = (BorderPane) rootVBox.getParent();
        bp.setCenter(rightBox);

        adminController.resizeWindow(rightBox);
    }
}
