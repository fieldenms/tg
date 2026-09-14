package ua.com.fielden.platform.companion;

import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import ua.com.fielden.platform.audit.AbstractAuditEntity;
import ua.com.fielden.platform.audit.AbstractAuditProp;
import ua.com.fielden.platform.audit.AuditingMode;
import ua.com.fielden.platform.audit.IAuditTypeFinder;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.dao.exceptions.EntityDeletionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.EntityBatchDeleteByIdsOperation;
import ua.com.fielden.platform.entity.query.EntityBatchDeleteByQueryModelOperation;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.eql.meta.EqlTables;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;

import javax.persistence.PersistenceException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.partition;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.hibernate.LockOptions.UPGRADE;
import static ua.com.fielden.platform.audit.AuditUtils.isAudited;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.exceptions.InvalidArgumentException.requireNonNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isActivatableReference;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;

/// Various delete operations that are used by entity companions.
/// The main purpose of this class is to be more like a mixin that provides an implementation of delete operations.
///
/// ### Deletion of audited entities
///
/// If auditing is [enabled][AuditingMode#ENABLED] and the entity type is audited, then **every** delete operation
/// here cascades the deletion to the entity's own audit records, in the same transaction as the deletion of the
/// entity itself.
/// Refer to [AbstractAuditEntity] for the rationale.
///
/// Only audit records that belong to the entity being deleted are removed.
/// Audit records belonging to *other* audited entities, which reference the entity being deleted (e.g., because they
/// captured a historical value of an entity-typed property), are left intact.
/// Deletion is therefore prohibited in that case by referential integrity, which is the intended behaviour.
///
public final class DeleteOperations<T extends AbstractEntity<?>> {

    private static final Logger LOGGER = getLogger();

    /// Maximum number of ID values per EQL statement.
    /// EQL binds one statement parameter per value, so batches must stay within the most restrictive limit imposed by
    /// a supported RDBMS on the number of statement parameters (SQL Server permits 2100).
    ///
    /// [EntityBatchDeleteByIdsOperation] is exempt, as it inlines ID values as SQL literals.
    ///
    private static final int ID_BATCH_SIZE = 990;

    public static final String
            ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES = "Deletion was unsuccessful due to existing dependencies.",
            ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_OTHER_REASONS = "Deletion was unsuccessful due to: %s",
            ERR_ONLY_PERSISTED_CAN_BE_DELETED = "Only persisted entity instances can be deleted.",
            ERR_DIRTY_CANNOT_BE_DELETED = "Dirty entity instances cannot be deleted.";

    private final Supplier<Session> session;
    private final Class<T> entityType;
    private final IEntityReader<T> reader;
    private final Supplier<EntityBatchDeleteByIdsOperation<T>> batchDeleteByIdsOp;
    private final EntityBatchDeleteByQueryModelOperation.Factory entityBatchDeleteFactory;
    private final IDomainMetadata domainMetadata;
    private final ICompanionObjectFinder coFinder;
    private final AuditingMode auditingMode;
    private final IAuditTypeFinder auditTypeFinder;

    @Inject
    public DeleteOperations(
            @Assisted final IEntityReader<T> reader,
            @Assisted final Supplier<Session> session,
            @Assisted final Class<T> entityType,
            final EqlTables eqlTables,
            final EntityBatchDeleteByQueryModelOperation.Factory entityBatchDeleteFactory,
            final IDomainMetadata domainMetadata,
            final ICompanionObjectFinder coFinder,
            final AuditingMode auditingMode,
            final IAuditTypeFinder auditTypeFinder)
    {
        this.reader = reader;
        this.session = session;
        this.entityType = entityType;
        this.entityBatchDeleteFactory = entityBatchDeleteFactory;
        this.domainMetadata = domainMetadata;
        this.coFinder = coFinder;
        this.auditingMode = auditingMode;
        this.auditTypeFinder = auditTypeFinder;
        this.batchDeleteByIdsOp = () -> new EntityBatchDeleteByIdsOperation<>(session.get(), eqlTables.getTableForEntityType(entityType));
    }

