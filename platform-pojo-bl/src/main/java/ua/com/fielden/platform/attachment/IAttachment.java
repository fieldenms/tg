package ua.com.fielden.platform.attachment;

import java.io.File;

import ua.com.fielden.platform.dao.IEntityDao;

/**
 * A companion contract for {@link Attachment}.
 * 
 * @author TG Team
 * 
 */
public interface IAttachment extends IEntityDao<Attachment> {
    File asFile(final Attachment attachment);
}
