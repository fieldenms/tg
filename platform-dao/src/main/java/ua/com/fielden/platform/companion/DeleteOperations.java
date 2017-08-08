package ua.com.fielden.platform.companion;

import static java.lang.String.format;
import static org.hibernate.LockOptions.UPGRADE;
import static ua.com.fielden.platform.companion.helper.ActivatableEntityRetrospectionHelper.collectActivatableNotDirtyProperties;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;

import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.query.EntityBatchDeleteByIdsOperation;
import ua.com.fielden.platform.entity.query.EntityBatchDeleteByQueryModelOperation;
import ua.com.fielden.platform.entity.query.QueryExecutionContext;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;

/**
 * A set of various delete operations that are used by entity companions. 
 * The main purpose of this call is to be more like a mixin that captures the implementation of delete operations.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public final class DeleteOperations<T extends AbstractEntity<?>> {

    private static final String DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES = "Deletion was unsuccessful due to existing dependencies.";

    private final Supplier<Session> session;
    private final Class<T> entityType;
    private final IEntityReader<T> reader;
    private final Supplier<QueryExecutionContext> qeCtx;
    private final Supplier<EntityBatchDeleteByIdsOperation<T>> batchDeleteByIdsOp;
    
    public DeleteOperations(
            final IEntityReader<T> reader,
            final Supplier<Session> session,
            final Class<T> entityType,
            final Supplier<QueryExecutionContext> qeCtx,
            final Supplier<EntityBatchDeleteByIdsOperation<T>> batchDeleteByIdsOp) {
        this.reader = reader;
        this.session = session;
        this.entityType = entityType;
        this.qeCtx = qeCtx;
        this.batchDeleteByIdsOp = batchDeleteByIdsOp;
    }
    
    /**
     * A convenient default implementation for entity deletion, which should be used by overriding method {@link #delete(Long)}.
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
        
        if (entity instanceof ActivatableAbstractEntity && ((ActivatableAbstractEntity<?>) entity).isActive()) {
            return deleteActivatable(entity);
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
        } catch (final ConstraintViolationException e) {
            throw new EntityCompanionException(DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES, e);
        }
    }
    
    /**
     * A method for deleting activatable entities.
     * It takes care of decrementing referenced activatable dependencies if any.
     * 
     * @param entity
     */
    private int deleteActivatable(final T entity) {
        if (!(entity instanceof ActivatableAbstractEntity)) {
            throw new EntityCompanionException(format("Entity of type [%s] is not activatable.", entity.getType()));
        }

        // only if entity is active do we need to decrement ref-counts of the referenced by it activatable entities, accept self references, which should be ignored
        if (((ActivatableAbstractEntity<?>) entity).isActive()) {
            // let's collect activatable properties from entity to check them for activity and also to decrement their refCount
            final Set<String> keyMembers = Finder.getKeyMembers(entity.getType()).stream().map(f -> f.getName()).collect(Collectors.toSet());
            final Set<T2<String, Class<ActivatableAbstractEntity<?>>>> activatableProps = collectActivatableNotDirtyProperties(entity, keyMembers);
            // reload entity for deletion in the lock mode to make sure it is not updated while its activatable dependencies are being processed
            final ActivatableAbstractEntity<?> persistedEntityToBeDeleted = (ActivatableAbstractEntity<?>) session.get().load(entity.getType(), entity.getId(), UPGRADE);
            
            activatableProps.stream()
            .map(prop -> T3.t3(persistedEntityToBeDeleted.get(prop._1), prop._2, prop._1))
            .filter(triple -> triple._1 != null)
            .forEach(
                    triple -> {
                        // get value from a persisted version of entity, which is loaded by Hibernate
                        // if a corresponding property is proxied due to insufficient fetch model, its value is retrieved lazily by Hibernate
                        final AbstractEntity<?> value = persistedEntityToBeDeleted.get(triple._3);
                        // load the latest value for the current property of an activatable type
                        final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) session.get().load(triple._2, value.getId(), UPGRADE);
                        persistedValue.setIgnoreEditableState(true);
                        // if activatable property value (persistedValue) is active and is not a self-reference then its refCount needs to be decremented
                        if (persistedValue.isActive() && !entity.equals(persistedValue)) {
                            session.get().update(persistedValue.decRefCount());
                        }
                    });
            
            
        }
        
        // delete entity by ID
        return deleteById(entity.getId());
    }


    /**
     * A convenient default implementation for deletion of entities specified by provided query model.
     *
     * @param entity
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
     * @param entity
     */
    public int defaultBatchDelete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        if (model == null) {
            throw new EntityCompanionException("Null is not an acceptable value for eQuery model.");
        }

        if (ActivatableAbstractEntity.class.isAssignableFrom(entityType)) {
            return defaultDelete(model, paramValues);
        } else try {
            return new EntityBatchDeleteByQueryModelOperation(qeCtx.get()).deleteEntities(model, paramValues);
        } catch (final ConstraintViolationException e) {
            throw new EntityCompanionException(DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES, e);
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
        } else try {
            return batchDeleteByIdsOp.get().deleteEntities(propName, entitiesIds);
        } catch (final ConstraintViolationException e) {
            throw new EntityCompanionException(DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES, e);
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

}
