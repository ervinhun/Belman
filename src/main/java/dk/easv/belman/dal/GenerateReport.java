package dk.easv.belman.dal;

import dk.easv.belman.be.User;
import dk.easv.belman.exceptions.BelmanException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateReport {
    private static final Logger logger = LoggerFactory.getLogger(GenerateReport.class);
    static {
        new DALManager();
    }
    private final String productNo;
    private static final String FILE_NAME = "report.pdf";
    private boolean isSendingEmail;
    private String email;




    public GenerateReport(String productNumber, User loggedInUser, boolean isSendingEmail, String email) {
        this.productNo = productNumber;
        this.isSendingEmail = isSendingEmail;
        this.email = email;
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = currentDate.format(formatter);
        String filePath = FilePaths.REPORT_DIRECTORY + productNo;
        float currentYPosition = 0;
        float margin = 40;
        float availableWidth;
        float minY = 100;
        DALManager dalManager = new DALManager();
        ArrayList<String> imagePaths = new ArrayList<>(dalManager.getPhotoPathsForReport(productNo));
        if (imagePaths.isEmpty()) {
            logger.error("No images found for product number: {}", productNo);
            return;
        }
        List<BufferedImage> bufferedImages = new ArrayList<>();
        bufferedImages.addAll(loadBufferedImages(imagePaths));


        // Create a new PDF document

        try (PDDocument document = new PDDocument()) {
            ObjectMapper objectMapper = new ObjectMapper();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            availableWidth = page.getMediaBox().getWidth() - 2 * margin;

            //Loading the json file
            QualityCheckJsonReader report = objectMapper.readValue(new File("report.json"), QualityCheckJsonReader.class);
            String title = report.getTitle();
            String dynamicText = report.getDynamicText();
            List<String> headText = report.getHeadText();
            List<String> bodyText = report.getBodyText();
            String signedBy = report.getSignedBy();

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            // Add logo
            PDImageXObject pdLogo = PDImageXObject.createFromFile("src/main/resources/dk/easv/belman/Images/belman.png", document);
            contentStream.drawImage(pdLogo, 20, 760, (float) pdLogo.getWidth() / 4, (float) pdLogo.getHeight() / 4);
            float titleFontSize = 20;
            float productNoFontSize = 16;
            float bodyTextFontSize = 12;
            float headerFontSize = 10;
            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            PDType1Font dynamicFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            // Set header
            contentStream.beginText();
            contentStream.setFont(dynamicFont, headerFontSize);
            contentStream.newLineAtOffset(480, 810);
            for (String headTextLine : headText) {
                contentStream.showText(headTextLine);
                contentStream.newLineAtOffset(0, -15);
            }
            contentStream.endText();
            // Calculate width of the title
            float titleWidth = titleFont.getStringWidth(title) / 1000 * titleFontSize;

            // Calculate width of the dynamic text
            dynamicText = dynamicText + " " + productNo;
            float dynamicWidth = dynamicFont.getStringWidth(dynamicText) / 1000 * productNoFontSize;

            // Calculate center positions
            PDRectangle mediaBox = page.getMediaBox();
            float centerXTitle = (mediaBox.getWidth() - titleWidth) / 2;
            float centerXDynamic = (mediaBox.getWidth() - dynamicWidth) / 2;

            // Set font and add title
            contentStream.beginText();
            contentStream.setFont(titleFont, titleFontSize);
            contentStream.newLineAtOffset(centerXTitle, 720);
            contentStream.showText(title);
            currentYPosition = 720 - titleFontSize - 20;

            // Set font and add order number line
            contentStream.newLineAtOffset(centerXDynamic - centerXTitle, -20);
            contentStream.setFont(dynamicFont, productNoFontSize);
            contentStream.showText(dynamicText);
            contentStream.endText();
            currentYPosition -= productNoFontSize + 20;


            // Add body text
            contentStream.beginText();
            contentStream.setFont(dynamicFont, bodyTextFontSize);
            contentStream.newLineAtOffset(100, currentYPosition);
            for (String bodyTextLine : bodyText) {
                contentStream.showText(bodyTextLine);
                contentStream.newLineAtOffset(0, -15);
                currentYPosition -= bodyTextFontSize + 15;
            }
            contentStream.endText();

            currentYPosition -= 10;

            // Add image
            for (BufferedImage bImage : bufferedImages) {

                // Create PDImageXObject from BufferedImage
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, bImage);
            float originalWidth = pdImage.getWidth();
            float originalHeight = pdImage.getHeight();

            // Scale to fit available width
            float scale = availableWidth / originalWidth;
            float scaledWidth = originalWidth * scale;
            float scaledHeight = originalHeight * scale;

            // Add new page if not enough space
            if (currentYPosition - scaledHeight < minY) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                currentYPosition = page.getMediaBox().getHeight() - margin;
            }

            // Draw the scaled image
            contentStream.drawImage(pdImage, margin, currentYPosition - scaledHeight, scaledWidth, scaledHeight);
            currentYPosition -= scaledHeight + 20; // spacing between images
        }
            // Add footer
            float footerHeight = 60; // font size 10 * 3 (because it's three lines) plus 2*15 for spacing
            if (currentYPosition - footerHeight < margin) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                currentYPosition = page.getMediaBox().getHeight() - margin;
            }
            float footerY = currentYPosition - 20;

            contentStream.beginText();
            contentStream.setFont(dynamicFont, 10);
            contentStream.newLineAtOffset(400, footerY);
            contentStream.showText(signedBy);
            contentStream.newLineAtOffset(50, -15);
            contentStream.showText(loggedInUser.getFullName());
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Date: " + formattedDate);
            contentStream.endText();

            contentStream.close();
            // Save the document
            File targetDir = new File(filePath);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            sleepForFileCreation(filePath + "/" + FILE_NAME);
            document.save(filePath + "/" + FILE_NAME);
            openDocument(productNo);

        } catch (IOException e) {
            logger.error("I/O error while generating PDF: {}", e.getMessage());
        } catch (IllegalStateException e) {
            logger.error("Illegal state encountered while generating PDF: {}", e.getMessage());
        } catch (SecurityException e) {
            logger.error("Security exception while accessing file system: {}", e.getMessage());
            } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread was interrupted while creating the file: {}", e.getMessage());
        }
    }

    private Collection<? extends BufferedImage> loadBufferedImages(ArrayList<String> imagePaths) {
        Collection<BufferedImage> bufferedImages = new ArrayList<>();
        if (imagePaths == null || imagePaths.isEmpty()) {
            logger.error("No image paths provided.");
            return bufferedImages;
        }
        for (String imagePath : imagePaths) {
            File file = new File(imagePath);
            if (file.exists()) {
                try {
                    BufferedImage image = javax.imageio.ImageIO.read(file);
                    bufferedImages.add(image);
                } catch (IOException e) {
                    logger.error("Error reading image file: {}", e.getMessage());
                }
            } else {
                logger.error("Image file does not exist: {}", imagePath);
            }
        }
        return bufferedImages;
    }

    private void sleepForFileCreation(String filepath) throws IOException, InterruptedException {
        File file = new File(filepath);
        if (file.exists()) {
            return;
        }
        Path directory = file.getParentFile().toPath();
        try (WatchService watchService = directory.getFileSystem().newWatchService()) {
            directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path createdFile = directory.resolve((Path) event.context());
                    if (createdFile.toFile().equals(file)) {
                        return;
                    }
                }
                if (!key.reset()) {
                    break;
                }
            }
        }
    }

    public String getFilePath() {
        return FilePaths.REPORT_DIRECTORY + productNo + "/" + FILE_NAME;
    }

    public void openDocument(String productNumber) {
        File pdfFile = new File(FilePaths.BASE_PATH + "report/" + productNumber + "/report.pdf");
        if (Desktop.isDesktopSupported() && pdfFile.exists()) {
            try {
                Desktop.getDesktop().open(pdfFile);
            } catch (IOException e) {
                logger.error("Error opening PDF: {}", e.getMessage());
            }
        } else {
            logger.error("Desktop is not supported on this system.");
        }
        if (isSendingEmail && pdfFile.exists()) {
            GmailService gmailService = null;
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
    }
}