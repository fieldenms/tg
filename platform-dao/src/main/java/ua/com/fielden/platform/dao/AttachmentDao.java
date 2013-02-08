package ua.com.fielden.platform.dao;

import java.util.Map;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachmentController;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(Attachment.class)
public class AttachmentDao extends CommonEntityDao<Attachment> implements IAttachmentController {

    @Inject
    protected AttachmentDao(final IFilter filter) {
	super(filter);
    }

    @Override
    public byte[] download(final Attachment attachment) {
	throw new UnsupportedOperationException("");
    }

    @Override
    public void delete(final Attachment entity) {
        defaultDelete(entity);
    }

    @Override
    public void delete(final EntityResultQueryModel<Attachment> model) {
        defaultDelete(model);
    }

    @Override
    public void delete(final EntityResultQueryModel<Attachment> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }

}
