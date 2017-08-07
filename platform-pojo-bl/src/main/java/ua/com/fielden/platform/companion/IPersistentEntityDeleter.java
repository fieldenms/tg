package ua.com.fielden.platform.companion;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * A part of the mutator contract that provides various method for deletion of persistent entities.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public interface IPersistentEntityDeleter<T extends AbstractEntity<?>> {

    /**
     * Deletes entity instance by its id. Currently, in most cases it is not supported since deletion is not a trivial operation.
     *
     * @param entity
     */
    default void delete(final T entity) {
        throw new UnsupportedOperationException("By default deletion is not supported.");
    }

    /**
     * Deletes entities returned by provided query model with provided param values.
     *
     * @param model
     */
    default void delete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        throw new UnsupportedOperationException("By default deletion is not supported.");
    }

    /**
     * Deletes entities returned by provided query model
     *
     * @param model
     */
    default void delete(final EntityResultQueryModel<T> model) {
        delete(model, Collections.<String, Object> emptyMap());
    }

    /**
     * Performs batch deletion of entities returned by provided query model
     *
     * @param model
     */
    default int batchDelete(final EntityResultQueryModel<T> model) {
        throw new UnsupportedOperationException("Batch deletion should be implemented in descendants.");
    }

    /**
     * Performs batch deletion of entities returned by provided query model with provided param values.
     *
     * @param model
     */
    default int batchDelete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        throw new UnsupportedOperationException("Batch deletion should be implemented in descendants.");
    }

    /**
     * Performs batch deletion of entities, which ids are provided by collection.
     *
     * @param entitiesIds
     */
    default int batchDelete(final Collection<Long> entitiesIds) {
        throw new UnsupportedOperationException("By default batch deletion is not supported.");
    }

    /**
     * Performs batch deletion of entities from provided list.
     *
     * @param entitiesIds
     */
    default int batchDelete(final List<T> entities){
        throw new UnsupportedOperationException("By default batch deletion is not supported.");
    }
    
    /**
     * Performs batch deletion of entities, which entity property values are within provided collection.
     * 
     * @param propName
     * @param propEntitiesIds
     * @return
     */
    default int batchDeleteByPropertyValues(final String propName, final Collection<Long> propEntitiesIds) {
        throw new UnsupportedOperationException("By default batch deletion is not supported.");
    }

    /**
     * Performs batch deletion of entities, which entity property values are within provided list.
     * 
     * @param propName
     * @param propEntities
     * @return
     */
    default <E extends AbstractEntity<?>> int batchDeleteByPropertyValues(final String propName, final List<E> propEntities) {
        throw new UnsupportedOperationException("By default batch deletion is not supported.");
    }
}
