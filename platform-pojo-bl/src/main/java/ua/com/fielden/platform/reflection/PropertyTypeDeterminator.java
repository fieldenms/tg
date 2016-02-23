package ua.com.fielden.platform.reflection;

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Contains methods for property type determination. Methods traverses through 1. class hierarchy 2. dot-notation expression.
 *
 * @author TG Team
 *
 */
public class PropertyTypeDeterminator {
    private static final String PROPERTY_SPLITTER = ".";

    /**
     * Let's hide default constructor, which is not needed for a static class.
     */
    private PropertyTypeDeterminator() {
    }

    /**
     * Determines a class of property/function defined by "dotNotationExp".
     *
     * @param type
     *            -- the class that should contain property/function defined by dot-notation expression. (e.g. "Vehicle" contains "status.isGeneratePmWo()")
     * @param dotNotationExp
     *            - a couple of functions/properties joined by ".". (e.g. "vehicle.getKey().getStatus().generatePmWo.getWorkOrder().key")
     * @return -- property/function class
     */
    public static Class<?> determinePropertyType(final Class<?> type, final String dotNotationExp) {
        final String[] propertiesOrFunctions = dotNotationExp.split(Reflector.DOT_SPLITTER);
        Class<?> result = type;
        for (final String propertyOrFunction : propertiesOrFunctions) {
            result = determineClass(result, propertyOrFunction, true, true);
        }
        return stripIfNeeded(result);
    }

    /**
     * Determines class of property/function that should be inside <code>clazz</code> hierarchy.
     *
     * If <code>clazz</code> doesn't have desired property or function -- {@link IllegalArgumentException} will be thrown.
     *
     * @param clazz
     *            -- the class that should contain "propertyOrFunction" (property or function)
     * @param propertyOrFunction
     *            -- the name of property or function (e.g. "isObservable()", "vehicle", "key", "getKey()") -- no dot-notation!
     * @param determineKeyType
     *            -- true => then correct "key"/"getKey()" class is returned, otherwise {@link Comparable} is returned.
     * @param determineElementType
     *            -- true => then correct element type of collectional property is returned, otherwise a type of collection (list, set etc.) is returned.
     *
     * @return
     */
    public static Class<?> determineClass(final Class<?> clazz, final String propertyOrFunction, final boolean determineKeyType, final boolean determineElementType) {
        if (StringUtils.isEmpty(propertyOrFunction)) {
            throw new IllegalArgumentException("Empty string should not be used here. clazz = " + clazz + ", propertyOrFunction = " + propertyOrFunction);
        }
        if (isDotNotation(propertyOrFunction)) {
            throw new IllegalArgumentException("Dot-notation should not be used here. clazz = " + clazz + ", propertyOrFunction = " + propertyOrFunction);
        }
        if (determineKeyType && (AbstractEntity.KEY.equals(propertyOrFunction) || AbstractEntity.GETKEY.equals(propertyOrFunction)) && AbstractEntity.class.isAssignableFrom(clazz)) {
            ////////////////// Key property or getKey() method type determination //////////////////
            return AnnotationReflector.getKeyType(clazz);
        } else {
            if (propertyOrFunction.endsWith("()")) { // parameterless function -- assuming "propertyOrFunction" is name of parameterless method (because propertyName ends with '()')
                try {
                    ////////////////// Parameterless Function return type determination //////////////////
                    final Method method = Reflector.getMethod(clazz, propertyOrFunction.substring(0, propertyOrFunction.length() - 2));
                    final Class<?> theType = method.getReturnType();
                    return determineElementType && isParameterizedType(theType) ? determineElementClassForMethod(method) : theType; // property element type should be retrieved if determineElementType == true
                } catch (final Exception e) {
                    throw new IllegalArgumentException("No " + propertyOrFunction + " method in " + clazz.getSimpleName() + " class.");
                }
            } else { // property -- assuming that "propertyOrFunction" is a name of a property (because its name contains no braces)
                ////////////////// Property class determination using property field. //////////////////
                final Field field = Finder.getFieldByName(clazz, propertyOrFunction);
                final Class<?> theType = field.getType();
                return determineElementType && isParameterizedType(theType) ? determineElementClass(field) : theType; // property element type should be retrieved if determineElementType == true
            }
        }
    }

    private static boolean isParameterizedType(final Class<?> theType) {
        return EntityUtils.isCollectional(theType) /* || EntityUtils.isPropertyDescriptor(theType) */;
    }

    /**
     * If method return type is collectional then it returns type of collection elements.
     *
     * @param field
     * @return
     */
    private static Class<?> determineElementClassForMethod(final Method method) {
        final ParameterizedType paramType = (ParameterizedType) method.getGenericReturnType();
        return classFrom(paramType.getActualTypeArguments()[0]);
    }

