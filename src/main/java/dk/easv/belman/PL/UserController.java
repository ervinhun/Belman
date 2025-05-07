package dk.easv.belman.PL;

import dk.easv.belman.be.User;
import dk.easv.belman.PL.model.UserModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Toggle;

public class UserController {
    @FXML private TextField    txtFullName;
    @FXML private TextField    txtUsername;
    @FXML private TextField    txtTagId;
    @FXML private RadioButton  chkAdmin;
    @FXML private RadioButton  chkQualityControl;
    @FXML private RadioButton  chkOperator;
    @FXML private ToggleGroup  tgRole;
    @FXML private Button       btnSave;
    @FXML private Button       btnCancel;
    @FXML private VBox         rootVBox;

    private VBox      rightBox;
    private UserModel model;


    @FXML
    private void initialize() {
        model = new UserModel();

        txtFullName.textProperty().bindBidirectional(model.fullNameProperty());
        txtUsername.textProperty().bindBidirectional(model.usernameProperty());
        txtTagId   .textProperty().bindBidirectional(model.tagIdProperty());

        chkAdmin       .setUserData(1);
        chkQualityControl.setUserData(2);
        chkOperator    .setUserData(3);

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

        model.errorMessageProperty().addListener((obs, o, msg) -> {
            if (msg != null && !msg.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
            }
        });
        model.successMessageProperty().addListener((obs, o, msg) -> {
            if (msg != null && !msg.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
            }
        });
    }

    public void getRightBox(VBox rightBox) {
        this.rightBox = rightBox;
    }

    public void setAdminController(dk.easv.belman.PL.AdminController controller) {
        this.adminController = controller;
    }

    public void setEditingUser(User user) {
        this.editingUser = user;

        txtFullName.setText(user.getFullName());
        txtUsername.setText(user.getUsername());
        txtUsername.setDisable(true);// Disable username field to prevent editing, since it's a unique identifier
        txtTagId.setText(user.getTagId());

        switch (user.getRoleId()) {
            case 1 -> chkAdmin.setSelected(true);
            case 2 -> chkQualityControl.setSelected(true);
            case 3 -> chkOperator.setSelected(true);
        }
    }


    @FXML
    private void btnSaveClick() {
        model.saveUser();
    }

    @FXML
    private void cancel() {
        model.clear();
        BorderPane bp = (BorderPane) rootVBox.getParent();
        bp.setCenter(rightBox);
    }
}
