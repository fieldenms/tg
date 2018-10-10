package ua.com.fielden.platform.attachment;

import ua.com.fielden.platform.entity.AbstractEntityWithInputStream;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * A functional entity that is responsible for handling uploading of files for the purpose of creating attachments.
 * <p>
 * In the result of successful uploading, property <code>key</code> should contain a corresponding instance of {@link Attachment}.
 * 
 * @author TG Team
 *
 */
@KeyType(Attachment.class)
@CompanionObject(IAttachmentUploader.class)
public class AttachmentUploader extends AbstractEntityWithInputStream<Attachment> {

}
