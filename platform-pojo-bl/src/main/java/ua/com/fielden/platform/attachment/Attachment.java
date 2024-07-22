package ua.com.fielden.platform.attachment;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

import ua.com.fielden.platform.attachment.definers.AssignAttachmentTitle;
import ua.com.fielden.platform.attachment.definers.UpdateAttachmentRevNo;
import ua.com.fielden.platform.attachment.validators.AttachmentTitleValidator;
import ua.com.fielden.platform.attachment.validators.CanBeUsedAsLastAttachmentRev;
import ua.com.fielden.platform.attachment.validators.CanBeUsedAsPrevAttachmentRev;
import ua.com.fielden.platform.attachment.validators.IsRevNoAlignedWithPrevRevision;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.RestrictCreationByUsers;
import ua.com.fielden.platform.entity.annotation.SkipDefaultStringKeyMemberValidation;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.validation.MaxLengthValidator;
import ua.com.fielden.platform.entity.validation.RestrictCommasValidator;
import ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.utils.Pair;

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
@KeyType(value = DynamicEntityKey.class, keyMemberSeparator = " | SHA1: ")
@KeyTitle("Attachment")
@DescTitle(value = "Description", desc = "A summary about the attachment, which can be used for search.")
@DisplayDescription
@MapEntityTo
@CompanionObject(IAttachment.class)
@RestrictCreationByUsers
public class Attachment extends AbstractPersistentEntity<DynamicEntityKey> {
    public static final String HYPERLINK = "[hyperlink]";
    public static final String pn_TITLE = "title";
    public static final String pn_SHA1 = "sha1";
    public static final String pn_ORIG_FILE_NAME = "origFileName";
    public static final String pn_REV_NO = "revNo";
    public static final String pn_PREV_REVISION = "prevRevision";
    public static final String pn_LAST_REVISION = "lastRevision";
    public static final String pn_LAST_MODIFIED = "lastModified";
    public static final String pn_MIME = "mime";
    public static final String pn_IS_LATEST_REV = "latestRev";
    public static final String pn_LATITUDE = "latitude";
    public static final String pn_LONGITUDE = "longitude";


    private static final Pair<String, String> entityTitleAndDesc = getEntityTitleAndDesc(Attachment.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty(length = 2048)
    @MapTo
    @Title(value = "Title or Link", desc = "A convenient document title or a link to an external resource")
    @CompositeKeyMember(1)
    @BeforeChange({@Handler(MaxLengthValidator.class), @Handler(AttachmentTitleValidator.class)})
    @SkipDefaultStringKeyMemberValidation({RestrictExtraWhitespaceValidator.class, RestrictCommasValidator.class}) // see AttachmentTitleValidator for more details
    private String title;

    @IsProperty
    @MapTo
    @Title(value = "SHA1", desc = "A unique SHA1-based checksum of the file referenced by this attachment.")
    @CompositeKeyMember(2)
    @Readonly
    @Final(persistedOnly = false)
    private String sha1;

    @IsProperty(length = 2048)
    @MapTo
    @Title(value = "File Name", desc = "The file name of the uploaded document or a link indication.")
    @Readonly
    @Required
    @Final(persistedOnly = false)
    @Dependent(pn_TITLE)
    @BeforeChange(@Handler(MaxLengthValidator.class))
    @AfterChange(AssignAttachmentTitle.class)
    private String origFileName;

    @IsProperty
    @MapTo
    @Title(value = "Last Modified", desc = "The date/time of the last file modification.")
    @Readonly
    @Final(persistedOnly = false)
    private Date lastModified;

    @IsProperty
    @MapTo
    @Title(value = "MIME", desc = "File MIME type.")
    @Readonly
    @Final(persistedOnly = false)
    private String mime;

    @IsProperty
    @MapTo
    @Title(value = "Rev#", desc = "Attachment revision number.")
    @Readonly
    @Required
    @BeforeChange(@Handler(IsRevNoAlignedWithPrevRevision.class))
    private Integer revNo;

    @IsProperty
    @MapTo
    @Title(value = "Prev. Rev.", desc = "An attachment that represent the previous revision of this document. Empty if there is no previous revision.")
    @Final
    @BeforeChange(@Handler(CanBeUsedAsPrevAttachmentRev.class))
    @AfterChange(UpdateAttachmentRevNo.class)
    private Attachment prevRevision;

    @IsProperty
    @MapTo
    @Title(value = "Latest Rev.", desc = "An attachment that represents the latest revision of this document. Empty if there is no revision history. References itself if there is revision history.")
    @Readonly
    @BeforeChange(@Handler(CanBeUsedAsLastAttachmentRev.class))
    private Attachment lastRevision;

    @IsProperty
    @Readonly
    @Calculated
    @Title(value = "Latest revision?", desc = "Indicates if the attachment represent the latest revision of the associated file.")
    private boolean latestRev;
    protected static final ExpressionModel latestRev_ = expr()
            .caseWhen().begin()
            .prop(pn_LAST_REVISION).isNull().or()
            .prop(ID).eq().prop(pn_LAST_REVISION)
            .end().then().val(true)
            .otherwise().val(false).endAsBool().model();

    // Latitude range: [-90, 90]
    // 6 decimal places using decimal degrees notation is at a 10 cm resolution
    @IsProperty(precision = 8, scale = 6)
    @MapTo
    @Title(value = "Latitude (North)", desc = "Latitude (North) from GPS coordinates embedded within this attachment.")
    private BigDecimal latitude;

    // Longitude range: [-180, 180]
    @IsProperty(precision = 9, scale = 6)
    @MapTo
    @Title(value = "Longitude (East)", desc = "Longitude (East) from GPS coordinates embedded within this attachment.")
    private BigDecimal longitude;

    public BigDecimal getLongitude() {
        return longitude;
    }

    @Observable
    public Attachment setLongitude(final BigDecimal longitude) {
        this.longitude = longitude;
        return this;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    @Observable
    public Attachment setLatitude(final BigDecimal latitude) {
        this.latitude = latitude;
        return this;
    }

    /**
     * A necessary flag to allow modification of the last revision upon revision history rewriting.
     * The last revision should not be modifiable in any other circumstances.
     */
    private boolean allowLastRevisionUpdate = false;

    /**
     *  A convenient predicate determining whether an attachment represents a hyperlink.
     *
     * @return
     */
    public boolean isHyperlinkAttachment() {
        return HYPERLINK.equals(getOrigFileName());
    }

    public boolean isLastRevisionUpdateAllowed() {
        return allowLastRevisionUpdate;
    }

    Attachment beginLastRevisionUpdate() {
        allowLastRevisionUpdate = true;
        return this;
    }

    Attachment endLastRevisionUpdate() {
        allowLastRevisionUpdate = false;
        return this;
    }

    @Observable
    public Attachment setLastRevision(final Attachment lastRevision) {
        this.lastRevision = lastRevision;
        return this;
    }

    public Attachment getLastRevision() {
        return lastRevision;
    }

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

    @Observable
    public Attachment setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public Date getLastModified() {
        return lastModified;
    }

    @Observable
    public Attachment setMime(final String mime) {
        this.mime = mime;
        return this;
    }

    public String getMime() {
        return mime;
    }

    @Observable
    protected Attachment setLatestRev(final boolean latestRevision) {
        latestRev = latestRevision;
        return this;
    }

    public boolean isLatestRev() {
        return latestRev;
    }

}
