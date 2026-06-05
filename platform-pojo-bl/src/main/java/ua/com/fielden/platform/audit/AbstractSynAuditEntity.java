package ua.com.fielden.platform.audit;

import com.google.common.collect.ImmutableSet;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.security.user.User;

import java.util.Date;
import java.util.Set;

/// Base type for synthetic audit-entity types.
///
/// It is expected that an audit-entity type declares key-member property [#AUDITED_ENTITY] that represents a reference to the audited entity.
/// This property cannot be declared in this base type due to a limitation on using type variables in property types.
///
/// @param <E>  type of the audited entity
///
@KeyType(DynamicEntityKey.class)
public abstract class AbstractSynAuditEntity<E extends AbstractEntity<?>> extends AbstractEntity<DynamicEntityKey> {

    // References to property names declared in this type
    public static final String
            AUDITED_VERSION = "auditedVersion",
            AUDIT_DATE = "auditDate",
            AUDIT_USER = "auditUser",
            AUDITED_TRANSACTION_GUID = "auditedTransactionGuid";

    /// Name of a property declared by subtypes.
    /// This property is a key-member representing a reference to the audited entity.
    /// Its type is the type of the audited entity.
    ///
    /// For example, audit-entity `ReVehicle_a3t` is expected to declare property `auditedEntity` of type `Vehicle`.
    ///
    public static final String AUDITED_ENTITY = "auditedEntity";

    /// The composite key order that must be specified by the [#AUDITED_ENTITY] property.
    ///
    /// Placing this property first ensures that its column comes first in the multi-column unique index that is always generated for composite keys
    /// This improves performance because the first column in such an index is searchable.
    ///
    static final int AUDITED_ENTITY_KEY_MEMBER_ORDER = 1;

    /// Name of a property declared by subtypes.
    /// This property models the one-2-many relationship between this synthetic audit-entity and an [AbstractSynAuditProp] entity.
    /// Its type is a [Set] parameterised with a corresponding synthetic audit-prop type.
    ///
    /// For example, synthetic audit-entity `ReVehicle_a3t` is expected to declare property `changedProps` of type `Set<ReVehicle_a3t_Prop>`.
    ///
    public static final String CHANGED_PROPS = "changedProps";

    /// Name of a property declared by subtypes.
    /// This property is a criterion for searching by changed properties (i.e., by property [#CHANGED_PROPS]).
    /// Its type is a [PropertyDescriptor] parameterised with the synthetic audit-entity type itself.
    ///
    /// For example, synthetic audit-entity `ReVehicle_a3t` is expected to declare property `changedPropsCrit`
    /// of type `PropertyDescriptor<ReVehicle_a3t>`.
    ///
    public static final String CHANGED_PROPS_CRIT = "changedPropsCrit";

    public static final Set<String> BASE_PROPERTIES = ImmutableSet.of(
            AUDITED_ENTITY, AUDITED_VERSION, AUDIT_DATE, AUDIT_USER, AUDITED_TRANSACTION_GUID
    );

    @IsProperty
    @Title(value = "Version", desc = "Version of the entity for which this audit record was created.")
    @CompositeKeyMember(2)
    @DenyIntrospection
    private Long auditedVersion;

    @IsProperty
    @Title(value = "Audit Date", desc = "Date/time the audited event took place.")
    @DenyIntrospection
    private Date auditDate;

    @IsProperty
    @Title(value = "Audit User", desc = "User who performed the audited event.")
    @DenyIntrospection
    private User auditUser;

    @IsProperty
    @Title(value = "Audit Transaction ID", desc = "A unique identifier of the transaction for the audited event.")
    @DenyIntrospection
    private String auditedTransactionGuid;

    /// Getter for property [#AUDITED_ENTITY].
    ///
    public abstract E getAuditedEntity();

    /// Getter for property [#CHANGED_PROPS].
    ///
    public abstract Set<? extends AbstractSynAuditProp<E>> getChangedProps();

    /// Dynamic getter for accessing values of audited properties.
    /// Given the name of an audited property as declared in the audited entity, this method accesses the value of a corresponding property in this audit-entity.
    /// It is an error if the specified property is not audited by this audit-entity.
    ///
    /// @param property  simple property name
    ///
    public final <T> T getA3t(final CharSequence property) {
        return get(AuditUtils.auditPropertyName(property));
    }

    public String getAuditedTransactionGuid() {
        return auditedTransactionGuid;
    }

    @Observable
    public AbstractSynAuditEntity<E> setAuditedTransactionGuid(final String auditedTransactionGuid) {
        this.auditedTransactionGuid = auditedTransactionGuid;
        return this;
    }

    public User getAuditUser() {
        return auditUser;
    }

    @Observable
    public AbstractSynAuditEntity<E> setAuditUser(final User auditUser) {
        this.auditUser = auditUser;
        return this;
    }

    public Date getAuditDate() {
        return auditDate;
    }

    @Observable
    public AbstractSynAuditEntity<E> setAuditDate(final Date auditDate) {
        this.auditDate = auditDate;
        return this;
    }

    public Long getAuditedVersion() {
        return auditedVersion;
    }

    @Observable
    public AbstractSynAuditEntity<E> setAuditedVersion(final Long auditedVersion) {
        this.auditedVersion = auditedVersion;
        return this;
    }

}
