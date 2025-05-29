package dk.easv.belman.dal;

import dk.easv.belman.be.Photo;
import dk.easv.belman.be.PhotoDataForReport;
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


import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;

public class GenerateReport {
    private static final Logger logger = LoggerFactory.getLogger(GenerateReport.class);

    private static OrderManager orderManager;


    private final String productNo;
    private final boolean isSendingEmail;
    private final String email;
    private static final float LINE_SPACING = 15f;
    private static final float IMAGE_SPACING = 20f;
    private static final float ANGLE_LABEL_X_OFFSET = 50f;
    private static final float ANGLE_LABEL_Y_OFFSET = 10f;
    private static final String DATE_FORMAT_PATTERN = "dd/MM/yyyy";


    private static void getOrderManager() {
        if (orderManager == null) {
            try {
                orderManager = new OrderManager();
            } catch (BelmanException e) {
                logger.error("Failed to initialize DALManager: {}", e.getMessage());
            }
        }
    }
    public GenerateReport(String productNumber, User loggedInUser, boolean isSendingEmail, String email) {
        this.productNo = productNumber;
        this.isSendingEmail = isSendingEmail;
        this.email = email;
        float currentYPosition = 0;
        getOrderManager();
        float margin = 40;
        float availableWidth;
        float minY = 100;
        List<Photo> photos = orderManager.getPhotosByONum(productNo);
        if (photos.isEmpty()) {
            logger.error("No images found for product number: {}", productNo);
            return;
        }

        List<PhotoDataForReport> bufferedImages = photos.stream()
                .map(p -> {
                    try {
                                BufferedImage img = ImageIO.read(new ByteArrayInputStream(p.getPhotoFile()));
                                if (img != null)
                                    return new PhotoDataForReport(img, p.getAngle());
                                else {
                                    logger.error("Image is null for angle: {}", p.getAngle());
                                    return null;
                                }
                    } catch (IOException e) {
                        logger.error("Failed to decode image blob for angle {}: {}", p.getAngle(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();


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
            PDType1Font imageAngle = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
            PDType1Font dynamicFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            // Set header
            contentStream.beginText();
            contentStream.setFont(dynamicFont, headerFontSize);
            contentStream.newLineAtOffset(480, 810);
            for (String headTextLine : headText) {
                contentStream.showText(headTextLine);
                contentStream.newLineAtOffset(0, -LINE_SPACING);
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
            currentYPosition = 720 - titleFontSize - IMAGE_SPACING;

            // Set font and add order number line
            contentStream.newLineAtOffset(centerXDynamic - centerXTitle, -IMAGE_SPACING);
            contentStream.setFont(dynamicFont, productNoFontSize);
            contentStream.showText(dynamicText);
            contentStream.endText();
            currentYPosition -= productNoFontSize + IMAGE_SPACING;


            // Add body text
            contentStream.beginText();
            contentStream.setFont(dynamicFont, bodyTextFontSize);
            contentStream.newLineAtOffset(100, currentYPosition);
            for (String bodyTextLine : bodyText) {
                contentStream.showText(bodyTextLine);
                contentStream.newLineAtOffset(0, -LINE_SPACING);
                currentYPosition -= bodyTextFontSize + LINE_SPACING;
            }
            contentStream.endText();

            currentYPosition -= 10;

            // Add image
            for (PhotoDataForReport photoData : bufferedImages) {
                // Create PDImageXObject from BufferedImage
                PDImageXObject pdImage = LosslessFactory.createFromImage(document, photoData.getImage());
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
                // Add image angle
                String angleText = "Angle: " + photoData.getAngle();
                contentStream.beginText();
                contentStream.setFont(imageAngle, 10);
                contentStream.newLineAtOffset(margin + scaledWidth - ANGLE_LABEL_X_OFFSET,
                        currentYPosition - scaledHeight - ANGLE_LABEL_Y_OFFSET);
                contentStream.showText(angleText);
                contentStream.endText();
                // Update current Y position


                currentYPosition -= scaledHeight + IMAGE_SPACING + 20; // spacing between images 10 for font size and 10 more for spacing between the image and the angle
            }
            // Add footer
            final float FONT_SIZE = 10;
            final int NUM_LINES = 3;
            float footerHeight = (FONT_SIZE * NUM_LINES) + (2 * LINE_SPACING);
            LocalDateTime dateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
            String formattedDate = dateTime.format(formatter);
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
            contentStream.newLineAtOffset(50, -LINE_SPACING);
            contentStream.showText(loggedInUser.getFullName());
            contentStream.newLineAtOffset(0, -LINE_SPACING);
            contentStream.showText("Date: " + formattedDate);
            contentStream.endText();

            contentStream.close();
            // Save the document
            PDDocument documentToSave = addPageNumber(document);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            documentToSave.save(outputStream);
            orderManager.savePdfToDb(productNo, outputStream, loggedInUser.getId());
            openDocument(productNo);

            //Updating the rest of the tables on a different thread
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() ->
                    orderManager.signQualityDocument(productNo, loggedInUser.getId())
            );
            executor.shutdown();

        } catch (IOException e) {
            logger.error("I/O error while generating PDF: {}", e.getMessage());
        } catch (IllegalStateException e) {
            logger.error("Illegal state encountered while generating PDF: {}", e.getMessage());
        } catch (SecurityException e) {
            logger.error("Security exception while accessing file system: {}", e.getMessage());
        }
    }


    public void openDocument(String productNumber) {
        new OpenFile(productNumber, isSendingEmail, email);
    }




    private PDDocument addPageNumber(PDDocument document) {
        PDType1Font pageNumberFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        int pageCount = document.getNumberOfPages();

        for (int i = 0; i < pageCount; i++) {
            PDPage currentPage = document.getPage(i);
            String pageNumberText = "Page " + (i + 1) + " of " + pageCount;
            float fontSize = 10;
            try {
                PDPageContentStream pageContentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
                float stringWidth = pageNumberFont.getStringWidth(pageNumberText) / 1000 * fontSize;
                float pageWidth = currentPage.getMediaBox().getWidth();
                float x = (pageWidth - stringWidth) / 2;
                float y = 20; // 20 units from the bottom
                pageContentStream.beginText();
                pageContentStream.setFont(pageNumberFont, fontSize);
                pageContentStream.newLineAtOffset(x, y);
                pageContentStream.showText(pageNumberText);
                pageContentStream.endText();
                pageContentStream.close();
            } catch (IOException e) {
                logger.error("Error drawing the font for page number: {}", e.getMessage());
            }
        }
        return document;
    }
}