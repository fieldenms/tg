package ua.com.fielden.platform.companion;

import static ua.com.fielden.platform.annotations.companion.Category.Operation.READ;
import static ua.com.fielden.platform.annotations.companion.Category.Operation.OTHER;
import static java.util.Collections.emptyMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import ua.com.fielden.platform.annotations.companion.Category;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.exceptions.UnexpectedNumberOfReturnedEntities;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
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
public interface IEntityReader<T extends AbstractEntity<?>> extends IEntityInstantiator<T>, IEntityStreamer<T> {

    /**
     * Should return an entity type the DAO is managing.
     *
     * @return
     */
    @Category(OTHER)
    Class<T> getEntityType();

    /**
     * Should return entity's key type.
     *
     * @return
     */
    @Category(OTHER)
    Class<? extends Comparable<?>> getKeyType();

    /**
     * A factory method that creates an instance of a companion object for the specified entity type.
     * The reader methods of such companion return <code>uninstrumented</code> entities.
     *
     * @return
     */
    @Category(OTHER)
    <C extends IEntityReader<E>, E extends AbstractEntity<?>> C co(final Class<E> type);

    /**
     * Returns default {@link FetchProvider} for the entity.
     * <p>
     * This fetch provider represents the 'aggregated' variant of all fetch providers needed mainly for entity master actions (and potentially others): <br>
     * <br>
     * 1. visual representation of entity properties in entity master UI <br>
     * 2. validation / modification processes with BCE / ACE / conversions handling <br>
     * 3. autocompletion of entity-typed properties
     *
     * @return
     */
    @Category(OTHER)
    IFetchProvider<T> getFetchProvider();

    /**
     * Should return true if entity with provided id and version value is stale, i.e. its version is older then the latest persisted entity with the same id.
     *
     * @param entityId
     * @param version
     * @return
     */
    @Category(READ)
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
    @Category(READ)
    T findById(final Long id, final fetch<T> fetchModel);

    @Category(READ)
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
    @Category(READ)
    T findById(final Long id);

    @Category(READ)
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
    @Category(READ)
    T findByKey(final Object... keyValues);

    @Category(READ)
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
    @Category(READ)
    T findByKeyAndFetch(final fetch<T> fetchModel, final Object... keyValues);

    @Category(READ)
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
    @Category(READ)
    T findByEntityAndFetch(final fetch<T> fetchModel, final T entity);

    @Category(READ)
    default Optional<T> findByEntityAndFetchOptional(final fetch<T> fetchModel, final T entity) {
        return Optional.ofNullable(findByEntityAndFetch(fetchModel, entity));
    }

    /**
     * Returns at most {@code numberOfEntities) first entities matching {@code query}.
     *
     * @param query
     * @param numberOfEntities
     * @return
     */
    @Category(READ)
    List<T> getFirstEntities(final QueryExecutionModel<T, ?> query, final int numberOfEntities);
    
    /**
     * Should return a reference to the first page of the specified size containing entity instances.
     *
     * @param pageCapacity
     * @return
     */
    @Category(READ)
    IPage<T> firstPage(final int pageCapacity);

    /**
     * Should return a reference to the first page of the specified size containing entity instances retrieved using the provided query model (new EntityQuery).
     *
     * @param pageCapacity
     * @param query
     * @return
     */
    @Category(READ)
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
    @Category(READ)
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
    @Category(READ)
    IPage<T> getPage(final int pageNo, final int pageCapacity);

    /**
     * Returns a reference to a page with requested number and capacity holding entity instances matching the provided query model (new EntityQuery).
     *
     * @param query
     * @param pageCapacity
     * @param pageNo
     * @return
     */
    @Category(READ)
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
    @Category(READ)
    IPage<T> getPage(final QueryExecutionModel<T, ?> query, final int pageNo, final int pageCount, final int pageCapacity);

    /**
     * Returns all entities produced by the provided query.
     * <p> 
     * Getting all entities matching the query may result in an excessive use of memory.
     * It should only be used if there is a certainty that the resultant list won't be too large.
     * <p>
     * In all other cases consider using Stream API. 
     *
     * @param quert
     * @return
     * 
     */
    @Category(READ)
    List<T> getAllEntities(final QueryExecutionModel<T, ?> query);

    /**
     * A convenient method for retrieving exactly one entity instance determined by the model. If more than one instance was found an exception is thrown. If there is no entity
     * found then a null value is returned.
     *
     * @param model
     * @return
     */
    @Category(READ)
    T getEntity(final QueryExecutionModel<T, ?> model);

    @Category(READ)
    default Optional<T> getEntityOptional(final QueryExecutionModel<T, ?> model) {
        try {
            return Optional.ofNullable(getEntity(model));
        } catch (final UnexpectedNumberOfReturnedEntities ex) {
            return Optional.empty();
        }
    }

    /**
     * Should return true if the passed entity exists in the persistent state.
     *
     * @param entity
     * @return
     */
    @Category(READ)
    boolean entityExists(final T entity);

    /**
     * Check whether entity of the managed by this DAO type and the provided ID exists.
     *
     * @param id
     * @return
     */
    @Category(READ)
    boolean entityExists(final Long id);

    /**
     * Should return true if an entity with the provided key exists in the persistent state.
     *
     * @param entity
     * @return
     */
    @Category(READ)
    boolean entityWithKeyExists(final Object... keyValues);

    /**
     * Returns a number of entities retrieved using the provided model.
     *
     * @param model
     * @return
     */
    @Category(READ)
    int count(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues);

    @Category(READ)
    int count(final EntityResultQueryModel<T> model);
    
    /**
     * Checks whether provided query model result is not empty.
     *
     * @param model
     * @return
     */
    @Category(READ)
    boolean exists(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues);

    @Category(READ)
    default boolean exists(final EntityResultQueryModel<T> model) {
        return exists(model, emptyMap());
    }
}
