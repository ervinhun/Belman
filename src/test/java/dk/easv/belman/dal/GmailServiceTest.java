package dk.easv.belman.dal;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class GmailServiceTest {

    @Test
    void sendEmailWithAttachment() throws Exception {
        // Arrange
        String recipient = "nyeres@gmail.com";
        String subject = "Test Email";
        String bodyText = "This is a test email with attachment.";
        String filePath = "src/test/resources/test_image.png";
        File file = new File(filePath);
        GmailService gmailService = new GmailService();
        // Act
        try {
            gmailService.sendEmailWithAttachment("me", recipient, subject, bodyText, file);
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
        if (file.exists()) {
            assertTrue(file.delete(), "Test file should be deleted");
        }
    }
}