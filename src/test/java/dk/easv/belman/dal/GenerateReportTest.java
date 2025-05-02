//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dk.easv.belman.dal;

import java.io.File;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GenerateReportTest {
    private String productNoString = "I524-08641";
    GenerateReportTest() {
    }

    @Disabled("Disabled for GHA tests")
    @Test
    void GenerateReport() {
        Assertions.assertDoesNotThrow(() -> {
            GenerateReport.GenerateReport(productNoString);
        });
        String reportFilePath = FilePaths.BASE_PATH + "report/" + productNoString + "/report.pdf";
        Assertions.assertTrue((new File(reportFilePath)).exists(), "Report file should be created");
    }

    @AfterEach
    void tearDown() {
        String reportFilePath = FilePaths.BASE_PATH + "report/" + productNoString + "/report.pdf";
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
