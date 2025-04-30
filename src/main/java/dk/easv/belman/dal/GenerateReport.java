package dk.easv.belman.dal;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.util.logging.Logger;

public class GenerateReport {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(GenerateReport.class.getName());
        String title = "Quality Check Report";
        String dynamicText = "Order ID: 12345";
        String headText1 = "Customer Name: John Doe";
        String headText2 = "Order Date: 2023-10-01";
        String headText3 = "Delivery Date: 2023-10-05";
        String bodyText1 = "Status: Delivered";
        String bodyText2 = "This is a sample report for a joint quality control document.";
        String bodyText3 = "The report contains information about the quality check of the order.";
        String bodyText4 = "Please find the details below:";
        String signedBy = "Signed by:";
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            // Add logo
            PDImageXObject pdLogo = PDImageXObject.createFromFile("src/main/resources/dk/easv/belman/Images/belman.png", document);
            contentStream.drawImage(pdLogo, 20, 775, (float) pdLogo.getWidth() / 4, (float) pdLogo.getHeight() / 4);

            // Set header
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(450, 810);
            contentStream.showText(headText1);
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText(headText2);
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText(headText3);
            contentStream.endText();

            // Set font and add title

            float titleFontSize = 20;
        PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        float titleWidth = titleFont.getStringWidth(title) / 1000 * titleFontSize;

        // Calculate width of the dynamic text
        float dynamicFontSize = 16;
        PDType1Font dynamicFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        float dynamicWidth = dynamicFont.getStringWidth(dynamicText) / 1000 * dynamicFontSize;

        // Calculate center positions
        PDRectangle mediaBox = page.getMediaBox();
        float centerXTitle = (mediaBox.getWidth() - titleWidth) / 2;
        float centerXDynamic = (mediaBox.getWidth() - dynamicWidth) / 2;

        // Set font and add title
        contentStream.beginText();
        contentStream.setFont(titleFont, titleFontSize);
        contentStream.newLineAtOffset(centerXTitle, 720);
        contentStream.showText(title);

        // Set font and add dynamic text
        contentStream.newLineAtOffset(centerXDynamic - centerXTitle, -20);
        contentStream.setFont(dynamicFont, dynamicFontSize);
        contentStream.showText(dynamicText);
        contentStream.endText();


            // Add body text
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.newLineAtOffset(100, 620);
            contentStream.showText(bodyText1);
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText(bodyText2);
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText(bodyText3);
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText(bodyText4);
            contentStream.endText();

            // Add image
            PDImageXObject pdImage = PDImageXObject.createFromFile("C:\\Users\\ervin\\Documents\\School\\SCO2\\Project\\belman\\src\\test\\resources\\test_image.jpg", document);
            contentStream.drawImage(pdImage, 100, 380, (float) pdImage.getWidth() / 2, (float) pdImage.getHeight() / 2);

            // Add footer
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(400, 100);
            contentStream.showText(signedBy);
            contentStream.endText();

            contentStream.close();
            document.save("output.pdf");
        } catch (IOException e) {
            logger.severe("Error generating PDF: " + e.getMessage());
        }
    }

}
