package ua.com.fielden.platform.companion;

import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
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

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.hibernate.LockOptions.UPGRADE;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.exceptions.InvalidArgumentException.requireNonNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isActivatableReference;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;

/// Various delete operations that are used by entity companions.
/// The main purpose of this class is to be more like a mixin that provides an implementation of delete operations.
///
public final class DeleteOperations<T extends AbstractEntity<?>> {

    private static final Logger LOGGER = getLogger();
    public static final String ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES = "Deletion was unsuccessful due to existing dependencies.";
    public static final String ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_OTHER_REASONS = "Deletion was unsuccessful due to: %s";
    public static final String ERR_ONLY_PERSISTED_CAN_BE_DELETED = "Only persisted entity instances can be deleted.";
    public static final String ERR_DIRTY_CANNOT_BE_DELETED = "Dirty entity instances cannot be deleted.";

    private final Supplier<Session> session;
    private final Class<T> entityType;
    private final IEntityReader<T> reader;
    private final Supplier<EntityBatchDeleteByIdsOperation<T>> batchDeleteByIdsOp;
    private final EntityBatchDeleteByQueryModelOperation.Factory entityBatchDeleteFactory;
    private final IDomainMetadata domainMetadata;
    private final ICompanionObjectFinder coFinder;

    @Inject
    public DeleteOperations(
            @Assisted final IEntityReader<T> reader,
            @Assisted final Supplier<Session> session,
            @Assisted final Class<T> entityType,
            final EqlTables eqlTables,
            final EntityBatchDeleteByQueryModelOperation.Factory entityBatchDeleteFactory,
            final IDomainMetadata domainMetadata,
            final ICompanionObjectFinder coFinder)
    {
        this.reader = reader;
        this.session = session;
        this.entityType = entityType;
        this.entityBatchDeleteFactory = entityBatchDeleteFactory;
        this.domainMetadata = domainMetadata;
        this.coFinder = coFinder;
        this.batchDeleteByIdsOp = () -> new EntityBatchDeleteByIdsOperation<>(session.get(), eqlTables.getTableForEntityType(entityType));
    }

    @ImplementedBy(FactoryImpl.class)
    public interface Factory {
        <E extends AbstractEntity<?>> DeleteOperations<E> create(final IEntityReader<E> reader,
                                                                 final Supplier<Session> session,
                                                                 final Class<E> entityType);
    }

    /// A convenient default implementation for entity deletion, which should be used by overriding method [CommonEntityDao#delete(AbstractEntity)]}.
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

        if (entity instanceof ActivatableAbstractEntity<?> activatable) {
            return deleteActivatable(activatable);
        } else {
            return deleteById(entity.getId());
        }
    }

    /// Deletes an entity by ID.
    ///
    /// @return the number of deleted entities, which could be 1 or 0.
    ///
    private int deleteById(final long id) {
        try {
            return session.get().createQuery(format("delete %s where id = %s", entityType.getName(), id)).executeUpdate();
        } catch (final PersistenceException ex) {
            final var msg = ex.getCause() instanceof ConstraintViolationException
                    ? ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES
                    : ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_OTHER_REASONS.formatted(ex.getMessage());
            LOGGER.error(msg, ex);
            throw new EntityDeletionException(msg, ex.getCause());
        }
    }

    /// Deletes the specified activatable entity, and decrements the `refCount` of referenced activatables if applicable.
    ///
    @SuppressWarnings("unchecked")
    private int deleteActivatable(final ActivatableAbstractEntity<?> entity) {
        // Iff the persisted version of `entity` is active, we need to decrement refCounts of referenced active activatables, ignoring self-references.
        // Reload entity for deletion in the lock mode to make sure it is not updated while its activatable dependencies are being processed.
        final var persistedEntity = (ActivatableAbstractEntity<?>) session.get().load(entity.getType(), entity.getId(), UPGRADE);
        if (persistedEntity.isActive()) {
            // Let's collect activatable properties from entity to check them for activity and also to decrement their refCount.
            // If `prop` is proxied, its value will be retrieved lazily by Hibernate.
            // Load the latest activatable value.
            // If `persistedActivatable` is active and not a self-reference, then its `refCount` needs to be decremented.
            activatableProperties((Class<? extends ActivatableAbstractEntity<?>>) entity.getType(), persistedEntity)
                    .map(prop -> extractActivatable(persistedEntity.get(prop)))
                    .filter(Objects::nonNull)
                    .map(activatable -> (ActivatableAbstractEntity<?>) session.get().load(activatable.getType(), activatable.getId(), UPGRADE))
                    .forEach(persistedActivatable -> {
                        persistedActivatable.setIgnoreEditableState(true);
                        if (persistedActivatable.isActive() && !entity.equals(persistedActivatable)) {
                            session.get().update(persistedActivatable.decRefCount());
                        }
                    });
        }

        return deleteById(entity.getId());
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

       return reader.stream(from(model).with(parameters).lightweight().model())
               .mapToInt(this::defaultDelete)
               .sum();
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
            return defaultDelete(model, parameters);
        } else {
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

        if (isActivatableEntityType(entityType)) {
            return defaultDelete(select(entityType).where().prop(propName).in().values(entitiesIds).model());
        } else {
            return batchDeleteByIdsOp.get().deleteEntities(propName, entitiesIds);
        }
    }

    /// The same as [#defaultBatchDeleteByPropertyValues(String,Collection)], but using a list of entity instances instead of ID values
    ///
    /// @return the number of deleted entities
    ///
    public <E extends AbstractEntity<?>> int defaultBatchDeleteByPropertyValues(final String propName, final List<E> propEntities) {
        return defaultBatchDeleteByPropertyValues(propName, propEntities.stream().map(AbstractEntity::getId).toList());
    }

    // This factory must be implemented by hand since com.google.inject.assistedinject.FactoryModuleBuilder
    // does not support generic factory methods.
    static final class FactoryImpl implements Factory {

        private final EqlTables eqlTables;
        private final EntityBatchDeleteByQueryModelOperation.Factory entityBatchDeleteFactory;
        private final IDomainMetadata domainMetadata;
        private final ICompanionObjectFinder coFinder;

        @Inject
        FactoryImpl(final EqlTables eqlTables,
                    final EntityBatchDeleteByQueryModelOperation.Factory entityBatchDeleteFactory,
                    final IDomainMetadata domainMetadata,
                    final ICompanionObjectFinder coFinder) {
            this.eqlTables = eqlTables;
            this.entityBatchDeleteFactory = entityBatchDeleteFactory;
            this.domainMetadata = domainMetadata;
            this.coFinder = coFinder;
        }

        public <E extends AbstractEntity<?>> DeleteOperations<E> create(final IEntityReader<E> reader,
                                                                        final Supplier<Session> session,
                                                                        final Class<E> entityType) {
            return new DeleteOperations<>(reader, session, entityType, eqlTables, entityBatchDeleteFactory, domainMetadata, coFinder);
        }
    }

}
