package ua.com.fielden.platform.dao2;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage2;
import ua.com.fielden.platform.security.user.User;

/**
 * Defines a contract that should be implemented by any data access object being that a Hibernate or REST driven implementation.
 *
 * Business logic and UI should strictly depend only on DAO interfaces -- not the concrete implementations. This will ensure implementation flexibility of the concrete way to
 * access data.
 *
 * @author TG Team
 *
 */
public interface IEntityDao2<T extends AbstractEntity<?>> {
    static final int DEFAULT_PAGE_CAPACITY = 25;

    /**
     * Username should be provided for every DAO instance in order to support data filtering and auditing.
     */
    void setUsername(final String username);

    /**
     * Returns provided name.
     * @return
     */
    String getUsername();

    /**
     * Should return the current application user.
     *
     * @return
     */
    abstract User getUser();

    /**
     * Should return an entity type the DAO is managing.
     *
     * @return
     */
    Class<T> getEntityType();

    /**
     * Should return entity's key type.
     *
     * @return
     */
    Class<? extends Comparable> getKeyType();

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
    T findById(final Long id, fetch<T> fetchModel);

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

    /**
     * Finds entity by its business key. If the key is composite then values of the key components should be passed in the same order as defined in the entity class using
     * annotation {@link CompositeKeyMember}.
     *
     * @param keyValues
     * @return
     */
    T findByKey(final Object... keyValues);

    /**
     * Finds entity by its business key and enhances it according to provided fetch model. If the key is composite then values of the key components should be passed in the same
     * order as defined in the entity class using annotation {@link CompositeKeyMember}.
     *
     * @param keyValues
     * @return
     */
    T findByKeyAndFetch(fetch<T> fetchModel, final Object... keyValues);

    /**
     * Should return a reference to the first page of the specified size containing entity instances.
     *
     * @param pageCapacity
     * @return
     */
    IPage2<T> firstPage(final int pageCapacity);

    /**
     * Returns a reference to a page with requested number and capacity holding entity instances retrieved sequentially ordered by ID.
     *
     * @param Equery
     * @param pageCapacity
     * @param pageNo
     * @return
     */
    IPage2<T> getPage(final int pageNo, final int pageCapacity);

    /**
     * Should return a reference to the first page of the specified size containing entity instances retrieved using the provided query model (new EntityQuery).
     *
     * @param pageCapacity
     * @param query
     * @return
     */
    IPage2<T> firstPage(final QueryExecutionModel<T, ?> query, final int pageCapacity);

    /**
     * Should return a reference to the first page of the specified size containing entity instances retrieved using the provided <code>summaryModel</code> and the summary
     * information based on <code>summaryModel</code>.
     *
     * @param model
     * @param summaryModel
     * @param pageCapacity
     * @return
     */
    IPage2<T> firstPage(final QueryExecutionModel<T, ?> model, final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> summaryModel, final int pageCapacity);

    /**
     * Returns a reference to a page with requested number and capacity holding entity instances matching the provided query model (new EntityQuery).
     *
     * @param query
     * @param pageCapacity
     * @param pageNo
     * @return
     */
    IPage2<T> getPage(final QueryExecutionModel<T, ?> query, final int pageNo, final int pageCapacity);

    /**
     * Same as above, but the actual implementation could take into account the page count information.
     *
     * @param query
     * @param pageNo
     * @param pageCount
     * @param pageCapacity
     * @return
     */
    IPage2<T> getPage(final QueryExecutionModel<T, ?> query, final int pageNo, final int pageCount, final int pageCapacity);

    /**
     * Persists (saves/updates) the entity.
     *
     * @param entity
     * @return
     */
    T save(final T entity);

    /**
     * Deletes entity instance by its id. Currently, in most cases it is not supported since deletion is not a trivial operation.
     *
     * @param entity
     */
    void delete(final T entity);

    /**
     * Deletes entities returned by provided query model with provided param values.
     *
     * @param model
     */
    void delete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues);

    /**
     * Deletes entities returned by provided query model
     * @param model
     */
    void delete(final EntityResultQueryModel<T> model);

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
     * A convenient method for retrieving exactly one entity instance determined by the model. If more than one instance was found an exception is thrown. If there is no entity
     * found then a null value is returned.
     *
     * @param model
     * @return
     */
    T getEntity(final QueryExecutionModel<T, ?> model);

    /**
     * Returns a number of entities retrieved using the provided model.
     *
     * @param model
     * @return
     */
    int count(final EntityResultQueryModel<T> model, Map<String, Object> paramValues);

    int count(final EntityResultQueryModel<T> model);

    /**
     * Returns all entities produced by the provided query.
     *
     * @param quert
     * @return
     */
    List<T> getAllEntities(final QueryExecutionModel<T, ?> query);

    /**
     * Should return a byte array representation the exported data in a format envisaged by the specific implementation.
     * <p>
     * For example it could be a byte array of GZipped Excel data.
     *
     * @param query
     *            -- query result of which should be exported.
     * @param propertyNames
     *            -- names of properties, including dot notated properties, which should be used in the export.
     * @param propertyTitles
     *            -- titles corresponding to the properties being exported, which are used as headers of columns.
     * @return
     */
    byte[] export(final QueryExecutionModel<T, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException;
}