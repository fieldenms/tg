package ua.com.fielden.platform.companion;

import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.QueryExecutionModel.Builder;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.dao.exceptions.UnexpectedNumberOfReturnedEntities;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IEntityFetcher;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.IFillModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.utils.Pair;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.companion.helper.KeyConditionBuilder.createQueryByKey;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.pagination.IPage.pageCount;
import static ua.com.fielden.platform.pagination.IPage.realPageCount;

/**
 * This is a base class that implements contract {@link IEntityReader}. 
 * It is designed to be sub-typed in order to implement abstract methods, which are introduced here and are more of an infrastructural nature.
 * <p>
 * The main purpose to have this class extended, is to enable correct DB session association with instances of {@link IPage} that are produced by various pagination API methods.
 * These pages can move between next or previous pages, thus, requiring a DB session.
 * However, by themselves, neither page instances nor this class are session-enabled.
 * So, this class should be sub-typed with a type that implements {@link ISessionEnabled}.     
 * 
 * @author TG Team
 *
 * @param <T>
 */
public abstract class AbstractEntityReader<T extends AbstractEntity<?>> implements IEntityReader<T> {
    private static final Logger LOGGER = getLogger();

    public static final String ERR_MISSING_ID_VALUE = "Argument [id] must have a value to find an instance of [%s].";
    public static final String ERR_COULD_NOT_FETCH_ONE_ENTITY = "Could not fetch one entity of type [%s].";
    public static final String ERR_COULD_NOT_FIND_AND_FETCH_BY_KEY = "Could not find and fetch by key an entity of type [%s].";
    public static final String ERR_MORE_THAN_ONE_ENTITY = "The provided query model leads to retrieval of more than one entity (%s).";

    ///////////////////////////////////////////////////////////
    ////////////// Infrastructural methods ////////////////////
    ///////////////////////////////////////////////////////////

    protected abstract Session getSession();

    protected abstract DbVersion getDbVersion();

    protected abstract boolean instrumented();

    protected abstract IEntityFetcher entityFetcher();

    ///////////////////////////////////////////////////////////
    /////////////// Entity reader API methods /////////////////
    ///////////////////////////////////////////////////////////
    
    @Override
    @SessionRequired
    public boolean isStale(final Long entityId, final Long version) {
        if (entityId == null) {
            return false;
        }

        return !exists(select(getEntityType()).where().prop(ID).eq().val(entityId).and().prop(VERSION).eq().val(version).model());
    }

    @Override
    @SessionRequired
    public T findById(final Long id) {
        return findById(id, null);
    }

    @Override
    @SessionRequired
    public T findById(final boolean filtered, final Long id, final fetch<T> fetchModel, final IFillModel<T> fillModel) {
        return fetchOneEntityInstance(filtered, id, fetchModel, fillModel);
    }
    
    @Override
    @SessionRequired
    public T findByKeyAndFetch(final boolean filtered, final fetch<T> fetchModel, final Object... keyValues) {
        try {
            final EntityResultQueryModel<T> queryModel = createQueryByKey(getDbVersion(), getEntityType(), getKeyType(), filtered, keyValues);
            final Builder<T, EntityResultQueryModel<T>> qemBuilder = from(queryModel).with(fetchModel);
            return getEntity(instrumented() ? qemBuilder.model() : qemBuilder.lightweight().model());
        } catch (final EntityCompanionException e) {
            throw e;
        } catch (final Exception e) {
            throw new EntityCompanionException(ERR_COULD_NOT_FIND_AND_FETCH_BY_KEY.formatted(getEntityType().getName()), e);
        }
    }

    @Override
    @SessionRequired
    public T findByKey(final Object... keyValues) {
        return findByKeyAndFetch(null, keyValues);
    }

    @Override
    @SessionRequired
    public T findByEntityAndFetch(final fetch<T> fetchModel, final T entity) {
        if (entity.getId() != null) {
            return findById(entity.getId(), fetchModel);
        } else {
            return findByKeyAndFetch(fetchModel, entity.getKey());
        }
    }

    @Override
    @SessionRequired
    public boolean entityExists(final T entity) {
        if (entity == null) {
            return false;
        }
        return entityExists(entity.getId());
    }

