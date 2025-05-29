package dk.easv.belman.pl.model;

import dk.easv.belman.config.ConfigCrypto;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import dk.easv.belman.exceptions.BelmanException;
import javafx.beans.property.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import static dk.easv.belman.dal.FilePaths.CONFIG_PATH;

public class UserModel {
    private final BLLManager bllManager = new BLLManager();

    private final StringProperty fullName = new SimpleStringProperty("");
    private final StringProperty username = new SimpleStringProperty("");
    private final StringProperty tagId = new SimpleStringProperty("");
    private final IntegerProperty roleId = new SimpleIntegerProperty(0);
    private User editingUser;
    private boolean newPassword = false;

    private final StringProperty errorMessage = new SimpleStringProperty();
    private final StringProperty successMessage = new SimpleStringProperty();


    public void saveUser() {
        Properties props = new Properties();
        String defaultPassword = "";
        String hashed;

        if (!newPassword) {

            try (FileInputStream fileInputStream = new FileInputStream(CONFIG_PATH)) {
                props.load(fileInputStream);
            } catch (IOException e) {
                throw new BelmanException("Error loading config properties: " + e.getMessage());
            }


            try {
                defaultPassword = ConfigCrypto.decrypt(props.getProperty("user.defaultPassword"));
            } catch (Exception e) {
                throw new BelmanException("Error decrypting the default password: " + e.getMessage());
            }
        }

        errorMessage.set("");
        successMessage.set("");

        if (fullName.get().isEmpty() || username.get().isEmpty()
                || tagId.get().isEmpty() || roleId.get() == 0) {
            errorMessage.set("Fill all fields and choose a role.");
            return;
        }

        if (!newPassword && editingUser == null) { // If adding a new user, it will use the default password
            try {
                hashed = bllManager.hashPass(username.get(), defaultPassword);
            } catch (Exception e) {
                errorMessage.set("Error hashing password: " + e.getMessage());
                return;
            }
        }
        else
            hashed = editingUser.getPassword();

        if(editingUser != null)
            hashed = editingUser.getPassword(); // Use existing password if editing

        if (editingUser != null) {
            editingUser.setFullName(fullName.get());
            editingUser.setTagId(Objects.equals(tagId.get(), "true") ? bllManager.hashPass(editingUser.getUsername(), "") : null);
            editingUser.setRoleId(roleId.get());
            editingUser.setPassword(hashed);

            boolean ok = bllManager.updateUser(editingUser);
            if (ok) {
                successMessage.set("User updated.");
            } else {
                errorMessage.set("Update failed.");
            }
            clear();
            editingUser = null;
        }
        else {  //Creating a new user

        User u = new User();
        u.setFullName(fullName.get());
        u.setUsername(username.get());
        u.setTagId(Objects.equals(tagId.get(), "true") ? bllManager.hashPass(u.getUsername(), "") : null);
        u.setRoleId(roleId.get());
        u.setPassword(hashed);

            try {
                if (bllManager.addUser(u) != null) {
                    successMessage.set("User created with default password: " + defaultPassword);
                    clear();
                } else {
                    errorMessage.set("User was not created.");
                }
            } catch (Exception ex) {
                errorMessage.set("Error creating user: " + ex.getMessage());
            }
        }
    }

    public void setEditingUser(User u) {
        this.editingUser = u;
        fullName.set(u.getFullName());
        username.set(u.getUsername());
        tagId.set(u.getTagId());
        roleId.set(u.getRoleId());
    }

    public void clear() {
        fullName.set("");
        username.set("");
        tagId.set("");
        roleId.set(0);
    }

    public void cancel(String prevTagId)
    {
        if(editingUser != null) editingUser.setTagId(prevTagId);
        fullName.set("");
        username.set("");
        tagId.set("");
        roleId.set(0);
    }

    public boolean checkNewPassword(String newPass, String newPass2) {
        if (newPass.isEmpty() || newPass2.isEmpty()) {
            errorMessage.set("To change the password, both fields must be filled.");
            return false;
        }
        if (!newPass.equals(newPass2)) {
            errorMessage.set("Passwords do not match.");
            return false;
        }
        newPassword = true;
        return true;
    }

    public void setNewPassword(String newPass) {
        if (editingUser != null) {
            try {

                String hashed = bllManager.hashPass(editingUser.getUsername(), newPass);
                editingUser.setPassword(hashed);
                boolean ok = bllManager.updateUser(editingUser);
                if (ok) {
                    successMessage.set("Password updated successfully.");
                } else {
                    errorMessage.set("Failed to update password.");
                }
            } catch (Exception e) {
                errorMessage.set("Error updating password: " + e.getMessage());
            }
        } else {
            errorMessage.set("No user is being edited.");
        }
    }

    // ─── Properties for binding ───────────────────────────────────────────────
    public StringProperty fullNameProperty() {
        return fullName;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty tagIdProperty() {
        return tagId;
    }

    public IntegerProperty roleIdProperty() {
        return roleId;
    }

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    public StringProperty successMessageProperty() {
        return successMessage;
    }
}
