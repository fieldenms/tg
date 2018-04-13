package ua.com.fielden.platform.mail;

import static ua.com.fielden.platform.types.try_wrapper.TryWrapper.Try;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.mail.exceptions.EmailException;
import ua.com.fielden.platform.types.tuples.T2;

/**
 * A utility class for sending emails via SMTP.
 * Public methods in this class can be used to send email in HTML or plain format with/o attachments.
 *
 * @author TG Team
 *
 */
public class SmtpEmailSender {

    private static enum EmailType {
        PLAIN {
            @Override
            public void setBodyText(final MimeMessage msg, final String body) throws Exception {
                msg.setText(body);
            }

            @Override
            public void setBodyText(final BodyPart bodyPart, final String body) throws Exception {
                bodyPart.setText(body + "\n\n");

            }

            @Override
            public String alterBody(final String body, final Stream<T2<Optional<File>, String>> optionalT2Stream1) {
                final StringBuilder bodySuffix = new StringBuilder();
                // Process missing files and append their names to body
                optionalT2Stream1.filter(t2 -> !t2._1.isPresent()).forEach(t2 -> bodySuffix.append(t2._2).append("\n"));
                if (bodySuffix.length() > 0) {
                    return new StringBuilder()
                            .append(body)
                            .append("\n\n")
                            .append(StringUtils.repeat("-", 80))
                            .append("\n\n")
                            .append(UNABLE_TO_ATTACH_FILES)
                            .append("\n\n")
                            .append(bodySuffix).toString();
                } else {
                    return body;
                }
            }
        },

        HTML {
            @Override
            public void setBodyText(final MimeMessage msg, final String body) throws Exception {
                msg.setContent(body, "text/html");
            }

            @Override
            public void setBodyText(final BodyPart bodyPart, final String body) throws Exception {
                bodyPart.setContent(body, "text/html");

            }

            @Override
            public String alterBody(final String body, final Stream<T2<Optional<File>, String>> optionalT2Stream1) {
                final StringBuilder bodySuffix = new StringBuilder();
                // Process missing files and append their names to body
                optionalT2Stream1.filter(t2 -> !t2._1.isPresent()).forEach(t2 -> bodySuffix.append(t2._2).append("<br>"));
                if (bodySuffix.length() > 0) {
                    return new StringBuilder()
                            .append(body)
                            .append("<br><hr><br>")
                            .append(UNABLE_TO_ATTACH_FILES)
                            .append("<br><br>")
                            .append(bodySuffix).toString();
                } else {
                    return body;
                }
            }

        };

        private static final String UNABLE_TO_ATTACH_FILES = "Unable to attach the following file(s):";

        public abstract void setBodyText(final MimeMessage msg, final String body) throws Exception;

        public abstract void setBodyText(final BodyPart bodyPart, final String body) throws Exception;

        public abstract String alterBody(final String body, final Stream<T2<Optional<File>, String>> optionalT2Stream1);
    }

    private final Logger logger = Logger.getLogger(SmtpEmailSender.class);
    private final String host;

    public SmtpEmailSender(final String host) {
        this.host = host;
    }

