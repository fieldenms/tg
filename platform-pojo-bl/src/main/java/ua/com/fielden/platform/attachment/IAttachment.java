package ua.com.fielden.platform.attachment;

import ua.com.fielden.platform.dao.IEntityDao;

/**
 * A controller for managing DAO related operations with {@link Attachment}.
 *
 * @author TG Team
 *
 */
public interface IAttachment extends IEntityDao<Attachment> {

    /**
     * Downloads attached file and returns it as a byte array.
     *
     * @param attachment
     * @return
     */
    byte[] download(final Attachment attachment);

    /**
     * Copies attachment together with associated file.
     * Returns a newly crated and persisted attachmen.
     *
     * @param attachment -- attachment that gets copied
     * @return
     */
    Attachment copy(final Attachment fromAttachment, final String key, final String desc);

}
