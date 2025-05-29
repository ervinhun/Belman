package dk.easv.belman.bll;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BLLManagerTest {

    private String productNoString = "TEST-001";
    private String invalidProductNoString = "INVALID-002";
    private static User user;
    private BLLManager bllManager;

    @BeforeEach
    void setUp() {
        user = new User(UUID.randomUUID(), "Test User", "testUser", "password123", "TAG123", 1, true);
        bllManager = new BLLManager();
    }

    @Test
    void getAllUsers() {
        List<User> lUser = bllManager.getAllUsers();
        assertNotNull(lUser, "List of users should not be null");
        assertFalse(lUser.isEmpty(), "List of users should not be empty");
    }

    @Test
    void getOrders() {
        List<Order> orders = bllManager.getOrders(null);
        assertNotNull(orders, "List of orders should not be null");
        assertFalse(orders.isEmpty(), "List of orders should not be empty");
    }

    @Test
    void isDocumentExists() {
        boolean exists = bllManager.isDocumentExists(productNoString);
        assertTrue(exists, "Document with product number " + productNoString + " should exist");

        boolean notExists = bllManager.isDocumentExists(invalidProductNoString);
        assertFalse(notExists, "Document with product number " + invalidProductNoString + " should not exist");
    }

    @Test
    void getUserById() {
        UUID validUserId = UUID.fromString("074E23EE-D0AB-4C08-A624-61F6E5A692BC");
        User foundUser = bllManager.getUserById(validUserId);
        User invalidUser = bllManager.getUserById(user.getId());
        assertNotNull(foundUser, "User should not be null");
        assertEquals(validUserId, foundUser.getId(), "User ID should match");
        assertEquals("dprince", foundUser.getUsername(), "Usernames should match");
        assertNull(invalidUser, "User with invalid ID should be null");
    }

    @Test
    void getPhotosNumbersforOrder() {
        int returnNumber = bllManager.getPhotosNumbersforOrder(productNoString);
        int invalidReturnNumber = bllManager.getPhotosNumbersforOrder(invalidProductNoString);
        assertTrue(returnNumber > 0, "Return number for valid product should be greater than 0");
        assertEquals(0, invalidReturnNumber, "Return number for invalid product should be 0");
        assertEquals(0, bllManager.getPhotosNumbersforOrder(null), "Return number for null product should be 0");
    }

    @Test
    void getPhotosForOrder() {
        List<Photo> photos = bllManager.getPhotosForOrder(productNoString);
        assertNotNull(photos, "Photos list should not be null");
        assertFalse(photos.isEmpty(), "Photos list for valid order should not be empty");

        List<Photo> invalidPhotos = bllManager.getPhotosForOrder(invalidProductNoString);
        assertNotNull(invalidPhotos, "Photos list for invalid order should not be null");
        assertTrue(invalidPhotos.isEmpty(), "Photos list for invalid order should be empty");
    }

    @Test
    void isDocumentExistsWithInvalidProduct() {
        boolean exists = bllManager.isDocumentExists(invalidProductNoString);
        assertFalse(exists, "Document with invalid product number should not exist");
    }

    @Test
    void userOperations() {
        User addedUser = bllManager.addUser(user);
        assertNotNull(addedUser, "Added user should not be null");
        assertEquals(user.getUsername(), addedUser.getUsername(), "Usernames should match");
        assertEquals(user.getFullName(), addedUser.getFullName(), "Full names should match");
        assertEquals(user.getTagId(), addedUser.getTagId(), "Tag IDs should match");
        user.setFullName("Updated User");
        boolean updated = bllManager.updateUser(user);
        assertTrue(updated, "User should be updated successfully");
        assertEquals(user.getFullName(), bllManager.getUserById(addedUser.getId()).getFullName(), "Updated full name should match");
    }

    @AfterAll
    static void tearDownAll() {
        BLLManager bllManager = new BLLManager();
        bllManager.deleteUser(user.getId());
    }
}