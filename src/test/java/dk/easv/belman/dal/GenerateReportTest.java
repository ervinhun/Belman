//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dk.easv.belman.dal;

import java.io.File;
import java.util.UUID;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import dk.easv.belman.be.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GenerateReportTest {
    private String productNoString = "I524-08642";
    GenerateReportTest() {
    }

    @Disabled("Disabled for GHA tests")
    @Test
    void GenerateReport() {
        final String[] generatedFilePath = {""};
        User user = new User(UUID.randomUUID(), "Test Albert", "test", "Test", "001", 2, null, null, true);

        Assertions.assertDoesNotThrow(() -> {
            GenerateReport generateReport = new GenerateReport(productNoString, user, true, "nyeres@gmail.com");
            generatedFilePath[0] = generateReport.getFilePath();
        });
        String reportFilePath = FilePaths.REPORT_DIRECTORY + productNoString + "/report.pdf";
        Assertions.assertTrue((new File(reportFilePath)).exists(), "Report file should be created");
        Assertions.assertTrue((new File(reportFilePath)).isFile(), "Report file should be a file");
        Assertions.assertTrue((new File(reportFilePath)).length() > 0, "Report file should not be empty");
        Assertions.assertTrue((new File(reportFilePath)).canRead(), "Report file should be readable");
        Assertions.assertEquals(generatedFilePath[0], reportFilePath, "Generated file path should match the expected path");
    }

    @AfterEach
    void tearDown() {
        String reportFilePath = FilePaths.BASE_PATH + "report/" + productNoString + "1/report.pdf";
        File reportFile = new File(reportFilePath);
        if (reportFile.exists()) {
            Assertions.assertTrue(reportFile.delete(), "Report file should be deleted");
        }
        File reportDir = new File(FilePaths.BASE_PATH + "report/" + productNoString);
        if (reportDir.exists()) {
            reportDir.delete();
        }

    }
}
