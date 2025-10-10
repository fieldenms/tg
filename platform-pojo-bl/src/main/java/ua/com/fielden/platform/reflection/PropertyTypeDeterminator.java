package ua.com.fielden.platform.reflection;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxyEntity;
import ua.com.fielden.platform.entity.proxy.IProxyEntity;
import ua.com.fielden.platform.entity.proxy.MockNotFoundEntityMaker;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import java.lang.reflect.*;
import java.util.Map;
import java.util.Optional;

import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService.APPENDIX;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.*;
import static ua.com.fielden.platform.utils.Pair.pair;

/**
 * Contains methods for property type determination.
 * Methods travers through:
 * <ol>
 *   <li>type hierarchies,</li>
 *   <li>dot-expressions that represent property paths.</li>
 * </ol>
 *
 * @author TG Team
 *
 */
public class PropertyTypeDeterminator {
    public static final String PROPERTY_SPLITTER = ".";
    public static final String ERR_TYPE_AND_PROP_REQUIRED = "Property type cannot be determined without both property name and owning type specified.";
    public static final String ERR_IDENTIFYING_BASE_TYPE_FOR_ENTITY = "Could not identify a base type for entity type [%s].";
    public static final String ERR_CLASS_STRIPPING_FOR_NULL = "Class stripping is not applicable to null values.";
    public static final String ERR_EMPTY_STRING_IS_NOT_EXPECTED = "Empty string is not expected here: type [%s], propertyOrFunction [%s].";
    public static final String ERR_DOT_EXPRESSION_IS_NOT_EXPECTED = "Dot-expression is not expected here: type [%s], propertyOrFunction [%s].";
    public static final String ERR_NEITHER_DOT_EXPRESSION_NOR_EMPTY_STRING_IS_EXPECTED = "Neither dot-expression nor empty string is expected here: type [%s], propertyOrFunction [%s].";
    public static final String ERR_NO_METHOD_IN_TYPE = "No method [%s] in type [%s].";
    public static final String ERR_NO_PROPERTY_IN_TYPE = "No property [%s] in type [%s].";
    public static final String ERR_DOT_EXPRESSION_IS_EXPECTED = "A dot-expression is expected.";

    /**
     * Hide default constructor for a utility class.
     */
    private PropertyTypeDeterminator() { }

    /**
     * Determines the type of property / method defined by a dot-expression.
     * <p>
     * If dot-expression is {@code "this"}, {@code type} is returned (stripped if needed).
     *
     * @param type  type that should contain property/method defined by the dot-expression (e.g. {@code Vehicle} contains {@code "status.isGeneratePmWo()"})
     * @param dotExpr methods / properties joined by {@code "."} (e.g. {@code "vehicle.getKey().getStatus().generatePmWo.getWorkOrder().key"})
     *
     * @return  property type / method return type
     */
    public static Class<?> determinePropertyType(final Class<?> type, final CharSequence dotExpr) {
        if (type == null || StringUtils.isEmpty(dotExpr)) {
            throw new ReflectionException(ERR_TYPE_AND_PROP_REQUIRED);
        }
        
        if ("this".contentEquals(dotExpr)) {
            return stripIfNeeded(type);
        }

        final String[] propertiesOrFunctions = splitPropPathToArray(dotExpr);
        Class<?> result = type;
        for (final String propertyOrFunction : propertiesOrFunctions) {
            result = determineClass(result, propertyOrFunction, true, true);
        }
        
        return result == null ? null : stripIfNeeded(result);
    }

