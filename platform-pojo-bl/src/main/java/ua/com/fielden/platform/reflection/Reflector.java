package ua.com.fielden.platform.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.validation.annotation.GreaterOrEqual;
import ua.com.fielden.platform.entity.validation.annotation.Max;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * This is a helper class to provide some commonly used method for retrieval of RTTI not provided directly by the Java reflection package.
 *
 * @author TG Team
 *
 */
public final class Reflector {
    private final static Map<MethodKey, Pair<Method, NoSuchMethodException>> methods = new HashMap<>();

    /** A symbol that represents a separator between properties in property path expressions. */
    public static final String DOT_SPLITTER = "\\.";
    /**
     * A symbol used as the property name substitution in property path expressions representing the next level up in the context of nested properties. Should occur only at the
     * beginning of the expression. There can be several sequentially linked ← separated by dot splitter.
     */
    public static final String UP_LEVEL = "←";

    /**
     * Let's hide default constructor, which is not needed for a static class.
     */
    private Reflector() {
    }

    // ========================================================================================================
    /////////////////////////////// Getting methods ///////////////////////////////////////////////////////////

    /**
     * This is a helper method used to walk along class hierarchy in search of the specified method.
     *
     * @param startWithClass
     * @param method
     * @param arguments
     * @return
     * @throws NoSuchMethodException
     * @throws Exception
     */
    public static Method getMethod(final Class<?> startWithClass, final String methodName, final Class<?>... arguments) throws NoSuchMethodException {
	try {
	    // setKey is a special case, because property "key" has a parametrised type that extends comparable.
	    // this means that there are two cases:
	    // 1. setter is overridden and a final key type is specified there -- need to use the assigned type as input parameter to setter in order to find it;
	    // 2. setter is not overridden -- need to use the lowest common denominator (Comparable) as input parameter to setter in order to find it.
	    if ("setKey".equals(methodName)) {
		try {
		    return getMethodForClass(startWithClass, methodName, arguments);
		} catch (final NoSuchMethodException e) {
		    return getMethodForClass(startWithClass, methodName, Comparable.class);
		}
	    }
	    return getMethodForClass(startWithClass, methodName, arguments);
	} catch (final NoSuchMethodException e) {
	    if (AbstractUnionEntity.class.isAssignableFrom(startWithClass)
		    && AbstractUnionEntity.commonMethodNames((Class<AbstractUnionEntity>) startWithClass).contains(methodName)) {
		return getMethodForClass(AbstractUnionEntity.unionProperties((Class<AbstractUnionEntity>) startWithClass).get(0).getType(), methodName, arguments);
	    }
	}
	throw new NoSuchMethodException("There is no method [" + methodName + "] in class [" + startWithClass.getSimpleName() + "] with arguments [" + Arrays.asList(arguments)
		+ "].");
    }

    /** Clear cached methods. */
    public static void clearMethodsCache() {
	methods.clear();
    }

    /**
     * Returns method specified with methodName from {@code startWithClass} class.
     *
     * @param startWithClass
     * @param methodName
     * @param arguments
     * @return
     * @throws NoSuchMethodException
     */
    protected static Method getMethodForClass(final Class<?> startWithClass, final String methodName, final Class<?>... arguments) throws NoSuchMethodException {
	Class<?> klass = startWithClass;
	while (klass != Object.class) { // need to iterated thought hierarchy in
	    // order to retrieve fields from above
	    // the current instance
	    try {
		return getDeclaredMethod(klass, methodName, arguments);
	    } catch (final NoSuchMethodException e) {
		klass = klass.getSuperclass();
	    }
	}
	throw new NoSuchMethodException(methodName);
    }

    private static class MethodKey {
	private final String klassName;
	private final String methodName;
	private final List<String> argumentsTypeNames;

	public MethodKey(final Class<?> klass, final String methodName, final Class<?>... arguments) {
	    this.klassName = klass.getName();
	    this.methodName = methodName;
	    argumentsTypeNames = new ArrayList<>();
	    for (final Class<?> argument : arguments) {
		argumentsTypeNames.add(argument.getName());
	    }
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((argumentsTypeNames == null) ? 0 : argumentsTypeNames.hashCode());
	    result = prime * result + ((klassName == null) ? 0 : klassName.hashCode());
	    result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
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
	    final MethodKey other = (MethodKey) obj;
	    if (argumentsTypeNames == null) {
		if (other.argumentsTypeNames != null) {
		    return false;
		}
	    } else if (!argumentsTypeNames.equals(other.argumentsTypeNames)) {
		return false;
	    }
	    if (klassName == null) {
		if (other.klassName != null) {
		    return false;
		}
	    } else if (!klassName.equals(other.klassName)) {
		return false;
	    }
	    if (methodName == null) {
		if (other.methodName != null) {
		    return false;
		}
	    } else if (!methodName.equals(other.methodName)) {
		return false;
	    }
	    return true;
	}
    }

