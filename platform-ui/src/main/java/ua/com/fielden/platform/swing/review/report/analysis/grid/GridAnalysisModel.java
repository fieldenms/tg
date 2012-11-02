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

public class GridAnalysisModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisReviewModel<T, CDTME, IAbstractAnalysisDomainTreeManager, IPage<T>> {


    private GridAnalysisView<T, CDTME> analysisView;

    public GridAnalysisModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria) {
	super(criteria, null);
	this.analysisView = null;
    }

    /**
     * Set the analysis view for this model.
     * Please note that one can set analysis view only once.
     * Otherwise The {@link IllegalStateException} will be thrown.
     *
     * @param analysisView
     */
    final void setAnalysisView(final GridAnalysisView<T, CDTME> analysisView){
	if(this.analysisView != null){
	    throw new IllegalStateException("The analysis view can be set only once!");
	}
	this.analysisView = analysisView;
    }

    @Override
    protected IPage<T> executeAnalysisQuery() {
	final Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> analysisQueries = createQueryExecutionModel();
	final int pageSize = analysisView.getPageSize();
	final IPage<T> newPage;
	if(analysisQueries.getValue() == null){
	    newPage = getCriteria().firstPage(analysisQueries.getKey(), pageSize);
	} else {
	    newPage = getCriteria().firstPage(analysisQueries.getKey(), analysisQueries.getValue(), pageSize);
	}
	getPageHolder().newPage(newPage);
	return newPage;
    }

    protected T getEntityById(final Long id) {
        return getCriteria().getEntityById(id);
    }

    @Override
    protected Result canLoadData() {
	return getCriteria().isValid();
    }

    @Override
    protected void exportData(final String fileName) throws IOException {
	final PropertyTableModel<T> tableModel = analysisView.getEgiPanel().getEgi().getActualModel();
	final List<String> propertyNames = new ArrayList<String>(tableModel.getPropertyColumnMappings().size());
	final List<String> propertyTitles = new ArrayList<String>(tableModel.getPropertyColumnMappings().size());
	for (final AbstractPropertyColumnMapping<T> mapping : tableModel.getPropertyColumnMappings()) {
	    final DynamicPropertyAnalyser propertyAnalyser = new DynamicPropertyAnalyser(getCriteria().getManagedType(), mapping.getPropertyName());
	    propertyNames.add(propertyAnalyser.getCriteriaFullName());
	    propertyTitles.add(mapping.getPropertyTitle());
	}
	getCriteria().export(fileName, createQueryExecutionModel().getKey(), propertyNames.toArray(new String[] {}), propertyTitles.toArray(new String[] {}));
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
    protected final Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> createQueryExecutionModel(){
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

    protected final GridAnalysisView<T, CDTME> getAnalysisView() {
	return analysisView;
    }
}