    /**
     * Determines class of property/function that should be inside <code>type</code> hierarchy.
     *
     * If <code>type</code> doesn't have desired property or function -- {@link ReflectionException} will be thrown.
     *
     * @param type  the class that should contain {@code propertyOrFunction} (property or function)
     * @param propertyOrFunction  the name of property or function (e.g., "isObservable()", "vehicle", "key", "getKey()"), not a dot-expression.
     * @param determineKeyType  true => then a correct "key"/"getKey()" class is returned, otherwise {@link Comparable} is returned.
     * @param determineElementType  true => then a correct element type of collectional property is returned, otherwise a type of collection (list, set, etc.) is returned.
     *
     * @return
     */
    public static Class<?> determineClass(final Class<?> type, final String propertyOrFunction, final boolean determineKeyType, final boolean determineElementType) {
        if (StringUtils.isEmpty(propertyOrFunction)) {
            throw new ReflectionException(ERR_EMPTY_STRING_IS_NOT_EXPECTED.formatted(type, propertyOrFunction));
        }
        if (isDotExpression(propertyOrFunction)) {
            throw new ReflectionException(ERR_DOT_EXPRESSION_IS_NOT_EXPECTED.formatted(type, propertyOrFunction));
        }
        if (determineKeyType && (AbstractEntity.KEY.equals(propertyOrFunction) || AbstractEntity.GETKEY.equals(propertyOrFunction)) && AbstractEntity.class.equals(type)) {
            return Comparable.class;
        } else if (determineKeyType && (AbstractEntity.KEY.equals(propertyOrFunction) || AbstractEntity.GETKEY.equals(propertyOrFunction)) && AbstractEntity.class.isAssignableFrom(type)) {
            ////////////////// Key property or getKey() method type determination //////////////////
            return AnnotationReflector.getKeyType(type);
        } else {
            if (propertyOrFunction.endsWith("()")) { // parameterless function -- assuming "propertyOrFunction" is name of parameterless method (because propertyName ends with '()')
                try {
                    ////////////////// Parameterless Function return type determination //////////////////
                    final Method method = Reflector.getMethod(type, propertyOrFunction.substring(0, propertyOrFunction.length() - 2));
                    final Class<?> theType = method.getReturnType();
                    return determineElementType && isParameterizedType(theType) ? determineElementClassForMethod(method) : theType; // property element type should be retrieved if determineElementType == true
                } catch (final Exception e) {
                    throw new ReflectionException("No " + propertyOrFunction + " method in " + type.getSimpleName() + " class.");
                }
            } else { // property -- assuming that "propertyOrFunction" is a name of a property (because its name contains no braces)
                ////////////////// Property class determination using property field. //////////////////
                final Field field = Finder.getFieldByName(type, propertyOrFunction);
                final Class<?> theType = field.getType();
                return determineElementType && isParameterizedType(theType) ? determineElementClass(field) : theType; // property element type should be retrieved if determineElementType == true
            }
        }
    }

    private static boolean isParameterizedType(final Class<?> theType) {
        return EntityUtils.isCollectional(theType) /* || EntityUtils.isPropertyDescriptor(theType) */;
    }

    /**
     * If the {@code method} return type is collectional then it returns type of that collection elements.
     *
     * @param method
     * @return
     */
    private static Class<?> determineElementClassForMethod(final Method method) {
        final ParameterizedType paramType = (ParameterizedType) method.getGenericReturnType();
        return classFrom(paramType.getActualTypeArguments()[0]);
    }

    /**
     * If the field is a collectional property, returns the type of collection elements, otherwise returns {@link Object}.
     */
    private static Class<?> determineElementClass(final Field field) {
        return field.getGenericType() instanceof ParameterizedType paramType
                ? classFrom(paramType.getActualTypeArguments()[0])
                : Object.class;
    }

    /**
     * Determines correct {@link Class} from different {@link Type} implementations.
     *
     * <p>
     * Type could be parameterized. If it is - inner type would be returned. If type is simple class - method returns this simple class.
     * <p>
     * Examples:
     *
     * <pre>
     * private class GenericsPropertiesTestClass&lt;T extends Number&gt; {
     *     private List&lt;Integer&gt; prop1; - <b>returns Integer</b>
     *     private List&lt;T&gt; prop2; - <b>returns Number</b>
     *     private List&lt;? extends Float&gt; prop3; - <b>returns Float</b>
     *     private List&lt;BigInteger[]&gt; prop4; - <b>returns BigInteger</b>
     *
     *     public List&lt;Integer&gt; getProp1() { - <b>returns Integer</b>
     * 	return prop1;
     *     }
     *
     *     public List&lt;T&gt; getProp2() { - <b>returns Number</b>
     * 	return prop2;
     *     }
     *
     *     public List&lt;? extends Float&gt; getProp3() { - <b>returns Float</b>
     * 	return prop3;
     *     }
     *
     *     public List&lt;BigInteger[]&gt; getProp4() { - <b>returns BigInteger</b>
     * 	return prop4;
     *     }
     * }
     *
     * </pre>
     *
     * @param type
     * @return
     */
    public static Class<?> classFrom(final Type type) {
        if (type instanceof ParameterizedType) {
            return classFrom(((ParameterizedType) type).getRawType());
        } else if (type instanceof TypeVariable) {
            return classFrom(((TypeVariable<?>) type).getBounds()[0]);
        } else if (type instanceof WildcardType) {
            return classFrom(((WildcardType) type).getUpperBounds()[0]);
        } else if (type instanceof GenericArrayType) {
            return classFrom(((GenericArrayType) type).getGenericComponentType());
        } else if (type instanceof Class) {
            return (Class<?>) type;
        } else {
            return null;
        }
    }

