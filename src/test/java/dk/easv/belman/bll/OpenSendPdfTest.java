package dk.easv.belman.bll;

import dk.easv.belman.exceptions.BelmanException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenSendPdfTest {

    @Test
    void openSendPdf() {
        // Arrange
        String productNo = "TEST-001";
        String invalidProductNo = "INVALID-001";

        // Act & Assert
        assertDoesNotThrow(() -> new OpenSendPdf(productNo, false, null), "OpenSendPdf should not throw an exception");
        assertThrows(BelmanException.class, () -> new OpenSendPdf(invalidProductNo, false, null), "OpenSendPdf should throw an exception for invalid product number");
        assertDoesNotThrow(() -> new OpenSendPdf(productNo, true, "nyeres@gmail.com"), "OpenSendPdf should not throw an exception when sending email");
    }

}