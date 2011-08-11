package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachmentController;
import ua.com.fielden.platform.equery.interfaces.IFilter;
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

}
