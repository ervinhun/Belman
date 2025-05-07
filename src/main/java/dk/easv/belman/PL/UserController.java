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
    private dk.easv.belman.PL.AdminController adminController;
    private User editingUser = null;

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
        if (bllManager == null) return;

        String fullName = txtFullName.getText();
        String username = txtUsername.getText();
        String tagId = txtTagId.getText();
        int roleId = getSelectedRoleId();

        if (fullName.isEmpty() || username.isEmpty() || tagId.isEmpty() || roleId == 0) {
            showError("Fill all fields and choose a role.");
            return;
        }

        if (editingUser == null) {
            String defaultPassword = "belman123";
            String hashedPassword = bllManager.hashPass(username, defaultPassword);

            User newUser = new User();
            newUser.setFullName(fullName);
            newUser.setUsername(username);
            newUser.setTagId(tagId);
            newUser.setRoleId(roleId);
            newUser.setPassword(hashedPassword);

            if (bllManager.addUser(newUser) != null) {
                adminController.addUserCard(newUser);
                showSuccess("User created with default password: " + defaultPassword);
                clearForm();
            } else {
                showError("User was not created.");
            }
        } else {
            editingUser.setFullName(fullName);
            editingUser.setTagId(tagId);
            editingUser.setRoleId(roleId);

            String defaultPassword = "belman123";
            String hashedPassword = bllManager.hashPass(editingUser.getUsername(), defaultPassword);
            editingUser.setPassword(hashedPassword);

            if (bllManager.updateUser(editingUser)) {
                showSuccess("User updated successfully.");
                adminController.refreshUsers();
                clearForm();
            } else {
                showError("User update failed.");
            }

            editingUser = null;
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
        txtUsername.setDisable(false);
        editingUser = null;
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
