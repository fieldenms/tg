package ua.com.fielden.platform.attachment;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.junit.Assert.*;

/**
 * Test case that covers the capturing of GPS coordinates from Exif image data..
 */
public class AttachmentGpsCoordinatesTest extends AbstractDaoTestCase {

    private final Logger logger = getLogger();
    private final Set<Attachment> uploads = new HashSet<>();

    @After
    public void deleteUploads() {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);

        final var localUploads = ImmutableList.copyOf(uploads);
        uploads.clear();

        for (final var attachment : localUploads) {
            final var path = Path.of(coAttachmentUploader.attachmentsLocation, attachment.getSha1());
            try {
                Files.deleteIfExists(path);
            } catch (final IOException e) {
                logger.warn("Failed to delete attachment [%s] at [%s]".formatted(attachment, path), e);
            }
        }
    }

    @Test
    public void latitude_and_longitude_are_extracted_as_a_result_of_uploading_an_image_with_gps_metadata() throws IOException {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);

        final Path fileToUpload = Path.of(coAttachmentUploader.attachmentsLocation, "gps-0.jpg");
        assertTrue(fileToUpload.toFile().exists());
        assertTrue(fileToUpload.toFile().canRead());

        final Attachment attachment = uploadJpeg(coAttachmentUploader, fileToUpload);
 
        assertNotNull(attachment);
        assertEquals(new BigDecimal("52.512211"), attachment.getLatitude());
        assertEquals(new BigDecimal("13.402742"), attachment.getLongitude());
    }

    @Test
    public void latitude_and_longitude_are_absent_as_a_result_of_uploading_an_image_without_gps_metadata() throws IOException {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);

        final Path fileToUpload = Path.of(coAttachmentUploader.attachmentsLocation, "nogps-0.jpg");
        assertTrue(fileToUpload.toFile().exists());
        assertTrue(fileToUpload.toFile().canRead());

        final Attachment attachment = uploadJpeg(coAttachmentUploader, fileToUpload);

        assertNotNull(attachment);
        assertNull(attachment.getLatitude());
        assertNull(attachment.getLongitude());
    }

    private Attachment uploadJpeg(final AttachmentUploaderDao coAttachmentUploader, final Path fileToUpload) throws IOException {
        final Attachment attachment;
        try (final InputStream is = new FileInputStream(fileToUpload.toAbsolutePath().toString())) {
            final AttachmentUploader uploader = new_(AttachmentUploader.class);
            uploader.setOrigFileName(requireNonNull(fileToUpload.getFileName()).toString())
                    .setMime("image/jpeg")
                    .setInputStream(is);
            attachment = coAttachmentUploader.save(uploader).getKey();
        }

        uploads.add(attachment);
        return attachment;
    }

}
