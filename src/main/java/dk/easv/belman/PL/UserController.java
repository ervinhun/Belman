package dk.easv.belman.PL;

import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class UserController {

    @FXML private TextField txtFullName;
    @FXML private TextField txtUsername;
    @FXML private RadioButton chkAdmin;
    @FXML private RadioButton chkQualityControl;
    @FXML private RadioButton chkOperator;
    @FXML private ToggleGroup tgRole;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private VBox rootVBox;
    @FXML private VBox rightBox;
    @FXML private TextField txtTagId;
    private BLLManager bllManager;

    @FXML
    private void initialize() {
        try {
            bllManager = new BLLManager();
        } catch (Exception e) {
            showError("Database connection failed: " + e.getMessage());
            disableForm();
        }
    }


    public void getRightBox(VBox rightBox)
    {
        this.rightBox = rightBox;
    }

    @FXML
    private void btnSaveClick() {
        if (bllManager == null) return;

        String fullName = txtFullName.getText();
        String username = txtUsername.getText();
        String tagId = txtTagId.getText();
        int roleId = getSelectedRoleId();

        if (fullName.isEmpty() || username.isEmpty() || tagId.isEmpty() || roleId == 0) {
            showError("Fill all fields and choose a role.");
            return;
        }

        String defaultPassword = "belman123";
        String hashedPassword = bllManager.hashPass(username, defaultPassword);

        User u = new User();
        u.setFullName(fullName);
        u.setUsername(username);
        u.setTagId(tagId);
        u.setRoleId(roleId);
        u.setPassword(hashedPassword);

        if (bllManager.addUser(u) != null) {
            showSuccess("User created with default password: " + defaultPassword);
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
    private void cancel() {
        clearForm();
        BorderPane borderPane = (BorderPane) rootVBox.getParent();
        borderPane.setCenter(rightBox);
    }


    private void clearForm() {
        txtFullName.clear();
        txtUsername.clear();
        txtTagId.clear();
        tgRole.selectToggle(null);
    }

    private void disableForm() {
        btnSave.setDisable(true);
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private void showSuccess(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

}
