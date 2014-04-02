package ua.com.fielden.platform.mail;

import static java.lang.System.err;
import static java.lang.System.out;

import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.ReadOnlyFolderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.internet.InternetAddress;

public class ReadEmails {

    private void println(final String data) {
        out.println(data);
    }

    public void processMail(final String server, final String user, final String password) {

        Session session = null;
        Store store = null;
        Folder folder = null;
        Message message = null;
        Message[] messages = null;
        Object messagecontentObject = null;
        String sender = null;
        String subject = null;
        Multipart multipart = null;
        Part part = null;
        String contentType = null;

        try {
            println("--------------processing mails started-----------------");
            session = Session.getDefaultInstance(new Properties(), null);

            println("getting the session for accessing email.");
            store = session.getStore("imap");

            store.connect(server, user, password);
            println("Connection established.");

            // Get a handle on the default folder
            folder = store.getDefaultFolder();

            println("Getting the Inbox folder.");

            // Retrieve the "Inbox"
            folder = folder.getFolder("inbox");

            //Reading the Email Index in Read / Write Mode
            folder.open(Folder.READ_WRITE);

            // Retrieve the messages
            messages = folder.getMessages();

            // Loop over all of the messages
            for (int messageNumber = 0; messageNumber < messages.length; messageNumber++) {
                // Retrieve the next message to be read
                message = messages[messageNumber];

                System.out.println("SEEN: " + message.getFlags().contains(Flag.SEEN)); // identifies what emails have been read

                // Retrieve the message content
                messagecontentObject = message.getContent();

                // Determine email type
                if (messagecontentObject instanceof Multipart) {
                    println("Found Email with Attachment");
                    sender = ((InternetAddress) message.getFrom()[0]).getPersonal();

                    // If the "personal" information has no entry, check the address for the sender information
                    println("If the personal information has no entry, check the address for the sender information.");

                    if (sender == null) {
                        sender = ((InternetAddress) message.getFrom()[0]).getAddress();
                        println("sender in NULL. Printing Address:" + sender);
                    }
                    println("Sender -." + sender);

                    // Get the subject information
                    subject = message.getSubject();

                    println("subject=" + subject);

                    // Retrieve the Multipart object from the message
                    multipart = (Multipart) message.getContent();

                    println("Retrieve the Multipart object from the message");

                    // Loop over the parts of the email
                    for (int i = 0; i < multipart.getCount(); i++) {
                        // Retrieve the next part
                        part = multipart.getBodyPart(i);

                        // Get the content type
                        contentType = part.getContentType();

                        // Display the content type
                        println("Content: " + contentType);

                        if (contentType.startsWith("text/plain")) {
                            println("---------reading content type text/plain  mail -------------");
                        } else {
                            // Retrieve the file name
                            final String fileName = part.getFileName();
                            println("Retrieve the fileName = " + fileName);
                        }
                    }
                } else {
                    println("Found Mail Without Attachment");
                    sender = ((InternetAddress) message.getFrom()[0]).getPersonal();

                    // If the "personal" information has no entry, check the address for the sender information
                    println("If the personal information has no entry, check the address for the sender information.");

                    if (sender == null) {
                        sender = ((InternetAddress) message.getFrom()[0]).getAddress();
                        println("sender in NULL. Printing Address:" + sender);
                    }
                    println("Sender:\t" + sender);

                    // Get the subject information
                    subject = message.getSubject();
                    println("subject=" + subject);
                }
            }

            // Close the folder
            folder.close(true);

            // Close the message store
            store.close();
        } catch (final AuthenticationFailedException e) {
            println("Not able to process the mail reading.");
            e.printStackTrace();
        } catch (final FolderClosedException e) {
            println("Not able to process the mail reading.");
            e.printStackTrace();
        } catch (final FolderNotFoundException e) {
            println("Not able to process the mail reading.");
            e.printStackTrace();
        } catch (final NoSuchProviderException e) {
            println("Not able to process the mail reading.");
            e.printStackTrace();
        } catch (final ReadOnlyFolderException e) {
            println("Not able to process the mail reading.");
            e.printStackTrace();
        } catch (final StoreClosedException e) {
            println("Not able to process the mail reading.");
            e.printStackTrace();
        } catch (final Exception e) {
            println("Not able to process the mail reading.");
            e.printStackTrace();
        }
    }

    public static void main(final String... args) {
        if (args.length != 3) {
            err.println("Email server address, user and password are required.");
            System.exit(1);
        }
        new ReadEmails().processMail(args[0], args[1], args[2]);
    }

}