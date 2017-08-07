package ua.com.fielden.platform.companion;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.companion.KeyConditionHelper.createQueryByKey;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAggregates;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.hibernate.Session;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.QueryExecutionModel.Builder;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.dao.exceptions.UnexpectedNumberOfReturnedEntities;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.EntityFetcher;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.QueryExecutionContext;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.utils.Pair;

public class EntityReader<T extends AbstractEntity<?>> implements IEntityReader<T> {
    
    private final Class<T> entityType;
    private final Class<? extends Comparable<?>> keyType;
    private final boolean filterable;
    private final BooleanSupplier shouldInstrument;
    
    private final Supplier<Session> session;
    private final Supplier<EntityFactory> entityFactory;
    private final Supplier<ICompanionObjectFinder> coFinder;
    private final Supplier<DomainMetadata> domainMetadata;
    private final Supplier<IFilter> filter;
    private final Supplier<String> username;
    private final Supplier<IUniversalConstants> universalConstants;
    private final Supplier<IdOnlyProxiedEntityTypeCache> idOnlyProxiedEntityTypeCache;
    
    public EntityReader(
            final Class<T> entityType,
            final Class<? extends Comparable<?>> keyType,
            final boolean filterable,
            final BooleanSupplier shouldInstrument,
            final Supplier<Session> session,
            final Supplier<EntityFactory> entityFactory,
            final Supplier<ICompanionObjectFinder> coFinder,
            final Supplier<DomainMetadata> domainMetadata,
            final Supplier<IFilter> filter,
            final Supplier<String> username,
            final Supplier<IUniversalConstants> universalConstants,
            final Supplier<IdOnlyProxiedEntityTypeCache> idOnlyProxiedEntityTypeCache) {
        this.entityType = entityType;
        this.keyType = keyType;
        this.filterable = filterable;
        this.shouldInstrument = shouldInstrument;
        
        this.session = session;
        this.entityFactory = entityFactory;
        this.coFinder = coFinder;
        this.domainMetadata = domainMetadata;
        this.filter = filter;
        this.username = username;
        this.universalConstants = universalConstants;
        this.idOnlyProxiedEntityTypeCache = idOnlyProxiedEntityTypeCache;
    }
    
    public Class<T> getEntityType() {
        return entityType;
    }
    
    public Class<? extends Comparable<?>> getKeyType() {
        return keyType;
    }

    
    private boolean instrumented() {
        return shouldInstrument.getAsBoolean();
    }
    
    @Override
    public boolean isStale(final Long entityId, final Long version) {
        if (entityId == null) {
            return false;
        }

        final Integer count = ((Number) session.get().createQuery("select count(*) from " + getEntityType().getName() + " where id = :id and version = :version")//
        .setParameter("id", entityId).setParameter("version", version).uniqueResult()).intValue();

        return count != 1;
    }

    @Override
    public T findById(final Long id) {
        return findById(id, null);
    }

    @Override
    public T findById(final Long id, final fetch<T> fetchModel) {
        return fetchOneEntityInstance(id, fetchModel);
    }

    @Override
    public T findByKeyAndFetch(final fetch<T> fetchModel, final Object... keyValues) {
        try {
            final EntityResultQueryModel<T> queryModel = createQueryByKey(getEntityType(), getKeyType(), filterable, keyValues);
            final Builder<T, EntityResultQueryModel<T>> qemBuilder = from(queryModel).with(fetchModel);
            return getEntity(instrumented() ? qemBuilder.model() : qemBuilder.lightweight().model());
        } catch (final EntityCompanionException e) {
            throw e;
        } catch (final Exception e) {
            throw new EntityCompanionException(format("Could not find and fetch by key an entity of type [%s].", getEntityType().getName()), e);
        }
    }

    @Override
    public T findByKey(final Object... keyValues) {
        return findByKeyAndFetch(null, keyValues);
    }

    @Override
    public T findByEntityAndFetch(final fetch<T> fetchModel, final T entity) {
        if (entity.getId() != null) {
            return findById(entity.getId(), fetchModel);
        } else {
            return findByKeyAndFetch(fetchModel, entity.getKey());
        }
    }

    @Override
    public boolean entityExists(final T entity) {
        return entityExists(entity.getId());
    }

    @Override
    public boolean entityExists(final Long id) {
        if (id == null) {
            return false;
        }
        return session.get().createQuery("select id from " + getEntityType().getName() + " where id = :in_id").setLong("in_id", id).uniqueResult() != null;
    }

