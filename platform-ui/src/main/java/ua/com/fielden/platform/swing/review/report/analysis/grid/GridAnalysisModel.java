package ua.com.fielden.platform.swing.review.report.analysis.grid;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.review.DynamicFetchBuilder;
import ua.com.fielden.platform.swing.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.swing.review.DynamicParamBuilder;
import ua.com.fielden.platform.swing.review.DynamicPropertyAnalyser;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;
import ua.com.fielden.platform.utils.Pair;

public class GridAnalysisModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisReviewModel<T, CDTME, IAbstractAnalysisDomainTreeManager> {

    /** holds the last executed query */
    private Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> analysisQueries;

    public GridAnalysisModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria) {
	super(criteria, null);
    }

    /**
     * Creates query execution models, validates them and either runs returning the first page or throws {@link Result} to indicate any errors.
     */
    @Override
    protected Result executeAnalysisQuery() {
	analysisQueries = createQueryExecutionModel();
	return runQuery(analysisQueries);
    }


    private Result runQuery(final Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> analysisQueries) {
	final Result analysisQueryExecutionResult = canLoadData();
	if(!analysisQueryExecutionResult.isSuccessful()){
	    return analysisQueryExecutionResult;
	}

	final int pageSize = getAnalysisView().getPageSize();
	final IPage<T> newPage;
	if(analysisQueries.getValue() == null){
	    newPage = getCriteria().firstPage(analysisQueries.getKey(), pageSize);
	} else {
	    newPage = getCriteria().firstPage(analysisQueries.getKey(), analysisQueries.getValue(), pageSize);
	}
	getPageHolder().newPage(newPage);
	return Result.successful(newPage);
    }

    @Override
    protected Result reExecuteAnalysisQuery() {
	return analysisQueries == null ? executeAnalysisQuery() : runQuery(analysisQueries);
    }













    protected T getEntityById(final Long id) {
        return getCriteria().getEntityById(id);
    }

    private Result canLoadData() {
	return getCriteria().isValid();
    }

    @Override
    protected Result exportData(final String fileName) throws IOException {
	final Result analysisQueryExecutionResult = canLoadData();
	if(!analysisQueryExecutionResult.isSuccessful()){
	    return analysisQueryExecutionResult;
	}
	final PropertyTableModel<T> tableModel = getAnalysisView().getEgiPanel().getEgi().getActualModel();
	final List<String> propertyNames = new ArrayList<String>(tableModel.getPropertyColumnMappings().size());
	final List<String> propertyTitles = new ArrayList<String>(tableModel.getPropertyColumnMappings().size());
	for (final AbstractPropertyColumnMapping<T> mapping : tableModel.getPropertyColumnMappings()) {
	    final DynamicPropertyAnalyser propertyAnalyser = new DynamicPropertyAnalyser(getCriteria().getManagedType(), mapping.getPropertyName());
	    propertyNames.add(propertyAnalyser.getCriteriaFullName());
	    propertyTitles.add(mapping.getPropertyTitle());
	}
	getCriteria().export(fileName, createQueryExecutionModel().getKey(), propertyNames.toArray(new String[] {}), propertyTitles.toArray(new String[] {}));
	return Result.successful(this);
    }

    @Override
    protected String[] getExportFileExtensions() {
	return new String[] {getDefaultExportFileExtension()};
    }

    @Override
    protected String getDefaultExportFileExtension() {
	return "xls";
    }

    /**
     * Returns the pair of {@link QueryExecutionModel} instances. The second {@link QueryExecutionModel} is total query model.
     *
     * @return
     */
    public final Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> createQueryExecutionModel(){
	final Class<T> root = getCriteria().getEntityClass();
	final Class<T> managedType = getCriteria().getManagedType();
	final IAddToResultTickManager resultTickManager = getCriteria().getCentreDomainTreeMangerAndEnhancer().getSecondTick();
	final IAddToCriteriaTickManager criteriaTickManager = getCriteria().getCentreDomainTreeMangerAndEnhancer().getFirstTick();
	final IDomainTreeEnhancer enhancer = getCriteria().getCentreDomainTreeMangerAndEnhancer().getEnhancer();
	final Pair<Set<String>, Set<String>> separatedFetch = EntityQueryCriteriaUtils.separateFetchAndTotalProperties(root, resultTickManager, enhancer);
	final Map<String, Pair<Object, Object>> paramMap = EntityQueryCriteriaUtils.createParamValuesMap(root, managedType, criteriaTickManager);
	final EntityResultQueryModel<T> notOrderedQuery = createQueryModel();
	final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = from(notOrderedQuery)//
		.with(DynamicOrderingBuilder.createOrderingModel(managedType, resultTickManager.orderedProperties(root)))//
		.with(DynamicFetchBuilder.createFetchOnlyModel(managedType, separatedFetch.getKey()))//
		.with(DynamicParamBuilder.buildParametersMap(managedType, paramMap)).model();
	if (!separatedFetch.getValue().isEmpty()) {
	    final QueryExecutionModel<T, EntityResultQueryModel<T>> totalQuery = from(notOrderedQuery)//
		    .with(DynamicFetchBuilder.createTotalFetchModel(managedType, separatedFetch.getValue()))//
		    .with(DynamicParamBuilder.buildParametersMap(managedType, paramMap)).model();
	    return new Pair<QueryExecutionModel<T,EntityResultQueryModel<T>>, QueryExecutionModel<T,EntityResultQueryModel<T>>>(resultQuery, totalQuery);
	} else {
	    return new Pair<QueryExecutionModel<T,EntityResultQueryModel<T>>, QueryExecutionModel<T,EntityResultQueryModel<T>>>(resultQuery, null);
	}
    }

    /**
     * Returns the {@link EntityResultQueryModel} instance, that is used for query generation in the {@link #createQueryExecutionModel()} routine.
     * Override this to provide custom query generation.
     *
     * @return
     */
    protected EntityResultQueryModel<T> createQueryModel(){
	return DynamicQueryBuilder.createQuery(getCriteria().getManagedType(), getCriteria().createQueryProperties()).model();
    }

    @Override
    protected GridAnalysisView<T, CDTME> getAnalysisView() {
	return (GridAnalysisView<T, CDTME>)super.getAnalysisView();
    }
}