    @Override
    @SessionRequired
    public boolean entityExists(final Long id) {
        if (id == null) {
            return false;
        }

        return exists(select(getEntityType()).where().prop(ID).eq().val(id).model());
    }

    @Override
    @SessionRequired
    public boolean entityWithKeyExists(final Object... keyValues) {
        final T entity = findByKeyAndFetch(null, keyValues);
        return entity != null;
    }

    @Override
    @SessionRequired
    public int count(final EntityResultQueryModel<T> model) {
        return count(model, emptyMap());
    }

    @Override
    @SessionRequired
    public int count(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        return evalNumOfPages(model, paramValues, 1).getKey();
    }
    
    @Override
    @SessionRequired
    public boolean exists(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        final AggregatedResultQueryModel existsQuery = select().yield().caseWhen().exists(model).then().val(1).otherwise().val(0).endAsInt().as("exists").modelAsAggregate();
        final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> countModel = from(existsQuery).with(paramValues).with(fetchAggregates().with("exists")).lightweight().model();
        final int result = entityFetcher().getEntities(getSession(), countModel).get(0).get("exists");
        return result == 1;
    }

    /**
     * Returns a stream of entities that match the provided query.
     * <p>
     * The returned stream must always be wrapped into <code>try with resources</code> clause to ensure that the underlying resultset is closed.
     */
    @Override
    @SessionRequired
    public Stream<T> stream(final QueryExecutionModel<T, ?> queryModel) {
        return stream(queryModel, 100);
    }