    /**
     * Determines a type ({@link Type}) for a property/function that is defined by {@code dotExpr}.
     *
     * @param type  a class that should contain property/function defined by a dot-expression (e.g., "Vehicle" contains "status.isGeneratePmWo()")
     * @param dotExpr  a dot-expression of functions and properties (e.g., "vehicle.getKey().getStatus().generatePmWo.getWorkOrder().key").
     * @return  a property/function type
     */
    public static Type determinePropertyTypeWithCorrectTypeParameters(final Class<?> type, final String dotExpr) {
        final Pair<Class<?>, String> transformed = transform(type, dotExpr);
        final Type resultType = determineType(transformed.getKey(), transformed.getValue());
        return (resultType instanceof Class) ? stripIfNeeded((Class<?>) resultType) : resultType;
    }

    /**
     * Determines a type ({@link Type}) of a property/function, defined in the {@code type} hierarchy.
     *
     * If {@code type} doesn't have desired property or function -- {@link ReflectionException} will be thrown.
     *
     * @param type  a class containing {@code propertyOrFunction} (property or function)
     * @param propertyOrFunction  a name of a property or function (e.g., "isObservable()", "vehicle", "key", "getKey()"), not a dot-expression.
     *
     * @return
     */
    private static Type determineType(final Class<?> type, final String propertyOrFunction) {
        if (StringUtils.isEmpty(propertyOrFunction) || isDotExpression(propertyOrFunction)) {
            throw new ReflectionException(ERR_NEITHER_DOT_EXPRESSION_NOR_EMPTY_STRING_IS_EXPECTED.formatted(type, propertyOrFunction));
        }
        if ((AbstractEntity.KEY.equals(propertyOrFunction) || AbstractEntity.GETKEY.equals(propertyOrFunction)) && AbstractEntity.class.isAssignableFrom(type)) {
            return AnnotationReflector.getKeyType(type);
            //////////////////Key property or getKey() method type determination //////////////////
        } else if (propertyOrFunction.endsWith("()")) { // parameterless function -- assuming "propertyOrFunction" is name of parameterless method (because propertyName ends with '()')
            try {
                ////////////////// Parameterless Function return type determination //////////////////
                return Reflector.getMethod(type, propertyOrFunction.substring(0, propertyOrFunction.length() - 2)).getGenericReturnType(); // getReturnType();
            } catch (final Exception e) {
                throw new ReflectionException(ERR_NO_METHOD_IN_TYPE.formatted(propertyOrFunction, type));
            }
        } else { // property -- assuming that "propertyOrFunction" is a name of a property (because its name contains no braces)
            //	    return Reflector.getFieldByName(type, propertyOrFunction).getType();
            ////////////////// Property class determination using property accessor. //////////////////
            try {
                return Reflector.obtainPropertyAccessor(type, propertyOrFunction).getGenericReturnType();
            } catch (final ReflectionException ex) {
                throw new ReflectionException(ERR_NO_PROPERTY_IN_TYPE.formatted(propertyOrFunction, type), ex);
            }
        }
    }

    /**
     * If the given type is non-structurally enhanced, recursively finds its base type, otherwise returns the type itself.
     * The base type is determined recursively, so the returned type may be more than one superclass away from the given one.
     */
    public static Class<?> stripIfNeeded(final Class<?> type) {
        if (type == null) {
            throw new ReflectionException(ERR_CLASS_STRIPPING_FOR_NULL);
        } else if (isInstrumented(type) || isProxied(type) || isLoadedByHibernate(type) || isMockNotFoundType(type)) {
            return stripIfNeeded(type.getSuperclass());
        }
        return type;
    }

    /**
     * If the type is enhanced, recursively finds its base type, otherwise returns the type itself.
     * This method is similar to {@link #stripIfNeeded(Class)}, but also removes structural enhancements,
     * indicated by suffix {@link DynamicTypeNamingService#APPENDIX} in the type name.
     */
    public static Class<? extends AbstractEntity<?>> baseEntityType(final Class<? extends AbstractEntity<?>> type) {
        final Class<? extends AbstractEntity<?>> strippedType = (Class<? extends AbstractEntity<?>>) stripIfNeeded(type);
        final var simpleName = strippedType.getSimpleName();
        if (simpleName.contains(APPENDIX)) {
            final String typeName = strippedType.getName();
            try {
                return (Class<? extends AbstractEntity<?>>) Class.forName(typeName.substring(0, typeName.indexOf(APPENDIX)));
            } catch (final ClassNotFoundException ex) {
                throw new ReflectionException(ERR_IDENTIFYING_BASE_TYPE_FOR_ENTITY.formatted(simpleName), ex);
            }
        } else {
            return strippedType;
        }
    }

