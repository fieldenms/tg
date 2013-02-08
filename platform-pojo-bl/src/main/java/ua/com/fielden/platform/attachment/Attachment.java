package ua.com.fielden.platform.attachment;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyReadonly;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;
import ua.com.fielden.platform.error.Result;

/**
 * Class representing file attachment. The convention is to use the name of the attached file as entitie's key.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Converted File", desc = "Unique name of the file attached.")
@DescTitle(value = "Description", desc = "A comment about the attached file, which can be used for search.")
@DescRequired
@KeyReadonly
@MapEntityTo("ATTACHMENTS")
@DefaultController(IAttachmentController.class)
public class Attachment extends AbstractEntity<String> {

    /** Used purely to represent a new file being attached. */
    private File file;

    @IsProperty
    @MapTo
    @Required
    @Readonly
    @Title(value = "File name", desc = "The original name of the file represented by this attachment.")
    private String fileName;

    @Observable
    public Attachment setFileName(final String fileName) {
	this.fileName = fileName;
	return this;
    }

    public String getFileName() {
	return fileName;
    }

    public File getFile() {
	return file;
    }

    public void setFile(final File file) {
	this.file = file;
	if (file != null) {
	    try {
		final String key = URLEncoder.encode(file.getName().toUpperCase(), "UTF-8");
		setKey(key);
	    } catch (final UnsupportedEncodingException e) {
		throw new Result(e);
	    }

	    setFileName(file.getName());
	}
    }

    public void setJustFile(final File file) {
	this.file = file;
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
