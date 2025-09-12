package ua.com.fielden.platform.reflection;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableBiMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.Accessor;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.validation.annotation.GreaterOrEqual;
import ua.com.fielden.platform.entity.validation.annotation.Max;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.utils.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isPropertyAnnotationPresent;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isDotExpression;
import static ua.com.fielden.platform.utils.EntityUtils.*;
import static ua.com.fielden.platform.utils.Pair.pair;

/// This is a helper class to provide some commonly used method for retrieval of RTTI not provided directly by the Java reflection package.
///
public final class Reflector {
    /// A maximum cache size for caching reflection related information.
    public static final int MAXIMUM_CACHE_SIZE = 10_000;

    /// A cache for [Method] instances.
    private static final Cache<Class<?>, Cache<String, Method>> METHOD_CACHE = CacheBuilder.newBuilder().weakKeys().initialCapacity(1000).maximumSize(MAXIMUM_CACHE_SIZE).concurrencyLevel(50).build();
    /// A cache for persistent property checks.
    private static final Cache<Class<? extends AbstractEntity<?>>, Cache<String, Boolean>> PERSISTENT_PROP_CACHE = CacheBuilder.newBuilder().weakKeys().initialCapacity(500).maximumSize(MAXIMUM_CACHE_SIZE).concurrencyLevel(50).build();
    /// A cache for calculated property checks.
    private static final Cache<Class<? extends AbstractEntity<?>>, Cache<String, Boolean>> CALCULATED_PROP_CACHE = CacheBuilder.newBuilder().weakKeys().initialCapacity(500).maximumSize(MAXIMUM_CACHE_SIZE).concurrencyLevel(50).build();

    public static final String REL_SYMBOL = "←";
    public static final String SELF = "SELF";

    public static final String ERR_SIMPLE_PROPERTY_NAME_IS_EXPECTED = "[propName] must be a simple property name, but was [%s].";
    public static final String ERR_METHOD_WITH_ARGS_NOT_FOUND = "There is no method [%s] in class [%s] with arguments [%s].";
    public static final String ERR_METHOD_NOT_FOUND = "Could not find method [%s] for type [%s].";
    public static final String ERR_WHILE_CHECKING_OVERRIDING_OF_METHOD = "Checking the overriding of method [%s] for type [%s] with base type [%s] failed.";
    public static final String ERR_ACCESSOR_NOT_FOUND = "Could not obtain accessor for property [%s] in type [%s].";
    public static final String ERR_SETTER_NOT_FOUND = "Could not obtain setter for property [%s] in type [%s].";
    public static final String ERR_GETTING_ANNOTATION_PARAM_VALUE = "Could not get annotation param value.";
    public static final String ERR_SELF_IN_RELATIVE_PATHS = "SELF should be not be used in relative paths.";
    public static final String ERR_UNEXPECTED_REL_SYMBOL_POSITION = "Relative property path may contain symbol ← only at the beginning.";
    public static final String ERR_CONTEXT_OR_RELATIVE_PATH_IS_INVALID = "Either the context or the relative property path is incorrect.";
    public static final String ERR_CONTEXT_OR_PATH_IS_RELATIVE = "Both the context and the property path should be absolute.";
    public static final String ERR_PROPERTY_PERSISTENT_NATURE_NOT_DETERMINED = "Could not determine whether property [%s.%s] is persistent.";
    public static final String ERR_PROPERTY_CALCULATED_NATURE_NOT_DETERMINED = "Could not determine whether property [%s.%s] is calculated.";
    public static final String ERR_TO_ASSIGN_STATIC_FIELD = "Could not assign value to a static field.";

    public static long cleanUp() {
        METHOD_CACHE.cleanUp();
        PERSISTENT_PROP_CACHE.cleanUp();
        CALCULATED_PROP_CACHE.cleanUp();
        return METHOD_CACHE.size() + PERSISTENT_PROP_CACHE.size() + CALCULATED_PROP_CACHE.size();
    }

