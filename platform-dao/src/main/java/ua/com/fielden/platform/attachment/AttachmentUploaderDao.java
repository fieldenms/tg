package ua.com.fielden.platform.attachment;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.logging.log4j.Logger;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import ua.com.fielden.platform.cypher.HexString;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityAlreadyExists;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.rx.AbstractSubjectKind;
import ua.com.fielden.platform.security.user.User;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.error.Result.*;

/**
 * DAO implementation for companion object {@link AttachmentUploaderCo}.
 * <p>
 * It has two responsibilities:
 * <ul>
 * <li> Save the input stream as a file into the attachment location, but only if another file with the same content does not yet exist.
 * <li> Create and persist an attachment {@link Attachment} associated with the uploaded file resource.
 * </ul>
 *
 * @author TG Team
 *
 */
@EntityType(AttachmentUploader.class)
public class AttachmentUploaderDao extends CommonEntityDao<AttachmentUploader> implements AttachmentUploaderCo {

    private static final int DEBUG_DELAY_PROCESSING_TIME_MILLIS = 0;
    private static final Random RND = new Random(100);

    private static final Logger LOGGER = getLogger(AttachmentUploaderDao.class);
    public static final String WARN_RESTRICTED_MIME = "An attempt to load file [%s] with a restricted mime type identified as [%s] (provided a [%s]) by user [%s].";
    public static final String ERR_RESTRICTED_MIME = "Files of type [%s] are not supported.";

    public final String attachmentsLocation;
    public final Set<String> attachmentsAllowlist;

    @Inject
    public AttachmentUploaderDao(
            final @Named("attachments.location") String attachmentsLocation,
            final @Named("attachments.allowlist") String attachmentsAllowlist)
    {
        this.attachmentsLocation = attachmentsLocation;
        this.attachmentsAllowlist = isBlank(attachmentsAllowlist)
                                    ? Set.of()
                                    : Arrays.stream(attachmentsAllowlist.split(",")).map(String::trim).collect(toUnmodifiableSet());
    }

    @Override
    @SessionRequired
    public AttachmentUploader save(final AttachmentUploader uploader) {
        uploader.getEventSourceSubject().ifPresent(ess -> ess.publish(5));
        if (uploader.getInputStream() == null) {
            LOGGER.fatal(() -> "Input stream is missing when attempting to upload [%s].".formatted(uploader.getOrigFileName()));
            throw failure("Input stream was not provided.");
        }

        final Path tmpPath = Paths.get(new File(tmpFileName()).toURI());
        final String sha1;
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA1");
            uploader.getEventSourceSubject().ifPresent(ess -> publishWithDelay(ess, 10));

            LOGGER.debug(() -> "Saving uploaded [%s] to tmp file [%s].".formatted(uploader.getOrigFileName(), tmpPath));
            try (final InputStream is = uploader.getInputStream();
                 final DigestInputStream dis = new DigestInputStream(is, md) /* digest decorator to compute SHA1 checksum while reading a stream */ ) {

                // writing input stream to tmp file...
                Files.copy(dis, tmpPath);
                uploader.getEventSourceSubject().ifPresent(ess -> publishWithDelay(ess, 40));
            }

            if (DEBUG_DELAY_PROCESSING_TIME_MILLIS > 0) {
                if (RND.nextInt(300) > 160) {
                    throw new RuntimeException("This is a purposeful DEBUG exception to model file uploading/processing errors.");
                }
            }

            // convert digest to string for target file creation
            final byte[] digest = md.digest();
            sha1 = HexString.bufferToHex(digest, 0, digest.length);
            uploader.getEventSourceSubject().ifPresent(ess -> publishWithDelay(ess, 65));

            // let's validate the file nature by analysing its magic number
            canAcceptFile(uploader, tmpPath, getUser()).ifFailure(Result::throwRuntime);

            // if the target file already exist then need to create it by copying tmp file
            final File targetFile = new File(targetFileName(sha1));
            if (!targetFile.exists()) {
                final Path targetPath = Paths.get(targetFile.toURI());
                Files.copy(tmpPath, targetPath);
                uploader.getEventSourceSubject().ifPresent(ess -> publishWithDelay(ess, 80));
            }

        } catch (final Exception ex) {
            LOGGER.fatal(() -> "Failed to upload [%s].".formatted(uploader.getOrigFileName()), ex);
            throw Result.failure(ex);
        } finally {
            // remove tmp file, and simply log an error if it could not be removed
            try {
                Files.deleteIfExists(tmpPath);
            } catch (final IOException ex) {
                LOGGER.error(() -> "Could not remove tmp file [%s].".formatted(tmpPath), ex);
            }
            uploader.getEventSourceSubject().ifPresent(ess -> publishWithDelay(ess, 85));
        }

