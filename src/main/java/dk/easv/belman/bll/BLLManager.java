package dk.easv.belman.bll;

import dk.easv.belman.be.User;
import dk.easv.belman.dal.DALManager;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class BLLManager {
    private final DALManager dalManager;

    public BLLManager() {
        this.dalManager = new DALManager();
    }

    public String hashPass(String username, String pass) {
        PasswordHasher hasher = new PasswordHasher();
        String hashedPass = null;
        try {
            hashedPass = hasher.hashPassword(pass, username);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        return hashedPass;
    }

    public User login(String username, String password) {
        User user = dalManager.login(username, hashPass(username, password));
        return user;
    }
}