    /// Regex pattern that represents a separator between properties in property path expressions.
    public static final Pattern DOT_SPLITTER_PATTERN = Pattern.compile("\\.");

    /// A symbol used as the property name substitution in property path expressions representing the next level up in the context of nested properties. Should occur only at the
    /// beginning of the expression. There can be several sequentially linked ← separated by dot splitter.
    public static final String UP_LEVEL = REL_SYMBOL;

    private static final Logger LOGGER = getLogger(Reflector.class);
    
    /// Hide the default constructor, which is not needed for a static class.
    private Reflector() {
    }

    // ========================================================================================================
    /////////////////////////////// Getting methods ///////////////////////////////////////////////////////////

    /// This is a helper method used to walk along class hierarchy in search of the specified method.
    ///
    public static Method getMethod(final Class<?> startWithClass, final String methodName, final Class<?>... arguments) throws NoSuchMethodException {
        try {
            // setKey is a special case, because property "key" has a parametrised type that extends comparable.
            // this means that there are two cases:
            // 1. setter is overridden and a final key type is specified there -- need to use the assigned type as input parameter to setter in order to find it
            // 2. setter is not overridden -- need to use the lowest common denominator (Comparable) as input parameter to setter in order to find it.
            if ("setKey".equals(methodName)) {
                try {
                    return getMethodForClass(startWithClass, methodName, arguments);
                } catch (final NoSuchMethodException ex) {
                    return getMethodForClass(startWithClass, methodName, Comparable.class);
                }
            }
            return getMethodForClass(startWithClass, methodName, arguments);
        } catch (final NoSuchMethodException e) {
            if (AbstractUnionEntity.class.isAssignableFrom(startWithClass) &&
                AbstractUnionEntity.commonMethodNames((Class<AbstractUnionEntity>) startWithClass).contains(methodName))
            {
                return getMethodForClass(AbstractUnionEntity.unionProperties((Class<AbstractUnionEntity>) startWithClass).getFirst().getType(), methodName, arguments);
            }
        }
        throw new NoSuchMethodException(ERR_METHOD_WITH_ARGS_NOT_FOUND.formatted(methodName, startWithClass.getSimpleName(), Arrays.asList(arguments)));
    }

    /// Returns method specified with methodName from `startWithClass` class.
    ///
    public static Method getMethodForClass(final Class<?> startWithClass, final String methodName, final Class<?>... arguments) throws NoSuchMethodException {
        Class<?> klass = startWithClass;
        while (klass != Object.class) { // need to iterated thought hierarchy in
            // order to retrieve fields from above
            // the current instance
            try {
                return getDeclaredMethod(klass, methodName, arguments);
            } catch (final ReflectionException e) {
                klass = klass.getSuperclass();
            }
        }
        throw new NoSuchMethodException(methodName);
    }


    private static Method getDeclaredMethod(final Class<?> klass, final String methodName, final Class<?>... arguments) {
        final String methodKey = format("%s(%s)", methodName, Stream.of(arguments).map(Class::getName).collect(joining(", ")));
        final Cache<String, Method> methodOrException;
        try {
            methodOrException = METHOD_CACHE.get(klass, () -> CacheBuilder.newBuilder().weakValues().build());
        } catch (final ExecutionException ex) {
            throw new ReflectionException(ERR_METHOD_NOT_FOUND.formatted(methodKey, klass), ex);
        }
        
        try {
            return methodOrException.get(methodKey, () -> klass.getDeclaredMethod(methodName, arguments));
        } catch (final ExecutionException ex) {
            throw new ReflectionException(ERR_METHOD_NOT_FOUND.formatted(methodKey, klass), ex);
        }
    }

    /// Returns constructor specified from `startWithClass` class.
    ///
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

