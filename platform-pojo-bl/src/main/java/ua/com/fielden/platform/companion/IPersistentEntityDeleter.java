package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/// A part of the mutator contract that provides various method for deletion of persistent entities.
///
public interface IPersistentEntityDeleter<T extends AbstractEntity<?>> {

    String ERR_UNSUPPORTED_DELETION = "By default deletion is not supported.";
    String ERR_UNSUPPORTED_BATCH_DELETION = "Batch deletion should be implemented in descendants.";

    /// Deletes entity instance by its `id`.
    /// Unsupported by default, since deletion is not a trivial operation.
    ///
    default void delete(final T entity) {
        throw new UnsupportedOperationException(ERR_UNSUPPORTED_DELETION);
    }

    /// Deletes entities returned by provided `query` with `paramValues`.
    /// Unsupported by default, since deletion is not a trivial operation.
    ///
    default void delete(final EntityResultQueryModel<T> query, final Map<String, Object> paramValues) {
        throw new UnsupportedOperationException(ERR_UNSUPPORTED_DELETION);
    }

    /// Deletes entities returned by provided `query`.
    /// Redirects the call to [#delete(EntityResultQueryModel, Map)] with empty parameters.
    ///
    default void delete(final EntityResultQueryModel<T> query) {
        delete(query, Collections.<String, Object> emptyMap());
    }

    /// Performs batch deletion of entities returned by provided `query`.
    /// Unsupported by default, since deletion is not a trivial operation.
    ///
    default int batchDelete(final EntityResultQueryModel<T> query) {
        throw new UnsupportedOperationException(ERR_UNSUPPORTED_BATCH_DELETION);
    }

    /// Performs batch deletion of entities returned by provided `query` with `paramValues`.
    /// Unsupported by default, since deletion is not a trivial operation.
    ///
    default int batchDelete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        throw new UnsupportedOperationException(ERR_UNSUPPORTED_BATCH_DELETION);
    }

    /// Performs batch deletion of entities, identified by their IDs.
    /// Unsupported by default, since deletion is not a trivial operation.
    ///
    default int batchDelete(final Collection<Long> entitiesIds) {
        throw new UnsupportedOperationException(ERR_UNSUPPORTED_BATCH_DELETION);
    }

    /// Performs batch deletion of entities.
    /// Unsupported by default, since deletion is not a trivial operation.
    ///
    default int batchDelete(final List<T> entities){
        throw new UnsupportedOperationException(ERR_UNSUPPORTED_BATCH_DELETION);
    }
    
    /// Performs batch deletion of entities, which are identified by matching their entity-typed property `propName` against IDs in `propEntitiesIds`.
    ///
    /// Here is what it means in terms of EQL:
    /// ```Java
    /// select(entityType).where().prop(propName).in().values(entitiesIds).model()
    /// ```
    ///
    /// Unsupported by default, since deletion is not a trivial operation.
    ///
    default int batchDeleteByPropertyValues(final String propName, final Collection<Long> propEntitiesIds) {
        throw new UnsupportedOperationException(ERR_UNSUPPORTED_BATCH_DELETION);
    }

    /// The same as [#batchDeleteByPropertyValues(String, Collection)], but for a list of entities.
    /// Unsupported by default, since deletion is not a trivial operation.
    ///
    default <E extends AbstractEntity<?>> int batchDeleteByPropertyValues(final String propName, final List<E> propEntities) {
        throw new UnsupportedOperationException(ERR_UNSUPPORTED_BATCH_DELETION);
    }

}