    private static synchronized Method getDeclaredMethod(final Class<?> klass, final String methodName, final Class<?>... arguments) throws NoSuchMethodException, SecurityException {
	// return klass.getDeclaredMethod(methodName, arguments);
	final MethodKey methodKey = new MethodKey(klass, methodName, arguments);
	final Pair<Method, NoSuchMethodException> methodOrException = methods.get(methodKey);
	if (methodOrException == null) {
	    try {
		final Method method = klass.getDeclaredMethod(methodName, arguments);
		methods.put(methodKey, new Pair<Method, NoSuchMethodException>(method, null));
		return method;
	    } catch (final NoSuchMethodException e1) {
		methods.put(methodKey, new Pair<Method, NoSuchMethodException>(null, e1));
		throw e1;
	    } catch (final SecurityException e2) {
		throw e2;
	    }
	} else if (methodOrException.getKey() != null) {
	    return methodOrException.getKey();
	} else {
	    throw methodOrException.getValue();
	}
    }

    /**
     * Returns constructor specified from {@code startWithClass} class.
     *
     * @param startWithClass
     * @param methodName
     * @param arguments
     * @return
     * @throws NoSuchMethodException
     */
    public static <L> Constructor<? super L> getConstructorForClass(final Class<L> startWithClass, final Class<?>... arguments) throws NoSuchMethodException {
	Class<? super L> klass = startWithClass;
	while (klass != Object.class) { // need to iterated thought hierarchy in
	    // order to retrieve fields from above
	    // the current instance
	    try {
		return klass.getConstructor(arguments);
	    } catch (final NoSuchMethodException e) {
		klass = klass.getSuperclass();
	    }
	}
	throw new NoSuchMethodException("constructor with " + Arrays.asList(arguments));
    }

    /**
     * Returns the method specified with methodName and the array of it's arguments. In order to determine correct method it uses instances instead of classes.
     *
     * @param instance
     * @param methodName
     * @param arguments
     * @return
     * @throws NoSuchMethodException
     */
    public static Method getMethod(final Object instance, final String methodName, final Class<?>... arguments) throws NoSuchMethodException {
	try {
	    return getMethodForClass(instance instanceof AbstractEntity ? ((AbstractEntity) instance).getType() : instance.getClass(), methodName, arguments);
	} catch (final NoSuchMethodException e) {
	    if (instance instanceof AbstractUnionEntity
		    && AbstractUnionEntity.commonMethodNames((Class<AbstractUnionEntity>) ((AbstractUnionEntity) instance).getType()).contains(methodName)) {
		final AbstractEntity activeEntity = ((AbstractUnionEntity) instance).activeEntity();
		if (activeEntity != null) {
		    return getMethodForClass(activeEntity.getType(), methodName, arguments);
		}
	    }
	}
	throw new NoSuchMethodException(methodName);
    }

    /**
     * Depending on the type of the field, the getter may start not with ''get'' but with ''is''. This method tries to determine a correct getter.
     *
     * @param propertyName
     * @param entity
     * @return
     * @throws Exception
     */
    public static Method obtainPropertyAccessor(final Class<?> entityClass, final String propertyName) throws NoSuchMethodException {
	final String propertyNameInGetter = propertyName.toUpperCase().charAt(0) + propertyName.substring(1);
	try {
	    return Reflector.getMethod(entityClass, "get" + propertyNameInGetter);
	} catch (final Exception e) {
	    return Reflector.getMethod(entityClass, "is" + propertyNameInGetter);
	}
    }

    /**
     * Tries to obtain property setter for property, specified using dot-notation. Heavily uses
     * {@link PropertyTypeDeterminator#determinePropertyTypeWithoutKeyTypeDetermination(Class, String)} to obtain penult property in dot-notation
     *
     * @param entityClass
     * @param dotNotationExp
     * @return
     * @throws NoSuchMethodException
     * @throws Exception
     */
    public static Method obtainPropertySetter(final Class<?> entityClass, final String dotNotationExp) throws NoSuchMethodException {
	if (StringUtils.isEmpty(dotNotationExp) || dotNotationExp.contains("()")) {
	    throw new IllegalArgumentException("DotNotationExp could not be empty or could not define construction with methods.");
	}
	final Pair<Class<?>, String> transformed = PropertyTypeDeterminator.transform(entityClass, dotNotationExp);
	return Reflector.getMethod(transformed.getKey(), "set" + transformed.getValue().substring(0, 1).toUpperCase() + transformed.getValue().substring(1), PropertyTypeDeterminator.determineClass(transformed.getKey(), transformed.getValue(), AbstractEntity.KEY.equalsIgnoreCase(transformed.getValue()), false));
    }

