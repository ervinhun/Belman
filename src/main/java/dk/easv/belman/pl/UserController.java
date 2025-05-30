package dk.easv.belman.pl;

import dk.easv.belman.pl.model.UserModel;
import dk.easv.belman.be.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Toggle;


public class UserController {
    @FXML
    private TextField txtFullName;
    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtTagId;
    @FXML
    private CheckBox cbTagId;
    @FXML
    private RadioButton chkAdmin;
    @FXML
    private RadioButton chkQualityControl;
    @FXML
    private RadioButton chkOperator;
    @FXML
    private ToggleGroup tgRole;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;
    @FXML
    private VBox rootVBox;
    @FXML
    private Label lblError;
    @FXML
    private GridPane gridPassword;
    @FXML
    private PasswordField txtNewPass;
    @FXML
    private PasswordField txtNewPass2;

    private VBox      rightBox;
    private UserModel model;
    private AdminController adminController;
    private String prevTagId;


    @FXML
    private void initialize() {
        model = new UserModel();
        hidePasswordFields(true);

        txtFullName.textProperty().bindBidirectional(model.fullNameProperty());
        txtUsername.textProperty().bindBidirectional(model.usernameProperty());
        txtTagId   .textProperty().bindBidirectional(model.tagIdProperty());

        chkAdmin       .setUserData(1);
        chkQualityControl.setUserData(2);
        chkOperator    .setUserData(3);
        lblError.textProperty().bind(model.errorMessageProperty());
        lblError.visibleProperty().bind(model.errorMessageProperty().isNotEmpty());
        tgRole.selectedToggleProperty().addListener((_, _, newT) -> {
            if (newT != null) {
                model.roleIdProperty().set((int)newT.getUserData());
            } else {
                model.roleIdProperty().set(0);
            }
        });
        model.roleIdProperty().addListener((_, _, val) -> {
            for (Toggle t : tgRole.getToggles()) {
                if (Integer.valueOf((int)t.getUserData()).equals(val.intValue())) {
                    tgRole.selectToggle(t);
                    break;
                }
            }
        });

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
        if (!txtNewPass.getText().isEmpty() && !txtNewPass2.getText().isEmpty()) {
            if (model.checkNewPassword(txtNewPass.getText(), txtNewPass2.getText())) {
                model.setNewPassword(txtNewPass.getText());
            }
            else {
                return;
            }
            txtNewPass.clear();
            txtNewPass2.clear();
        }
        model.saveUser();

        String success = model.successMessageProperty().get();
        if (success != null && !success.isEmpty()) {
            adminController.reloadUsers();
            model.clear();
            goBack();
        }
    }


    public void setEditingUser(User u) {
        prevTagId = u.getTagId();
        cbTagId.setSelected(u.getTagId() != null);
        hidePasswordFields(false);
        u.setTagId(cbTagId.isSelected() ? "true" : "false");
        txtTagId.setText(u.getTagId());
        model.setEditingUser(u);
        txtUsername.setDisable(true);
    }


    @FXML
    private void cancel() {
        model.clear();
        model.cancel(prevTagId);
        goBack();
    }

    private void goBack()
    {
        BorderPane bp = (BorderPane) rootVBox.getParent();
        bp.setCenter(rightBox);
        txtUsername.setDisable(false);

        adminController.resizeWindow(rightBox);
        hidePasswordFields(true);
    }

    private void hidePasswordFields(boolean hide) {
        gridPassword.setVisible(!hide);
        gridPassword.setManaged(!hide);
        txtUsername.setDisable(!hide);
    }
}
