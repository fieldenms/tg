package ua.com.fielden.platform.eql.retrieval;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.retrieval.EntityHibernateRetrievalQueryProducer.mkQueryProducerWithPagination;
import static ua.com.fielden.platform.eql.retrieval.EntityHibernateRetrievalQueryProducer.mkQueryProducerWithoutPagination;
import static ua.com.fielden.platform.eql.retrieval.EntityResultTreeBuilder.build;
import static ua.com.fielden.platform.eql.stage3.EqlQueryTransformer.transform;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.EntityContainer;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.QueryExecutionContext;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.entity.query.stream.ScrollableResultStream;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage3.etc.Yield3;
import ua.com.fielden.platform.eql.stage3.etc.Yields3;
import ua.com.fielden.platform.eql.stage3.operands.queries.ResultQuery3;
import ua.com.fielden.platform.streaming.SequentialGroupingStream;

public class EntityContainerFetcher {
    private final QueryExecutionContext executionContext;
    private final Logger logger = getLogger(this.getClass());
    
    public EntityContainerFetcher(final QueryExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public <E extends AbstractEntity<?>> List<EntityContainer<E>> listAndEnhanceContainers(final QueryProcessingModel<E, ?> queryModel, final Integer pageNumber, final Integer pageCapacity) {
        final DomainMetadataAnalyser domainMetadataAnalyser = new DomainMetadataAnalyser(executionContext.getDomainMetadata());
        final QueryModelResult<E> modelResult = getModelResult(queryModel, domainMetadataAnalyser.getDbVersion(), executionContext.getFilter(), executionContext.getUsername());

        if (modelResult.idOnlyQuery()) {
            return listContainersForIdOnlyQuery(queryModel, modelResult.resultType, pageNumber, pageCapacity);
        }

        final List<EntityContainer<E>> result = listContainersAsIs(modelResult, pageNumber, pageCapacity);
        // logger.debug("Fetch model:\n" + modelResult.getFetchModel());
        return new EntityContainerEnhancer<E>(this, domainMetadataAnalyser, executionContext.getIdOnlyProxiedEntityTypeCache()).enhance(result, modelResult.fetchModel, queryModel.getParamValues());
    }

    public <E extends AbstractEntity<?>> Stream<List<EntityContainer<E>>> streamAndEnhanceContainers(final QueryProcessingModel<E, ?> queryModel, final Optional<Integer> fetchSize) {
        final DomainMetadataAnalyser domainMetadataAnalyser = new DomainMetadataAnalyser(executionContext.getDomainMetadata());
        final QueryModelResult<E> modelResult = getModelResult(queryModel, domainMetadataAnalyser.getDbVersion(), executionContext.getFilter(), executionContext.getUsername());

        if (modelResult.idOnlyQuery()) {
            return streamContainersForIdOnlyQuery(queryModel, modelResult.resultType, fetchSize);
        }

        final Stream<List<EntityContainer<E>>> stream = streamContainersAsIs(modelResult, fetchSize);
        // logger.debug("Fetch model:\n" + modelResult.getFetchModel());

        final EntityContainerEnhancer<E> entityContainerEnhancer = new EntityContainerEnhancer<>(this, domainMetadataAnalyser, executionContext.getIdOnlyProxiedEntityTypeCache());

        return stream.map(container -> entityContainerEnhancer.enhance(container, modelResult.fetchModel, queryModel.getParamValues()));
    }

    private <E extends AbstractEntity<?>> List<EntityContainer<E>> listContainersForIdOnlyQuery(final QueryProcessingModel<E, ?> queryModel, final Class<E> resultType, final Integer pageNumber, final Integer pageCapacity) {
        final EntityResultQueryModel<E> idOnlyModel = select(resultType).where().prop("id").in().model((SingleResultQueryModel<?>) queryModel.queryModel).model();

        final QueryProcessingModel<E, EntityResultQueryModel<E>> idOnlyQpm = new QueryProcessingModel<>(idOnlyModel, queryModel.orderModel, queryModel.fetchModel, queryModel.getParamValues(), queryModel.lightweight);

        return listAndEnhanceContainers(idOnlyQpm, pageNumber, pageCapacity);
    }

    private <E extends AbstractEntity<?>> List<EntityContainer<E>> listContainersAsIs(final QueryModelResult<E> modelResult, final Integer pageNumber, final Integer pageCapacity) {
        final EntityTree<E> resultTree = build(modelResult.resultType, modelResult.getSortedYieldedPropsInfo(), executionContext.getDomainMetadata().eqlDomainMetadata);
        final EntityHibernateRetrievalQueryProducer queryProducer = mkQueryProducerWithPagination(modelResult.sql, resultTree.getSortedScalars(), modelResult.getParamValues(), pageNumber, pageCapacity);

        final Query query = queryProducer.produceHibernateQuery(executionContext.getSession());

        final EntityRawResultConverter<E> entityRawResultConverter = new EntityRawResultConverter<>(executionContext.getEntityFactory());

        // let's execute the query and time the duration
        final DateTime st = new DateTime();
        final List<?> res = query.list();
        final Period pd = new Period(st, new DateTime());
        logger.debug(format("Query exec duration: %s m %s s %s ms for type [%s].", pd.getMinutes(), pd.getSeconds(), pd.getMillis(), modelResult.resultType.getSimpleName()));
        
        return entityRawResultConverter.transformFromNativeResult(resultTree, res);
    }

    private <E extends AbstractEntity<?>> Stream<List<EntityContainer<E>>> streamContainersForIdOnlyQuery(final QueryProcessingModel<E, ?> queryModel, final Class<E> resultType, final Optional<Integer> fetchSize) {
        final EntityResultQueryModel<E> idOnlyModel = select(resultType).where().prop("id").in().model((SingleResultQueryModel<?>) queryModel.queryModel).model();

        final QueryProcessingModel<E, EntityResultQueryModel<E>> idOnlyQpm = new QueryProcessingModel<>(idOnlyModel, queryModel.orderModel, queryModel.fetchModel, queryModel.getParamValues(), queryModel.lightweight);

        return streamAndEnhanceContainers(idOnlyQpm, fetchSize);
    }

    private <E extends AbstractEntity<?>> Stream<List<EntityContainer<E>>> streamContainersAsIs(final QueryModelResult<E> modelResult, final Optional<Integer> fetchSize) {
        final EntityTree<E> resultTree = build(modelResult.resultType, modelResult.getSortedYieldedPropsInfo(), executionContext.getDomainMetadata().eqlDomainMetadata);
        final EntityHibernateRetrievalQueryProducer queryProducer = mkQueryProducerWithoutPagination(modelResult.sql, resultTree.getSortedScalars(), modelResult.getParamValues());
        final int batchSize = fetchSize.orElse(100);
        final Query query = queryProducer //
                .produceHibernateQuery(executionContext.getSession()) //
                .setFetchSize(batchSize);
        final Stream<Object[]> stream = ScrollableResultStream.streamOf(query.scroll(ScrollMode.FORWARD_ONLY));

        final EntityRawResultConverter<E> entityRawResultConverter = new EntityRawResultConverter<>(executionContext.getEntityFactory());

        return SequentialGroupingStream.stream(stream, (el, group) -> group.size() < batchSize, Optional.of(batchSize)) //
                .map(group -> entityRawResultConverter.transformFromNativeResult(resultTree, group));
    }

    private <E extends AbstractEntity<?>> QueryModelResult<E> getModelResult(final QueryProcessingModel<E, ?> qem, final DbVersion dbVersion, final IFilter filter, final String username) {
        final TransformationResult2<ResultQuery3> tr = transform(qem, filter, username, executionContext.dates(), executionContext.getDomainMetadata().eqlDomainMetadata);
        final ResultQuery3 entQuery3 = tr.item;
        final String sql = entQuery3.sql(dbVersion);
        return new QueryModelResult<>((Class<E>) entQuery3.resultType, sql, getResultPropsInfos(entQuery3.yields), tr.updatedContext.getParamValues(), qem.fetchModel);
    }

    public static SortedSet<ResultQueryYieldDetails> getResultPropsInfos(final Yields3 model) {
        final SortedSet<ResultQueryYieldDetails> result = new TreeSet<>();
        for (final Yield3 yield : model.getYields()) {
            final Class<?> yieldType = yield.type != null ? yield.type : yield.operand.type();
            final Object yieldHibType = yield.hibType != null ? yield.hibType : yield.operand.hibType();
            result.add(new ResultQueryYieldDetails(yield.alias, yieldType, yieldHibType, yield.column));
        }
        return result;
    }
}