    /// Identifies whether `type` represents proxy, loaded by Hibernate.
    ///
    public static boolean isLoadedByHibernate(final Class<?> type) {
        final String name = type.getSimpleName();
        return name.contains("$HibernateProxy") || name.contains("$$_javassist") || name.contains("_$$_");
    }

    /**
     * Returns {@code true} if the specified class is proxied, {@code false} otherwise.
     *
     * @param type
     * @return
     */
    public static boolean isProxied(final Class<?> type) {
        return IProxyEntity.class.isAssignableFrom(type);
    }

    /**
     * Returns {@code true} if the specified class represents an id-only proxy.
     *
     * @param type
     * @return
     */
    public static boolean isIdOnlyProxy(final Class<?> type) {
        return IIdOnlyProxyEntity.class.isAssignableFrom(type);
    }

    /**
     * Returns {@code true} if the specified class is instrumented by Guice, and thus instances of this type should be fully initialised
     * from TG perspective (having meta-properties, fitted with ACE/BCE interceptors, etc.).
     *
     * @param type
     * @return
     */
    public static boolean isInstrumented(final Class<?> type) {
        return type.getName().contains("$$EnhancerByGuice");
    }

    /**
     * A predicate to identify whether {@code entityType} represents a mock-not-found type.
     *
     * @param entityType
     * @return
     */
    public static boolean isMockNotFoundType(final Class<?> entityType) {
        return entityType.getName().endsWith(MockNotFoundEntityMaker.MOCK_TYPE_ENDING);
    }

    public static boolean isDotExpression(final CharSequence expr) {
        return expr.toString().contains(PROPERTY_SPLITTER);
    }

    public static Pair<String, String> penultAndLast(final CharSequence dotExpr) {
        if (!isDotExpression(dotExpr)) {
            throw new ReflectionException(ERR_DOT_EXPRESSION_IS_EXPECTED);
        }
        final String dotNotationStr = dotExpr.toString();
        final int indexOfLastDot = dotNotationStr.lastIndexOf(PROPERTY_SPLITTER);
        final String penultPart = dotNotationStr.substring(0, indexOfLastDot);
        final String lastPart = dotNotationStr.substring(indexOfLastDot + 1);
        return new Pair<>(penultPart, lastPart);
    }

    public static Pair<String, String> firstAndRest(final CharSequence dotNotationExp) {
        if (!isDotExpression(dotNotationExp)) {
            throw new ReflectionException(ERR_DOT_EXPRESSION_IS_EXPECTED);
        }
        final String dotNotationStr = dotNotationExp.toString();
        final int indexOfFirstDot = dotNotationStr.indexOf(PROPERTY_SPLITTER);
        final String firstPart = dotNotationStr.substring(0, indexOfFirstDot);
        final String restPart = dotNotationStr.substring(indexOfFirstDot + 1);
        return pair(firstPart, restPart);
    }

    /**
     * Transforms "type/dotExpr" pair into a form of "penultPropertyType/lastPropertyName".
     *
     * @param type
     * @param dotExpr
     * @return
     */
    public static Pair<Class<?>, String> transform(final Class<?> type, final CharSequence dotExpr) {
        if (isDotExpression(dotExpr)) { // dot-expression that defines a path to a property or a function
            final Pair<String, String> pl = penultAndLast(dotExpr);
            return pair(determinePropertyType(type, pl.getKey()), pl.getValue());
        } else { // empty or first level property/function.
            return pair(type, dotExpr.toString());
        }
    }

    /**
     * Identifies whether property dot-expression {@code dotExpr} in type {@code entityType} is collectional.
     *
     * @param entityType
     * @param dotExpr
     * @return
     */
    public static boolean isCollectional(final Class<?> entityType, final String dotExpr) {
        final Field field = Finder.findFieldByName(entityType, dotExpr);
        return EntityUtils.isCollectional(field.getType());
    }

