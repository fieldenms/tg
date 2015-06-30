package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.Query;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.generation.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails;
import ua.com.fielden.platform.entity.query.generation.elements.Yield;
import ua.com.fielden.platform.entity.query.generation.elements.Yields;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

public class EntityContainerFetcher {
    private final QueryExecutionContext executionContext;    
    private final Logger logger = Logger.getLogger(this.getClass());

    public EntityContainerFetcher(final QueryExecutionContext executionContext) {
        this.executionContext = executionContext;
    }
    
    public <E extends AbstractEntity<?>> List<EntityContainer<E>> listAndEnhanceContainers(final QueryExecutionModel<E, ?> queryModel, final Integer pageNumber, final Integer pageCapacity)
            throws Exception {
        final DomainMetadataAnalyser domainMetadataAnalyser = new DomainMetadataAnalyser(executionContext.getDomainMetadata());
        final QueryModelResult<E> modelResult = getModelResult(queryModel, domainMetadataAnalyser, executionContext.getFilter(), executionContext.getUsername());

        if (modelResult.idOnlyQuery()) {
            return listContainersForIdOnlyQuery(queryModel, modelResult.getResultType(), pageNumber, pageCapacity);
        }
        
        final List<EntityContainer<E>> result = listContainersAsIs(modelResult, pageNumber, pageCapacity);
        logger.debug("Fetch model:\n" + modelResult.getFetchModel());
        return new EntityContainerEnhancer<E>(this, domainMetadataAnalyser).enhance(result, modelResult.getFetchModel());
    }
    
    private <E extends AbstractEntity<?>> List<EntityContainer<E>> listContainersForIdOnlyQuery(final QueryExecutionModel<E, ?> queryModel, Class<E> resultType, final Integer pageNumber, final Integer pageCapacity) throws Exception {
        return listAndEnhanceContainers(from(select(resultType).where().prop("id").in().model((SingleResultQueryModel) queryModel.getQueryModel()).model()). //
        lightweight(queryModel.isLightweight()). //
        with(queryModel.getOrderModel()). //
        with(queryModel.getFetchModel()). //
        with(queryModel.getParamValues()).model(), pageNumber, pageCapacity);
    }

    private <E extends AbstractEntity<?>> List<EntityContainer<E>> listContainersAsIs(final QueryModelResult<E> modelResult, final Integer pageNumber, final Integer pageCapacity)
            throws Exception {
        final EntityTree<E> resultTree = new EntityResultTreeBuilder().buildEntityTree(modelResult.getResultType(), modelResult.getYieldedPropsInfo());

        EntityHibernateRetrievalQueryProducer queryProducer = new EntityHibernateRetrievalQueryProducer(modelResult.getSql(), resultTree.getScalarFromEntityTree(), modelResult.getParamValues(), pageNumber, pageCapacity);
        
        final Query query = queryProducer.produceHibernateQuery(executionContext.getSession());

        EntityRawResultConverter<E> entityRawResultConverter = new EntityRawResultConverter<E>(executionContext.getEntityFactory(), executionContext.getCoFinder());

        return entityRawResultConverter.transformFromNativeResult(resultTree, query.list());
    }
    
    private <T extends AbstractEntity<?>> QueryModelResult<T> getModelResult(final QueryExecutionModel<T, ?> qem, final DomainMetadataAnalyser domainMetadataAnalyser, final IFilter filter, final String username) {
        final EntQueryGenerator gen = new EntQueryGenerator(domainMetadataAnalyser, filter, username, executionContext.getUniversalConstants());
        final IRetrievalModel<T> fm = qem.getFetchModel() == null ? //
        (qem.getQueryModel().getResultType().equals(EntityAggregates.class) ? null
                : new EntityRetrievalModel<T>(fetch(qem.getQueryModel().getResultType()), domainMetadataAnalyser))
                : // 
                (qem.getQueryModel().getResultType().equals(EntityAggregates.class) ? new EntityAggregatesRetrievalModel<T>(qem.getFetchModel(), domainMetadataAnalyser)
                        : new EntityRetrievalModel<T>(qem.getFetchModel(), domainMetadataAnalyser));

        final EntQuery entQuery = gen.generateEntQueryAsResultQuery(qem.getQueryModel(), qem.getOrderModel(), qem.getQueryModel().getResultType(), fm, qem.getParamValues());
        final String sql = entQuery.sql();
        return new QueryModelResult<T>(entQuery.type(), sql, getResultPropsInfos(entQuery.getYields()), entQuery.getValuesForSqlParams(), fm);
    }

    private SortedSet<ResultQueryYieldDetails> getResultPropsInfos(final Yields model) {
        final SortedSet<ResultQueryYieldDetails> result = new TreeSet<ResultQueryYieldDetails>();
        for (final Yield yield : model.getYields()) {
            result.add(new ResultQueryYieldDetails(yield.getInfo().getName(), yield.getInfo().getJavaType(), yield.getInfo().getHibType(), yield.getInfo().getColumn(), yield.getInfo().getYieldDetailsType()));
        }
        return result;
    }
}