package ua.com.fielden.platform.domaintree.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.utils.Pair;

/**
 * A domain manager implementation with all sufficient logic for domain modification / loading. <br><br>
 *
 * <b>Implementation notes:</b><br>
 * 1. After the modifications have been applied manager consists of a map of (entityType -> real enhanced entityType).
 * To play correctly with any type information with enhanced domain you need to use ({@link #getManagedType(Class)} of entityType; dotNotationName) instead of (entityType; dotNotationName).<br>
 * 2. The current version of manager after some modifications (calcProperty has been added/removed/changed) holds a full list of calculated properties for all types.
 * This list should be applied or discarded using {@link #apply()} or {@link #discard()} interface methods.<br>
 * 3.
 *
 * @author TG Team
 *
 */
public final class DomainTreeEnhancer extends AbstractDomainTree implements IDomainTreeEnhancer {
    private static final long serialVersionUID = -7996646149855822266L;
    private static final Logger logger = Logger.getLogger(DomainTreeEnhancer.class);

    /** Holds a set of root types to work with. */
    private final Set<Class<?>> rootTypes;
    /** Holds byte arrays of <b>enhanced</b> (and only <b>enhanced</b>) types mapped to their original root types. The first item in the list is "enhanced root type's" array. */
    private final Map<Class<?>, List<ByteArray>> originalAndEnhancedRootTypesArrays;
    /** Holds current domain differences from "standard" domain (all calculated properties for all root types). */
    private final Map<Class<?>, List<ICalculatedProperty>> calculatedProperties;
    /** Holds a current (and already applied / loaded) snapshot of domain -- consists of a pairs of root types: [original -> real] (or [original -> original] in case of not enhanced type) */
    private final transient Map<Class<?>, Class<?>> originalAndEnhancedRootTypes;

    public static class ByteArray {
	private final byte[] array;

	protected ByteArray() {
	    array = null;
	}

	public ByteArray(final byte[] array) {
	    this.array = array;
	}

	public byte[] getArray() {
	    return array;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + Arrays.hashCode(array);
	    return result;
	}

	@Override
	public boolean equals(final Object obj) {
	    if (this == obj) {
		return true;
	    }
	    if (obj == null) {
		return false;
	    }
	    if (getClass() != obj.getClass()) {
		return false;
	    }
	    final ByteArray other = (ByteArray) obj;
	    if (!Arrays.equals(array, other.array)) {
		return false;
	    }
	    return true;
	}
    }

