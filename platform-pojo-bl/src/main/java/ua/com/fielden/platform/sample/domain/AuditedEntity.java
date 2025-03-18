package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.audit.Audited;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.*;

import java.util.Date;

/**
 * Entity for testing of auditing facilitites.
 * Its use should be limited to auditing tests, as its structure is prone to change.
 */
@KeyType(String.class)
@MapEntityTo
@Audited
public class AuditedEntity extends AbstractPersistentEntity<String> {

    @IsProperty
    @MapTo
    private Date date1;

    @IsProperty
    @MapTo
    private boolean bool1;

    @IsProperty
    @MapTo
    private String str2;

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
