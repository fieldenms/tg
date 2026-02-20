package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

/// Base type for entity types that model a one-2-many association with an audit-entity to represent properties of the audited entity whose values were changed as part of an audit event.
///
/// Specific types derived from this one are expected to declare properties [#AUDIT_ENTITY] and [#PROPERTY].
/// These properties cannot be declared in this base type due to a limitation on using type variables in property types.
///
/// @param <E>  type of the audited entity
///
@KeyType(DynamicEntityKey.class)
public abstract class AbstractAuditProp<E extends AbstractEntity<?>> extends AbstractEntity<DynamicEntityKey> {

    /// Name of the property that is declared by specific audit-prop types.
    /// This property is a key-member representing a reference to the audit-entity.
    /// Its type is the type of the audit-entity.
    ///
    /// For example, audit-prop type `VehicleAuditProp` is expected to declare property `auditEntity` of type `VehicleAudit`.
    ///
    public static final String AUDIT_ENTITY = "auditEntity";

    /// Name of the property that is declared by specific audit-prop types.
    /// This property is a key-member representing a changed property.
    ///
    /// Its type is a [PropertyDescriptor] parameterised with the type of corresponding **synthetic audit-entity**.
    /// The choice of the type parameter is motivated by the evolutionary model of audit-entity types.
    /// An audited entity type cannot be used because it could result in a persisted property descriptor becoming invalid due to structural changes to that audited type.
    ///
    public static final String PROPERTY = "property";

    /// Getter for property [#AUDIT_ENTITY].
    ///
    public abstract AbstractAuditEntity<E> getAuditEntity();

    /// Setter for property [#AUDIT_ENTITY].
    ///
    public abstract AbstractAuditProp<E> setAuditEntity(AbstractAuditEntity<E> entity);

    /// Getter for property [#PROPERTY].
    ///
    public abstract PropertyDescriptor<AbstractSynAuditEntity<E>> getProperty();

    /// Setter for property [#PROPERTY].
    ///
    public abstract AbstractAuditProp<E> setProperty(PropertyDescriptor<AbstractSynAuditEntity<E>> property);

}
