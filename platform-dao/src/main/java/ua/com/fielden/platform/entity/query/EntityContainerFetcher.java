package ua.com.fielden.platform.entity.query;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails.YieldDetailsType.COMPOSITE_TYPE_HEADER;
import static ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails.YieldDetailsType.UNION_ENTITY_HEADER;
import static ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails.YieldDetailsType.USUAL_PROP;
import static ua.com.fielden.platform.eql.stage2.elements.PathsToTreeTransformator.groupChildren;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

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
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails;
import ua.com.fielden.platform.entity.query.generation.elements.Yield;
import ua.com.fielden.platform.entity.query.generation.elements.Yields;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.entity.query.stream.ScrollableResultStream;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.meta.ShortMetadata;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.elements.sources.ChildGroup;
import ua.com.fielden.platform.eql.stage3.elements.Yield3;
import ua.com.fielden.platform.eql.stage3.elements.Yields3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ResultQuery3;
import ua.com.fielden.platform.streaming.SequentialGroupingStream;

public class EntityContainerFetcher {
    private final QueryExecutionContext executionContext;    
    private final Logger logger = Logger.getLogger(this.getClass());
    public static Long dur = 0l;
    public static Long dur1 = 0l;
    public static Long dur2 = 0l;
    public static Long dur3 = 0l;
    public static Long dur4 = 0l;
    public static Long dur5 = 0l;
    public static Long dur6 = 0l;
    public static Long count = 0l;

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
        // logger.debug("Fetch model:\n" + modelResult.getFetchModel());
        return new EntityContainerEnhancer<E>(this, domainMetadataAnalyser, executionContext.getIdOnlyProxiedEntityTypeCache()).enhance(result, modelResult.getFetchModel(), queryModel.getParamValues());
    }
    
    public <E extends AbstractEntity<?>> Stream<List<EntityContainer<E>>> streamAndEnhanceContainers(final QueryProcessingModel<E, ?> queryModel, final Optional<Integer> fetchSize) {
        final DomainMetadataAnalyser domainMetadataAnalyser = new DomainMetadataAnalyser(executionContext.getDomainMetadata());
        final QueryModelResult<E> modelResult = getModelResult(queryModel, domainMetadataAnalyser, executionContext.getFilter(), executionContext.getUsername());

        if (modelResult.idOnlyQuery()) {
            return streamContainersForIdOnlyQuery(queryModel, modelResult.getResultType(), fetchSize);
        }
        
        final Stream<List<EntityContainer<E>>> stream = streamContainersAsIs(modelResult, fetchSize);
        // logger.debug("Fetch model:\n" + modelResult.getFetchModel());
        
        final EntityContainerEnhancer<E> entityContainerEnhancer = new EntityContainerEnhancer<>(this, domainMetadataAnalyser, executionContext.getIdOnlyProxiedEntityTypeCache());
        
        return stream.map(container -> entityContainerEnhancer.enhance(container, modelResult.getFetchModel(), queryModel.getParamValues()));
    }

    
    private <E extends AbstractEntity<?>> List<EntityContainer<E>> listContainersForIdOnlyQuery(final QueryProcessingModel<E, ?> queryModel, final Class<E> resultType, final Integer pageNumber, final Integer pageCapacity) {
        final EntityResultQueryModel<E> idOnlyModel = select(resultType).where().prop("id").in().model((SingleResultQueryModel<?>) queryModel.queryModel).model();
        
        final QueryProcessingModel<E,EntityResultQueryModel<E>> idOnlyQpm = new QueryProcessingModel<>(idOnlyModel, queryModel.orderModel, queryModel.fetchModel, queryModel.getParamValues(), queryModel.lightweight);
        
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
        
        final QueryProcessingModel<E,EntityResultQueryModel<E>> idOnlyQpm = new QueryProcessingModel<>(idOnlyModel, queryModel.orderModel, queryModel.fetchModel, queryModel.getParamValues(), queryModel.lightweight);
        
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
        QueryModelResult<E> result;
        count = count + 1;
        final DateTime st = new DateTime();
//        if (!qem.getParamValues().containsKey("EQL3")) {
//            final EntQueryGenerator gen = new EntQueryGenerator(domainMetadataAnalyser, filter, username, executionContext.dates());
//
//            final EntQuery entQuery = gen.generateEntQueryAsResultQuery(qem.queryModel, qem.orderModel, qem.queryModel.getResultType(), qem.fetchModel, qem.getParamValues());
//            final String sql = entQuery.sql();
//            result = new QueryModelResult<>((Class<E>)entQuery.type(), sql, getResultPropsInfos(entQuery.getYields()), entQuery.getValuesForSqlParams(), qem.fetchModel);
//        } else {
            try {
                final ua.com.fielden.platform.eql.stage1.builders.EntQueryGenerator gen1 = new ua.com.fielden.platform.eql.stage1.builders.EntQueryGenerator(domainMetadataAnalyser.getDbVersion(), filter, username, executionContext.dates(), qem.getParamValues());
                final DateTime st1 = new DateTime();
                final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo = (new ShortMetadata(executionContext.getDomainMetadata().lmd, filter, username, executionContext.dates(), qem.getParamValues())).generate(executionContext.getDomainMetadata().lmd.getEntityPropsMetadata().keySet());
                final Period pd1 = new Period(st1, new DateTime());
                dur1 = dur1 + 60000 * pd1.getMinutes() + 1000 * pd1.getSeconds() + pd1.getMillis();
                final PropsResolutionContext resolutionContext = new PropsResolutionContext(domainInfo);

                final DateTime st2 = new DateTime();
                final ResultQuery2 s1tr = gen1.generateEntQueryAsResultQuery(qem.queryModel, qem.orderModel, qem.fetchModel).transform(resolutionContext);
                final Period pd2 = new Period(st2, new DateTime());
                dur2 = dur2 + 60000 * pd2.getMinutes() + 1000 * pd2.getSeconds() + pd2.getMillis();

                final DateTime st3 = new DateTime();
                
                final Map<String, List<ChildGroup>> grouped = groupChildren(s1tr.collectProps(), domainInfo);
                final Period pd3 = new Period(st3, new DateTime());
                dur3 = dur3 + 60000 * pd3.getMinutes() + 1000 * pd3.getSeconds() + pd3.getMillis();
                
                final DateTime st4 = new DateTime();
                final TransformationResult<ResultQuery3> s2tr = s1tr.transform(new TransformationContext(executionContext.getDomainMetadata().lmd.getTables(), grouped));
                final Period pd4 = new Period(st4, new DateTime());
                dur4 = dur4 + 60000 * pd4.getMinutes() + 1000 * pd4.getSeconds() + pd4.getMillis();
                final ResultQuery3 entQuery3 = s2tr.item;
                final String sql3 = entQuery3.sql(domainMetadataAnalyser.getDbVersion());
                result =  new QueryModelResult<>((Class<E>)entQuery3.resultType, sql3, getResultPropsInfos(entQuery3.yields), s2tr.updatedContext.getParamValues(), qem.fetchModel);
            } catch (final Exception e) {
                e.printStackTrace();
                throw new EqlException("Can't accomplish QueryModelResult creation due to: " + e);
                // TODO: handle exception
            }
//        }
        
        final Period pd = new Period(st, new DateTime());
        dur = dur + 60000 * pd.getMinutes() + 1000 * pd.getSeconds() + pd.getMillis();

        return result;
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
            final Class<?> yieldType = ID.equals(yield.alias) && (isPersistedEntityType(yield.operand.type()) || isSyntheticBasedOnPersistentEntityType((Class<? extends AbstractEntity<?>>) yield.operand.type()))  ? Long.class : 
                yield.type != null ? yield.type : yield.operand.type();
            //System.out.println(yield.operand);
            final Object yieldHibType = yield.hibType != null ? yield.hibType : yield.operand.hibType();
            if (yield.isHeader) {
                result.add(new ResultQueryYieldDetails(yield.alias, yieldType, yieldHibType, null, isUnionEntityType(yieldType) ? UNION_ENTITY_HEADER : COMPOSITE_TYPE_HEADER));
            } else {
                if (yield.column == null) {
                    throw new EqlException("There is no column for yield with alias [" + yield.alias + "] of type [" + yield.type + "].");    
                }
                result.add(new ResultQueryYieldDetails(yield.alias, yieldType, yieldHibType, yield.column.name, USUAL_PROP));    
            }
            
        }
        return result;
    }
}