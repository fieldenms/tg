package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.audit.Audited;
import ua.com.fielden.platform.audit.DisableAuditing;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.types.RichText;

import java.util.Date;

/**
 * Entity for testing of auditing facilitites.
 * Its use should be limited to auditing tests, as its structure is prone to change.
 */
@KeyType(String.class)
@MapEntityTo
@CompanionObject(AuditedEntityCo.class)
@Audited
public class AuditedEntity extends AbstractPersistentEntity<String> {

    public enum Property implements IConvertableToPath {
        date1, bool1, str2, str3, richText, union, invalidate;

        @Override
        public String toPath() {
            return name();
        }
    }

    @IsProperty
    @MapTo
    private Date date1;

    @IsProperty
    @MapTo
    private boolean bool1;

    @IsProperty
    @MapTo
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

}
