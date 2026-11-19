package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.audit.annotations.Audited;
import ua.com.fielden.platform.audit.annotations.DisableAuditing;
import ua.com.fielden.platform.audit.annotations.InactiveAuditProperty;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

import java.util.Date;

/// Entity for testing of auditing facilities.
/// Its use should be limited to auditing tests, as its structure is prone to change.
///
/// This entity emulates a scenario where its structure has changed over time, resulting in several audit-entity types:
/// - [AuditedEntity_a3t_2] reflects the removal of property `str1` as property `a3t_str1`, annotated with [InactiveAuditProperty].
/// - [AuditedEntity_a3t_3] audits the properties added afterwards (`colour`, `hyperlink`).
/// - [AuditedEntity_a3t_4] reflects invisible properties:
///   new properties `secret` and `token`, annotated with [Invisible], and `date1`, which was annotated with `@Invisible(centreOnly = true)` after it had been audited.
///   It also audits new property `note`, which was not invisible at the time.
/// - [AuditedEntity_a3t_5] reflects the removal of invisible property `token`,
///   and property `note` being annotated with both [Invisible] and [DisableAuditing]:
///   their audit-properties are inactive and invisible.
///
@KeyType(String.class)
@MapEntityTo
@CompanionObject(AuditedEntityCo.class)
@Audited
public class AuditedEntity extends AbstractPersistentEntity<String> {

    public enum Property implements IConvertableToPath {
        key, date1, date2, bool1, str2, str3, richText, union, invalidate, colour, hyperlink, secret, note,
        // Removed properties that are retained by previous audit-entity types.
        str1, token;

        @Override
        public String toPath() {
            return name();
        }
    }

    @IsProperty
    @MapTo
    @Invisible(centreOnly = true)
    private Date date1;

    @IsProperty
    @MapTo
    @PersistentType(userType = IUtcDateTimeType.class) // Uses @PersistentType to test its propagation to audit-entity properties.
    private Date date2;

    @IsProperty
    @MapTo
    private boolean bool1;

    @IsProperty
    @MapTo("STR2") // To test generation of @MapTo in the audit-entity.
    private String str2;

    @IsProperty
    @MapTo
    @DisableAuditing
    private String str3;

    @IsProperty
    @MapTo
    private RichText richText;

    @IsProperty
    @MapTo
    private UnionEntity union;

    // Indicates whether this instance should be made invalid.
    // The invalidation logic is implemented in the definer.
    // This models a real-world scenario of a persisted entity becoming invalid upon retrieval from a persistent store.
    @IsProperty
    @MapTo
    @AfterChange(AuditedEntityInvalidateDefiner.class)
    private boolean invalidate;

    @IsProperty
    @MapTo
    private Colour colour;

    @IsProperty
    @MapTo
    private Hyperlink hyperlink;

    @IsProperty
    @MapTo
    @Invisible
    private String secret;

    @IsProperty
    @MapTo
    @Invisible
    @DisableAuditing
    private String note;

    public String getSecret() {
        return secret;
    }

    @Observable
    public AuditedEntity setSecret(final String secret) {
        this.secret = secret;
        return this;
    }

    public String getNote() {
        return note;
    }

    @Observable
    public AuditedEntity setNote(final String note) {
        this.note = note;
        return this;
    }

    public Colour getColour() {
        return colour;
    }

    @Observable
    public AuditedEntity setColour(final Colour colour) {
        this.colour = colour;
        return this;
    }

    public Hyperlink getHyperlink() {
        return hyperlink;
    }

    @Observable
    public AuditedEntity setHyperlink(final Hyperlink hyperlink) {
        this.hyperlink = hyperlink;
        return this;
    }

    public boolean getInvalidate() {
        return invalidate;
    }

    @Observable
    public AuditedEntity setInvalidate(final boolean invalidate) {
        this.invalidate = invalidate;
        return this;
    }

    public UnionEntity getUnion() {
        return union;
    }

    @Observable
    public AuditedEntity setUnion(final UnionEntity union) {
        this.union = union;
        return this;
    }

    public RichText getRichText() {
        return richText;
    }

    @Observable
    public AuditedEntity setRichText(final RichText richText) {
        this.richText = richText;
        return this;
    }

    public String getStr3() {
        return str3;
    }

    @Observable
    public AuditedEntity setStr3(final String str3) {
        this.str3 = str3;
        return this;
    }

    public String getStr2() {
        return str2;
    }

    @Observable
    public AuditedEntity setStr2(final String str2) {
        this.str2 = str2;
        return this;
    }

    public boolean getBool1() {
        return bool1;
    }

    @Observable
    public AuditedEntity setBool1(final boolean bool1) {
        this.bool1 = bool1;
        return this;
    }

    public Date getDate1() {
        return date1;
    }

    @Observable
    public AuditedEntity setDate1(final Date date1) {
        this.date1 = date1;
        return this;
    }

    public Date getDate2() {
        return date2;
    }

    @Observable
    public AuditedEntity setDate2(final Date date2) {
        this.date2 = date2;
        return this;
    }

}
