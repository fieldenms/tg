package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.security.user.User;

import java.util.Date;
import java.util.Set;

/**
 * Base type for all audit-entity types.
 * <p>
 * It is expected that in a typical scenario an audit-entity type has a key-member property representing a reference to the audited entity.
 * However, to provide more control in atypical scenarios, this base type leaves it up to specific audit-entity types to declare such a property.
 * <p>
 * It is also expected that each audit-entity type will participate in a one-to-many association with an entity type derived
 * from {@link AbstractAuditProp}, to represent properties, values of which changed during an audit event.
 * Thus, all audit-types are expected to declare a corresponding collectional property and implement its accessor - {@link #getChangedProps()};
 * an abstract setter is not declared in this base type because there is no suitable type for the setter's parameter
 * (method types are contravariant in the parameter type).
 *
 * @param <E>  type of the audited entity
 */
@KeyType(DynamicEntityKey.class)
public abstract class AbstractAuditEntity<E extends AbstractEntity<?>> extends AbstractEntity<DynamicEntityKey> {

    public static final String A3T = "a3t";

    // References to property names declared in this type
    public static final String
            AUDITED_VERSION = "auditedVersion",
            AUDIT_DATE = "auditDate",
            USER = "user",
            AUDITED_TRANSACTION_GUID = "auditedTransactionGuid";

    static final int NEXT_COMPOSITE_KEY_MEMBER = 2;

    public abstract E getAuditedEntity();

    public abstract AbstractAuditEntity<E> setAuditedEntity(E auditedEntity);

    public abstract Set<? extends AbstractAuditProp<? extends AbstractAuditEntity<E>>> getChangedProps();

    @IsProperty
    @MapTo
    @Title(value = "Audited entity version", desc = "Version of the entity for which this audit record was created.")
    @CompositeKeyMember(1)
    private Long auditedVersion;

    @IsProperty
    @Title(value = "Date", desc = "Date/time the audited event took place.")
    @MapTo
    @Final
    @Required
    private Date auditDate;

    @IsProperty
    @Title(value = "User", desc = "User who performed the audited event.")
    @MapTo
    @Final
    @Required
    private User user;

    @IsProperty
    @Title(value = "Audited Transaction ID", desc = "A unique identifier of the transaction for the audited event.")
    @MapTo
    @Final
    @Required
    private String auditedTransactionGuid;

    /**
     * Dynamic getter for accessing values of audited properties.
     * Given the name of an audited property as declared in the audited entity, this method accesses the value of a corresponding property in this audit-entity.
     * It is an error if the specified property is not audited by this audit-entity.
     *
     * @param property  simple property name
     */
    public final <T> T getA3t(final CharSequence property) {
        return get(AuditUtils.auditPropertyName(property));
    }

    /**
     * Dynamic setter for setting values of audited properties.
     * Given the name of an audited property as declared in the audited entity, this method sets the value of a corresponding property in this audit-entity.
     * It is an error if the specified property is not audited by this audit-entity.
     *
     * @param property  simple property name
     */
    public final AbstractAuditEntity<E> setA3t(final CharSequence property, final Object value) {
        set(AuditUtils.auditPropertyName(property), value);
        return this;
    }

    public String getAuditedTransactionGuid() {
        return auditedTransactionGuid;
    }

    @Observable
    public AbstractAuditEntity<E> setAuditedTransactionGuid(final String auditedTransactionGuid) {
        this.auditedTransactionGuid = auditedTransactionGuid;
        return this;
    }

    public User getUser() {
        return user;
    }

    @Observable
    public AbstractAuditEntity<E> setUser(final User user) {
        this.user = user;
        return this;
    }

    public Date getAuditDate() {
        return auditDate;
    }

    @Observable
    public AbstractAuditEntity<E> setAuditDate(final Date auditDate) {
        this.auditDate = auditDate;
        return this;
    }

    public Long getAuditedVersion() {
        return auditedVersion;
    }

    @Observable
    public AbstractAuditEntity<E> setAuditedVersion(final Long auditedVersion) {
        this.auditedVersion = auditedVersion;
        return this;
    }

}
