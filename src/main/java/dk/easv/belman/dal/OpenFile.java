package dk.easv.belman.dal;

import dk.easv.belman.exceptions.BelmanException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class OpenFile {
    public OpenFile(String filePath) {
        File fileToOpen = new File(filePath);
        if (Desktop.isDesktopSupported() && fileToOpen.exists()) {
            try {
                Desktop.getDesktop().open(fileToOpen);
            } catch (IOException e) {
                throw new BelmanException("Error getting the file. " + e);
            }
        } else {
            throw new BelmanException("Desktop is not supported on this system.");
        }
    }

    public OpenFile(String filePath, boolean isSendingEmail, String email) {
        File fileToOpen = new File(filePath);
        if (Desktop.isDesktopSupported() && fileToOpen.exists()) {
            try {
                Desktop.getDesktop().open(fileToOpen);
            } catch (IOException e) {
                throw new BelmanException("Desktop is not supported to open the file. " + e);
            }
        } else {
            throw new BelmanException("Desktop is not supported on this system.");
        }
        if (isSendingEmail && fileToOpen.exists()) {

            GmailService gmailService = null;
            try {
                gmailService = new GmailService();
            } catch (GeneralSecurityException | IOException e) {
                throw new BelmanException(e.getMessage());
            }
            try {
                if (email != null && !email.isEmpty()) {
                    gmailService.sendEmailWithAttachment("me", "nyeres@gmail.com",
                            "Quality Check Report", "Please find the attached report.", fileToOpen.getAbsoluteFile());
                } else {
                    throw new BelmanException("Email address is null or empty.");
                }
            } catch (Exception e) {
                throw new BelmanException("Error while trying to send an e-mail: " + e);
            }
        }
    }
}
