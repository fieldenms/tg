package ua.com.fielden.platform.mail;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.Test;

import static jakarta.mail.Message.RecipientType.*;
import static org.junit.Assert.*;

public class SmtpEmailSenderTest {

    @Test
    public void different_ways_of_specifying_TO_addresses_get_recognised() throws MessagingException {
        final var emailSession = new SmtpEmailSender("locahost").newEmailSession();
        final var message = new MimeMessage(emailSession);
        SmtpEmailSender.assignRecipientAddresses("email1@fielden.com.au;to:email2@fielden.com.au,TO:email3@fielden.com.au,email4@fielden.com.au", message);
        final Address[] toRecipients = message.getRecipients(TO);
        assertEquals(4, toRecipients.length);
        assertEquals("email1@fielden.com.au", toRecipients[0].toString());
        assertEquals("email2@fielden.com.au", toRecipients[1].toString());
        assertEquals("email3@fielden.com.au", toRecipients[2].toString());
        assertEquals("email4@fielden.com.au", toRecipients[3].toString());
    }

    @Test
    public void different_ways_of_specifying_CC_addresses_get_recognised() throws MessagingException {
        final var emailSession = new SmtpEmailSender("locahost").newEmailSession();
        final var message = new MimeMessage(emailSession);
        SmtpEmailSender.assignRecipientAddresses("CC:email1@fielden.com.au;cc:email2@fielden.com.au,Cc:email3@fielden.com.au;cC:email4@fielden.com.au", message);
        final Address[] ccRecipients = message.getRecipients(CC);
        assertEquals(4, ccRecipients.length);
        assertEquals("email1@fielden.com.au", ccRecipients[0].toString());
        assertEquals("email2@fielden.com.au", ccRecipients[1].toString());
        assertEquals("email3@fielden.com.au", ccRecipients[2].toString());
        assertEquals("email4@fielden.com.au", ccRecipients[3].toString());
    }

    @Test
    public void different_ways_of_specifying_BCC_addresses_get_recognised() throws MessagingException {
        final var emailSession = new SmtpEmailSender("locahost").newEmailSession();
        final var message = new MimeMessage(emailSession);
        SmtpEmailSender.assignRecipientAddresses("BCC:email1@fielden.com.au;bcc:email2@fielden.com.au,bCc:email3@fielden.com.au;BcC:email4@fielden.com.au", message);
        final Address[] bccRecipients = message.getRecipients(BCC);
        assertEquals(4, bccRecipients.length);
        assertEquals("email1@fielden.com.au", bccRecipients[0].toString());
        assertEquals("email2@fielden.com.au", bccRecipients[1].toString());
        assertEquals("email3@fielden.com.au", bccRecipients[2].toString());
        assertEquals("email4@fielden.com.au", bccRecipients[3].toString());
    }

    @Test
    public void different_ways_of_specifying_recipient_addresses_get_recognised() throws MessagingException {
        final var emailSession = new SmtpEmailSender("locahost").newEmailSession();
        final var message = new MimeMessage(emailSession);
        SmtpEmailSender.assignRecipientAddresses("to:email1@fielden.com.au;bcc:email2@fielden.com.au,CC:email3@fielden.com.au;email4@fielden.com.au", message);
        final Address[] toRecipients = message.getRecipients(TO);
        assertEquals(2, toRecipients.length);
        assertEquals("email1@fielden.com.au", toRecipients[0].toString());
        assertEquals("email4@fielden.com.au", toRecipients[1].toString());

        final Address[] ccRecipients = message.getRecipients(CC);
        assertEquals(1, ccRecipients.length);
        assertEquals("email3@fielden.com.au", ccRecipients[0].toString());

        final Address[] bccRecipients = message.getRecipients(BCC);
        assertEquals(1, bccRecipients.length);
        assertEquals("email2@fielden.com.au", bccRecipients[0].toString());
    }

}