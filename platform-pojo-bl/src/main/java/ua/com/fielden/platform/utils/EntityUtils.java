package ua.com.fielden.platform.utils;

import static ua.com.fielden.platform.entity.AbstractEntity.COMMON_PROPS;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.fetch.FetchProviderFactory;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.ConverterFactory.Converter;

public class EntityUtils {
    private final static Logger logger = Logger.getLogger(EntityUtils.class);

    /** Private default constructor to prevent instantiation. */
    private EntityUtils() {
    }

    /**
     * dd/MM/yyyy format instance
     */
    public static final SimpleDateFormat dateWithoutTimeFormat = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Convenient method for value to {@link String} conversion
     *
     * @param value
     * @param valueType
     * @return
     */
    public static String toString(final Object value, final Class<?> valueType) {
        if (value == null) {
            return "";
        }
        if (valueType == Integer.class || valueType == int.class) {
            return NumberFormat.getInstance().format(value);
        } else if (Number.class.isAssignableFrom(valueType) || valueType == double.class) {
            return NumberFormat.getInstance().format(new BigDecimal(value.toString()));
        } else if (valueType == Date.class || valueType == DateTime.class) {
            final Date date = valueType == Date.class ? (Date) value : ((DateTime) value).toDate();
            return new SimpleDateFormat("dd/MM/yyyy").format(date) + " " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
        } else if (Money.class.isAssignableFrom(valueType)) {
            return value instanceof Number ? new Money(value.toString()).toString() : value.toString();
        } else if (valueType == BigDecimalWithTwoPlaces.class) {
            return value instanceof Number ? String.format("%,10.2f", value) : value.toString();
        } else {
            return value.toString();
        }
    }

    /**
     * Invokes method {@link #toString(Object, Class)} with the second argument being assigned as value's class.
     *
     * @param value
     * @return
     */
    public static String toString(final Object value) {
        if (value == null) {
            return "";
        }
        return toString(value, value.getClass());
    }

    /**
     * Null-safe comparator.
     *
     * @param o1
     * @param o2
     * @return
     */
    public static int safeCompare(final Comparable c1, final Comparable c2) {
        if (c1 == null && c2 == null) {
            return 0;
        } else if (c1 == null) {
            return -1;
        } else if (c2 == null) {
            return 1;
        } else {
            return c1.compareTo(c2);
        }
    }

    /**
     * Null-safe equals.
     *
     * @param o1
     * @param o2
     * @return
     */
    public static boolean safeEquals(final Object o1, final Object o2) {
        if (o1 == null && o2 == null || o1 != null && o1.equals(o2)) {
            return true;
        }
        return false;
    }

    /**
     * Null-safe equals based on the {@link AbstractEntity}'s id property. If id property is not present in both entities then default equals for entities will be called.
     *
     * @param entity1
     * @param entity2
     * @return
     */
    public static boolean areEqual(final AbstractEntity<?> entity1, final AbstractEntity<?> entity2) {
        if (entity1 != null && entity2 != null) {
            if (entity1.getId() == null && entity2.getId() == null) {
                return entity1.equals(entity2);
            } else {
                return safeEquals(entity1.getId(), entity2.getId());
            }
        }
        return entity1 == entity2;
    }

