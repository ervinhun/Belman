package dk.easv.belman.dal;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class OpenFile {
    public OpenFile(String filePath) {
        //File pdfFile = new File(FilePaths.BASE_PATH + "report/" + productNumber + "/report.pdf");
        File fileToOpen = new File(filePath);
        if (Desktop.isDesktopSupported() && fileToOpen.exists()) {
            try {
                Desktop.getDesktop().open(fileToOpen);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Desktop is not supported on this system.");
        }
    }

    public OpenFile(String filePath, boolean isSendingEmail, String email) {
        File fileToOpen = new File(filePath);
        if (Desktop.isDesktopSupported() && fileToOpen.exists()) {
            try {
                Desktop.getDesktop().open(fileToOpen);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Desktop is not supported on this system.");
        }
    }
}
