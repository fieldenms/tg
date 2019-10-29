package ua.com.fielden.platform.attachment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * A test case validating basic operations of attachments such as uploading and deletion.
 * 
 * @author TG Team
 * 
 */
public class AttachmentOperationsTest extends AbstractDaoTestCase {

    @Test
    public void attachment_instance_is_created_as_the_result_of_successful_file_upload() throws IOException {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);
        
        final String fileNameToUpload = "!readme.txt"; 
        final Path fileToUpload = Paths.get(coAttachmentUploader.attachmentsLocation + File.separator + fileNameToUpload);
        assertTrue(fileToUpload.toFile().exists());
        assertTrue(fileToUpload.toFile().canRead());

        final Attachment attachment = upload(coAttachmentUploader, fileToUpload, "readme.txt");
 
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
            uploader.setOrigFileName(origFileName).setMime("text/plain").setInputStream(is);

            return coAttachmentUploader.save(uploader).getKey();
        }
    }

}