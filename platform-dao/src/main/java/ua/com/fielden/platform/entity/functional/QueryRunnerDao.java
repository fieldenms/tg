package ua.com.fielden.platform.entity.functional;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.DynamicEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.IQueryRunner;
import ua.com.fielden.platform.entity.functional.centre.QueryRunner;
import ua.com.fielden.platform.entity.functional.paginator.Page;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.review.DynamicFetchBuilder;
import ua.com.fielden.platform.swing.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.swing.review.DynamicParamBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link IQueryRunner}.
 *
 * @author Developers
 *
 */
@EntityType(QueryRunner.class)
public class QueryRunnerDao extends CommonEntityDao<QueryRunner> implements IQueryRunner {

    private final ICompanionObjectFinder companionObjectFinder;
    private final DynamicEntityDao dynamicDao;

    @Inject
    public QueryRunnerDao(final IFilter filter, final ICompanionObjectFinder companionObjectFinder, final DynamicEntityDao dynamicDao) {
        super(filter);
        this.companionObjectFinder = companionObjectFinder;
        this.dynamicDao = dynamicDao;
    }

    @SuppressWarnings("unchecked")
    @Override
    @SessionRequired
    public QueryRunner save(final QueryRunner queryRunner) {
        // Here we get the type of the centre entity, which should be used to get result instances.
        // This type is generated and in this case IGeneratedEntityController should be used.
        final Class<AbstractEntity<?>> centreEntityType = queryRunner.getQuery().getActualEntityType();

        final ICompleted<AbstractEntity<?>> query = DynamicQueryBuilder.createQuery(centreEntityType, queryRunner.getQuery().getQueryProperties());
        final fetch<AbstractEntity<?>> fetchModel = DynamicFetchBuilder.createFetchOnlyModel(centreEntityType, queryRunner.getQuery().createFetchProps());
        final Set<String> summaryProps = queryRunner.getQuery().createSummaryProps();
        final fetch<AbstractEntity<?>> totalFetchModel = summaryProps == null || summaryProps.isEmpty() ? null
                : DynamicFetchBuilder.createTotalFetchModel(centreEntityType, summaryProps);
        final OrderingModel queryOrdering = DynamicOrderingBuilder.createOrderingModel(centreEntityType, queryRunner.getQuery().createOrderingProps());
        final Map<String, Pair<Object, Object>> paramMap = queryRunner.getQuery().createParamMap();
        final Map<String, Object> parameters = parameters(centreEntityType, paramMap);

        final QueryExecutionModel<AbstractEntity<?>, EntityResultQueryModel<AbstractEntity<?>>> queryModel = from(query.model()).with(fetchModel).with(queryOrdering).with(parameters).model();
        final QueryExecutionModel<AbstractEntity<?>, EntityResultQueryModel<AbstractEntity<?>>> totalQueryModel = totalFetchModel == null ? null : from(query.model()).with(totalFetchModel).with(parameters).model();

        IEntityDao<AbstractEntity<?>> controller = companionObjectFinder.find(centreEntityType);
        controller = controller == null ? dynamicDao : controller;

        final IPage<AbstractEntity<?>> resultPage = controller.firstPage(queryModel, totalQueryModel, queryRunner.getPageCapacity());
        final Page page = queryRunner.getEntityFactory().newPlainEntity(Page.class, null).
                setNumberOfPages(resultPage.numberOfPages()).
                setPageNo(resultPage.no()).
                setSummary(resultPage.summary()).
                setResults(resultPage.data());
        return queryRunner.getEntityFactory().newEntity(QueryRunner.class).setQuery(null).setPage(page);
    }

    private Map<String, Object> parameters(final Class<AbstractEntity<?>> centreEntityType, final Map<String, Pair<Object, Object>> paramMap) {
        return DynamicParamBuilder.buildParametersMap(centreEntityType, paramMap);
    }
}