    /// Returns the method specified with methodName and the array of it's arguments. In order to determine correct method it uses instances instead of classes.
    ///
    public static Method getMethod(final Object instance, final String methodName, final Class<?>... arguments) throws NoSuchMethodException {
        try {
            return getMethodForClass(instance instanceof AbstractEntity<?> entity ? entity.getType() : instance.getClass(), methodName, arguments);
        } catch (final NoSuchMethodException e) {
            if (instance instanceof AbstractUnionEntity unionEntity &&
                AbstractUnionEntity.commonMethodNames((Class<AbstractUnionEntity>) unionEntity.getType()).contains(methodName))
            {
                final AbstractEntity<?> activeEntity = unionEntity.activeEntity();
                if (activeEntity != null) {
                    return getMethodForClass(activeEntity.getType(), methodName, arguments);
                }
            }
        }
        throw new NoSuchMethodException(methodName);
    }
    
    /// Returns `true` if the specified method `methodName` is overridden in `type` or its super types that extend `baseType` where this method is inherited from.
    /// Or the method is declared in `type` (i.e. it is not inherited).
    ///
    /// Basically, `true` is returned if `methodName` appears anywhere in the type hierarchy but the base type.
    ///
    public static <BASE> boolean isMethodOverriddenOrDeclared(final Class<BASE> baseType, final Class<? extends BASE> type, final String methodName, final Class<?>... arguments) {
        try {
            final Method method = Reflector.getMethod(type, methodName, arguments);
            return method.getDeclaringClass() != baseType;
        } catch (final NoSuchMethodException | SecurityException ex) {
            LOGGER.debug(() -> ERR_WHILE_CHECKING_OVERRIDING_OF_METHOD.formatted(methodName, type.getName(), baseType.getName()), ex);
            return false;
        }
    }

    /// Depending on the type of the field, the getter may start not with ''get'' but with ''is''. This method tries to determine a correct getter.
    ///
    public static Method obtainPropertyAccessor(final Class<?> entityClass, final CharSequence propertyName) {
        try {
            return Reflector.getMethod(entityClass, Accessor.GET.getName(propertyName));
        } catch (final Exception ex) {
            try {
                return Reflector.getMethod(entityClass, Accessor.IS.getName(propertyName));
            } catch (final NoSuchMethodException $) {
                throw new ReflectionException(ERR_ACCESSOR_NOT_FOUND.formatted(propertyName, entityClass.getName()), ex);
            }
        }
    }

    /// Tries to find a property setter for property, specified using dot-expression.
    ///
    public static Method obtainPropertySetter(final Class<?> entityClass, final CharSequence propPath) {
        if (StringUtils.isEmpty(propPath) || propPath.toString().contains("()")) {
            throw new InvalidArgumentException("[propPath] should not be empty and should not include methods.");
        }
        final Pair<Class<?>, String> transformed = PropertyTypeDeterminator.transform(entityClass, propPath);
        try {
            final String methodName = "set" + transformed.getValue().substring(0, 1).toUpperCase() + transformed.getValue().substring(1);
            final Class<?> argumentType = PropertyTypeDeterminator.determineClass(transformed.getKey(), transformed.getValue(), AbstractEntity.KEY.equalsIgnoreCase(transformed.getValue()), false);

            return Reflector.getMethod(transformed.getKey(), methodName, argumentType);
        } catch (final Exception ex) {
            throw new ReflectionException(ERR_SETTER_NOT_FOUND.formatted(propPath, entityClass.getName()), ex);
        }
    }

    // ========================================================================================================
    /////////////////////////////// Miscellaneous utilities ///////////////////////////////////////////////////

    /// Returns min and max possible values for property.
    ///
    public static Pair<Integer, Integer> extractValidationLimits(final AbstractEntity<?> entity, final String propertyName) {
        final Field field = Finder.findFieldByName(entity.getType(), propertyName);
        Integer min = null, max = null;
        final Set<Annotation> propertyValidationAnotations = entity.findValidationAnnotationsForProperty(field, PropertyTypeDeterminator.determinePropertyType(entity.getType(), propertyName));
        for (final Annotation annotation : propertyValidationAnotations) {
            if (annotation instanceof GreaterOrEqual) {
                min = ((GreaterOrEqual) annotation).value();
            } else if (annotation instanceof Max) {
                max = ((Max) annotation).value();
            }
        }
        return new Pair<>(min, max);
    }

