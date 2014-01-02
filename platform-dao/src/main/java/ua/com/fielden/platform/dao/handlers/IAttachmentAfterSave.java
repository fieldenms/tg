package ua.com.fielden.platform.dao.handlers;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.dao.annotations.AfterSave;


/**
 * This is a specific to entity {@link Attachment} contract that should be fulfilled for handling of the after save event.
 * Due to a soft-binding nature of annotation {@link AfterSave}, this contract may or may not be provided for a concrete application.
 *
 * @author TG Team

 */
public interface IAttachmentAfterSave extends IAfterSave<Attachment> {
}
