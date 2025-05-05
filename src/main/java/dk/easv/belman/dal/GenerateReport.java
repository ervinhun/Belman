package dk.easv.belman.dal;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class GenerateReport {
    private final String productNo;

    public GenerateReport(String productNumber) {
        this.productNo = productNumber;
        String filePath = FilePaths.BASE_PATH + "report/" + productNo + "/";
        Logger logger = Logger.getLogger(GenerateReport.class.getName());
        // Create a new PDF document

        try (PDDocument document = new PDDocument()) {
            ObjectMapper objectMapper = new ObjectMapper();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

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

            // Set font and add title


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

            // Set font and add order number line
            contentStream.newLineAtOffset(centerXDynamic - centerXTitle, -20);
            contentStream.setFont(dynamicFont, productNoFontSize);
            contentStream.showText(dynamicText);
            contentStream.endText();


            // Add body text
            contentStream.beginText();
            contentStream.setFont(dynamicFont, bodyTextFontSize);
            contentStream.newLineAtOffset(100, 620);
            for (String bodyTextLine : bodyText) {
                contentStream.showText(bodyTextLine);
                contentStream.newLineAtOffset(0, -15);
            }
            contentStream.endText();
            float totalHeight = bodyText.size() * bodyTextFontSize + (bodyText.size() - 1) * 15;
            float imageBegginingY = 620 - totalHeight - 20;
            // Add image
            PDImageXObject pdImage = PDImageXObject.createFromFile("src/test/resources/test_image.jpg", document);
            contentStream.drawImage(pdImage, 100, 380, (float) pdImage.getWidth() / 2, (float) pdImage.getHeight() / 2);

            // Add footer
            float footerY = imageBegginingY - pdImage.getHeight() - 20;
            contentStream.beginText();
            contentStream.setFont(dynamicFont, 10);
            contentStream.newLineAtOffset(400, footerY);
            contentStream.showText(signedBy);
            contentStream.newLineAtOffset(50, -15);
            contentStream.showText("Søren");
            contentStream.endText();

            contentStream.close();
            // Save the document
            File targetDir = new File(filePath);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            document.save(filePath + "report.pdf");
            openDocument(productNo);

        } catch (IOException e) {
            logger.severe("Error generating PDF: " + e.getMessage());
        }
    }

    public String getFilePath() {
        return FilePaths.BASE_PATH + "report/" + productNo + "/report.pdf";
    }

    public void openDocument(String productNumber) {
        File pdfFile = new File(FilePaths.BASE_PATH + "report/" + productNumber + "/report.pdf");
        if (Desktop.isDesktopSupported() && pdfFile.exists()) {
            try {
                Desktop.getDesktop().open(pdfFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Desktop is not supported on this system.");
        }
    }

}
