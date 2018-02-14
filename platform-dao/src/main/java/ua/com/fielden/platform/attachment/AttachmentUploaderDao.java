package ua.com.fielden.platform.attachment;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static ua.com.fielden.platform.error.Result.failure;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Random;

import javax.management.RuntimeErrorException;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ua.com.fielden.platform.cypher.HexString;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityAlreadyExists;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.rx.AbstractSubjectKind;

/** 
 * DAO implementation for companion object {@link IAttachmentUploader}.
 * <p>
 * It has two responsibilities:
 * <ul>
 * <li> Save the input stream as a file into the attachment location, but only if a corresponding file content does not exist.
 * <li> Create and persist an attachment {@link Attachment} associated with the uploaded file resource.  
 * </ul>
 * 
 * @author TG Team
 *
 */
@EntityType(AttachmentUploader.class)
public class AttachmentUploaderDao extends CommonEntityDao<AttachmentUploader> implements IAttachmentUploader {

    private static final Logger LOGGER = Logger.getLogger(AttachmentUploaderDao.class);
    
    private final String attachmentsLocation;
    
    @Inject
    public AttachmentUploaderDao(final @Named("attachments.location") String attachmentsLocation, final IFilter filter) {
        super(filter);
        this.attachmentsLocation = attachmentsLocation;
    }
    
    private static final Random rnd = new Random(100);
    private static void delay(final AbstractSubjectKind<Integer> ess, final int prc) {
        try {
            Thread.sleep(rnd.nextInt(300));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        ess.publish(prc);
    }
    
    @Override
    public AttachmentUploader save(final AttachmentUploader uploader) {
        
        uploader.getEventSourceSubject().ifPresent(ess -> ess.publish(5));
        if (uploader.getInputStream() == null) {
            LOGGER.fatal(format("Input stream is missing when attempting to upload [%s].", uploader.getOrigFileName()));
            throw failure("Input stream was not provided.");
        }
        
        final Path tmpPath = Paths.get(new File(tmpFileName()).toURI());
        final String sha1;
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA1");
            uploader.getEventSourceSubject().ifPresent(ess -> delay(ess, 10));

            LOGGER.debug(format("Saving uploaded [%s] to tmp file [%s].", uploader.getOrigFileName(), tmpPath));
            try (final InputStream is = uploader.getInputStream();
                 final DigestInputStream dis = new DigestInputStream(is, md) /* digest decorator to compute SHA1 checksum while reading a stream */ ) {

                // writing input stream to tmp file...
                Files.copy(dis, tmpPath);
                uploader.getEventSourceSubject().ifPresent(ess -> delay(ess, 40));
            }
            
            if (rnd.nextInt(300) > 160) {
                throw new RuntimeException("ha-ha-ha this is on purpose");
            }
            
            // convert digest to string for target file creation
            final byte[] digest = md.digest();
            sha1 = HexString.bufferToHex(digest, 0, digest.length);
            uploader.getEventSourceSubject().ifPresent(ess -> delay(ess, 65));
            
            // if the target file already exist then need to create it by copying tmp file
            final File targetFile = new File(targetFileName(sha1));
            if (!targetFile.exists()) {
                final Path targetPath = Paths.get(targetFile.toURI());
                Files.copy(tmpPath, targetPath);
                uploader.getEventSourceSubject().ifPresent(ess -> delay(ess, 80));
            }
            
        } catch (final Exception ex) {
            LOGGER.fatal(format("Failed to upload [%s].", uploader.getOrigFileName()), ex);
            throw Result.failure(ex);
        } finally {
            // remove tmp file, and simply log an error if it could not be removed
            try {
                Files.deleteIfExists(tmpPath);
            } catch (final IOException ex) {
                LOGGER.error(format("Could not remove tmp file [%s].", tmpPath), ex);
            }
            uploader.getEventSourceSubject().ifPresent(ess -> delay(ess, 85));
        }

        // now we can create/retrieve a corresponding Attachment instance
        LOGGER.debug(format("Creating an attachment for uploaded [%s].", uploader.getOrigFileName()));
        final Attachment attachment = co$(Attachment.class).new_()
                .setSha1(sha1)
                .setOrigFileName(uploader.getOrigFileName())
                .setLastModified(uploader.getLastModified())
                .setMime(uploader.getMime());
        try {
            final Attachment savedAttachment = co$(Attachment.class).save(attachment);
            uploader.setKey(savedAttachment);
            LOGGER.debug(format("New attachment [%s] is created successfully.", attachment));
            uploader.getEventSourceSubject().ifPresent(ess -> delay(ess, 95));
        } catch (final EntityAlreadyExists ex) {
            uploader.getEventSourceSubject().ifPresent(ess -> ess.publish(90));
            LOGGER.debug(format("Attachment [%s] already exists. Reusing existing.", attachment));
            final Attachment existingAttachment = co(Attachment.class).findByEntityAndFetch(co(Attachment.class).getFetchProvider().fetchModel(), attachment);
            if (existingAttachment == null) {
                final String errAttachmentNotFound = format("Attachment [%s] could not be located.", attachment);
                LOGGER.error(errAttachmentNotFound);
                throw failure(errAttachmentNotFound);
            }
            uploader.setKey(existingAttachment);
        }

        // make sure we report 100% completion
        LOGGER.debug(format("Completed attachment uploading of [%s] successfully.", uploader.getOrigFileName()));
        uploader.getEventSourceSubject().ifPresent(ess -> delay(ess, 100));
        
        return uploader;
    }

    private String tmpFileName() {
        return attachmentsLocation + File.separator  + getUsername() + "_" + randomUUID().toString() + ".tmp";
    }
    
    private String targetFileName(final String sha1) {
        return attachmentsLocation + File.separator  + sha1;
    }

}