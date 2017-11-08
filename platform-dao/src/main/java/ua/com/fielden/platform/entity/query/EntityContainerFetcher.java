package ua.com.fielden.platform.entity.query;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.entity.query.stream.ScrollableResultStream;
import ua.com.fielden.platform.streaming.SequentialGroupingStream;

public class EntityContainerFetcher {
    private final QueryExecutionContext executionContext;    
    private final Logger logger = Logger.getLogger(this.getClass());

    public EntityContainerFetcher(final QueryExecutionContext executionContext) {
        this.executionContext = executionContext;
    }
    
    public <E extends AbstractEntity<?>> List<EntityContainer<E>> listAndEnhanceContainers(final QueryExecutionModel<E, ?> queryModel, final Integer pageNumber, final Integer pageCapacity) {
        final DomainMetadataAnalyser domainMetadataAnalyser = new DomainMetadataAnalyser(executionContext.getDomainMetadata());
        final QueryModelResult<E> modelResult = getModelResult(queryModel, domainMetadataAnalyser, executionContext.getFilter(), executionContext.getUsername());

        if (modelResult.idOnlyQuery()) {
            return listContainersForIdOnlyQuery(queryModel, modelResult.getResultType(), pageNumber, pageCapacity);
        }
        
        final List<EntityContainer<E>> result = listContainersAsIs(modelResult, pageNumber, pageCapacity);
        logger.debug("Fetch model:\n" + modelResult.getFetchModel());
        return new EntityContainerEnhancer<E>(this, domainMetadataAnalyser, executionContext.getIdOnlyProxiedEntityTypeCache()).enhance(result, modelResult.getFetchModel());
    }
    
    public <E extends AbstractEntity<?>> Stream<List<EntityContainer<E>>> streamAndEnhanceContainers(final QueryExecutionModel<E, ?> queryModel, final Optional<Integer> fetchSize) {
        final DomainMetadataAnalyser domainMetadataAnalyser = new DomainMetadataAnalyser(executionContext.getDomainMetadata());
        final QueryModelResult<E> modelResult = getModelResult(queryModel, domainMetadataAnalyser, executionContext.getFilter(), executionContext.getUsername());

        if (modelResult.idOnlyQuery()) {
            return streamContainersForIdOnlyQuery(queryModel, modelResult.getResultType(), fetchSize);
        }
        
        final Stream<List<EntityContainer<E>>> stream = streamContainersAsIs(modelResult, fetchSize);
        logger.debug("Fetch model:\n" + modelResult.getFetchModel());
        
        final EntityContainerEnhancer<E> entityContainerEnhancer = new EntityContainerEnhancer<>(this, domainMetadataAnalyser, executionContext.getIdOnlyProxiedEntityTypeCache());
        
        return stream.map(container -> entityContainerEnhancer.enhance(container, modelResult.getFetchModel()));
    }

    
    private <E extends AbstractEntity<?>> List<EntityContainer<E>> listContainersForIdOnlyQuery(final QueryExecutionModel<E, ?> queryModel, final Class<E> resultType, final Integer pageNumber, final Integer pageCapacity) {
        final EntityResultQueryModel<E> idOnlyModel = select(resultType).where().prop("id").in().model((SingleResultQueryModel<?>) queryModel.getQueryModel()).model();
        
        final QueryExecutionModel<E,EntityResultQueryModel<E>> idOnlyQem = from(idOnlyModel)
        .with(queryModel.getOrderModel())
        .with(queryModel.getFetchModel())
        .with(queryModel.getParamValues())
        .lightweight().model();
        
        return listAndEnhanceContainers(idOnlyQem, pageNumber, pageCapacity);
    }

    private <E extends AbstractEntity<?>> List<EntityContainer<E>> listContainersAsIs(final QueryModelResult<E> modelResult, final Integer pageNumber, final Integer pageCapacity) {
        final EntityTree<E> resultTree = new EntityResultTreeBuilder().buildEntityTree(modelResult.getResultType(), modelResult.getYieldedPropsInfo());

        final EntityHibernateRetrievalQueryProducer queryProducer = EntityHibernateRetrievalQueryProducer.mkQueryProducerWithPagination(modelResult.getSql(), resultTree.getScalarFromEntityTree(), modelResult.getParamValues(), pageNumber, pageCapacity);
        
        final Query query = queryProducer.produceHibernateQuery(executionContext.getSession());

        final EntityRawResultConverter<E> entityRawResultConverter = new EntityRawResultConverter<>(executionContext.getEntityFactory());

        // let's execute the query and time the duration
        final DateTime st = new DateTime();
        final List<?> res = query.list();
        final Period pd = new Period(st, new DateTime());
        logger.info(format("Query exec duration: %s m %s s %s ms for type [%s].", pd.getMinutes(), pd.getSeconds(), pd.getMillis(), modelResult.getResultType().getSimpleName()));
        return entityRawResultConverter.transformFromNativeResult(resultTree, res);
    }