    /// Returns a list of parameters declared for the specified annotation type. An empty list is returned in case where there are no parameter declarations.
    ///
    public static List<String> annotataionParams(final Class<? extends Annotation> annotationType) {
        final List<String> names = new ArrayList<>();
        for (final Method param : annotationType.getDeclaredMethods()) {
            names.add(param.getName());
        }
        return names;
    }

    /// Obtains and returns a pair of parameter type and its value for the specified annotation parameter.
    ///
    public static Pair<Class<?>, Object> getAnnotationParamValue(final Annotation annotation, final String paramName) {
        try {
            final Method method = annotation.getClass().getDeclaredMethod(paramName);
            method.setAccessible(true);
            return pair(method.getReturnType(), method.invoke(annotation));
        } catch (final Exception ex) {
            throw new ReflectionException(ERR_GETTING_ANNOTATION_PARAM_VALUE, ex);
        }
    }

    /// Converts a relative property path to an absolute path with respect to the provided context.
    ///
    /// @param context
    ///            a dot-expression for a property path from the root, which indicated the relative position in the type tree against which all other paths should be calculated.
    /// @param relativePropertyPath
    ///            relative property path, which may contain ← and dots for separating individual properties.
    /// @return
    public static String fromRelative2AbsolutePath(final String context, final String relativePropertyPath) {
        // if the relativePropertyPath path does not start with ← then it is absolute already
        if (!relativePropertyPath.startsWith(REL_SYMBOL)) {
            if (SELF.equalsIgnoreCase(relativePropertyPath)) {
                return context;
            }
            return (StringUtils.isEmpty(context)) ? relativePropertyPath : context + "." + relativePropertyPath;
        } else if (SELF.equalsIgnoreCase(relativePropertyPath)) {
            throw new InvalidStateException(ERR_SELF_IN_RELATIVE_PATHS);
        }

        final int endOfLevelUp = relativePropertyPath.lastIndexOf(UP_LEVEL);
        final String returnPath = relativePropertyPath.substring(0, endOfLevelUp + 1);
        final String propertyPathWithoutLevelUp = relativePropertyPath.substring(endOfLevelUp + 2);
        final int returnPathLength = propertyDepth(returnPath);

        final String missingPathFromRoot = pathFromRoot(context, returnPathLength);
        final String absolutePath = StringUtils.isEmpty(missingPathFromRoot) ? propertyPathWithoutLevelUp : missingPathFromRoot + "." + propertyPathWithoutLevelUp;
        if (absolutePath.contains(REL_SYMBOL)) {
            throw new InvalidStateException(ERR_UNEXPECTED_REL_SYMBOL_POSITION);
        }
        return absolutePath;
    }

