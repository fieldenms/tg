package ua.com.fielden.platform.attachment;

import java.io.File;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyReadonly;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.error.Result;

/**
 * Class representing file attachment. The convention is to use the name of the attached file as entitie's key.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "File", desc = "Unique name of the file attached.")
@DescTitle(value = "Description", desc = "A comment about the attached file, which can be used for search.")
@DescRequired
@KeyReadonly
@MapEntityTo("ATTACHMENTS")
public class Attachment extends AbstractEntity<String> {

    /** User purely to represent a new file being attached. */
    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(final File file) {
        this.file = file;
        if (file != null) {
            setKey(file.getName().toUpperCase());
        }
    }

    @Override
    protected Result validate() {
        final Result result = super.validate();
        if (!result.isSuccessful()) {
            return result;
        } else if (file == null && !isPersisted()) {
            return new Result(this, new IllegalStateException("Attachment is missing a file."));
        }
        return result;
    }

}
