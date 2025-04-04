package ua.com.fielden.platform.attachment;

import com.google.common.collect.ImmutableSet;
import ua.com.fielden.platform.dao.IEntityDao;

import java.util.Set;

/**
 * A companion object for {@link AttachmentUploader}.
 * 
 * @author TG Team
 *
 */
public interface AttachmentUploaderCo extends IEntityDao<AttachmentUploader> {

    Set<String> ATTACHMENTS_DENYLIST = ImmutableSet.of(
            "application/x-msdownload",
            "application/octet-stream",
            "application/vnd.microsoft.portable-executable",
            "application/x-ms-installer",
            "application/x-elf",
            "application/x-executable",  // Generic executables
            "application/x-php",         // PHP scripts
            "application/x-sh",          // Bash scripts
            "application/x-perl",        // Perl scripts
            "application/x-python",      // Python scripts
            "application/x-python-code",
            "application/x-msdos-program",
            "application/x-bat",
            "application/x-cmd",
            "application/x-csh",
            "application/javascript",
            "application/zip",
            "application/x-zip-compressed",
            "application/x-rar-compressed",
            "application/x-7z-compressed",
            "application/gzip",
            "application/x-tar",
            "application/x-gtar",
            "application/x-bzip2",
            "application/x-iso9660-image",
            "text/x-php",                // Another PHP variant
            "text/x-shellscript",        // Shell scripts
            "text/x-python",             // Python scripts
            "text/x-perl",               // Perl scripts
            "text/javascript"
    );


}