    /**
     * Returns a stream of entities that match the provided query. 
     * Argument <code>fetchSize</code> provides a hint how many rows should be fetched in a batch at the time of scrolling.
     * <p>
     * The returned stream must always be wrapped into <code>try with resources</code> clause to ensure that the underlying resultset is closed.
     */
    @Override
    @SessionRequired
    public Stream<T> stream(final QueryExecutionModel<T, ?> queryModel, final int fetchSize) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? queryModel.lightweight() : queryModel;
        return entityFetcher().streamEntities(getSession(), qem, Optional.of(fetchSize));
    }
    
    /**
     * Returns at most {@code numberOfEntities) first entities matching {@code query}. 
     */
    @Override
    @SessionRequired
    public List<T> getFirstEntities(final QueryExecutionModel<T, ?> query, final int numberOfEntities) {
        return getEntitiesOnPage(query, 0, numberOfEntities);
    }

    /**
     * Returns a first page holding up to <code>size</code> instance of entities retrieved by the provided query model. This allows a query based pagination.
     */
    @Override
    @SessionRequired
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> model, final int pageCapacity) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? model.lightweight() : model;
        return new EntityQueryPage(qem, 0, pageCapacity, evalNumOfPages(qem.getQueryModel(), qem.getParamValues(), pageCapacity));
    }

    /**
     * Returns a first page holding up to <code>pageCapacity</code> instance of entities retrieved by the provided query model with appropriate summary model. This allows a query
     * based pagination.
     */
    @Override
    @SessionRequired
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> model, final QueryExecutionModel<T, ?> summaryModel, final int pageCapacity) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? model.lightweight() : model;
        return new EntityQueryPage(qem, summaryModel, 0, pageCapacity, evalNumOfPages(qem.getQueryModel(), qem.getParamValues(), pageCapacity));
    }

    @Override
    @SessionRequired
    public IPage<T> getPage(final QueryExecutionModel<T, ?> model, final int pageNo, final int pageCapacity) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? model.lightweight() : model;
        return getPage(qem, pageNo, 0, pageCapacity);
    }

    @Override
    @SessionRequired
    public IPage<T> getPage(final QueryExecutionModel<T, ?> model, final int pageNo, final int pageCount, final int pageCapacity) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? model.lightweight() : model;
        
        final Pair<Integer, Integer> numberOfPagesAndCount = pageCount > 0 ? Pair.pair(pageCount, pageCount * pageCapacity) : evalNumOfPages(qem.getQueryModel(), qem.getParamValues(), pageCapacity);

        final int pageNumber = pageNo < 0 ? numberOfPagesAndCount.getKey() - 1 : pageNo;
        return new EntityQueryPage(qem, pageNumber, pageCapacity, numberOfPagesAndCount);
    }

    /**
     * Fetches the results of the specified page based on the request of the given instance of {@link QueryExecutionModel}.
     * <p>
     * This method is required strictly for "capturing" a DB session to enable pagination as implemented by {@link EntityQueryPage}.
     *
     * @param queryModel
     * @param pageNumber
     * @param pageCapacity
     * @return
     */
    @SessionRequired
    protected List<T> getEntitiesOnPage(final QueryExecutionModel<T, ?> queryModel, final Integer pageNumber, final Integer pageCapacity) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? queryModel.lightweight() : queryModel;
        return entityFetcher().getEntitiesOnPage(getSession(), qem, pageNumber, pageCapacity);
    }

    @Override
    @SessionRequired
    public T getEntity(final QueryExecutionModel<T, ?> model) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? model.lightweight() : model;
        final List<T> data = getFirstEntities(qem, 2);
        if (data.size() > 1) {
            throw new UnexpectedNumberOfReturnedEntities(ERR_MORE_THAN_ONE_ENTITY.formatted(data.size()));
        }
        return data.size() == 1 ? data.get(0) : null;
    }

    @Override
    @SessionRequired
    public Optional<T> getEntityOptional(final QueryExecutionModel<T, ?> model) {
        final var qem = !instrumented() ? model.lightweight() : model;
        final List<T> data = getFirstEntities(qem, 2);
        return data.size() == 1 ? Optional.of(data.getFirst()) : Optional.empty();
    }

    /**
     * Calculates the number of pages with a given size, needed to fit all records in a result set.
     *
     *
     * @param model
     * @param pageCapacity
     * @return
     */
    @SessionRequired
    protected Pair<Integer, Integer> evalNumOfPages(final QueryModel<T> model, final Map<String, Object> paramValues, final int pageCapacity) {
        final AggregatedResultQueryModel countQuery = model instanceof EntityResultQueryModel ? select((EntityResultQueryModel<T>) model).yield().countAll().as("count").modelAsAggregate()
                : select((AggregatedResultQueryModel) model).yield().countAll().as("count").modelAsAggregate();
        final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> countModel = from(countQuery).with(paramValues).with(fetchAggregates().with("count")).lightweight().model();

        final List<EntityAggregates> counts = entityFetcher().getEntities(getSession(), countModel);

        final int resultSize = ((Number) counts.get(0).get("count")).intValue();

        final Integer pageSize = realPageCount(resultSize, pageCapacity);

        return Pair.pair(pageSize, resultSize);
    }

    /**
     * A private helper method.
     *
     * @param filtered controls whether user-filtering is on
     */
    private T fetchOneEntityInstance(final boolean filtered, final Long id, final fetch<T> fetchModel, final IFillModel<T> fillModel) {
        if (id == null) {
            throw new EntityCompanionException(ERR_MISSING_ID_VALUE.formatted(getEntityType().getName()));
        }

        final var query = select(getEntityType()).where().prop(ID).eq().val(id).model().setFilterable(filtered);
        final var qem = from(query).with(fetchModel).with(fillModel).lightweight(!instrumented()).model();
        try {
            return getEntity(qem);
        } catch (final Exception ex) {
            final var exception = new EntityCompanionException(ERR_COULD_NOT_FETCH_ONE_ENTITY.formatted(getEntityType().getSimpleName()), ex);
            LOGGER.error(() -> "%s\nQuery: %s".formatted(exception.getMessage(), qem), ex);
            throw exception;
        }
    }

    /**
     * Implements pagination based on the provided query.
     *
     * @author TG Team
     *
     */
    public class EntityQueryPage implements IPage<T> {
        private static final String ERR_MSG_NO_QUERY_PROVIDED = "There was no query provided to retrieve the data.";
        private final int pageNumber; // zero-based
        private final Pair<Integer, Integer> numberOfPagesAndCount;
        private final int numberOfPages;
        private final int pageCapacity;
        private final List<T> data;
        private final QueryExecutionModel<T, ?> queryModel;
        private final T summary;

        public EntityQueryPage(final QueryExecutionModel<T, ?> queryModel, final int pageNumber, final int pageCapacity, final Pair<Integer, Integer> numberOfPagesAndCount) {
            this(queryModel, (QueryExecutionModel<T, ?>) null, pageNumber, pageCapacity, numberOfPagesAndCount);
        }

        public EntityQueryPage(final QueryExecutionModel<T, ?> queryModel, final QueryExecutionModel<T, ?> summaryModel, final int pageNumber, final int pageCapacity, final Pair<Integer, Integer> numberOfPagesAndCount) {
            this(queryModel, summaryModel != null && numberOfPagesAndCount.getValue() > 0 ? getEntity(summaryModel) : null, pageNumber, pageCapacity, numberOfPagesAndCount);
        }

        public EntityQueryPage(final QueryExecutionModel<T, ?> queryModel, final T summary, final int pageNumber, final int pageCapacity, final Pair<Integer, Integer> numberOfPagesAndCount) {
            this.numberOfPagesAndCount = numberOfPagesAndCount;
            this.pageNumber = pageNumber;
            this.pageCapacity = pageCapacity;
            this.numberOfPages = pageCount(numberOfPagesAndCount.getKey());
            this.queryModel = queryModel;
            this.data = numberOfPagesAndCount.getValue() > 0 ? getEntitiesOnPage(queryModel, pageNumber, pageCapacity) : new ArrayList<>();

            this.summary = summary;
        }

        @Override
        public T summary() {
            return summary;
        }

        @Override
        public int capacity() {
            return pageCapacity;
        }

        @Override
        public List<T> data() {
            return Collections.unmodifiableList(data);
        }

        @Override
        public boolean hasNext() {
            return pageNumber < numberOfPages - 1;
        }

        @Override
        public boolean hasPrev() {
            return no() > 0;
        }

        @Override
        public IPage<T> next() {
            if (hasNext()) {
                if (queryModel != null && summary != null) {
                    return new EntityQueryPage(queryModel, summary, pageNumber + 1, pageCapacity, numberOfPagesAndCount);
                } else if (queryModel != null) {
                    return new EntityQueryPage(queryModel, pageNumber + 1, pageCapacity, numberOfPagesAndCount);
                } else {
                    throw new EntityCompanionException(ERR_MSG_NO_QUERY_PROVIDED);
                }
            }
            return null;
        }

        @Override
        public IPage<T> prev() {
            if (hasPrev()) {
                if (queryModel != null && summary != null) {
                    return new EntityQueryPage(queryModel, summary, pageNumber - 1, pageCapacity, numberOfPagesAndCount);
                } else if (queryModel != null) {
                    return new EntityQueryPage(queryModel, pageNumber - 1, pageCapacity, numberOfPagesAndCount);
                } else {
                    throw new EntityCompanionException(ERR_MSG_NO_QUERY_PROVIDED);
                }
            }
            return null;
        }

        @Override
        public IPage<T> first() {
            if (hasPrev()) {
                if (queryModel != null && summary != null) {
                    return new EntityQueryPage(queryModel, summary, 0, pageCapacity, numberOfPagesAndCount);
                } else if (queryModel != null) {
                    return new EntityQueryPage(queryModel, 0, pageCapacity, numberOfPagesAndCount);
                } else {
                    throw new EntityCompanionException(ERR_MSG_NO_QUERY_PROVIDED);
                }
            }
            return null;
        }

        @Override
        public IPage<T> last() {
            if (hasNext()) {
                if (queryModel != null && summary != null) {
                    return new EntityQueryPage(queryModel, summary, numberOfPages - 1, pageCapacity, numberOfPagesAndCount);
                } else if (queryModel != null) {
                    return new EntityQueryPage(queryModel, numberOfPages - 1, pageCapacity, numberOfPagesAndCount);
                } else {
                    throw new EntityCompanionException(ERR_MSG_NO_QUERY_PROVIDED);
                }
            }
            return null;
        }

        @Override
        public int numberOfPages() {
            return numberOfPages;
        }

        @Override
        public String toString() {
            return "Page " + (no() + 1) + " of " + numberOfPages;
        }

        @Override
        public int no() {
            return pageNumber;
        }
    }

}
