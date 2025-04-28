package dk.easv.belman.dal;

import javafx.scene.image.ImageView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class CreateThumbnailTest {

    private File thumbnailFile;
    @Test
    void createThumbnail() {
        // Arrange
        int size = CreateThumbnail.getSize();
        String testImagePath = "file:src/test/resources/test_image.jpg";
        ImageView thumbnail = CreateThumbnail.createThumbnail(testImagePath);
        // Act
        assertNotNull(thumbnail);
        assertTrue(thumbnail.getFitWidth() == size || thumbnail.getFitHeight() == size,
                "Either width or height must be equal to the thumbnail size");
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