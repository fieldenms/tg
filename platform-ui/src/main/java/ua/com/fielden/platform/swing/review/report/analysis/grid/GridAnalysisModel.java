package ua.com.fielden.platform.swing.review.report.analysis.grid;

import static ua.com.fielden.platform.report.query.generation.GridAnalysisQueryGenerator.property;
import static ua.com.fielden.platform.report.query.generation.GridAnalysisQueryGenerator.where;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.criteria.generator.impl.CriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.SinglePage;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.report.query.generation.AbstractQueryComposer;
import ua.com.fielden.platform.report.query.generation.GridAnalysisQueryGenerator;
import ua.com.fielden.platform.report.query.generation.IQueryComposer;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.review.DynamicPropertyAnalyser;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class GridAnalysisModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisReviewModel<T, CDTME, IAbstractAnalysisDomainTreeManager> {
    private final Logger logger = Logger.getLogger(getClass());
    private final long TRANSACTION_ENTITY_DELTA_DELAY = 6000;
    /** holds the last executed query */
    private Pair<IQueryComposer<T>, IQueryComposer<T>> analysisQueries;
    private final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, CDTME>> queryCustomiser;

    /******* Delta related stuff ********/
    private DeltaRetriever deltaRetriever;
    private ICentreDomainTreeManagerAndEnhancer cdtmaeCopy;
    // FIXME private final static String tdPropCopy = "_________transactionDatePropertyCopy";

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
	// private final TreeSet<T> entities;
	private final List<T> entities;
	private Timer timer;
	private Date oldNow;
	private final Comparator<T> comparator;

	public DeltaRetriever(final IPage<T> initialPage, final Comparator<T> comparator, final Date oldNow) {
	    this.oldNow = oldNow;
	    this.comparator = comparator;
	    entities = new ArrayList<T>(initialPage.data());
	    Collections.sort(entities, this.comparator);
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

		    try {
			final Date old = new Date(oldNow.getTime());
			oldNow = new Date();
			final Result result = getDelta(old, oldNow);
			if (result.isSuccessful()) {
			    final IPage<T> deltaPage = page(result);
			    if (!deltaPage.data().isEmpty()) {
				getPageHolder().newPage(produceEnhancedPage(deltaPage)); // update loaded page
			    }
			} else {
			    logger.error("DELTA RETRIEVAL HAS BEEN FAILED DUE TO [" + result.getMessage() + "].");
			    result.printStackTrace();
			}
		    } catch (final Exception e) {
			logger.error("DELTA RETRIEVAL HAS BEEN FAILED DUE TO [" + e.getMessage() + "].");
			e.printStackTrace();
		    }

		    timer = null;

		    if (getAnalysisView().isShowing()) {
			logger.info("INIT DELTA RETRIEVAL ==> isShowing == " + getAnalysisView().isShowing());
			scheduleDeltaRetrieval();
		    } else {
			logger.info("DELTA RETRIEVAL STOPPED ==> isShowing == " + getAnalysisView().isShowing());
		    }
		}
	    };
	    timer.schedule(deltaRetrivalTask, delay);
	}

	public IPage<T> produceEnhancedPage(final IPage<T> retrievedPage) {
	    for (final T entity : retrievedPage.data()) {
		final boolean removed = entities.remove(entity); // remove old entity if exists
		if (!removed) {
		    throw new IllegalStateException("There is no entity [" + entity + "] to be removed!");
		}
	    }
	    entities.addAll(retrievedPage.data());
	    Collections.sort(entities, this.comparator);

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
    protected Pair<IQueryComposer<T>, IQueryComposer<T>> enhanceByTransactionDateBoundaries(final Date oldNow, final Date now) {
	// FIXME: this method has been changed to ignore 'tdPropCopy' property!
	// FIXME: please, change the way of customising delta queries to be more general, not dependent on 'transaction date property'!

	// FIXME: this method has been changed to ignore 'tdPropCopy' property!
	// FIXME: please, change the way of customising delta queries to be more general, not dependent on 'transaction date property'!

	// FIXME: this method has been changed to ignore 'tdPropCopy' property!
	// FIXME: please, change the way of customising delta queries to be more general, not dependent on 'transaction date property'!

	// FIXME: this method has been changed to ignore 'tdPropCopy' property!
	// FIXME: please, change the way of customising delta queries to be more general, not dependent on 'transaction date property'!

	// queries.getKey()!   .getQueryModel(). setParam transDate (left == null ? [---, right] : [left, right])
	if (queryCustomiser.getQueryGenerator(this) instanceof GridAnalysisQueryGenerator) {
	    final GridAnalysisQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer> qGenerator = (GridAnalysisQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer>) queryCustomiser.getQueryGenerator(this);
	    final Class<T> root = qGenerator.entityClass();
	    // FIXME final String tdProp = AnnotationReflector.getTransactionDateProperty(root);
	    if (oldNow == null) { // RUN is performed (not Delta)
		final ICentreDomainTreeManagerAndEnhancer cdtme = qGenerator.getCdtme();
		cdtmaeCopy = EntityUtils.deepCopy(cdtme, ((CentreDomainTreeManagerAndEnhancer) cdtme).getSerialiser());
		// FIXME cdtmaeCopy.getEnhancer().addCalculatedProperty(root, "", tdProp, tdPropCopy, "A copy of transaction date property to enhance query with delta boundaries", CalculatedPropertyAttribute.NO_ATTR, "");
		// FIXME cdtmaeCopy.getEnhancer().apply();
		// FIXME cdtmaeCopy.getFirstTick().check(root, tdPropCopy, true);

		provideCustomPropertiesForQueries(cdtmaeCopy);
	    }
//	FIXME    final Class<?> tdPropType = PropertyTypeDeterminator.determinePropertyType(root, tdProp);
//FIXME	    if (EntityUtils.isDate(tdPropType)) {
//	FIXME	cdtmaeCopy.getFirstTick().setValue(root, tdPropCopy, oldNow);
//FIXME		cdtmaeCopy.getFirstTick().setValue2(root, tdPropCopy, now);
// FIXME    } else if (EntityUtils.isDateTime(tdPropType)) {
//	FIXME	cdtmaeCopy.getFirstTick().setValue(root, tdPropCopy, oldNow == null ? null : new DateTime(oldNow.getTime()));
//FIXME		cdtmaeCopy.getFirstTick().setValue2(root, tdPropCopy, new DateTime(now.getTime()));
//	FIXME    } else {
//FIXME		throw new IllegalArgumentException("The type [" + tdPropType.getSimpleName() + "] of property [" + tdProp + "] in entity [" + root + "] is not supported.");
//	FIXME}

	    final GridAnalysisQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer> newQGenerator = new GridAnalysisQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer>(root, cdtmaeCopy);
	    final List<IQueryComposer<T>> newQueries = newQGenerator.generateQueryModel().getQueries();
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
		deltaRetriever = null;
	    }
	    now = new Date();
	    final Pair<IQueryComposer<T>, IQueryComposer<T>> queries = enhanceByTransactionDateBoundaries(null, now);
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

    private Comparator<T> comparator0(final List<Pair<String, Ordering>> orderedProps, final Comparator<T> accumulatedComparator) {
	if (orderedProps.isEmpty()) {
	    return accumulatedComparator;
	} else {
	    final Pair<String, Ordering> lastPropertyOrdering = orderedProps.get(orderedProps.size() - 1);
	    final String property = lastPropertyOrdering.getKey().equals("") ? AbstractEntity.KEY : lastPropertyOrdering.getKey();
	    final Ordering ordering = lastPropertyOrdering.getValue();
	    return comparator0(new ArrayList<Pair<String, Ordering>>(orderedProps.subList(0, orderedProps.size() - 1)), new Comparator<T>() {
		@Override
		public int compare(final T o1, final T o2) {
		    final int compare = compare2(o1.get(property), o2.get(property), ordering);
		    return compare == 0 ? (accumulatedComparator == null ? 0 : accumulatedComparator.compare(o1, o2)) : compare;
		}
	    });
	}
    }

    private static int compare2(final Object value1, final Object value2, final Ordering ordering) {
	return Ordering.ASCENDING.equals(ordering) ? compare1(value1, value2) : (-compare1(value1, value2));
    }

    private static int compare1(final Object value1, final Object value2) {
	if (value1 == null) {
	    if (value2 == null) {
		return 0;
	    } else {
		return -1;
	    }
	} else {
	    if (value2 == null) {
		return 1;
	    } else {
		if (!(value1 instanceof Comparable)) {
		    throw new IllegalStateException("Property value [" + value1 + "] of entity is not comparable.");
		}
		final int compare = ((Comparable) value1).compareTo(value2);
		return compare;
	    }
	}
    }

    private Comparator<T> createComparator() {
	if (queryCustomiser.getQueryGenerator(this) instanceof GridAnalysisQueryGenerator) {
	    final GridAnalysisQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer> qGenerator = (GridAnalysisQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer>) queryCustomiser.getQueryGenerator(this);
	    final Class<T> root = qGenerator.entityClass();
	    final ICentreDomainTreeManagerAndEnhancer cdtme = qGenerator.getCdtme();
	    return comparator0(cdtme.getSecondTick().orderedProperties(root), new Comparator<T>() {
		@Override
		public int compare(final T o1, final T o2) {
		    if (o1 == null) {
			throw new IllegalArgumentException("Cannot order 'null' objects (first one).");
		    }
		    if (o2 == null) {
			throw new IllegalArgumentException("Cannot order 'null' objects (second one).");
		    }
		    if (!(o1 instanceof Comparable)) {
			throw new IllegalArgumentException("Cannot order 'non-comparable' objects.");
		    }
		    return ((Comparable) o1).compareTo(o2);
		}
	    });
	} else {
	    throw new IllegalStateException("Comparator for delta-centre cannot be used for query customiser which type is not GridAnalysisQueryGenerator.");
	}
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
    private Result runQuery(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria, final Pair<IQueryComposer<T>, IQueryComposer<T>> analysisQueries) {
	final int pageSize = getAnalysisView().getPageSize();
	final IPage<T> newPage;
	try {
	    if(analysisQueries.getValue() == null) {
		newPage = criteria.firstPage(analysisQueries.getKey().composeQuery(), pageSize);
	    } else {
		newPage = criteria.firstPage(analysisQueries.getKey().composeQuery(), analysisQueries.getValue().composeQuery(), pageSize);
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

    /**
     * Returns entities by their id's using the current query and fetch model.
     * Returns empty list if there are entities those satisfies current query model.
     *
     * @param entities
     * @return
     */
    protected List<T> getUpdatedEntitiesById(final List<Long> ids) {
	final boolean isSynthetic = Reflector.isSynthetic(getCriteria().getEntityClass());
	final boolean isEntityKey = EntityUtils.isEntityType(AnnotationReflector.getKeyType(getCriteria().getEntityClass()));
	if (analysisQueries != null) {
	    if(!isSynthetic && !ids.isEmpty()) {
		return getCriteria().getAllEntities(previousQueryWithIdParam("id", ids).composeQuery());
	    } else if(isSynthetic && isEntityKey && !ids.isEmpty()) {
		return getCriteria().getAllEntities(previousQueryWithIdParam("key.id", ids).composeQuery());
	    } else if(isSynthetic && !isEntityKey && ids.isEmpty()){
		reExecuteAnalysisQuery();
	    }
	}
	return new ArrayList<>();
    }

    private IQueryComposer<T> previousQueryWithIdParam(final String idParam, final List<Long> ids) {
	if(analysisQueries != null && analysisQueries.getKey().getQuery() != null) {
	    return new AbstractQueryComposer<T>() {

		@Override
		public ICompleted<T> getQuery() {
		    return where(analysisQueries.getKey().getQuery()).prop(property(idParam)).in().values(ids.toArray());
		}

		@Override
		public fetch<T> getFetch() {
		    return analysisQueries.getKey().getFetch();
		}

		@Override
		public OrderingModel getOrdering() {
		    return null;
		}

		@Override
		public Map<String, Object> getParams() {
		    return analysisQueries.getKey().getParams();
		}
	    };
	}
	throw new IllegalStateException("The previous analysis query wasn't specified!");
    }

    private Result canCreateQuery() {
	return getCriteria().isValid();
    }

    @Override
    protected Result exportData(final String fileName) throws IOException {
	final IQueryComposer<T> queryModel;
	try {
	    queryModel = createQueryExecutionModel().getKey();
	} catch(final Result result){
	    return result;
	}
	final PropertyTableModel<T> tableModel = getAnalysisView().getEgiPanel().getEgi().getActualModel();
	final Pair<List<String>, List<String>> propertyNamesAndTitles = propertyNamesAndTitles(tableModel);
	getCriteria().export(fileName, queryModel.composeQuery(), propertyNamesAndTitles.getKey().toArray(new String[] {}), propertyNamesAndTitles.getValue().toArray(new String[] {}));
	return Result.successful(this);
    }

    protected Pair<List<String>, List<String>> propertyNamesAndTitles(final PropertyTableModel<T> tableModel) {
	final List<String> propertyNames = new ArrayList<String>(tableModel.getPropertyColumnMappings().size());
	final List<String> propertyTitles = new ArrayList<String>(tableModel.getPropertyColumnMappings().size());
	for (final AbstractPropertyColumnMapping<T> mapping : tableModel.getPropertyColumnMappings()) {
	    final DynamicPropertyAnalyser propertyAnalyser = new DynamicPropertyAnalyser(getCriteria().getManagedType(), mapping.getPropertyName());
	    propertyNames.add(propertyAnalyser.getCriteriaFullName());
	    propertyTitles.add(mapping.getPropertyTitle());
	}
	return new Pair<>(propertyNames, propertyTitles);
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
    public final Pair<IQueryComposer<T>, IQueryComposer<T>> createQueryExecutionModel(){
	final Result queryGenerationResult = canCreateQuery();
	if(!queryGenerationResult.isSuccessful()){
	    throw queryGenerationResult;
	}
	final List<IQueryComposer<T>> queries = queryCustomiser.getQueryGenerator(this).generateQueryModel().getQueries();
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
	final Pair<IQueryComposer<T>, IQueryComposer<T>> queries = enhanceByTransactionDateBoundaries(old, oldNow);
	final Result result = runQuery(getUpdatedCriteria(), queries);
	return result;
    }
}
