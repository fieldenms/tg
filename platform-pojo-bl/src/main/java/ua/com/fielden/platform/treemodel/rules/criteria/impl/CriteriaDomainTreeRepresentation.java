package ua.com.fielden.platform.treemodel.rules.criteria.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.ResultOnly;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.treemodel.rules.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.treemodel.rules.criteria.ICriteriaDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTree;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.impl.EnhancementLinkedRootsSet;
import ua.com.fielden.platform.treemodel.rules.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.treemodel.rules.impl.EnhancementRootsMap;
import ua.com.fielden.platform.treemodel.rules.impl.EnhancementSet;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * A domain tree representation for entity centres specific. A first tick means "include to criteria", second -- "include to result-set".<br>
 *
 * There are no parameters for first tick, parameters for second tick configure simple properties aggregation for result-set (aka Totals).
 *
 * @author TG Team
 *
 */
public class CriteriaDomainTreeRepresentation extends AbstractDomainTreeRepresentation implements ICriteriaDomainTreeRepresentation {
    private static final long serialVersionUID = -2470587042269751368L;

    /**
     * A <i>representation</i> constructor for the first time instantiation. Initialises also children references on itself.
     */
    public CriteriaDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, rootTypes, AbstractDomainTree.createSet(), new AddToCriteriaTick(), new AddToResultSetTick(), AbstractDomainTree.<ListenedArrayList>createRootsMap());
    }

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected CriteriaDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final IAddToCriteriaTickRepresentation firstTick, final IAddToResultTickRepresentation secondTick, final EnhancementRootsMap<ListenedArrayList> includedProperties) {
	super(serialiser, rootTypes, excludedProperties, firstTick, secondTick, includedProperties);
    }

    @Override
    public IAddToCriteriaTickRepresentation getFirstTick() {
	return (IAddToCriteriaTickRepresentation) super.getFirstTick();
    }

    @Override
    public IAddToResultTickRepresentation getSecondTick() {
	return (IAddToResultTickRepresentation) super.getSecondTick();
    }

    /**
     * A first tick representation for entity centres specific. <br><br>
     *
     * A checked tick means that a property (no parameters can be applied) should be added to entity centre criteria.<br><br>
     *
     * For a collectional property an action "Create calculated property..." should be used to create aggregated property (or more complex expression).
     *
     * @author TG Team
     *
     */
    protected static class AddToCriteriaTick extends AbstractTickRepresentation implements IAddToCriteriaTickRepresentation {
	private static final long serialVersionUID = -4402869381859112250L;
	private final EnhancementPropertiesMap<Object> propertiesDefaultValues1;
	private final EnhancementPropertiesMap<Object> propertiesDefaultValues2;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr" field.
	 */
	protected AddToCriteriaTick() {
	    super();
	    propertiesDefaultValues1 = createPropertiesMap();
	    propertiesDefaultValues2 = createPropertiesMap();
	}

	@Override
	public boolean isDisabledImmutably(final Class<?> root, final String property) {
	    final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	    final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, property);
	    final KeyType keyTypeAnnotation = AnnotationReflector.getAnnotation(KeyType.class, propertyType);

	    return (super.isDisabledImmutably(root, property)) || // a) disable manually disabled properties b) the checked by default properties should be disabled (immutable checking)
		    (!isEntityItself && AnnotationReflector.isAnnotationPresentInHierarchy(ResultOnly.class, root, property)) || // disable result-only properties and their children
		    (!isEntityItself && isCalculatedAndOfTypes(root, property, CalculatedPropertyCategory.AGGREGATED_EXPRESSION)) || // disable AGGREGATED_EXPRESSION properties for criteria tick
		    (EntityUtils.isEntityType(propertyType) && (EntityUtils.isEntityType(keyTypeAnnotation.value()) || DynamicEntityKey.class.isAssignableFrom(keyTypeAnnotation.value()))); // disable properties of "entity with AE or composite key" type
	}

	@Override
	public boolean isCheckedImmutably(final Class<?> root, final String property) {
	    final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	    final Pair<Class<?>, String> penultAndLast = PropertyTypeDeterminator.transform(root, property);

	    return (super.isCheckedImmutably(root, property)) || // check+disable manually checked properties
		    (!isEntityItself && AnnotationReflector.isPropertyAnnotationPresent(CritOnly.class, penultAndLast.getKey(), penultAndLast.getValue())); // check+disable crit-only properties (the children should be excluded!)
	}

	private Object typeAndSingleRelatedValue(final Class<?> root, final String property) {
	    final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	    final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, property);
	    final CritOnly critAnnotation = AnnotationReflector.getPropertyAnnotation(CritOnly.class, root, property);
	    final boolean single = critAnnotation != null && Type.SINGLE.equals(critAnnotation.value());
	    return DynamicQueryBuilder.getEmptyValue(propertyType, single);
	}

	@Override
	public Object getValueByDefault(final Class<?> root, final String property) {
	    illegalExcludedProperties(getDtr(), root, property, "Could not ask a 'value by default' for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return (propertiesDefaultValues1.containsKey(key(root, property))) ? propertiesDefaultValues1.get(key(root, property)) : typeAndSingleRelatedValue(root, property);
	}

	@Override
	public IAddToCriteriaTickRepresentation setValueByDefault(final Class<?> root, final String property, final Object value) {
	    illegalExcludedProperties(getDtr(), root, property, "Could not set a 'value by default' for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    propertiesDefaultValues1.put(key(root, property), value);
	    return this;
	}

	@Override
	public Object getValue2ByDefault(final Class<?> root, final String property) {
	    illegalExcludedProperties(getDtr(), root, property, "Could not ask a 'value 2 by default' for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return (propertiesDefaultValues2.containsKey(key(root, property))) ? propertiesDefaultValues2.get(key(root, property)) : typeAndSingleRelatedValue(root, property);
	}

	@Override
	public IAddToCriteriaTickRepresentation setValue2ByDefault(final Class<?> root, final String property, final Object value2) {
	    illegalExcludedProperties(getDtr(), root, property, "Could not set a 'value 2 by default' for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    propertiesDefaultValues2.put(key(root, property), value2);
	    return this;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + ((propertiesDefaultValues1 == null) ? 0 : propertiesDefaultValues1.hashCode());
	    result = prime * result + ((propertiesDefaultValues2 == null) ? 0 : propertiesDefaultValues2.hashCode());
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
	    final AddToCriteriaTick other = (AddToCriteriaTick) obj;
	    if (propertiesDefaultValues1 == null) {
		if (other.propertiesDefaultValues1 != null)
		    return false;
	    } else if (!propertiesDefaultValues1.equals(other.propertiesDefaultValues1))
		return false;
	    if (propertiesDefaultValues2 == null) {
		if (other.propertiesDefaultValues2 != null)
		    return false;
	    } else if (!propertiesDefaultValues2.equals(other.propertiesDefaultValues2))
		return false;
	    return true;
	}
    }

    /**
     * A second tick representation for entity centres specific. <br><br>
     *
     * A checked tick means that a property (with a couple of Totals parameters applied) should be added to entity centre result-set.<br><br>
     *
     * For a collectional property an action "Create calculated property..." should be used to create aggregated property (or more complex expression).
     *
     * @author TG Team
     *
     */
    protected static class AddToResultSetTick extends AbstractTickRepresentation implements IAddToResultTickRepresentation {
	private static final long serialVersionUID = -3615430853744511063L;
	private final EnhancementPropertiesMap<Integer> propertiesWidths;
	private final EnhancementRootsMap<List<Pair<String, Ordering>>> rootsListsOfOrderings;
	private final EnhancementSet propertiesOrderingDisablement;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr" field.
	 */
	protected AddToResultSetTick() {
	    super();
	    propertiesWidths = createPropertiesMap();
	    propertiesOrderingDisablement = createSet();
	    rootsListsOfOrderings = createRootsMap();
	}

	@Override
	public boolean isDisabledImmutably(final Class<?> root, final String property) {
	    final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	    final Pair<Class<?>, String> penultAndLast = PropertyTypeDeterminator.transform(root, property);
	    final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determineClass(penultAndLast.getKey(), penultAndLast.getValue(), true, true);
	    final KeyType keyTypeAnnotation = AnnotationReflector.getAnnotation(KeyType.class, propertyType);

	    return (super.isDisabledImmutably(root, property)) || // a) disable manually disabled properties b) the checked by default properties should be disabled (immutable checking)
		    (!isEntityItself && AnnotationReflector.isPropertyAnnotationPresent(CritOnly.class, penultAndLast.getKey(), penultAndLast.getValue())) || // disable crit-only properties (the children should be excluded!)
		    (isCollectionOrInCollectionHierarchy(root, property)) || // disable properties in collectional hierarchy and collections itself
		    (Reflector.isSynthetic(propertyType)) || // disable synthetic entities itself (and also synthetic properties -- rare case)
		    (!isEntityItself && isCalculatedAndOfTypes(root, property, CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION)) || // disable ATTRIBUTED_COLLECTIONAL_EXPRESSION properties for result-set tick
		    (EntityUtils.isEntityType(propertyType) && (EntityUtils.isEntityType(keyTypeAnnotation.value()) || DynamicEntityKey.class.isAssignableFrom(keyTypeAnnotation.value()))); // disable properties of "entity with AE or composite key" type
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
	    final Class<?> keyType = PropertyTypeDeterminator.determinePropertyType(root, AbstractEntity.KEY);
	    final List<Pair<String, Ordering>> pairs = new ArrayList<Pair<String, Ordering>>();
	    if (!EntityUtils.isEntityType(keyType) && !DynamicEntityKey.class.isAssignableFrom(keyType)) {
		pairs.add(new Pair<String, Ordering>("", Ordering.ASCENDING));
	    }
	    return pairs;
	}

	@Override
	public void setOrderedPropertiesByDefault(final Class<?> root, final List<Pair<String, Ordering>> orderedPropertiesByDefault) {
	    illegalExcludedProperties(getDtr(), root, "", "Could not set an 'ordering by default' for already 'excluded' type [" + root.getSimpleName() + "].");
	    rootsListsOfOrderings.put(root, new ArrayList<Pair<String, Ordering>>(orderedPropertiesByDefault));
	}

	@Override
	public int getWidthByDefault(final Class<?> root, final String property) {
	    illegalExcludedProperties(getDtr(), root, property, "Could not ask a 'width by default' for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return (propertiesWidths.containsKey(key(root, property))) ? propertiesWidths.get(key(root, property)) : 80;
	}

	@Override
	public void setWidthByDefault(final Class<?> root, final String property, final int width) {
	    illegalExcludedProperties(getDtr(), root, property, "Could not set a 'width by default' for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    propertiesWidths.put(key(root, property), width);
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + ((propertiesOrderingDisablement == null) ? 0 : propertiesOrderingDisablement.hashCode());
	    result = prime * result + ((propertiesWidths == null) ? 0 : propertiesWidths.hashCode());
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
	    final AddToResultSetTick other = (AddToResultSetTick) obj;
	    if (propertiesOrderingDisablement == null) {
		if (other.propertiesOrderingDisablement != null)
		    return false;
	    } else if (!propertiesOrderingDisablement.equals(other.propertiesOrderingDisablement))
		return false;
	    if (propertiesWidths == null) {
		if (other.propertiesWidths != null)
		    return false;
	    } else if (!propertiesWidths.equals(other.propertiesWidths))
		return false;
	    if (rootsListsOfOrderings == null) {
		if (other.rootsListsOfOrderings != null)
		    return false;
	    } else if (!rootsListsOfOrderings.equals(other.rootsListsOfOrderings))
		return false;
	    return true;
	}
    }

    /**
     * A specific Kryo serialiser for {@link CriteriaDomainTreeRepresentation}.
     *
     * @author TG Team
     *
     */
    public static class CriteriaDomainTreeRepresentationSerialiser extends AbstractDomainTreeRepresentationSerialiser<CriteriaDomainTreeRepresentation> {
	public CriteriaDomainTreeRepresentationSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public CriteriaDomainTreeRepresentation read(final ByteBuffer buffer) {
	    final EnhancementLinkedRootsSet rootTypes = readValue(buffer, EnhancementLinkedRootsSet.class);
	    final EnhancementSet excludedProperties = readValue(buffer, EnhancementSet.class);
	    final AddToCriteriaTick firstTick = readValue(buffer, AddToCriteriaTick.class);
	    final AddToResultSetTick secondTick = readValue(buffer, AddToResultSetTick.class);
	    final EnhancementRootsMap<ListenedArrayList> includedProperties = readValue(buffer, EnhancementRootsMap.class);
	    return new CriteriaDomainTreeRepresentation(kryo(), rootTypes, excludedProperties, firstTick, secondTick, includedProperties);
	}
    }
}
