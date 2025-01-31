package ua.com.fielden.platform.attachment;

import ua.com.fielden.platform.dao.IEntityDao;

import java.nio.file.Path;

/**
 * A companion object for {@link AttachmentUploader}.
 * 
 * @author TG Team
 *
 */
public interface AttachmentUploaderCo extends IEntityDao<AttachmentUploader> {

    String[] RESTRICTED_FILE_TYPES = new String[] {"application/x-msdownload", "application/octet-stream", "application/vnd.microsoft.portable-executable"};

    Path attachmentPath(final Attachment attachment);

}