    @ImplementedBy(FactoryImpl.class)
    public interface Factory {
        <E extends AbstractEntity<?>> DeleteOperations<E> create(final IEntityReader<E> reader,
                                                                 final Supplier<Session> session,
                                                                 final Class<E> entityType);
    }

    /// A convenient default implementation for entity deletion, which should be used by overriding method [CommonEntityDao#delete(AbstractEntity)].
    ///
    /// @return the number of deleted entities, which could be 1 or 0.
    ///
    public int defaultDelete(final T entity) {
        requireNonNull(entity, "entity");

        if (!entity.isPersisted()) {
            throw new EntityCompanionException(ERR_ONLY_PERSISTED_CAN_BE_DELETED);
        }
        if (entity.isInstrumented() && entity.isDirty()) {
            throw new EntityCompanionException(ERR_DIRTY_CANNOT_BE_DELETED);
        }

        beforeDeletingActivatable(entity);
        beforeDeletingAudited(entity);
        return deleteById(entity.getId());
    }

    /// Decrements `refCount` of active activatables referenced by `entity`, if `entity` is an activatable that is active.
    /// Does nothing otherwise.
    ///
    @SuppressWarnings("unchecked")
    private void beforeDeletingActivatable(final T entity) {
        if (entity instanceof ActivatableAbstractEntity<?> activatable) {
            // Iff the persisted version of `entity` is active, we need to decrement refCounts of referenced active activatables, ignoring self-references.
            // Reload entity for deletion in the lock mode to make sure it is not updated while its activatable dependencies are being processed.
            final var persistedEntity = (ActivatableAbstractEntity<?>) session.get().load(activatable.getType(), activatable.getId(), UPGRADE);
            if (persistedEntity.isActive()) {
                // Let's collect activatable properties from entity to check them for activity and also to decrement their refCount.
                // If `prop` is proxied, its value will be retrieved lazily by Hibernate.
                // Load the latest activatable value.
                // If `persistedActivatable` is active and not a self-reference, then its `refCount` needs to be decremented.
                activatableProperties((Class<? extends ActivatableAbstractEntity<?>>) activatable.getType(), persistedEntity)
                        .map(prop -> extractActivatable(persistedEntity.get(prop)))
                        .filter(Objects::nonNull)
                        .map(ref -> (ActivatableAbstractEntity<?>) session.get().load(ref.getType(), ref.getId(), UPGRADE))
                        .forEach(persistedActivatable -> {
                            persistedActivatable.setIgnoreEditableState(true);
                            if (persistedActivatable.isActive() && !activatable.equals(persistedActivatable)) {
                                session.get().update(persistedActivatable.decRefCount());
                            }
                        });
            }
        }
    }

    /// Cascade deletes audit records of `entity`, if its type is audited and auditing is enabled.
    /// Does nothing otherwise.
    ///
    private void beforeDeletingAudited(final T entity) {
        if (isAuditCascadeRequired()) {
            // A source-less select avoids an otherwise pointless scan of the audited entity's own table.
            deleteAuditRecords(select().yield().val(entity.getId()).modelAsPrimitive(), Map.of());
        }
    }

    /// Whether delete operations must cascade to audit records.
    /// This predicate is the exact counterpart of the one governing the *creation* of audit records upon a save,
    /// so audit records are removed under precisely the conditions under which they are written.
    ///
    private boolean isAuditCascadeRequired() {
        return auditingMode == AuditingMode.ENABLED && isAudited(entityType);
    }