    /**
     * Given a collectional property, returns a pair (raw collectional type, collectional element type).
     * <p>
     * This method does not check the property definition's correctness.
     * <p>
     * The element type must be specified with {@link IsProperty}. If it's missing, the first type parameter will be used.
     * This lax behaviour might become stricter in the future.
     * <pre>{@code
     * @IsProperty(Vehicle.class)
     * List<Vehicle> vehicles;
     * => (List.class, Vehicle.class)
     *
     * @IsProperty
     * List<Vehicle> vehicles;
     * => (List.class, Vehicle.class)
     *
     * @IsProperty
     * String name;
     * => ()
     * }</pre>
     */
    public static Optional<T2<Class<?>, Class<?>>> collectionalType(final Field field) {
        if (field.getGenericType() instanceof ParameterizedType paramType) {
            if (paramType.getRawType() instanceof Class<?> rawClass && EntityUtils.isCollectional(rawClass)) {
                final @Nullable Class<?> eltClass;
                final IsProperty atIsProperty = getAnnotation(field, IsProperty.class);
                if (atIsProperty != null && atIsProperty.value() != IsProperty.DEFAULT_VALUE) {
                    eltClass = atIsProperty.value();
                } else if (paramType.getActualTypeArguments().length == 1) {
                    eltClass = classFrom(paramType.getActualTypeArguments()[0]);
                } else {
                    eltClass = null;
                }

                return eltClass == null ? Optional.empty() : Optional.of(t2(rawClass, eltClass));
            }
        }

        return Optional.empty();
    }

    /**
     * Identifies whether property <code>doNotationExp</code> in type <code>entityType</code> is map.
     *
     * @param entityType
     * @param doNotationExp
     * @return
     */
    public static boolean isMap(final Class<?> entityType, final String doNotationExp) {
        final Field field = Finder.findFieldByName(entityType, doNotationExp);
        return Map.class.isAssignableFrom(field.getType());
    }

    /**
     * Identifies whether the specified property is defined as mapped or calculated.
     * The main intent for such information is to identify properties that are query-able.
     * For example, plain properties and {@link CritOnly} properties are not.
     * 
     * @param entitType
     * @param dotNotationExp
     * @return
     */
    public static boolean isMappedOrCalculated(final Class<? extends AbstractEntity<?>> entitType, final String dotNotationExp) {
        return Finder.findFieldByNameOptionally(entitType, dotNotationExp)
               .map(field -> field.isAnnotationPresent(IsProperty.class) && (field.isAnnotationPresent(MapTo.class) || field.isAnnotationPresent(Calculated.class)))
               .orElse(false);
    }
    
    /**
     * Identifies whether property <code>doNotationExp</code> has a type, which is recognized as representing a numeric value such as decimal, money, long or integer.
     *
     * @param entityType
     * @param doNotationExp
     * @return
     */
    public static boolean isNumeric(final Class<?> entityType, final String doNotationExp) {
        final Field field = Finder.findFieldByName(entityType, doNotationExp);
        return isNumeric(field.getType());
    }

    /**
     * Identifies whether the specified property type represents a number, which could be an integer or a decimal, including money.
     *
     * @param propType
     * @return
     */
    public static boolean isNumeric(final Class<?> propType) {
        return isDecimal(propType) || isInteger(propType);
    }

    /**
     * Identifies whether property with a given name in the given entity type is required by definition.
     *
     * @param propName
     * @param entityType
     * @return
     */
    public static boolean isRequiredByDefinition(final String propName, final Class<?> entityType) {
        final Class<?> strippedType = stripIfNeeded(entityType);
        return isRequiredByDefinition(Finder.findFieldByName(strippedType, propName), strippedType);
    }

    /**
     * Identifies whether property, which is represented by a field is required by definition.
     * The value of <code>entityType</code> is the type where the property that is represented by <code>propField</code>, belongs (this is not necessarily the type where the field is declared).
     *
     * @param propField
     * @param entityType
     * @return
     */
    public static boolean isRequiredByDefinition(final Field propField, final Class<?> entityType) {
        final String name = propField.getName();
        return  AbstractEntity.KEY.equals(name) ||
                AnnotationReflector.isAnnotationPresent(propField, Required.class) ||
                isRequiredDesc(name, entityType) ||
                isRequiredCompositeKeyMember(propField);
    }

    /**
     * A convenient helper method for {@link #isRequiredByDefinition(Field, Class)}, which identifies whether a given field represent a non-optional composite key member.
     * This method could be useful elsewhere.
     *
     * @param propField
     * @return
     */
    public static boolean isRequiredCompositeKeyMember(final Field propField) {
        return AnnotationReflector.isAnnotationPresent(propField, CompositeKeyMember.class) && !AnnotationReflector.isAnnotationPresent(propField, ua.com.fielden.platform.entity.annotation.Optional.class);
    }

    /**
     * A convenient helper method for {@link #isRequiredByDefinition(Field, Class)}, which identifies whether a given field represents a required entity description.
     * It is unlikely to be useful outside the current context.
     * Hence, declared as <code>private</code>.
     *
     * @param propName
     * @param entityType
     * @return
     */
    private static boolean isRequiredDesc(final String propName, final Class<?> entityType) {
        return AbstractEntity.DESC.equals(propName) && AnnotationReflector.isAnnotationPresentForClass(DescRequired.class, entityType);
    }
}
