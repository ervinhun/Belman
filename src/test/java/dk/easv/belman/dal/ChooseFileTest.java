package dk.easv.belman.dal;

import dk.easv.belman.dal.ChooseFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChooseFileTest {
    private List<String> filePaths;

    @Test
    void testGetSelectedFilePath() throws Exception {
        // Setup: manually create a ChooseFile object
        ChooseFile chooseFile = new ChooseFile(null, null);

        // Use reflection to set the private field "chosenFile" for testing
        Field field = ChooseFile.class.getDeclaredField("chosenFile");
        field.setAccessible(true);
        field.set(chooseFile, List.of(
                new File("src/test/resources/test_image.jpg").getAbsolutePath(),
                new File("src/test/resources/test_image.png").getAbsolutePath()
        ));

        filePaths = chooseFile.getSelectedFilePath();

        assertEquals(2, filePaths.size());
        assertTrue(filePaths.get(0).endsWith("test_image.jpg"));
        assertTrue(filePaths.get(1).endsWith("test_image.png"));
    }

    @AfterEach
    void cleanUp() {
        if (filePaths != null) {
            for (String filePath : filePaths) {
                File file = new File(filePath);
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        System.err.println("Warning: could not delete test file " + filePath);
                    }
                }
            }
        }
    }
}