    /// Deletes audit records of the audited entities identified by `auditedEntityIds`, which yields their ID values.
    ///
    /// Audit-prop records are deleted first, then audit-entity records.
    /// **This order is essential**: audit-prop records are located by joining to their audit-entity record, so deleting
    /// audit-entity records first would leave the audit-prop records orphaned and unreachable.
    /// Both are deleted for all audit versions.
    ///
    /// Deletion is performed directly, rather than via companions, which deliberately do not support deletion on their own.
    ///
    private void deleteAuditRecords(final SingleResultQueryModel<?> auditedEntityIds, final Map<String, Object> parameters) {
        final var auditTypes = auditTypeFinder.navigate(entityType);
        final var batchDelete = entityBatchDeleteFactory.create(session);

        for (final var auditPropType : auditTypes.allAuditPropTypes()) {
            batchDelete.deleteEntities(
                    select(auditPropType).where().prop(AbstractAuditProp.PATH_TO_AUDITED_ENTITY).in().model(auditedEntityIds).model(),
                    parameters);
        }
        for (final var auditEntityType : auditTypes.allAuditEntityTypes()) {
            batchDelete.deleteEntities(
                    select(auditEntityType).where().prop(AbstractAuditEntity.AUDITED_ENTITY).in().model(auditedEntityIds).model(),
                    parameters);
        }
    }

    /// Deletes an entity by ID.
    ///
    /// @return the number of deleted entities, which could be 1 or 0.
    ///
    private int deleteById(final long id) {
        try {
            return session.get().createQuery("delete %s where id = %s".formatted(entityType.getName(), id)).executeUpdate();
        } catch (final PersistenceException ex) {
            final var msg = ex.getCause() instanceof ConstraintViolationException
                    ? ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES
                    : ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_OTHER_REASONS.formatted(ex.getMessage());
            LOGGER.error(msg, ex);
            throw new EntityDeletionException(msg, ex.getCause());
        }
    }

    private Stream<String> activatableProperties(final Class<? extends ActivatableAbstractEntity<?>> entityType, final AbstractEntity<?> persistedEntity) {
        return domainMetadata.forEntity(entityType)
                .properties()
                .stream()
                .filter(prop -> isActivatableReference(entityType, prop.name(), persistedEntity.get(prop.name()), coFinder))
                .map(PropertyMetadata::name);
    }


    private static @Nullable ActivatableAbstractEntity<?> extractActivatable(final AbstractEntity<?> entity) {
        return switch (entity) {
            case ActivatableAbstractEntity<?> it -> it;
            case AbstractUnionEntity union -> union.activeEntity() instanceof ActivatableAbstractEntity<?> it ? it : null;
            case null, default -> null;
        };
    }

    /// Deletes entities returned by the specified query.
    ///
    /// @return the number of deleted entities
    ///
    public int defaultDelete(final EntityResultQueryModel<T> model, final Map<String, Object> parameters) {
        requireNonNull(model, "model");

        try (final var stream = reader.stream(from(model).with(parameters).lightweight().model())) {
            return stream.mapToInt(this::defaultDelete).sum();
        }
    }

    /// The same as [#defaultDelete(EntityResultQueryModel,Map)], but with empty parameters.
    ///
    /// @return the number of deleted entities
    ///
    public int defaultDelete(final EntityResultQueryModel<T> model) {
        return defaultDelete(model, Map.of());
    }

    /// A convenient default implementation for batch deletion of entities specified by provided query model.
    ///
    /// @return the number of deleted entities
    ///
    public int defaultBatchDelete(final EntityResultQueryModel<T> model, final Map<String, Object> parameters) {
        requireNonNull(model, "model");

        if (isActivatableEntityType(entityType)) {
            // Activatables are deleted one by one to maintain `refCount` of referenced activatables.
            // Audit records, if any, are cascade deleted as part of deleting each individual entity.
            return defaultDelete(model, parameters);
        } else {
            if (isAuditCascadeRequired()) {
                deleteAuditRecords(model, parameters);
            }
            return entityBatchDeleteFactory.create(session).deleteEntities(model, parameters);
        }
    }

    /// The same as [#defaultBatchDelete(EntityResultQueryModel,Map)], but with empty parameters.
    ///
    /// @return the number of deleted entities
    ///
    public int defaultBatchDelete(final EntityResultQueryModel<T> model) {
        return defaultBatchDelete(model, Map.of());
    }

