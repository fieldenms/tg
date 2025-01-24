package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

/**
 * Base type for entity types that model a one-2-many association with an audit-entity to represent properties of the audited entity whose values were changed as part of an audit event.
 * <p>
 * Specific types derived from this one are expected to declare properties {@link #AUDIT_ENTITY} and {@link #PROPERTY}.
 * These properties cannot be declared in this base type due to a limitation on using type variables in property types.
 *
 * @param <AE>  type of the audit-entity
 */
@KeyType(DynamicEntityKey.class)
public abstract class AbstractAuditProp<AE extends AbstractAuditEntity<?>> extends AbstractEntity<DynamicEntityKey> {

    /**
     * Name of the property that is declared by specific audit-prop types.
     * This property is a key-member representing a reference to the audit-entity.
     * Its type is the type of the audit-entity.
     * <p>
     * For example, audit-prop type {@code VehicleAuditProp} is expected to declare property {@code auditEntity} of type {@code VehicleAudit}.
     */
    public static final String AUDIT_ENTITY = "auditEntity";

    /**
     * Name of the property that is declared by specific audit-prop types.
     * This property is a key-member representing a changed property.
     * <p>
     * Its type is a {@link PropertyDescriptor} parameterised with the type of a corresponding <b>synthetic audit-entity</b>.
     * The choice of the type parameter is motivated by the evolutionary model of audit-entity types.
     * An audited entity type cannot be used because it could result in a persisted property descriptor becoming invalid due to structural changes to that audited type.
     */
    public static final String PROPERTY = "property";

    /**
     * Getter for property {@link #AUDIT_ENTITY}.
     */
    public abstract AE getAuditEntity();

    /**
     * Setter for property {@link #AUDIT_ENTITY}.
     */
    public abstract AbstractAuditProp<AE> setAuditEntity(AE entity);

    /**
     * Getter for property {@link #PROPERTY}.
     */
    public abstract PropertyDescriptor<? extends AbstractSynAuditEntity<?>> getProperty();

    /**
     * Setter for property {@link #PROPERTY}.
     */
    public abstract AbstractAuditProp<AE> setProperty(PropertyDescriptor<? extends AbstractSynAuditEntity<?>> property);

}
