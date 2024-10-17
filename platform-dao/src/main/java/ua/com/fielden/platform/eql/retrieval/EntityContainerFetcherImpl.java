package ua.com.fielden.platform.eql.retrieval;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.Period;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.EntityContainer;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.entity.query.stream.ScrollableResultStream;
import ua.com.fielden.platform.eql.meta.EqlTables;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.retrieval.records.EntityTree;
import ua.com.fielden.platform.eql.retrieval.records.QueryModelResult;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.streaming.SequentialGroupingStream;
import ua.com.fielden.platform.utils.IDates;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.retrieval.EntityHibernateRetrievalQueryProducer.produceQueryWithPagination;
import static ua.com.fielden.platform.eql.retrieval.EntityHibernateRetrievalQueryProducer.produceQueryWithoutPagination;
import static ua.com.fielden.platform.eql.retrieval.EntityResultTreeBuilder.build;
import static ua.com.fielden.platform.eql.retrieval.HibernateScalarsExtractor.getSortedScalars;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

@Singleton
final class EntityContainerFetcherImpl implements IEntityContainerFetcher {

    private final Logger logger = getLogger(this.getClass());

    private final IDomainMetadata domainMetadata;
    private final IDbVersionProvider dbVersionProvider;
    private final EqlTables eqlTables;
    private final QuerySourceInfoProvider querySourceInfoProvider;
    private final IFilter filter;
    private final IUserProvider userProvider;
    private final IDates dates;
    private final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache;
    private final EntityFactory entityFactory;

    @Inject
    EntityContainerFetcherImpl(
            final IDomainMetadata domainMetadata,
            final IDbVersionProvider dbVersionProvider,
            final EqlTables eqlTables,
            final QuerySourceInfoProvider querySourceInfoProvider,
            final IFilter filter,
            final IUserProvider userProvider,
            final IDates dates,
            final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache,
            final EntityFactory entityFactory)
    {
        this.domainMetadata = domainMetadata;
        this.dbVersionProvider = dbVersionProvider;
        this.eqlTables = eqlTables;
        this.querySourceInfoProvider = querySourceInfoProvider;
        this.filter = filter;
        this.userProvider = userProvider;
        this.dates = dates;
        this.idOnlyProxiedEntityTypeCache = idOnlyProxiedEntityTypeCache;
        this.entityFactory = entityFactory;
    }

    @Override
    public <E extends AbstractEntity<?>> List<EntityContainer<E>> listAndEnhanceContainers(
            final Session session,
            final QueryProcessingModel<E, ?> queryModel,
            final Integer pageNumber,
            final Integer pageCapacity)
    {
        final var modelResult = getModelResult(queryModel);
        final List<EntityContainer<E>> result = listContainersAsIs(session, modelResult, pageNumber, pageCapacity);
        // logger.debug("Fetch model:\n" + modelResult.getFetchModel());
        return new EntityContainerEnhancer(this, domainMetadata, idOnlyProxiedEntityTypeCache)
                .enhance(session, result, modelResult.fetchModel(), queryModel.getParamValues());
    }

    @Override
    public <E extends AbstractEntity<?>> Stream<List<EntityContainer<E>>> streamAndEnhanceContainers(
            final Session session,
            final QueryProcessingModel<E, ?> queryModel,
            final Optional<Integer> fetchSize)
    {
        final var modelResult = getModelResult(queryModel);
        final Stream<List<EntityContainer<E>>> stream = streamContainersAsIs(session, modelResult, fetchSize);
        // logger.debug("Fetch model:\n" + modelResult.getFetchModel());

        final EntityContainerEnhancer entityContainerEnhancer = new EntityContainerEnhancer(this, domainMetadata, idOnlyProxiedEntityTypeCache);

        return stream.map(container -> entityContainerEnhancer.enhance(session, container, modelResult.fetchModel(), queryModel.getParamValues()));
    }

