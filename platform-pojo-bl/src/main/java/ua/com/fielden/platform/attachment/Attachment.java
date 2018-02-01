package ua.com.fielden.platform.attachment;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.validation.annotation.Final;

/**
 * Entity representing a file attachment. It has a composite key, consisting of:
 * <ul>
 * <li><code>title</code> -- defaulted to the file name, but editable by user to provide a more relevant title;
 * <li><code>sha1</code> -- SHA1-based checksum of the associated file.
 * </ul>
 * Having the combination of <code>title</code> and <code>sha1</code> as the attachment key, prevents file duplicates, but permits existence of several attachments referencing the same file.
 * The "sameness" of files is determined by the SHA1 checksum of their content, not the file name.
 * <p>
 * All attachments are created as revision 1 (property <code>revNo</code>).
 * If an attachment gets associated with some other attachment by means of assigning it to an empty property <code>prevRevision</code> of that other attachment, then the revision number for that attachment gets incremented by 1.
 * Any attachment can only be associated once. This ensures immutable and linear hierarchy of attachment revisions.
 * <p>
 * Attachments with the same SHA1 cannot be in the immediate association as they represent the same file.
 *  
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@DescTitle(value = "Description", desc = "A summary about the attachment, which can be used for search.")
@DisplayDescription
@MapEntityTo
@CompanionObject(IAttachment.class)
public class Attachment extends AbstractPersistentEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "A concenient attachment title that would indicate what it is about")
    @CompositeKeyMember(1)
    private String title;
    
    @IsProperty
    @MapTo
    @Title(value = "SHA1", desc = "A unique SHA1-based checksum of the file referenced by this attachment.")
    @CompositeKeyMember(2)
    @Readonly
    private String sha1;

    @IsProperty
    @MapTo
    @Title(value = "File Name", desc = "The file name of the file, uploading which resulted in creation of this attachment.")
    @Readonly
    @Required
    private String origFileName;
    
    @IsProperty
    @MapTo
    @Title(value = "Rev#", desc = "Attachment revision number.")
    @Readonly
    @Final(persistentOnly = false)
    @Required
    private Integer revNo;

    @IsProperty
    @MapTo
    @Title(value = "Prev. Rev.", desc = "An attachment that represent the previous revision of this attachment. Could be empty.")
    @Final
    //@BeforeChange(CheckIfAttachmentCanBePrevRevision.class)
    private Attachment prevRevision;

    @Observable
    public Attachment setPrevRevision(final Attachment prevRevision) {
        this.prevRevision = prevRevision;
        return this;
    }

    public Attachment getPrevRevision() {
        return prevRevision;
    }
    
    @Observable
    public Attachment setRevNo(final Integer revNo) {
        this.revNo = revNo;
        return this;
    }

    public Integer getRevNo() {
        return revNo;
    }

    @Observable
    public Attachment setOrigFileName(final String origFileName) {
        this.origFileName = origFileName;
        return this;
    }

    public String getOrigFileName() {
        return origFileName;
    }
    
    @Observable
    public Attachment setSha1(final String sha1) {
        this.sha1 = sha1;
        return this;
    }

    public String getSha1() {
        return sha1;
    }

    @Observable
    public Attachment setTitle(final String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }
}
