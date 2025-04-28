package dk.easv.belman.dal;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateThumbnailTest {

    private File thumbnailFile;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize the JavaFX toolkit by launching a basic JavaFX Application
        if (!Platform.isFxApplicationThread()) {
            // This ensures that the JavaFX runtime is initialized
            Platform.runLater(() -> {
                // You can leave this empty, just ensure that it runs on the JavaFX thread
            });
            // Sleep for a short time to allow the JavaFX toolkit to initialize
            Thread.sleep(1000); // Adjust if necessary
        }
    }

    @Test
    void createThumbnail() throws InterruptedException {
        // Arrange
        int size = CreateThumbnail.getSize();
        String testImagePath = "file:src/test/resources/test_image.jpg";

        // Use Platform.runLater() to run code on the JavaFX thread
        Platform.runLater(() -> {
            // This ensures that the ImageView is created on the JavaFX Application Thread
            ImageView thumbnail = CreateThumbnail.createThumbnail(testImagePath);

            // Act
            assertNotNull(thumbnail);
            assertTrue(thumbnail.getFitWidth() == size || thumbnail.getFitHeight() == size,
                    "Either width or height must be equal to the thumbnail size");
        });

        // Allow some time for the UI thread to complete its work
        Thread.sleep(1000); // You may need to adjust this based on your specific case
    }

    @AfterEach
    void cleanUp() {
        if (thumbnailFile != null && thumbnailFile.exists()) {
            boolean deleted = thumbnailFile.delete();
            if (!deleted) {
                System.err.println("Warning: could not delete thumbnail test file!");
            }
        }
    }
}