    private Session newEmailSession() {
        final Properties props = new Properties();
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
     * @param filePaths
     */
    public void sendPlainMessageWithAttachments(
            final String fromAddress,
            final String csvToAddresses,
            final String subject,
            final String body,
            final Path... filePaths) {
        if (filePaths.length == 0) {
            throw new EmailException("At least one attachment is expected.");
        }

        final T2<String, Stream<T2<File, String>>> t2 = preProcessAttachments(EmailType.PLAIN, body, filePaths);
        sendPlainMessageWithAttachments(fromAddress, csvToAddresses, subject, t2._1, t2._2);
    }

    /**
     * Sends a plain text email with attachments.
     *
     * @param fromAddress
     * @param csvToAddresses
     * @param subject
     * @param body
     * @param coAttachment
     * @param attachments
     */
    public void sendPlainMessageWithAttachments(
            final String fromAddress,
            final String csvToAddresses,
            final String subject,
            final String body,
            final IAttachment coAttachment,
            final Attachment... attachments) {
        if (attachments.length == 0) {
            throw new EmailException("At least one attachment is expected.");
        }

        final T2<String, Stream<T2<File, String>>> t2 = preProcessAttachments(EmailType.PLAIN, body, coAttachment, attachments);
        sendPlainMessageWithAttachments(fromAddress, csvToAddresses, subject, t2._1, t2._2);
    }

    /**
     * Sends a HTML text email with attachments.
     *
     * @param fromAddress
     * @param csvToAddresses
     * @param subject
     * @param body
     * @param filePaths
     */
    public void sendHtmlMessageWithAttachments(
            final String fromAddress,
            final String csvToAddresses,
            final String subject,
            final String body,
            final Path... filePaths) {
        if (filePaths.length == 0) {
            throw new EmailException("At least one attachment is expected.");
        }

        final T2<String, Stream<T2<File, String>>> t2 = preProcessAttachments(EmailType.HTML, body, filePaths);
        sendHtmlMessageWithImagesAndAttachments(fromAddress, csvToAddresses, subject, t2._1, new Path[] {}, t2._2);
    }

    /**
     * Sends a HTML text email with attachments.
     *
     * @param fromAddress
     * @param csvToAddresses
     * @param subject
     * @param body
     * @param attachments
     */
    public void sendHtmlMessageWithAttachments(
            final String fromAddress,
            final String csvToAddresses,
            final String subject,
            final String body,
            final IAttachment coAttachment,
            final Attachment... attachments) {
        if (attachments.length == 0) {
            throw new EmailException("At least one attachment is expected.");
        }

        final T2<String, Stream<T2<File, String>>> t2 = preProcessAttachments(EmailType.HTML, body, coAttachment, attachments);
        sendHtmlMessageWithImagesAndAttachments(fromAddress, csvToAddresses, subject, t2._1, new Path[] {}, t2._2);
    }

    /**
     * Sends a HTML text email with embedded images and attachments.
     *
     * @param fromAddress
     * @param csvToAddresses
     * @param subject
     * @param body
     * @param imagePaths
     * @param coAttachment
     * @param attachments
     */
    public void sendHtmlMessageWithImagesAndAttachments(
            final String fromAddress,
            final String csvToAddresses,
            final String subject,
            final String body,
            final Path[] imagePaths,
            final IAttachment coAttachment,
            final Attachment... attachments) {
        if (imagePaths.length == 0) {
            throw new EmailException("At least one image is expected.");
        }

        if (attachments.length == 0) {
            throw new EmailException("At least one attachment is expected.");
        }

        final T2<String, Stream<T2<File, String>>> t2 = preProcessAttachments(EmailType.HTML, body, coAttachment, attachments);
        sendHtmlMessageWithImagesAndAttachments(fromAddress, csvToAddresses, subject, t2._1, imagePaths, t2._2);
    }

    /**
     * Sends a HTML text email with embedded images and attachments.
     *
     * @param fromAddress
     * @param csvToAddresses
     * @param subject
     * @param body
     * @param imagePaths
     * @param filePaths
     */

    public void sendHtmlMessageWithImagesAndAttachments(
            final String fromAddress,
            final String csvToAddresses,
            final String subject,
            final String body,
            final Path[] imagePaths,
            final Path... filePaths) {
        if (imagePaths.length == 0) {
            throw new EmailException("At least one image is expected.");
        }
        if (filePaths.length == 0) {
            throw new EmailException("At least one attachment is expected.");
        }

        final T2<String, Stream<T2<File, String>>> t2 = preProcessAttachments(EmailType.HTML, body, filePaths);
        sendHtmlMessageWithImagesAndAttachments(fromAddress, csvToAddresses, subject, t2._1, imagePaths, t2._2);
    }


    private void sendMessage(
            final String fromAddress,
            final String csvToAddresses,
            final String subject,
            final String body,
            final EmailType type) {
        try {
            final Session session = newEmailSession();
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

    /**
     * This
     *
     * @param fromAddress
     * @param csvToAddresses
     * @param subject
     * @param body
     * @param type
     * @param attachments
     */
    private void sendPlainMessageWithAttachments(
            final String fromAddress,
            final String csvToAddresses,
            final String subject,
            final String body,
            final Stream<T2<File, String>> attachments) {
        try {
            final Session session = newEmailSession();
            final MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            assignToAddresses(csvToAddresses, message);
            message.setSubject(subject);

            message.setContent(buildPlainMultipart(body, attachments));
            message.setSentDate(new Timestamp(System.currentTimeMillis()));
            message.saveChanges();
            Transport.send(message);
        } catch (final Exception ex) {
            logger.error("Error during email sending.", ex);
            throw new EmailException("Error during email sending.", ex);
        }

    }

    private void sendHtmlMessageWithImagesAndAttachments(
            final String fromAddress,
            final String csvToAddresses,
            final String subject,
            final String body,
            final Path[] imagePaths,
            final Stream<T2<File, String>> attachments) {
        try {
            final Session session = newEmailSession();
            final MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            assignToAddresses(csvToAddresses, message);
            message.setSubject(subject);

            // add everything to the email
            message.setContent(buildHtmlMultipart(body, imagePaths, attachments));
            message.setSentDate(new Timestamp(System.currentTimeMillis()));
            message.saveChanges();
            Transport.send(message);
        } catch (final Exception ex) {
            logger.error("Error during email sending.", ex);
            throw new EmailException("Error during email sending.", ex);
        }
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

    private static void addImagesInline(final Multipart parent, final Path[] images) throws MessagingException {
        for (final Path image : images) {
            final BodyPart imagePart = new MimeBodyPart();
            final File file = image.toFile();
            final DataSource source = new FileDataSource(file);
            imagePart.setDataHandler(new DataHandler(source));
            imagePart.setFileName(file.getName());
            // Gmail requires the cid have <> around it
            imagePart.setHeader("Content-ID", "<" + file.getName() + ">");
            imagePart.setDisposition(BodyPart.INLINE);
            parent.addBodyPart(imagePart);
        }
    }

    /**
     * This method assumes that attachments have already been pre-validated and
     * only valid files are passed in.
     *
     * @param parent
     * @param attachments
     * @throws MessagingException
     */
    private static void addAttachments(final Multipart parent, final Stream<T2<File, String>> attachments) {
        attachments.forEach(t2 -> {
            final MimeBodyPart mbpAttachment = new MimeBodyPart();
            final DataSource source = new FileDataSource(t2._1);
            Try(() -> {
                mbpAttachment.setDataHandler(new DataHandler(source));
                mbpAttachment.setDisposition(BodyPart.ATTACHMENT);
                mbpAttachment.setFileName(t2._2);
                parent.addBodyPart(mbpAttachment);
            });
        });
    }

    private static Multipart newChild(final Multipart parent, final String subType) throws MessagingException {
        final MimeMultipart child = new MimeMultipart(subType);
        final MimeBodyPart mbp = new MimeBodyPart();
        parent.addBodyPart(mbp);
        mbp.setContent(child);
        return child;
    }

    private static void addHtmlVersion(final Multipart parent, final String messageHtml, final Path[] images) throws MessagingException {
        // HTML version
        final Multipart mpRelated = newChild(parent, "related");

        // Html
        final MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(messageHtml, "text/html; charset=\"utf-8\"");
        mpRelated.addBodyPart(htmlPart);

        // Inline images
        addImagesInline(mpRelated, images);
    }

    private T2<String, Stream<T2<File, String>>> preProcessAttachments(final EmailType emailType, final String body, final Stream<T2<Optional<File>, String>> optionalT2Stream1, final Stream<T2<Optional<File>, String>> optionalT2Stream2) {
        // alteredBody, which are present
        final String alteredBody = emailType.alterBody(body, optionalT2Stream1);
        // Stream files, which are present
        final Stream<T2<File, String>> fileStream = optionalT2Stream2.filter(t2 -> t2._1.isPresent()).map(t2 -> new T2<>(t2._1.get(), t2._2));
        return new T2<>(alteredBody.toString(), fileStream);
    }

    private Stream<T2<Optional<File>, String>> makeOptionalT2Stream(final IAttachment coAttachment, final Attachment[] attachments) {
        return Stream.of(attachments).map(attachment -> new T2<>(coAttachment.asFile(attachment), attachment.getOrigFileName()));
    }

    private Stream<T2<Optional<File>, String>> makeOptionalT2Stream(final Path[] filePaths) {
        return Stream.of(filePaths).map(path -> {
            final File file = path.toFile();
            if (file.exists() && file.canRead()) {
                return new T2<>(Optional.of(file), file.getName());
            } else {
                return new T2<Optional<File>, String>(Optional.empty(), file.getName());
            }
        });
    }

    private T2<String, Stream<T2<File, String>>> preProcessAttachments(final EmailType emailType, final String body, final IAttachment coAttachment, final Attachment[] attachments) {
        return preProcessAttachments(emailType, body, makeOptionalT2Stream(coAttachment, attachments), makeOptionalT2Stream(coAttachment, attachments));
    }

    private T2<String, Stream<T2<File, String>>> preProcessAttachments(final EmailType emailType, final String body, final Path[] filePaths) {
        return preProcessAttachments(emailType, body, makeOptionalT2Stream(filePaths), makeOptionalT2Stream(filePaths));
    }

    public static Multipart buildHtmlMultipart(final String messageHtml, final Path[] images, final Stream<T2<File, String>> attachments) throws MessagingException {
        final Multipart mpMixed = new MimeMultipart("mixed");
        // HTML version only
        addHtmlVersion(mpMixed, messageHtml, images);
        // attachments
        addAttachments(mpMixed, attachments);
        return mpMixed;
    }

    public static Multipart buildPlainMultipart(final String messageText, final Stream<T2<File, String>> attachments) throws MessagingException {
        final Multipart mpMixed = new MimeMultipart("mixed");
        // Plain text version only
        addPlainVersion(mpMixed, messageText);
        // attachments
        addAttachments(mpMixed, attachments);
        return mpMixed;
    }

    private static void addPlainVersion(final Multipart parent, final String messageText) throws MessagingException {
        final MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(messageText, "text/plain; charset=\"utf-8\"");
        parent.addBodyPart(textPart);
    }


    public static void main(final String[] args) {
        final SmtpEmailSender sender = new SmtpEmailSender("192.168.1.8");
        final Path path1 = Paths.get(".classpath");
        final Path path2 = Paths.get(".project");
        final Path path3 = Paths.get("desktop-script.sh");
        final Path path4 = Paths.get("mobile-script.sh");

        final Path pathImage = Paths.get("tg-logo.png");
        final String plainBody = "Hello,\n\nHow are you?\n\nRegards,\nSmtpEmailSender";
        final String imageHtml = "<img src=\"cid:tg-logo.png\" height=\"100\" width=\"100\"><br>";
        final String htmlBodyTemplate = "<html><body>%s" +
                "<table><tr><td>Services:</td><td>&nbsp;</td></tr><tr><td><b>AA23ILS</b></td><td>ILS, Runway 23, Auckland</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td></tr><tr><td>Description</td><td>AA23ILS down</td></tr><tr><td>Subsystem</td><td>AFL</td></tr><tr><td>Location</td><td>AA</td></tr><tr><td>Transaction date/time&nbsp;&nbsp;</td><td>10/04/2018 15:00</td></tr><tr><td>WA Type</td><td>FT</td></tr><tr><td>Manager</td><td>Tim Martin</td></tr><tr><td>Team</td><td>SOE&M</td></tr><tr><td>Technician</td><td>Katherine Kaynes</td></tr></table></body></html>";

        final String htmlBodyWithImage = String.format(htmlBodyTemplate, imageHtml);
        final String htmlBodyWithoutImage = String.format(htmlBodyTemplate, "");
        final Path[] imagePaths = new Path[1];
        imagePaths[0] = pathImage;
        sender.sendHtmlMessageWithAttachments("oles@fielden.com.au", "oles@fielden.com.au", "HtmlMessageWithAttachments", htmlBodyWithoutImage, path1, path2, path3, path4);
        sender.sendHtmlMessageWithImagesAndAttachments("oles@fielden.com.au", "oles@fielden.com.au", "HtmlMessageWithImagesAndAttachments", htmlBodyWithImage, imagePaths, path1, path2, path3, path4);
        sender.sendPlainMessageWithAttachments("oles@fielden.com.au", "oles@fielden.com.au", "PlainMessageWithAttachments", plainBody, path1, path2, path3, path4);
//        sender.sendPlainMessageWithAttachments("oles@fielden.com.au", "oles.hodych@gmail.com", "Plain text with text mime type", "Plain text, but HTML mime type", path1, path2);
//        sender.sendHtmlMessageWithAttachments("oles@fielden.com.au", "oles.hodych@gmail.com ", "Html text with HTML mime type", "Html text, but HTML mime type</br></br>", path1, path2);
//        sender.sendHtmlMessage("oles@fielden.com.au", "oles@fielden.com.au  ", "Plain text with HTML mime type", "Plain text, but HTML mime type");
//        sender.sendPlainMessage("oles@fielden.com.au", "oles@fielden.com.au", "HTML text with TXT mime type", "<html>Please open the <a href='https://tgdev.com:8092/login'>link</a> to reset you password.</html>");
//        sender.sendHtmlMessage("oles@fielden.com.au", "oles@fielden.com.au", "HTML text with HTML mime type, not <html> block", "Please open the <a href='https://tgdev.com:8092/login'>link</a> to reset you password.");
//        sender.sendHtmlMessage("oles@fielden.com.au", "oles@fielden.com.au", "HTML text with HTML mime type", "<html>Please open the <a href='https://tgdev.com:8092/login'>link</a> to reset you password.</html>");
    }

}
