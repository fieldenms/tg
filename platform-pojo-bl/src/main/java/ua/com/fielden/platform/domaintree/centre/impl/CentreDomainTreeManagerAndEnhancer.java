package ua.com.fielden.platform.domaintree.centre.impl;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyException;
import ua.com.fielden.platform.domaintree.ILocatorManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.PivotDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.SentinelDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.SentinelDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager.AddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager.AddToResultTickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.TickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.AbstractTickRepresentation;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel.GroupingPeriods;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * Criteria (entity-centre) domain tree manager with "power" of managing domain with calculated properties. The calculated properties can be managed exactly as simple properties.<br>
 *
 * @author TG Team
 *
 */
public class CentreDomainTreeManagerAndEnhancer extends AbstractDomainTreeManagerAndEnhancer implements ICentreDomainTreeManagerAndEnhancer {
    private final transient ISerialiser serialiser;
    private final transient Logger logger = Logger.getLogger(getClass());
    private final LinkedHashMap<String, IAbstractAnalysisDomainTreeManager> persistentAnalyses;
    private final transient LinkedHashMap<String, IAbstractAnalysisDomainTreeManager> currentAnalyses;
    private final transient LinkedHashMap<String, IAbstractAnalysisDomainTreeManager> freezedAnalyses;

    private final transient List<IAnalysisListener> analysisListeners;

