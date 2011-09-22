package ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.treemodel.rules.Function;
import ua.com.fielden.platform.treemodel.rules.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.IAbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.impl.EnhancementRootsMap;
import ua.com.fielden.platform.treemodel.rules.impl.EnhancementSet;
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
    private static final long serialVersionUID = -662851789405859468L;

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    public AbstractAnalysisDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final ITickRepresentation firstTick, final ITickRepresentation secondTick, final EnhancementRootsMap<ListenedArrayList> includedProperties) {
	super(serialiser, rootTypes, excludedProperties, firstTick, secondTick, includedProperties);
    }

    @Override
    public IAbstractAnalysisAddToDistributionTickRepresentation getFirstTick() {
	return (IAbstractAnalysisAddToDistributionTickRepresentation) super.getFirstTick();
    }

    @Override
    public IAbstractAnalysisAddToAggregationTickRepresentation getSecondTick() {
	return (IAbstractAnalysisAddToAggregationTickRepresentation) super.getSecondTick();
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
	private static final long serialVersionUID = 285667836926011724L;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr" field.
	 */
	protected AbstractAnalysisAddToDistributionTickRepresentation() {
	    super();
	}

	@Override
	public boolean isDisabledImmutably(final Class<?> root, final String property) {
	    final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	    final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, property);
	    final KeyType keyTypeAnnotation = AnnotationReflector.getAnnotation(KeyType.class, propertyType);
	    final Calculated calculatedAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(Calculated.class, root, property);

	    return (super.isDisabledImmutably(root, property)) || // a) disable manually disabled properties b) the checked by default properties should be disabled (immutable checking)
	    // TODO (!isEntityItself && AnnotationReflector.isAnnotationPresentInHierarchy(ResultOnly.class, root, property)) || // disable result-only properties and their children
	    (isEntityItself) || // empty property means "entity itself" and it should be disabled for distribution
	    (isCollectionOrInCollectionHierarchy(root, property)) || // disable properties in collectional hierarchy and collections itself
	    (!isEntityItself && EntityUtils.isEntityType(propertyType) && (EntityUtils.isEntityType(keyTypeAnnotation.value()) || DynamicEntityKey.class.isAssignableFrom(keyTypeAnnotation.value()))) || // disable properties of "entity with AE or composite key" type

	    (!isEntityItself && Integer.class.isAssignableFrom(propertyType) && calculatedAnnotation == null) || // disable non-calc integer props
	    (!isEntityItself && Integer.class.isAssignableFrom(propertyType) && calculatedAnnotation != null && !EntityUtils.isDate(PropertyTypeDeterminator.determinePropertyType(root, calculatedAnnotation.origination()))) || // disable integer calculated properties which were originated not from Date property
	    (!isEntityItself && !Integer.class.isAssignableFrom(propertyType) && !EntityUtils.isEntityType(propertyType) && !EntityUtils.isBoolean(propertyType)); // disable properties of non-"entity or boolean" type
	    //		    Integer.class.isAssignableFrom(propertyType) && calculatedAnnotation != null && !EntityUtils.isDate(PropertyTypeDeterminator.determinePropertyType(root, calculatedAnnotation.origination())));// disable integer calculated properties which were originated not from Date property
	    //(!isEntityItself && !EntityUtils.isEntityType(propertyType) && !EntityUtils.isDate(propertyType) && !EntityUtils.isBoolean(propertyType)); // disable properties of "entity with AE or composite key" type
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
	private static final long serialVersionUID = -9171185828185404608L;

	private final EnhancementRootsMap<List<Pair<String, Ordering>>> rootsListsOfOrderings;
	private final EnhancementSet propertiesOrderingDisablement;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr" field.
	 */
	protected AbstractAnalysisAddToAggregationTickRepresentation() {
	    super();
	    this.rootsListsOfOrderings = createRootsMap();
	    this.propertiesOrderingDisablement = createSet();
	}

	@Override
	public boolean isDisabledImmutably(final Class<?> root, final String property) {
	    final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	    //final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, property);
	    //final Calculated calculatedAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(Calculated.class, root, property);

	    return (super.isDisabledImmutably(root, property)) || // a) disable manually disabled properties b) the checked by default properties should be disabled (immutable checking)
	    !(!isEntityItself && isCalculatedAndOfTypes(root, property, CalculatedPropertyCategory.AGGREGATED_EXPRESSION));//
	    //	    (isCollectionOrInCollectionHierarchy(root, property)) || // disable properties in collectional hierarchy and collections itself
	    //	    // TODO (!isEntityItself && AnnotationReflector.isAnnotationPresentInHierarchy(ResultOnly.class, root, property)) || // disable result-only properties and their children
	    //	    (Reflector.isSynthetic(propertyType)) || // disable synthetic entities itself (and also synthetic properties -- rare case)
	    //	    // TODO (!isEntityItself && EntityUtils.isEntityType(propertyType) && (EntityUtils.isEntityType(keyTypeAnnotation.value()) || DynamicEntityKey.class.isAssignableFrom(keyTypeAnnotation.value()))) || // disable properties of "entity with AE or composite key" type
	    //	    (!isEntityItself && (EntityUtils.isDate(propertyType) || EntityUtils.isBoolean(propertyType))); // disable date and boolean properties
	}

	@Override
	public boolean isOrderingDisabledImmutably(final Class<?> root, final String property) {
	    illegalExcludedProperties(getDtr(), root, property, "Could not ask an 'ordering disablement' for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return (propertiesOrderingDisablement.contains(key(root, property))) ? true : false;
	}

	@Override
	public void disableOrderingImmutably(final Class<?> root, final String property) {
	    illegalExcludedProperties(getDtr(), root, property, "Could not disable an 'ordering' for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    propertiesOrderingDisablement.add(key(root, property));
	}

	@Override
	public List<Pair<String, Ordering>> orderedPropertiesByDefault(final Class<?> root) {
	    illegalExcludedProperties(getDtr(), root, "", "Could not ask an 'ordering by default' for already 'excluded' type [" + root.getSimpleName() + "].");
	    if (rootsListsOfOrderings.containsKey(root)) {
		return rootsListsOfOrderings.get(root);
	    }
	    return new ArrayList<Pair<String, Ordering>>();
	}

	@Override
	public void setOrderedPropertiesByDefault(final Class<?> root, final List<Pair<String, Ordering>> orderedPropertiesByDefault) {
	    illegalExcludedProperties(getDtr(), root, "", "Could not set an 'ordering by default' for already 'excluded' type [" + root.getSimpleName() + "].");
	    rootsListsOfOrderings.put(root, new ArrayList<Pair<String, Ordering>>(orderedPropertiesByDefault));
	}
    }

    @Override
    public boolean isExcludedImmutably(final Class<?> root, final String property) {
	final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	final Pair<Class<?>, String> penultAndLast = PropertyTypeDeterminator.transform(root, property);
	// overridden to exclude crit-only itself properties
	return (super.isExcludedImmutably(root, property)) || // base TG domain representation usage
	(!isEntityItself && AnnotationReflector.isPropertyAnnotationPresent(CritOnly.class, penultAndLast.getKey(), penultAndLast.getValue())); // exclude crit-only properties
    }

    @Override
    public Set<Function> availableFunctions(final Class<?> root, final String property) {
	final Set<Function> availableFunctions = super.availableFunctions(root, property);
	if (isInCollectionHierarchy(root, property)) {
	    availableFunctions.remove(Function.ALL);
	    availableFunctions.remove(Function.ANY);
	}
	return availableFunctions;
    }

    //    /**
    //     * A specific Kryo serialiser for {@link AnalysisDomainTreeRepresentation}.
    //     *
    //     * @author TG Team
    //     *
    //     */
    //    public static class AnalysisDomainTreeRepresentationSerialiser extends AbstractDomainTreeRepresentationSerialiser<AnalysisDomainTreeRepresentation> {
    //	public AnalysisDomainTreeRepresentationSerialiser(final TgKryo kryo) {
    //	    super(kryo);
    //	}
    //
    //	@Override
    //	public AnalysisDomainTreeRepresentation read(final ByteBuffer buffer) {
    //	    final Set<Class<?>> rootTypes = readValue(buffer, HashSet.class);
    //	    final Set<Pair<Class<?>, String>> excludedProperties = readValue(buffer, HashSet.class);
    //	    final ITickRepresentation firstTick = readValue(buffer, ITickRepresentation.class);
    //	    final ITickRepresentation secondTick = readValue(buffer, ITickRepresentation.class);
    //	    return new AnalysisDomainTreeRepresentation(kryo(), rootTypes, excludedProperties, firstTick, secondTick);
    //	}
    //    }
}