    /**
     * Returns value that indicates whether entity is among entities. The equality comparison is based on {@link #areEquals(AbstractEntity, AbstractEntity)} method
     *
     * @param entities
     * @param entity
     * @return
     */
    public static <T extends AbstractEntity<?>> boolean containsById(final List<T> entities, final T entity) {
        for (final AbstractEntity<?> e : entities) {
            if (areEqual(e, entity)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns index of the entity in the entities list. The equality comparison is based on the {@link #areEquals(AbstractEntity, AbstractEntity)} method.
     *
     * @param entities
     * @param entity
     * @return
     */
    public static <T extends AbstractEntity<?>> int indexOfById(final List<T> entities, final T entity) {
        for (int index = 0; index < entities.size(); index++) {
            if (areEqual(entities.get(index), entity)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * This method chooses appropriate Converter for any types of property. Even for properties of [AbstractEntity's descendant type] or List<[AbstractEntity's descendant type]> or
     * List<String>
     *
     * @param entity
     * @param propertyName
     * @return
     */
    public static Converter chooseConverterBasedUponPropertyType(final AbstractEntity<?> entity, final String propertyName, final ShowingStrategy showingStrategy) {
        final MetaProperty metaProperty = Finder.findMetaProperty(entity, propertyName);
        return chooseConverterBasedUponPropertyType(metaProperty, showingStrategy);
    }

    /**
     * this method chooses appropriate Converter for any types of property. Even for properties of [AbstractEntity's descendant type] or List<[AbstractEntity's descendant type]> or
     * List<String>
     *
     * @param entity
     * @param propertyName
     * @return
     */
    public static Converter chooseConverterBasedUponPropertyType(final Class<?> propertyType, final Class<?> collectionType, final ShowingStrategy showingStrategy) {
        if (propertyType.equals(String.class)) {
            return null;
        } else if (Number.class.isAssignableFrom(propertyType)) {
            return ConverterFactory.createNumberConverter();
        } else if (Money.class.isAssignableFrom(propertyType)) {
            return ConverterFactory.createMoneyConverter();
        } else if (Date.class.equals(propertyType) || DateTime.class.equals(propertyType)) {
            return ConverterFactory.createDateConverter();
        } else if (AbstractEntity.class.isAssignableFrom(propertyType)) {
            return ConverterFactory.createAbstractEntityOrListConverter(showingStrategy);
        } else if (List.class.isAssignableFrom(propertyType)) {
            if (collectionType != null) {
                final Class<?> typeArgClass = collectionType;
                if (AbstractEntity.class.isAssignableFrom(typeArgClass)) {
                    return ConverterFactory.createAbstractEntityOrListConverter(showingStrategy);
                } else if (typeArgClass.equals(String.class)) {
                    return ConverterFactory.createStringListConverter();
                } else {
                    System.out.println(new Exception("listType actualTypeArgument is not String or descendant of AbstractEntity!"));
                    return null;
                }
            } else {
                System.out.println(new Exception("listType is not Parameterized???!!"));
                return null;
            }
        } else if (Enum.class.isAssignableFrom(propertyType)) {
            return ConverterFactory.createTrivialConverter();
        } else {
            return null;
        }
    }

    /**
     * Does the same as {@link #chooseConverterBasedUponPropertyType(AbstractEntity, String)}
     *
     * @param metaProperty
     * @return
     */
    public static Converter chooseConverterBasedUponPropertyType(final MetaProperty metaProperty, final ShowingStrategy showingStrategy) {
        return chooseConverterBasedUponPropertyType(metaProperty.getType(), metaProperty.getPropertyAnnotationType(), showingStrategy);
    }

    /**
     * Obtains {@link MetaProperty} using {@link #findFirstFailedMetaProperty(AbstractEntity, String)} and returns {@link #getLabelText(MetaProperty, boolean)}
     *
     * @return the subject's text value
     * @throws MissingConverterException
     * @throws ClassCastException
     *             if the subject value is not a String
     */
    @SuppressWarnings("unchecked")
    public static String getLabelText(final AbstractEntity entity, final String propertyName, final ShowingStrategy showingStrategy) {
        final MetaProperty metaProperty = findFirstFailedMetaProperty(entity, propertyName);
        return getLabelText(metaProperty, false, showingStrategy);
    }

    public enum ShowingStrategy {
        KEY_ONLY, DESC_ONLY, KEY_AND_DESC
    }

    /**
     * Gets converter for passed {@link MetaProperty} with showKeyOnly param and returns {@link #getLabelText(MetaProperty, Converter)}
     *
     * @param metaProperty
     * @param showKeyOnly
     * @return
     */
    public static String getLabelText(final MetaProperty metaProperty, final boolean returnEmptyStringIfInvalid, final ShowingStrategy showingStrategy) {
        final ConverterFactory.Converter converter = chooseConverterBasedUponPropertyType(metaProperty, showingStrategy);
        return getLabelText(metaProperty, returnEmptyStringIfInvalid, converter);
    }

    /**
     * Returns text value for passed {@link MetaProperty} using passed {@link Converter}.
     *
     * @param returnEmptyStringIfInvalid
     *            - if {@link Boolean#TRUE} passed as this parameter, then empty string will be returned if passed {@link MetaProperty} is invalid (converter is not used at all in
     *            this case). Otherwise {@link MetaProperty#getLastInvalidValue()} will be obtained from invalid {@link MetaProperty}, converted using passed {@link Converter} and
     *            returned.
     *
     * @return the subject's text value
     * @throws MissingConverterException
     * @throws ClassCastException
     *             if the subject value is not a String
     */
    public static String getLabelText(final MetaProperty metaProperty, final boolean returnEmptyStringIfInvalid, final Converter converter) {
        if (metaProperty != null) {
            // hierarchy is valid, only the last property could be invalid
            final Object value = metaProperty.getLastAttemptedValue();
            if (!metaProperty.isValid() && returnEmptyStringIfInvalid) {
                return "";
            }

            return getLabelText(value, converter);
        } else {
            // some property (not the last) is invalid, thus showing empty string
            return "";
        }
    }

    /**
     * Returns label text representation of value using specified converter.
     *
     * @param value
     * @param converter
     * @return
     */
    public static String getLabelText(final Object value, final Converter converter) {
        if (value != null && !value.getClass().equals(String.class) && converter == null) {
            return value.toString();
        }
        final String str = converter != null ? converter.convertToString(value) : (String) value;
        return str == null ? "" : str;
    }

    /**
     * Formats passeed value according to its type.
     *
     * @param value
     * @param valueType
     * @return
     */
    public static String formatTooltip(final Object value, final Class<?> valueType) {
        if (value == null) {
            return "";
        }
        if (valueType == Integer.class) {
            return NumberFormat.getInstance().format(value);
        } else if (Number.class.isAssignableFrom(valueType)) {
            return NumberFormat.getInstance().format(new BigDecimal(value.toString()));
        } else if (valueType == Date.class || valueType == DateTime.class) {
            final Object convertedValue = value instanceof DateTime ? ((DateTime) value).toDate() : value;
            return new SimpleDateFormat("dd/MM/yyyy").format(convertedValue) + " " + DateFormat.getTimeInstance(DateFormat.SHORT).format(convertedValue);
        } else {
            return value.toString();
        }
    }

    /**
     * Checks and answers if the two objects are both {@code null} or equal.
     *
     * <pre>
     * #equals(null, null)  == true
     * #equals(&quot;Hi&quot;, &quot;Hi&quot;)  == true
     * #equals(&quot;Hi&quot;, null)  == false
     * #equals(null, &quot;Hi&quot;)  == false
     * #equals(&quot;Hi&quot;, &quot;Ho&quot;)  == false
     * </pre>
     *
     * @param o1
     *            the first object to compare
     * @param o2
     *            the second object to compare
     * @return boolean {@code true} if and only if both objects are {@code null} or equal
     */
    public static boolean equalsEx(final Object o1, final Object o2) {
        return o1 == o2 || o1 != null && o2 != null && o1.equals(o2);
    }

    /**
     * Returns current value(if property is valid, then its value, otherwise last incorrect value of corresponding meta-property) of property of passed entity.<br>
     * <br>
     * Note : does not support dot-notated property names.
     *
     * @param entity
     * @param propertyName
     * @return
     */
    public static Object getCurrentValue(final AbstractEntity<?> entity, final String propertyName) {
        final MetaProperty metaProperty = entity.getProperty(propertyName);
        if (metaProperty == null) {
            throw new IllegalArgumentException("Couldn't find meta-property named '" + propertyName + "' in " + entity);
        } else {
            return metaProperty.isValid() ? entity.get(propertyName) : metaProperty.getLastInvalidValue();
        }
    }

    /**
     * Returns either {@link MetaProperty} corresponding to last property in <code>propertyName</code> if all previous {@link MetaProperty}ies are valid and without warnings, or
     * first failed {@link MetaProperty} or one with warning.
     *
     * @param entity
     * @param propertyName
     * @return
     */
    public static MetaProperty findFirstFailedMetaProperty(final AbstractEntity<?> entity, final String propertyName) {
        final List<MetaProperty> metaProperties = Finder.findMetaProperties(entity, propertyName);
        return findFirstFailedMetaProperty(metaProperties);
    }

    /**
     * Does the same as method {@link #findFirstFailedMetaProperty(AbstractEntity, String)} but already on the provided list of {@link MetaProperty}s.
     *
     * @param metaProperties
     * @return
     */
    public static MetaProperty findFirstFailedMetaProperty(final List<MetaProperty> metaProperties) {
        MetaProperty firstFailedMetaProperty = metaProperties.get(metaProperties.size() - 1);
        for (int i = 0; i < metaProperties.size(); i++) {
            final MetaProperty metaProperty = metaProperties.get(i);
            if (!metaProperty.isValid() || metaProperty.hasWarnings()) {
                firstFailedMetaProperty = metaProperty;
                break;
            }
        }
        return firstFailedMetaProperty;
    }

    /**
     * This method throws Result (so can be used to specify DYNAMIC validation inside the date setters) when the specified finish/start dates are invalid together.
     *
     * @param start
     * @param finish
     * @param fieldPrefix
     *            - the prefix for the field in the error message for e.g. "actual" or "early".
     * @param finishSetter
     *            - use true if validation have to be performed inside the "finish" date setter, false - inside the "start" date setter
     * @throws Result
     */
    public static void validateDateRange(final Date start, final Date finish, final MetaProperty startProperty, final MetaProperty finishProperty, final boolean finishSetter)
            throws Result {
        if (finish != null) {
            if (start != null) {
                if (start.after(finish)) {
                    throw new Result("", new Exception(finishSetter ? //
                    /*      */finishProperty.getTitle() + " cannot be before " + startProperty.getTitle() + "." //
                    : startProperty.getTitle() + " cannot be after " + finishProperty.getTitle() + "."));
                }
            } else {
                throw new Result("", new Exception(finishSetter ? //
                /*      */finishProperty.getTitle() + " cannot be specified without " + startProperty.getTitle() //
                : startProperty.getTitle() + " cannot be empty when " + finishProperty.getTitle() + " is specified."));
            }
        }
    }

    /**
     * This method throws Result (so can be used to specify DYNAMIC validation inside the date setters) when the specified finish/start date times are invalid together.
     *
     * @param start
     * @param finish
     * @param fieldPrefix
     *            - the prefix for the field in the error message for e.g. "actual" or "early".
     * @param finishSetter
     *            - use true if validation have to be performed inside the "finish" date setter, false - inside the "start" date setter
     * @throws Result
     */
    public static void validateDateTimeRange(final DateTime start, final DateTime finish, final MetaProperty startProperty, final MetaProperty finishProperty, final boolean finishSetter)
            throws Result {
        if (finish != null) {
            if (start != null) {
                if (start.isAfter(finish)) {
                    throw new Result("", new Exception(finishSetter ? //
                    /*      */finishProperty.getTitle() + " cannot be before " + startProperty.getTitle() + "." //
                    : startProperty.getTitle() + " cannot be after " + finishProperty.getTitle() + "."));
                }
            } else {
                throw new Result("", new Exception(finishSetter ? //
                /*      */finishProperty.getTitle() + " cannot be specified without " + startProperty.getTitle() //
                : startProperty.getTitle() + " cannot be empty when " + finishProperty.getTitle() + " is specified."));
            }
        }
    }

    /**
     * A convenient method for validating two integer properties that form a range [from;to].
     * <p>
     * Note, the use use Of Number is not possible because it does not implement interface Comparable due to valid reasons. See
     * http://stackoverflow.com/questions/480632/why-doesnt-java-lang-number-implement-comparable from more.
     *
     * @param start
     * @param finish
     * @param startProperty
     * @param finishProperty
     * @param finishSetter
     * @throws Result
     */
    public static void validateIntegerRange(final Integer start, final Integer finish, final MetaProperty startProperty, final MetaProperty finishProperty, final boolean finishSetter)
            throws Result {
        if (finish != null) {
            if (start != null) {
                if (start.compareTo(finish) > 0) { //  after(finish)
                    throw new Result("", new Exception(finishSetter ? //
                    /*      */finishProperty.getTitle() + " cannot be less than " + startProperty.getTitle() + "." //
                    : startProperty.getTitle() + " cannot be greater than " + finishProperty.getTitle() + "."));
                }
            } else {
                throw new Result("", new Exception(finishSetter ? //
                /*      */finishProperty.getTitle() + " cannot be specified without " + startProperty.getTitle() //
                : startProperty.getTitle() + " cannot be empty when " + finishProperty.getTitle() + " is specified."));
            }
        }
    }

    /**
     * A convenient method for validating two double properties that form a range [from;to].
     *
     * @param start
     * @param finish
     * @param startProperty
     * @param finishProperty
     * @param finishSetter
     * @throws Result
     */
    public static void validateDoubleRange(final Double start, final Double finish, final MetaProperty startProperty, final MetaProperty finishProperty, final boolean finishSetter)
            throws Result {
        if (finish != null) {
            if (start != null) {
                if (start.compareTo(finish) > 0) { //  after(finish)
                    throw new Result("", new Exception(finishSetter ? //
                    /*      */finishProperty.getTitle() + " cannot be less than " + startProperty.getTitle() + "." //
                    : startProperty.getTitle() + " cannot be greater than " + finishProperty.getTitle() + "."));
                }
            } else {
                throw new Result("", new Exception(finishSetter ? //
                /*      */finishProperty.getTitle() + " cannot be specified without " + startProperty.getTitle() //
                : startProperty.getTitle() + " cannot be empty when " + finishProperty.getTitle() + " is specified."));
            }
        }
    }

    /**
     * A convenient method for validating two money properties that form a range [from;to].
     *
     * @param start
     * @param finish
     * @param startProperty
     * @param finishProperty
     * @param finishSetter
     * @throws Result
     */
    public static void validateMoneyRange(final Money start, final Money finish, final MetaProperty startProperty, final MetaProperty finishProperty, final boolean finishSetter)
            throws Result {
        if (finish != null) {
            if (start != null) {
                if (start.compareTo(finish) > 0) { //  after(finish)
                    throw new Result("", new Exception(finishSetter ? //
                    /*      */finishProperty.getTitle() + " cannot be less than " + startProperty.getTitle() + "." //
                    : startProperty.getTitle() + " cannot be greater than " + finishProperty.getTitle() + "."));
                }
            } else {
                throw new Result("", new Exception(finishSetter ? //
                /*      */finishProperty.getTitle() + " cannot be specified without " + startProperty.getTitle() //
                : startProperty.getTitle() + " cannot be empty when " + finishProperty.getTitle() + " is specified."));
            }
        }
    }

    /**
     * Indicates whether type represents enumeration.
     *
     * @param type
     * @return
     */
    public static boolean isEnum(final Class<?> type) {
        return Enum.class.isAssignableFrom(type);
    }

    /**
     * Indicates whether type represents "rangable" values like {@link Number}, {@link Money} or {@link Date}.
     *
     * @param type
     * @return
     */
    public static boolean isRangeType(final Class<?> type) {
        return Number.class.isAssignableFrom(type) || Money.class.isAssignableFrom(type) || Date.class.isAssignableFrom(type);
    }

    /**
     * Indicates whether type represents boolean values.
     *
     * @param type
     * @return
     */
    public static boolean isBoolean(final Class<?> type) {
        return boolean.class.isAssignableFrom(type);
    }

    /**
     * Indicates whether type represents date values.
     *
     * @param type
     * @return
     */
    public static boolean isDate(final Class<?> type) {
        return Date.class.isAssignableFrom(type);
    }

    /**
     * Indicates whether type represents {@link DateTime} values.
     *
     * @param type
     * @return
     */
    public static boolean isDateTime(final Class<?> type) {
        return DateTime.class.isAssignableFrom(type);
    }

    /**
     * Indicates whether type represents string values.
     *
     * @return
     */
    public static boolean isString(final Class<?> type) {
        return String.class.isAssignableFrom(type);
    }

    /**
     * Indicates whether type represents {@link AbstractEntity}-typed values.
     *
     * @return
     */
    public static boolean isEntityType(final Class<?> type) {
        return AbstractEntity.class.isAssignableFrom(type);
    }

    /**
     * Indicates whether type represents {@link Integer}-typed values.
     *
     * @return
     */
    public static boolean isInteger(final Class<?> type) {
        return Integer.class.isAssignableFrom(type);
    }

    /**
     * Indicates whether type represents either {@link BigDecimal} or {@link Money}-typed values.
     *
     * @return
     */
    public static boolean isDecimal(final Class<?> type) {
        return BigDecimal.class.isAssignableFrom(type) || Money.class.isAssignableFrom(type);
    }

    public static boolean isDynamicEntityKey(final Class<?> type) {
        return DynamicEntityKey.class.isAssignableFrom(type);
    }

    /**
     * Indicates that given entity type is mapped to database.
     *
     * @return
     */
    public static boolean isPersistedEntityType(final Class<?> type) {
        return type != null && isEntityType(type) && AnnotationReflector.getAnnotation(type, MapEntityTo.class) != null;
    }

    /**
     * Identifies whether the entity type represent a composite entity.
     *
     * @param entityType
     * @return
     */
    public static <T extends AbstractEntity<?>> boolean isCompositeEntity(final Class<T> entityType) {
        final KeyType keyAnnotation = AnnotationReflector.getAnnotation(entityType, KeyType.class);

        if (keyAnnotation != null) {
            return DynamicEntityKey.class.isAssignableFrom(keyAnnotation.value());
        } else {
            return false;
        }
    }

    /**
     * Indicates that given entity type is based on query model.
     *
     * @return
     */
    public static <ET extends AbstractEntity<?>> boolean isQueryBasedEntityType(final Class<ET> type) {
        return type != null && isEntityType(type) && AnnotationReflector.getAnnotation(type, MapEntityTo.class) == null && getEntityModelsOfQueryBasedEntityType(type).size() > 0;
    }

    /**
     * Returns list of query models, which given entity type is based on (assuming it is after all).
     *
     * @param entityType
     * @return
     */
    public static <ET extends AbstractEntity<?>> List<EntityResultQueryModel<ET>> getEntityModelsOfQueryBasedEntityType(final Class<ET> entityType) {
        final List<EntityResultQueryModel<ET>> result = new ArrayList<EntityResultQueryModel<ET>>();
        try {
            final Field exprField = entityType.getDeclaredField("model_");
            exprField.setAccessible(true);
            result.add((EntityResultQueryModel<ET>) exprField.get(null));
            return result;
        } catch (final Exception e) {
        }
        try {
            final Field exprField = entityType.getDeclaredField("models_");
            exprField.setAccessible(true);
            result.addAll((List<EntityResultQueryModel<ET>>) exprField.get(null));
            return result;
        } catch (final Exception e) {
        }
        return result;
    }

    /**
     * Indicates whether type represents {@link Collection}-typed values.
     *
     * @return
     */
    public static boolean isCollectional(final Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    /**
     * Indicates whether type represents {@link PropertyDescriptor}-typed values.
     *
     * @return
     */
    public static boolean isPropertyDescriptor(final Class<?> type) {
        return PropertyDescriptor.class.isAssignableFrom(type);
    }

    /**
     * Indicates whether type represents {@link AbstractUnionEntity}-typed values.
     *
     * @return
     */
    public static boolean isUnionEntityType(final Class<?> type) {
        return type != null && AbstractUnionEntity.class.isAssignableFrom(type);
    }

    /**
     * Returns a deep copy of an object (all hierarchy of properties will be copied).<br>
     * <br>
     *
     * <b>Important</b> : Depending on {@link ISerialiser} implementation, all classes that are used in passed object hierarchy should correspond some contract. For e.g. Kryo based
     * serialiser requires all the classes to be registered and to have default constructor, simple java serialiser requires all the classes to implement {@link Serializable} etc.
     *
     * @param oldObj
     * @param serialiser
     * @return -- <code>null</code> if <code>oldObj</code> is <code>null</code>, otherwise a deep copy of <code>oldObj</code>.
     *
     */
    public static <T> T deepCopy(final T oldObj, final ISerialiser serialiser) {
        if (oldObj == null) { // obviously return null if oldObj == null
            return null;
        }
        try {
            final byte[] serialised = serialiser.serialise(oldObj);
            return serialiser.deserialise(serialised, (Class<T>) oldObj.getClass());
        } catch (final Exception e) {
            throw deepCopyError(oldObj, e);
        }
        //	final ObjectOutputStream oos = null;
        //	final ObjectInputStream ois = null;
        //	try {
        //	    final ByteArrayOutputStream bos = new ByteArrayOutputStream(); // A
        //	    oos = new ObjectOutputStream(bos); // B
        //	    // serialize and pass the object
        //	    oos.writeObject(oldObj); // C
        //	    oos.flush(); // D
        //	    final ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray()); // E
        //	    ois = new ObjectInputStream(bin); // F
        //	    // return the new object
        //	    return (T) ois.readObject(); // G
        //	} catch (final Exception e) {
        //	    throw deepCopyError(oldObj, e);
        //	} finally {
        //	    try {
        //		if (oos != null) {
        //		    oos.close();
        //		}
        //		if (ois != null) {
        //		    ois.close();
        //		}
        //	    } catch (final IOException e2) {
        //		throw deepCopyError(oldObj, e2);
        //	    }
        //	}
    }

    /**
     * Returns the not enhanced copy of the specified enhancedEntity.
     *
     * @param enhancedEntity
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractEntity<?>> T makeNotEnhanced(final T enhancedEntity) {
        return enhancedEntity == null ? null : (T) enhancedEntity.copy(DynamicEntityClassLoader.getOriginalType(enhancedEntity.getType()));
    }

    /**
     * Returns the not enhanced copy of the list of enhanced entities.
     *
     * @param enhancedEntities
     * @return
     */
    public static <T extends AbstractEntity<?>> List<T> makeNotEnhanced(final List<T> enhancedEntities) {
        if (enhancedEntities == null) {
            return null;
        }
        final List<T> notEnhnacedEntities = new ArrayList<>();
        for (final T entry : enhancedEntities) {
            notEnhnacedEntities.add(makeNotEnhanced(entry));
        }
        return notEnhnacedEntities;
    }

    protected static IllegalStateException deepCopyError(final Object oldObj, final Exception e) {
        final String message = "The deep copy operation has been failed for object [" + oldObj + "]. Cause = [" + e.getMessage() + "].";
        e.printStackTrace();
        logger.error(message);
        return new IllegalStateException(message);
    }

    /**
     * A convenient method for extracting type information from all enum value for a specified enum type.
     *
     * @param <E>
     * @param type
     * @return
     */
    public static <E extends Enum<E>> List<Class<?>> extractTypes(final Class<E> type) {
        final List<Class<?>> result = new ArrayList<Class<?>>();
        result.add(type);
        final EnumSet<E> mnemonicEnumSet = EnumSet.allOf(type);
        for (final E value : mnemonicEnumSet) {
            result.add(value.getClass());
        }
        return result;
    }

    /**
     * Performs {@link AbstractEntity} instance's post-creation actions such as original values setting, definers invoking, dirtiness resetting etc.
     *
     * @param instance
     * @return
     */
    public static AbstractEntity<?> handleMetaProperties(final AbstractEntity<?> instance, final Set<String> proxiedProps) {
        final boolean unionEntity = instance instanceof AbstractUnionEntity;
        if (!unionEntity && instance.getProperties().containsKey("key")) {
            final Object keyValue = instance.get("key");
            if (keyValue != null) {
                // handle property "key" assignment
                instance.set("key", keyValue);
            }
        }

        for (final MetaProperty metaProp : instance.getProperties().values()) {
            final boolean notNull = metaProp != null;
            final boolean notCommonPropOfUnionEntity = notNull && !(COMMON_PROPS.contains(metaProp.getName()) && unionEntity);
            final boolean notProxied = notNull && !(proxiedProps.contains(metaProp.getName()));
            if (notNull && notCommonPropOfUnionEntity && notProxied) {
                final Object newOriginalValue = instance.get(metaProp.getName());
                metaProp.setOriginalValue(newOriginalValue);
                metaProp.define(newOriginalValue);
            }
        }
        if (!unionEntity) {
            instance.setDirty(false);
        }

        return instance;
    }

    /**
     * Splits dot.notated property in two parts: first level property and the rest of subproperties.
     *
     * @param dotNotatedPropName
     * @return
     */
    public static Pair<String, String> splitPropByFirstDot(final String dotNotatedPropName) {
        final int firstDotIndex = dotNotatedPropName.indexOf(".");
        if (firstDotIndex != -1) {
            return new Pair<String, String>(dotNotatedPropName.substring(0, firstDotIndex), dotNotatedPropName.substring(firstDotIndex + 1));
        } else {
            return new Pair<String, String>(dotNotatedPropName, null);
        }
    }

    /**
     * Splits dot.notated property in two parts: last subproperty (as second part) and prior subproperties.
     *
     * @param dotNotatedPropName
     * @return
     */
    public static Pair<String, String> splitPropByLastDot(final String dotNotatedPropName) {
        final int lastDotIndex = findLastDotInString(0, dotNotatedPropName);
        if (lastDotIndex != -1) {
            return new Pair<String, String>(dotNotatedPropName.substring(0, lastDotIndex - 1), dotNotatedPropName.substring(lastDotIndex));
        } else {
            return new Pair<String, String>(null, dotNotatedPropName);
        }
    }

    private static int findLastDotInString(final int fromPosition, final String dotNotatedPropName) {
        final int nextDotIndex = dotNotatedPropName.indexOf(".", fromPosition);
        if (nextDotIndex != -1) {
            return findLastDotInString(nextDotIndex + 1, dotNotatedPropName);
        } else {
            return fromPosition;
        }
    }

    /**
     * Returns true if the provided <code>dotNotationProp</code> is a valid property in the specified entity type.
     *
     * @param type
     * @param dotNotationProp
     * @return
     */
    public static boolean isProperty(final Class<?> type, final String dotNotationProp) {
        try {
            return AnnotationReflector.isAnnotationPresent(Finder.findFieldByName(type, dotNotationProp), IsProperty.class);
        } catch (final Exception ex) {
            return false;
        }
    }

    /**
     * Retrieves all persisted properties fields within given entity type
     *
     * @param entityType
     * @return
     */
    public static List<Field> getRealProperties(final Class entityType) {
        final List<Field> result = new ArrayList<Field>();

        for (final Field propField : Finder.findRealProperties(entityType)) { //, MapTo.class
            if (!(propField.getName().equals("desc") && !hasDescProperty(entityType))) {
                result.add(propField);
            }
        }

        return result;
    }

    public static <ET extends AbstractEntity<?>> boolean hasDescProperty(final Class<ET> entityType) {
        return AnnotationReflector.isAnnotationPresentForClass(DescTitle.class, entityType);
    }

    /**
     * Retrieves all collectional properties fields within given entity type
     *
     * @param entityType
     * @return
     */
    public static List<Field> getCollectionalProperties(final Class entityType) {
        final List<Field> result = new ArrayList<Field>();

        for (final Field propField : Finder.findRealProperties(entityType)) {
            if (Collection.class.isAssignableFrom(propField.getType()) && Finder.hasLinkProperty(entityType, propField.getName())) {
                result.add(propField);
            }
        }

        return result;
    }

    public static class BigDecimalWithTwoPlaces {
    };

    /**
     * Produces list of props that should be added to order model instead of composite key.
     *
     * @param entityType
     * @param prefix
     * @return
     */
    public static List<String> getOrderPropsFromCompositeEntityKey(final Class<? extends AbstractEntity<DynamicEntityKey>> entityType, final String prefix) {
        final List<String> result = new ArrayList<>();
        final List<Field> keyProps = Finder.getKeyMembers(entityType);
        for (final Field keyMemberProp : keyProps) {
            if (DynamicEntityKey.class.equals(getKeyType(keyMemberProp.getType()))) {
                result.addAll(getOrderPropsFromCompositeEntityKey((Class<AbstractEntity<DynamicEntityKey>>) keyMemberProp.getType(), (prefix != null ? prefix + "." : "")
                        + keyMemberProp.getName()));
            } else if (isEntityType(keyMemberProp.getType())) {
                result.add((prefix != null ? prefix + "." : "") + keyMemberProp.getName() + ".key");
            } else {
                result.add((prefix != null ? prefix + "." : "") + keyMemberProp.getName());
            }
        }

        return result;
    }

    public static SortedSet<String> getFirstLevelProps(final Set<String> allProps) {
        final SortedSet<String> result = new TreeSet<String>();
        for (final String prop : allProps) {
            result.add(splitPropByFirstDot(prop).getKey());
        }
        return result;
    }

    /**
     * A convenient method for constructing a pair of property and its title as defined at the entity type level.
     *
     * @param entityType
     * @param propName
     * @return
     */
    public static Pair<String, String> titleAndProp(final Class<? extends AbstractEntity<?>> entityType, final String propName) {
        return new Pair<>(TitlesDescsGetter.getTitleAndDesc(propName, entityType).getKey(), propName);
    }

    /**
     * Returns <code>true</code> if the original value is stale according to fresh value for current version of entity, <code>false</code> otherwise.
     *
     * @param originalValue
     *            -- original value for the property of stale entity
     * @param freshValue
     *            -- fresh value for the property of current (fresh) version of entity
     * @return
     */
    public static boolean isStale(final Object originalValue, final Object freshValue) {
        return !EntityUtils.equalsEx(freshValue, originalValue);
    }

    /**
     * Returns <code>true</code> if the new value for stale entity conflicts with fresh value for current version of entity, <code>false</code> otherwise.
     *
     * @param staleNewValue
     *            -- new value for the property of stale entity
     * @param staleOriginalValue
     *            -- original value for the property of stale entity
     * @param freshValue
     *            -- fresh value for the property of current (fresh) version of entity
     * @return
     */
    public static boolean isConflicting(final Object staleNewValue, final Object staleOriginalValue, final Object freshValue) {
        // old implementation:
        //        return (freshValue == null && staleOriginalValue != null && staleNewValue != null) ||
        //                (freshValue != null && staleOriginalValue == null && staleNewValue == null) ||
        //                (freshValue != null && !freshValue.equals(staleOriginalValue) && !freshValue.equals(staleNewValue));
        return isStale(staleOriginalValue, freshValue) && !EntityUtils.equalsEx(staleNewValue, freshValue);
    }

    /**
     * Creates empty {@link IFetchProvider} for concrete <code>entityType</code>.
     *
     * @param entityType
     * @return
     */
    public static <T extends AbstractEntity<?>> IFetchProvider<T> fetch(final Class<T> entityType) {
        return FetchProviderFactory.createDefaultFetchProvider(entityType);
    }

    /**
     * Creates {@link IFetchProvider} for concrete <code>entityType</code> with 'key' and 'desc' (analog of {@link EntityQueryUtils#fetchKeyAndDescOnly(Class)}).
     *
     * @param entityType
     * @return
     */
    public static <T extends AbstractEntity<?>> IFetchProvider<T> fetchWithKeyAndDesc(final Class<T> entityType) {
        return FetchProviderFactory.createFetchProviderWithKeyAndDesc(entityType);
    }
}