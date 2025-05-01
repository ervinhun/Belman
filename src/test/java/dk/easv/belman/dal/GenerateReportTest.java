//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dk.easv.belman.dal;

import java.io.File;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GenerateReportTest {
    GenerateReportTest() {
    }

    @Test
    void main() {
        Assertions.assertDoesNotThrow(() -> {
            GenerateReport.main(new String[0]);
        });
        String reportFilePath = "output.pdf";
        Assertions.assertTrue((new File(reportFilePath)).exists(), "Report file should be created");
    }

    @AfterEach
    void tearDown() {
        String reportFilePath = "output.pdf";
        File reportFile = new File(reportFilePath);
        if (reportFile.exists()) {
            Assertions.assertTrue(reportFile.delete(), "Report file should be deleted");
        }

    }
}