    private <E extends AbstractEntity<?>> Stream<List<EntityContainer<E>>> streamContainersForIdOnlyQuery(final QueryExecutionModel<E, ?> queryModel, final Class<E> resultType, final Optional<Integer> fetchSize) {
        final EntityResultQueryModel<E> idOnlyModel = select(resultType).where().prop("id").in().model((SingleResultQueryModel<?>) queryModel.getQueryModel()).model();
        
        final QueryExecutionModel<E,EntityResultQueryModel<E>> idOnlyQem = from(idOnlyModel)
        .with(queryModel.getOrderModel())
        .with(queryModel.getFetchModel())
        .with(queryModel.getParamValues())
        .lightweight().model();
        
        return streamAndEnhanceContainers(idOnlyQem, fetchSize);
    }

    
    private <E extends AbstractEntity<?>> Stream<List<EntityContainer<E>>> streamContainersAsIs(final QueryModelResult<E> modelResult, final Optional<Integer> fetchSize) {
        final EntityTree<E> resultTree = new EntityResultTreeBuilder().buildEntityTree(modelResult.getResultType(), modelResult.getYieldedPropsInfo());

        final EntityHibernateRetrievalQueryProducer queryProducer = EntityHibernateRetrievalQueryProducer.mkQueryProducerWithoutPagination(modelResult.getSql(), resultTree.getScalarFromEntityTree(), modelResult.getParamValues());
        final int batchSize = fetchSize.orElse(100);
        final Query query = queryProducer
                            .produceHibernateQuery(executionContext.getSession())            
                            .setFetchSize(batchSize);
        final Stream<Object[]> stream = ScrollableResultStream.streamOf(query.scroll(ScrollMode.FORWARD_ONLY));
        
        final EntityRawResultConverter<E> entityRawResultConverter = new EntityRawResultConverter<>(executionContext.getEntityFactory());
        
        return SequentialGroupingStream.stream(stream, (el, group) -> group.size() < batchSize, Optional.of(batchSize))
                .map(group -> entityRawResultConverter.transformFromNativeResult(resultTree, group));
    }

    private <E extends AbstractEntity<?>> QueryModelResult<E> getModelResult(final QueryExecutionModel<E, ?> qem, final DomainMetadataAnalyser domainMetadataAnalyser, final IFilter filter, final String username) {
//        final EntQueryGenerator gen = new EntQueryGenerator(domainMetadataAnalyser, filter, username, executionContext.getUniversalConstants());
//        final IRetrievalModel<E> fm = qem.getFetchModel() == null ? //
//        (qem.getQueryModel().getResultType().equals(EntityAggregates.class) ? null
//                : new EntityRetrievalModel<E>(fetch(qem.getQueryModel().getResultType()), domainMetadataAnalyser))
//                : // 
//                (qem.getQueryModel().getResultType().equals(EntityAggregates.class) ? new EntityAggregatesRetrievalModel<E>(qem.getFetchModel(), domainMetadataAnalyser)
//                        : new EntityRetrievalModel<E>(qem.getFetchModel(), domainMetadataAnalyser));
//
//        final EntQuery entQuery = gen.generateEntQueryAsResultQuery(qem.getQueryModel(), qem.getOrderModel(), qem.getQueryModel().getResultType(), fm, qem.getParamValues());
//        final String sql = entQuery.sql();
//        return new QueryModelResult<>(entQuery.type(), sql, getResultPropsInfos(entQuery.getYields()), entQuery.getValuesForSqlParams(), fm);
        throw new EqlException("Not fixed yet (awaiting EQL3 completion).");
    }

//    private SortedSet<ResultQueryYieldDetails> getResultPropsInfos(final Yields model) {
//        final SortedSet<ResultQueryYieldDetails> result = new TreeSet<>();
//        for (final Yield yield : model.getYields()) {
//            result.add(new ResultQueryYieldDetails(yield.getInfo().getName(), yield.getInfo().getJavaType(), yield.getInfo().getHibType(), yield.getInfo().getColumn(), yield.getInfo().getYieldDetailsType()));
//        }
//        return result;
//    }
}