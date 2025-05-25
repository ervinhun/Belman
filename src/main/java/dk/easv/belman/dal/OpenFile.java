package dk.easv.belman.dal;

import dk.easv.belman.exceptions.BelmanException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class OpenFile {
    private static final Logger logger = LoggerFactory.getLogger(OpenFile.class);

    public OpenFile(String productNumber, boolean isSendingEmail, String email) {
        PDDocument document = openFileAndConvert(productNumber);

        // Save the document to a file and open it in a window
        File pdfFile = convertToFile(document);

        // Send email with the PDF attachment
        if (isSendingEmail && pdfFile != null && pdfFile.exists()) {
            sendEmail(email, pdfFile);
        }
        else if (pdfFile == null || !pdfFile.exists()) {
            logger.error("PDF file is null or does not exist.");
        }
    }

    private PDDocument openFileAndConvert(String productNumber) {
        DALManager dalManager = new DALManager();
        try {
            return Loader.loadPDF(dalManager.getPdfFromDb(productNumber));
        } catch (IOException e) {
            throw new BelmanException("Error while opening the pdf from db: " + e);
        }
    }

    private void sendEmail(String email, File pdfFile) {
        // Send email with the PDF attachment
        GmailService gmailService;
        try {
            gmailService = new GmailService();
        } catch (GeneralSecurityException | IOException e) {
            throw new BelmanException(e.getMessage());
        }
        try {
            if (email != null && !email.isEmpty()) {
                gmailService.sendEmailWithAttachment("me", email,
                        "Quality Check Report", "Please find the attached report.", pdfFile);
            } else {
                logger.error("Email address is null or empty.");
            }
        } catch (Exception e) {
            logger.error("Error while trying to send an e-mail: {}", e.getMessage());
        }
    }

    public File convertToFile(PDDocument document) {
        // Convert the document to a file
        File pdfFile = null;
        try {
            pdfFile = File.createTempFile("report", ".pdf");
            document.save(pdfFile);
            document.close();
        } catch (IOException e) {
            logger.error("Error while saving the PDF to a file: {}", e.getMessage());
        } finally {
            assert pdfFile != null;
            if (pdfFile.exists()) {
                pdfFile.deleteOnExit(); // Deletes the file when the JVM exits
            }
        }

        //Opening the pdf file
        if (Desktop.isDesktopSupported() && pdfFile.exists() && !GraphicsEnvironment.isHeadless()) {
            try {
                Desktop.getDesktop().open(pdfFile);
            } catch (IOException e) {
                logger.error("Error opening PDF: {}", e.getMessage());
            }
        } else {
            logger.error("Desktop is not supported on this system.");
        }
        return pdfFile;
    }
}

