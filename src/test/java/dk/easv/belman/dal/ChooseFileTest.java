package dk.easv.belman.dal;

import javafx.scene.image.ImageView;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Objects;
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
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/" + testFileName + "2.jpg")), testImageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        testFiles.add(testImageFile);
        testImageFile = new File(tempDir, testFileName + ".png");
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/" + testFileName + ".png")), testImageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        testFiles.add(testImageFile);
    }


    @Test
    void getFileExtension() {
        String fileName = testFileName + ".jpg";
        String fileName2 = testFileName + ".png";
        String expectedExtension = "jpg";
        String expectedExtension2 = "png";
        String actualExtension = ChooseFile.getFileExtension(fileName);
        String actualExtension2 = ChooseFile.getFileExtension(fileName2);
        assertEquals(expectedExtension, actualExtension);
        assertEquals(expectedExtension2, actualExtension2);

        // Test with a file without an extension
        String fileNameWithoutExtension = "test_image";
        String actualExtensionWithout = ChooseFile.getFileExtension(fileNameWithoutExtension);
        assertEquals("", actualExtensionWithout, "Expected empty string for file without extension");
    }

    @Test
    void testChooseFile() {
        ChooseFile chooseFile = new ChooseFile(null, testFiles);
        assertNotNull(chooseFile.getChosenFile());
        assertEquals(2, chooseFile.getChosenFile().size());
        assertTrue(chooseFile.getChosenFile().stream().anyMatch(file -> file.getName().equals(testFileName + ".jpg")));
        assertTrue(chooseFile.getChosenFile().stream().anyMatch(file -> file.getName().equals(testFileName + ".png")));
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
