package ua.com.fielden.platform.attachment;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

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
            action.setSingleAttachment(null);
            final EntityResultQueryModel<Attachment> query = select(Attachment.class).where().prop(ID).in().values(action.getAttachmentIds().toArray(new Long[] {})).model();
            try (final Stream<Attachment> attachments = co(Attachment.class).stream(from(query).with(attachmentFetchModel).model())) {
                attachments.forEach(att -> {
                    action.setSingleAttachment(att);
                    if (co$(entityType) instanceof ICanAttach) {
                        final ICanAttach co = (ICanAttach) co$(entityType);
                        co.attach(att, action.getMasterEntity());
                    }
                });
            }
        } else { // otherwise do nothing...
            LOGGER.debug("Either master entity or attachments are missing.");
        }
        
        return action;
    }
    
}
