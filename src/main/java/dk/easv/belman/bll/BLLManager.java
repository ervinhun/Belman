package dk.easv.belman.bll;

import dk.easv.belman.be.User;
import dk.easv.belman.dal.DALManager;
import dk.easv.belman.exceptions.BelmanException;

import java.util.List;
import java.util.UUID;

public class BLLManager {
    private final DALManager dalManager;

    public BLLManager() throws BelmanException {
        dalManager = new DALManager();
    }

    public List<User> getAllUsers() {
        return dalManager.getAllUsers();
    }

    public UUID addUser(User user) {
        return dalManager.insertUser(user);
    }

    public boolean updateUser(User user) {
        return dalManager.updateUser(user);
    }

    public void deleteUser(UUID id) {
        dalManager.deleteUser(id);
    }
}
