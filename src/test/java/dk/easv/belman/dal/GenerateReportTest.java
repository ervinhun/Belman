//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dk.easv.belman.dal;

import java.io.File;
import java.util.UUID;

import dk.easv.belman.be.User;
import dk.easv.belman.bll.GenerateReport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class GenerateReportTest {
    private final String productNoString = "UTEST-001";
    GenerateReportTest() {
    }

    @Disabled("Disabled for GHA tests")
    @Test
    void GenerateReport() {
        // Arrange
        User user = new User(UUID.randomUUID(), "Test User", "testUser",
                UUID.randomUUID().toString(), null, 1, true);
        GenerateReport generateReport = new GenerateReport(productNoString, user, false, null);
        assertNotNull(generateReport, "GenerateReport instance should not be null");
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