    /**
     * Method checks whether the key of the entity type associated with this DAO if composite or not.
     *
     * If composite then <code>WHERE</code> statement is build using composite key members and the passed values. The number of values should match the number of composite key
     * members.
     *
     * Otherwise, <code>WHERE</code> statement is build using only property <code>key</code>.
     *
     * The created query expects a unique result, and throws a runtime exception if this is not the case.
     *
     * TODO Need to consider the case of polymorphic associations such as Rotable, which can be both Bogie and/or Wheelset.
     */
    @Override
    public boolean entityWithKeyExists(final Object... keyValues) {
        final T entity = findByKeyAndFetch(null, keyValues);
        return entity != null;
    }
    
    @Override
    public int count(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        return evalNumOfPages(model, paramValues, 1).getKey();
    }

    @Override
    public int count(final EntityResultQueryModel<T> model) {
        return count(model, Collections.<String, Object> emptyMap());
    }

    /**
     * Returns a stream of entities that match the provided query. Argument <code>fetchSize</code> provides a hint how many rows should be fetched in a batch at the time of scrolling.
     * 
     * The returned stream must always be wrapped into <code>try with resources</code> clause to ensure that the underlying resultset is closed.
     */
    @Override
    public Stream<T> stream(final QueryExecutionModel<T, ?> queryModel, final int fetchSize) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? queryModel.lightweight() : queryModel;
        return new EntityFetcher(newQueryExecutionContext()).streamEntities(qem, Optional.of(fetchSize));
    }
    
    /**
     * Returns a stream of entities that match the provided query.
     * The returned stream must always be wrapped into <code>try with resources</code> clause to ensure that the underlying resultset is closed.
     */
    @Override
    public Stream<T> stream(final QueryExecutionModel<T, ?> queryModel) {
        return stream(queryModel, 100);
    }

    /**
     * Returns a first page holding up to <code>pageCapacity</code> instance of entities retrieved by query with no filtering conditions. Useful for things like autocompleters.
     */
    @Override
    public IPage<T> firstPage(final int pageCapacity) {
        return new EntityQueryPage(getDefaultQueryExecutionModel(), 0, pageCapacity, evalNumOfPages(getDefaultQueryExecutionModel().getQueryModel(), Collections.<String, Object> emptyMap(), pageCapacity));
    }
    
    /**
     * Returns a first page holding up to <code>size</code> instance of entities retrieved by the provided query model. This allows a query based pagination.
     */
    @Override
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> model, final int pageCapacity) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? model.lightweight() : model;
        return new EntityQueryPage(qem, 0, pageCapacity, evalNumOfPages(qem.getQueryModel(), qem.getParamValues(), pageCapacity));
    }

    /**
     * Returns a first page holding up to <code>pageCapacity</code> instance of entities retrieved by the provided query model with appropriate summary model. This allows a query
     * based pagination.
     */
    @Override
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> model, final QueryExecutionModel<T, ?> summaryModel, final int pageCapacity) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? model.lightweight() : model;
        return new EntityQueryPage(qem, summaryModel, 0, pageCapacity, evalNumOfPages(qem.getQueryModel(), qem.getParamValues(), pageCapacity));
    }

    @Override
    public IPage<T> getPage(final QueryExecutionModel<T, ?> model, final int pageNo, final int pageCapacity) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? model.lightweight() : model;
        return getPage(qem, pageNo, 0, pageCapacity);
    }

    @Override
    public IPage<T> getPage(final QueryExecutionModel<T, ?> model, final int pageNo, final int pageCount, final int pageCapacity) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? model.lightweight() : model;
        
        final Pair<Integer, Integer> numberOfPagesAndCount = pageCount > 0 ? Pair.pair(pageCount, pageCount * pageCapacity) : evalNumOfPages(qem.getQueryModel(), qem.getParamValues(), pageCapacity);

        final int pageNumber = pageNo < 0 ? numberOfPagesAndCount.getKey() - 1 : pageNo;
        return new EntityQueryPage(qem, pageNumber, pageCapacity, numberOfPagesAndCount);
    }

    @Override
    public IPage<T> getPage(final int pageNo, final int pageCapacity) {
        final Pair<Integer, Integer> numberOfPagesAndCount = evalNumOfPages(getDefaultQueryExecutionModel().getQueryModel(), Collections.<String, Object> emptyMap(), pageCapacity);
        final int pageNumber = pageNo < 0 ? numberOfPagesAndCount.getKey() - 1 : pageNo;
        return new EntityQueryPage(getDefaultQueryExecutionModel(), pageNumber, pageCapacity, numberOfPagesAndCount);
    }

    @Override
    public T getEntity(final QueryExecutionModel<T, ?> model) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? model.lightweight() : model;
        final List<T> data;
        try(final Stream<T> stream = stream(qem, 2)) {
            data = stream.limit(2).collect(toList());
        }
        if (data.size() > 1) {
            throw new UnexpectedNumberOfReturnedEntities(format("The provided query model leads to retrieval of more than one entity (%s).", data.size()));
        }
        return data.size() == 1 ? data.get(0) : null;
    }

    /**
     * Calculates the number of pages of the given size required to fit the whole result set.
     *
     *
     * @param model
     * @param pageCapacity
     * @return
     */
    @Override
    public Pair<Integer, Integer> evalNumOfPages(final QueryModel<T> model, final Map<String, Object> paramValues, final int pageCapacity) {
        final AggregatedResultQueryModel countQuery = model instanceof EntityResultQueryModel ? select((EntityResultQueryModel<T>) model).yield().countAll().as("count").modelAsAggregate()
                : select((AggregatedResultQueryModel) model).yield().countAll().as("count").modelAsAggregate();
        final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> countModel = from(countQuery).with(paramValues).with(fetchAggregates().with("count")).lightweight().model();

        final List<EntityAggregates> counts = new EntityFetcher(newQueryExecutionContext()).getEntities(countModel);

        final int resultSize = ((Number) counts.get(0).get("count")).intValue();

        final Integer pageSize = resultSize % pageCapacity == 0 ? resultSize / pageCapacity : resultSize / pageCapacity + 1;

        return Pair.pair(pageSize, resultSize);
    }

    /**
     * A private helper method.
     * 
     * @param id
     * @param fetchModel
     * @return
     */
    private T fetchOneEntityInstance(final Long id, final fetch<T> fetchModel) {
        try {
            final EntityResultQueryModel<T> query = select(getEntityType()).where().prop(AbstractEntity.ID).eq().val(id).model();
            query.setFilterable(filterable);
            return getEntity(instrumented() ? from(query).with(fetchModel).model(): from(query).with(fetchModel).lightweight().model());
        } catch (final Exception e) {
            throw new EntityCompanionException(format("Could not fetch one entity of type [%s].", getEntityType().getName()), e);
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
            this(queryModel, summaryModel != null && numberOfPagesAndCount.getValue() > 0 ? calcSummary(summaryModel) : null, pageNumber, pageCapacity, numberOfPagesAndCount);
        }

        public EntityQueryPage(final QueryExecutionModel<T, ?> queryModel, final T summary, final int pageNumber, final int pageCapacity, final Pair<Integer, Integer> numberOfPagesAndCount) {
            this.numberOfPagesAndCount = numberOfPagesAndCount;
            this.pageNumber = pageNumber;
            this.pageCapacity = pageCapacity;
            this.numberOfPages = numberOfPagesAndCount.getKey() == 0 ? 1 : numberOfPagesAndCount.getKey();
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
    
    /**
     * Calculates summary based on the assumption that <code>model</code> represents summary model for the type T.
     */
    private T calcSummary(final QueryExecutionModel<T, ?> model) {
        final List<T> list = stream(model).collect(toList());
        return list.size() == 1 ? list.get(0) : null;
    }

    /**
     * Fetches the results of the specified page based on the request of the given instance of {@link QueryExecutionModel}.
     *
     * @param queryModel
     * @param pageNumber
     * @param pageCapacity
     * @return
     */
    @Override
    public List<T> getEntitiesOnPage(final QueryExecutionModel<T, ?> queryModel, final Integer pageNumber, final Integer pageCapacity) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? queryModel.lightweight() : queryModel;
        return new EntityFetcher(newQueryExecutionContext()).getEntitiesOnPage(qem, pageNumber, pageCapacity);
    }
    
    /**
     * A helper method to create new instances of {@link QueryExecutionContext}.
     * @return
     */
    private QueryExecutionContext newQueryExecutionContext() {
        return new QueryExecutionContext(
                session.get(), 
                entityFactory.get(), 
                coFinder.get(), 
                domainMetadata.get(), 
                filter.get(), 
                username.get(), 
                universalConstants.get(), 
                idOnlyProxiedEntityTypeCache.get());
    }

    protected QueryExecutionModel<T, EntityResultQueryModel<T>> produceDefaultQueryExecutionModel(final Class<T> entityType) {
        final EntityResultQueryModel<T> query = select(entityType).model();
        query.setFilterable(filterable);
        final OrderingModel orderBy = orderBy().prop(AbstractEntity.ID).asc().model();
        return instrumented() ? from(query).with(orderBy).model() : from(query).with(orderBy).lightweight().model();
    }

    protected QueryExecutionModel<T, EntityResultQueryModel<T>> getDefaultQueryExecutionModel() {
        return produceDefaultQueryExecutionModel(entityType);
    }

}
