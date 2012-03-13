package ua.com.fielden.platform.attachment;

import ua.com.fielden.platform.dao2.IEntityDao2;

/**
 * A controller for managing DAO related operations with {@link Attachment}.
 *
 * @author TG Team
 *
 */
public interface IAttachmentController2 extends IEntityDao2<Attachment> {
    byte[] download(final Attachment attachment);
}
