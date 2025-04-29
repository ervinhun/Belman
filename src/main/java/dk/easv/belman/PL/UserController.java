package dk.easv.belman.PL;

import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

public class UserController {

    @FXML private TextField txtFullName;
    @FXML private TextField txtUsername;
    @FXML private TextField txtPassword;
    @FXML private TextField txtConfirmPassword;
    @FXML private RadioButton chkAdmin;
    @FXML private RadioButton chkQualityControl;
    @FXML private RadioButton chkOperator;
    @FXML private ToggleGroup tgRole;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btnGenerateQR;
    @FXML private ImageView qrImage;

    private BLLManager bllManager;

    @FXML
    private void initialize() {
        try {
            bllManager = new BLLManager();
        } catch (Exception e) {
            showError("Database connection failed: " + e.getMessage());
            disableForm();
        }

        btnGenerateQR.setDisable(true);
        tgRole.selectedToggleProperty().addListener((obs, oldVal, newVal) ->
                btnGenerateQR.setDisable(newVal == null || !((RadioButton) newVal).getText().equals("Operator"))
        );
    }

    @FXML
    private void btnSaveClick() {
        if (bllManager == null) return;

        String fullName = txtFullName.getText();
        String username  = txtUsername.getText();
        String password  = txtPassword.getText();
        String confirm   = txtConfirmPassword.getText();
        int roleId       = getSelectedRoleId();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirm.isEmpty() || roleId == 0) {
            showError("Fill all fields and choose a role.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        User u = new User();
        u.setFullName(fullName);
        u.setUsername(username);
        u.setPassword(password);
        u.setTagId(null);
        u.setRoleId(roleId);

        if (bllManager.addUser(u) != null) {
            showSuccess("User created.");
            clearForm();
        } else {
            showError("User was not created.");
        }
    }

    private int getSelectedRoleId() {
        if (chkAdmin.isSelected()) return 1;
        if (chkQualityControl.isSelected()) return 2;
        if (chkOperator.isSelected()) return 3;
        return 0;
    }

    @FXML
    private void btnCancelClick() {
        clearForm();
    }

    @FXML
    private void chkOperator() {
    }

    @FXML
    private void btnGenerateQRClick() {
    }

    private void clearForm() {
        txtFullName.clear();
        txtUsername.clear();
        txtPassword.clear();
        txtConfirmPassword.clear();
        tgRole.selectToggle(null);
        btnGenerateQR.setDisable(true);
    }

    private void disableForm() {
        btnSave.setDisable(true);
        btnGenerateQR.setDisable(true);
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private void showSuccess(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
