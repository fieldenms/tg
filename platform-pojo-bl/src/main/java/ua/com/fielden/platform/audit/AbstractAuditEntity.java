package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.security.user.User;

import java.util.Date;

/// Base type for all audit-entity types.
///
/// It is expected that an audit-entity type declares key-member property [#AUDITED_ENTITY] that represents a reference to the audited entity.
/// This property cannot be declared in this base type due to a limitation on using type variables in property types.
///
/// Each audit-entity type is in a one-2-many relationship with [an audit-prop type][AbstractAuditProp],
/// to represent properties, values of which changed during an audit event.
/// This relationship, however, is implicit — it is not modelled via a collectional property.
/// Instead, the union of such associations is explicitly modelled by corresponding
/// [synthetic audit-entity][AbstractSynAuditEntity] and [audit-prop][AbstractSynAuditProp] types.
///
/// Values of property [#auditedTransactionGuid] may have special meaning (see the property's documentation).
///
/// ### Column names
///
/// There is a defined mapping between column names of audited properties and their corresponding audit properties.
///
/// ```
/// columnName(auditProperty) = "A3T_" + columnName(auditedProperty)
/// ```
///
/// This applies even in those cases when an audited property has an explicit column name specified via [MapTo].
///
/// However, there is a caveat: if the column name of an audited property is changed when an audit-entity type already exists,
/// the column name of the corresponding audit property will not change.
///
/// For example:
/// 1. Audit type `User_a3t_1` is generated for `User`, and `User.base` is audited by `User_a3t_1.a3t_base`.
///    * `User.base` maps to column `BASE_`.
///    * `User_a3t_1.a3t_base` maps to column `A3T_BASE_`.
/// 2. `User.base` gets annotated with `@MapTo("_BASE")`.
///    While the column name is now changed for `User.base`, the column name for `User_a3t_1.a3t_base` remains unchanged.
///    In this case, the solution is to manually edit `User_a3t_1.a3t_base` by annotating it with `@MapTo("A3T__BASE")`.
///
/// @param <E>  type of the audited entity
///
@KeyType(DynamicEntityKey.class)
public abstract class AbstractAuditEntity<E extends AbstractEntity<?>> extends AbstractEntity<DynamicEntityKey> {

    public static final String A3T = "a3t";

    // References to property names declared in this type
    public static final String
            AUDITED_VERSION = "auditedVersion",
            AUDIT_DATE = "auditDate",
            AUDIT_USER = "auditUser",
            AUDITED_TRANSACTION_GUID = "auditedTransactionGuid";

    /// Name of the property that is declared by specific audit-entity types.
    /// This property is a key-member representing a reference to the audited entity.
    /// Its type is the type of the audited entity.
    ///
    /// For example, audit-entity `VehicleAudit` is expected to declare property `auditedEntity` of type `Vehicle`.
    ///
    public static final String AUDITED_ENTITY = "auditedEntity";

    /// The composite key order that must be specified by the [#AUDITED_ENTITY] property.
    /// Placing this property first ensures that its column comes first in the multi-column unique index that is always
    /// generated for composite keys, which improves performance because the first column in such an index is searchable.
    static final int AUDITED_ENTITY_KEY_MEMBER_ORDER = 1;

    /// Getter for property [#AUDITED_ENTITY].
    ///
    public abstract E getAuditedEntity();

    /// Setter for property [#AUDITED_ENTITY].
    ///
    public abstract AbstractAuditEntity<E> setAuditedEntity(E auditedEntity);

    @IsProperty
    @MapTo
    @Title(value = "Version", desc = "Version of the entity for which this audit record was created.")
    @CompositeKeyMember(2)
    private Long auditedVersion;

    @IsProperty
    @Title(value = "Audit Date", desc = "Date/time the audited event took place.")
    @MapTo
    @Final
    @Required
    private Date auditDate;

    @IsProperty
    @Title(value = "Audit User", desc = "User who performed the audited event.")
    @MapTo
    @Final
    @Required
    private User auditUser;

    /// The value may be either a transaction GUID or one of the [reserved values][ReservedTransactionGuid].
    ///
    @IsProperty
    @Title(value = "Audit Transaction ID", desc = "A unique identifier of the transaction for the audited event.")
    @MapTo
    @Final
    @Required
    private String auditedTransactionGuid;

    /// Dynamic getter for accessing values of audited properties.
    /// Given the name of an audited property as declared in the audited entity,
    /// this method accesses the value of a corresponding property in this audit-entity.
    /// It is an error if the specified property is not audited by this audit-entity.
    ///
    /// @param property  simple property name
    ///
    public final <T> T getA3t(final CharSequence property) {
        return get(AuditUtils.auditPropertyName(property));
    }

    /// Dynamic setter for setting values of audited properties.
    /// Given the name of an audited property as declared in the audited entity,
    /// this method sets the value of a corresponding property in this audit-entity.
    /// It is an error if the specified property is not audited by this audit-entity.
    ///
    /// @param property  simple property name
    ///
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

    public User getAuditUser() {
        return auditUser;
    }

    @Observable
    public AbstractAuditEntity<E> setAuditUser(final User auditUser) {
        this.auditUser = auditUser;
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

    /// Special values for property [#auditedTransactionGuid].
    ///
    public enum ReservedTransactionGuid {

        /// Indicates that an audit record was imported during data migration, and the data did not contain a transaction GUID.
        ///
        MIGRATED ("MIGRATED");

        public final String value;

        ReservedTransactionGuid(final String value) {
            this.value = value;
        }
    }

}
