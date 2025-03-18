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
    <E extends AbstractEntity<?>> Class<AbstractAuditProp<E>> getAuditPropTypeForAuditEntity(Class<AbstractAuditEntity<E>> auditEntityType);

    /**
     * Returns a collection of all versions of an audit entity type for the specified entity type.
     * <p>
     * It is an error if an audit entity type for the specified entity type doesn't exist.
     */
    <E extends AbstractEntity<?>> Collection<Class<AbstractAuditEntity<E>>> getAllAuditEntityTypesFor(Class<? extends AbstractEntity<?>> entityType);

    /**
     * If the specified entity type is audited, returns a collection of all versions of its audit entity type.
     * Otherwise, returns an empty collection.
     */
    <E extends AbstractEntity<?>> Collection<Class<AbstractAuditEntity<E>>> findAllAuditEntityTypesFor(Class<? extends AbstractEntity<?>> entityType);

    /**
     * Locates and returns the synthetic audit-entity type for the specified entity type.
     * <p>
     * It is an error if an audit-entity type for the specified entity type doesn't exist.
     */
    <E extends AbstractEntity<?>> Class<AbstractSynAuditEntity<E>> getSynAuditEntityType(Class<E> entityType);

    /**
     * Locates the synthetic audit-entity type for the specified entity type and returns it if found.
     * Otherwise, returns an empty optional.
     */
    <E extends AbstractEntity<?>> Optional<Class<AbstractSynAuditEntity<E>>> findSynAuditEntityType(Class<E> entityType);

    /**
     * Locates and returns the synthetic audit-prop type for the specified synthetic audit-entity type.
     * <p>
     * It is an error if the requested synthetic audit-prop type doesn't exist.
     */
    <E extends AbstractEntity<?>> Class<AbstractSynAuditProp<E>> getSynAuditPropTypeForSynAuditEntity(Class<AbstractSynAuditEntity<E>> type);

    /**
     * Locates the synthetic audit-prop type for the specified synthetic audit-entity type and returns it if found.
     * Otherwise, returns an empty optional.
     */
    <E extends AbstractEntity<?>> Optional<Class<AbstractSynAuditProp<E>>> findSynAuditPropTypeForSynAuditEntity(Class<AbstractSynAuditEntity<E>> type);

}
