package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

/**
 * Base type for synthetic {@linkplain AbstractAuditProp audit-prop entity types}.
 * <p>
 * Specific types derived from this one are expected to declare properties {@link #AUDIT_ENTITY} and {@link #PROPERTY}.
 * These properties cannot be declared in this base type due to a limitation on using type variables in property types.
 *
 * @param <E>  type of the audited entity
 */
@KeyType(DynamicEntityKey.class)
public abstract class AbstractSynAuditProp<E extends AbstractEntity<?>> extends AbstractEntity<DynamicEntityKey> {

    /**
     * Name of a property declared by subtypes.
     * This property is a key-member representing a reference to the corresponding {@linkplain AbstractSynAuditEntity synthetic audit-entity}.
     * <p>
     * For example, synthetic audit-prop type {@code ReVehicle_a3t_Prop} is expected to declare property {@code auditEntity} of type {@code ReVehicle_a3t}.
     */
    public static final String AUDIT_ENTITY = "auditEntity";

    /**
     * Name of a property declared by subtypes.
     * This property is a key-member representing a changed property.
     * <p>
     * Its type is a {@link PropertyDescriptor} parameterised with the type of the corresponding synthetic audit-entity.
     * <p>
     * For example, synthetic audit-prop type {@code ReVehicle_a3t_Prop} is expected to declare property {@code property}
     * of type {@code PropertyDescriptor<ReVehicle_a3t>}.
     */
    public static final String PROPERTY = "property";

    /**
     * Getter for property {@link #AUDIT_ENTITY}.
     */
    public abstract AbstractSynAuditEntity<E> getAuditEntity();

    /**
     * Setter for property {@link #AUDIT_ENTITY}.
     */
    public abstract AbstractSynAuditProp<E> setAuditEntity(AbstractSynAuditEntity<E> entity);

    /**
     * Getter for property {@link #PROPERTY}.
     */
    public abstract PropertyDescriptor<AbstractSynAuditEntity<E>> getProperty();

    /**
     * Setter for property {@link #PROPERTY}.
     */
    public abstract AbstractSynAuditProp<E> setProperty(PropertyDescriptor<AbstractSynAuditEntity<E>> propertyDescriptor);

}
