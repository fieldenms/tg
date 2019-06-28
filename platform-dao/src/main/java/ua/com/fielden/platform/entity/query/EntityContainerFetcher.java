package ua.com.fielden.platform.entity.query;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.generation.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails;
import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails.YieldDetailsType;
import ua.com.fielden.platform.entity.query.generation.elements.Yield;
import ua.com.fielden.platform.entity.query.generation.elements.Yields;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.entity.query.stream.ScrollableResultStream;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.meta.MetadataGenerator;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage3.elements.Column;
import ua.com.fielden.platform.eql.stage3.elements.Table;
import ua.com.fielden.platform.eql.stage3.elements.Yield3;
import ua.com.fielden.platform.eql.stage3.elements.Yields3;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntQuery3;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.streaming.SequentialGroupingStream;

public class EntityContainerFetcher {
    private final QueryExecutionContext executionContext;    
    private final Logger logger = Logger.getLogger(this.getClass());

    public EntityContainerFetcher(final QueryExecutionContext executionContext) {
        this.executionContext = executionContext;
    }
    
    public <E extends AbstractEntity<?>> List<EntityContainer<E>> listAndEnhanceContainers(final QueryProcessingModel<E, ?> queryModel, final Integer pageNumber, final Integer pageCapacity) {
        final DomainMetadataAnalyser domainMetadataAnalyser = new DomainMetadataAnalyser(executionContext.getDomainMetadata());
        final QueryModelResult<E> modelResult = getModelResult(queryModel, domainMetadataAnalyser, executionContext.getFilter(), executionContext.getUsername());

        if (modelResult.idOnlyQuery()) {
            return listContainersForIdOnlyQuery(queryModel, modelResult.getResultType(), pageNumber, pageCapacity);
        }
        
        final List<EntityContainer<E>> result = listContainersAsIs(modelResult, pageNumber, pageCapacity);
        logger.debug("Fetch model:\n" + modelResult.getFetchModel());
        return new EntityContainerEnhancer<E>(this, domainMetadataAnalyser, executionContext.getIdOnlyProxiedEntityTypeCache()).enhance(result, modelResult.getFetchModel());
    }
    
    public <E extends AbstractEntity<?>> Stream<List<EntityContainer<E>>> streamAndEnhanceContainers(final QueryProcessingModel<E, ?> queryModel, final Optional<Integer> fetchSize) {
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

    
    private <E extends AbstractEntity<?>> List<EntityContainer<E>> listContainersForIdOnlyQuery(final QueryProcessingModel<E, ?> queryModel, final Class<E> resultType, final Integer pageNumber, final Integer pageCapacity) {
        final EntityResultQueryModel<E> idOnlyModel = select(resultType).where().prop("id").in().model((SingleResultQueryModel<?>) queryModel.queryModel).model();
        
        final QueryProcessingModel<E,EntityResultQueryModel<E>> idOnlyQpm = new QueryProcessingModel<>(idOnlyModel, queryModel.orderModel, queryModel.fetchModel, queryModel.paramValues, queryModel.lightweight);
        
        return listAndEnhanceContainers(idOnlyQpm, pageNumber, pageCapacity);
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

    private <E extends AbstractEntity<?>> Stream<List<EntityContainer<E>>> streamContainersForIdOnlyQuery(final QueryProcessingModel<E, ?> queryModel, final Class<E> resultType, final Optional<Integer> fetchSize) {
        final EntityResultQueryModel<E> idOnlyModel = select(resultType).where().prop("id").in().model((SingleResultQueryModel<?>) queryModel.queryModel).model();
        
        final QueryProcessingModel<E,EntityResultQueryModel<E>> idOnlyQpm = new QueryProcessingModel<>(idOnlyModel, queryModel.orderModel, queryModel.fetchModel, queryModel.paramValues, queryModel.lightweight);
        
        return streamAndEnhanceContainers(idOnlyQpm, fetchSize);
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

    private <E extends AbstractEntity<?>> QueryModelResult<E> getModelResult(final QueryProcessingModel<E, ?> qem, final DomainMetadataAnalyser domainMetadataAnalyser, final IFilter filter, final String username) {
        if (qem.paramValues.containsKey("EQL3")) {
            final ua.com.fielden.platform.eql.stage1.builders.EntQueryGenerator gen1 = new ua.com.fielden.platform.eql.stage1.builders.EntQueryGenerator();
            Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo = null;
            
            try {
                domainInfo = new MetadataGenerator(null).generate(executionContext.getDomainMetadata().getPersistedEntityMetadataMap().keySet());
            } catch (final Exception e) {
                e.printStackTrace();
            }
            
            final PropsResolutionContext resolutionContext = new PropsResolutionContext(domainInfo );
            final Map<String, Table> tables = new HashMap<>();
            final Map<String, Column> columns = new HashMap<>();
            columns.put("id", new Column("_ID"));
            columns.put("key", new Column("KEY_"));
            columns.put("replacedBy", new Column("REPLACEDBY_"));
            final Table vehT = new Table("TGVEHICLE_", columns);
            tables.put(TgVehicle.class.getName(), vehT);
            final EntQuery3 entQuery3 = gen1.generateEntQueryAsResultQuery(qem.queryModel, qem.orderModel).transform(resolutionContext).item.transform(new TransformationContext(tables)).item;
            final String sql3 = entQuery3.sql();
            return new QueryModelResult<>((Class<E>)EntityAggregates.class, sql3, getResultPropsInfos(entQuery3.yields), Collections.<String, Object>emptyMap(), qem.fetchModel);
            
        } else {
            final EntQueryGenerator gen = new EntQueryGenerator(domainMetadataAnalyser, filter, username, executionContext.getUniversalConstants());
            final EntQuery entQuery = gen.generateEntQueryAsResultQuery(qem.queryModel, qem.orderModel, qem.queryModel.getResultType(), qem.fetchModel, qem.paramValues);
            final String sql = entQuery.sql();
            return new QueryModelResult<>((Class<E>)entQuery.type(), sql, getResultPropsInfos(entQuery.getYields()), entQuery.getValuesForSqlParams(), qem.fetchModel);
        }
    }

    private SortedSet<ResultQueryYieldDetails> getResultPropsInfos(final Yields model) {
        final SortedSet<ResultQueryYieldDetails> result = new TreeSet<>();
        for (final Yield yield : model.getYields()) {
            result.add(new ResultQueryYieldDetails(yield.getInfo().getName(), yield.getInfo().getJavaType(), yield.getInfo().getHibType(), yield.getInfo().getColumn(), yield.getInfo().getYieldDetailsType()));
        }
        return result;
    }
    
    private SortedSet<ResultQueryYieldDetails> getResultPropsInfos(final Yields3 model) {
        final SortedSet<ResultQueryYieldDetails> result = new TreeSet<>();
        for (final Yield3 yield : model.getYields()) {
            result.add(new ResultQueryYieldDetails(yield.alias, null, null, yield.alias, YieldDetailsType.USUAL_PROP));
        }
        return result;
    }
}