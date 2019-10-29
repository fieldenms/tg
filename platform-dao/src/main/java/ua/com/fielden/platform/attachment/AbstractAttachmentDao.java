package ua.com.fielden.platform.attachment;

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

import java.util.Collection;
import java.util.List;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for Attachments companion object.
 *
 * @author TG Air Team
 *
 */

public abstract class AbstractAttachmentDao<A extends AbstractAttachment<A, ?>> extends CommonEntityDao<A> {

    @Inject
    public AbstractAttachmentDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public A save(final A entity) {
        persistAttachmentIfRequired(entity);
        return super.save(entity);
    }

    /**
     * Ensures that the assigned attachment instance is persisted.
     *
     * @param entity
     */
    protected void persistAttachmentIfRequired(final AbstractAttachment<A, ?> entity) {
        final Attachment currAttachment = entity.getAttachment();
        if (currAttachment != null && !currAttachment.isPersisted()) {
            entity.setAttachment(co$(Attachment.class).save(currAttachment));
        }
    }

    @Override
    @SessionRequired
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

    @Override
    @SessionRequired
    public int batchDelete(final List<A> entities) {
        return defaultBatchDelete(entities);
    }

    @Override
    protected IFetchProvider<A> createFetchProvider() {
        final String attachment = "attachment.";
        return super.createFetchProvider().with("attachedTo", "attachment",
                DESC,
                attachment + pn_TITLE, attachment + pn_SHA1, attachment + pn_ORIG_FILE_NAME, attachment + pn_REV_NO,
                attachment + pn_PREV_REVISION, attachment + pn_PREV_REVISION + "." + pn_REV_NO, attachment + pn_PREV_REVISION + "." + pn_LAST_REVISION,
                attachment + pn_LAST_REVISION, attachment + pn_LAST_MODIFIED, attachment + pn_MIME, attachment + pn_IS_LATEST_REV);
    }

}
