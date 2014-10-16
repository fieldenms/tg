package ua.com.fielden.platform.entity.functional;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.util.List;
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
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
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
	final Class<AbstractEntity<?>> entityType = queryRunner.getQuery().getActualEntityType();

	final List<QueryProperty> queryProps = queryRunner.getQuery().getQueryProperties();
	final ICompleted<AbstractEntity<?>> query = DynamicQueryBuilder.createQuery(entityType, queryProps);

	final fetch<AbstractEntity<?>> fetchModel = DynamicFetchBuilder.createFetchOnlyModel(entityType, queryRunner.getQuery().createFetchProps());

	final Set<String> summaryProps = queryRunner.getQuery().createSummaryProps();
	final fetch<AbstractEntity<?>> total = summaryProps == null || summaryProps.isEmpty() ? null : DynamicFetchBuilder.createTotalFetchModel(entityType, summaryProps);

	final OrderingModel queryOrdering = DynamicOrderingBuilder.createOrderingModel(entityType, queryRunner.getQuery().createOrderingProps());

	final Map<String, Pair<Object, Object>> paramMap = queryRunner.getQuery().createParamMap();

	IEntityDao<AbstractEntity<?>> controller = companionObjectFinder.find(entityType);
	controller = controller == null ? dynamicDao : controller;

	final QueryExecutionModel<AbstractEntity<?>, EntityResultQueryModel<AbstractEntity<?>>> queryModel = from(query.model()).//
		with(fetchModel).//
		with(queryOrdering).//
		with(DynamicParamBuilder.buildParametersMap(entityType, paramMap)).model();
	final QueryExecutionModel totalModel = total != null ? from(query.model()).//
		with(total).//
		with(DynamicParamBuilder.buildParametersMap(entityType, paramMap)).model() : null;
	final IPage<AbstractEntity<?>> resultPage = controller.firstPage(queryModel, totalModel, queryRunner.getPageCapacity());
	final Page page = queryRunner.getEntityFactory().newEntity(Page.class).
		setNumberOfPages(resultPage.numberOfPages()).
		setPageNo(resultPage.no()).
		setSummary(resultPage.summary()).
		setResult(resultPage.data());
	return queryRunner.getEntityFactory().newEntity(QueryRunner.class).setQuery(null).setPage(page);
    }
}