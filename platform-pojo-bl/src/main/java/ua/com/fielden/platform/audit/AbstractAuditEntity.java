package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.security.user.User;

import java.util.Date;

/**
 * Base type for all audit-entity types.
 * <p>
 * It is expected that in a typical scenario an audit-entity type has a key-member property representing a reference to the audited entity.
 * However, to provide more control in atypical scenarios, this base type leaves it up to specific audit-entity types to declare such a property.
 *
 * @param <E>  type of the audited entity
 */
@KeyType(DynamicEntityKey.class)
public abstract class AbstractAuditEntity<E extends AbstractEntity<?>> extends AbstractEntity<DynamicEntityKey> {

    static final int NEXT_COMPOSITE_KEY_MEMBER = 3;

    public abstract E getAuditedEntity();

    public abstract AbstractAuditEntity<E> setAuditedEntity(E auditedEntity);

    @IsProperty
    @Title(value = "Date", desc = "Date/time the audited event took place.")
    @MapTo
    @Final
    @CompositeKeyMember(1)
    private Date auditDate;

    @IsProperty
    @Title(value = "User", desc = "User who performed the audited event.")
    @MapTo
    @Final
    @CompositeKeyMember(2)
    private User user;

    @IsProperty
    @Title(value = "Audit Transaction ID", desc = "A unique identifier of the audit transaction for this audit record.")
    @MapTo
    @Final
    private String auditTransactionGuid;

    public String getAuditTransactionGuid() {
        return auditTransactionGuid;
    }

    @Observable
    public AbstractAuditEntity<E> setAuditTransactionGuid(final String auditTransactionGuid) {
        this.auditTransactionGuid = auditTransactionGuid;
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

}