    /// Batch deletion of entities by their ID values.
    ///
    /// @return the number of deleted entities
    ///
    public int defaultBatchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDeleteByPropertyValues(ID, entitiesIds);
    }

    /// Batch deletion of entities by ID values of their properties.
    /// Entities that have the specified property matching any of the specified ID values are deleted.
    ///
    /// @return the number of deleted entities
    ///
    public int defaultBatchDeleteByPropertyValues(final String propName, final Collection<Long> entitiesIds) {
        if (entitiesIds.isEmpty()) {
            throw new EntityCompanionException("No entity ids have been provided for deletion.");
        }

        // Both branches below match entities with an EQL query, which binds a statement parameter per ID value.
        // Hence, the batching by `ID_BATCH_SIZE` that keeps those queries within the DB limit on the number of parameters.
        if (isActivatableEntityType(entityType)) {
            // Activatables are deleted one by one to maintain `refCount` of referenced activatables.
            // Audit records, if any, are cascade deleted as part of deleting each individual entity.
            int deletedCount = 0;
            for (final var batchOfIds : partition(entitiesIds, ID_BATCH_SIZE)) {
                deletedCount += defaultDelete(selectByPropertyValues(propName, batchOfIds));
            }
            return deletedCount;
        } else {
            if (isAuditCascadeRequired()) {
                for (final var batchOfIds : partition(entitiesIds, ID_BATCH_SIZE)) {
                    deleteAuditRecords(selectByPropertyValues(propName, batchOfIds), Map.of());
                }
            }
            // Unlike the audit-cascade queries above, `batchDeleteByIdsOp` inlines ID values as SQL literals, so it needs no batching.
            return batchDeleteByIdsOp.get().deleteEntities(propName, entitiesIds);
        }
    }

    /// A query matching entities whose property `propName` has one of the specified ID values.
    ///
    private EntityResultQueryModel<T> selectByPropertyValues(final String propName, final Collection<Long> entitiesIds) {
        return select(entityType).where().prop(propName).in().values(entitiesIds).model();
    }

    /// The same as [#defaultBatchDeleteByPropertyValues(String,Collection)], but using a list of entity instances instead of ID values
    ///
    /// @return the number of deleted entities
    ///
    public <E extends AbstractEntity<?>> int defaultBatchDeleteByPropertyValues(final String propName, final List<E> propEntities) {
        return defaultBatchDeleteByPropertyValues(propName, propEntities.stream().map(AbstractEntity::getId).toList());
    }

    /// This factory must be implemented by hand since [FactoryModuleBuilder] does not support generic factory methods.
    ///
    static final class FactoryImpl implements Factory {

        private final EqlTables eqlTables;
        private final EntityBatchDeleteByQueryModelOperation.Factory entityBatchDeleteFactory;
        private final IDomainMetadata domainMetadata;
        private final ICompanionObjectFinder coFinder;
        private final AuditingMode auditingMode;
        private final IAuditTypeFinder auditTypeFinder;

        @Inject
        FactoryImpl(
                final EqlTables eqlTables,
                final EntityBatchDeleteByQueryModelOperation.Factory entityBatchDeleteFactory,
                final IDomainMetadata domainMetadata,
                final ICompanionObjectFinder coFinder,
                final AuditingMode auditingMode,
                final IAuditTypeFinder auditTypeFinder)
        {
            this.eqlTables = eqlTables;
            this.entityBatchDeleteFactory = entityBatchDeleteFactory;
            this.domainMetadata = domainMetadata;
            this.coFinder = coFinder;
            this.auditingMode = auditingMode;
            this.auditTypeFinder = auditTypeFinder;
        }

        public <E extends AbstractEntity<?>> DeleteOperations<E> create(
                final IEntityReader<E> reader,
                final Supplier<Session> session,
                final Class<E> entityType)
        {
            return new DeleteOperations<>(reader,
                                          session,
                                          entityType,
                                          eqlTables,
                                          entityBatchDeleteFactory,
                                          domainMetadata,
                                          coFinder,
                                          auditingMode,
                                          auditTypeFinder);
        }

    }

}
