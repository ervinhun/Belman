package dk.easv.belman.bll;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.QualityDocument;
import dk.easv.belman.be.User;

import dk.easv.belman.dal.OrderManager;
import dk.easv.belman.dal.UserManager;
import dk.easv.belman.exceptions.BelmanException;

import java.util.List;
import java.util.UUID;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class BLLManager {
    private final UserManager userManager;
    private final OrderManager orderManager;

    public BLLManager() throws BelmanException {
        userManager = new UserManager();
        orderManager = new OrderManager();
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

    public User login(String username, String password, boolean isHashed) {
        return userManager.login(isHashed ? null : username, isHashed ? password : hashPass(username, password));
    }

    public void logout(User user) {
        UUID id = user.getId();
        userManager.logout(id);
    }

    public List<User> getAllUsers() {
        return userManager.getAllUsers();
    }

    public User addUser(User user) {
        if (user.getTagId() != null && user.getTagId().equals("true")) {
            user.setTagId(hashPass(user.getUsername(), ""));
        }
        else
            user.setTagId(null);
        UUID id = userManager.insertUser(user);
        if (id == null) return null;
        user.setId(id);
        return user;
    }

    public boolean updateUser(User user) {
        return userManager.updateUser(user);
    }

    public void deleteUser(UUID id) {
        userManager.deleteUser(id);
    }

    public List<Order> getOrders(String username) {
        return orderManager.getOrders(username);
    }

    public boolean signOrder(String orderNumber, UUID userId, boolean isSendingEmail, String email) {
        long productId = orderManager.getProductIdFromProductNumber(orderNumber);
        if (productId == -1) return false;
        QualityDocument qcDoc = new QualityDocument(userId, productId);
        User user = userManager.getUserById(userId);
        new GenerateReport(orderNumber, user, isSendingEmail, email);
        qcDoc.setGeneratedBy(userId);
        return orderManager.isDocumentExists(orderNumber);
    }

    public boolean isDocumentExists(String orderNumber) {
        return orderManager.isDocumentExists(orderNumber);
    }

    public List<Photo> getPhotosForOrder(String orderNumber) {
        return orderManager.getPhotosByONum(orderNumber);
    }

    public void savePhotosBinary(List<Photo> photos, String orderNumber) {
        orderManager.savePhotosBinary(photos, orderNumber);
    }

    public User getUserById(UUID id) {
        return userManager.getUserById(id);
    }

    public void sendBackToOperator(String orderNumber, UUID userId, String angle) {
        orderManager.sendBackToOperator(orderNumber, userId, angle);
    }

    public int getPhotosNumbersforOrder(String orderNumberToSign) {
        return orderManager.getPhotosNumbersforOrder(orderNumberToSign);
    }
}