    // ========================================================================================================
    /////////////////////////////// Miscellaneous utilities ///////////////////////////////////////////////////

    /**
     * A contract for determining if the property of specified type <code>propertyType</code> could be sortable or not. For now - only property of AE type with composite key could
     * not be sortable.
     *
     * @param propertyType
     * @return
     */
    public static boolean notSortable(final Class<?> propertyType) {
	final KeyType keyType = propertyType.getAnnotation(KeyType.class);
	return AbstractEntity.class.isAssignableFrom(propertyType) && keyType != null && DynamicEntityKey.class.isAssignableFrom(keyType.value());
    }

    /**
     * Returns min and max possible values for property.
     *
     * @param entity
     * @param propertyName
     * @return
     */
    public static Pair<Comparable, Comparable> extractValidationLimits(final AbstractEntity<?> entity, final String propertyName) {
	final List<Field> fields = Finder.findProperties(entity.getType());
	Comparable<?> min = null, max = null;
	for (final Field field : fields) { // for each property field
	    if (field.getName().equals(propertyName)) { //
		final List<Annotation> propertyValidationAnotations = entity.extractValidationAnnotationForProperty(field, PropertyTypeDeterminator.determinePropertyType(entity.getType(), propertyName), false);
		for (final Annotation annotation : propertyValidationAnotations) {
		    if (annotation instanceof GreaterOrEqual) {
			min = ((GreaterOrEqual) annotation).value();
		    } else if (annotation instanceof Max) {
			max = ((Max) annotation).value();
		    }
		}
	    }
	}
	if (min == null) {
	    min = Integer.MIN_VALUE;
	}
	if (max == null) {
	    max = Integer.MAX_VALUE;
	}
	return new Pair<Comparable, Comparable>(min, max);
    }

    /**
     * Indicates whether specified class is synthetic entity or not.
     *
     * @param clazz
     * @return
     */
    public static boolean isSynthetic(final Class<?> clazz) {
	return EntityUtils.isQueryBasedEntityType((Class<? extends AbstractEntity<?>>) clazz);
    }

    /**
     * Returns a list of parameters declared for the specified annotation type. An empty list is returned in case where there are no parameter declarations.
     *
     * @param annotationType
     * @return
     */
    public static List<String> annotataionParams(final Class<? extends Annotation> annotationType) {
	final List<String> names = new ArrayList<String>();
	for (final Method param : annotationType.getDeclaredMethods()) {
	    names.add(param.getName());
	}
	return names;
    }

    /**
     * Obtains and returns a pair of parameter type and its value for the specified annotation parameter.
     *
     * @param annotation
     * @param paramName
     * @return parameter value
     */
    public static Pair<Class<?>, Object> getAnnotationParamValue(final Annotation annotation, final String paramName) {
	try {
	    final Method method = annotation.getClass().getDeclaredMethod(paramName);
	    method.setAccessible(true);
	    final Pair<Class<?>, Object> result = new Pair<Class<?>, Object>(method.getReturnType(), method.invoke(annotation));
	    return result;
	} catch (final Exception e) {
	    throw new IllegalStateException(e);
	}
    }

    /**
     * Converts a relative property path to an absolute path with respect to the provided context.
     *
     * @param context
     *            -- the dot notated property path from the root, which indicated the relative position in the type tree against which all other paths should be calculated.
     * @param relativePropertyPath
     *            -- relative property path, which may contain ← and dots for separating individual properties.
     * @return
     */
    public static String fromRelative2AbsotulePath(final String context, final String relativePropertyPath) {
	// if the relativePropertyPath path does not start with ← then it is absolute already
	if (!relativePropertyPath.startsWith("←")) {
	    if ("SELF".equalsIgnoreCase(relativePropertyPath)) {
		return context;
	    }
	    return (StringUtils.isEmpty(context)) ? relativePropertyPath : context + "." + relativePropertyPath;
	} else if ("SELF".equalsIgnoreCase(relativePropertyPath)) {
	    throw new IllegalStateException("SELF should be not be used in relative paths.");
	}

	final int endOfLevelUp = relativePropertyPath.lastIndexOf(UP_LEVEL);
	final String returnPath = relativePropertyPath.substring(0, endOfLevelUp + 1);
	final String propertyPathWithoutLevelUp = relativePropertyPath.substring(endOfLevelUp + 2);
	final int returnPathLength = propertyDepth(returnPath);

	final String missingPathFromRoot = pathFromRoot(context, returnPathLength);
	final String absolutePath = StringUtils.isEmpty(missingPathFromRoot) ? propertyPathWithoutLevelUp : missingPathFromRoot + "." + propertyPathWithoutLevelUp;
	if (absolutePath.contains("←")) {
	    throw new IllegalArgumentException("Relative property path may contain symbol ← only at the beginning.");
	}
	return absolutePath;
    }

