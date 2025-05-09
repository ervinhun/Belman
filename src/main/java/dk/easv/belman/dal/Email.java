package dk.easv.belman.dal;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Email {


    public static void sendEmail(String subject, String body, String filePath) {
        try {
            if (Desktop.isDesktopSupported()) {
                String subjectEncoded = encode(subject);
                String bodyEncoded = encode(body);
                String uri = String.format("mailto:?subject=%s&body=%s", subjectEncoded, bodyEncoded);

                URI mailto = new URI(uri);
                Desktop.getDesktop().mail(mailto);
            } else {
                Desktop.getDesktop().browse(new URI("https://mail.google.com/mail/u/0/#compose?su=" + subject + "&body=" + body));
            }
            Desktop.getDesktop().open(new File(filePath));
        } catch (Exception e) {
            throw new IllegalArgumentException("Error opening the mail application. " + e);
        }
    }


    public static String encode(String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }
}


