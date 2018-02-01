package ua.com.fielden.platform.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.dao.annotations.AfterSave;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.handlers.IAttachmentAfterSave;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

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

    public byte[] download(final Attachment attachment) {
        final File file = new File(attachmentsLocation + "/" + attachment.getKey());
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
