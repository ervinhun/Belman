package dk.easv.belman.bll;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.QualityDocument;
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
        String hashedPass = "";
        try {
            hashedPass = hasher.hashPassword(pass, username);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
        return hashedPass;
    }

    public User login(String username, String password) {
        return dalManager.login(username, hashPass(username, password));
    }

    public void logout(User user) {
        UUID id = user.getId();
        dalManager.logout(id);
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

    public boolean signOrder(String orderNumber, UUID userId, boolean isSendingEmail, String email) {
        long productId = dalManager.getProductIdFromProductNumber(orderNumber);
        if (productId == -1) return false;
        QualityDocument qcDoc = new QualityDocument(userId, productId);
        User user = dalManager.getUserById(userId);
        GenerateReport report = new GenerateReport(orderNumber, user, isSendingEmail, email);
        String filePath = report.getFilePath();
        qcDoc.setQcDocPath(filePath);
        qcDoc.setGeneratedBy(userId);
        return dalManager.signQualityDocument(qcDoc);
    }

    public boolean isDocumentExists(String orderNumber) {
        return dalManager.isDocumentExists(orderNumber);
    }

    public List<Photo> getPhotosForOrder(String orderNumber) {
        return dalManager.getPhotosByONum(orderNumber);
    }

    public void savePhotos(List<Photo> photos, String orderNumber) { dalManager.savePhotos(photos, orderNumber); }

    public void savePhotosBinary(List<Photo> photos, String orderNumber) {
        dalManager.savePhotosBinary(photos, orderNumber);
    }

    public User getUserById(UUID id) {
        return dalManager.getUserById(id);
    }

    public void sendBackToOperator(String orderNumber, UUID userId) {
        dalManager.sendBackToOperator(orderNumber, userId);
    }
}
