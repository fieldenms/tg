package ua.com.fielden.platform.swing.review.report.analysis.grid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.SinglePage;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.review.DynamicPropertyAnalyser;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;
import ua.com.fielden.platform.utils.Pair;

public class GridAnalysisModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisReviewModel<T, CDTME, IAbstractAnalysisDomainTreeManager> {
    private final long TRANSACTION_ENTITY_DELTA_DELAY = 6000;
    /** holds the last executed query */
    private Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> analysisQueries;

    private final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, CDTME>> queryCustomiser;
    private DeltaRetriever deltaRetriever;

    public GridAnalysisModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria, final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, CDTME>> queryCustomiser) {
	super(criteria, null);
	this.queryCustomiser = queryCustomiser;
    }

    /**
     * An utility class to receive delta and to provide a merged entities.
     *
     * @author TG Team
     *
     */
    private class DeltaRetriever {
        private final TreeSet<T> entities;
        private Timer timer;
        private Date oldNow;

        public DeltaRetriever(final IPage<T> initialPage, final Comparator<T> comparator, final Date oldNow) {
            this.oldNow = oldNow;
            entities = new TreeSet<T>(comparator);
	    entities.addAll(initialPage.data());
	}

	public void scheduleDeltaRetrieval() {
	    if (timer != null) {
		throw new IllegalStateException("Can not schedule one more task, when other task exists.");
	    }

            timer = new Timer();
	    final TimerTask deltaRetrivalTask = new TimerTask() {
		@Override
		public void run() {
		    if (analysisQueries == null) {
			throw new IllegalArgumentException("Cannot retrieve 'delta' if main query has not been run.");
		    }

		    final Date old = new Date(oldNow.getTime());
		    oldNow = new Date();
		    final Result result = runQuery(enhanceByTransactionDateBoundaries(analysisQueries, old, oldNow));
		    if (result.isSuccessful()) {
			getPageHolder().newPage(produceEnhancedPage(page(result))); // update loaded page
		    } else {
			System.err.println("DELTA RETRIEVAL HAS BEEN FAILED DUE TO [" + result.getMessage() + "].");
		    }

		    timer = null;
		    scheduleDeltaRetrieval();
		}
	    };
	    timer.schedule(deltaRetrivalTask, TRANSACTION_ENTITY_DELTA_DELAY);
	}

	public IPage<T> produceEnhancedPage(final IPage<T> retrievedPage) {
	    for (final T entity : retrievedPage.data()) {
		entities.add(entity); // efficient adding (merging) to a tree structure
	    }
	    return new SinglePage<T>(new ArrayList<T>(entities)) {
		@Override
		public T summary() {
		    return retrievedPage.summary();
		}
	    };
	}

	public void stop() {
	    timer.cancel();
	}
    }

    /**
     * Enhance existing queries (immutably) with transaction date boundaries.
     *
     * @param oldNow -- if <code>null</code> then the initial query is performed, else -- delta query from "oldNow" should be performed.
     * @param queries
     * @return
     */
    private Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> enhanceByTransactionDateBoundaries(final Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> queries, final Date left, final Date right) {
	// TODO queries.getKey()!   .getQueryModel(). setParam transDate (left == null ? [---, right] : [left, right])

	// TODO total query should remain the same (to get updated totals) and other query should be filtered by transaction date (from NOW)
	return queries;
    }

    private IPage<T> page(final Result result) {
	return (IPage<T>) result.getInstance();
    }

    /**
     * Creates query execution models, validates them and either runs returning the first page or throws {@link Result} to indicate any errors.
     */
    @Override
    public Result executeAnalysisQuery() {
	analysisQueries = createQueryExecutionModel();

	final Result result;
	final Date now;
	if (AnnotationReflector.isTransactionEntity(getCriteria().getEntityClass())) {
	    if (deltaRetriever != null) {
		deltaRetriever.stop();
	    }
	    now = new Date();
	    result = runQuery(enhanceByTransactionDateBoundaries(analysisQueries, null, now));
	} else {
	    result = runQuery(analysisQueries);
	    now = null;
	}

	if (result.isSuccessful()) {
	    final IPage<T> loadedPage = promotePage(result);

	    if (AnnotationReflector.isTransactionEntity(getCriteria().getEntityClass())) {
		deltaRetriever = new DeltaRetriever(loadedPage, createComparator(), now);
		deltaRetriever.scheduleDeltaRetrieval();
	    }
	}
	return result;
    }

    private Comparator<T> createComparator() {
	// TODO create comparator based on the ordering properties of CDTMAE.
	return null;
    }

    public IPage<T> promotePage(final Result result) {
	final IPage<T> loadedPage = page(result);
	getPageHolder().newPage(loadedPage); // update loaded page
	return loadedPage;
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
	return Result.successful(newPage);
    }

    @Override
    public Result reExecuteAnalysisQuery() {
	if (analysisQueries == null) {
	    return executeAnalysisQuery();
	} else {
	    final Result result = runQuery(analysisQueries);
	    if (result.isSuccessful()) {
		promotePage(result);
	    }
	    return result;
	}
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
