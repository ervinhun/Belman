package dk.easv.belman.pl.model;

import dk.easv.belman.config.ConfigCrypto;
import dk.easv.belman.config.ConfigReader;
import dk.easv.belman.be.User;
import dk.easv.belman.bll.BLLManager;
import dk.easv.belman.exceptions.BelmanException;
import javafx.beans.property.*;

import java.util.Properties;

public class UserModel {
    private final BLLManager bllManager = new BLLManager();

    private final StringProperty fullName = new SimpleStringProperty("");
    private final StringProperty username = new SimpleStringProperty("");
    private final StringProperty tagId = new SimpleStringProperty("");
    private final IntegerProperty roleId = new SimpleIntegerProperty(0);
    Properties props;
    private User editingUser;

    private final StringProperty errorMessage = new SimpleStringProperty();
    private final StringProperty successMessage = new SimpleStringProperty();

    public void saveUser() {
        props = new Properties();
        String defaultPassword = "";

        //Opening the config properties file to get the default password
        try {
            props.load(ConfigReader.class.getResourceAsStream("/config.properties"));
        } catch (Exception e) {
            throw new BelmanException("Error loading config properties: " + e);
        }

        //Trying to decyorpt the default password
        try {
            defaultPassword = ConfigCrypto.decrypt(props.getProperty("user.defaultPassword"));
        } catch (Exception e) {
            throw new BelmanException("Error decrypting the default password: " + e);
        }
        errorMessage.set("");
        successMessage.set("");

        if (fullName.get().isEmpty() || username.get().isEmpty()
                || tagId.get().isEmpty() || roleId.get() == 0) {
            errorMessage.set("Fill all fields and choose a role.");
            return;
        }

        String hashed;
        try {
            hashed = bllManager.hashPass(username.get(), defaultPassword);
        } catch (Exception e) {
            errorMessage.set("Error hashing password: " + e.getMessage());
            return;
        }
        if (editingUser != null) {
            editingUser.setFullName(fullName.get());
            editingUser.setTagId(tagId.get());
            editingUser.setRoleId(roleId.get());
            editingUser.setPassword(bllManager.hashPass(editingUser.getUsername(), defaultPassword));

            boolean ok = bllManager.updateUser(editingUser);
            if (ok) successMessage.set("User updated.");
            else errorMessage.set("Update failed.");
            editingUser = null;
            return;
        }
        User u = new User();
        u.setFullName(fullName.get());
        u.setUsername(username.get());
        u.setTagId(tagId.get());
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