        // now we can create/retrieve a corresponding Attachment instance
        LOGGER.debug(()-> "Creating an attachment for uploaded [%s].".formatted(uploader.getOrigFileName()));
        final Attachment attachment = co$(Attachment.class).new_()
                .setSha1(sha1)
                .setOrigFileName(uploader.getOrigFileName())
                .setLastModified(uploader.getLastModified())
                .setMime(uploader.getMime());
        try {
            final Attachment savedAttachment = co$(Attachment.class).save(attachment);
            uploader.setKey(savedAttachment);
            LOGGER.debug(() -> "New attachment [%s] is created successfully.".formatted(attachment));
            uploader.getEventSourceSubject().ifPresent(ess -> publishWithDelay(ess, 95));
        } catch (final EntityAlreadyExists ex) {
            uploader.getEventSourceSubject().ifPresent(ess -> ess.publish(90));
            LOGGER.debug(() -> "Attachment [%s] already exists. Reusing existing.".formatted(attachment));
            final Attachment existingAttachment = co(Attachment.class).findByEntityAndFetch(co(Attachment.class).getFetchProvider().fetchModel(), attachment);
            if (existingAttachment == null) {
                final String errAttachmentNotFound = "Attachment [%s] could not be located.".formatted(attachment);
                LOGGER.error(errAttachmentNotFound);
                throw failure(errAttachmentNotFound);
            }
            uploader.setKey(existingAttachment);
        }

        // make sure we report 100% completion
        LOGGER.debug(() -> "Completed attachment uploading of [%s] successfully.".formatted(uploader.getOrigFileName()));
        uploader.getEventSourceSubject().ifPresent(ess -> publishWithDelay(ess, 100));

        return uploader;
    }

    private Result canAcceptFile(final AttachmentUploader uploader, final Path tmpPath, final User user) throws IOException {
        try (final InputStream is = Files.newInputStream(tmpPath);
             final BufferedInputStream bis = new BufferedInputStream(is))
        {
            final AutoDetectParser parser = new AutoDetectParser();
            final Detector detector = parser.getDetector();
            final Metadata meta = new Metadata();
            final MediaType mediaType = detector.detect(bis, meta);
            // application/x-tika-ooxml     application/vnd.openxmlformats-officedocument.wordprocessingml.document
            // application/x-tika-ooxml     application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
            // application/x-tika-msoffice  application/vnd.ms-excel
            // application/x-tika-msoffice  application/msword

            LOGGER.debug(() -> "Mime type for uploaded file [%s] identified as [%s], the provided is [%s].".formatted(uploader.getOrigFileName(), mediaType, uploader.getMime()));
            if (attachmentsAllowlist.isEmpty()) {
                if (ATTACHMENTS_DENYLIST.contains(mediaType.toString())) {
                    LOGGER.warn(() -> format(WARN_RESTRICTED_MIME, uploader.getOrigFileName(), mediaType, uploader.getMime(), user));
                    return failuref(ERR_RESTRICTED_MIME, mediaType);
                }
            }
            else if (!attachmentsAllowlist.contains(mediaType.toString())) {
                LOGGER.warn(() -> format(WARN_RESTRICTED_MIME, uploader.getOrigFileName(), mediaType, uploader.getMime(), user));
                return failuref(ERR_RESTRICTED_MIME, mediaType);
            }
            // It is possible that MIME for the uploader is already specified.
            // Nevertheless, let's use Tika's MIME to ensure that the validated MIME is associated with the uploader.
            uploader.setMime(mediaType.toString());
        }
        return successful();
    }

    /**
     * A convenient method for DEBUG purposes to mimic long-running file uploads/processing.
     *
     * @param ess
     * @param prc
     */
    private static void publishWithDelay(final AbstractSubjectKind<Integer> ess, final int prc) {
        if (DEBUG_DELAY_PROCESSING_TIME_MILLIS > 0) {
            try {
                Thread.sleep(RND.nextInt(DEBUG_DELAY_PROCESSING_TIME_MILLIS));
            } catch (final InterruptedException ex) {
                LOGGER.debug("Interrupted during debug sleep.", ex);
            }
        }
        ess.publish(prc);
    }

    private String tmpFileName() {
        return attachmentsLocation + File.separator  + getUsername() + "_" + randomUUID().toString() + ".tmp";
    }

    private String targetFileName(final String sha1) {
        return attachmentsLocation + File.separator  + sha1;
    }

}
