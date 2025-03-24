package ua.com.fielden.platform.attachment;

import static org.assertj.core.api.Assertions.assertThat;
import com.google.inject.name.Named;
import jakarta.inject.Inject;
import org.dataloader.impl.Assertions;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

/**
 * A test case validating basic operations of attachments such as uploading and deletion.
 * 
 * @author TG Team
 * 
 */
public class AttachmentOperationsTest extends AbstractDaoTestCase {

    @Inject @Named("attachments.location")
    private String attachmentsLocation;
    @Inject @Named("attachments.allowlist")
    private String attachmentsAllowlist;

    private final String plainTextFileName = "!readme.txt";
    private final String phpTextFileName = "exploit.php";
    private final String docsFileName = "document.docx";
    private final String pdfFileName = "document.pdf";

    @After
    public void tearDown() {
        final Path fileToUpload = Paths.get(attachmentsLocation + File.separator + plainTextFileName);
    }

    @Test
    public void allowlist_is_formed_correctly_from_application_properties() {
        final AttachmentUploaderDao co = co(AttachmentUploader.class);

        assertThat(co.attachmentsAllowlist)
                .hasSize(3)
                .contains("text/plain", "application/pdf", "application/x-tika-ooxml");
    }

    @Test
    public void attachment_instance_is_created_as_the_result_of_successful_upload_of_word_document() throws IOException {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);

