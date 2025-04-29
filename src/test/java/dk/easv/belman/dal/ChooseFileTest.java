package dk.easv.belman.dal;

import javafx.scene.image.ImageView;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;


import static org.junit.jupiter.api.Assertions.*;

class ChooseFileTest {
    @TempDir
    static File tempDir; // JUnit creates a temporary folder automatically

    private File testImageFile;
    private File savedThumbnail;
    private ArrayList<File> testFiles;
    private String testFileName;


    @BeforeEach
    void setUp() throws IOException {
        // Create a dummy image file to simulate upload
        testFileName = "test_image";
        testFiles = new ArrayList<>();
        testImageFile = new File(tempDir, testFileName + ".jpg");
        Files.copy(getClass().getResourceAsStream("/" + testFileName + ".jpg"), testImageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        testFiles.add(testImageFile);
        testImageFile = new File(tempDir, testFileName + ".png");
        Files.copy(getClass().getResourceAsStream("/" + testFileName + ".png"), testImageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        testFiles.add(testImageFile);
    }
    @Disabled
    @Test
    void testCreateThumbnail() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        int THUMBNAIL_SIZE = ChooseFile.getThumbnailSize();
        Platform.runLater(() -> {
            try {
                // JavaFX operations
                ImageView thumbnail = new ImageView();
                thumbnail.setFitWidth(THUMBNAIL_SIZE);
                thumbnail.setFitHeight(THUMBNAIL_SIZE);
                thumbnail.setPreserveRatio(true);
                double fitSize = thumbnail.getFitHeight() == THUMBNAIL_SIZE ? thumbnail.getFitHeight() : thumbnail.getFitWidth();
                assertEquals(THUMBNAIL_SIZE, fitSize);
            } finally {
                latch.countDown(); // Very important: release test thread
            }
        });
        latch.await();
    }
    @Disabled
    @Test
    void testChooseFile() {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ChooseFile chooseFile = new ChooseFile(null, "testProductNo", testFiles);
                assertNotNull(chooseFile.getSelectedFilePath());
                assertEquals(testFiles.size(), chooseFile.getChosenFile().size());
            } finally {
                latch.countDown(); // Very important: release test thread
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void getFileExtension() {
        String fileName = testFileName + ".jpg";
        String expectedExtension = "jpg";
        String actualExtension = ChooseFile.getFileExtension(fileName);
        assertEquals(expectedExtension, actualExtension);
    }

    @AfterEach
    void cleanUp() {
        for (File file : testFiles) {
            if (file != null && file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    System.err.println("Warning: could not delete test file: " + file.getAbsolutePath());
                }
            }
        }
    }
}
