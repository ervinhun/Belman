package dk.easv.belman.bll;

import dk.easv.belman.be.QualityDocument;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.User;
import dk.easv.belman.dal.DALManager;

import dk.easv.belman.dal.GenerateReport;
import dk.easv.belman.exceptions.BelmanException;

import java.util.List;
import java.util.UUID;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class BLLManager {
    private final DALManager dalManager;
 
    public BLLManager() throws BelmanException {
        dalManager = new DALManager();
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

    public List<User> getAllUsers() {
        return dalManager.getAllUsers();
    }

    public User addUser(User user) {
        UUID id = dalManager.insertUser(user);
        if (id == null) return null;
        user.setId(id);
        return user;
    }

    public boolean updateUser(User user) {
        return dalManager.updateUser(user);
    }

    public void deleteUser(UUID id) {
        dalManager.deleteUser(id);
    }

    public List<Order> getOrders(String username) { return dalManager.getOrders(username); }

    public Boolean signOrder(String productNumber, UUID userId) {
        long productId = dalManager.getProductIdFromProductNumber(productNumber);
        if (productId == -1) return false;
        QualityDocument qcDoc = new QualityDocument(userId, productId);
        GenerateReport report = new GenerateReport(productNumber);
        String filePath = report.getFilePath();
        qcDoc.setQcDocPath(filePath);
        qcDoc.setGeneratedBy(userId);
        dalManager.signQualityDocument(qcDoc);
        //return dalManager.signOrder(productNumber, userId);
        return true;
    }

    public boolean checkIfDocumentExists(String orderNumber) {
        return dalManager.checkIfDocumentExists(orderNumber);
    }
}
