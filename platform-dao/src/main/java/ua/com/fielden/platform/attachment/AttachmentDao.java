package ua.com.fielden.platform.attachment;

import static java.lang.String.format;
import static ua.com.fielden.platform.attachment.Attachment.pn_IS_LATEST_REV;
import static ua.com.fielden.platform.attachment.Attachment.pn_LAST_MODIFIED;
import static ua.com.fielden.platform.attachment.Attachment.pn_LAST_REVISION;
import static ua.com.fielden.platform.attachment.Attachment.pn_MIME;
import static ua.com.fielden.platform.attachment.Attachment.pn_ORIG_FILE_NAME;
import static ua.com.fielden.platform.attachment.Attachment.pn_PREV_REVISION;
import static ua.com.fielden.platform.attachment.Attachment.pn_REV_NO;
import static ua.com.fielden.platform.attachment.Attachment.pn_SHA1;
import static ua.com.fielden.platform.attachment.Attachment.pn_TITLE;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ua.com.fielden.platform.attachment.validators.CanBeUsedAsPrevAttachmentRev;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.AfterSave;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.handlers.IAttachmentAfterSave;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;

@EntityType(Attachment.class)
@AfterSave(IAttachmentAfterSave.class)
public class AttachmentDao extends CommonEntityDao<Attachment> implements IAttachment {

    private final String attachmentsLocation;

    @Inject
    protected AttachmentDao(
            final IFilter filter,
            final EntityFactory factory,
            final @Named("attachments.location") String attachmentsLocation) {
        super(filter);
        this.attachmentsLocation = attachmentsLocation;
    }
    
    @Override
    public File asFile(final Attachment attachment) {
        final File file = new File(attachmentsLocation + File.separatorChar + attachment.getSha1());
        if (file.canRead()) {
            return file;
        } else {
            throw failure(format("Could not read file for attachment [%s]", attachment));
        }
    }

    @Override
    @SessionRequired
    public Attachment save(final Attachment attachment) {
        attachment.isValid().ifFailure(Result::throwRuntime);

        // check if prev. revision was specified
        // this would indicate that document revision process is at play...
        final boolean revisionHistoryModified = attachment.getPrevRevision() != null && attachment.getProperty(pn_PREV_REVISION).isDirty();
        
        final Attachment savedAttachment = super.save(attachment);
        
        if (revisionHistoryModified) {
            updateAttachmentRevisionHistory(savedAttachment).ifFailure(Result::throwRuntime);
            return findByEntityAndFetch(getFetchProvider().fetchModel(), savedAttachment);
        } else {        
            return savedAttachment;
        }
    }
    
    /**
     * Ensures correct revision history, including revision numbering and references.
     * 
     * @param savedAttachment
     * @param revNoIncBy 
     * @return
     */
    private Result updateAttachmentRevisionHistory(final Attachment savedAttachment) {
        if (savedAttachment.isDirty()) {
            return failure("Attachment revision history can only be updated for persisted attachments.");
        }
        
        // if last revision is not populated or references itself then the saved attachment represents the latest revision
        // this means that prev. revision needs to have its last revision updated
        if (savedAttachment.getLastRevision() == null || equalsEx(savedAttachment, savedAttachment.getLastRevision())) {
            final Attachment prevRev = findByEntityAndFetch(getFetchProvider().fetchModel(), savedAttachment.getPrevRevision());
            super.save(prevRev.setLastRevision(savedAttachment));
            super.save(savedAttachment.setLastRevision(savedAttachment));
            return Result.successful(savedAttachment);
        } else { // otherwise, this is the case of joining two revision histories or the case of updating the history from the tail-end -- both are handled identically
            final Attachment lastRev = co(Attachment.class).findByEntityAndFetch(getFetchProvider().fetchModel(), savedAttachment.getLastRevision());
            return traverseAndUpdateHistory(lastRev, lastRev, /* sha1Checksums = */ setOf());
        }
    }
    
    private Result traverseAndUpdateHistory(final Attachment lastRev, final Attachment tracedRevision, final Set<String> sha1Checksums) {
        if (tracedRevision == null) {
            return successful(tracedRevision);
        } else {
            if (sha1Checksums.contains(tracedRevision.getSha1())) {
                return failure(format(CanBeUsedAsPrevAttachmentRev.ERR_DUPLICATE_SHA1, tracedRevision.getSha1()));
            }
            sha1Checksums.add(tracedRevision.getSha1());
            
            final Result res = traverseAndUpdateHistory(lastRev, tracedRevision.getPrevRevision() != null ? co(Attachment.class).findByEntityAndFetch(getFetchProvider().fetchModel(), tracedRevision.getPrevRevision()) : null, sha1Checksums);
            if (!res.isSuccessful()) {
                return res;
            }
            
            try {
                final Attachment attachmentToUpdate = findByEntityAndFetch(getFetchProvider().fetchModel(), tracedRevision);
                if (attachmentToUpdate.getPrevRevision() != null) {
                    attachmentToUpdate.setRevNo(attachmentToUpdate.getPrevRevision().getRevNo() + 1);
                }
                attachmentToUpdate.beginLastRevisionUpdate().setLastRevision(lastRev).endLastRevisionUpdate();
                
                return successful(super.save(attachmentToUpdate));
            } catch (final Result ex) {
                return ex;
            } catch (final Exception ex) {
                return failure(ex);
            }
        }
    }

    @Override
    protected IFetchProvider<Attachment> createFetchProvider() {
        return super.createFetchProvider().with(
                DESC,
                pn_TITLE, pn_SHA1, pn_ORIG_FILE_NAME, pn_REV_NO, 
                pn_PREV_REVISION, pn_PREV_REVISION + "." + pn_REV_NO, pn_PREV_REVISION + "." + pn_LAST_REVISION, 
                pn_LAST_REVISION, pn_LAST_MODIFIED, pn_MIME, pn_IS_LATEST_REV);
    }
    
    @Override
    public Attachment new_() {
        return super.new_().setRevNo(0);
    }
    
    public byte[] download(final Attachment attachment) {
        final File file = new File(attachmentsLocation + File.separatorChar + attachment.getKey());
        if (file.canRead()) {
            try (FileInputStream is = new FileInputStream(file)) {
                return IOUtils.toByteArray(is);
            } catch (final IOException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            throw new IllegalArgumentException("Could not read file " + file.getName());
        }
    }

    @Override
    @SessionRequired
    public void delete(final Attachment entity) {
        defaultDelete(entity);
    }

    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<Attachment> model) {
        defaultDelete(model);
    }

    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<Attachment> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(final List<Attachment> entities) {
        return defaultBatchDelete(entities);
    }


}