    private <E extends AbstractEntity<?>> QueryModelResult<E> getModelResult(final QueryProcessingModel<E, ?> qpm) {
        class $ {
        static boolean isIdOnlyQuery(final QueryModelResult<?> queryModelResult) {
            return isPersistedEntityType(queryModelResult.resultType()) && queryModelResult.yieldedColumns().size() == 1 && ID.equals(queryModelResult.yieldedColumns().get(0).name())
                   // This condition prevents the recursive call below from recursing further (which would never terminate),
                   // but it's unclear why this particular condition was chosen. Effectively, it's true only for such QPMs
                   // that have a fetch model wider than their yields (i.e., a sole ID yield).
                   && !(queryModelResult.fetchModel().getPrimProps().size() == 1 && queryModelResult.fetchModel().getPrimProps().contains(ID) &&
                        queryModelResult.fetchModel().getRetrievalModels().isEmpty());
        }
        }

        final QueryModelResult<E> modelResult = EqlQueryTransformer.getModelResult(
                qpm, dbVersionProvider.dbVersion(), filter, userProvider.getUsername(), dates, domainMetadata,
                eqlTables, querySourceInfoProvider);

        // TODO: This piece of code is supposedly responsible for "re-fetching the whole entity by ID in order to be able to enhance it",
        //       but its effect and purpose is not understood yet.
        //       See Issue #1991.
        if ($.isIdOnlyQuery(modelResult)) {
            final var idOnlyQuery = select(modelResult.resultType())
                    .where().prop(ID).in().model((SingleResultQueryModel<?>) qpm.queryModel)
                    .model();
            final var idOnlyQpm = new QueryProcessingModel<>(idOnlyQuery, qpm.orderModel, qpm.fetchModel, qpm.getParamValues(), qpm.lightweight);
            return getModelResult(idOnlyQpm);
        } else {
            return modelResult;
        }
    }

    private <E extends AbstractEntity<?>> List<EntityContainer<E>> listContainersAsIs(
            final Session session,
            final QueryModelResult<E> modelResult,
            final Integer pageNumber,
            final Integer pageCapacity)
    {
        final EntityTree<E> resultTree = build(modelResult.resultType(), modelResult.yieldedColumns(), querySourceInfoProvider);

        final Query query = produceQueryWithPagination(session, modelResult.sql(), getSortedScalars(resultTree), modelResult.paramValues(), pageNumber, pageCapacity, dbVersionProvider.dbVersion());

        final EntityRawResultConverter<E> entityRawResultConverter = new EntityRawResultConverter<>(entityFactory);

        // let's execute the query and time the duration
        final DateTime st = new DateTime();
        final List<?> res = query.list();
        final Period pd = new Period(st, new DateTime());
        logger.debug(format("Query exec duration: %s m %s s %s ms for type [%s].", pd.getMinutes(), pd.getSeconds(), pd.getMillis(), modelResult.resultType().getSimpleName()));

        return entityRawResultConverter.transformFromNativeResult(resultTree, res);
    }

    private <E extends AbstractEntity<?>> Stream<List<EntityContainer<E>>> streamContainersAsIs(
            final Session session,
            final QueryModelResult<E> modelResult,
            final Optional<Integer> fetchSize)
    {
        final EntityTree<E> resultTree = build(modelResult.resultType(), modelResult.yieldedColumns(), querySourceInfoProvider);
        final int batchSize = fetchSize.orElse(100);
        final Query query = produceQueryWithoutPagination(session, modelResult.sql(), getSortedScalars(resultTree), modelResult.paramValues(), dbVersionProvider.dbVersion())
                .setFetchSize(batchSize);
        final Stream<Object[]> stream = ScrollableResultStream.streamOf(query.scroll(ScrollMode.FORWARD_ONLY));

        final EntityRawResultConverter<E> entityRawResultConverter = new EntityRawResultConverter<>(entityFactory);

        return SequentialGroupingStream.stream(stream, (el, group) -> group.size() < batchSize, Optional.of(batchSize)) //
                .map(group -> entityRawResultConverter.transformFromNativeResult(resultTree, group));
    }

}
