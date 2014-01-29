package ua.com.fielden.platform.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.dao.annotations.AfterSave;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.handlers.IAttachmentAfterSave;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;
import com.google.inject.name.Named;

@EntityType(Attachment.class)
@AfterSave(IAttachmentAfterSave.class)
public class AttachmentDao extends CommonEntityDao<Attachment> implements IAttachment {

    private final String attachmentsLocation;
    private final EntityFactory factory;

    @Inject
    protected AttachmentDao(//
	    final IFilter filter,//
	    final EntityFactory factory, //
	    final @Named("attachments.location") String attachmentsLocation) {
	super(filter);
	this.attachmentsLocation = attachmentsLocation;
	this.factory = factory;
    }

    @Override
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

    @Override
    @SessionRequired
    public Attachment copy(final Attachment fromAttachment, final String key, final String desc) {
	if (findByKey(key) != null) {
	    throw Result.failure("Attachment " + key + " already exists.");
	}

	final File fromFile = new File(attachmentsLocation + "/" + fromAttachment.getKey());
	if (!fromFile.canRead()) {
	    throw Result.failure(fromAttachment, "Could not read file " + fromFile.getName());
	}

	try {
	    final File toFile = new File(attachmentsLocation + "/" + key);
	    Files.copy(fromFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING); // will replace is such file already exists
	    final Attachment copy = factory.newEntity(Attachment.class, key, desc);
	    copy.setFile(toFile);
	    return save(copy);
	} catch (final IOException e) {
	    throw Result.failure(e);
	}
    }
}
