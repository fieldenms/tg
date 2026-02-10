package ua.com.fielden.platform.eql.retrieval;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.Logger;
import org.hibernate.ScrollMode;
import org.hibernate.Session;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.EntityContainer;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.entity.query.stream.ScrollableResultStream;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.retrieval.exceptions.EntityRetrievalException;
import ua.com.fielden.platform.eql.retrieval.records.EntityTree;
import ua.com.fielden.platform.eql.retrieval.records.QueryModelResult;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.StreamUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.retrieval.EntityHibernateRetrievalQueryProducer.produceQueryWithPagination;
import static ua.com.fielden.platform.eql.retrieval.EntityHibernateRetrievalQueryProducer.produceQueryWithoutPagination;
import static ua.com.fielden.platform.eql.retrieval.EntityResultTreeBuilder.build;
import static ua.com.fielden.platform.eql.retrieval.HibernateScalarsExtractor.getSortedScalars;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentEntityType;

@Singleton
final class EntityContainerFetcherImpl implements IEntityContainerFetcher {

    private static final Logger LOGGER = getLogger();

    public static final String ERR_DURING_ENTITY_RETRIEVAL = "Error during entity retrieval.";

    private final IDomainMetadata domainMetadata;
    private final IDbVersionProvider dbVersionProvider;
    private final QuerySourceInfoProvider querySourceInfoProvider;
    private final IUserProvider userProvider;
    private final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache;
    private final EntityFactory entityFactory;
    private final EqlQueryTransformer eqlQueryTransformer;

    @Inject
    EntityContainerFetcherImpl(
            final IDomainMetadata domainMetadata,
            final IDbVersionProvider dbVersionProvider,
            final QuerySourceInfoProvider querySourceInfoProvider,
            final IUserProvider userProvider,
            final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache,
            final EntityFactory entityFactory,
            final EqlQueryTransformer eqlQueryTransformer)
    {
        this.domainMetadata = domainMetadata;
        this.dbVersionProvider = dbVersionProvider;
        this.querySourceInfoProvider = querySourceInfoProvider;
        this.userProvider = userProvider;
        this.idOnlyProxiedEntityTypeCache = idOnlyProxiedEntityTypeCache;
        this.entityFactory = entityFactory;
        this.eqlQueryTransformer = eqlQueryTransformer;
    }

    @Override
    public <E extends AbstractEntity<?>> List<EntityContainer<E>> listAndEnhanceContainers(
            final Session session,
            final QueryProcessingModel<E, ?> queryModel,
            final Integer pageNumber,
            final Integer pageCapacity)
    {
        try {
            final var modelResult = getModelResult(queryModel);
            final List<EntityContainer<E>> result = listContainersAsIs(session, modelResult, pageNumber, pageCapacity);
            return new EntityContainerEnhancer(this, domainMetadata, idOnlyProxiedEntityTypeCache)
                    .enhance(session, result, modelResult.fetchModel(), queryModel.getParamValues());
        } catch (final Exception ex) {
            final var exception = ex instanceof EntityRetrievalException it ? it : new EntityRetrievalException(ERR_DURING_ENTITY_RETRIEVAL, ex);
            LOGGER.error(() -> "%s\nQuery: %s".formatted(exception.getMessage(), queryModel), exception);
            throw exception;
        }
    }

    @Override
    public <E extends AbstractEntity<?>> Stream<List<EntityContainer<E>>> streamAndEnhanceContainers(
            final Session session,
            final QueryProcessingModel<E, ?> queryModel,
            final Optional<Integer> fetchSize)
    {
        try {
            final var modelResult = getModelResult(queryModel);
            final Stream<List<EntityContainer<E>>> stream = streamContainersAsIs(session, modelResult, fetchSize);
            final var entityContainerEnhancer = new EntityContainerEnhancer(this, domainMetadata, idOnlyProxiedEntityTypeCache);
            return stream.map(containers -> entityContainerEnhancer.enhance(session, containers, modelResult.fetchModel(), queryModel.getParamValues()));
        } catch (final Exception ex) {
            final var exception = ex instanceof EntityRetrievalException it ? it : new EntityRetrievalException(ERR_DURING_ENTITY_RETRIEVAL, ex);
            LOGGER.error(() -> "%s\nQuery: %s".formatted(exception.getMessage(), queryModel), exception);
            throw exception;
        }
    }

    private <E extends AbstractEntity<?>> QueryModelResult<E> getModelResult(final QueryProcessingModel<E, ?> qpm) {
        class $ {
            static boolean isForeignIdOnlyQuery(final QueryModelResult<?> queryModelResult) {
                return isPersistentEntityType(queryModelResult.resultType())
                       && queryModelResult.yieldedColumns().size() == 1
                       && ID.equals(queryModelResult.yieldedColumns().getFirst().name())
                       // The type of ID can be either an entity or Long.
                       // If it is an entity type, this is a foreign id-only query (a whole entity is being yielded).
                       // Otherwise, ID is yielded as a number (a local id-only query).
                       && isEntityType(queryModelResult.yieldedColumns().getFirst().propType().javaType());
            }
        }

        final QueryModelResult<E> modelResult = eqlQueryTransformer.getModelResult(qpm, userProvider.getUsername());

        // Foreign id-only queries should be wrapped to access the whole graph of the entities being yielded.
        // This does not apply to entity aggregates where IDs might be yielded – they are treated as numbers.
        if ($.isForeignIdOnlyQuery(modelResult)) {
            final var idOnlyQuery = select(modelResult.resultType())
                    .where().prop(ID).in().model((SingleResultQueryModel<?>) qpm.queryModel)
                    .model();
            final var idOnlyQpm = new QueryProcessingModel<>(idOnlyQuery, qpm.orderModel, qpm.fetchModel, qpm.getParamValues(), qpm.lightweight);
            return eqlQueryTransformer.getModelResult(idOnlyQpm, userProvider.getUsername());
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
        final var query = produceQueryWithPagination(session, modelResult.sql(), getSortedScalars(resultTree), modelResult.paramValues(), pageNumber, pageCapacity, dbVersionProvider.dbVersion());
        final var entityRawResultConverter = new EntityRawResultConverter<E>(entityFactory);

        // Uncomment to time the duration.
        // final DateTime st = new DateTime();
        final List<?> res = query.list();
        // final Period pd = new Period(st, new DateTime());
        // logger.debug(format("Query exec duration: %s m %s s %s ms for type [%s].", pd.getMinutes(), pd.getSeconds(), pd.getMillis(), modelResult.resultType().getSimpleName()));

        return entityRawResultConverter.transformFromNativeResult(resultTree, res);
    }

    private <E extends AbstractEntity<?>> Stream<List<EntityContainer<E>>> streamContainersAsIs(
            final Session session,
            final QueryModelResult<E> modelResult,
            final Optional<Integer> fetchSize)
    {
        final EntityTree<E> resultTree = build(modelResult.resultType(), modelResult.yieldedColumns(), querySourceInfoProvider);
        final int batchSize = fetchSize.orElse(100);
        final var query = produceQueryWithoutPagination(session, modelResult.sql(), getSortedScalars(resultTree), modelResult.paramValues(), dbVersionProvider.dbVersion())
                          .setFetchSize(batchSize);
        final Stream<Object[]> stream = ScrollableResultStream.streamOf(query.scroll(ScrollMode.FORWARD_ONLY));

        final var entityRawResultConverter = new EntityRawResultConverter<E>(entityFactory);

        return StreamUtils.windowed(stream, batchSize)
                .map(group -> entityRawResultConverter.transformFromNativeResult(resultTree, group));
    }

}
