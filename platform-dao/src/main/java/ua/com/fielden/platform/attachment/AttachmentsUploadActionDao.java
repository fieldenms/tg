package ua.com.fielden.platform.attachment;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;

/**
 * A companion that is responsible for associating attachments with other domain entities.
 * 
 * @author TG Team
 *
 */
@EntityType(AttachmentsUploadAction.class)
public class AttachmentsUploadActionDao extends CommonEntityDao<AttachmentsUploadAction> implements IAttachmentsUploadAction {

    private static final Logger LOGGER = Logger.getLogger(AttachmentsUploadActionDao.class);
    
    @Inject
    protected AttachmentsUploadActionDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public AttachmentsUploadAction save(final AttachmentsUploadAction action) {
        // if there are attachments and master entity then build their associations
        if (action.getMasterEntity() != null && !action.getAttachmentIds().isEmpty()) {
            final fetch<Attachment> attachmentFetchModel = co(Attachment.class).getFetchProvider().fetchModel();
            final Class<? extends AbstractEntity<?>> entityType = action.getMasterEntity().getType();
            if (co$(action.getMasterEntity().getType()) instanceof ICanAttach) {
                action.setSingleAttachment(null);
                final ICanAttach co = (ICanAttach) co$(entityType);
                action.getAttachmentIds().stream()
                .map(id -> co(Attachment.class).findById(id, attachmentFetchModel))
                .filter(Objects::nonNull) // just in case
                .map(att -> {action.setSingleAttachment(att); return co.attach(att, action.getMasterEntity());})
                .forEach(assoc -> LOGGER.debug(format("Attachment association created: %s", assoc)));
            } else {
                throw failure(format("Companion for %s cannot attach attachments.", getEntityTitleAndDesc(entityType).getKey()));
            }
        } else { // otherwise do nothing...
            LOGGER.debug("Either master entity or attachments are missing.");
        }
        
        return action;
    }
    
}