        final Path fileToUpload = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + docsFileName);
        assertTrue(fileToUpload.toFile().exists());
        assertTrue(fileToUpload.toFile().canRead());

        final Attachment attachment = upload(coAttachmentUploader, fileToUpload, docsFileName);

        assertNotNull(attachment);
        assertTrue(attachment.isPersisted());
        assertNotNull(attachment.getSha1());

        final Path uploadedFile = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + attachment.getSha1());
        assertTrue(uploadedFile.toFile().exists());
        assertTrue(uploadedFile.toFile().canRead());

        // clean up by deleting the just uploaded file
        assertTrue(Files.deleteIfExists(uploadedFile));
    }

    @Test
    public void attachment_instance_is_created_as_the_result_of_successful_upload_of_pdf_document() throws IOException {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);

        final Path fileToUpload = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + pdfFileName);
        assertTrue(fileToUpload.toFile().exists());
        assertTrue(fileToUpload.toFile().canRead());

        final Attachment attachment = upload(coAttachmentUploader, fileToUpload, pdfFileName);

        assertNotNull(attachment);
        assertTrue(attachment.isPersisted());
        assertNotNull(attachment.getSha1());

        final Path uploadedFile = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + attachment.getSha1());
        assertTrue(uploadedFile.toFile().exists());
        assertTrue(uploadedFile.toFile().canRead());

        // clean up by deleting the just uploaded file
        assertTrue(Files.deleteIfExists(uploadedFile));
    }

    @Test
    public void php_files_are_not_recognised_as_pain_text_and_cannot_be_attached() throws IOException {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);

        final Path fileToUpload = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + phpTextFileName);
        assertTrue(fileToUpload.toFile().exists());
        assertTrue(fileToUpload.toFile().canRead());

        assertThatThrownBy(() -> upload(coAttachmentUploader, fileToUpload, phpTextFileName))
                .isInstanceOf(Result.class)
                .hasMessage("Files of type [text/x-php] are not supported.");
    }

    @Test
    public void attachment_instance_is_created_as_the_result_of_successful_stream_upload() throws IOException {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);

        final Attachment attachment = coAttachmentUploader.save((AttachmentUploader) new_(AttachmentUploader.class)
                .setOrigFileName("readme.txt")
                .setInputStream(new ByteArrayInputStream("some data".getBytes()))).getKey();

        assertNotNull(attachment);
        assertTrue(attachment.isPersisted());
        assertNotNull(attachment.getSha1());

        final Path uploadedFile = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + attachment.getSha1());
        assertTrue(uploadedFile.toFile().exists());
        assertTrue(uploadedFile.toFile().canRead());

        // let's do clean up by deleting just uploaded file
        assertTrue(Files.deleteIfExists(uploadedFile));
    }

    @Test
    public void uploading_the_same_attachment_in_different_transactions_multiple_times_succeeds_without_any_data_duplication() throws IOException {
        Path uploadedFile = null;
        try {
            final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);

            final AttachmentUploader newUpload1 = (AttachmentUploader) new_(AttachmentUploader.class)
                    .setOrigFileName("readme.txt")
                    .setInputStream(new ByteArrayInputStream("some data".getBytes()));
            final Attachment attachment1 = coAttachmentUploader.save(newUpload1).getKey();

            final AttachmentUploader newUpload2 = (AttachmentUploader) new_(AttachmentUploader.class)
                    .setOrigFileName("readme.txt")
                    .setInputStream(new ByteArrayInputStream("some data".getBytes()));
            final Attachment attachment2 = coAttachmentUploader.save(newUpload2).getKey();

            final AttachmentUploader newUpload3 = (AttachmentUploader) new_(AttachmentUploader.class)
                    .setOrigFileName("readme.txt")
                    .setInputStream(new ByteArrayInputStream("some data".getBytes()));
            final Attachment attachment3 = coAttachmentUploader.save(newUpload3).getKey();

            assertNotNull(attachment1);
            assertNotNull(attachment2);
            assertNotNull(attachment3);
            assertTrue(attachment1.isPersisted());
            assertTrue(attachment2.isPersisted());
            assertTrue(attachment3.isPersisted());
            assertEquals(attachment1.getId(), attachment2.getId());
            assertEquals(attachment2.getId(), attachment3.getId());

            uploadedFile = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + attachment1.getSha1());
            assertTrue(uploadedFile.toFile().exists());
            assertTrue(uploadedFile.toFile().canRead());
        } finally {
            // let's do clean up by deleting just uploaded file
            if (uploadedFile != null) {
                assertTrue(Files.deleteIfExists(uploadedFile));
            }
        }
    }

    @Test
    @SessionRequired
    public void uploading_the_same_attachment_in_a_single_transactions_multiple_times_failes_due_to_transaction_rollback() throws IOException {
        Path uploadedFile = null;
        Attachment attachment1 = null;
        try {
            final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);

            final AttachmentUploader newUpload1 = (AttachmentUploader) new_(AttachmentUploader.class)
                    .setOrigFileName("readme.txt")
                    .setInputStream(new ByteArrayInputStream("some data".getBytes()));
            attachment1 = coAttachmentUploader.save(newUpload1).getKey();
            uploadedFile = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + attachment1.getSha1());
            assertTrue(uploadedFile.toFile().exists());
            assertTrue(uploadedFile.toFile().canRead());

            final AttachmentUploader newUpload2 = (AttachmentUploader) new_(AttachmentUploader.class)
                    .setOrigFileName("readme.txt")
                    .setInputStream(new ByteArrayInputStream("some data".getBytes()));
            coAttachmentUploader.save(newUpload2).getKey();
            fail("Attempts to save a duplicate attachment in the same transaction should have failed due to the transaction rollback.");
        } catch (final Result ex) {
            assertEquals("Attachment [readme.txt | SHA1: BAF34551FECB48ACC3DA868EB85E1B6DAC9DE356] could not be located.", ex.getMessage());
        } finally {
            // let's do clean up by deleting just uploaded file
            if (uploadedFile != null) {
                assertTrue(Files.deleteIfExists(uploadedFile));
            }
        }
        assertFalse("Attachment should have been rolled back.", co(Attachment.class).entityExists(attachment1));
    }

    @Test
    public void uploading_of_the_same_file_with_different_names_results_in_several_attachments_but_only_one_file_associated_with_them() throws IOException {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);
        
        final String fileNameToUpload = "!readme.txt"; 
        final Path fileToUpload = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + fileNameToUpload);
        assertTrue(fileToUpload.toFile().exists());
        assertTrue(fileToUpload.toFile().canRead());
        
        final Attachment attachment1 = upload(coAttachmentUploader, fileToUpload, "readme1.txt");
        final Attachment attachment2 = upload(coAttachmentUploader, fileToUpload, "readme2.txt");
        
        assertNotNull(attachment1);
        assertTrue(attachment1.isPersisted());
        assertNotNull(attachment2);
        assertTrue(attachment2.isPersisted());
        assertNotEquals(attachment1, attachment2);
        assertEquals(attachment1.getSha1(), attachment2.getSha1());
        
        final Path uploadedFile = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + attachment1.getSha1());
        assertTrue(uploadedFile.toFile().exists());
        assertTrue(uploadedFile.toFile().canRead());

        // let's do clean up by deleting just uploaded file
        assertTrue(Files.deleteIfExists(uploadedFile));
    }

    @Test
    public void deleting_attachment_deletes_associated_file_if_there_are_no_other_associations_with_that_file() throws IOException {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);
        
        final String fileNameToUpload = "!readme.txt"; 
        final Path fileToUpload = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + fileNameToUpload);
        assertTrue(fileToUpload.toFile().exists());
        assertTrue(fileToUpload.toFile().canRead());
        
        final Attachment attachment = upload(coAttachmentUploader, fileToUpload, "readme.txt");

        final Path uploadedFile = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + attachment.getSha1());
        assertTrue(uploadedFile.toFile().exists());
        assertTrue(uploadedFile.toFile().canRead());

        co(Attachment.class).delete(attachment);
        assertFalse(co(Attachment.class).entityExists(attachment));
        assertFalse(uploadedFile.toFile().exists());
    }

    @Test
    public void deleting_attachment_that_is_associated_with_a_file_which_is_associated_with_another_attachment_does_not_delete_that_file() throws IOException {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);
        
        final String fileNameToUpload = "!readme.txt"; 
        final Path fileToUpload = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + fileNameToUpload);
        assertTrue(fileToUpload.toFile().exists());
        assertTrue(fileToUpload.toFile().canRead());
        
        final Attachment attachment1 = upload(coAttachmentUploader, fileToUpload, "readme1.txt");
        final Attachment attachment2 = upload(coAttachmentUploader, fileToUpload, "readme2.txt");

        co(Attachment.class).delete(attachment1);
        assertFalse(co(Attachment.class).entityExists(attachment1));
        
        final Path uploadedFile = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + attachment1.getSha1());
        assertTrue(uploadedFile.toFile().exists());
        assertTrue(uploadedFile.toFile().canRead());

        co(Attachment.class).delete(attachment2);
        assertFalse(co(Attachment.class).entityExists(attachment2));
        assertFalse(uploadedFile.toFile().exists());
    }

    @Test
    public void deleting_attachment_with_missing_file_is_supported() throws IOException {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);
        
        final String fileNameToUpload = "!readme.txt"; 
        final Path fileToUpload = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + fileNameToUpload);
        assertTrue(fileToUpload.toFile().exists());
        assertTrue(fileToUpload.toFile().canRead());
        
        final Attachment attachment = upload(coAttachmentUploader, fileToUpload, "readme.txt");
        final Path uploadedFile = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + attachment.getSha1());
        assertTrue(uploadedFile.toFile().exists());
        assertTrue(uploadedFile.toFile().canRead());
        
        // let's delete the file to make attachment an orphan
        assertTrue(Files.deleteIfExists(uploadedFile));
        assertFalse(uploadedFile.toFile().exists());

        co(Attachment.class).delete(attachment);
        assertFalse(co(Attachment.class).entityExists(attachment));
    }

    @Test
    public void batch_deletion_of_attachments_by_IDs_is_supported() throws IOException {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);
        
        final String fileNameToUpload = "!readme.txt"; 
        final Path fileToUpload = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + fileNameToUpload);
        assertTrue(fileToUpload.toFile().exists());
        assertTrue(fileToUpload.toFile().canRead());
        
        final Attachment attachment1 = upload(coAttachmentUploader, fileToUpload, "readme1.txt");
        final Attachment attachment2 = upload(coAttachmentUploader, fileToUpload, "readme2.txt");
        
        co(Attachment.class).batchDelete(listOf(attachment1.getId(), attachment2.getId()));

        assertFalse(co(Attachment.class).entityExists(attachment1));
        assertFalse(co(Attachment.class).entityExists(attachment2));
        
        final Path uploadedFile = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + attachment1.getSha1());
        assertFalse(uploadedFile.toFile().exists());
    }

    @Test
    public void hyperlink_attachment_can_be_created_for_valid_URIs() {
        final IAttachment co$ = co$(Attachment.class);
        final String uri = "https://supported.uri.org";
        final Attachment hyperAttachment = co$.newAsHyperlink(uri).orElseThrow(null);
        assertNotNull(hyperAttachment);
        final Attachment savedAttachment = co$.save(hyperAttachment);
        assertTrue(savedAttachment.isPersisted());
        assertEquals(hyperAttachment, savedAttachment);
        
        assertEquals(uri, savedAttachment.getTitle());
        assertEquals(Attachment.HYPERLINK, savedAttachment.getOrigFileName());
        assertTrue(isNotEmpty(savedAttachment.getSha1()));
        assertNull(savedAttachment.getLastModified());
        assertNull(savedAttachment.getMime());
        assertNull(savedAttachment.getLastRevision());
        assertEquals(Integer.valueOf(0), savedAttachment.getRevNo());
    }

    @Test
    public void hyperlink_attachment_cannot_be_created_for_invalid_URIs_or_unsupported_protocols() {
        final IAttachment co$ = co$(Attachment.class);
        assertFalse(co$.newAsHyperlink("file://supported.uri.org").isPresent());
        assertFalse(co$.newAsHyperlink("https:broken.uri").isPresent());
        assertFalse(co$.newAsHyperlink("uri.missing.protocol").isPresent());
    }

    /**
     * This is just a helper method for performing file uploading.
     * 
     * @param coAttachmentUploader
     * @param fileToUpload
     * @param origFileName
     * @return
     * @throws IOException
     */
    private Attachment upload(final AttachmentUploaderDao coAttachmentUploader, final Path fileToUpload, final String origFileName) throws IOException {
        try (final InputStream is = new FileInputStream(fileToUpload.toAbsolutePath().toString())) {
            final AttachmentUploader uploader = new_(AttachmentUploader.class);
            uploader.setOrigFileName(origFileName).setInputStream(is);

            return coAttachmentUploader.save(uploader).getKey();
        }
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

}