    /**
     * A helper function, which recursively determines the depth of the context path in comparison to the relative property path provide.
     *
     * @return
     */
    private static String pathFromRoot(final String context, final int relativePathLength) {
	// this basically means that either context was not specified correctly or the ← symbol in the relative path was included too many times
	if (relativePathLength > propertyDepth(context)) {
	    throw new IllegalArgumentException("Either the context or the relative property path is incorrect.");
	}

	String toReturn = context;
	if (relativePathLength > 0) {
	    final int lastDot = context.lastIndexOf('.');
	    if (lastDot > 0) {
		toReturn = pathFromRoot(context.substring(0, lastDot), relativePathLength - 1);
	    } else {
		toReturn = pathFromRoot("", relativePathLength - 1);
	    }
	}
	return toReturn;
    }

    /**
     * Converts an absolute property path to a relative one in respect to the provided context.
     *
     * @param context
     *            the dot notated property path from the root, which indicated the relative position in the type tree against which all other paths should be calculated.
     * @param absolutePropertyPath
     *            -- an absolute property path, which needs to be converted to a relative path with respect to the specified context.
     * @return
     */
    public static String fromAbsotule2RelativePath(final String context, final String absolutePropertyPath) {
	if (absolutePropertyPath.contains("←")) {
	    throw new IllegalArgumentException("Both the context and the property path should be absolute.");
	}
	if (context.equals(absolutePropertyPath)) {
	    return "SELF";
	}

	// calculate the matching path depth from the beginning
	final String[] contextElements = context.split(DOT_SPLITTER);
	final String[] propertyElements = absolutePropertyPath.split(DOT_SPLITTER);
	final int length = Math.min(contextElements.length, propertyElements.length);
	int longestPathUp = propertyDepth(context);
	for (int index = 0; index < length; index++) {
	    if (!contextElements[index].equals(propertyElements[index])) {
		break;
	    }
	    longestPathUp--;
	}

	// construct the relative path portion
	final StringBuilder sb = new StringBuilder();
	for (int index = 0; index < longestPathUp; index++) {
	    sb.append("←.");
	}
	// append the remaining property path
	for (int index = propertyDepth(context) - longestPathUp; index < propertyElements.length; index++) {
	    sb.append(propertyElements[index]);
	    if (index < propertyElements.length - 1) {
		sb.append(".");
	    }
	}

	return sb.toString();
    }

    /**
     * Calculate the property depth in the type tree based on the number of "." separators.
     *
     * @param propertyPath
     * @return
     */
    public static int propertyDepth(final String propertyPath) {
	if (StringUtils.isEmpty(propertyPath)) {
	    return 0;
	}
	return propertyPath.split(DOT_SPLITTER).length;
    }

    /**
     * Converts a relative property path to an absolute path with respect to the provided context.
     * Unlike relative2Absolute this method inverts the path making the context property to be the first node in the path.
     *
     * @param type
     * @param contextProperty
     * @param dotNotaionalExp
     * @return
     */
    public static String relative2AbsoluteInverted(final Class<? extends AbstractEntity<?>> type, final String contextProperty, final String dotNotaionalExp) {
	// if the context property is not specified then there should be no relative paths
	if (StringUtils.isEmpty(contextProperty) || !dotNotaionalExp.contains("←")) {
	    return dotNotaionalExp;
	}
	// otherwise transformation from relative to absolute path is required
	final StringBuilder absProp = new StringBuilder();

	String currProp = contextProperty;

	final String[] path = dotNotaionalExp.split(DOT_SPLITTER);
	int index = 0;
	while ("←".equals(path[index])) {
	    // find link property and add it to the absolute path
	    final String linkProperty = Finder.findLinkProperty(type, currProp);
	    absProp.append(linkProperty + ".");
	    // prepare for the next iteration by defining running property as the current one without the last element of the path.
	    index++;
	    final int lastDot = currProp.lastIndexOf(".");
	    if (lastDot >= 0) {
		currProp = currProp.substring(0, lastDot);
	    } else {
		break;
	    }
	}
	// append the remaining after the last ← properties in the path
	for (final int rem = index; index < path.length; index++) {
	    absProp.append(path[rem]);
	    if (index < path.length - 1) {
		absProp.append(".");
	    }
	}
	// return absolute path
	return absProp.toString();
    }
}
