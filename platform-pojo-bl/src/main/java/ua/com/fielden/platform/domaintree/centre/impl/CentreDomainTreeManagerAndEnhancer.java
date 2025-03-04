package ua.com.fielden.platform.domaintree.centre.impl;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyException;
import ua.com.fielden.platform.domaintree.IRootTyped;
import ua.com.fielden.platform.domaintree.centre.*;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.*;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager.AddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager.AddToResultTickManager;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.TickManager;
import ua.com.fielden.platform.domaintree.impl.*;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.AbstractTickRepresentation;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity_centre.mnemonics.DateRangePrefixEnum;
import ua.com.fielden.platform.entity_centre.mnemonics.MnemonicEnum;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel.GroupingPeriods;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import java.lang.reflect.Field;
import java.util.*;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.validateRootTypes;
import static ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer.createFrom;

/**
 * Criteria (entity-centre) domain tree manager with "power" of managing domain with calculated properties. The calculated properties can be managed exactly as simple properties.<br>
 *
 * @author TG Team
 *
 */
public class CentreDomainTreeManagerAndEnhancer extends AbstractDomainTreeManagerAndEnhancer implements ICentreDomainTreeManagerAndEnhancer {
    private final transient EntityFactory entityFactory;
    private final transient Logger logger = getLogger(getClass());
    private final LinkedHashMap<String, IAbstractAnalysisDomainTreeManager> persistentAnalyses;
    private final transient LinkedHashMap<String, IAbstractAnalysisDomainTreeManager> currentAnalyses;
    private final transient LinkedHashMap<String, IAbstractAnalysisDomainTreeManager> freezedAnalyses;


