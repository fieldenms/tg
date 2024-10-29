package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

/**
 * Base type for entity types that model a one-2-many association with an audit-entity to represent properties of the audited entity that were modified as part of an audit event.
 * <p>
 * This type does not declare any properties due to limited support for generically-typed properties.
 * Therefore, it is up to specific entity types to declare the required properties, which are:
 * <ol>
 *   <li> {@code auditEntity: AE}; composite key-member.
 *   <li> {@code property: PropertyDescriptor<AE>}; composite key-member.
 *        <p>
 *        Audit-entity type is used to parameterise the property descriptor because of the evolutionary model of audit-entity types.
 *        The audited type cannot be used as it could result in a persisted property descriptor becoming invalid due to structural changes to the audited type.
 * </ol>
 *
 * @param <AE>  type of the audit-entity
 */
@KeyType(DynamicEntityKey.class)
public abstract class AbstractAuditProp<AE extends AbstractAuditEntity<?>> extends AbstractEntity<DynamicEntityKey> {

    public abstract AE getAuditedEntity();

    public abstract AbstractAuditProp<AE> setAuditedEntity(AE entity);

    public abstract PropertyDescriptor<AE> getProperty();

    public abstract AbstractAuditProp<AE> setProperty(PropertyDescriptor<AE> propertyDescriptor);

}
