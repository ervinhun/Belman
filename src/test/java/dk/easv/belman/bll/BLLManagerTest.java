package dk.easv.belman.bll;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.User;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@Disabled("Disabled for GHActions due to no DB access")
class BLLManagerTest {

    private final String productNoString = "TEST-001";
    private final String invalidProductNoString = "INVALID-002";
    private static User user;
    private BLLManager bllManager;

    @BeforeEach
    void setUp() {
        user = new User(UUID.randomUUID(), "Test User", "testUser", "password123", "TAG123", 1, true);
        bllManager = new BLLManager();
    }

    @Test
    void getAllUsers() {
        assumeFalse("true".equals(System.getenv("GITHUB_ACTIONS")),
                "Skipping test on GitHub Actions (no DB access)");
        List<User> lUser = bllManager.getAllUsers();
        assertNotNull(lUser, "List of users should not be null");
        assertFalse(lUser.isEmpty(), "List of users should not be empty");
    }

    @Test
    @DisplayName("Tests to get all the orders from the database")
    void getOrders() {
        assumeFalse("true".equals(System.getenv("GITHUB_ACTIONS")),
                "Skipping test on GitHub Actions (no DB access)");
        List<Order> orders = bllManager.getOrders(null);
        assertNotNull(orders, "List of orders should not be null");
        assertFalse(orders.isEmpty(), "List of orders should not be empty");
    }

    @Test
    @DisplayName("Tests to get qc document for TEST-001")
    void isDocumentExists() {
        assumeFalse("true".equals(System.getenv("GITHUB_ACTIONS")),
                "Skipping test on GitHub Actions (no DB access)");
        boolean exists = bllManager.isDocumentExists(productNoString);
        assertTrue(exists, "Document with product number " + productNoString + " should exist");

        boolean notExists = bllManager.isDocumentExists(invalidProductNoString);
        assertFalse(notExists, "Document with product number " + invalidProductNoString + " should not exist");
    }

    @Test
    @DisplayName("Tests to get user by ID")
    void getUserById() {
        assumeFalse("true".equals(System.getenv("GITHUB_ACTIONS")),
                "Skipping test on GitHub Actions (no DB access)");
        UUID validUserId = UUID.fromString("074E23EE-D0AB-4C08-A624-61F6E5A692BC");
        User foundUser = bllManager.getUserById(validUserId);
        User invalidUser = bllManager.getUserById(user.getId());
        assertNotNull(foundUser, "User should not be null");
        assertEquals(validUserId, foundUser.getId(), "User ID should match");
        assertEquals("dprince", foundUser.getUsername(), "Usernames should match");
        assertNull(invalidUser, "User with invalid ID should be null");
    }

    @Test
    @DisplayName("Tests to get photos number for order no TEST-001")
    void getPhotosNumbersforOrder() {
        assumeFalse("true".equals(System.getenv("GITHUB_ACTIONS")),
                "Skipping test on GitHub Actions (no DB access)");
        int returnNumber = bllManager.getPhotosNumbersforOrder(productNoString);
        int invalidReturnNumber = bllManager.getPhotosNumbersforOrder(invalidProductNoString);
        assertTrue(returnNumber > 0, "Return number for valid product should be greater than 0");
        assertEquals(0, invalidReturnNumber, "Return number for invalid product should be 0");
        assertEquals(0, bllManager.getPhotosNumbersforOrder(null), "Return number for null product should be 0");
    }

    @Test
    @DisplayName("Tests to get photos for order no TEST-001")
    void getPhotosForOrder() {
        assumeFalse("true".equals(System.getenv("GITHUB_ACTIONS")),
                "Skipping test on GitHub Actions (no DB access)");
        List<Photo> photos = bllManager.getPhotosForOrder(productNoString);
        assertNotNull(photos, "Photos list should not be null");
        assertFalse(photos.isEmpty(), "Photos list for valid order should not be empty");

        List<Photo> invalidPhotos = bllManager.getPhotosForOrder(invalidProductNoString);
        assertNotNull(invalidPhotos, "Photos list for invalid order should not be null");
        assertTrue(invalidPhotos.isEmpty(), "Photos list for invalid order should be empty");
    }

    @Test
    @DisplayName("Tests to check if document exists with invalid product number")
    void isDocumentExistsWithInvalidProduct() {
        assumeFalse("true".equals(System.getenv("GITHUB_ACTIONS")),
                "Skipping test on GitHub Actions (no DB access)");
        boolean exists = bllManager.isDocumentExists(invalidProductNoString);
        assertFalse(exists, "Document with invalid product number should not exist");
    }

    @Test
    @DisplayName("Tests user operations: add, update")
    void userOperations() {
        assumeFalse("true".equals(System.getenv("GITHUB_ACTIONS")),
                "Skipping test on GitHub Actions (no DB access)");
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

    @AfterEach
    void tearDownAll() {
        assumeFalse("true".equals(System.getenv("GITHUB_ACTIONS")),
                "Skipping test on GitHub Actions (no DB access)");
        bllManager.deleteUser(user.getId());
    }
}