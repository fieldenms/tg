package ua.com.fielden.platform.attachment;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyReadonly;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.error.Result;

/**
 * Class representing file attachment. The convention is to use the name of the attached file as entitie's key.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "File name", desc = "Unique name of the file attached.")
@DescTitle(value = "Description", desc = "A comment about the attached file, which can be used for search.")
@DescRequired
@KeyReadonly
@MapEntityTo("ATTACHMENTS")
@CompanionObject(IAttachment.class)
@DisplayDescription
public class Attachment extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    /** Used purely to represent a new file being attached.
     * Please note that this field is not a property. */
    private File file;

    @IsProperty
    @Title(value = "Is modified?", desc = "Indicates whether the actual attachment file was modified.")
    private boolean modified = false;

    @Override
    @Observable
    public Attachment setDesc(final String desc) {
	super.setDesc(desc);
	return this;
    }

    @Override
    @NotNull
    @Observable
    public Attachment setKey(final String key) {
	super.setKey(key);
	return this;
    }

    public String getFileExtension() {
	final String key = getKey().toString();
	final int pos = key.lastIndexOf(".");

	return key.substring(pos + 1, key.length());
    }

    public String getFileNameWithoutExtension() {
	final String key = getKey().toString();
	final int pos = key.lastIndexOf(".");

	return key.substring(0, pos);
    }

    public File getFile() {
	return file;
    }

    @Observable
    public Attachment setModified(final boolean modified) {
	this.modified = modified;
	return this;
    }

    public boolean isModified() {
	return modified;
    }

    public Attachment setFile(final File file) {
	this.file = file;
	// let's assign attachment key equal to file's name if the key is empty
	if (StringUtils.isEmpty(getKey())) {
	    setKey(file.getName());
	}
	return this;
    }

    @Override
    protected Result validate() {
	final Result result = super.validate();
	if (!result.isSuccessful()) {
	    return result;
	} else if (file == null && !isPersisted()) {
	    return new Result(this, new IllegalStateException("New attachment is missing a file."));
	} else if (file == null && isModified()) {
	    return new Result(this, new IllegalStateException("Modified attachment is missing a file."));
	}
	return result;
    }
}