    /**
     * If field is collectional property then it returns type of collection elements.
     *
     * @param field
     * @return
     */
    private static Class<?> determineElementClass(final Field field) {
        final ParameterizedType paramType = (ParameterizedType) field.getGenericType();
        return classFrom(paramType.getActualTypeArguments()[0]);
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
     * Determines a type ({@link Type}) of property/function defined by "dotNotationExp".
     *
     * @param type
     *            -- the class that should contain property/function defined by dot-notation expression. (e.g. "Vehicle" contains "status.isGeneratePmWo()")
     * @param dotNotationExp
     *            - a couple of functions/properties joined by ".". (e.g. "vehicle.getKey().getStatus().generatePmWo.getWorkOrder().key")
     * @return -- property/function type
     */
    public static Type determinePropertyTypeWithCorrectTypeParameters(final Class<?> type, final String dotNotationExp) {
        final Pair<Class<?>, String> transformed = transform(type, dotNotationExp);
        final Type resultType = determineType(transformed.getKey(), transformed.getValue());
        return (resultType instanceof Class) ? stripIfNeeded((Class<?>) resultType) : resultType;
    }

    /**
     * Determines type ({@link Type}) of property/function that should be inside <code>clazz</code> hierarchy.
     *
     * If <code>clazz</code> doesn't have desired property or function -- {@link IllegalArgumentException} will be thrown.
     *
     * @param clazz
     *            -- the class that should contain "propertyOrFunction" (property or function)
     * @param propertyOrFunction
     *            -- the name of property or function (e.g. "isObservable()", "vehicle", "key", "getKey()") -- no dot-notation!
     * @param determineKeyType
     *            -- true => then correct "key"/"getKey()" class is returned, otherwise {@link Comparable} is returned.
     *
     * @return
     */
    private static Type determineType(final Class<?> clazz, final String propertyOrFunction) {
        if (StringUtils.isEmpty(propertyOrFunction) || isDotNotation(propertyOrFunction)) {
            throw new IllegalArgumentException("Dot-notation or empty string should not be used here.");
        }
        if ((AbstractEntity.KEY.equals(propertyOrFunction) || AbstractEntity.GETKEY.equals(propertyOrFunction)) && AbstractEntity.class.isAssignableFrom(clazz)) {
            return AnnotationReflector.getKeyType(clazz);
            //////////////////Key property or getKey() method type determination //////////////////
        } else if (propertyOrFunction.endsWith("()")) { // parameterless function -- assuming "propertyOrFunction" is name of parameterless method (because propertyName ends with '()')
            try {
                ////////////////// Parameterless Function return type determination //////////////////
                return Reflector.getMethod(clazz, propertyOrFunction.substring(0, propertyOrFunction.length() - 2)).getGenericReturnType(); // getReturnType();
            } catch (final Exception e) {
                throw new IllegalArgumentException("No " + propertyOrFunction + " method in " + clazz.getSimpleName() + " class.");
            }
        } else { // property -- assuming that "propertyOrFunction" is a name of a property (because its name contains no braces)
            //	    return Reflector.getFieldByName(clazz, propertyOrFunction).getType();
            ////////////////// Property class determination using property accessor. //////////////////
            try {
                return Reflector.obtainPropertyAccessor(clazz, propertyOrFunction).getGenericReturnType();
            } catch (final ReflectionException e) {
                throw new ReflectionException(format("No [%s] property in type [%s].", propertyOrFunction, clazz.getName()), e);
            }
        }
    }

    /**
     * Returns class without enhancements if present.
     *
     * @param clazz
     * @return
     */
    public static Class<?> stripIfNeeded(final Class<?> clazz) {
        return clazz != null && PropertyTypeDeterminator.isEnhanced(clazz) ? stripIfNeeded(clazz.getSuperclass()) : clazz;
    }

    /**
     * Returns if specified class is enhanced by Guice/Hibernate.
     *
     * Enhancer.isEnhanced does not recognise classes enhanced directly with CGLIB (Hibernate), therefore need to provide an alternative way.
     *
     * @param klass
     * @return
     */
    public static boolean isEnhanced(final Class<?> klass) {
        return klass.getName().contains("$$Enhancer") || klass.getName().contains("$$_javassist") || klass.getName().contains("$ByteBuddy$");
    }

    public static boolean isDotNotation(final String exp) {
        return exp.contains(PROPERTY_SPLITTER);
    }

    public static Pair<String, String> penultAndLast(final String dotNotationExp) {
        if (!isDotNotation(dotNotationExp)) {
            throw new IllegalArgumentException("Should be dot-notation.");
        }
        final int indexOfLastDot = dotNotationExp.lastIndexOf(PROPERTY_SPLITTER);
        final String penultPart = dotNotationExp.substring(0, indexOfLastDot);
        final String lastPart = dotNotationExp.substring(indexOfLastDot + 1);
        return new Pair<String, String>(penultPart, lastPart);
    }

    public static Pair<String, String> firstAndRest(final String dotNotationExp) {
        if (!isDotNotation(dotNotationExp)) {
            throw new IllegalArgumentException("Should be dot-notation.");
        }
        final int indexOfFirstDot = dotNotationExp.indexOf(PROPERTY_SPLITTER);
        final String firstPart = dotNotationExp.substring(0, indexOfFirstDot);
        final String restPart = dotNotationExp.substring(indexOfFirstDot + 1);
        return new Pair<String, String>(firstPart, restPart);
    }

    /**
     * Transforms "type/dotNotationExp" pair into form of "penultPropertyType/lastPropertyName".
     *
     * @param type
     * @param dotNotationExp
     * @return
     */
    public static Pair<Class<?>, String> transform(final Class<?> type, final String dotNotationExp) {
        if (isDotNotation(dotNotationExp)) { // dot-notation expression defines property/function.
            final Pair<String, String> pl = penultAndLast(dotNotationExp);
            return new Pair<Class<?>, String>(determinePropertyType(type, pl.getKey()), pl.getValue());
        } else { // empty or first level property/function.
            return new Pair<Class<?>, String>(type, dotNotationExp);
        }
    }

    /**
     * Identifies whether property <code>doNotationExp</code> in type <code>entityType</code> is collectional.
     *
     * @param entityType
     * @param doNotationExp
     * @return
     */
    public static boolean isCollectional(final Class<?> entityType, final String doNotationExp) {
        final Field field = Finder.findFieldByName(entityType, doNotationExp);
        return EntityUtils.isCollectional(field.getType());
    }
}
