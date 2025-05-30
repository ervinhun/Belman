package dk.easv.belman.bll;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import dk.easv.belman.exceptions.BelmanException;


import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.*;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class GmailService {
    private static final String APPLICATION_NAME = "Belman A/S";
    private static final JsonFactory GSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/gmail.send");
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private final Gmail service;

    public GmailService() throws BelmanException, GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        InputStream in = getClass().getResourceAsStream("/credential.json");
        if (in == null) {
            throw new FileNotFoundException("credential.json not found");
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(GSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, GSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        service = new Gmail.Builder(httpTransport, GSON_FACTORY,
                new com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp(
                        flow, new com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver()).authorize("user"))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public void sendEmailWithAttachment(String userId, String to, String subject,
                                        String bodyText, File file) throws BelmanException, IOException, MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(userId));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setText(bodyText);

        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        attachmentBodyPart.setDataHandler(new DataHandler(new FileDataSource(file)));
        attachmentBodyPart.setFileName(file.getName());

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        multipart.addBodyPart(attachmentBodyPart);
        email.setContent(multipart);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(rawMessageBytes);

        Message message = new Message();
        message.setRaw(encodedEmail);

        service.users().messages().send(userId, message).execute();
    }
}
