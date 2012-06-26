package ua.com.fielden.platform.domaintree.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.utils.EntityUtils;
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
    private static final Logger logger = Logger.getLogger(DomainTreeEnhancer.class);

    /** Holds byte arrays & <b>enhanced</b> types mapped to their original root types. Contains pairs of [original -> real & arrays] or [original -> original & emptyArrays] (in case of not enhanced type). */
    private final Map<Class<?>, Pair<Class<?>, Map<String, ByteArray>>> originalAndEnhancedRootTypesAndArrays;

    /** Holds current domain differences from "standard" domain (all calculated properties for all root types). */
    private final transient Map<Class<?>, List<CalculatedProperty>> calculatedProperties;


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
     * Constructs a new instance of domain enhancer with clean, not enhanced, domain.
     *
     * @param rootTypes -- root types
     *
     */
    public DomainTreeEnhancer(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, rootTypes, new HashMap<Class<?>, Map<String, ByteArray>>());
    }

    /**
     * Constructs a new instance of domain enhancer with full information about containing root types (<b>enhanced</b> or not).
     * This primary constructor should be used for serialisation and copying. Please also note that calculated property changes, that were not applied, will be disappeared!
     * So every enhancer should be carefully applied (or discarded) before serialisation.
     *
     */
    public DomainTreeEnhancer(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Map<Class<?>, Map<String, ByteArray>> originalTypesAndEnhancedArrays) {
	super(serialiser);

	this.originalAndEnhancedRootTypesAndArrays = new HashMap<Class<?>, Pair<Class<?>, Map<String, ByteArray>>>();
	// init a map with NOT enhanced types and empty byte arrays.
	this.originalAndEnhancedRootTypesAndArrays.putAll(createOriginalAndEnhancedRootTypesAndArraysFromRootTypes(rootTypes));

	// complement a map with enhanced types. A new instance of classLoader is needed for loading enhanced "byte arrays".
	final DynamicEntityClassLoader classLoader = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());
	for (final Entry<Class<?>, Map<String, ByteArray>> entry : originalTypesAndEnhancedArrays.entrySet()) {
	    final Map<String, ByteArray> arrays = Collections.unmodifiableMap(entry.getValue());
	    if (arrays.isEmpty()) {
		throw new IllegalArgumentException("Enhanced arrays should not be empty for type [" + entry.getKey() + "].");
	    }
	    for (final Entry<String, ByteArray> pathAndArray : arrays.entrySet()) {
		final Class<?> defineClass = classLoader.defineClass(pathAndArray.getValue().getArray());
		if ("".equals(pathAndArray.getKey())) {
		    this.originalAndEnhancedRootTypesAndArrays.put(entry.getKey(), new Pair<Class<?>, Map<String, ByteArray>>(defineClass, new HashMap<String, ByteArray>(entry.getValue())));
		}
	    }
	}

	this.calculatedProperties = new HashMap<Class<?>, List<CalculatedProperty>>();
	this.calculatedProperties.putAll(extractAll(this, true));
    }

    /**
     * Creates a map of [original -> original & emptyArrays] for provided <code>rootTypes</code>.
     *
     * @param rootTypes
     * @return
     */
    private static Map<Class<?>, Pair<Class<?>, Map<String, ByteArray>>> createOriginalAndEnhancedRootTypesAndArraysFromRootTypes(final Set<Class<?>> rootTypes) {
	final Map<Class<?>, Pair<Class<?>, Map<String, ByteArray>>> originalAndEnhancedRootTypesAndArrays = new HashMap<Class<?>, Pair<Class<?>, Map<String, ByteArray>>>();
	for (final Class<?> rootType : rootTypes) {
	    originalAndEnhancedRootTypesAndArrays.put(rootType, new Pair<Class<?>, Map<String, ByteArray>>(rootType, new HashMap<String, ByteArray>()));
	}
	return originalAndEnhancedRootTypesAndArrays;
    }

    @Override
    public Class<?> getManagedType(final Class<?> type) {
	CalculatedProperty.validateRootWithoutRootTypeEnforcement(this, type);
	return originalAndEnhancedRootTypesAndArrays.get(type) == null ? type : originalAndEnhancedRootTypesAndArrays.get(type).getKey();
    }

    @Override
    public List<ByteArray> getManagedTypeArrays(final Class<?> type) {
	CalculatedProperty.validateRoot(this, type);
	final Map<String, ByteArray> byteArrays = originalAndEnhancedRootTypesAndArrays.get(type).getValue();
	return new ArrayList<ByteArray>(byteArrays.values());
    }

    @Override
    public void apply() {
	//////////// Performs migration [calculatedProperties => originalAndEnhancedRootTypes] ////////////
	final Map<Class<?>, Pair<Class<?>, Map<String, ByteArray>>> freshOriginalAndEnhancedRootTypesAndArrays = generateHierarchy(originalAndEnhancedRootTypesAndArrays.keySet(), calculatedProperties);
	originalAndEnhancedRootTypesAndArrays.clear();
	originalAndEnhancedRootTypesAndArrays.putAll(freshOriginalAndEnhancedRootTypesAndArrays);
    }

    /**
     * Fully generates a new hierarchy of "originalAndEnhancedRootTypes" that conform to "calculatedProperties".
     *
     * @param rootTypes
     * @param calculatedProperties
     * @return
     */
    protected static Map<Class<?>, Pair<Class<?>, Map<String, ByteArray>>> generateHierarchy(final Set<Class<?>> rootTypes, final Map<Class<?>, List<CalculatedProperty>> calculatedProperties) {
	// single classLoader instance is needed for single "apply" transaction
	final DynamicEntityClassLoader classLoader = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());
	final Map<Class<?>, Pair<Class<?>, Map<String, ByteArray>>> originalAndEnhancedRootTypes = createOriginalAndEnhancedRootTypesAndArraysFromRootTypes(rootTypes);
	final Map<Class<?>, Map<String, Map<String, CalculatedProperty>>> groupedCalculatedProperties = groupByPaths(calculatedProperties);

	// iterate through calculated property places (e.g. Vehicle.class+"" or WorkOrder.class+"veh.status") with no care about order
	for (final Entry<Class<?>, Map<String, Map<String, CalculatedProperty>>> entry : groupedCalculatedProperties.entrySet()) {
	    final Class<?> originalRoot = entry.getKey();
	    // generate predefined root type name for all calculated properties
	    final String predefinedRootTypeName = new DynamicTypeNamingService().nextTypeName(originalRoot.getName());
	    for (final Entry<String, Map<String, CalculatedProperty>> placeAndProps : entry.getValue().entrySet()) {
		final Map<String, CalculatedProperty> props = placeAndProps.getValue();
		if (props != null && !props.isEmpty()) {
		    final Class<?> realRoot = originalAndEnhancedRootTypes.get(originalRoot).getKey();
		    // a path to calculated properties
		    final String path = placeAndProps.getKey();

		    final NewProperty[] newProperties = new NewProperty[props.size()];
		    int i = 0;
		    for (final Entry<String, CalculatedProperty> nameWithProp : props.entrySet()) {
			final CalculatedProperty prop = nameWithProp.getValue();
			final String originationProperty = prop.getOriginationProperty() == null ? "" : prop.getOriginationProperty();
			final Annotation calcAnnotation = new CalculatedAnnotation().contextualExpression(prop.getContextualExpression()).rootTypeName(predefinedRootTypeName).contextPath(prop.getContextPath()).origination(originationProperty).attribute(prop.getAttribute()).category(prop.category()).newInstance();
			newProperties[i++] = new NewProperty(nameWithProp.getKey(), prop.resultType(), false, prop.getTitle(), prop.getDesc(), calcAnnotation);
		    }
		    // determine a "real" parent type:
		    final Class<?> realParentToBeEnhanced = StringUtils.isEmpty(path) ? realRoot : PropertyTypeDeterminator.determinePropertyType(realRoot, path);
		    try {
			final Map<String, ByteArray> existingByteArrays = new HashMap<String, ByteArray>(originalAndEnhancedRootTypes.get(originalRoot).getValue());

			// generate & load new type enhanced by calculated properties
			final Class<?> realParentEnhanced = classLoader.startModification(realParentToBeEnhanced.getName()).addProperties(newProperties).endModification();
			// propagate enhanced type to root
			final Pair<Class<?>, Map<String, ByteArray>> rootAfterPropagationAndAdditionalByteArrays = propagateEnhancedTypeToRoot(realParentEnhanced, realRoot, path, classLoader);
			final Class<?> rootAfterPropagation = rootAfterPropagationAndAdditionalByteArrays.getKey();
			// insert new byte arrays into beginning (the first item is an array of root type)
			existingByteArrays.putAll(rootAfterPropagationAndAdditionalByteArrays.getValue());
			// replace relevant root type in cache
			originalAndEnhancedRootTypes.put(originalRoot, new Pair<Class<?>, Map<String, ByteArray>>(rootAfterPropagation, existingByteArrays));
		    } catch (final ClassNotFoundException e) {
			e.printStackTrace();
			logger.error(e);
			throw new RuntimeException(e);
		    }
		}
	    }
	    try {
		// modify root type name with predefinedRootTypeName
		final Pair<Class<?>, Map<String, ByteArray>> current = originalAndEnhancedRootTypes.get(originalRoot);
		final Class<?> rootWithPredefinedName = classLoader.startModification(current.getKey().getName()).modifyTypeName(predefinedRootTypeName).endModification();
		final Map<String, ByteArray> byteArraysWithRenamedRoot = new HashMap<String, ByteArray>();

		byteArraysWithRenamedRoot.putAll(current.getValue());
		byteArraysWithRenamedRoot.put("", new ByteArray(classLoader.getCachedByteArray(rootWithPredefinedName.getName())));
		final Pair<Class<?>, Map<String, ByteArray>> neww = new Pair<Class<?>, Map<String, ByteArray>>(rootWithPredefinedName, byteArraysWithRenamedRoot);
		originalAndEnhancedRootTypes.put(originalRoot, neww);
	    } catch (final ClassNotFoundException e) {
		e.printStackTrace();
		logger.error(e);
		throw new RuntimeException(e);
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
    private static Map<Class<?>, Map<String, Map<String, CalculatedProperty>>> groupByPaths(final Map<Class<?>, List<CalculatedProperty>> calculatedProperties) {
	final Map<Class<?>, Map<String, Map<String, CalculatedProperty>>> grouped = new HashMap<Class<?>, Map<String, Map<String, CalculatedProperty>>>();
	for (final Entry<Class<?>, List<CalculatedProperty>> entry : calculatedProperties.entrySet()) {
	    final List<CalculatedProperty> props = entry.getValue();
	    if (props != null && !props.isEmpty()) {
		final Class<?> root = entry.getKey();
		if (!grouped.containsKey(root)) {
		    grouped.put(root, new HashMap<String, Map<String, CalculatedProperty>>());
		}
		for (final CalculatedProperty prop : props) {
		    final String path = prop.path();
		    if (!grouped.get(root).containsKey(path)) {
			grouped.get(root).put(path, new HashMap<String, CalculatedProperty>());
		    }
		    grouped.get(root).get(path).put(prop.name(), prop);
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
    protected static Pair<Class<?>, Map<String, ByteArray>> propagateEnhancedTypeToRoot(final Class<?> enhancedType, final Class<?> root, final String path, final DynamicEntityClassLoader classLoader) {
	final Map<String, ByteArray> additionalByteArrays = new HashMap<String, ByteArray>();
	// add a byte array corresponding to "enhancedType"
	additionalByteArrays.put(path, new ByteArray(classLoader.getCachedByteArray(enhancedType.getName())));

	if (StringUtils.isEmpty(path)) { // replace current root type with new one
	    return new Pair<Class<?>, Map<String, ByteArray>>(enhancedType, additionalByteArrays);
	}
	final Pair<Class<?>, String> transformed = PropertyTypeDeterminator.transform(root, path);

	final String nameOfTheTypeToAdapt = transformed.getKey().getName();
	final String nameOfThePropertyToAdapt = transformed.getValue();
	try {
	    // change type if simple field and change signature in case of collectional field
	    final boolean isCollectional = Collection.class.isAssignableFrom(PropertyTypeDeterminator.determineClass(transformed.getKey(), transformed.getValue(), true, false));
	    final NewProperty propertyToBeModified = !isCollectional ? NewProperty.changeType(nameOfThePropertyToAdapt, enhancedType) : NewProperty.changeTypeSignature(nameOfThePropertyToAdapt, enhancedType);
	    final Class<?> nextEnhancedType = classLoader.startModification(nameOfTheTypeToAdapt).modifyProperties(propertyToBeModified).endModification();
	    final String nextProp = PropertyTypeDeterminator.isDotNotation(path) ? PropertyTypeDeterminator.penultAndLast(path).getKey() : "";
	    final Pair<Class<?>, Map<String, ByteArray>> lastTypeThatIsRootAndPropagatedArrays = propagateEnhancedTypeToRoot(nextEnhancedType, root, nextProp, classLoader);
	    additionalByteArrays.putAll(lastTypeThatIsRootAndPropagatedArrays.getValue());

	    return new Pair<Class<?>, Map<String, ByteArray>>(lastTypeThatIsRootAndPropagatedArrays.getKey(), additionalByteArrays);
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
	calculatedProperties.putAll(extractAll(this, true));
    }

    /**
     * Extracts all calculated properties from enhanced root types.
     *
     * @param dte
     * @return
     */
    protected static Map<Class<?>, List<CalculatedProperty>> extractAll(final DomainTreeEnhancer dte, final boolean validateTitleContextOfExtractedProperties) {
	final Map<Class<?>, List<CalculatedProperty>> newCalculatedProperties = new HashMap<Class<?>, List<CalculatedProperty>>();
	for (final Entry<Class<?>, Pair<Class<?>, Map<String, ByteArray>>> originalAndEnhancedAndArrays : dte.originalAndEnhancedRootTypesAndArrays.entrySet()) {
	    final List<CalculatedProperty> calc = reload(originalAndEnhancedAndArrays.getValue().getKey(), originalAndEnhancedAndArrays.getKey(), "", dte, validateTitleContextOfExtractedProperties);
	    for (final CalculatedProperty calculatedProperty : calc) {
		addCalculatedProperty(calculatedProperty, newCalculatedProperties);
	    }
	}
	return newCalculatedProperties;
    }

    /**
     * Extracts recursively contextual <code>calculatedProperties</code> from enhanced domain <code>type</code>.
     *
     * @param type -- enhanced type to load properties
     * @param root -- not enhanced root type
     * @param path -- the path to loaded calculated props
     * @param dte
     */
    private static List<CalculatedProperty> reload(final Class<?> type, final Class<?> root, final String path, final DomainTreeEnhancer dte, final boolean validateTitleContextOfExtractedProperties) {
	final List<CalculatedProperty> newCalcProperties = new ArrayList<CalculatedProperty>();
	if (!DynamicEntityClassLoader.isEnhanced(type)) {
	    return newCalcProperties;
	} else {
	    // add all first level calculated properties if any exist
	    for (final Field calculatedField : Finder.findRealProperties(type, Calculated.class)) {
		final Calculated calcAnnotation = calculatedField.getAnnotation(Calculated.class);
		if (calcAnnotation != null && !StringUtils.isEmpty(calcAnnotation.value()) && AnnotationReflector.isContextual(calcAnnotation)) {
		    final Title titleAnnotation = calculatedField.getAnnotation(Title.class);
		    final String title = titleAnnotation == null ? "" : titleAnnotation.value();
		    final String desc = titleAnnotation == null ? "" : titleAnnotation.desc();
		    final CalculatedProperty calculatedProperty = CalculatedProperty.createCorrect(dte.getFactory(), root, calcAnnotation.contextPath(), calcAnnotation.value(), title, desc, calcAnnotation.attribute(), "".equals(calcAnnotation.origination()) ? null : calcAnnotation.origination(), dte, validateTitleContextOfExtractedProperties);
		    newCalcProperties.add(calculatedProperty);
		}
	    }
	    // reload all "entity-typed" and "collectional entity-typed" sub-properties if they are enhanced
	    for (final Field prop : Finder.findProperties(type)) {
		if (EntityUtils.isEntityType(prop.getType()) || EntityUtils.isCollectional(prop.getType())) {
		    final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(type, prop.getName());
		    final String newPath = StringUtils.isEmpty(path) ? prop.getName() : (path + "." + prop.getName());
		    newCalcProperties.addAll(reload(propType, root, newPath, dte, validateTitleContextOfExtractedProperties));
		}
	    }
	    return newCalcProperties;
	}
    }

    /**
     * Checks whether calculated property with the suggested name exists (if it does not exist throws {@link IncorrectCalcPropertyException}) and return it.
     *
     * @param root
     * @param pathAndName
     * @return
     */
    protected final CalculatedProperty calculatedPropertyWhichShouldExist(final Class<?> root, final String pathAndName) {
	final CalculatedProperty calculatedProperty = calculatedProperty(root, pathAndName);
	if (calculatedProperty == null) {
	    throw new IncorrectCalcPropertyException("The calculated property with path & name [" + pathAndName + "] does not exist in type [" + root + "].");
	}
	return calculatedProperty;
    }

    /**
     * Iterates through the set of calculated properties to find appropriate calc property.
     *
     * @param root
     * @param pathAndName
     * @return
     */
    protected final CalculatedProperty calculatedProperty(final Class<?> root, final String pathAndName) {
	return calculatedProperty(calculatedProperties.get(root), pathAndName);
    }

    /**
     * Iterates through the set of calculated properties to find appropriate calc property.
     *
     * @param root
     * @param pathAndName
     * @return
     */
    protected static final CalculatedProperty calculatedProperty(final List<CalculatedProperty> calcProperties, final String pathAndName) {
	if (calcProperties != null) {
	    for (final CalculatedProperty prop : calcProperties) {
		if (prop.pathAndName().equals(pathAndName)) {
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
     */
    private static void addCalculatedProperty(final CalculatedProperty calculatedProperty, final Map<Class<?>, List<CalculatedProperty>> calculatedProperties) {
	final Class<?> root = calculatedProperty.getRoot();
	final CalculatedProperty cp = calculatedProperty;
	if (!cp.isValid().isSuccessful()) {
	    throw cp.isValid();
	}
	if (!calculatedProperties.containsKey(root)) {
	    calculatedProperties.put(root, new ArrayList<CalculatedProperty>());
	}
	calculatedProperties.get(root).add(calculatedProperty);
    }

    @Override
    public void addCalculatedProperty(final ICalculatedProperty calculatedProperty) {
	addCalculatedProperty((CalculatedProperty) calculatedProperty, calculatedProperties);
    }

    @Override
    public void addCalculatedProperty(final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty) {
	addCalculatedProperty(CalculatedProperty.createCorrect(getFactory(), root, contextPath, contextualExpression, title, desc, attribute, originationProperty, this));
    }

    @Override
    public ICalculatedProperty getCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName) {
	return calculatedPropertyWhichShouldExist(rootType, calculatedPropertyName);
    }

    @Override
    public ICalculatedProperty copyCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName) {
        return calculatedPropertyWhichShouldExist(rootType, calculatedPropertyName).copy();
    }

    @Override
    public void removeCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName) {
	final CalculatedProperty calculatedProperty = calculatedPropertyWhichShouldExist(rootType, calculatedPropertyName);

	/////////////////
	final List<CalculatedProperty> calcs = calculatedProperties.get(rootType);
	boolean existsInOtherExpressionsAsOriginationProperty = false;
	String containingExpression = null;
	for (final CalculatedProperty calc : calcs) {
	    if (StringUtils.equals(calc.getOriginationProperty(), Reflector.fromAbsotule2RelativePath(calc.getContextPath(), calculatedPropertyName))) {
		existsInOtherExpressionsAsOriginationProperty = true;
		containingExpression = calc.pathAndName();
		break;
	    }
	}
	if (existsInOtherExpressionsAsOriginationProperty) {
	    throw new IllegalArgumentException("Cannot remove a property that exists in other expressions as 'origination' property. See property [" + containingExpression + "].");
	}
	/////////////////

	final boolean removed = calculatedProperties.get(rootType).remove(calculatedProperty);

	if (!removed) {
	    throw new IllegalStateException("The property [" + calculatedPropertyName + "] has been validated but can not be removed.");
	}
	if (calculatedProperties.get(rootType).isEmpty()) {
	    calculatedProperties.remove(rootType);
	}
    }

    @Override
    public Set<Class<?>> rootTypes() {
	return new HashSet<Class<?>>(originalAndEnhancedRootTypesAndArrays.keySet());
    }

    /**
     * Extracts only <b>enhanced</b> type's arrays mapped to original types.
     *
     * @return
     */
    private Map<Class<?>, Map<String, ByteArray>> originalTypesAndEnhancedArrays() {
	final Map<Class<?>, Map<String, ByteArray>> originalTypesAndEnhancedArrays = new HashMap<Class<?>, Map<String, ByteArray>>();
	for (final Entry<Class<?>, Pair<Class<?>, Map<String, ByteArray>>> entry : originalAndEnhancedRootTypesAndArrays.entrySet()) {
	    if (!entry.getValue().getValue().isEmpty()) {
		originalTypesAndEnhancedArrays.put(entry.getKey(), new HashMap<String, ByteArray>(entry.getValue().getValue()));
	    }
	}
	return originalTypesAndEnhancedArrays;
    }

    /**
     * A current snapshot of calculated properties, possibly not applied.
     *
     * @return
     */
    protected Map<Class<?>, List<CalculatedProperty>> calculatedProperties() {
	return calculatedProperties;
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
	    // IMPORTANT : rootTypes() and originalTypesAndEnhancedArrays() are the mirror for "originalAndEnhancedRootTypesAndArrays".
	    // They have no enhanced classes, but have their byte arrays.
	    // So they should be used for serialisation, comparison and hashCode() implementation.
	    final Set<Class<?>> rootTypes = readValue(buffer, HashSet.class);
	    final Map<Class<?>, Map<String, ByteArray>> originalTypesAndEnhancedArrays = readValue(buffer, HashMap.class);
	    return new DomainTreeEnhancer(kryo(), rootTypes, originalTypesAndEnhancedArrays);
	}

	@Override
	public void write(final ByteBuffer buffer, final DomainTreeEnhancer domainTreeEnhancer) {
	    // IMPORTANT : rootTypes() and originalTypesAndEnhancedArrays() are the mirror for "originalAndEnhancedRootTypesAndArrays".
	    // They have no enhanced classes, but have their byte arrays.
	    // So they should be used for serialisation, comparison and hashCode() implementation.
	    writeValue(buffer, domainTreeEnhancer.rootTypes());
	    writeValue(buffer, domainTreeEnhancer.originalTypesAndEnhancedArrays());
	}
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	// IMPORTANT : rootTypes() and originalTypesAndEnhancedArrays() are the mirror for "originalAndEnhancedRootTypesAndArrays".
	// They have no enhanced classes, but have their byte arrays.
	// So they should be used for serialisation, comparison and hashCode() implementation.
	result = prime * result + rootTypes().hashCode();
	result = prime * result + originalTypesAndEnhancedArrays().hashCode();
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final DomainTreeEnhancer other = (DomainTreeEnhancer) obj;
	// IMPORTANT : rootTypes() and originalTypesAndEnhancedArrays() are the mirror for "originalAndEnhancedRootTypesAndArrays".
	// They have no enhanced classes, but have their byte arrays.
	// So they should be used for serialisation, comparison and hashCode() implementation.
	return rootTypes().equals(other.rootTypes()) && originalTypesAndEnhancedArrays().equals(other.originalTypesAndEnhancedArrays());
    }
}
