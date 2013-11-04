package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.domaintree.Function;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementSet;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * A domain tree representation for "analyses" specific. A first tick means "include to distribution", second -- "include to aggregation".<br>
 *
 * There are DateFunction parameters for first tick and parameters for second tick configure simple properties aggregation (see {@link Function}).
 *
 * @author TG Team
 *
 */
public abstract class AbstractAnalysisDomainTreeRepresentation extends AbstractDomainTreeRepresentation implements IAbstractAnalysisDomainTreeRepresentation {
    private final transient ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager;

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    public AbstractAnalysisDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final AbstractAnalysisAddToDistributionTickRepresentation firstTick, final AbstractAnalysisAddToAggregationTickRepresentation secondTick) {
	super(serialiser, rootTypes, excludedProperties, firstTick, secondTick);

	parentCentreDomainTreeManager = null; // as soon as this analysis wiil be added into centre manager -- this field should be initialised
    }

    protected ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager() {
	return parentCentreDomainTreeManager;
    }

    protected Class<?> managedType(final Class<?> root) {
	return parentCentreDomainTreeManager().getEnhancer().getManagedType(DynamicEntityClassLoader.getOriginalType(root));
    }

    @Override
    public IAbstractAnalysisAddToDistributionTickRepresentation getFirstTick() {
	return (IAbstractAnalysisAddToDistributionTickRepresentation) super.getFirstTick();
    }

    @Override
    public IAbstractAnalysisAddToAggregationTickRepresentation getSecondTick() {
	return (IAbstractAnalysisAddToAggregationTickRepresentation) super.getSecondTick();
    }

    @Override
    public boolean isExcludedImmutably(final Class<?> root, final String property) {
	// inject an enhanced type into method implementation
	final Class<?> managedType = managedType(root);

	final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	final Pair<Class<?>, String> penultAndLast = PropertyTypeDeterminator.transform(managedType, property);
	// overridden to exclude crit-only itself properties
	return (super.isExcludedImmutably(managedType, property)) || // base TG domain representation usage
	(!isEntityItself && AnnotationReflector.isPropertyAnnotationPresent(CritOnly.class, penultAndLast.getKey(), penultAndLast.getValue())); // exclude crit-only properties
    }

    @Override
    public final IDomainTreeRepresentation excludeImmutably(final Class<?> root, final String property) {
	// inject an enhanced type into method implementation
        super.excludeImmutably(managedType(root), property);
	return this;
    }

    @Override
    public List<String> includedProperties(final Class<?> root) {
	// inject an enhanced type into method implementation
        return super.includedProperties(managedType(root));
    }

    /**
     * Getter of mutable "included properties" cache for internal purposes.
     * <p>
     * Please note that you can only mutate this list with methods {@link List#add(Object)} and {@link List#remove(Object)} to correctly reflect the changes on depending objects.
     * (e.g. UI tree models, checked properties etc.)
     *
     * @param root
     * @return
     */
    @Override
    public List<String> includedPropertiesMutable(final Class<?> root) {
	return parentCentreDomainTreeManager() != null ? super.includedPropertiesMutable(managedType(root)) : null;
    }

    @Override
    public IDomainTreeRepresentation warmUp(final Class<?> root, final String property) {
	// inject an enhanced type into method implementation
        super.warmUp(managedType(root), property);
	return this;
    }

    @Override
    public Set<Function> availableFunctions(final Class<?> root, final String property) {
	// inject an enhanced type into method implementation
	final Class<?> managedType = managedType(root);

	final Set<Function> availableFunctions = super.availableFunctions(managedType, property);
	if (isInCollectionHierarchy(managedType, property)) {
	    availableFunctions.remove(Function.ALL);
	    availableFunctions.remove(Function.ANY);
	}
	return availableFunctions;
    }

    /**
     * A first tick representation for "analyses" specific. <br><br>
     *
     * A checked tick means that a property (some date functions can be applied) should be added to "analyses" distribution.<br><br>
     *
     * For a collectional property an action "Create calculated property..." should be used to create aggregated property (or more complex expression).
     *
     * @author TG Team
     *
     */
    protected abstract static class AbstractAnalysisAddToDistributionTickRepresentation extends AbstractTickRepresentation implements IAbstractAnalysisAddToDistributionTickRepresentation {
	private final transient ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr" field.
	 */
	protected AbstractAnalysisAddToDistributionTickRepresentation() {
	    super();

	    parentCentreDomainTreeManager = null; // as soon as this analysis wiil be added into centre manager -- this field should be initialised
	}

	private ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager() {
	    return parentCentreDomainTreeManager;
	}

	protected Class<?> managedType(final Class<?> root) {
	    return parentCentreDomainTreeManager().getEnhancer().getManagedType(DynamicEntityClassLoader.getOriginalType(root));
	}

	@Override
	public boolean isDisabledImmutablyLightweight(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);

	    final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	    final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, property);
	    final KeyType keyTypeAnnotation = AnnotationReflector.getAnnotation(propertyType, KeyType.class);
	    final Calculated calculatedAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(Calculated.class, managedType, property);
	    //final String origination = calculatedAnnotation != null ? Reflector.fromRelative2AbsotulePath(calculatedAnnotation.contextPath(), calculatedAnnotation.origination()) : null;

	    return (super.isDisabledImmutablyLightweight(managedType, property)) || // a) disable manually disabled properties b) the checked by default properties should be disabled (immutable checking)
	    // TODO (!isEntityItself && AnnotationReflector.isAnnotationPresentInHierarchy(ResultOnly.class, root, property)) || // disable result-only properties and their children
	    (isEntityItself) || // empty property means "entity itself" and it should be disabled for distribution
	    (isCollectionOrInCollectionHierarchy(managedType, property)) || // disable properties in collectional hierarchy and collections itself
	    (!isEntityItself && isDisabledImmutablyPropertiesOfEntityType(propertyType, keyTypeAnnotation)) || // disable properties of "entity with AE key" type

	    //(!isEntityItself && Integer.class.isAssignableFrom(propertyType) && calculatedAnnotation == null) || // disable non-calc integer props
	    //(!isEntityItself && Integer.class.isAssignableFrom(propertyType) && calculatedAnnotation != null && !StringUtils.isEmpty(origination) && !EntityUtils.isDate(PropertyTypeDeterminator.determinePropertyType(managedType, origination))) || // disable integer calculated properties which were originated not from Date property
	    (!isEntityItself && !Integer.class.isAssignableFrom(propertyType) && !EntityUtils.isEntityType(propertyType) && !EntityUtils.isBoolean(propertyType) && !EntityUtils.isString(propertyType)); // disable properties of non-"entity or boolean or string" type
	    //		    Integer.class.isAssignableFrom(propertyType) && calculatedAnnotation != null && !EntityUtils.isDate(PropertyTypeDeterminator.determinePropertyType(root, calculatedAnnotation.origination())));// disable integer calculated properties which were originated not from Date property
	    //(!isEntityItself && !EntityUtils.isEntityType(propertyType) && !EntityUtils.isDate(propertyType) && !EntityUtils.isBoolean(propertyType)); // disable properties of "entity with AE or composite key" type
	}

	@Override
	public final ITickRepresentation disableImmutably(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    super.disableImmutably(managedType(root), property);
	    return this;
	}

	@Override
	public boolean isCheckedImmutably(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return super.isCheckedImmutably(managedType(root), property);
	}
    }

    /**
     * A second tick representation for "analyses" specific. <br><br>
     *
     * A checked tick means that a property (with a couple of aggregation parameters applied) should be added to "analyses" aggregation.<br><br>
     *
     * For a collectional property an action "Create calculated property..." should be used to create aggregated property (or more complex expression).
     *
     * @author TG Team
     *
     */
    protected abstract static class AbstractAnalysisAddToAggregationTickRepresentation extends AbstractTickRepresentation implements IAbstractAnalysisAddToAggregationTickRepresentation {
	private final EnhancementRootsMap<List<Pair<String, Ordering>>> rootsListsOfOrderings;
	private final EnhancementSet propertiesOrderingDisablement;

	private final transient ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr" field.
	 */
	protected AbstractAnalysisAddToAggregationTickRepresentation() {
	    super();
	    this.rootsListsOfOrderings = createRootsMap();
	    this.propertiesOrderingDisablement = createSet();

	    parentCentreDomainTreeManager = null; // as soon as this analysis wiil be added into centre manager -- this field should be initialised
	}

	private ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager() {
	    return parentCentreDomainTreeManager;
	}

	protected Class<?> managedType(final Class<?> root) {
	    return parentCentreDomainTreeManager().getEnhancer().getManagedType(DynamicEntityClassLoader.getOriginalType(root));
	}

	@Override
	public final boolean isDisabledImmutablyLightweight(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);
	    return (super.isDisabledImmutablyLightweight(managedType, property)) || // a) disable manually disabled properties b) the checked by default properties should be disabled (immutable checking)
		    !(isEnabledImmutably(root, property)); // provides "reversed" disabling -- disable all properties that is not enabled by "isEnabledImmutably(root, property)" contract

	    //	    (isCollectionOrInCollectionHierarchy(root, property)) || // disable properties in collectional hierarchy and collections itself
	    //	    // TODO (!isEntityItself && AnnotationReflector.isAnnotationPresentInHierarchy(ResultOnly.class, root, property)) || // disable result-only properties and their children
	    //	    (Reflector.isSynthetic(propertyType)) || // disable synthetic entities itself (and also synthetic properties -- rare case)
	    //	    // TODO (!isEntityItself && EntityUtils.isEntityType(propertyType) && (EntityUtils.isEntityType(keyTypeAnnotation.value()) || DynamicEntityKey.class.isAssignableFrom(keyTypeAnnotation.value()))) || // disable properties of "entity with AE or composite key" type
	    //	    (!isEntityItself && (EntityUtils.isDate(propertyType) || EntityUtils.isBoolean(propertyType))); // disable date and boolean properties
	}

	/**
	 * A contract for reverse "enabling" of the properties. For e.g. in base analyses second tick we should enable only AGGREGATED_EXPRESSION properties.
	 * This contract can be fully overridden to provide a very different functionality.
	 *
	 * @param root
	 * @param property
	 * @return
	 */
	protected boolean isEnabledImmutably(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);
	    final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	    return !isEntityItself && isCalculatedAndOfTypes(managedType, property, CalculatedPropertyCategory.AGGREGATED_EXPRESSION);
	}

	@Override
	public final ITickRepresentation disableImmutably(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    super.disableImmutably(managedType(root), property);
	    return this;
	}

	@Override
	public boolean isCheckedImmutably(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return super.isCheckedImmutably(managedType(root), property);
	}

	@Override
	public boolean isOrderingDisabledImmutably(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);

	    illegalExcludedProperties(getDtr(), managedType, property, "Could not ask an 'ordering disablement' for already 'excluded' property [" + property + "] in type [" + managedType.getSimpleName() + "].");
	    return (propertiesOrderingDisablement.contains(key(managedType, property))) ? true : false;
	}

	@Override
	public IOrderingRepresentation disableOrderingImmutably(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);

	    illegalExcludedProperties(getDtr(), managedType, property, "Could not disable an 'ordering' for already 'excluded' property [" + property + "] in type [" + managedType.getSimpleName() + "].");
	    propertiesOrderingDisablement.add(key(managedType, property));
	    return this;
	}

	@Override
	public List<Pair<String, Ordering>> orderedPropertiesByDefault(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);

	    illegalExcludedProperties(getDtr(), managedType, "", "Could not ask an 'ordering by default' for already 'excluded' type [" + managedType.getSimpleName() + "].");
	    if (rootsListsOfOrderings.containsKey(managedType)) {
		return rootsListsOfOrderings.get(managedType);
	    }
	    return new ArrayList<Pair<String, Ordering>>();
	}

	@Override
	public IOrderingRepresentation setOrderedPropertiesByDefault(final Class<?> root, final List<Pair<String, Ordering>> orderedPropertiesByDefault) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);

	    illegalExcludedProperties(getDtr(), managedType, "", "Could not set an 'ordering by default' for already 'excluded' type [" + managedType.getSimpleName() + "].");
	    rootsListsOfOrderings.put(managedType, new ArrayList<Pair<String, Ordering>>(orderedPropertiesByDefault));
	    return this;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + ((propertiesOrderingDisablement == null) ? 0 : propertiesOrderingDisablement.hashCode());
	    result = prime * result + ((rootsListsOfOrderings == null) ? 0 : rootsListsOfOrderings.hashCode());
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
	    final AbstractAnalysisAddToAggregationTickRepresentation other = (AbstractAnalysisAddToAggregationTickRepresentation) obj;
	    if (propertiesOrderingDisablement == null) {
		if (other.propertiesOrderingDisablement != null)
		    return false;
	    } else if (!propertiesOrderingDisablement.equals(other.propertiesOrderingDisablement))
		return false;
	    if (rootsListsOfOrderings == null) {
		if (other.rootsListsOfOrderings != null)
		    return false;
	    } else if (!rootsListsOfOrderings.equals(other.rootsListsOfOrderings))
		return false;
	    return true;
	}
    }
}
