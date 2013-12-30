package ua.com.fielden.platform.attachment;

import ua.com.fielden.platform.dao.IEntityDao;

/**
 * A controller for managing DAO related operations with {@link Attachment}.
 *
 * @author TG Team
 *
 */
public interface IAttachment extends IEntityDao<Attachment> {
    byte[] download(final Attachment attachment);
}
