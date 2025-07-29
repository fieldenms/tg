package ua.com.fielden.platform.companion;

import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.dao.exceptions.EntityDeletionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.query.EntityBatchDeleteByIdsOperation;
import ua.com.fielden.platform.entity.query.EntityBatchDeleteByQueryModelOperation;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.meta.EqlTables;

import javax.annotation.Nullable;
import javax.persistence.PersistenceException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.hibernate.LockOptions.UPGRADE;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.collectActivatableNotDirtyProperties;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;

/**
 * A set of various delete operations that are used by entity companions. 
 * The main purpose of this call is to be more like a mixin that captures the implementation of delete operations.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public final class DeleteOperations<T extends AbstractEntity<?>> {

    private static final Logger LOGGER = getLogger(DeleteOperations.class);
    public static final String ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES = "Deletion was unsuccessful due to existing dependencies.";
    public static final String ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_OTHER_REASONS = "Deletion was unsuccessful due to: %s";

    private final Supplier<Session> session;
    private final Class<T> entityType;
    private final IEntityReader<T> reader;
    private final Supplier<EntityBatchDeleteByIdsOperation<T>> batchDeleteByIdsOp;
    private final EntityBatchDeleteByQueryModelOperation.Factory entityBatchDeleteFactory;

    @Inject
    public DeleteOperations(
            @Assisted final IEntityReader<T> reader,
            @Assisted final Supplier<Session> session,
            @Assisted final Class<T> entityType,
            final EqlTables eqlTables,
            final EntityBatchDeleteByQueryModelOperation.Factory entityBatchDeleteFactory)
    {
        this.reader = reader;
        this.session = session;
        this.entityType = entityType;
        this.entityBatchDeleteFactory = entityBatchDeleteFactory;
        this.batchDeleteByIdsOp = () -> new EntityBatchDeleteByIdsOperation<>(session.get(), eqlTables.getTableForEntityType(entityType));
    }

    @ImplementedBy(FactoryImpl.class)
    public interface Factory {
        <E extends AbstractEntity<?>> DeleteOperations<E> create(final IEntityReader<E> reader,
                                                                 final Supplier<Session> session,
                                                                 final Class<E> entityType);
    }

    /**
     * A convenient default implementation for entity deletion, which should be used by overriding method {@link ua.com.fielden.platform.dao.CommonEntityDao#delete(AbstractEntity)}}.
     *
     * @param entity
     */
    public int defaultDelete(final T entity) {
        if (entity == null) {
            throw new EntityCompanionException("Null is not an acceptable value for an entity instance.");
        }
        if (!entity.isPersisted()) {
            throw new EntityCompanionException("Only persisted entity instances can be deleted.");
        }
        if (entity.isInstrumented() && entity.isDirty()) {
            throw new EntityCompanionException("Dirty entity instances cannot be deleted.");
        }

        if (entity instanceof ActivatableAbstractEntity<?> activatable) {
            return deleteActivatable(activatable);
        } else {
            return deleteById(entity.getId());
        }
    }

    /**
     * Deletes an entity by ID.
     *
     * @param id
     * @return the number of deleted entities, which could be 1 or 0.
     */
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
    private int deleteActivatable(final ActivatableAbstractEntity<?> entity) {
        // Iff the persisted version of `entity` is active, we need to decrement refCounts of referenced active activatables, ignoring self-references.
        // Reload entity for deletion in the lock mode to make sure it is not updated while its activatable dependencies are being processed.
        final var persistedEntity = (ActivatableAbstractEntity<?>) session.get().load(entity.getType(), entity.getId(), UPGRADE);
        if (persistedEntity.isActive()) {
            // Let's collect activatable properties from entity to check them for activity and also to decrement their refCount.
            final var keyMembers = getKeyMembers(entity.getType()).stream().map(Field::getName).collect(toSet());
            for (final var prop : collectActivatableNotDirtyProperties(entity, keyMembers)) {
                // If `prop` is proxied, its value will be retrieved lazily by Hibernate.
                final var activatable = extractActivatable(persistedEntity.get(prop));
                if (activatable != null) {
                    // Load the latest activatable value.
                    final var persistedActivatable = (ActivatableAbstractEntity<?>) session.get().load(activatable.getType(), activatable.getId(), UPGRADE);
                    persistedActivatable.setIgnoreEditableState(true);
                    // If `persistedActivatable` is active and is not a self-reference then its `refCount` needs to be decremented.
                    if (persistedActivatable.isActive() && !entity.equals(persistedActivatable)) {
                        session.get().update(persistedActivatable.decRefCount());
                    }
                }
            }
        }

        return deleteById(entity.getId());
    }

    private static @Nullable ActivatableAbstractEntity<?> extractActivatable(final AbstractEntity<?> entity) {
        return switch (entity) {
            case ActivatableAbstractEntity<?> it -> it;
            case AbstractUnionEntity union -> union.activeEntity() instanceof ActivatableAbstractEntity<?> it ? it : null;
            case null, default -> null;
        };
    }

    /**
     * A convenient default implementation for deletion of entities specified by provided query model.
     *
     * @param model
     * @param paramValues
     */
    public int defaultDelete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        if (model == null) {
            throw new EntityCompanionException("Null is not an acceptable value for eQuery model.");
        }

       return reader.stream(from(model).with(paramValues).lightweight().model())
               .mapToInt(entity -> defaultDelete(entity))
               .sum();
    }

    /**
     * The same as {@link #defaultDelete(EntityResultQueryModel, Map)}, but with empty parameters.
     *
     * @param model
     */
    public int defaultDelete(final EntityResultQueryModel<T> model) {
        return defaultDelete(model, Collections.<String, Object> emptyMap());
    }

    /**
     * A convenient default implementation for batch deletion of entities specified by provided query model.
     *
     * @param model
     * @param paramValues
     */
    public int defaultBatchDelete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        if (model == null) {
            throw new EntityCompanionException("Null is not an acceptable value for eQuery model.");
        }

        if (ActivatableAbstractEntity.class.isAssignableFrom(entityType)) {
            return defaultDelete(model, paramValues);
        } else {
            return entityBatchDeleteFactory.create(session).deleteEntities(model, paramValues);
        }
    }

    /**
     * The same as {@link #defaultBatchDelete(EntityResultQueryModel, Map)}, but with empty parameters.
     *
     * @param model
     * @return
     */
    public int defaultBatchDelete(final EntityResultQueryModel<T> model) {
        return defaultBatchDelete(model, Collections.<String, Object> emptyMap());
    }

    /**
     * Batch deletion of entities by their ID values.
     *
     * @param entitiesIds
     * @return
     */
    public int defaultBatchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDeleteByPropertyValues(ID, entitiesIds);
    }

    /**
     * A more generic version of batch deletion of entities {@link #defaultBatchDelete(Collection)} that accepts a property name and a collection of ID values.
     * Those entities that have the specified property matching any of those ID values get deleted.
     *
     * @param propName
     * @param entitiesIds
     * @return
     */
    public int defaultBatchDeleteByPropertyValues(final String propName, final Collection<Long> entitiesIds) {
        if (entitiesIds.isEmpty()) {
            throw new EntityCompanionException("No entity ids have been provided for deletion.");
        }

        if (ActivatableAbstractEntity.class.isAssignableFrom(entityType)) {
            final EntityResultQueryModel<T> model = select(entityType).where().prop(propName).in().values(entitiesIds.toArray()).model();
            return defaultDelete(model);
        } else {
            return batchDeleteByIdsOp.get().deleteEntities(propName, entitiesIds);
        }
    }

    /**
     * The same as {@link #defaultBatchDeleteByPropertyValues(String, Collection)}, but for a list of entities.
     *
     * @param propName
     * @param propEntities
     * @return
     */
    public <E extends AbstractEntity<?>> int defaultBatchDeleteByPropertyValues(final String propName, final List<E> propEntities) {
        return defaultBatchDeleteByPropertyValues(propName, propEntities.stream().map(v -> v.getId()).collect(Collectors.toList()));
    }

    // This factory must be implemented by hand since com.google.inject.assistedinject.FactoryModuleBuilder
    // does not support generic factory methods.
    static final class FactoryImpl implements Factory {

        private final EqlTables eqlTables;
        private final EntityBatchDeleteByQueryModelOperation.Factory entityBatchDeleteFactory;

        @Inject
        FactoryImpl(final EqlTables eqlTables,
                    final EntityBatchDeleteByQueryModelOperation.Factory entityBatchDeleteFactory) {
            this.eqlTables = eqlTables;
            this.entityBatchDeleteFactory = entityBatchDeleteFactory;
        }

        public <E extends AbstractEntity<?>> DeleteOperations<E> create(final IEntityReader<E> reader,
                                                                        final Supplier<Session> session,
                                                                        final Class<E> entityType) {
            return new DeleteOperations<>(reader, session, entityType, eqlTables, entityBatchDeleteFactory);
        }
    }

}
