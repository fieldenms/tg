package ua.com.fielden.platform.audit;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Collection;
import java.util.Optional;

/**
 * Locates audit types.
 */
@ImplementedBy(AuditTypeFinder.class)
public interface IAuditTypeFinder {

    /**
     * Locates and returns the most recent audit-entity type for the specified entity type.
     * <p>
     * It is an error if an audit-entity type for the specified entity type doesn't exist.
     */
    <E extends AbstractEntity<?>> Class<AbstractAuditEntity<E>> getAuditEntityType(Class<E> entityType);

    /**
     * Locates the most recent audit-entity type for the specified entity type and returns it if found.
     * Otherwise, returns an empty optional.
     */
    <E extends AbstractEntity<?>> Optional<Class<AbstractAuditEntity<E>>> findAuditEntityType(Class<E> entityType);

    /**
     * Locates and returns the an audit-entity type with the specified version for the specified entity type.
     * <p>
     * It is an error if an audit-entity type with the specified version doesn't exist.
     */
    <E extends AbstractEntity<?>> Class<AbstractAuditEntity<E>> getAuditEntityType(Class<E> entityType, int version);

    /**
     * Locates an audit-entity type with the specified version for the specified entity type and returns it if found.
     * Otherwise, returns an empty optional.
     */
    <E extends AbstractEntity<?>> Optional<Class<AbstractAuditEntity<E>>> findAuditEntityType(Class<E> entityType, int version);

    /**
     * Locates and returns an audit-prop type for the specified audit-entity type.
     * <p>
     * It is an error if an audit-prop type for the specified audit-entity type doesn't exist.
     */
    <AE extends AbstractAuditEntity<?>> Class<AbstractAuditProp<AE>> getAuditPropTypeForAuditEntity(Class<AE> auditEntityType);

    /**
     * Returns a collection of all versions of an audit entity type for the specified entity type.
     * <p>
     * It is an error if an audit entity type for the specified entity type doesn't exist.
     */
    <E extends AbstractEntity<?>> Collection<Class<AbstractAuditEntity<E>>> getAllAuditEntityTypesFor(Class<? extends AbstractEntity<?>> entityType);

    /**
     * If the specified entity type is audited, returns a collection of all versions of its audit entity type.
     * Otherwise, returns an empty stream.
     */
    <E extends AbstractEntity<?>> Collection<Class<AbstractAuditEntity<E>>> findAllAuditEntityTypesFor(Class<? extends AbstractEntity<?>> entityType);

}
