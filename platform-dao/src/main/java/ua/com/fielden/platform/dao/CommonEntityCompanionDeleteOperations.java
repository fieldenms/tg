package ua.com.fielden.platform.dao;

import static java.lang.String.format;
import static org.hibernate.LockOptions.UPGRADE;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.exception.ConstraintViolationException;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.EntityBatchDeleteByIdsOperation;
import ua.com.fielden.platform.entity.query.EntityBatchDeleterByQueryModel;
import ua.com.fielden.platform.entity.query.QueryExecutionContext;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.types.tuples.T3;

/**
 * A set of various delete operations that are used by entity companions. 
 * The main purpose of this call is to be more like a mixin that captures the implementation of delete operations.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public final class CommonEntityCompanionDeleteOperations<T extends AbstractEntity<?>> {

    private static final String DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES = "Deletion was unsuccessful due to existing dependencies.";

    private final CommonEntityDao<T> co;
    
    public CommonEntityCompanionDeleteOperations(final CommonEntityDao<T> co) {
        this.co = co;
    }
    
    /**
     * A method for deleting activatable entities.
     * It takes care of decrementing referenced activatable dependencies if any.
     * 
     * @param entity
     */
    private void deleteActivatable(final T entity) {
        if (!(entity instanceof ActivatableAbstractEntity)) {
            throw new EntityCompanionException(format("Entity of type [%s] is not activatable.", entity.getType()));
        }

        // only if entity is active do we need to decrement ref-counts of the referenced by it activatable entities, accept self references, which should be ignored
        if (((ActivatableAbstractEntity<?>) entity).isActive()) {
            // let's collect activatable properties from entity to check them for activity and also to decrement their refCount
            final Set<String> keyMembers = Finder.getKeyMembers(entity.getType()).stream().map(f -> f.getName()).collect(Collectors.toSet());
            final Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> activatableProps = co.collectActivatableNotDirtyProperties(entity, keyMembers);
            // reload entity for deletion in the lock mode to make sure it is not updated while its activatable dependencies are being processed
            final ActivatableAbstractEntity<?> persistedEntityToBeDeleted = (ActivatableAbstractEntity<?>) co.getSession().load(entity.getType(), entity.getId(), UPGRADE);
            
            activatableProps.stream()
            .map(prop -> T3.t3(persistedEntityToBeDeleted.get(prop.getName()), prop.getType(), prop.getName()))
            .filter(triple -> triple._1 != null)
            .forEach(
                    triple -> {
                        // get value from a persisted version of entity, which is loaded by Hibernate
                        // if a corresponding property is proxied due to insufficient fetch model, its value is retrieved lazily by Hibernate
                        final AbstractEntity<?> value = persistedEntityToBeDeleted.get(triple._3);
                        // load the latest value for the current property of an activatable type
                        final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) co.getSession().load(triple._2, value.getId(), UPGRADE);
                        persistedValue.setIgnoreEditableState(true);
                        // if activatable property value (persistedValue) is active and is not a self-reference then its refCount needs to be decremented
                        if (persistedValue.isActive() && !entity.equals(persistedValue)) {
                            co.getSession().update(persistedValue.decRefCount());
                        }
                    });
            
            
        }
        
        // delete entity by ID
        deleteById(entity.getId());
    }
    
    /**
     * A convenient default implementation for entity deletion, which should be used by overriding method {@link #delete(Long)}.
     *
     * @param entity
     */
    protected void defaultDelete(final T entity) {
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
            deleteActivatable(entity);
        } else {
            deleteById(entity.getId());
        }
    }

    private void deleteById(final long id) {
        try {
            co.getSession().createQuery(format("delete %s where id = %s", co.getEntityType().getName(), id)).executeUpdate();
        } catch (final ConstraintViolationException e) {
            throw new EntityCompanionException(DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES, e);
        }
    }

    /**
     * A convenient default implementation for deletion of entities specified by provided query model.
     *
     * @param entity
     */
    protected void defaultDelete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        if (model == null) {
            throw new EntityCompanionException("Null is not an acceptable value for eQuery model.");
        }

        final List<T> toBeDeleted = co.getAllEntities(from(model).with(paramValues).lightweight().model());

        for (final T entity : toBeDeleted) {
            defaultDelete(entity);
        }
    }

    /**
     * The same as {@link #defaultDelete(EntityResultQueryModel, Map)}, but with empty parameters.
     * 
     * @param model
     */    
    protected void defaultDelete(final EntityResultQueryModel<T> model) {
        defaultDelete(model, Collections.<String, Object> emptyMap());
    }

    /**
     * A convenient default implementation for batch deletion of entities specified by provided query model.
     *
     * @param entity
     */
    protected int defaultBatchDelete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        if (model == null) {
            throw new EntityCompanionException("Null is not an acceptable value for eQuery model.");
        }

        try {
            final QueryExecutionContext queryExecutionContext = new QueryExecutionContext(co.getSession(), co.getEntityFactory(), co.getCoFinder(), co.getDomainMetadata(), co.getFilter(), co.getUsername(), co.getUniversalConstants(), co.getIdOnlyProxiedEntityTypeCache());
            return new EntityBatchDeleterByQueryModel(queryExecutionContext).deleteEntities(model, paramValues);
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
    protected int defaultBatchDelete(final EntityResultQueryModel<T> model) {
        return defaultBatchDelete(model, Collections.<String, Object> emptyMap());
    }

    /**
     * Batch deletion of entities in the provided list.
     *  
     * @param entities
     * @return
     */
    protected int defaultBatchDelete(final List<? extends AbstractEntity<?>> entities) {
        final Set<Long> ids = new LinkedHashSet<>();
        for (final AbstractEntity<?> entity : entities) {
            ids.add(entity.getId());
        }
        return co.batchDelete(ids);
    }
    
    /**
     * Batch deletion of entities by their ID values.
     * 
     * @param entitiesIds
     * @return
     */
    protected int defaultBatchDelete(final Collection<Long> entitiesIds) {
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
    protected int defaultBatchDeleteByPropertyValues(final String propName, final Collection<Long> entitiesIds) {
        if (entitiesIds.isEmpty()) {
            throw new EntityCompanionException("No entity ids have been provided for deletion.");
        }

        try {
            return new EntityBatchDeleteByIdsOperation<T>(co.getSession(), (PersistedEntityMetadata<T>) co.getDomainMetadata().getPersistedEntityMetadataMap().get(co.getEntityType())).deleteEntities(propName, entitiesIds);
        } catch (final ConstraintViolationException e) {
            throw new EntityCompanionException(DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES, e);
        }
    }


}