    /// A helper function, which recursively determines the depth of the context path in comparison to the relative property path provide.
    ///
    private static String pathFromRoot(final String context, final int relativePathLength) {
        // this basically means that either context was not specified correctly or the ← symbol in the relative path was included too many times
        if (relativePathLength > propertyDepth(context)) {
            throw new InvalidArgumentException(ERR_CONTEXT_OR_RELATIVE_PATH_IS_INVALID);
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

    /// Converts an absolute property path to a relative one in respect to the provided context.
    ///
    /// @param context
    ///            a dot-expression for a property path from the root, which indicated the relative position in the type tree against which all other paths should be calculated.
    /// @param absolutePropertyPath
    ///            an absolute property path, which needs to be converted to a relative path with respect to the specified context.
    ///
    public static String fromAbsolute2RelativePath(final String context, final String absolutePropertyPath) {
        if (absolutePropertyPath.contains(REL_SYMBOL)) {
            throw new InvalidArgumentException(ERR_CONTEXT_OR_PATH_IS_RELATIVE);
        }
        if (context.equals(absolutePropertyPath)) {
            return SELF;
        }

        // calculate the matching path depth from the beginning
        final String[] contextElements = laxSplitPropPathToArray(context);
        final String[] propertyElements = laxSplitPropPathToArray(absolutePropertyPath);
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
        sb.append("←.".repeat(Math.max(0, longestPathUp)));
        // append the remaining property path
        for (int index = propertyDepth(context) - longestPathUp; index < propertyElements.length; index++) {
            sb.append(propertyElements[index]);
            if (index < propertyElements.length - 1) {
                sb.append(".");
            }
        }

        return sb.toString();
    }

    /// Calculate the property depth in the type tree based on the number of "." separators.
    ///
    public static int propertyDepth(final String propertyPath) {
        return StringUtils.isEmpty(propertyPath)
               ? 0
               : laxSplitPropPathToArray(propertyPath).length;
    }

    /// Converts a relative property path to an absolute path with respect to the provided context. Unlike relative2Absolute this method inverts the path making the context property
    /// to be the first node in the path.
    ///
    public static String relative2AbsoluteInverted(final Class<? extends AbstractEntity<?>> type, final String contextProperty, final String propPath) {
        // if the context property is not specified then there should be no relative paths
        if (StringUtils.isEmpty(contextProperty) || !propPath.contains(REL_SYMBOL)) {
            return propPath;
        }
        // otherwise transformation from relative to absolute path is required
        final StringBuilder absProp = new StringBuilder();

        String currProp = contextProperty;

        final String[] path = laxSplitPropPathToArray(propPath);
        int index = 0;
        while (REL_SYMBOL.equals(path[index])) {
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

    /// A convenient method returning a separator that is used to represent a composite entity as a single string value.
    ///
    public static String getKeyMemberSeparator(final Class<? extends AbstractEntity<DynamicEntityKey>> type) {
        return AnnotationReflector.getAnnotation(type, KeyType.class).keyMemberSeparator();
    }
    
    /// A predicate that evaluates to `true` only if none of the properties in a dot-expression is proxied for a given entity and none of the intermediate values is `null`.
    ///
    /// For example, `isPropertyProxied(entity, "prop.subProp.subSubProp")` would be `false` in the following cases:
    ///
    ///  1.
    ///     -  `prop` is not proxied,
    ///     -  `entity.get("prop")` is `null`,
    ///  2.
    ///     -  `prop` is not proxied,
    ///     -  `entity.get("prop")` is not `null`,
    ///     -  `"prop.subProp"` is not proxied,
    ///     -  `entity.get("prop").get("subProp")` is `null`,
    ///  3.
    ///     -  `prop` is not proxied,
    ///     -  `entity.get("prop")` is not `null`,
    ///     -  `"prop.subProp"` is not proxied,
    ///     -  `entity.get("prop").get("subProp")` is not `null`,
    ///     -  `"prop.subProp.subSubProp"` is not proxied.
    ///
    /// And the same would be `true` in the following cases:
    ///
    ///  1.
    ///     -  `prop` is proxied
    ///  2.
    ///     -  `prop` is not proxied,
    ///     -  `entity.get("prop")` is not `null`,
    ///     -  `"prop.subProp"` is proxied
    ///  3.
    ///     -  `prop` is not proxied,
    ///     -  `entity.get("prop")` is not `null`,
    ///     -  `"prop.subProp"` is not proxied,
    ///     -  `entity.get("prop").get("subProp")` is not `null`,
    ///     -  `"prop.subProp.subSubProp"` is proxied.
    ///
    public static boolean isPropertyProxied(final AbstractEntity<?> entity, final CharSequence propPath) {
        final var props = splitPropPath(propPath).iterator();
        return isPropertyProxied_(entity, props.next(), props);
    }
    // a helper function to implement recursive processing of propPath
    private static boolean isPropertyProxied_(final AbstractEntity<?> entity, final String propName, final Iterator<String> tail) {
        if (entity.proxiedPropertyNames().contains(propName)) {
            return true;
        }
        if (tail.hasNext()) {
            final AbstractEntity<?> nextEntity = entity.get(propName);
            // If the next entity is null then there is nothing to violate the proxy condition -- consider its sub-properties to be not proxied.
            return nextEntity != null && isPropertyProxied_(nextEntity, tail.next(), tail);
        }
        return false;
    }

    /// Identifies whether the specified field represents a retrievable property.
    /// The notion of `retrievable` is different to `persistent` as it also includes calculated properties, which do get retrieved from a database.
    ///
    public static boolean isPropertyRetrievable(final AbstractEntity<?> entity, final Field field) {
        final String name = field.getName();
        return entity.isPersistent()
               && (
                      field.isAnnotationPresent(Calculated.class) ||
                      (!name.equals(AbstractEntity.KEY) && !name.equals(AbstractEntity.DESC) && field.isAnnotationPresent(MapTo.class)) ||
                      (name.equals(AbstractEntity.KEY) && !entity.isComposite()) ||
                      (name.equals(AbstractEntity.DESC) && AnnotationReflector.isAnnotationPresent(entity.getType(), DescTitle.class)) ||
                      (Finder.isOne2One_association(entity.getType(), name))
                  );
    }
    
    /// A convenient equivalent to method [#isPropertyRetrievable(AbstractEntity,Field)] that accepts property name instead of the Field instance.
    ///
    public static boolean isPropertyRetrievable(final AbstractEntity<?> entity, final CharSequence propName) {
        return isPropertyRetrievable(entity, Finder.findFieldByName(entity.getClass(), propName)); 
    }

    /// This predicate is true for persistent properties.
    ///
    /// @param propName  a simple property name
    ///
    public static boolean isPropertyPersistent(final Class<? extends AbstractEntity<?>> entityType, final CharSequence propName) {
        if (isDotExpression(propName)) {
            throw new InvalidArgumentException(ERR_SIMPLE_PROPERTY_NAME_IS_EXPECTED.formatted(propName));
        }

        try {
            return PERSISTENT_PROP_CACHE
                    .get(entityType, () -> CacheBuilder.newBuilder().initialCapacity(30).concurrencyLevel(50).build())
                    .get(propName.toString(), () -> isPropertyPersistent_(entityType, propName));
        } catch (final ExecutionException ex) {
            throw new ReflectionException(ERR_PROPERTY_PERSISTENT_NATURE_NOT_DETERMINED.formatted(entityType.getSimpleName(), propName), ex.getCause());
        }
    }
    
    private static boolean isPropertyPersistent_(final Class<? extends AbstractEntity<?>> entityType, final CharSequence propName) {
        // This logic should remain aligned with domain metadata (PropertyMetadata.isPersistent).
        if (!isPersistentEntityType(entityType) && !isUnionEntityType(entityType)) {
            return false;
        }
        else if (AbstractEntity.KEY.contentEquals(propName)) {
            return !isUnionEntityType(entityType) && !isCompositeEntity(entityType);
        }
        else if (AbstractEntity.DESC.contentEquals(propName)) {
            return !isUnionEntityType(entityType) && hasDescProperty(entityType);
        }
        else {
            return isPropertyAnnotationPresent(MapTo.class, entityType, propName.toString());
        }
    }
    
    /// This predicate is true for calculated properties.
    /// There are two kinds of calculated properties:
    /// 1. Explicitly calculated - annotated with @Calculated
    /// 2. Implicitly calculated - one-2-one relationship
    /// Calculated properties may only occur in persistent and synthetic entities.
    ///
    /// TODO: we skip the case of one-2-many relationship for now, until better understanding of how to better recognise them.
    ///
    /// @param entityType  the entity type
    /// @param propName  a simple property name
    ///
    public static boolean isPropertyCalculated(final Class<? extends AbstractEntity<?>> entityType, final CharSequence propName) {
        if (isDotExpression(propName)) {
            throw new InvalidArgumentException(ERR_SIMPLE_PROPERTY_NAME_IS_EXPECTED.formatted(propName));
        }

        try {
            return CALCULATED_PROP_CACHE
                    .get(entityType, () -> CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(50).build())
                    .get(propName.toString(), () -> isPropertyCalculated_(entityType, propName));
        } catch (final ExecutionException ex) {
            throw new ReflectionException(ERR_PROPERTY_CALCULATED_NATURE_NOT_DETERMINED.formatted(entityType.getSimpleName(), propName), ex.getCause());
        }
    }
    
    private static boolean isPropertyCalculated_(final Class<? extends AbstractEntity<?>> entityType, final CharSequence propName) {
        // Calculated properties can only exist in persistent or synthetic entities.
        if (!isPersistentEntityType(entityType) && !isSyntheticEntityType(entityType)) {
            return false;
        }
        
        // Check for explicit @Calculated annotation
        if (isPropertyAnnotationPresent(Calculated.class, entityType, propName.toString())) {
            return true;
        }
        
        // Check for implicit calculation via one-2-one association
        return Finder.isOne2One_association(entityType, propName.toString());
    }
    
    /// A helper function to assign value to a private static field.
    ///
    public static void assignStatic(final Field field, final Object value) {
        try {
            field.setAccessible(true);
            field.set(null, value);
        } catch (final Exception ex) {
            throw new ReflectionException(ERR_TO_ASSIGN_STATIC_FIELD, ex);
        }
    }

    public static ParameterizedType newParameterizedType(final Class<?> rawType, final Type... typeArguments) {
        final Type owner = rawType.getDeclaringClass();

        return new ParameterizedType() {
            @Override public Class<?> getRawType() { return rawType; }
            @Override public Type getOwnerType() { return owner; }
            @Override public Type[] getActualTypeArguments() { return typeArguments; }
            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder();
                if (owner != null) {
                    sb.append(owner.getTypeName());
                    sb.append('$');
                    sb.append(rawType.getSimpleName());
                }
                else {
                    sb.append(rawType.getName());
                }

                final StringJoiner sj = new StringJoiner(", ", "<", ">").setEmptyValue("");
                for (final Type arg : typeArguments) {
                    sj.add(arg.getTypeName());
                }

                return sb.append(sj.toString()).toString();
            }

            @Override
            public boolean equals(final Object obj) {
                return this == obj
                       || obj instanceof ParameterizedType that
                          && Objects.equals(rawType, that.getRawType())
                          && Objects.equals(owner, that.getOwnerType())
                          && Arrays.equals(typeArguments, that.getActualTypeArguments());
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + Objects.hashCode(rawType);
                result = prime * result + Objects.hashCode(owner);
                result = prime * result + Arrays.hashCode(typeArguments);
                return result;
            }
        };
    }

    private static final ImmutableBiMap<Class<?>, Class<?>> BOXED_TO_UNBOXED_TYPE_MAP = ImmutableBiMap.of(
            Boolean.class, boolean.class,
            Byte.class, byte.class,
            Character.class, char.class,
            Float.class, float.class,
            Integer.class, int.class,
            Long.class, long.class,
            Short.class, short.class,
            Double.class, double.class);

    public static boolean isBoxedType(final Type type) {
        return type instanceof Class<?> cls && BOXED_TO_UNBOXED_TYPE_MAP.containsKey(cls);
    }

    public static Optional<Class<?>> getUnboxedType(final Type type) {
        return type instanceof Class<?> cls
                ? Optional.ofNullable(BOXED_TO_UNBOXED_TYPE_MAP.get(cls))
                : Optional.empty();
    }

    public static Optional<Class<?>> getBoxedType(final Type type) {
        return type instanceof Class<?> cls
                ? Optional.ofNullable(BOXED_TO_UNBOXED_TYPE_MAP.inverse().get(cls))
                : Optional.empty();
    }

}
