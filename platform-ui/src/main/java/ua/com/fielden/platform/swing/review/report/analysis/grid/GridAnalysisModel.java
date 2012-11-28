package ua.com.fielden.platform.swing.review.report.analysis.grid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.review.DynamicPropertyAnalyser;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;
import ua.com.fielden.platform.utils.Pair;

public class GridAnalysisModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisReviewModel<T, CDTME, IAbstractAnalysisDomainTreeManager> {

    /** holds the last executed query */
    private Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> analysisQueries;

    private final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, CDTME>> queryCustomiser;

    public GridAnalysisModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria, final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, CDTME>> queryCustomiser) {
	super(criteria, null);
	this.queryCustomiser = queryCustomiser;
    }

    /**
     * Creates query execution models, validates them and either runs returning the first page or throws {@link Result} to indicate any errors.
     */
    @Override
    public Result executeAnalysisQuery() {
	analysisQueries = createQueryExecutionModel();
	return runQuery(analysisQueries);
    }

    /**
     * Runs the specified query models. The first query of the specified pair returns result for grid the second one returns result for totals.
     *
     * @param analysisQueries
     * @return
     */
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
    public Result reExecuteAnalysisQuery() {
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
	final List<QueryExecutionModel<T, EntityResultQueryModel<T>>> queries = queryCustomiser.getQueryGenerator(this).generateQueryModel().getQueries();
	if (queries.size() == 2) {
	    return new Pair<>(queries.get(0), queries.get(1));
	} else {
	    return new Pair<>(queries.get(0), null);
	}
    }

    @Override
    protected GridAnalysisView<T, CDTME> getAnalysisView() {
	return (GridAnalysisView<T, CDTME>)super.getAnalysisView();
    }
}