    /**
     * A <i>manager with enhancer</i> constructor for the first time instantiation.
     */
    public CentreDomainTreeManagerAndEnhancer(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, new CentreDomainTreeManager(serialiser, AbstractDomainTree.validateRootTypes(rootTypes)), new DomainTreeEnhancer(serialiser, AbstractDomainTree.validateRootTypes(rootTypes)), new HashMap<String, IAbstractAnalysisDomainTreeManager>(), new HashMap<String, IAbstractAnalysisDomainTreeManager>(), new HashMap<String, IAbstractAnalysisDomainTreeManager>());
    }

    /**
     * A <i>manager with enhancer</i> constructor with transient analyses (current and freezed).
     */
    protected CentreDomainTreeManagerAndEnhancer(final ISerialiser serialiser, final CentreDomainTreeManager base, final IDomainTreeEnhancer enhancer, final Map<String, IAbstractAnalysisDomainTreeManager> persistentAnalyses, final Map<String, IAbstractAnalysisDomainTreeManager> currentAnalyses, final Map<String, IAbstractAnalysisDomainTreeManager> freezedAnalyses) {
	super(base, enhancer);

	this.serialiser = serialiser;
	this.persistentAnalyses = new LinkedHashMap<String, IAbstractAnalysisDomainTreeManager>();
	this.persistentAnalyses.putAll(persistentAnalyses);
	// VERY IMPORTANT : Please note that deepCopy operation is not applicable here, because deserialisation process cannot be mixed with serialisation.
	// This constructor is explicitly used in deserialisation.
	this.currentAnalyses = new LinkedHashMap<String, IAbstractAnalysisDomainTreeManager>();
	this.currentAnalyses.putAll(currentAnalyses);
	this.freezedAnalyses = new LinkedHashMap<String, IAbstractAnalysisDomainTreeManager>();
	this.freezedAnalyses.putAll(freezedAnalyses);

	this.analysisListeners = new ArrayList<IAnalysisListener>();

	for (final IAbstractAnalysisDomainTreeManager analysisManager : this.persistentAnalyses.values()) {
	    initAnalysisManagerReferencesOn(analysisManager, this);
	}
	for (final IAbstractAnalysisDomainTreeManager analysisManager : this.currentAnalyses.values()) {
	    initAnalysisManagerReferencesOn(analysisManager, this);
	}
	for (final IAbstractAnalysisDomainTreeManager analysisManager : this.freezedAnalyses.values()) {
	    initAnalysisManagerReferencesOn(analysisManager, this);
	}
    }

    @Override
    protected IDomainTreeEnhancer enhancer() {
	return super.enhancer();
    }

    /**
     * Creates a domain tree enhancer wrapper that takes care about population of domain tree changes (calc props) in representation "included properties"
     * (which triggers other population like manager's "checked properties" automatically).
     *
     * @return
     */
    @Override
    protected CentreDomainTreeEnhancerWithPropertiesPopulation createEnhancerWrapperWithPropertiesPopulation() {
	return new CentreDomainTreeEnhancerWithPropertiesPopulation((DomainTreeEnhancer) enhancer(), this);
    }

    /**
     * The {@link DomainTreeEnhancer} wrapper that reflects the changes in manager and also in children analyses.
     *
     * @author TG Team
     *
     */
    protected static class CentreDomainTreeEnhancerWithPropertiesPopulation extends DomainTreeEnhancerWithPropertiesPopulation {
	private final CentreDomainTreeManagerAndEnhancer mgrAndEnhancer;

	/**
	 * A {@link DomainTreeEnhancerWithPropertiesPopulation} constructor which requires a base implementations of {@link DomainTreeEnhancer} and {@link AbstractDomainTreeRepresentation}.
	 *
	 * @param baseEnhancer
	 * @param dtr
	 */
	protected CentreDomainTreeEnhancerWithPropertiesPopulation(final DomainTreeEnhancer baseEnhancer, final CentreDomainTreeManagerAndEnhancer mgrAndEnhancer) {
	    super(baseEnhancer, (DomainTreeRepresentationAndEnhancer) mgrAndEnhancer.getRepresentation());

	    this.mgrAndEnhancer = mgrAndEnhancer;
	}

	@Override
	protected void beforeApplyPopulation(final Set<Pair<Class<?>, String>> retainedAndSignificantlyChanged, final Set<Pair<Class<?>, String>> removed) {
	    // Iterate through all analyses and all properties to be removed (or retained but significantly changed).
	    // Find if there is at least one property that used in any analysis and which is trying to be removed / significantly changed.
	    // If it exists -- throw illegal "removal exception"!
	    // Otherwise -- remove all meta-state!
	    for (final Pair<Class<?>, String> rootAndRemovalProp : union(removed, retainedAndSignificantlyChanged)) {
		for (final String analysisKey : mgrAndEnhancer.analysisKeys()) {
		    final IAbstractAnalysisDomainTreeManager analysis = mgrAndEnhancer.getAnalysisManager(analysisKey);
		    final Class<?> root = rootAndRemovalProp.getKey();
		    final String property = rootAndRemovalProp.getValue();
		    if (!analysis.getRepresentation().isExcludedImmutably(root, property) && !analysis.getRepresentation().getFirstTick().isDisabledImmutably(root, property) && analysis.getFirstTick().isChecked(root, property) || //
			    !analysis.getRepresentation().isExcludedImmutably(root, property) && !analysis.getRepresentation().getSecondTick().isDisabledImmutably(root, property) && analysis.getSecondTick().isChecked(root, property)) {
			throw new IncorrectCalcPropertyException("Can not remove (or significantly change) a property [" + property + "] in type [" + root + "] which is used as distribution or aggregation property in analysis [" + analysisKey + "].");
		    }
		}
	    }
	    for (final Pair<Class<?>, String> rootAndRemovalProp : union(removed, retainedAndSignificantlyChanged)) {
		for (final String analysisKey : mgrAndEnhancer.analysisKeys()) {
		    removeMetaStateFromPropertyToBeRemoved(rootAndRemovalProp.getKey(), rootAndRemovalProp.getValue(), mgrAndEnhancer.getAnalysisManager(analysisKey).getRepresentation());
		}
	    }
	    super.beforeApplyPopulation(retainedAndSignificantlyChanged, removed);
	}

	@Override
	protected void afterApplyPopulation(final Set<Pair<Class<?>, String>> retainedAndSignificantlyChanged, final Set<Pair<Class<?>, String>> neew) {
	    super.afterApplyPopulation(retainedAndSignificantlyChanged, neew);

	    for (final Pair<Class<?>, String> rootAndProp : union(neew, retainedAndSignificantlyChanged)) {
		for (final String analysisKey : mgrAndEnhancer.analysisKeys()) {
		    populateMetaStateForActuallyAddedNewProperty(rootAndProp.getKey(), rootAndProp.getValue(), mgrAndEnhancer.getAnalysisManager(analysisKey).getRepresentation());
		}
	    }
	}
    }

    private static IAbstractAnalysisDomainTreeManager copyAnalysis(final IAbstractAnalysisDomainTreeManager analysisManager, final ISerialiser serialiser) {
	if (analysisManager == null) {
	    return null;
	}
	final IAbstractAnalysisDomainTreeManager copy = EntityUtils.deepCopy(analysisManager, serialiser);
	return initAnalysisManagerReferencesOn(copy, analysisManager.parentCentreDomainTreeManager());
    }

    public static IAbstractAnalysisDomainTreeManager initAnalysisManagerReferencesOn(final IAbstractAnalysisDomainTreeManager analysisManager, final ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager) {
	final AbstractAnalysisDomainTreeManager mgr = (AbstractAnalysisDomainTreeManager) analysisManager;

	initAnalysisManagerReferencesOn(mgr.getRepresentation(), parentCentreDomainTreeManager);

	// initialise the references on THIS instance in AbstractAnalysisDomainTreeManager, its both ticks, its representation and its both ticks
	try {
	    setValueForLazyField(mgr, parentCentreDomainTreeManager);
	    setValueForLazyField(mgr.getFirstTick(), parentCentreDomainTreeManager);
	    setValueForLazyField(mgr.getSecondTick(), parentCentreDomainTreeManager);
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new IllegalStateException(e);
	}
	return analysisManager;
    }

    public static IAbstractAnalysisDomainTreeRepresentation initAnalysisManagerReferencesOn(final IAbstractAnalysisDomainTreeRepresentation analysisRepresentation, final ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager) {
	final AbstractAnalysisDomainTreeRepresentation dtr = (AbstractAnalysisDomainTreeRepresentation) analysisRepresentation;

	// initialise the references on THIS instance in AbstractAnalysisDomainTreeManager's representation and its both ticks
	try {
	    setValueForLazyField(dtr, parentCentreDomainTreeManager);
	    
	    // load analysis property tree! it depends on "parent centre domain type"!
	    for (final Class<?> type : dtr.includedPropertiesTypes()) {
		dtr.includedPropertiesMutable(type);
	    }

	    setValueForLazyField(dtr.getFirstTick(), parentCentreDomainTreeManager);
	    setValueForLazyField(dtr.getSecondTick(), parentCentreDomainTreeManager);
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new IllegalStateException(e);
	}
	return dtr;
    }


    private static void setValueForLazyField(final Object mgr, final ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager) throws IllegalAccessException {
	final Field parentCentreDomainTreeManagerField = Finder.findFieldByName(mgr.getClass(), "parentCentreDomainTreeManager");
	final boolean isAccessible = parentCentreDomainTreeManagerField.isAccessible();
	parentCentreDomainTreeManagerField.setAccessible(true);
	parentCentreDomainTreeManagerField.set(mgr, parentCentreDomainTreeManager);
	parentCentreDomainTreeManagerField.setAccessible(isAccessible);
    }

    @Override
    public boolean addAnalysisListener(final IAnalysisListener listener) {
	return analysisListeners.add(listener);
    }

    @Override
    public boolean removeAnalysisListener(final IAnalysisListener listener) {
	return analysisListeners.remove(listener);
    }

    @Override
    public void initAnalysisManagerByDefault(final String name, final AnalysisType analysisType) {
	if (isFreezedAnalysisManager(name)) {
	    error("Unable to Init analysis instance if it is freezed for title [" + name + "].");
	}
	if (getAnalysisManager(name) != null) {
	    throw new IllegalArgumentException("The analysis with name [" + name + "] already exists.");
	}
	// create a new instance and put to "current" map
	if (AnalysisType.PIVOT.equals(analysisType)) {
	    currentAnalyses.put(name, initAnalysisManagerReferencesOn(new PivotDomainTreeManager(getSerialiser(), getRepresentation().rootTypes()), this));
	} if (AnalysisType.SIMPLE.equals(analysisType)) {
	    currentAnalyses.put(name, initAnalysisManagerReferencesOn(new AnalysisDomainTreeManager(getSerialiser(), getRepresentation().rootTypes()), this));
	} if (AnalysisType.SENTINEL.equals(analysisType)) {
	    provideSentinelAnalysesAggregationProperty(getRepresentation().rootTypes());
	    currentAnalyses.put(name, initAnalysisManagerReferencesOn(new SentinelDomainTreeManager(getSerialiser(), getRepresentation().rootTypes()), this));
	    final SentinelDomainTreeManager sdtm = (SentinelDomainTreeManager) getAnalysisManager(name);
	    sdtm.provideMetaStateForCountOfSelfDashboardProperty();
	} if (AnalysisType.LIFECYCLE.equals(analysisType)) {
	    provideLifecycleAnalysesDatePeriodProperties(getRepresentation().rootTypes());
	    currentAnalyses.put(name, initAnalysisManagerReferencesOn(new LifecycleDomainTreeManager(getSerialiser(), getRepresentation().rootTypes()), this));
	    final LifecycleDomainTreeManager ldtm = (LifecycleDomainTreeManager) getAnalysisManager(name);
	    ldtm.provideMetaStateForLifecycleAnalysesDatePeriodProperties();
	}
	// fire "initialised" event
	if (getAnalysisManager(name) != null) {
	    for (final IAnalysisListener listener : analysisListeners) {
		listener.propertyStateChanged(null, name, true, null);
	    }
	}
    }

    /**
     * Enhances centre manager domain to include COUNT_OF_SELF_DASHBOARD property, which will be used for sentinel analyses.
     *
     * @param rootType
     */
    public void provideSentinelAnalysesAggregationProperty(final Set<Class<?>> rootTypes) {
	for (final Class<?> rootType : rootTypes) {
	    // add "count of self" calculated property that is essential for "sentinel" analyses
	    try {
		getEnhancer().getCalculatedProperty(rootType, SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD);
	    } catch (final IncorrectCalcPropertyException e) {
		getEnhancer().addCalculatedProperty(rootType, "", "COUNT(SELF)", "Count of self (Dashboard)", "This calculated property is used for sentinels as aggregation function that calculates a number of entities by each status.", CalculatedPropertyAttribute.NO_ATTR, "SELF");
		getEnhancer().apply();
	    }
	}
    }

    /**
     * Enhances centre manager domain to include "Date Period" distribution properties, which will be used for lifecycle analyses.
     *
     * @param rootType
     */
    public void provideLifecycleAnalysesDatePeriodProperties(final Set<Class<?>> rootTypes) {
	for (final Class<?> rootType : rootTypes) {
	    // add "Date Period" distribution calculated properties that are essential for "lifecycle" analyses
	    for (final GroupingPeriods period : GroupingPeriods.values()) {
		addDatePeriodProperty(rootType, period);
	    }
	}
    }

    private void addDatePeriodProperty(final Class<?> rootType, final GroupingPeriods period) {
	try {
	    getEnhancer().getCalculatedProperty(rootType, period.getPropertyName());
	} catch (final IncorrectCalcPropertyException e) {
	    final String expr = "\"This is date period String property, which should be enabled for distribution\"";
	    final String descAddition = "\nThis calculated property is used for lifecycle as distribution property (by time).";
	    final CalculatedProperty calc = (CalculatedProperty) getEnhancer().addCalculatedProperty(rootType, "", expr, period.getTitle() /* period.getPropertyName() */, period.getDesc() + descAddition, CalculatedPropertyAttribute.NO_ATTR, "SELF");

	    // TODO tricky setting!
	    calc.setNameVeryTricky(period.getPropertyName());

	    getEnhancer().apply();
	}
    }

    @Override
    public void discardAnalysisManager(final String name) {
	final boolean wasInitialised = getAnalysisManager(name) != null;
	final IAbstractAnalysisDomainTreeManager dtm = copyAnalysis(persistentAnalyses.get(name), getSerialiser());
	if (dtm != null) {
	    currentAnalyses.put(name, dtm);
	} else {
	    currentAnalyses.remove(name);
	}

	if (isFreezedAnalysisManager(name)) {
	    unfreeze(name);
	}
	// fire "removed" event
	if (wasInitialised && (getAnalysisManager(name) == null)) {
	    for (final IAnalysisListener listener : analysisListeners) {
		listener.propertyStateChanged(null, name, false, null);
	    }
	}
    }

    @Override
    public void acceptAnalysisManager(final String name) {
	if (isFreezedAnalysisManager(name)) {
	    unfreeze(name);

	    currentAnalyses.put(name, copyAnalysis(currentAnalyses.get(name), getSerialiser())); // this is necessary to dispose current manager with listeners and get equal "fresh" instance
	} else {
	    final IAbstractAnalysisDomainTreeManager dtm = copyAnalysis(currentAnalyses.get(name), getSerialiser());
	    if (dtm != null) {
		persistentAnalyses.put(name, dtm);
	    } else {
		persistentAnalyses.remove(name);
	    }
	}
    }

    @Override
    public boolean isChangedAnalysisManager(final String name) {
	return !EntityUtils.equalsEx(currentAnalyses.get(name), persistentAnalyses.get(name));
    }

    @Override
    public void removeAnalysisManager(final String name) {
	if (isFreezedAnalysisManager(name)) {
	    error("Unable to remove analysis instance if it is freezed for title [" + name + "].");
	}
	final IAbstractAnalysisDomainTreeManager mgr = getAnalysisManager(name);
	if (mgr == null) {
	    throw new IllegalArgumentException("The unknown analysis with name [" + name + "] can not be removed.");
	}
	currentAnalyses.remove(name);
	acceptAnalysisManager(name);

	// fire "removed" event
	if (getAnalysisManager(name) == null) {
	    for (final IAnalysisListener listener : analysisListeners) {
		listener.propertyStateChanged(null, name, false, null);
	    }
	}
    }

    @Override
    public IAbstractAnalysisDomainTreeManager getAnalysisManager(final String name) {
	return currentAnalyses.get(name);
    }

    @Override
    public void freezeAnalysisManager(final String name) {
	if (isFreezedAnalysisManager(name)) {
	    error("Unable to freeze the analysis instance more than once for title [" + name + "].");
	}
	notInitiliasedError(currentAnalyses.get(name), name);
	notInitiliasedError(persistentAnalyses.get(name), name);

	freezedAnalyses.put(name, persistentAnalyses.remove(name));
	persistentAnalyses.put(name, copyAnalysis(currentAnalyses.get(name), getSerialiser()));
	currentAnalyses.put(name, copyAnalysis(currentAnalyses.get(name), getSerialiser())); // this is necessary to dispose current manager with listeners and get equal "fresh" instance
    }

    /**
     * Returns <code>true</code> if the analysis instance is in 'freezed' state, <code>false</code> otherwise.
     *
     * @param name
     * @return
     */
    @Override
    public boolean isFreezedAnalysisManager(final String name) {
	return freezedAnalyses.get(name) != null;
    }

    /**
     * Unfreezes the centre instance that is currently freezed.
     *
     * @param root
     * @param name
     */
    protected void unfreeze(final String name) {
	if (!isFreezedAnalysisManager(name)) {
	    error("Unable to unfreeze the analysis instance that is not 'freezed' for title [" + name + "].");
	}
	persistentAnalyses.put(name, freezedAnalyses.remove(name));
    }

    /**
     * Throws an error when the instance is <code>null</code> (not initialised).
     *
     * @param mgr
     * @param root
     * @param name
     */
    private void notInitiliasedError(final IAbstractAnalysisDomainTreeManager mgr, final String name) {
	if (mgr == null) {
	    error("Unable to perform this operation on the analysis instance, that wasn't initialised, for title [" + name + "].");
	}
    }

    /**
     * Logs and throws an {@link IllegalArgumentException} error with specified message.
     *
     * @param message
     */
    private void error(final String message) {
	logger.error(message);
	throw new IllegalArgumentException(message);
    }

    @Override
    public List<String> analysisKeys() {
	return new ArrayList<String>(currentAnalyses.keySet());
    }


    @Override
    protected AddToCriteriaTickManagerAndEnhancer createFirstTick(final TickManager base) {
	return new AddToCriteriaTickManagerAndEnhancer(base);
    }

    @Override
    protected AddToResultTickManagerAndEnhancer createSecondTick(final TickManager base) {
	return new AddToResultTickManagerAndEnhancer(base);
    }

    @Override
    protected DomainTreeRepresentationAndEnhancer createRepresentation(final AbstractDomainTreeRepresentation base) {
	return new CentreDomainTreeRepresentationAndEnhancer(base);
    }

    @Override
    public ICentreDomainTreeManager base() {
	return (ICentreDomainTreeManager) super.base();
    }

    @Override
    public IAddToCriteriaTickManager getFirstTick() {
	return (IAddToCriteriaTickManager) super.getFirstTick();
    }

    @Override
    public IAddToResultTickManager getSecondTick() {
	return (IAddToResultTickManager) super.getSecondTick();
    }

    /**
     * Overridden to take into account calculated properties.
     *
     * @author TG Team
     *
     */
    /* TODO reduce visibility */
    public class AddToCriteriaTickManagerAndEnhancer extends TickManagerAndEnhancer implements IAddToCriteriaTickManager, ILocatorManager {

	private AddToCriteriaTickManagerAndEnhancer(final TickManager base) {
	    super(base);
	}

	@Override
	public /* TODO reduce visibility */ AddToCriteriaTickManager base() {
	    return (AddToCriteriaTickManager) super.base();
	}

	@Override
	public void refreshLocatorManager(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().refreshLocatorManager(enhancer().getManagedType(root), property);
	}

	@Override
	public void resetLocatorManagerToDefault(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().resetLocatorManagerToDefault(enhancer().getManagedType(root), property);
	}

	@Override
	public void acceptLocatorManager(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().acceptLocatorManager(enhancer().getManagedType(root), property);
	}

	@Override
	public void discardLocatorManager(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().discardLocatorManager(enhancer().getManagedType(root), property);
	}

	@Override
	public void saveLocatorManagerGlobally(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().saveLocatorManagerGlobally(enhancer().getManagedType(root), property);
	}

	@Override
	public void freezeLocatorManager(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().freezeLocatorManager(enhancer().getManagedType(root), property);
	}

	@Override
	public ILocatorDomainTreeManagerAndEnhancer getLocatorManager(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getLocatorManager(enhancer().getManagedType(root), property);
	}

	@Override
	public Pair<Phase, Type> phaseAndTypeOfLocatorManager(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().phaseAndTypeOfLocatorManager(enhancer().getManagedType(root), property);
	}

	@Override
	public boolean isChangedLocatorManager(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().isChangedLocatorManager(enhancer().getManagedType(root), property);
	}

	@Override
	public List<Pair<Class<?>, String>> locatorKeys() {
	    // inject an enhanced type into method implementation
	    return base().locatorKeys();
	}

	@Override
	public int getColumnsNumber() {
	    // inject an enhanced type into method implementation
	    return base().getColumnsNumber();
	}

	@Override
	public IAddToCriteriaTickManager setColumnsNumber(final int columnsNumber) {
	    // inject an enhanced type into method implementation
	    base().setColumnsNumber(columnsNumber);
	    return this;
	}

	@Override
	public Object getValue(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getValue(enhancer().getManagedType(root), property);
	}

	@Override
	public boolean isValueEmpty(final Class<?> root, final String property) {
	    return base().isValueEmpty(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setValue(final Class<?> root, final String property, final Object value) {
	    // inject an enhanced type into method implementation
	    base().setValue(enhancer().getManagedType(root), property, value);
	    return this;
	}

	@Override
	public Object getValue2(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getValue2(enhancer().getManagedType(root), property);
	}

	@Override
	public boolean is2ValueEmpty(final Class<?> root, final String property) {
	    return base().is2ValueEmpty(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setValue2(final Class<?> root, final String property, final Object value2) {
	    // inject an enhanced type into method implementation
	    base().setValue2(enhancer().getManagedType(root), property, value2);
	    return this;
	}

	@Override
	public boolean addPropertyValueListener(final IPropertyValueListener listener) {
	    return base().addPropertyValueListener(listener);
	}

	@Override
	public boolean removePropertyValueListener(final IPropertyValueListener listener) {
	    return base().removePropertyValueListener(listener);
	}

	@Override
	public boolean addPropertyValue2Listener(final IPropertyValueListener listener) {
	    return base().addPropertyValue2Listener(listener);
	}

	@Override
	public boolean removePropertyValue2Listener(final IPropertyValueListener listener) {
	    return base().removePropertyValue2Listener(listener);
	}


	@Override
	public Boolean getExclusive(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getExclusive(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setExclusive(final Class<?> root, final String property, final Boolean exclusive) {
	    // inject an enhanced type into method implementation
	    base().setExclusive(enhancer().getManagedType(root), property, exclusive);
	    return this;
	}

	@Override
	public Boolean getExclusive2(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getExclusive2(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setExclusive2(final Class<?> root, final String property, final Boolean exclusive2) {
	    // inject an enhanced type into method implementation
	    base().setExclusive2(enhancer().getManagedType(root), property, exclusive2);
	    return this;
	}

	@Override
	public DateRangePrefixEnum getDatePrefix(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getDatePrefix(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setDatePrefix(final Class<?> root, final String property, final DateRangePrefixEnum datePrefix) {
	    // inject an enhanced type into method implementation
	    base().setDatePrefix(enhancer().getManagedType(root), property, datePrefix);
	    return this;
	}

	@Override
	public MnemonicEnum getDateMnemonic(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getDateMnemonic(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setDateMnemonic(final Class<?> root, final String property, final MnemonicEnum dateMnemonic) {
	    // inject an enhanced type into method implementation
	    base().setDateMnemonic(enhancer().getManagedType(root), property, dateMnemonic);
	    return this;
	}

	@Override
	public Boolean getAndBefore(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getAndBefore(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setAndBefore(final Class<?> root, final String property, final Boolean andBefore) {
	    // inject an enhanced type into method implementation
	    base().setAndBefore(enhancer().getManagedType(root), property, andBefore);
	    return this;
	}

	@Override
	public Boolean getOrNull(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getOrNull(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setOrNull(final Class<?> root, final String property, final Boolean orNull) {
	    // inject an enhanced type into method implementation
	    base().setOrNull(enhancer().getManagedType(root), property, orNull);
	    return this;
	}

	@Override
	public Boolean getNot(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getNot(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setNot(final Class<?> root, final String property, final Boolean not) {
	    // inject an enhanced type into method implementation
	    base().setNot(enhancer().getManagedType(root), property, not);
	    return this;
	}

	@Override
	public Set<Class<?>> rootTypes() {
	    return base().rootTypes();
	}

	@Override
	protected void insertCheckedProperty(final Class<?> root, final String property, final int index) {
	    // inject an enhanced type into method implementation
	    base().insertCheckedProperty(enhancer().getManagedType(root), property, index);
	}

	@Override
	protected void removeCheckedProperty(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().removeCheckedProperty(enhancer().getManagedType(root), property);
	}
    }

    /**
     * Overridden to take into account calculated properties.
     *
     * @author TG Team
     *
     */
    protected class AddToResultTickManagerAndEnhancer extends TickManagerAndEnhancer implements IAddToResultTickManager {
	private AddToResultTickManagerAndEnhancer(final TickManager base) {
	    super(base);
	}

	@Override
	protected AddToResultTickManager base() {
	    return (AddToResultTickManager) super.base();
	}

	@Override
	public List<Pair<String, Ordering>> orderedProperties(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    return base().orderedProperties(enhancer().getManagedType(root));
	}

	@Override
	public void toggleOrdering(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().toggleOrdering(enhancer().getManagedType(root), property);
	}

	@Override
	public boolean addPropertyOrderingListener(final IPropertyOrderingListener listener) {
	    // TODO Auto-generated method stub
	    throw new UnsupportedOperationException("IPropertyOrderingListener is currently unsupported for CentreDomainTreeManager's second tick.");
	}

	@Override
	public boolean removePropertyOrderingListener(final IPropertyOrderingListener listener) {
	    // TODO Auto-generated method stub
	    throw new UnsupportedOperationException("IPropertyOrderingListener is currently unsupported for CentreDomainTreeManager's second tick.");
	}

	@Override
	public int getWidth(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getWidth(enhancer().getManagedType(root), property);
	}

	@Override
	public void setWidth(final Class<?> root, final String property, final int width) {
	    // inject an enhanced type into method implementation
	    base().setWidth(enhancer().getManagedType(root), property, width);
	}
    }

    @Override
    public ICentreDomainTreeRepresentation getRepresentation() {
	return (ICentreDomainTreeRepresentation) super.getRepresentation();
    }

    /**
     * Overridden to take into account calculated properties.
     *
     * @author TG Team
     *
     */
    protected class CentreDomainTreeRepresentationAndEnhancer extends DomainTreeRepresentationAndEnhancer implements ICentreDomainTreeRepresentation {
	protected CentreDomainTreeRepresentationAndEnhancer(final AbstractDomainTreeRepresentation base) {
	    super(base);
	}

	@Override
	protected IAddToCriteriaTickRepresentation createFirstTick(final AbstractTickRepresentation base) {
	    return new AddToCriteriaTickRepresentationAndEnhancer(base);
	}

	@Override
	protected IAddToResultTickRepresentation createSecondTick(final AbstractTickRepresentation base) {
	    return new AddToResultTickRepresentationAndEnhancer(base);
	}

	/**
	 * Overridden to take into account calculated properties.
	 *
	 * @author TG Team
	 *
	 */
	protected class AddToCriteriaTickRepresentationAndEnhancer extends TickRepresentationAndEnhancer implements IAddToCriteriaTickRepresentation {

	    protected AddToCriteriaTickRepresentationAndEnhancer(final AbstractTickRepresentation base) {
		super(base);
	    }

	    @Override
	    protected IAddToCriteriaTickRepresentation base() {
		return (IAddToCriteriaTickRepresentation) super.base();
	    }

	    @Override
	    public Object getValueByDefault(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		return base().getValueByDefault(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public Object getEmptyValueFor(final Class<?> root, final String property) {
		return base().getEmptyValueFor(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public IAddToCriteriaTickRepresentation setValueByDefault(final Class<?> root, final String property, final Object value) {
		// inject an enhanced type into method implementation
		base().setValueByDefault(enhancer().getManagedType(root), property, value);
		return this;
	    }

	    @Override
	    public Object getValue2ByDefault(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		return base().getValue2ByDefault(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public Object get2EmptyValueFor(final Class<?> root, final String property) {
		return base().get2EmptyValueFor(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public IAddToCriteriaTickRepresentation setValue2ByDefault(final Class<?> root, final String property, final Object value2) {
		// inject an enhanced type into method implementation
		base().setValue2ByDefault(enhancer().getManagedType(root), property, value2);
		return this;
	    }

	}

	/**
	 * Overridden to take into account calculated properties.
	 *
	 * @author TG Team
	 *
	 */
	protected class AddToResultTickRepresentationAndEnhancer extends TickRepresentationAndEnhancer implements IAddToResultTickRepresentation {
	    protected AddToResultTickRepresentationAndEnhancer(final AbstractTickRepresentation base) {
		super(base);
	    }

	    @Override
	    protected IAddToResultTickRepresentation base() {
		return (IAddToResultTickRepresentation) super.base();
	    }

	    @Override
	    public boolean isOrderingDisabledImmutably(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		return base().isOrderingDisabledImmutably(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public void disableOrderingImmutably(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		base().disableOrderingImmutably(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public List<Pair<String, Ordering>> orderedPropertiesByDefault(final Class<?> root) {
		// inject an enhanced type into method implementation
		return base().orderedPropertiesByDefault(enhancer().getManagedType(root));
	    }

	    @Override
	    public void setOrderedPropertiesByDefault(final Class<?> root, final List<Pair<String, Ordering>> orderedPropertiesByDefault) {
		// inject an enhanced type into method implementation
		base().setOrderedPropertiesByDefault(enhancer().getManagedType(root), orderedPropertiesByDefault);
	    }

	    @Override
	    public int getWidthByDefault(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		return base().getWidthByDefault(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public void setWidthByDefault(final Class<?> root, final String property, final int width) {
		// inject an enhanced type into method implementation
		base().setWidthByDefault(enhancer().getManagedType(root), property, width);
	    }
	}

	@Override
	public IAddToCriteriaTickRepresentation getFirstTick() {
	    return (IAddToCriteriaTickRepresentation) super.getFirstTick();
	}

	@Override
	public IAddToResultTickRepresentation getSecondTick() {
	    return (IAddToResultTickRepresentation) super.getSecondTick();
	}
    }

    @Override
    public boolean isRunAutomatically() {
	return base().isRunAutomatically();
    }

    @Override
    public ICentreDomainTreeManager setRunAutomatically(final boolean runAutomatically) {
	base().setRunAutomatically(runAutomatically);
	return this;
    }

    protected Map<String, IAbstractAnalysisDomainTreeManager> persistentAnalyses() {
	return persistentAnalyses;
    }

    protected Map<String, IAbstractAnalysisDomainTreeManager> currentAnalyses() {
	return currentAnalyses;
    }

    protected Map<String, IAbstractAnalysisDomainTreeManager> freezedAnalyses() {
	return freezedAnalyses;
    }

    /**
     * A specific Kryo serialiser for {@link CentreDomainTreeManagerAndEnhancer} with transient "current" and "freezed" analyses.
     *
     * @author TG Team
     *
     */
    public static class CentreDomainTreeManagerAndEnhancerWithTransientAnalysesSerialiser extends TgSimpleSerializer<CentreDomainTreeManagerAndEnhancer> {
	public CentreDomainTreeManagerAndEnhancerWithTransientAnalysesSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public CentreDomainTreeManagerAndEnhancer read(final ByteBuffer buffer) {
	    final CentreDomainTreeManager base = readValue(buffer, CentreDomainTreeManager.class);
	    final DomainTreeEnhancer enhancer = readValue(buffer, DomainTreeEnhancer.class);

	    final Map<String, IAbstractAnalysisDomainTreeManager> persistentAnalyses = readValue(buffer, LinkedHashMap.class);
//	    for (final Entry<String, IAbstractAnalysisDomainTreeManagerAndEnhancer> entry : persistentAnalyses.entrySet()) {
//		EntityUtils.deepCopy(entry.getValue(), new TgKryoForDomainTreesTestingPurposes(kryo().factory(), new ClassProviderForTestingPurposes()));
//	    }
	    final Map<String, IAbstractAnalysisDomainTreeManager> currentAnalyses = readValue(buffer, LinkedHashMap.class);
	    final Map<String, IAbstractAnalysisDomainTreeManager> freezedAnalyses = readValue(buffer, LinkedHashMap.class);
	    return new CentreDomainTreeManagerAndEnhancer(kryo, base, enhancer, persistentAnalyses, currentAnalyses, freezedAnalyses);
	}

	@Override
	public void write(final ByteBuffer buffer, final CentreDomainTreeManagerAndEnhancer manager) {
	    writeValue(buffer, manager.base());
	    writeValue(buffer, manager.enhancer());
	    writeValue(buffer, manager.persistentAnalyses);
	    writeValue(buffer, manager.currentAnalyses);
	    writeValue(buffer, manager.freezedAnalyses);
	}
    }

    public ISerialiser getSerialiser() {
	return serialiser;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((persistentAnalyses == null) ? 0 : persistentAnalyses.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final CentreDomainTreeManagerAndEnhancer other = (CentreDomainTreeManagerAndEnhancer) obj;
	if (persistentAnalyses == null) {
	    if (other.persistentAnalyses != null)
		return false;
	} else if (!persistentAnalyses.equals(other.persistentAnalyses))
	    return false;
	return true;
    }
}