    /**
     * A <i>manager with enhancer</i> constructor for the first time instantiation.
     */
    public CentreDomainTreeManagerAndEnhancer(final EntityFactory entityFactory, final Set<Class<?>> rootTypes) {
        this(entityFactory, new CentreDomainTreeManager(entityFactory, validateRootTypes(rootTypes)), new DomainTreeEnhancer(entityFactory, validateRootTypes(rootTypes)), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    /**
     * A <i>manager with enhancer</i> constructor for instantiating with calculated / custom properties inside.
     *
     * @param rootTypes
     * @param calculatedAndCustomProperties
     */
    public CentreDomainTreeManagerAndEnhancer(
            final EntityFactory entityFactory,
            final Set<Class<?>> rootTypes,
            final T2<Map<Class<?>, Set<CalculatedPropertyInfo>>, Map<Class<?>, List<CustomProperty>>> calculatedAndCustomProperties) {
        this(
            entityFactory,
            new CentreDomainTreeManager(
                entityFactory,
                validateRootTypes(rootTypes)
            ),
            createFrom(
                entityFactory,
                validateRootTypes(rootTypes),
                calculatedAndCustomProperties._1,
                calculatedAndCustomProperties._2
            ),
            new HashMap<>(),
            new HashMap<>(),
            new HashMap<>()
        );
    }

    /**
     * A <i>manager with enhancer</i> constructor with transient analyses (current and freezed).
     */
    public CentreDomainTreeManagerAndEnhancer(final EntityFactory entityFactory, final CentreDomainTreeManager base, final DomainTreeEnhancer enhancer, final Map<String, IAbstractAnalysisDomainTreeManager> persistentAnalyses, final Map<String, IAbstractAnalysisDomainTreeManager> currentAnalyses, final Map<String, IAbstractAnalysisDomainTreeManager> freezedAnalyses) {
        super(base, enhancer);

        this.entityFactory = entityFactory;
        this.persistentAnalyses = new LinkedHashMap<>();
        this.persistentAnalyses.putAll(persistentAnalyses);
        // VERY IMPORTANT : Please note that deepCopy operation is not applicable here, because deserialisation process cannot be mixed with serialisation.
        // This constructor is explicitly used in deserialisation.
        this.currentAnalyses = new LinkedHashMap<>();
        this.currentAnalyses.putAll(currentAnalyses);
        this.freezedAnalyses = new LinkedHashMap<>();
        this.freezedAnalyses.putAll(freezedAnalyses);

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
     * Creates a domain tree enhancer wrapper that takes care about population of domain tree changes (calc props) in representation "included properties" (which triggers other
     * population like manager's "checked properties" automatically).
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
         * A {@link DomainTreeEnhancerWithPropertiesPopulation} constructor which requires a base implementations of {@link DomainTreeEnhancer} and
         * {@link AbstractDomainTreeRepresentation}.
         *
         * @param baseEnhancer
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
                    if (!analysis.getRepresentation().isExcludedImmutably(root, property) && !analysis.getRepresentation().getFirstTick().isDisabledImmutably(root, property)
                            && analysis.getFirstTick().isChecked(root, property)
                            || //
                            !analysis.getRepresentation().isExcludedImmutably(root, property) && !analysis.getRepresentation().getSecondTick().isDisabledImmutably(root, property)
                            && analysis.getSecondTick().isChecked(root, property)) {
                        throw new IncorrectCalcPropertyException("Can not remove (or significantly change) a property [" + property + "] in type [" + root
                                + "] which is used as distribution or aggregation property in analysis [" + analysisKey + "].");
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

    private static IAbstractAnalysisDomainTreeManager copyAnalysis(final IAbstractAnalysisDomainTreeManager analysisManager, final EntityFactory entityFactory) {
        if (analysisManager == null) {
            return null;
        }
        final IAbstractAnalysisDomainTreeManager copy = analysisManager; // FIXME provide alternative solution when analysis managers to be resurrected: EntityUtils.deepCopy(analysisManager, entityFactory);
        return initAnalysisManagerReferencesOn(copy, analysisManager.parentCentreDomainTreeManager());
    }

    public static IAbstractAnalysisDomainTreeManager initAnalysisManagerReferencesOn(final IAbstractAnalysisDomainTreeManager analysisManager, final ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager) {
        final AbstractAnalysisDomainTreeManager mgr = (AbstractAnalysisDomainTreeManager) analysisManager;

        initAnalysisManagerReferencesOn(mgr.getRepresentation(), parentCentreDomainTreeManager);

        // initialise the references on THIS instance in AbstractAnalysisDomainTreeManager, its both ticks, its representation and its both ticks
        try {
            setValueForLazyField(mgr, "parentCentreDomainTreeManager", parentCentreDomainTreeManager);
            setValueForLazyField(mgr.getFirstTick(), "parentCentreDomainTreeManager", parentCentreDomainTreeManager);
            setValueForLazyField(mgr.getSecondTick(), "parentCentreDomainTreeManager", parentCentreDomainTreeManager);
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
            setValueForLazyField(dtr, "parentCentreDomainTreeManager", parentCentreDomainTreeManager);

            // load analysis property tree! it depends on "parent centre domain type"!
            for (final Class<?> type : dtr.rootTypes()) {
                dtr.includedPropertiesMutable(type);
            }

            setValueForLazyField(dtr.getFirstTick(), "parentCentreDomainTreeManager", parentCentreDomainTreeManager);
            setValueForLazyField(dtr.getSecondTick(), "parentCentreDomainTreeManager", parentCentreDomainTreeManager);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        return dtr;
    }

    private static void setValueForLazyField(final Object mgr, final String propertyName, final ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager)
            throws IllegalAccessException {
        final Field parentCentreDomainTreeManagerField = Finder.findFieldByName(mgr.getClass(), propertyName);
        final boolean isAccessible = parentCentreDomainTreeManagerField.isAccessible();
        parentCentreDomainTreeManagerField.setAccessible(true);
        parentCentreDomainTreeManagerField.set(mgr, parentCentreDomainTreeManager);
        parentCentreDomainTreeManagerField.setAccessible(isAccessible);
    }

    @Override
    public ICentreDomainTreeManagerAndEnhancer initAnalysisManagerByDefault(final String name, final AnalysisType analysisType) {
        if (isFreezedAnalysisManager(name)) {
            error("Unable to Init analysis instance if it is freezed for title [" + name + "].");
        }
        if (getAnalysisManager(name) != null) {
            throw new DomainTreeException("The analysis with name [" + name + "] already exists.");
        }
        // create a new instance and put to "current" map
        if (AnalysisType.PIVOT.equals(analysisType)) {
            currentAnalyses.put(name, initAnalysisManagerReferencesOn(new PivotDomainTreeManager(getEntityFactory(), getRepresentation().rootTypes()), this));
        }
        if (AnalysisType.SIMPLE.equals(analysisType)) {
            currentAnalyses.put(name, initAnalysisManagerReferencesOn(new AnalysisDomainTreeManager(getEntityFactory(), getRepresentation().rootTypes()), this));
        }
        if (AnalysisType.SENTINEL.equals(analysisType)) {
            provideSentinelAnalysesAggregationProperty(getRepresentation().rootTypes());
            currentAnalyses.put(name, initAnalysisManagerReferencesOn(new SentinelDomainTreeManager(getEntityFactory(), getRepresentation().rootTypes()), this));
            final SentinelDomainTreeManager sdtm = (SentinelDomainTreeManager) getAnalysisManager(name);
            sdtm.provideMetaStateForCountOfSelfDashboardProperty();
        }
        if (AnalysisType.LIFECYCLE.equals(analysisType)) {
            provideLifecycleAnalysesDatePeriodProperties(getRepresentation().rootTypes());
            currentAnalyses.put(name, initAnalysisManagerReferencesOn(new LifecycleDomainTreeManager(getEntityFactory(), getRepresentation().rootTypes()), this));
            final LifecycleDomainTreeManager ldtm = (LifecycleDomainTreeManager) getAnalysisManager(name);
            ldtm.provideMetaStateForLifecycleAnalysesDatePeriodProperties();
        }
        if (AnalysisType.MULTIPLEDEC.equals(analysisType)) {
            currentAnalyses.put(name, initAnalysisManagerReferencesOn(new MultipleDecDomainTreeManager(getEntityFactory(), getRepresentation().rootTypes()), this));
        }
        return this;
    }

    /**
     * Enhances centre manager domain to include COUNT_OF_SELF_DASHBOARD property, which will be used for sentinel analyses.
     *
     * @param rootTypes
     */
    public void provideSentinelAnalysesAggregationProperty(final Set<Class<?>> rootTypes) {
        for (final Class<?> rootType : rootTypes) {
            // add "count of self" calculated property that is essential for "sentinel" analyses
            try {
                getEnhancer().getCalculatedProperty(rootType, SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD);
            } catch (final IncorrectCalcPropertyException e) {
                getEnhancer().addCalculatedProperty(rootType, "", "COUNT(SELF)", "Count of self (Dashboard)", "This calculated property is used for sentinels as aggregation function that calculates a number of entities by each status.", CalculatedPropertyAttribute.NO_ATTR, "SELF", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
                getEnhancer().apply();
            }
        }
    }

    /**
     * Enhances centre manager domain to include "Date Period" distribution properties, which will be used for lifecycle analyses.
     *
     * @param rootTypes
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
            final CalculatedProperty calc = (CalculatedProperty) getEnhancer().addCalculatedProperty(rootType, "", expr, period.getTitle() /* period.getPropertyName() */, period.getDesc()
                    + descAddition, CalculatedPropertyAttribute.NO_ATTR, "SELF", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);

            // TODO tricky setting!
            calc.setNameVeryTricky(period.getPropertyName());

            getEnhancer().apply();
        }
    }

    @Override
    public ICentreDomainTreeManagerAndEnhancer discardAnalysisManager(final String name) {
        final IAbstractAnalysisDomainTreeManager dtm = copyAnalysis(persistentAnalyses.get(name), getEntityFactory());
        if (dtm != null) {
            currentAnalyses.put(name, dtm);
        } else {
            currentAnalyses.remove(name);
        }

        if (isFreezedAnalysisManager(name)) {
            unfreeze(name);
        }
        return this;
    }

    @Override
    public ICentreDomainTreeManagerAndEnhancer acceptAnalysisManager(final String name) {
        if (isFreezedAnalysisManager(name)) {
            unfreeze(name);

            currentAnalyses.put(name, copyAnalysis(currentAnalyses.get(name), getEntityFactory())); // this is necessary to dispose current manager with listeners and get equal "fresh" instance
        } else {
            final IAbstractAnalysisDomainTreeManager dtm = copyAnalysis(currentAnalyses.get(name), getEntityFactory());
            if (dtm != null) {
                persistentAnalyses.put(name, dtm);
            } else {
                persistentAnalyses.remove(name);
            }
        }
        return this;
    }

    @Override
    public boolean isChangedAnalysisManager(final String name) {
        return !EntityUtils.equalsEx(currentAnalyses.get(name), persistentAnalyses.get(name));
    }

    @Override
    public ICentreDomainTreeManagerAndEnhancer removeAnalysisManager(final String name) {
        if (isFreezedAnalysisManager(name)) {
            error("Unable to remove analysis instance if it is freezed for title [" + name + "].");
        }
        final IAbstractAnalysisDomainTreeManager mgr = getAnalysisManager(name);
        if (mgr == null) {
            throw new DomainTreeException("The unknown analysis with name [" + name + "] can not be removed.");
        }
        currentAnalyses.remove(name);
        acceptAnalysisManager(name);
        return this;
    }

    @Override
    public IAbstractAnalysisDomainTreeManager getAnalysisManager(final String name) {
        return currentAnalyses.get(name);
    }

    @Override
    public ICentreDomainTreeManagerAndEnhancer freezeAnalysisManager(final String name) {
        if (isFreezedAnalysisManager(name)) {
            error("Unable to freeze the analysis instance more than once for title [" + name + "].");
        }
        notInitiliasedError(currentAnalyses.get(name), name);
        notInitiliasedError(persistentAnalyses.get(name), name);

        freezedAnalyses.put(name, persistentAnalyses.remove(name));
        persistentAnalyses.put(name, copyAnalysis(currentAnalyses.get(name), getEntityFactory()));
        currentAnalyses.put(name, copyAnalysis(currentAnalyses.get(name), getEntityFactory())); // this is necessary to dispose current manager with listeners and get equal "fresh" instance
        return this;
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
     * @param name
     */
    private void notInitiliasedError(final IAbstractAnalysisDomainTreeManager mgr, final String name) {
        if (mgr == null) {
            error("Unable to perform this operation on the analysis instance, that wasn't initialised, for title [" + name + "].");
        }
    }

    /**
     * Logs and throws an {@link DomainTreeException} error with specified message.
     *
     * @param message
     */
    private void error(final String message) {
        logger.error(message);
        throw new DomainTreeException(message);
    }

    @Override
    public List<String> analysisKeys() {
        return new ArrayList<>(currentAnalyses.keySet());
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
    public class AddToCriteriaTickManagerAndEnhancer extends TickManagerAndEnhancer implements IAddToCriteriaTickManager, IRootTyped {

        private AddToCriteriaTickManagerAndEnhancer(final TickManager base) {
            super(base);
        }

        @Override
        public/* TODO reduce visibility */AddToCriteriaTickManager base() {
            return (AddToCriteriaTickManager) super.base();
        }

        @Override
        public IAddToCriteriaTickManager swap(final Class<?> root, final String property1, final String property2) {
            super.swap(root, property1, property2);
            return this;
        }

        @Override
        public IAddToCriteriaTickManager moveToTheEnd(final Class<?> root, final String what) {
            super.moveToTheEnd(root, what);
            return this;
        }

        @Override
        public IAddToCriteriaTickManager check(final Class<?> root, final String property, final boolean check) {
            super.check(root, property, check);
            return this;
        }

        @Override
        public IAddToCriteriaTickManager move(final Class<?> root, final String what, final String beforeWhat) {
            super.move(root, what, beforeWhat);
            return this;
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
        public Integer getOrGroup(final Class<?> root, final String property) {
            // inject an enhanced type into method implementation
            return base().getOrGroup(enhancer().getManagedType(root), property);
        }

        @Override
        public IAddToCriteriaTickManager setOrGroup(final Class<?> root, final String property, final Integer orGroup) {
            // inject an enhanced type into method implementation
            base().setOrGroup(enhancer().getManagedType(root), property, orGroup);
            return this;
        }

        @Override
        public boolean getAutocompleteActiveOnly(final Class<?> root, final String property) {
            // inject an enhanced type into method implementation
            return base().getAutocompleteActiveOnly(enhancer().getManagedType(root), property);
        }

        @Override
        public IAddToCriteriaTickManager setAutocompleteActiveOnly(final Class<?> root, final String property, final boolean autocompleteActiveOnly) {
            // inject an enhanced type into method implementation
            base().setAutocompleteActiveOnly(enhancer().getManagedType(root), property, autocompleteActiveOnly);
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

        @Override
        public boolean selectionCriteriaEquals(final IAddToCriteriaTickManager obj) {
            return this == obj ||
                getClass() == obj.getClass()
                && base().selectionCriteriaEquals(((AddToCriteriaTickManagerAndEnhancer) obj).base());
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
        public IOrderingManager toggleOrdering(final Class<?> root, final String property) {
            // inject an enhanced type into method implementation
            base().toggleOrdering(enhancer().getManagedType(root), property);
            return this;
        }

        @Override
        public int getWidth(final Class<?> root, final String property) {
            // inject an enhanced type into method implementation
            return base().getWidth(enhancer().getManagedType(root), property);
        }

        @Override
        public IWidthManager setWidth(final Class<?> root, final String property, final int width) {
            // inject an enhanced type into method implementation
            base().setWidth(enhancer().getManagedType(root), property, width);
            return this;
        }

        @Override
        public int getGrowFactor(final Class<?> root, final String property) {
            // inject an enhanced type into method implementation
            return base().getGrowFactor(enhancer().getManagedType(root), property);
        }

        @Override
        public IAddToResultTickManager setGrowFactor(final Class<?> root, final String property, final int growFactor) {
            // inject an enhanced type into method implementation
            base().setGrowFactor(enhancer().getManagedType(root), property, growFactor);
            return this;
        }

        @Override
        public T2<EnhancementPropertiesMap<Integer>, EnhancementPropertiesMap<Integer>> getWidthsAndGrowFactors() {
            return base().getWidthsAndGrowFactors();
        }

        @Override
        public void setWidthsAndGrowFactors(final T2<EnhancementPropertiesMap<Integer>, EnhancementPropertiesMap<Integer>> widthsAndGrowFactors) {
            base().setWidthsAndGrowFactors(widthsAndGrowFactors);
        }

        @Override
        public int getPageCapacity() {
            return base().getPageCapacity();
        }

        @Override
        public IAddToResultTickManager setPageCapacity(final int pageCapacity) {
            base().setPageCapacity(pageCapacity);
            return this;
        }

        @Override
        public int getMaxPageCapacity() {
            return base().getMaxPageCapacity();
        }

        @Override
        public IAddToResultTickManager setMaxPageCapacity(final int maxPageCapacity) {
            base().setMaxPageCapacity(maxPageCapacity);
            return this;
        }

        @Override
        public int getVisibleRowsCount() {
            return base().getVisibleRowsCount();
        }

        @Override
        public IAddToResultTickManager setVisibleRowsCount(final int visibleRowsCount) {
            base().setVisibleRowsCount(visibleRowsCount);
            return this;
        }

        @Override
        public int getNumberOfHeaderLines() {
            return base().getNumberOfHeaderLines();
        }

        @Override
        public IAddToResultTickManager setNumberOfHeaderLines(final int numberOfHeaderLines) {
            base().setNumberOfHeaderLines(numberOfHeaderLines);
            return this;
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

            @Override
            public IAddToCriteriaTickRepresentation setValuesByDefault(final Class<?> root, final Map<String, Object> propertyValuePairs) {
                base().setValuesByDefault(root, propertyValuePairs);
                return this;
            }

            @Override
            public IAddToCriteriaTickRepresentation setValues2ByDefault(final Class<?> root, final Map<String, Object> propertyValuePairs) {
                base().setValues2ByDefault(root, propertyValuePairs);
                return this;
            }

            @Override
            public Map<String, Object> getValuesByDefault(final Class<?> root) {
                return base().getValuesByDefault(root);
            }

            @Override
            public Map<String, Object> getValues2ByDefault(final Class<?> root) {
                return base().getValues2ByDefault(root);
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
            public IOrderingRepresentation disableOrderingImmutably(final Class<?> root, final String property) {
                // inject an enhanced type into method implementation
                base().disableOrderingImmutably(enhancer().getManagedType(root), property);
                return this;
            }

            @Override
            public List<Pair<String, Ordering>> orderedPropertiesByDefault(final Class<?> root) {
                // inject an enhanced type into method implementation
                return base().orderedPropertiesByDefault(enhancer().getManagedType(root));
            }

            @Override
            public IOrderingRepresentation setOrderedPropertiesByDefault(final Class<?> root, final List<Pair<String, Ordering>> orderedPropertiesByDefault) {
                // inject an enhanced type into method implementation
                base().setOrderedPropertiesByDefault(enhancer().getManagedType(root), orderedPropertiesByDefault);
                return this;
            }

            @Override
            public int getWidthByDefault(final Class<?> root, final String property) {
                // inject an enhanced type into method implementation
                return base().getWidthByDefault(enhancer().getManagedType(root), property);
            }

            @Override
            public IWidthRepresentation setWidthByDefault(final Class<?> root, final String property, final int width) {
                // inject an enhanced type into method implementation
                base().setWidthByDefault(enhancer().getManagedType(root), property, width);
                return this;
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
    public Integer getPreferredView() {
        return base().getPreferredView();
    }

    @Override
    public ICentreDomainTreeManager setPreferredView(final Integer preferredView) {
        return base().setPreferredView(preferredView);
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

    public EntityFactory getEntityFactory() {
        return entityFactory;
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
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CentreDomainTreeManagerAndEnhancer other = (CentreDomainTreeManagerAndEnhancer) obj;
        if (persistentAnalyses == null) {
            if (other.persistentAnalyses != null) {
                return false;
            }
        } else if (!persistentAnalyses.equals(other.persistentAnalyses)) {
            return false;
        }
        return true;
    }
}