    /**
     * Constructs a new instance of domain enhancer with clean not enhanced domain.
     *
     * @param rootTypes -- root types
     */
    public DomainTreeEnhancer(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, rootTypes, new HashMap<Class<?>, List<ByteArray>>(), null);
    }

    /**
     * Constructs a new instance of domain enhancer with an enhanced domain (provided using byte arrays of <b>enhanced</b> (and only <b>enhanced</b>) types mapped to their original types).
     *
     * @param rootTypes -- root types
     * @param originalAndEnhancedRootTypesArrays -- a map of pair [original => enhanced class byte array] root types
     *
     */
    public DomainTreeEnhancer(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Map<Class<?>, List<ByteArray>> originalAndEnhancedRootTypesArrays) {
	this(serialiser, rootTypes, originalAndEnhancedRootTypesArrays, null);
    }

    /**
     * Constructs a new instance of domain enhancer with an <b>enhanced</b> (and only <b>enhanced</b>) types and current calculated properties (which can differ from accepted enhanced domain).
     *
     * @param rootTypes -- root types
     * @param originalAndEnhancedRootTypesArrays -- a map of pair [original => enhanced class byte array] root types
     * @param calculatedProperties -- current version of calculated properties
     */
    public DomainTreeEnhancer(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Map<Class<?>, List<ByteArray>> originalAndEnhancedRootTypesArrays, final Map<Class<?>, List<ICalculatedProperty>> calculatedProperties) {
	super(serialiser);
	this.rootTypes = new HashSet<Class<?>>();
	this.rootTypes.addAll(rootTypes);

	this.originalAndEnhancedRootTypesArrays = new HashMap<Class<?>, List<ByteArray>>();
	this.originalAndEnhancedRootTypesArrays.putAll(originalAndEnhancedRootTypesArrays);

	// Initialise a map with enhanced (or not) types. A new instance of classLoader is needed for loading enhanced "byte arrays".
	final DynamicEntityClassLoader classLoader = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());
	this.originalAndEnhancedRootTypes = new HashMap<Class<?>, Class<?>>();
	for (final Class<?> rootType : this.rootTypes) {
	    this.originalAndEnhancedRootTypes.put(rootType, rootType);
	}
	for (final Entry<Class<?>, List<ByteArray>> entry : this.originalAndEnhancedRootTypesArrays.entrySet()) {
	    final List<ByteArray> arrays = new ArrayList<ByteArray>(entry.getValue());
	    if (!arrays.isEmpty()) {
		this.originalAndEnhancedRootTypes.put(entry.getKey(), classLoader.defineClass(arrays.get(0).getArray()));
		arrays.remove(0);
		for (final ByteArray array : arrays) {
		    classLoader.defineClass(array.getArray());
		}
	    }
	}

	this.calculatedProperties = new HashMap<Class<?>, List<ICalculatedProperty>>();
	this.calculatedProperties.putAll(calculatedProperties == null ? extractAll(originalAndEnhancedRootTypes, this, getFactory()) : calculatedProperties);
    }

    private static Map<Class<?>, Pair<Class<?>, List<byte[]>>> createOriginalAndEnhancedRootTypesFromRootTypes(final Set<Class<?>> rootTypes) {
	final Map<Class<?>, Pair<Class<?>, List<byte[]>>> originalAndEnhancedRootTypes = new HashMap<Class<?>, Pair<Class<?>, List<byte[]>>>();
	for (final Class<?> rootType : rootTypes) {
	    originalAndEnhancedRootTypes.put(rootType, new Pair<Class<?>, List<byte[]>>(rootType, new ArrayList<byte[]>()));
	}
	return originalAndEnhancedRootTypes;
    }

    @Override
    public Class<?> getManagedType(final Class<?> type) {
	final Class<?> mutatedType = originalAndEnhancedRootTypes.get(type);
	return mutatedType == null ? type : mutatedType;
    }

    @Override
    public void apply() {
	//////////// Performs migration [calculatedProperties => originalAndEnhancedRootTypes] ////////////
	final Map<Class<?>, Pair<Class<?>, List<byte[]>>> freshOriginalAndEnhancedRootTypes = generateHierarchy(originalAndEnhancedRootTypes.keySet(), calculatedProperties);
	originalAndEnhancedRootTypes.clear();
	originalAndEnhancedRootTypesArrays.clear();
	for (final Entry<Class<?>, Pair<Class<?>, List<byte[]>>> entry : freshOriginalAndEnhancedRootTypes.entrySet()) {
	    originalAndEnhancedRootTypes.put(entry.getKey(), entry.getValue().getKey());
	    final List<ByteArray> arrays = new ArrayList<ByteArray>();
	    for (final byte[] array : entry.getValue().getValue()) {
		arrays.add(new ByteArray(array));
	    }
	    originalAndEnhancedRootTypesArrays.put(entry.getKey(), arrays);
	}
    }

    /**
     * Fully generates a new hierarchy of "originalAndEnhancedRootTypes" that conform to "calculatedProperties".
     *
     * @param rootTypes
     * @param calculatedProperties
     * @return
     */
    protected static Map<Class<?>, Pair<Class<?>, List<byte[]>>> generateHierarchy(final Set<Class<?>> rootTypes, final Map<Class<?>, List<ICalculatedProperty>> calculatedProperties) {
	// single classLoader instance is needed for single "apply" transaction
	final DynamicEntityClassLoader classLoader = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());

	final Map<Class<?>, Pair<Class<?>, List<byte[]>>> originalAndEnhancedRootTypes = createOriginalAndEnhancedRootTypesFromRootTypes(rootTypes);

	final Map<Pair<Class<?>, String>, Map<String, ICalculatedProperty>> groupedCalculatedProperties = groupByPaths(calculatedProperties);

	// iterate through calculated property places (e.g. Vehicle.class+"" or WorkOrder.class+"veh.status") with no care about order
	for (final Entry<Pair<Class<?>, String>, Map<String, ICalculatedProperty>> placeAndProps : groupedCalculatedProperties.entrySet()) {
	    final Map<String, ICalculatedProperty> props = placeAndProps.getValue();
	    if (props != null && !props.isEmpty()) {
		final Class<?> originalRoot = placeAndProps.getKey().getKey();
		final Class<?> realRoot = originalAndEnhancedRootTypes.get(originalRoot).getKey();
		// a path to calculated properties
		final String path = placeAndProps.getKey().getValue();

		final NewProperty[] newProperties = new NewProperty[props.size()];
		int i = 0;
		for (final Entry<String, ICalculatedProperty> nameWithProp : props.entrySet()) {
		    final ICalculatedProperty prop = nameWithProp.getValue();
		    final Annotation calcAnnotation = new CalculatedAnnotation().contextualExpression(prop.getContextualExpression()).contextPath(prop.getContextPath()).origination(prop.getOriginationProperty()).attribute(prop.getAttribute()).category(prop.category()).newInstance();
		    newProperties[i++] = new NewProperty(nameWithProp.getKey(), prop.resultType(), false, prop.getTitle(), prop.getDesc(), calcAnnotation);
		}
		// determine a "real" parent type:
		final Class<?> realParentToBeEnhanced = StringUtils.isEmpty(path) ? realRoot : PropertyTypeDeterminator.determinePropertyType(realRoot, path);
		try {
		    final List<byte[]> existingByteArrays = new ArrayList<byte[]>(originalAndEnhancedRootTypes.get(originalRoot).getValue());

		    // generate & load new type enhanced by calculated properties
		    final Class<?> realParentEnhanced = classLoader.startModification(realParentToBeEnhanced.getName()).addProperties(newProperties).endModification();
		    // propagate enhanced type to root
		    final Pair<Class<?>, List<byte[]>> rootAfterPropagationAndAdditionalByteArrays = propagateEnhancedTypeToRoot(realParentEnhanced, realRoot, path, classLoader);
		    final Class<?> rootAfterPropagation = rootAfterPropagationAndAdditionalByteArrays.getKey();
		    // insert new byte arrays into beginning (the first item is an array of root type)
		    if (existingByteArrays.isEmpty()) {
			existingByteArrays.addAll(rootAfterPropagationAndAdditionalByteArrays.getValue());
		    } else {
			existingByteArrays.addAll(0, rootAfterPropagationAndAdditionalByteArrays.getValue());
		    }
		    // replace relevant root type in cache
		    originalAndEnhancedRootTypes.put(originalRoot, new Pair<Class<?>, List<byte[]>>(rootAfterPropagation, existingByteArrays));
		} catch (final ClassNotFoundException e) {
		    e.printStackTrace();
		    logger.error(e);
		    throw new RuntimeException(e);
		}
	    }
	}
	return originalAndEnhancedRootTypes;
    }

    /**
     * Groups calc props into the map by its domain paths.
     *
     * @param calculatedProperties
     * @return
     */
    private static Map<Pair<Class<?>, String>, Map<String, ICalculatedProperty>> groupByPaths(final Map<Class<?>, List<ICalculatedProperty>> calculatedProperties) {
	final Map<Pair<Class<?>, String>, Map<String, ICalculatedProperty>> grouped = new HashMap<Pair<Class<?>,String>, Map<String,ICalculatedProperty>>();
	for (final Entry<Class<?>, List<ICalculatedProperty>> entry : calculatedProperties.entrySet()) {
	    final List<ICalculatedProperty> props = entry.getValue();
	    if (props != null && !props.isEmpty()) {
		final Class<?> root = entry.getKey();
		for (final ICalculatedProperty prop : props) {
		    final Pair<Class<?>, String> key = AbstractDomainTree.key(root, prop.path());
		    if (!grouped.containsKey(key)) {
			grouped.put(key, new HashMap<String, ICalculatedProperty>());
		    }
		    grouped.get(key).put(prop.name(), prop);
		}
	    }
	}
	return grouped;
    }

    /**
     * Propagates recursively the <code>enhancedType</code> from place [root; path] to place [root; ""].
     *
     * @param enhancedType -- the type to replace the current type of property "path" in "root" type
     * @param root
     * @param path
     * @param classLoader
     * @return
     */
    protected static Pair<Class<?>, List<byte[]>> propagateEnhancedTypeToRoot(final Class<?> enhancedType, final Class<?> root, final String path, final DynamicEntityClassLoader classLoader) {
	final List<byte[]> additionalByteArrays = new ArrayList<byte[]>();
	// add a byte array corresponding to "enhancedType"
	additionalByteArrays.add(classLoader.getCachedByteArray(enhancedType.getName()));

	if (StringUtils.isEmpty(path)) { // replace current root type with new one
	    return new Pair<Class<?>, List<byte[]>>(enhancedType, additionalByteArrays);
	}
	final Pair<Class<?>, String> transformed = PropertyTypeDeterminator.transform(root, path);

	final String nameOfTheTypeToAdapt = transformed.getKey().getName();
	final String nameOfThePropertyToAdapt = transformed.getValue();
	try {
	    // change type if simple field and change signature in case of collectional field
	    final boolean isCollectional = Collection.class.isAssignableFrom(PropertyTypeDeterminator.determineClass(transformed.getKey(), transformed.getValue(), true, false));
	    final NewProperty propertyToBeModified = !isCollectional ? NewProperty.changeType(nameOfThePropertyToAdapt, enhancedType) : NewProperty.changeTypeSignature(nameOfThePropertyToAdapt, enhancedType);
	    final Class<?> nextEnhancedType = classLoader.startModification(nameOfTheTypeToAdapt).modifyProperties(propertyToBeModified).endModification();
	    //	    // add a byte array corresponding to "nextEnhancedType"
	    //	    additionalByteArrays.add(classLoader.getCachedByteArray(nextEnhancedType.getName()));

	    final String nextProp = PropertyTypeDeterminator.isDotNotation(path) ? PropertyTypeDeterminator.penultAndLast(path).getKey() : "";
	    final Pair<Class<?>, List<byte[]>> lastTypeThatIsRootAndPropagatedArrays = propagateEnhancedTypeToRoot(nextEnhancedType, root, nextProp, classLoader);
	    // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
	    // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
	    // TODO TODO TODO TODO TODO TODO TODO comment following line   TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
	    // TODO TODO TODO TODO TODO TODO TODO to go to previous logic with single enhanced root type loading   TODO TODO
	    // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
	    additionalByteArrays.addAll(0, lastTypeThatIsRootAndPropagatedArrays.getValue());

	    return new Pair<Class<?>, List<byte[]>>(lastTypeThatIsRootAndPropagatedArrays.getKey(), additionalByteArrays);
	} catch (final ClassNotFoundException e) {
	    e.printStackTrace();
	    logger.error(e);
	    throw new RuntimeException(e);
	}
    }

    @Override
    public void discard() {
	//////////// Performs migration [originalAndEnhancedRootTypes => calculatedProperties] ////////////
	calculatedProperties.clear();
	calculatedProperties.putAll(extractAll(originalAndEnhancedRootTypes, this, getFactory()));
    }

    /**
     * Extracts all calculated properties from enhanced root types.
     *
     * @param originalAndEnhancedRootTypes
     * @param dte
     * @return
     */
    protected static Map<Class<?>, List<ICalculatedProperty>> extractAll(final Map<Class<?>, Class<?>> originalAndEnhancedRootTypes, final DomainTreeEnhancer dte, final EntityFactory factory) {
	final Map<Class<?>, List<ICalculatedProperty>> newCalculatedProperties = new HashMap<Class<?>, List<ICalculatedProperty>>();
	for (final Entry<Class<?>, Class<?>> originalAndEnhanced : originalAndEnhancedRootTypes.entrySet()) {
	    final List<ICalculatedProperty> calc = reload(originalAndEnhanced.getValue(), originalAndEnhanced.getKey(), "", dte, factory);
	    for (final ICalculatedProperty calculatedProperty : calc) {
		addCalculatedProperty(calculatedProperty, newCalculatedProperties, originalAndEnhancedRootTypes);
	    }
	}
	return newCalculatedProperties;
    }

    /**
     * Extracts recursively <code>calculatedProperties</code> from enhanced domain <code>type</code>.
     *
     * @param type -- enhanced type to load properties
     * @param root -- not enhanced root type
     * @param path -- the path to loaded calculated props
     * @param dte
     */
    private static List<ICalculatedProperty> reload(final Class<?> type, final Class<?> root, final String path, final DomainTreeEnhancer dte, final EntityFactory factory) {
	final List<ICalculatedProperty> newCalcProperties = new ArrayList<ICalculatedProperty>();
	if (!DynamicEntityClassLoader.isEnhanced(type)) {
	    return newCalcProperties;
	} else {
	    // add all first level calculated properties if any exist
	    for (final Field calculatedField : Finder.findRealProperties(type, Calculated.class)) {
		final Calculated calcAnnotation = calculatedField.getAnnotation(Calculated.class);
		if (calcAnnotation != null && !StringUtils.isEmpty(calcAnnotation.contextualExpression())) {
		    final Title titleAnnotation = calculatedField.getAnnotation(Title.class);
		    final String title = titleAnnotation == null ? "" : titleAnnotation.value();
		    final String desc = titleAnnotation == null ? "" : titleAnnotation.desc();
		    final ICalculatedProperty calculatedProperty = CalculatedProperty.createWithoutValidation(factory, root, calcAnnotation.contextPath(), calcAnnotation.contextualExpression(), title, desc, calcAnnotation.attribute(), calcAnnotation.origination(), dte);
		    newCalcProperties.add(calculatedProperty);
		}
	    }
	    // reload all "entity-typed" sub-properties if they are enhanced
	    for (final Field entityProp : Finder.findPropertiesThatAreEntities(type)) {
		final String newPath = StringUtils.isEmpty(path) ? entityProp.getName() : (path + "." + entityProp.getName());
		newCalcProperties.addAll(reload(entityProp.getType(), root, newPath, dte, factory));
	    }
	    return newCalcProperties;
	}
    }

    @Override
    public ICalculatedProperty validateCalculatedPropertyKey(final Class<?> root, final String pathAndName, final Boolean correctIfExists) {
	return validateCalculatedPropertyKey1(root, pathAndName, correctIfExists, calculatedProperties, originalAndEnhancedRootTypes);
    }

    /**
     * Validates the calculated property by its key [root + pathAndName]. Validation depends on current state of enhanced domain.
     *
     * @param root
     * @param pathAndName
     * @param correctIfExists -- specify <code>true</code> for "correct" key if the calc property exists (when property getting/removing), <code>false</code> for "incorrect" key if the calc property exists (when property adding).
     * @param calculatedProperties
     * @param originalAndEnhancedRootTypes
     * @return
     */
    private static ICalculatedProperty validateCalculatedPropertyKey1(final Class<?> root, final String pathAndName, final Boolean correctIfExists, final Map<Class<?>, List<ICalculatedProperty>> calculatedProperties, final Map<Class<?>, Class<?>> originalAndEnhancedRootTypes) {
	if (StringUtils.isEmpty(pathAndName)) {
	    throw new IncorrectCalcPropertyKeyException("The calculated property pathAndName cannot be empty.");
	}
	// throw exception when the place is not in the context of root type
	if (!originalAndEnhancedRootTypes.keySet().contains(root)) {
	    throw new IncorrectCalcPropertyKeyException("The calculated property [" + pathAndName + "] is not in the context of any root type.");
	}

	final Pair<String, String> pathAndName1 = PropertyTypeDeterminator.isDotNotation(pathAndName) ? PropertyTypeDeterminator.penultAndLast(pathAndName) : new Pair<String, String>("", pathAndName);
	final String path = pathAndName1.getKey();
	validatePath(root, path, "The place [" + path + "] in type [" + root.getSimpleName() + "] of calculated property does not exist.");

	final ICalculatedProperty calculatedProperty = calculatedProperty(root, pathAndName, calculatedProperties);
	if (correctIfExists == null) {
	    return calculatedProperty;
	} else if (correctIfExists) {
	    if (calculatedProperty == null) {
		throw new IncorrectCalcPropertyKeyException("The calculated property with name [" + pathAndName + "] does not exist.");
	    }
	    return calculatedProperty;
	} else {
	    if (calculatedProperty != null) {
		throw new IncorrectCalcPropertyKeyException("The calculated property with name [" + pathAndName + "] already exists.");
	    }
	    try {
		PropertyTypeDeterminator.determinePropertyType(root, pathAndName);
		// if (AbstractDomainTreeRepresentation.isCalculated(root, pathAndName)) {
		//     return null; // the property with a suggested name exists in original domain, but it is "calculated", which is correct
		// }
	    } catch (final Exception e) {
		return null; // the property with a suggested name does not exist in original domain, which is correct
	    }
	    throw new IncorrectCalcPropertyKeyException("The property with the name [" + pathAndName + "] already exists in original domain (inside " + root.getSimpleName() + " root). Please try another name for calculated property.");
	}
    }

    protected static void validatePath(final Class<?> root, final String path, final String message) {
	if (path == null) {
	    throw new IncorrectCalcPropertyKeyException(message);
	}
	if (!"".equals(path)) { // validate path
	    try {
		PropertyTypeDeterminator.determinePropertyType(root, path); // throw exception when the place does not exist
	    } catch (final Exception e) {
		throw new IncorrectCalcPropertyKeyException(message);
	    }
	}
    }

    /**
     * Iterates through the set of calculated properties to find appropriate calc property.
     *
     * @param root
     * @param pathAndName
     * @param calculatedProperties
     * @return
     */
    private static ICalculatedProperty calculatedProperty(final Class<?> root, final String pathAndName, final Map<Class<?>, List<ICalculatedProperty>> calculatedProperties) {
	final List<ICalculatedProperty> calcProperties = calculatedProperties.get(root);
	if (calcProperties != null) {
	    for (final ICalculatedProperty prop : calcProperties) {
		if (pathAndName.equals(prop.pathAndName())) {
		    return prop;
		}
	    }
	}
	return null;
    }

    /**
     * Validates and adds calc property to a calculatedProperties.
     *
     * @param calculatedProperty
     * @param calculatedProperties
     * @param originalAndEnhancedRootTypes
     */
    private static void addCalculatedProperty(final ICalculatedProperty calculatedProperty, final Map<Class<?>, List<ICalculatedProperty>> calculatedProperties, final Map<Class<?>, Class<?>> originalAndEnhancedRootTypes) {
	final Class<?> root = calculatedProperty.getRoot();
	validateCalculatedPropertyKey1(root, calculatedProperty.pathAndName(), false, calculatedProperties, originalAndEnhancedRootTypes);

	if (!calculatedProperties.containsKey(root)) {
	    calculatedProperties.put(root, new ArrayList<ICalculatedProperty>());
	}
	final boolean added = calculatedProperties.get(root).add(calculatedProperty);
	if (!added) {
	    logger.warn("The calculated property [" + calculatedProperty.getTitle() + "] with name [" + calculatedProperty.pathAndName() + "] is already present and is trying to be added again.");
	}
    }

    @Override
    public void addCalculatedProperty(final ICalculatedProperty calculatedProperty) {
	addCalculatedProperty(calculatedProperty, calculatedProperties, originalAndEnhancedRootTypes);
    }

    @Override
    public void addCalculatedProperty(final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty) {
	final CalculatedProperty created = CalculatedProperty.createAndValidate(getFactory(), root, contextPath, contextualExpression, title, desc, attribute, originationProperty, this);
	addCalculatedProperty(created);
    }

    @Override
    public ICalculatedProperty getCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName) {
	return validateCalculatedPropertyKey1(rootType, calculatedPropertyName, true, calculatedProperties, originalAndEnhancedRootTypes);
    }

    @Override
    public void removeCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName) {
	final ICalculatedProperty calculatedProperty = validateCalculatedPropertyKey1(rootType, calculatedPropertyName, true, calculatedProperties, originalAndEnhancedRootTypes);
	final boolean removed = calculatedProperties.get(rootType).remove(calculatedProperty);

	if (!removed) {
	    throw new IllegalStateException("The property [" + calculatedPropertyName + "] has been validated but can not be removed.");
	}
	if (calculatedProperties.get(rootType).isEmpty()) {
	    calculatedProperties.remove(rootType);
	}
    }

    /**
     * A specific Kryo serialiser for {@link DomainTreeEnhancer}.
     *
     * @author TG Team
     *
     */
    public static class DomainTreeEnhancerSerialiser extends AbstractDomainTreeSerialiser<DomainTreeEnhancer> {
	public DomainTreeEnhancerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public DomainTreeEnhancer read(final ByteBuffer buffer) {
	    final Set<Class<?>> rootTypes = readValue(buffer, HashSet.class);
	    final Map<Class<?>, List<ByteArray>> originalAndEnhancedRootTypesArrays = readValue(buffer, HashMap.class);
	    final Map<Class<?>, List<ICalculatedProperty>> calculatedProperties = readValue(buffer, HashMap.class);
	    return new DomainTreeEnhancer(kryo(), rootTypes, originalAndEnhancedRootTypesArrays, calculatedProperties);
	}

	@Override
	public void write(final ByteBuffer buffer, final DomainTreeEnhancer domainTreeEnhancer) {
	    writeValue(buffer, domainTreeEnhancer.rootTypes);
	    writeValue(buffer, domainTreeEnhancer.originalAndEnhancedRootTypesArrays);
	    writeValue(buffer, domainTreeEnhancer.calculatedProperties);
	}
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((calculatedProperties == null) ? 0 : calculatedProperties.hashCode());
	result = prime * result + ((originalAndEnhancedRootTypesArrays == null) ? 0 : originalAndEnhancedRootTypesArrays.hashCode());
	result = prime * result + ((rootTypes == null) ? 0 : rootTypes.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final DomainTreeEnhancer other = (DomainTreeEnhancer) obj;
	if (calculatedProperties == null) {
	    if (other.calculatedProperties != null) {
		return false;
	    }
	} else if (!calculatedProperties.equals(other.calculatedProperties)) {
	    return false;
	}
	if (originalAndEnhancedRootTypesArrays == null) {
	    if (other.originalAndEnhancedRootTypesArrays != null) {
		return false;
	    }
	} else if (!originalAndEnhancedRootTypesArrays.equals(other.originalAndEnhancedRootTypesArrays)) {
	    return false;
	}
	if (rootTypes == null) {
	    if (other.rootTypes != null) {
		return false;
	    }
	} else if (!rootTypes.equals(other.rootTypes)) {
	    return false;
	}
	return true;
    }

    protected Map<Class<?>, List<ICalculatedProperty>> calculatedProperties() {
        return calculatedProperties;
    }

    protected Map<Class<?>, Class<?>> getOriginalAndEnhancedRootTypes() {
        return originalAndEnhancedRootTypes;
    }
}
