package ua.com.fielden.platform.swing.review.report.analysis.grid;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import org.joda.time.DateTime;

import ua.com.fielden.platform.criteria.generator.impl.CriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.SinglePage;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.report.query.generation.GridAnalysisQueryGenerator;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.review.DynamicPropertyAnalyser;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class GridAnalysisModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisReviewModel<T, CDTME, IAbstractAnalysisDomainTreeManager> {
    private final long TRANSACTION_ENTITY_DELTA_DELAY = 6000;
    /** holds the last executed query */
    private Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> analysisQueries;
    private final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, CDTME>> queryCustomiser;

    /******* Delta related stuff ********/
    private DeltaRetriever deltaRetriever;
    private ICentreDomainTreeManagerAndEnhancer cdtmaeCopy;
    private final static String tdPropCopy = "_________transactionDatePropertyCopy";

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
    public class DeltaRetriever {
	private final TreeSet<T> entities;
	private Timer timer;
	private Date oldNow;

	public DeltaRetriever(final IPage<T> initialPage, final Comparator<T> comparator, final Date oldNow) {
	    this.oldNow = oldNow;
	    entities = new TreeSet<T>(comparator);
	    entities.addAll(initialPage.data());
	}

	public void scheduleDeltaRetrieval() {
	    scheduleDeltaRetrieval(transactionEntityDeltaDelay());
	}

	public void scheduleDeltaRetrieval(final long delay) {
	    if (timer != null) {
		System.err.println("One more task is trying to be schedulled, when other task exists ==> ignored.");
		return;
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
		    final Result result = getDelta(old, oldNow);
		    if (result.isSuccessful()) {
			final IPage<T> deltaPage = page(result);
			if (!deltaPage.data().isEmpty()) {
			    getPageHolder().newPage(produceEnhancedPage(deltaPage)); // update loaded page
			}
		    } else {
			System.err.println("DELTA RETRIEVAL HAS BEEN FAILED DUE TO [" + result.getMessage() + "].");
		    }

		    timer = null;

		    if (getAnalysisView().isShowing()) {
			System.err.println("INIT DELTA RETRIEVAL ==> isShowing == " + getAnalysisView().isShowing());
			scheduleDeltaRetrieval();
		    } else {
			System.err.println("DELTA RETRIEVAL STOPPED ==> isShowing == " + getAnalysisView().isShowing());
		    }
		}
	    };
	    timer.schedule(deltaRetrivalTask, delay);
	}

	public IPage<T> produceEnhancedPage(final IPage<T> retrievedPage) {
	    for (final T entity : retrievedPage.data()) {
		entities.remove(entity); // remove old entity if exists
		entities.add(entity); // efficient adding (merging) to a tree structure
	    }
	    return createSinglePage(retrievedPage, new ArrayList<T>(entities));
	}

	public void stop() {
	    if (timer != null) {
		timer.cancel();
	    }
	}
    }

    protected long initialTransactionEntityDeltaDelay() {
	return TRANSACTION_ENTITY_DELTA_DELAY;
    }

    protected long transactionEntityDeltaDelay() {
	return TRANSACTION_ENTITY_DELTA_DELAY;
    }

    protected static <T extends AbstractEntity<?>> IPage<T> createSinglePage(final IPage<T> retrievedPage, final Collection<T> entities) {
	return new SinglePage<T>(new ArrayList<T>(entities)) {
	    @Override
	    public T summary() {
		return retrievedPage == null ? null : retrievedPage.summary();
	    }

	    @Override
	    public String toString() {
		return "Page 1 of 1";
	    }
	};
    }

    public void stopDeltaRetrievalIfAny() {
	if (deltaRetriever != null) {
	    deltaRetriever.stop();
	}
    }

    /**
     * Provides a new {@link EntityQueryCriteria} for "cdtmaeCopy".
     *
     * TODO : this is dangerous, need to create a proper copy (perhaps through {@link CriteriaGenerator}?).
     *
     * @return
     */
    public EntityQueryCriteria<CDTME, T, IEntityDao<T>> getUpdatedCriteria() {
	final EntityQueryCriteria<CDTME, T, IEntityDao<T>> old = getCriteria();

	final EntityQueryCriteria<CDTME, T, IEntityDao<T>> newCriteria = new EntityQueryCriteria<CDTME, T, IEntityDao<T>>(null, old.getGeneratedEntityController(), old.getSerialiser(), old.getControllerProvider()) {
	};

	// Set dao for generated entity query criteria.
	final Field daoField = Finder.findFieldByName(EntityQueryCriteria.class, "dao");
	final boolean isDaoAccessable = daoField.isAccessible();
	daoField.setAccessible(true);
	try {
	    daoField.set(newCriteria, old.companionObject());
	} catch (IllegalArgumentException | IllegalAccessException e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
	daoField.setAccessible(isDaoAccessable);

	// Set domain tree manager for entity query criteria.
	final Field dtmField = Finder.findFieldByName(EntityQueryCriteria.class, "cdtme");
	final boolean isCdtmeAccessable = dtmField.isAccessible();
	dtmField.setAccessible(true);
	try {
	    dtmField.set(newCriteria, cdtmaeCopy);
	} catch (IllegalArgumentException | IllegalAccessException e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
	dtmField.setAccessible(isCdtmeAccessable);
	return newCriteria;
    }

    public ICentreDomainTreeManagerAndEnhancer getCdtme() {
	if (queryCustomiser.getQueryGenerator(this) instanceof GridAnalysisQueryGenerator) {
	    final GridAnalysisQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer> qGenerator = (GridAnalysisQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer>) queryCustomiser.getQueryGenerator(this);
	    return qGenerator.getCdtme();
	}
	return null;
    }

    /**
     * This method is designed to be overridden for adding some custom fetch properties or other stuff to override query.
     *
     * @param cdtmaeCopy
     */
    protected void provideCustomPropertiesForQueries(final ICentreDomainTreeManagerAndEnhancer cdtmaeCopy) {
    }

    protected static void checkFetchPropertyIfNotChecked(final ICentreDomainTreeManagerAndEnhancer cdtmaeCopy, final Class<?> root, final String property) {
	if (!cdtmaeCopy.getSecondTick().isChecked(root, property)) {
	    cdtmaeCopy.getSecondTick().check(root, property, true);
	}
    }

    /**
     * Enhance existing queries (immutably) with transaction date boundaries.
     *
     * @param oldNow -- if <code>null</code> then the initial query is performed, else -- delta query from "oldNow" should be performed.
     * @param now -- if <code>null</code> then the initial query is performed, else -- delta query from "oldNow" should be performed.
     * @param queries
     * @return
     */
    protected Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> enhanceByTransactionDateBoundaries(final Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> queries, final Date oldNow, final Date now) {
	// queries.getKey()!   .getQueryModel(). setParam transDate (left == null ? [---, right] : [left, right])
	if (queryCustomiser.getQueryGenerator(this) instanceof GridAnalysisQueryGenerator) {
	    final GridAnalysisQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer> qGenerator = (GridAnalysisQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer>) queryCustomiser.getQueryGenerator(this);
	    final Class<T> root = qGenerator.entityClass();
	    final String tdProp = AnnotationReflector.getTransactionDateProperty(root);
	    if (oldNow == null) { // RUN is performed (not Delta)
		final ICentreDomainTreeManagerAndEnhancer cdtme = qGenerator.getCdtme();
		cdtmaeCopy = EntityUtils.deepCopy(cdtme, ((CentreDomainTreeManagerAndEnhancer) cdtme).getSerialiser());
		cdtmaeCopy.getEnhancer().addCalculatedProperty(root, "", tdProp, tdPropCopy, "A copy of transaction date property to enhance query with delta boundaries", CalculatedPropertyAttribute.NO_ATTR, "");
		cdtmaeCopy.getEnhancer().apply();
		cdtmaeCopy.getFirstTick().check(root, tdPropCopy, true);

		provideCustomPropertiesForQueries(cdtmaeCopy);
	    }
	    final Class<?> tdPropType = PropertyTypeDeterminator.determinePropertyType(root, tdProp);
	    if (EntityUtils.isDate(tdPropType)) {
		cdtmaeCopy.getFirstTick().setValue(root, tdPropCopy, oldNow);
		cdtmaeCopy.getFirstTick().setValue2(root, tdPropCopy, now);
	    } else if (EntityUtils.isDateTime(tdPropType)) {
		cdtmaeCopy.getFirstTick().setValue(root, tdPropCopy, oldNow == null ? null : new DateTime(oldNow.getTime()));
		cdtmaeCopy.getFirstTick().setValue2(root, tdPropCopy, new DateTime(now.getTime()));
	    } else {
		throw new IllegalArgumentException("The type [" + tdPropType.getSimpleName() + "] of property [" + tdProp + "] in entity [" + root + "] is not supported.");
	    }

	    final GridAnalysisQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer> newQGenerator = new GridAnalysisQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer>(root, cdtmaeCopy);
	    final List<QueryExecutionModel<T, EntityResultQueryModel<T>>> newQueries = newQGenerator.generateQueryModel().getQueries();
	    if (newQueries.size() == 2) {
		// TODO total query should remain the same (to get updated totals) and other query should be filtered by transaction date (from NOW)
		return new Pair<>(newQueries.get(0), newQueries.get(1));
	    } else {
		return new Pair<>(newQueries.get(0), null);
	    }
	} else {
	    throw new IllegalArgumentException("Non GridAnalysisQueryGenerator is not supported for TransactionalEntity handling.");
	}
    }

    protected IPage<T> page(final Result result) {
	return (IPage<T>) result.getInstance();
    }

    /**
     * Creates query execution models, validates them and either runs returning the first page or throws {@link Result} to indicate any errors.
     */
    @Override
    public Result executeAnalysisQuery() {
	try {
	    analysisQueries = createQueryExecutionModel();
	} catch(final Result result){
	    return result;
	}

	final Result result;
	final Date now;
	if (AnnotationReflector.isTransactionEntity(getCriteria().getEntityClass())) {
	    if (deltaRetriever != null) {
		deltaRetriever.stop();
	    }
	    now = new Date();
	    final Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> queries = enhanceByTransactionDateBoundaries(analysisQueries, null, now);
	    result = runQuery((EntityUtils.equalsEx(analysisQueries, queries)) ? getCriteria() : getUpdatedCriteria(), queries);
	} else {
	    result = runQuery(getCriteria(), analysisQueries);
	    now = null;
	}

	if (result.isSuccessful()) {
	    final IPage<T> loadedPage = promotePage(result);

	    if (AnnotationReflector.isTransactionEntity(getCriteria().getEntityClass())) {
		deltaRetriever = new DeltaRetriever(loadedPage, createComparator(), now);
		deltaRetriever.scheduleDeltaRetrieval(initialTransactionEntityDeltaDelay());
	    }
	}
	return result;
    }

    //TODO maybe it should be removed.
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
    private Result runQuery(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria, final Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> analysisQueries) {
	final int pageSize = getAnalysisView().getPageSize();
	final IPage<T> newPage;
	try {
	    if(analysisQueries.getValue() == null) {
		newPage = criteria.firstPage(analysisQueries.getKey(), pageSize);
	    } else {
		newPage = criteria.firstPage(analysisQueries.getKey(), analysisQueries.getValue(), pageSize);
	    }
	} catch (final Result ex) {
	    return ex;
	} catch (final Exception ex) {
	    return new Result(ex);
	}
	return Result.successful(newPage);
    }

    @Override
    public Result reExecuteAnalysisQuery() {
	if (analysisQueries == null) {
	    return executeAnalysisQuery();
	} else {
	    final Result result = runQuery(getCriteria(), analysisQueries);
	    if (result.isSuccessful()) {
		promotePage(result);
	    }
	    return result;
	}
    }

    protected T getEntityById(final Long id) {
	return getCriteria().getEntityById(id);
    }

    private Result canCreateQuery() {
	return getCriteria().isValid();
    }

    @Override
    protected Result exportData(final String fileName) throws IOException {
	final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel;
	try {
	    queryModel = createQueryExecutionModel().getKey();
	} catch(final Result result){
	    return result;
	}
	final PropertyTableModel<T> tableModel = getAnalysisView().getEgiPanel().getEgi().getActualModel();
	final List<String> propertyNames = new ArrayList<String>(tableModel.getPropertyColumnMappings().size());
	final List<String> propertyTitles = new ArrayList<String>(tableModel.getPropertyColumnMappings().size());
	for (final AbstractPropertyColumnMapping<T> mapping : tableModel.getPropertyColumnMappings()) {
	    final DynamicPropertyAnalyser propertyAnalyser = new DynamicPropertyAnalyser(getCriteria().getManagedType(), mapping.getPropertyName());
	    propertyNames.add(propertyAnalyser.getCriteriaFullName());
	    propertyTitles.add(mapping.getPropertyTitle());
	}
	getCriteria().export(fileName, queryModel, propertyNames.toArray(new String[] {}), propertyTitles.toArray(new String[] {}));
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
	final Result queryGenerationResult = canCreateQuery();
	if(!queryGenerationResult.isSuccessful()){
	    throw queryGenerationResult;
	}
	final List<QueryExecutionModel<T, EntityResultQueryModel<T>>> queries = queryCustomiser.getQueryGenerator(this).generateQueryModel().getQueries();
	if (queries.size() == 2) {
	    return new Pair<>(queries.get(0), queries.get(1));
	} else {
	    return new Pair<>(queries.get(0), null);
	}
    }

    public DeltaRetriever getDeltaRetriever() {
	return deltaRetriever;
    }

    @Override
    protected GridAnalysisView<T, CDTME> getAnalysisView() {
	return (GridAnalysisView<T, CDTME>)super.getAnalysisView();
    }

    protected Result getDelta(final Date old, final Date oldNow) {
	final Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> queries = enhanceByTransactionDateBoundaries(analysisQueries, old, oldNow);
	final Result result = runQuery(getUpdatedCriteria(), queries);
	return result;
    }
}
