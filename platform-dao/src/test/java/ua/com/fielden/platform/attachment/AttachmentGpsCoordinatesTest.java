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
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.attachment.Attachment.pn_LATITUDE;
import static ua.com.fielden.platform.attachment.Attachment.pn_LONGITUDE;
import static ua.com.fielden.platform.attachment.validators.LatitudeValidator.ERR_LATITUDE_RANGE;
import static ua.com.fielden.platform.attachment.validators.LongitudeValidator.ERR_LONGITUDE_RANGE;
import static ua.com.fielden.platform.test_utils.TestUtils.assertFileReadable;

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
            final var path = coAttachmentUploader.attachmentPath(attachment);
            try {
                Files.deleteIfExists(path);
            } catch (final IOException e) {
                logger.warn("Failed to delete attachment [%s] at [%s]".formatted(attachment, path), e);
            }
        }
    }

    @Test
    public void latitude_and_longitude_are_extracted_as_a_result_of_uploading_an_image_with_gps_metadata() {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);

        final Path fileToUpload = Path.of(coAttachmentUploader.attachmentsLocation, "gps-0.jpg");
        final Attachment attachment = uploadJpeg(coAttachmentUploader, fileToUpload);
 
        assertNotNull(attachment);
        assertEquals(new BigDecimal("52.512211"), attachment.getLatitude());
        assertEquals(new BigDecimal("13.402742"), attachment.getLongitude());
        assertFileReadable(coAttachmentUploader.attachmentPath(attachment));
    }

    @Test
    public void latitude_and_longitude_are_absent_as_a_result_of_uploading_an_image_without_gps_metadata() throws IOException {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);

        final Path fileToUpload = Path.of(coAttachmentUploader.attachmentsLocation, "nogps-0.jpg");
        final Attachment attachment = uploadJpeg(coAttachmentUploader, fileToUpload);

        assertNotNull(attachment);
        assertNull(attachment.getLatitude());
        assertNull(attachment.getLongitude());
        assertFileReadable(coAttachmentUploader.attachmentPath(attachment));
    }

    @Test
    public void latitude_and_longitude_are_absent_as_a_result_of_uploading_a_non_image_file() throws IOException {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);

        final Path fileToUpload = Path.of(coAttachmentUploader.attachmentsLocation, "!readme.txt");
        final Attachment attachment = uploadJpeg(coAttachmentUploader, fileToUpload);

        assertNotNull(attachment);
        assertNull(attachment.getLatitude());
        assertNull(attachment.getLongitude());
        assertFileReadable(coAttachmentUploader.attachmentPath(attachment));
    }

    @Test
    public void latitude_values_must_fall_in_the_expected_range() {
        final Consumer<BigDecimal> assertFailure = latitude -> {
            final var attachment = new_(Attachment.class);
            attachment.setLatitude(latitude);
            assertFalse(attachment.getProperty(pn_LATITUDE).isValid());
            assertEquals(ERR_LATITUDE_RANGE, attachment.getProperty(pn_LATITUDE).getFirstFailure().getMessage());
        };
        final Consumer<BigDecimal> assertSuccess = latitude -> {
            final var attachment = new_(Attachment.class);
            attachment.setLatitude(latitude);
            assertTrue(attachment.getProperty(pn_LATITUDE).isValid());
        };

        assertFailure.accept(new BigDecimal(-100));
        assertFailure.accept(new BigDecimal(91));
        assertSuccess.accept(new BigDecimal(90));
        assertSuccess.accept(new BigDecimal(-90));
        assertSuccess.accept(new BigDecimal(0));
    }

    @Test
    public void longitude_values_must_fall_in_the_expected_range() {
        final Consumer<BigDecimal> assertFailure = longitude -> {
            final var attachment = new_(Attachment.class);
            attachment.setLongitude(longitude);
            assertFalse(attachment.getProperty(pn_LONGITUDE).isValid());
            assertEquals(ERR_LONGITUDE_RANGE, attachment.getProperty(pn_LONGITUDE).getFirstFailure().getMessage());
        };
        final Consumer<BigDecimal> assertSuccess = longitude -> {
            final var attachment = new_(Attachment.class);
            attachment.setLongitude(longitude);
            assertTrue(attachment.getProperty(pn_LONGITUDE).isValid());
        };

        assertFailure.accept(new BigDecimal(-400));
        assertFailure.accept(new BigDecimal(181));
        assertSuccess.accept(new BigDecimal(180));
        assertSuccess.accept(new BigDecimal(-180));
        assertSuccess.accept(new BigDecimal(0));
    }

    @Test
    public void gps_coordinates_are_ignored_as_a_result_of_uploading_an_image_with_invalid_latitude() {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);

        final Path fileToUpload = Path.of(coAttachmentUploader.attachmentsLocation, "invalid-lat-gps.jpg");
        final Attachment attachment = uploadJpeg(coAttachmentUploader, fileToUpload);

        assertNotNull(attachment);
        assertNull(attachment.getLatitude());
        assertNull(attachment.getLongitude());
        assertFileReadable(coAttachmentUploader.attachmentPath(attachment));
    }

    @Test
    public void gps_coordinates_are_ignored_as_a_result_of_uploading_an_image_with_invalid_longitude() {
        final AttachmentUploaderDao coAttachmentUploader = co(AttachmentUploader.class);

        final Path fileToUpload = Path.of(coAttachmentUploader.attachmentsLocation, "invalid-lon-gps.jpg");
        final Attachment attachment = uploadJpeg(coAttachmentUploader, fileToUpload);

        assertNotNull(attachment);
        assertNull(attachment.getLatitude());
        assertNull(attachment.getLongitude());
        assertFileReadable(coAttachmentUploader.attachmentPath(attachment));
    }

    private Attachment uploadJpeg(final AttachmentUploaderDao coAttachmentUploader, final Path fileToUpload) {
        assertFileReadable(fileToUpload.toFile());
        final Attachment attachment;
        try (final InputStream is = new FileInputStream(fileToUpload.toAbsolutePath().toString())) {
            final AttachmentUploader uploader = new_(AttachmentUploader.class);
            uploader.setOrigFileName(requireNonNull(fileToUpload.getFileName()).toString())
                    .setMime("image/jpeg")
                    .setInputStream(is);
            attachment = coAttachmentUploader.save(uploader).getKey();
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }

        uploads.add(attachment);
        return attachment;
    }

}
