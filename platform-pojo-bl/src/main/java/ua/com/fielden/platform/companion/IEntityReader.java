package ua.com.fielden.platform.companion;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;

/**
 * The reader contract for entity companion objects, which should be implemented by companions of persistent or synthetic entities.
 * It provides various methods to read entities from a persistent data store.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public interface IEntityReader<T extends AbstractEntity<?>> {

    /**
     * Returns all entities produced by the provided query.
     *
     * @param quert
     * @return
     * @deprecated Streaming API must be used instead.
     */
    @Deprecated
    List<T> getAllEntities(final QueryExecutionModel<T, ?> query);

    /**
     * Returns first entities produced by the provided query.
     *
     * @param quert
     * @return
     * @deprecated Streaming API must be used instead.
     */
    @Deprecated
    List<T> getFirstEntities(final QueryExecutionModel<T, ?> query, final int numberOfEntities);

    /**
     * Should return true if entity with provided id and version value is stale, i.e. its version is older then the latest persisted entity with the same id.
     *
     * @param entityId
     * @param version
     * @return
     */
    boolean isStale(final Long entityId, final Long version);

    /**
     * Finds entity by its surrogate id.
     *
     * @param id
     *            -- ID of the entity to be loaded.
     * @param models
     *            -- one or more fetching models specifying the initialisation strategy (i.e. what properties should be retrieved).
     * @return
     */
    T findById(final Long id, final fetch<T> fetchModel);
    
    default Optional<T> findByIdOptional(final Long id, final fetch<T> fetchModel) {
        return Optional.ofNullable(findById(id, fetchModel)); 
    }

    /**
     * Finds entity by its surrogate id.
     *
     * @param id
     *            -- ID of the entity to be loaded.
     * @param models
     *            -- one or more fetching models specifying the initialisation strategy (i.e. what properties should be retrieved).
     * @return
     */
    T findById(final Long id);
    
    default Optional<T> findByIdOptional(final Long id) {
        return Optional.ofNullable(findById(id));
    }

    /**
     * Finds entity by its business key. If the key is composite then values of the key components should be passed in the same order as defined in the entity class using
     * annotation {@link CompositeKeyMember}.
     *
     * @param keyValues
     * @return
     */
    T findByKey(final Object... keyValues);
    
    default Optional<T> findByKeyOptional(final Object... keyValues) {
        return Optional.ofNullable(findByKey(keyValues));
    }

    /**
     * Finds entity by its business key and enhances it according to provided fetch model. If the key is composite then values of the key components should be passed in the same
     * order as defined in the entity class using annotation {@link CompositeKeyMember}.
     *
     * @param keyValues
     * @return
     */
    T findByKeyAndFetch(final fetch<T> fetchModel, final Object... keyValues);
    
    default Optional<T> findByKeyAndFetchOptional(final fetch<T> fetchModel, final Object... keyValues) {
        return Optional.ofNullable(findByKeyAndFetch(fetchModel, keyValues));
    }

    /**
     * Finds entity by its instance and enhances it according to provided fetch model.
     *
     * @param fetchModel
     * @param entity
     * @return
     */
    T findByEntityAndFetch(final fetch<T> fetchModel, final T entity);
    
    default Optional<T> findByEntityAndFetchOptional(final fetch<T> fetchModel, final T entity) {
        return Optional.ofNullable(findByEntityAndFetch(fetchModel, entity));
    }

    /**
     * Should return a reference to the first page of the specified size containing entity instances.
     *
     * @param pageCapacity
     * @return
     */
    IPage<T> firstPage(final int pageCapacity);

    /**
     * Should return a reference to the first page of the specified size containing entity instances retrieved using the provided query model (new EntityQuery).
     *
     * @param pageCapacity
     * @param query
     * @return
     */
    IPage<T> firstPage(final QueryExecutionModel<T, ?> query, final int pageCapacity);

    /**
     * Should return a reference to the first page of the specified size containing entity instances retrieved using the provided <code>summaryModel</code> and the summary
     * information based on <code>summaryModel</code>.
     *
     * @param model
     * @param summaryModel
     * @param pageCapacity
     * @return
     */
    default IPage<T> firstPage(final QueryExecutionModel<T, ?> model, final QueryExecutionModel<T, ?> summaryModel, final int pageCapacity) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * Returns a reference to a page with requested number and capacity holding entity instances retrieved sequentially ordered by ID.
     *
     * @param Equery
     * @param pageCapacity
     * @param pageNo
     * @return
     */
    IPage<T> getPage(final int pageNo, final int pageCapacity);

    /**
     * Returns a reference to a page with requested number and capacity holding entity instances matching the provided query model (new EntityQuery).
     *
     * @param query
     * @param pageCapacity
     * @param pageNo
     * @return
     */
    IPage<T> getPage(final QueryExecutionModel<T, ?> query, final int pageNo, final int pageCapacity);

    /**
     * Same as above, but the actual implementation could take into account the page count information.
     *
     * @param query
     * @param pageNo
     * @param pageCount
     * @param pageCapacity
     * @return
     */
    IPage<T> getPage(final QueryExecutionModel<T, ?> query, final int pageNo, final int pageCount, final int pageCapacity);

    /**
     * A convenient method for retrieving exactly one entity instance determined by the model. If more than one instance was found an exception is thrown. If there is no entity
     * found then a null value is returned.
     *
     * @param model
     * @return
     */
    T getEntity(final QueryExecutionModel<T, ?> model);

    default Optional<T> getEntityOptional(final QueryExecutionModel<T, ?> model) {
        return Optional.ofNullable(getEntity(model));
    }
    
    /**
     * Returns a non-parallel stream with the data based on the provided query.
     * The returned stream must always be wrapped into <code>try with resources</code> clause to ensure that the underlying resultset is closed.
     * 
     * @param qem -- EQL model
     * @param fetchSize -- a batch size for retrieve the next lot of data to feed the stream
     * @return
     */
    Stream<T> stream(final QueryExecutionModel<T, ?> qem, final int fetchSize);
    
    /**
     * A convenience method based on {@link #stream(QueryExecutionModel, int), but with a default fetch size. 
     * The returned stream must always be wrapped into <code>try with resources</code> clause to ensure that the underlying resultset is closed.
     * 
     * @param qem
     * @return
     */
    Stream<T> stream(final QueryExecutionModel<T, ?> qem);

    /**
     * Should return true if the passed entity exists in the persistent state.
     *
     * @param entity
     * @return
     */
    boolean entityExists(final T entity);

    /**
     * Check whether entity of the managed by this DAO type and the provided ID exists.
     *
     * @param id
     * @return
     */
    boolean entityExists(final Long id);

    /**
     * Should return true if an entity with the provided key exists in the persistent state.
     *
     * @param entity
     * @return
     */
    boolean entityWithKeyExists(final Object... keyValues);

    /**
     * Returns a number of entities retrieved using the provided model.
     *
     * @param model
     * @return
     */
    int count(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues);

    int count(final EntityResultQueryModel<T> model);
}
