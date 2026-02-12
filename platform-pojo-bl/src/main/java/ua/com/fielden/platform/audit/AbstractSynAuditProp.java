package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

/// Base type for synthetic {@linkplain AbstractAuditProp audit-prop entity types}.
///
/// Specific types derived from this one are expected to declare properties [#AUDIT_ENTITY] and [#PROPERTY].
/// These properties cannot be declared in this base type due to a limitation on using type variables in property types.
///
/// @param <E>  type of the audited entity
///
@KeyType(DynamicEntityKey.class)
public abstract class AbstractSynAuditProp<E extends AbstractEntity<?>> extends AbstractEntity<DynamicEntityKey> {

    /// Name of a property declared by subtypes.
    /// This property is a key-member representing a reference to the corresponding {@linkplain AbstractSynAuditEntity synthetic audit-entity}.
    ///
    /// For example, synthetic audit-prop type `ReVehicle_a3t_Prop` is expected to declare property `auditEntity` of type `ReVehicle_a3t`.
    ///
    public static final String AUDIT_ENTITY = "auditEntity";

    /// Name of a property declared by subtypes.
    /// This property is a key-member representing a changed property.
    ///
    /// Its type is a [PropertyDescriptor] parameterised with the type of the corresponding synthetic audit-entity.
    ///
    /// For example, synthetic audit-prop type `ReVehicle_a3t_Prop` is expected to declare property `property`
    /// of type `PropertyDescriptor<ReVehicle_a3t>`.
    ///
    public static final String PROPERTY = "property";

    /// Getter for property [#AUDIT_ENTITY].
    ///
    public abstract AbstractSynAuditEntity<E> getAuditEntity();

    /// Setter for property [#AUDIT_ENTITY].
    ///
    public abstract AbstractSynAuditProp<E> setAuditEntity(AbstractSynAuditEntity<E> entity);

    /// Getter for property [#PROPERTY].
    ///
    public abstract PropertyDescriptor<AbstractSynAuditEntity<E>> getProperty();

    /// Setter for property [#PROPERTY].
    ///
    public abstract AbstractSynAuditProp<E> setProperty(PropertyDescriptor<AbstractSynAuditEntity<E>> propertyDescriptor);

}
