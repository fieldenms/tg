package ua.com.fielden.platform.mail;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.mail.exceptions.EmailException;

public class SendEmails {

    private static enum EmailType {
        PLAIN {
            @Override
            public void setBodyText(MimeMessage msg, String body) throws Exception {
                msg.setText(body);
            }

            @Override
            public void setBodyText(BodyPart bodyPart, String body) throws Exception {
                bodyPart.setText(body);
                
            }
        }, 
        
        HTML {
            @Override
            public void setBodyText(MimeMessage msg, String body) throws Exception {
                msg.setContent(body, "text/html");
            }

            @Override
            public void setBodyText(BodyPart bodyPart, String body) throws Exception {
                bodyPart.setContent(body, "text/html");
                
            }
        };
        
        public abstract void setBodyText(final MimeMessage msg, final String body) throws Exception;
        public abstract void setBodyText(final BodyPart bodyPart, final String body) throws Exception;
    }
    
    private final Logger logger = Logger.getLogger(SendEmails.class);

    public Session getNewMailSession() {
        final Properties props = new Properties();
        final String host = "192.168.1.8";
        props.put("mail.smtp.host", host);
        final Session session = Session.getDefaultInstance(props, null);
        return session;
    }

    /**
     * Sends a plain text email with no attachments.
     * 
     * @param fromAddress
     * @param csvToAddresses
     * @param subject
     * @param body
     */
    public void sendPlainMessage(
            final String fromAddress, 
            final String csvToAddresses, 
            final String subject, 
            final String body) {
        sendMessage(fromAddress, csvToAddresses, subject, body, EmailType.PLAIN);
    }

    /**
     * Sends a HTML text email with no attachments.
     * 
     * @param fromAddress
     * @param csvToAddresses
     * @param subject
     * @param body
     */
    public void sendHtmlMessage(
            final String fromAddress, 
            final String csvToAddresses, 
            final String subject, 
            final String body) {
        sendMessage(fromAddress, csvToAddresses, subject, body, EmailType.HTML);
    }
    
    /**
     * Sends a plain text email with attachments.
     * 
     * @param fromAddress
     * @param csvToAddresses
     * @param subject
     * @param body
     * @param filePathsToAttach
     */
    public void sendPlainMessageWithAttachments(
            final String fromAddress, 
            final String csvToAddresses, 
            final String subject, 
            final String body, 
            final Path... filePathsToAttach) {
        sendMessageWithAttachments(fromAddress, csvToAddresses, subject, body, EmailType.PLAIN, filePathsToAttach);
    }

    /**
     * Sends a HTML text email with attachments.
     * 
     * @param fromAddress
     * @param csvToAddresses
     * @param subject
     * @param body
     * @param filePathsToAttach
     */
    public void sendHtmlMessageWithAttachments(
            final String fromAddress, 
            final String csvToAddresses, 
            final String subject, 
            final String body, 
            final Path... filePathsToAttach) {
        sendMessageWithAttachments(fromAddress, csvToAddresses, subject, body, EmailType.HTML, filePathsToAttach);
    }

    private void sendMessage(
            final String fromAddress, 
            final String csvToAddresses, 
            final String subject, 
            final String body,
            final EmailType type) {
        try {
            final Session session = getNewMailSession();
            final MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            assignToAddresses(csvToAddresses, message);
            message.setSubject(subject);
            type.setBodyText(message, body);
            message.setSentDate(new Timestamp(System.currentTimeMillis()));
            Transport.send(message);
        } catch (final Exception ex) {
            logger.error("Error during email sending.", ex);
            throw new EmailException("Error during email sending.", ex);
        }
    }
    
    private void sendMessageWithAttachments(
            final String fromAddress, 
            final String csvToAddresses, 
            final String subject, 
            final String body, 
            final EmailType type,
            final Path... filePathsToAttach) {
        if (filePathsToAttach.length == 0) {
            throw new EmailException("At least one attachment is expected.");
        }
        
        try {
            final Session session = getNewMailSession();
            final MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            assignToAddresses(csvToAddresses, message);
            message.setSubject(subject);

            // create the main message part
            final BodyPart mainBodyPart = new MimeBodyPart();
            type.setBodyText(mainBodyPart, body);
            
            // add everything to the email
            message.setContent(handleAttachments(body, mainBodyPart, filePathsToAttach), "multipart/mixed");
            message.setSentDate(new Timestamp(System.currentTimeMillis()));
            message.saveChanges();
            Transport.send(message);
        } catch (final Exception ex) {
            logger.error("Error during email sending.", ex);
            throw new EmailException("Error during email sending.", ex);
        }
    }

    private Multipart handleAttachments(final String body, final BodyPart mainBodyPart, final Path[] filePathsToAttach) throws Exception {
        final List<BodyPart> bodyParts = new ArrayList<BodyPart>();
        boolean relatedParts = false;

        // create attachments
        final String trimmedLowercaseBoddy = body.trim().toLowerCase();
        for (final Path attachment : filePathsToAttach) {
            final MimeBodyPart messageBodyPart = new MimeBodyPart();
            final File file = attachment.toFile();
            messageBodyPart.attachFile(file);

            if (trimmedLowercaseBoddy.contains("cid:" + file.getName())) {
                messageBodyPart.setHeader("Content-ID", file.getName());
                relatedParts = true;
            }

            bodyParts.add(messageBodyPart);
        }

        final Multipart multipart = relatedParts ? new MimeMultipart("related") : new MimeMultipart();
        multipart.addBodyPart(mainBodyPart);
        for (final BodyPart bodyPart : bodyParts) {
            multipart.addBodyPart(bodyPart);
        }

        return multipart;
    }
    
    /**
     * A helper method to process and assign the TO addresses.
     * 
     * @param csvToAddresses
     * @param message
     * @throws MessagingException
     * @throws AddressException
     */
    private void assignToAddresses(final String csvToAddresses, final MimeMessage message) throws MessagingException, AddressException {
        final String[] toAddresses = csvToAddresses.trim().split("[,;]");
        for (final String toAddresse : toAddresses) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddresse));
        }
    }

    
    public static void main(String[] args) {
        final SendEmails sender = new SendEmails();
        Path path1 = Paths.get("pom.xml");
        Path path2 = Paths.get("desktop-script.sh");
        System.out.println(path1.toFile().exists());
        sender.sendPlainMessageWithAttachments("oles@fielden.com.au", "\toles@fielden.com.au  ", "Plain text with text mime type", "Plain text, but HTML mime type", path1, path2);
        sender.sendHtmlMessageWithAttachments("oles@fielden.com.au", "\toles@fielden.com.au  ", "Html text with HTML mime type", "Html text, but HTML mime type", path1, path2);
        sender.sendHtmlMessage("oles@fielden.com.au", "oles@fielden.com.au  ", "Plain text with HTML mime type", "Plain text, but HTML mime type");
        sender.sendPlainMessage("oles@fielden.com.au", "oles@fielden.com.au", "HTML text with TXT mime type", "<html>Please open the <a href='https://tgdev.com:8092/login'>link</a> to reset you password.</html>");
        sender.sendHtmlMessage("oles@fielden.com.au", "oles@fielden.com.au", "HTML text with HTML mime type, not <html> block", "Please open the <a href='https://tgdev.com:8092/login'>link</a> to reset you password.");
        sender.sendHtmlMessage("oles@fielden.com.au", "oles@fielden.com.au", "HTML text with HTML mime type", "<html>Please open the <a href='https://tgdev.com:8092/login'>link</a> to reset you password.</html>");
    }

}
