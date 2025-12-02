package ua.com.fielden.platform.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import ua.com.fielden.platform.companion.IEntityInstantiator;
import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.entity.*;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.processors.minheritance.SpecifiedBy;
import ua.com.fielden.platform.reflection.*;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.try_wrapper.TryWrapper;
import ua.com.fielden.platform.types.tuples.T2;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Stream.*;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.fetch.FetchProviderFactory.*;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.reflection.AnnotationReflector.*;
import static ua.com.fielden.platform.reflection.Finder.*;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.PROPERTY_SPLITTER;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.unmodifiableListOf;
import static ua.com.fielden.platform.utils.StreamUtils.takeWhile;
import static ua.com.fielden.platform.web.centre.WebApiUtils.dslName;

public class EntityUtils {
    private static final Logger logger = getLogger();

    private static final Cache<Class<?>, Boolean> persistentTypes = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).initialCapacity(512).build();
    private static final Cache<Class<?>, Boolean> syntheticTypes = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).initialCapacity(512).build();
    private static final Cache<Class<?>, Boolean> entityCriteriaTypes = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).initialCapacity(512).build();

    public static final String ERR_PERSISTENT_NATURE_OF_ENTITY_TYPE = "Could not determine persistent nature of entity type [%s].";

    @Inject
    private static IDates dates;

    /** Private default constructor to prevent instantiation. */
    private EntityUtils() {
    }

    /// dd/MM/yyyy format.
    ///
    /// **DEPRECATED:** Use [IDates#dateFormat()] instead.
    ///
    @Deprecated(forRemoval = true, since = "2.2.0")
    public static final String dateWithoutTimeFormat = "dd/MM/yyyy";

    /// Convenient method for value to [String] conversion.
    ///
    /// Returns an empty string if `value` is `null`.
    ///
    public static String toString(final Object value, final Class<?> valueType) {
        if (value == null) {
            return "";
        }
        if (valueType == Integer.class || valueType == int.class) {
            return NumberFormat.getInstance().format(value);
        } else if (Number.class.isAssignableFrom(valueType) || valueType == double.class) {
            return NumberFormat.getInstance().format(new BigDecimal(value.toString()));
        } else if (value instanceof Date date) {
            return dates.toString(date);
        } else if (value instanceof DateTime dateTime) {
            return dates.toString(dateTime.toDate());
        } else if (Money.class.isAssignableFrom(valueType)) {
            return value instanceof Number ? new Money(value.toString()).toString() : value.toString();
        } else if (valueType == BigDecimalWithTwoPlaces.class) {
            return value instanceof Number ? String.format("%,10.2f", value) : value.toString();
        } else if (value instanceof Collection) {
            return "[" + ((Collection<?>) value).stream().map(v -> v + "").collect(Collectors.joining(", ")) + "]";
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
     * Converts {@link Number} to {@link BigDecimal} with the specified {@code scale}.
     *
     * @param number
     * @return
     */
    public static BigDecimal toDecimal(final Number number, final int scale) {
        if (number instanceof final BigDecimal decimal) {
            return decimal.scale() == scale ? decimal : decimal.setScale(scale, RoundingMode.HALF_UP);
        }
        return new BigDecimal(number.toString(), new MathContext(scale, RoundingMode.HALF_UP));
    }

    /**
     * The same as {@link #toDecimal(Number, int)}, but with scale set to 2.
     *
     * @param number
     * @return
     */
    public static BigDecimal toDecimal(final Number number) {
        return toDecimal(number, 2);
    }

    /**
     * This is a convenient function to get the first non-null value, similar as the COALESCE function in SQL.
     * Throws exception {@link NoSuchElementException} if there was no non-null elements.
     *
     * @param value
     * @param alternative
     * @param otherAlternatives
     * @return
     */
    public static <A> A coalesce(final A value, final A alternative, final A... otherAlternatives) {
        return concat(of(value, alternative), otherAlternatives != null ? stream(otherAlternatives) : empty())
                .filter(v -> v != null)
                .findFirst().get();
    }

    /**
     * Null-safe comparator.
     *
     * @param c1
     * @param c2
     * @return
     */
    public static <T> int safeCompare(final Comparable<T> c1, final T c2) {
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
                return equalsEx(entity1.getId(), entity2.getId());
            }
        }
        return entity1 == entity2;
    }

    /**
     * A convenient method to safely compare entity values even if they are <code>null</code>.
     * <p>
     * The <code>null</code> value is considered to be smaller than a non-null value.
     *
     * @param o1
     * @param o2
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static <T extends AbstractEntity<K>, K extends Comparable> int compare(final T o1, final T o2) {
        return safeCompare(o1, o2);
    }


    /**
     * Returns value that indicates whether entity is among entities. The equality comparison is based on {@link #areEqual(AbstractEntity, AbstractEntity)} method
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
     * Returns index of the entity in the entities list. The equality comparison is based on the {@link #areEqual(AbstractEntity, AbstractEntity)} method.
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
     * Also, this method uses <code>.compareTo</code> to compate instances of {@link BigDecimal}.
     * <p>
     *
     * @param o1
     *            the first object to compare
     * @param o2
     *            the second object to compare
     * @return boolean {@code true} if and only if both objects are {@code null} or equal
     */
    public static boolean equalsEx(final Object o1, final Object o2) {
        final boolean result;
        if (o1 == o2) {
            result = true;
        } else if (o1 != null && o2 != null) {
            // comparison of decimals requires special handling and it must be the first check
            if (o1 instanceof BigDecimal && o2 instanceof BigDecimal) {
                result = ((BigDecimal) o1).compareTo((BigDecimal) o2) == 0;
            } else if (o1.getClass().isAssignableFrom(o2.getClass())) {
                result = o1.equals(o2);
            } else if (o2.getClass().isAssignableFrom(o1.getClass())) {
                result = o2.equals(o1);
            } else if (o1 instanceof DateTime && o2 instanceof Date) {
                result = equalsEx(((DateTime) o1).toDate(), o2);
            } else if (o1 instanceof Date && o2 instanceof DateTime) {
                result = equalsEx(o1, ((DateTime) o2).toDate());
            } else {
                result = o1.equals(o2);
            }
        } else {
            result = false;
        }

        return result;
    }

    /**
     * Returns the current property value for an entity.<br>
     * If the property is invalid, then its last invalid value is returned.
     * <br>
     * Note: property dot-expressions are not supported.
     *
     * @param entity
     * @param propertyName
     * @return
     */
    public static Object getCurrentValue(final AbstractEntity<?> entity, final String propertyName) {
        final MetaProperty<?> metaProperty = entity.getProperty(propertyName);
        return metaProperty.isValid() ? entity.get(propertyName) : metaProperty.getLastInvalidValue();
    }

    /**
     * Returns either {@link MetaProperty} corresponding to last property in <code>propertyName</code> if all previous {@link MetaProperty}ies are valid and without warnings, or
     * first failed {@link MetaProperty} or one with warning.
     *
     * @param entity
     * @param propertyName
     * @return
     */
    public static MetaProperty<?> findFirstFailedMetaProperty(final AbstractEntity<?> entity, final String propertyName) {
        final List<MetaProperty<?>> metaProperties = Finder.findMetaProperties(entity, propertyName);
        return findFirstFailedMetaProperty(metaProperties);
    }

    /**
     * Does the same as method {@link #findFirstFailedMetaProperty(AbstractEntity, String)} but already on the provided list of {@link MetaProperty}s.
     *
     * @param metaProperties
     * @return
     */
    public static MetaProperty<?> findFirstFailedMetaProperty(final List<MetaProperty<?>> metaProperties) {
        MetaProperty<?> firstFailedMetaProperty = metaProperties.get(metaProperties.size() - 1);
        for (int i = 0; i < metaProperties.size(); i++) {
            final MetaProperty<?> metaProperty = metaProperties.get(i);
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
     * @param start a lower value for a range.
     * @param finish an upper value for a range.
     * @param startProperty a property representing the start of a range.
     * @param finishProperty a property representing the finish of a range.
     * @param finishSetter specify {@code true} if validation has to be performed for the finish property, {@code false} - for the start property.
     * @throws Result
     */
    public static void validateDateRange(final Date start, final Date finish, final MetaProperty<Date> startProperty, final MetaProperty<Date> finishProperty, final boolean finishSetter, final IDates dates) {
        if (finish != null) {
            if (start != null) {
                if (start.after(finish)) {
                    throw failure(finishSetter
                    ? format("Property [%s] (value [%s]) cannot be before property [%s] (value [%s]).", finishProperty.getTitle(), dates.toString(finish) , startProperty.getTitle(), dates.toString(start))
                    : format("Property [%s] (value [%s]) cannot be after property [%s] (value [%s]).", startProperty.getTitle(), dates.toString(start), finishProperty.getTitle(), dates.toString(finish)));
                }
            } else {
                throw failure(finishSetter
                ? format("Property [%s] (value [%s]) cannot be specified without property [%s].", finishProperty.getTitle(), finish, startProperty.getTitle())
                : format("Property [%s] cannot be empty if property [%s] (value [%s]) if specified.", startProperty.getTitle(), finishProperty.getTitle(), finish));

            }
        }
    }

    /**
     * This method throws {@link Result} when the specified finish/start date times are invalid together.
     *
     * @param start
     * @param finish
     * @param startProperty a property representing the start of a range.
     * @param finishProperty a property representing the finish of a range.
     * @param finishSetter specify {@code true} if validation has to be performed for the finish property, {@code false} - for the start property.
     * @throws Result
     */
    public static void validateDateTimeRange(final DateTime start, final DateTime finish, final MetaProperty<DateTime> startProperty, final MetaProperty<DateTime> finishProperty, final boolean finishSetter, final IDates dates) {
        if (finish != null) {
            if (start != null) {
                if (start.isAfter(finish)) {
                    throw failure(finishSetter
                    ? format("Property [%s] (value [%s]) cannot be before property [%s] (value [%s]).", finishProperty.getTitle(), dates.toString(finish) , startProperty.getTitle(), dates.toString(start))
                    : format("Property [%s] (value [%s]) cannot be after property [%s] (value [%s]).", startProperty.getTitle(), dates.toString(start), finishProperty.getTitle(), dates.toString(finish)));
                }
            } else {
                throw failure(finishSetter
                ? format("Property [%s] (value [%s]) cannot be specified without property [%s].", finishProperty.getTitle(), finish, startProperty.getTitle())
                : format("Property [%s] cannot be empty if property [%s] (value [%s]) if specified.", startProperty.getTitle(), finishProperty.getTitle(), finish));
            }
        }
    }

    /**
     * A convenient method for validating two integer properties that form a range [from;to].
     * <p>
     * Note, the use Of Number is not possible because it does not implement interface Comparable due to valid reasons. See
     * http://stackoverflow.com/questions/480632/why-doesnt-java-lang-number-implement-comparable from more.
     *
     * @param start
     * @param finish
     * @param startProperty
     * @param finishProperty
     * @param finishSetter
     * @throws Result
     */
    public static void validateIntegerRange(final Integer start, final Integer finish, final MetaProperty<Integer> startProperty, final MetaProperty<Integer> finishProperty, final boolean finishSetter) {
        if (finish != null) {
            if (start != null) {
                if (start.compareTo(finish) > 0) { //  after(finish)
                    throw failure(finishSetter
                                  ? finishProperty.getTitle() + " cannot be less than " + startProperty.getTitle() + "."
                                  : startProperty.getTitle() + " cannot be greater than " + finishProperty.getTitle() + ".");
                }
            } else {
                throw failure(finishSetter
                              ? finishProperty.getTitle() + " cannot be specified without " + startProperty.getTitle()
                              : startProperty.getTitle() + " cannot be empty when " + finishProperty.getTitle() + " is specified.");
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
    public static void validateDoubleRange(final Double start, final Double finish, final MetaProperty<Double> startProperty, final MetaProperty<Double> finishProperty, final boolean finishSetter) {
        if (finish != null) {
            if (start != null) {
                if (start.compareTo(finish) > 0) { //  after(finish)
                    throw failure(finishSetter
                                  ? finishProperty.getTitle() + " cannot be less than " + startProperty.getTitle() + "."
                                  : startProperty.getTitle() + " cannot be greater than " + finishProperty.getTitle() + ".");
                }
            } else {
                throw failure(finishSetter
                              ? finishProperty.getTitle() + " cannot be specified without " + startProperty.getTitle()
                              : startProperty.getTitle() + " cannot be empty when " + finishProperty.getTitle() + " is specified.");
            }
        }
    }

    /**
     * A convenient method for validating two {@link BigDecimal} properties that form a range [from;to].
     */
    public static void validateBigDecimalRange(final BigDecimal start, final BigDecimal finish,
                                               final MetaProperty<BigDecimal> startProperty,
                                               final MetaProperty<BigDecimal> finishProperty,
                                               final boolean finishSetter) {
        if (finish != null) {
            if (start != null) {
                if (start.compareTo(finish) > 0) { //  after(finish)
                    throw failure(finishSetter
                                  ? finishProperty.getTitle() + " cannot be less than " + startProperty.getTitle() + "."
                                  : startProperty.getTitle() + " cannot be greater than " + finishProperty.getTitle() + ".");
                }
            } else {
                throw failure(finishSetter
                              ? finishProperty.getTitle() + " cannot be specified without " + startProperty.getTitle()
                              : startProperty.getTitle() + " cannot be empty when " + finishProperty.getTitle() + " is specified.");
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
    public static void validateMoneyRange(final Money start, final Money finish, final MetaProperty<Money> startProperty, final MetaProperty<Money> finishProperty, final boolean finishSetter) {
        if (finish != null) {
            if (start != null) {
                if (start.compareTo(finish) > 0) { //  after(finish)
                    throw failure(finishSetter
                                  ? finishProperty.getTitle() + " cannot be less than " + startProperty.getTitle() + "."
                                  : startProperty.getTitle() + " cannot be greater than " + finishProperty.getTitle() + ".");
                }
            } else {
                throw failure(finishSetter
                              ? finishProperty.getTitle() + " cannot be specified without " + startProperty.getTitle()
                              : startProperty.getTitle() + " cannot be empty when " + finishProperty.getTitle() + " is specified.");
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
     * Indicates whether type represents {@link RichText} values.
     *
     * @return
     */
    public static boolean isRichText(final Class<?> type) {
        return RichText.class.isAssignableFrom(type);
    }

    /**
     * Indicates whether type represents {@link Hyperlink} values.
     *
     * @return
     */
    public static boolean isHyperlink(final Class<?> type) {
        return Hyperlink.class.isAssignableFrom(type);
    }

    /**
     * Indicates whether type represents {@link AbstractEntity}-typed values.
     *
     * @return
     */
    public static boolean isEntityType(final Class<?> type) {
        return type != null && AbstractEntity.class.isAssignableFrom(type);
    }

    /// Indicates whether type represents [IContinuationData]-typed values.
    ///
    public static boolean isContinuationData(final Class<?> type) {
        return type != null && IContinuationData.class.isAssignableFrom(type);
    }

    /// Indicates whether type represents [ActivatableAbstractEntity]-typed values.
    /// Or [AbstractUnionEntity]-typed values with at least one [ActivatableAbstractEntity]-typed property.
    ///
    public static boolean isActivatableEntityOrUnionType(final Class<?> type) {
        return isActivatableEntityType(type)
               || isUnionEntityType(type)
                  && unionProperties((Class<? extends AbstractUnionEntity>) type).stream().map(Field::getType).anyMatch(EntityUtils::isActivatableEntityType);
    }

    /// Indicates whether type represents [ActivatableAbstractEntity]-typed values.
    ///
    public static boolean isActivatableEntityType(final Class<?> type) {
        return type != null && ActivatableAbstractEntity.class.isAssignableFrom(type);
    }

    /// Indicates whether type represents persistent [ActivatableAbstractEntity]-typed values.
    ///
    public static boolean isActivatablePersistentEntityType(final Class<?> type) {
        return isActivatableEntityType(type) && isPersistentEntityType(type);
    }

    /**
     * Indicates whether type represents an integer value, which could be either {@link Integer} or {@link Long}.
     *
     * @return
     */
    public static boolean isInteger(final Class<?> type) {
        return Integer.class.isAssignableFrom(type) || Long.class.isAssignableFrom(type);
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
     * Determines if entity type represents one-2-one entity (e.g. VehicleFinancialDetails for Vehicle).
     *
     * @param entityType
     * @return
     */
    public static boolean isOneToOne(@Nullable final Class<? extends AbstractEntity<?>> entityType) {
        final Class<? extends Comparable<?>> keyType = getKeyType(entityType);
        return isPersistentEntityType(keyType);
    }

    /**
     * Identifies whether an entity type represents a composite entity.
     *
     * @param entityType
     * @return
     */
    public static <T extends AbstractEntity<?>> boolean isCompositeEntity(final Class<T> entityType) {
        return DynamicEntityKey.class.equals(AnnotationReflector.getKeyType(entityType));
    }

    /**
     * Determines whether the provided entity type represents a persistent entity that can be stored in a database.
     */
    public static boolean isPersistentEntityType(@Nullable final Class<?> type) {
        if (type == null) {
            return false;
        } else {
            try {
                return persistentTypes.get(type, () ->
                        isEntityType(type)
                        && !isUnionEntityType(type)
                        && !isSyntheticEntityType(type)
                        && AnnotationReflector.getAnnotation(type, MapEntityTo.class) != null);
            } catch (final Exception ex) {
                final String msg = ERR_PERSISTENT_NATURE_OF_ENTITY_TYPE.formatted(type.getSimpleName());
                logger.error(msg, ex);
                throw new ReflectionException(msg, ex);
            }
        }
    }

    ///
    /// Determines whether the provided entity type represents a persistent entity and has versioning information like created/updated, version, etc.
    /// This type should extend [AbstractPersistentEntity].
    ///
    /// @param type
    /// @return
    ///
    public static boolean isPersistentWithAuditData(@Nullable final Class<?> type) {
        return isPersistentEntityType(type) && AbstractPersistentEntity.class.isAssignableFrom(type);
    }

    /**
     * This was the original method, which had a typo in its name – `persisted` instead of `persistent`.
     * Method {@link #isPersistentEntityType(Class)} should be used instead.
     * In time, this method will be removed.
     */
    @Deprecated(forRemoval = true, since = "1.7.0")
    public static boolean isPersistedEntityType(@Nullable final Class<?> type) {
        return isPersistentEntityType(type);
    }

    /**
     * Returns a hierarchy of entity types starting from the given one.
     * <p>
     * <b>NOTE</b>: This method won't accept generic entity types.
     *
     * @param withAbstractEntity  whether to include {@link AbstractEntity} as the last element
     */
    public static Stream<Class<? extends AbstractEntity<?>>> entityTypeHierarchy(final Class<? extends AbstractEntity<?>> entityType,
                                                                                 final boolean withAbstractEntity) {
        final Stream<Class<? extends AbstractEntity<?>>> stream =
                Stream.iterate(entityType, type -> (Class<? extends AbstractEntity<?>>) type.getSuperclass());
        return withAbstractEntity
                // won't compile without type cast...
                ? StreamUtils.stopAfter(stream, type -> (Class) type == AbstractEntity.class)
                : stream.takeWhile(type -> (Class) type != AbstractEntity.class);
    }

    /**
     * Returns the first persistent entity type of the type hierarchy for {@code entityType}. This could be {@code entityType} itself or the first super type that represents a persistent entity.
     * Otherwise, an empty result is returned.
     *
     * @param entityType
     * @return
     */
    public static Optional<Class<? extends AbstractEntity<?>>> findFirstPersistentTypeInHierarchyFor(final Class<? extends AbstractEntity<?>> entityType) {
        Class<?> type = entityType;
        while (type != AbstractEntity.class) {
            if (isPersistentEntityType(type)) {
                return Optional.of((Class<? extends AbstractEntity<?>>) type);
            }
            type = type.getSuperclass();
        }
        return Optional.empty();
    }

    /**
     * Determines whether the provided entity type is of synthetic nature, which means that is based on an EQL model.
     *
     * @param type
     * @return
     */
    public static boolean isSyntheticEntityType(final Class<?> type) {
        if (!isEntityType(type) || isUnionEntityType(type)) {
            return false;
        } else {
            try {
                return syntheticTypes.get(type, () -> findSyntheticModelFieldFor(type.asSubclass(AbstractEntity.class)) != null);
            } catch (final Exception ex) {
                final String msg = format("Could not determine synthetic nature of entity type [%s].", type.getSimpleName());
                logger.error(msg, ex);
                throw new ReflectionException(msg, ex);
            }
        }
    }

    /**
     * If {@code entityType} contains either static field "model_" or "models_" of the appropriate types, indicating that
     * it's a synthetic entity, returns that field. Otherwise, returns {@code null}.
     * <p>
     * Since issue <a href="https://github.com/fieldenms/tg/issues/1692">#1692</a>, which started generating types as
     * subclasses of the original ones, static fields from supertypes are considered as well.
     *
     * @param entityType  entity type to be analysed
     * @return either a field corresponding to {@code model_} or {@code models_}, or {@code null}
     */
    public static <T extends AbstractEntity<?>> @Nullable Field findSyntheticModelFieldFor(final Class<T> entityType) {
        Class<?> klass = entityType;
        while (klass != AbstractEntity.class) { // iterated thought hierarchy
            for (final Field field : klass.getDeclaredFields()) {
                if (isStatic(field.getModifiers())) {
                    if ("model_".equals(field.getName()) && EntityResultQueryModel.class == field.getType() ||
                        "models_".equals(field.getName()) && List.class.isAssignableFrom(field.getType())) {
                        return field;
                    }
                }
            }
            klass = klass.getSuperclass(); // move to the next superclass in the hierarchy in search for more declared fields
        }
        return null;
    }

    /**
     * Determines whether the provided entity type is of synthetic nature that at the same time is based on a persistent type.
     * This kind of entity most typically should have a model with <code>yieldAll</code> clause.
     *
     * @param type
     * @return
     */
    public static boolean isSyntheticBasedOnPersistentEntityType(final Class<? extends AbstractEntity<?>> type) {
        if (!isSyntheticEntityType(type)) {
            return false;
        }
        return getBasePersistentTypeOpt(type).isPresent();
    }

    /**
     * Returns base persistent type in a hierarchy of {@code type}, if any.
     */
    @SuppressWarnings("unchecked")
    private static Optional<Class<? extends AbstractEntity<?>>> getBasePersistentTypeOpt(final Class<? extends AbstractEntity<?>> type) {
        // Let's traverse the type hierarchy to identify if there is a persistent super type...
        // Such traversal is now required because generation of new types extends the original type.
        // And so, there can be situations where a generated type has a synthetic-based-on-persistent type as its super type, and also needs to be recognised as being synthetic-based-on-persistent.
        // Due to the fact that a generated type can be based on a generated that is based on... etc., type hierarchy traversal is required.
        Class<?> superType = type.getSuperclass();
        while (superType != AbstractEntity.class) {
            if (isPersistentEntityType(superType)) {
                return Optional.of((Class<? extends AbstractEntity<?>>) superType);
            }
            superType = superType.getSuperclass();
        }
        return Optional.empty();
    }

    /// Determines whether the provided entity type models a union-type.
    ///
    public static boolean isUnionEntityType(final Class<?> type) {
        return type != null && AbstractUnionEntity.class.isAssignableFrom(type);
    }

    /// Union entity-typed values can only be validated if they are instrumented as any other entity-typed values.
    /// But for the sake of convenience, uninstrumented values are supported, which requires in-place instrumentation as part of the validation process.
    ///
    /// This method is a utility to perform instrumentation for uninstrumented values.
    ///
    /// TODO Instrumentation will no longer be necessary after #2466.
    ///
    public static <U extends AbstractUnionEntity> U instrument(final U unionEntity, final IEntityInstantiator<U> instantiator) {
        return unionEntity.isInstrumented()
               ? unionEntity
               : copy(unionEntity, instantiator.new_(), ID, VERSION);
    }


    /**
     * Determines whether {@code type} represents entity query criteria.
     * It uses temporal caching to speedup successive calls.
     *
     * @param type
     * @return
     */
    public static boolean isCriteriaEntityType(final Class<? extends AbstractEntity<?>> type) {
        try {
            return entityCriteriaTypes.get(type, () -> {
                return EntityQueryCriteria.class.isAssignableFrom(type);
            });
        } catch (final Exception ex) {
            final String msg = format("Could not determine criteria nature of entity type [%s].", type.getSimpleName());
            logger.error(msg, ex);
            throw new ReflectionException(msg, ex);
        }
    }

    public static boolean isGeneratedMultiInheritanceEntityType(final Class<?> type) {
        return isAnnotationPresent(type, SpecifiedBy.class);
    }

    /**
     * Returns {@code true} if domain introspection is denied for the specified element.
     *
     * @see DenyIntrospection
     */
    public static boolean isIntrospectionDenied(final AnnotatedElement element) {
        return isAnnotationPresent(element, DenyIntrospection.class);
    }

    /**
     * Returns {@code true} if domain introspection is denied for the specified property.
     *
     * @see DenyIntrospection
     */
    public static boolean isIntrospectionDenied(final Class<? extends AbstractEntity<?>> type, final CharSequence propertyPath) {
        return isIntrospectionDenied(Finder.findFieldByName(type, propertyPath));
    }

    /**
     * Returns {@code true} if introspection is allowed for the specified element.
     * For example, GraphQL and Domain Explorer functionality rely on this predicate to filter out entities that should not be supported for querying and review.
     */
    public static boolean isIntrospectionAllowed(final AnnotatedElement element) {
        if (element instanceof Class<?> type) {
            return !isIntrospectionDenied(type) && (isSyntheticEntityType(type) || isPersistentEntityType(type));
        }
        else {
            return !isIntrospectionDenied(element);
        }
    }

    /**
     * Returns {@code true} if introspection is allowed for the specified property.
     */
    public static boolean isIntrospectionAllowed(final Class<? extends AbstractEntity<?>> type, final CharSequence propertyPath) {
        return isIntrospectionAllowed(Finder.findFieldByName(type, propertyPath));
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
     * Returns a deep copy of an object (all hierarchy of properties will be copied).<br>
     * <br>
     *
     * <b>Important</b> : Depending on {@link ISerialiser} implementation, all classes that are used in passed object hierarchy should correspond some contract.
     * For e.g. simple java serialiser requires all the classes to implement {@link Serializable} etc.
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

    protected static IllegalStateException deepCopyError(final Object oldObj, final Exception e) {
        final String message = "The deep copy operation has been failed for object [" + oldObj + "]. Cause = [" + e.getMessage() + "].";
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
        final List<Class<?>> result = new ArrayList<>();
        result.add(type);
        final EnumSet<E> mnemonicEnumSet = EnumSet.allOf(type);
        for (final E value : mnemonicEnumSet) {
            result.add(value.getClass());
        }
        return result;
    }

    /**
     * Splits a property path into an array of simple property names.
     * <p>
     * The supplied path must be valid, otherwise a runtime exception is thrown. Specifically, the path:
     * <ul>
     *   <li> Must not be empty.
     *   <li> Must not contain empty property names (e.g., {@code "person..desc", ".person", "person."})
     * </ul>
     */
    public static String[] splitPropPathToArray(final CharSequence path) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Invalid property path: [%s]".formatted(path));
        }

        if (path.charAt(0) == '.' || path.charAt(path.length() - 1) == '.') {
            throw new IllegalArgumentException("Invalid property path: [%s]".formatted(path));
        }

        final var components = Reflector.DOT_SPLITTER_PATTERN.split(path);
        for (final var component : components) {
            if (component.isEmpty()) {
                throw new IllegalArgumentException("Invalid property path: [%s]".formatted(path));
            }
        }
        return components;
    }

    /**
     * Splits a property path into an array of simple property names, allowing empty names.
     */
    public static String[] laxSplitPropPathToArray(final CharSequence path) {
        return Reflector.DOT_SPLITTER_PATTERN.split(path);
    }

    /**
     * {@link #splitPropPathToArray(CharSequence)} and wrap the result into an unmodifiable list.
     */
    public static List<String> splitPropPath(final CharSequence path) {
        return unmodifiableListOf(splitPropPathToArray(path));
    }

    /**
     * Splits a property path into a list of simple property names, allowing empty names.
     */
    public static List<String> laxSplitPropPath(final CharSequence path) {
        return unmodifiableListOf(laxSplitPropPathToArray(path));
    }

    /**
     * Splits property dot-expression in two parts: first level property and the rest of subproperties.
     * If there is no rest, the 2nd pair element will be {@code null}.
     *
     * @param dotNotatedPropName
     * @return
     */
    public static Pair<String, String> splitPropByFirstDot(final String dotNotatedPropName) {
        final int firstDotIndex = dotNotatedPropName.indexOf(".");
        if (firstDotIndex != -1) {
            return new Pair<>(dotNotatedPropName.substring(0, firstDotIndex), dotNotatedPropName.substring(firstDotIndex + 1));
        } else {
            return new Pair<>(dotNotatedPropName, null);
        }
    }

    /**
     * Splits property dot-expression in two parts: last subproperty (as second part) and prior subproperties.
     *
     * @param dotNotatedPropName
     * @return
     */
    public static Pair<String, String> splitPropByLastDot(final String dotNotatedPropName) {
        final int lastDotIndex = findLastDotInString(0, dotNotatedPropName);
        if (lastDotIndex != -1) {
            return new Pair<>(dotNotatedPropName.substring(0, lastDotIndex - 1), dotNotatedPropName.substring(lastDotIndex));
        } else {
            return new Pair<>(null, dotNotatedPropName);
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
    public static boolean isProperty(final Class<?> type, final CharSequence dotNotationProp) {
        try {
            return AnnotationReflector.isAnnotationPresent(Finder.findFieldByName(type, dotNotationProp), IsProperty.class);
        } catch (final Exception ex) {
            logger.warn(ex);
            return false;
        }
    }

    /**
     * A predicate that evaluates to {@code true} for entity types with "real" property {@code desc}.
     *
     * @param entityType
     * @return
     */
    public static boolean hasDescProperty(final Class<? extends AbstractEntity<?>> entityType) {
        return AnnotationReflector.isAnnotationPresentForClass(DescTitle.class, entityType);
    }

    /**
     * Retrieves all collectional properties of an entity.
     */
    public static Stream<Field> streamCollectionalProperties(final Class<? extends AbstractEntity<?>> entityType) {
        return Finder.streamRealProperties(entityType)
                .filter(prop -> isCollectional(prop.getType()) && Finder.hasLinkProperty(entityType, prop.getName()));
    }

    /**
     * Retrieves all collectional properties of an entity.
     */
    public static List<Field> getCollectionalProperties(final Class<? extends AbstractEntity<?>> entityType) {
        return streamCollectionalProperties(entityType).collect(toImmutableList());
    }

    /**
     * Finds a collectional property with the given simple name.
     */
    public static Optional<Field> findCollectionalProperty(final Class<? extends AbstractEntity<?>> entityType,
                                                           final CharSequence name) {
        return streamCollectionalProperties(entityType)
                .filter(prop -> prop.getName().contentEquals(name))
                .findAny();
    }

    ///
    /// Returns a tuple of `(key, relative name)`, if `type` has a key with a single, entity-typed member.
    /// This is applicable to entities with composite keys that have a single entity-typed member, and entities representing one-2-one relationships.
    ///
    /// Returns empty [Optional] otherwise.
    ///
    @SuppressWarnings("unchecked")
    public static Optional<T2<Class<? extends AbstractEntity<?>>, String>> maybeSingleKeyMemberOfEntityType(final Class<? extends AbstractEntity<?>> type) {
        final var keyMembers = getKeyMembers(type);
        if (keyMembers.size() == 1) {
            if (isCompositeEntity(type)) {
                return isEntityType(keyMembers.getFirst().getType())
                       ? Optional.of(t2((Class<? extends AbstractEntity<?>>) keyMembers.getFirst().getType(), keyMembers.getFirst().getName()))
                       : Optional.empty();
            }
            final var keyType = getKeyType(type);
            return isEntityType(keyType)
                   ? Optional.of(t2((Class<? extends AbstractEntity<?>>) keyType, KEY))
                   : Optional.empty();
        }
        return Optional.empty();
    }

    ///
    /// Returns a base type if `type` is synthetic, based on a persistent entity type.
    ///
    /// Returns empty [Optional] otherwise.
    ///
    public static Optional<Class<? extends AbstractEntity<?>>> getBaseTypeForSyntheticEntity(final Class<? extends AbstractEntity<?>> type) {
        return isSyntheticBasedOnPersistentEntityType(type)
               ? getBasePersistentTypeOpt(type)
               : Optional.empty();
    }

    ///
    /// If `type` is a synthetic-based-on-persistent, then returns its base type.
    /// If `type` represents a one-2-one relationship or has a single entity-typed composite key member, then return that key's type.
    ///
    /// Returns empty [Optional] otherwise.
    ///
    public static Optional<Class<? extends AbstractEntity<?>>> maybeBaseTypeForSyntheticEntityOrSingleKeyMemberEntityType(final Class<? extends AbstractEntity<?>> type) {
        final var baseTypeForSyntheticEntity = getBaseTypeForSyntheticEntity(type);
        return baseTypeForSyntheticEntity.isPresent()
               ? baseTypeForSyntheticEntity
               : maybeSingleKeyMemberOfEntityType(type).map(t2 -> t2._1);
    }

    /// Given a generated multi-inheritance type, returns the specification type that was used to generate it.
    /// [SpecifiedBy] is used to locate the specification type.
    /// It is an error if [SpecifiedBy] is not present on the given type.
    ///
    @SuppressWarnings("unchecked")
    public static Class<? extends AbstractEntity<?>> specTypeFor(final Class<? extends AbstractEntity<?>> multiInheritanceType) {
        final var atSpecifiedBy = getAnnotationForClass(SpecifiedBy.class, multiInheritanceType);
        if (atSpecifiedBy == null) {
            throw new IllegalArgumentException(format(
                    "[%s] is missing annotation @%s or is not a generated multi-inheritance type.",
                    multiInheritanceType.getCanonicalName(), SpecifiedBy.class.getSimpleName()));
        }
        return (Class<? extends AbstractEntity<?>>) atSpecifiedBy.value();
    }

    public static class BigDecimalWithTwoPlaces {
    }

    public static SortedSet<String> getFirstLevelProps(final Set<String> allProps) {
        final SortedSet<String> result = new TreeSet<>();
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

    ////////////////////////////////////////////// ID_AND_VERSION //////////////////////////////////////////////

    /**
     * Creates empty {@link IFetchProvider} for concrete <code>entityType</code> with instrumentation.
     *
     * @param entityType
     * @param instrumented
     * @return
     */
    public static <T extends AbstractEntity<?>> IFetchProvider<T> fetch(final Class<T> entityType, final boolean instrumented) {
        return createDefaultFetchProvider(entityType, instrumented);
    }

    public static <T extends AbstractEntity<?>> IFetchProvider<T> fetch(final Class<T> entityType) {
        return createDefaultFetchProvider(entityType, false);
    }

    /**
     * Creates empty {@link IFetchProvider} for concrete <code>entityType</code> <b>without</b> instrumentation.
     *
     * @param entityType
     * @return
     */
    public static <T extends AbstractEntity<?>> IFetchProvider<T> fetchNotInstrumented(final Class<T> entityType) {
        return createDefaultFetchProvider(entityType, false);
    }

    ////////////////////////////////////////////// KEY_AND_DESC //////////////////////////////////////////////

    /**
     * Creates {@link IFetchProvider} for concrete <code>entityType</code> with 'key' and 'desc' (analog of {@link EntityQueryUtils#fetchKeyAndDescOnly(Class)}) with instrumentation.
     *
     * @param entityType
     * @param instrumented
     * @return
     */
    public static <T extends AbstractEntity<?>> IFetchProvider<T> fetchWithKeyAndDesc(final Class<T> entityType, final boolean instrumented) {
        return createFetchProviderWithKeyAndDesc(entityType, instrumented);
    }

    public static <T extends AbstractEntity<?>> IFetchProvider<T> fetchWithKeyAndDesc(final Class<T> entityType) {
        return createFetchProviderWithKeyAndDesc(entityType, false);
    }

    /**
     * Creates {@link IFetchProvider} for concrete <code>entityType</code> with 'key' and 'desc' (analog of {@link EntityQueryUtils#fetchKeyAndDescOnly(Class)}) <b>without</b> instrumentation.
     *
     * @param entityType
     * @return
     */
    public static <T extends AbstractEntity<?>> IFetchProvider<T> fetchNotInstrumentedWithKeyAndDesc(final Class<T> entityType) {
        return createFetchProviderWithKeyAndDesc(entityType, false);
    }

    ////////////////////////////////////////////// NONE //////////////////////////////////////////////
    /**
     * Creates {@link IFetchProvider} for concrete <code>entityType</code> with no properties and concrete instrumentation.
     *
     * @param entityType
     * @param instrumented
     * @return
     */
    public static <T extends AbstractEntity<?>> IFetchProvider<T> fetchNone(final Class<T> entityType, final boolean instrumented) {
        return createEmptyFetchProvider(entityType, instrumented);
    }

    /**
     * Creates un-instrumented {@link IFetchProvider} for concrete <code>entityType</code> with no properties.
     *
     * @param entityType
     * @return
     */
    public static <T extends AbstractEntity<?>> IFetchProvider<T> fetchNone(final Class<T> entityType) {
        return fetchNone(entityType, false);
    }

    /**
     * Tries to perform shallow copy of collectional value. If unsuccessful, throws unsuccessful {@link Result} describing the error.
     *
     * @param value
     * @return
     */
    public static <T> T copyCollectionalValue(final T value) {
        if (value == null) {
            return null; // return (null) copy
        }
        try {
            final Collection<?> collection = (Collection<?>) value;
            // try to obtain empty constructor to perform shallow copying of collection
            final Constructor<? extends Collection> constructor = collection.getClass().getConstructor();
            final Collection copy = constructor.newInstance();
            copy.addAll(collection);
            // return non-empty copy
            return (T) copy;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
            throw failuref("Collection copying has been failed. Type [%s]. Exception [%s].", value.getClass(), e.getMessage()); // throw result indicating the failure of copying
        }
    }

    /// The same as [#copy], but with variable arity for property names.
    ///
    public static <T extends AbstractEntity> T copy(final AbstractEntity<?> fromEntity, final T toEntity, final CharSequence... skipProperties) {
        copy_(fromEntity, toEntity, Stream.of(skipProperties).map(CharSequence::toString).collect(Collectors.toSet()));
        return toEntity;
    }

    /// The most generic and most straightforward function to copy properties from instance `fromEntity` to `toEntity`,
    /// with the ability to skip the specified properties from being copied.
    ///
    /// @param fromEntity  An instance that is the source from which property values are copied from.
    /// @param toEntity   A destination that is an instance where the property values are copied to.
    /// @param skipProperties  A sequence of property names, which may include ID and VERSION.
    /// @return  `toEntity` is returned for convenience.
    ///
    public static <T extends AbstractEntity> T copy(final AbstractEntity<?> fromEntity, final T toEntity, final Set<? extends CharSequence> skipProperties) {
        copy_(fromEntity, toEntity, skipProperties.stream().map(CharSequence::toString).collect(Collectors.toSet()));
        return toEntity;
    }

    private static <T extends AbstractEntity> void copy_(final AbstractEntity<?> fromEntity, final T toEntity, final Set<String> skipProperties) {
        // Under certain circumstances, copying happens for an uninstrumented entity instance
        // In such cases, there would be no meta-properties, and copying would fail.
        // Therefore, it is important to perform ad-hoc property retrieval via reflection.
        final List<String> realProperties = Finder.streamRealProperties(fromEntity.getType()).map(Field::getName).collect(Collectors.toList());
        // Need to add ID and VERSION in order for them to be treated as entity properties
        // They will get skipped if provided as part of skipProperties array
        realProperties.add(ID);
        realProperties.add(VERSION);

        // Copy each identified property, which is not proxied or skipped into a new instance.
        realProperties.stream()
                .filter(name -> !skipProperties.contains(name))
                .filter(propName -> !fromEntity.proxiedPropertyNames().contains(propName))
                .forEach(propName -> {
                    if (KEY.equals(propName) && toEntity.getKeyType().equals(fromEntity.getKeyType()) && DynamicEntityKey.class.isAssignableFrom(fromEntity.getKeyType())) {
                        toEntity.setKey(new DynamicEntityKey(toEntity));
                    } else {
                        try {
                            toEntity.set(propName, fromEntity.get(propName));
                        } catch (final Exception ex) {
                            logger.trace(() -> "Setter for property %s did not succeed during copying.".formatted(propName), ex);
                        }
                    }
                });
    }

    /**
     * A bijective function between entity instances as Java objects and their synthesised identifier.
     * For the same Java object it returns the same code.
     * The implementation of this function is based on {@link System#identityHashCode(Object)}, which is not always produce unique values.
     * Thus, the computed identity hash code is prepended with full entity type name to further reduce a chance for collision.
     *
     * @param entity
     * @return
     */
    public static String getEntityIdentity(final AbstractEntity<?> entity) {
        return entity.getType().getName() + System.identityHashCode(entity);
    }

    /**
     * A convenient method to fetch using keyValues an optional instance of
     * entity, which is intended to be used to populate a value of the specified
     * property of some other entity, using the fetch model as defined by the
     * fetch provider of that other entity.
     * <p>
     * For example:
     *
     * <pre>
     * final WorkActivityType waType = EntityUtils.<WorkActivityType>fetchEntityForPropOf("waType", coWorkActivity, "FU").orElseThrow(...);
     * </pre>
     *
     * @param propName
     * @param coOther
     * @param keyValues
     * @return
     */
    public static <T extends AbstractEntity<?>> Optional<T> fetchEntityForPropOf(final CharSequence propName, final IEntityReader<?> coOther, final Object... keyValues) {
        final Class<T> entityClass = (Class<T>) PropertyTypeDeterminator.determinePropertyType(coOther.getEntityType(), propName);
        final fetch<T> eFetch = coOther.getFetchProvider().<T> fetchFor(propName).fetchModel();
        return coOther.co(entityClass).findByKeyAndFetchOptional(eFetch, keyValues);
    }

    /**
     * A convenient method to fetch using id an optional instance of entity,
     * which is intended to be used to populate a value of the specified
     * property of some other entity, using the fetch model as defined by the
     * fetch provider of that other entity.
     * <p>
     * For example:
     *
     * <pre>
     * final PmRoutine pmRoutine = EntityUtils.<PmRoutine>fetchEntityForPropOf(pmId, "pmRoutine", co(PmExpendable.class)).orElseThrow(...);
     * </pre>
     */
    public static <T extends AbstractEntity<?>> Optional<T> fetchEntityForPropOf(final Long id, final CharSequence propName, final IEntityReader<?> coOther) {
        final Class<T> entityClass = (Class<T>) PropertyTypeDeterminator.determinePropertyType(coOther.getEntityType(), propName);
        final fetch<T> eFetch = coOther.getFetchProvider().<T> fetchFor(propName).fetchModel();
        return coOther.co(entityClass).findByIdOptional(id, eFetch);
    }

    /**
     * A convenient method to fetch using an existing entity instance an
     * optional instance of entity, which is intended to be used to populate a
     * value of the specified property of some other entity, using the fetch
     * model as defined by the fetch provider of that other entity.
     * <p>
     * For example:
     *
     * <pre>
     * final PmRoutine freshPmRoutine = EntityUtils.<PmRoutine>fetchEntityForPropOf(stalePmRoutine, "pmRoutine", co(PmExpendable.class)).orElseThrow(...);
     * </pre>
     */
    public static <T extends AbstractEntity<?>> Optional<T> fetchEntityForPropOf(final T instance, final CharSequence propName, final IEntityReader<?> coOther) {
        return fetchEntityForPropOf(instance.getId(), propName, coOther);
    }

    /**
     * Finds entity by {@code id} and retrieves it with a fetch model suitable for mutation (i.e. the same as for entity masters).
     * However, if the resultant entity to be mutated then argument {@code co} must correspond to an instrumenting instance.
     *
     * @param id
     * @param co -- either pure reader or mutator if the resultant entity needs to be changed
     * @return
     */
    public static <T extends AbstractEntity<?>> Optional<T> findByIdWithMasterFetch(final IEntityReader<T> co, final long id) {
        return co.findByIdOptional(id, co.getFetchProvider().fetchModel());
    }

    /**
     * Finds entity by {@code key} and retrieves it with a fetch model suitable for mutation (i.e. the same as for entity masters).
     * However, if the resultant entity to be mutated then argument {@code co} must correspond to an instrumenting instance.
     *
     * @param co -- either pure reader or mutator if the resultant entity needs to be changed
     * @param keyValues -- an array of values for entity key members
     * @return
     */
    public static <T extends AbstractEntity<?>> Optional<T> findByKeyWithMasterFetch(final IEntityReader<T> co, final Object... keyValues) {
        return co.findByKeyAndFetchOptional(co.getFetchProvider().fetchModel(), keyValues);
    }

    /**
     * Traverses a property path starting from the end of the path to generate a stream of tuples {@code (property, value)}. The last property in the path is excluded if it is not entity-typed.
     * If the path only contains one non-entity-typed property or it is empty then a stream with only empty property and root entity pair is returned.
     *
     * <p>
     * If the path is invalid then the resultant stream would be empty.
     * If the path is valid, but there {@code null} values on some of the intermediate properties then corresponding sub-path values would be represented as empty {@link Optional}.
     * <p>
     * This functionality is useful if one needs to analyse the nested values of entities that conclude in the property described by {@code propertyPath} starting with entity {@code root}.
     *
     * @param root
     * @param propertyPath
     * @return
     */
    public static Stream<T2<String, Optional<? extends AbstractEntity<?>>>> traversePropPath(final AbstractEntity<?> root, final String propertyPath) {
        if (root == null) {
            return empty();
        } else if (propertyPath == null) {
            return Stream.of(t2("", Optional.of(root)));
        }
        final Stream<String> paths = Stream.iterate(propertyPath, path -> {
            final int indexOfLastDot = lastIndexOf(path, PROPERTY_SPLITTER);
            return indexOfLastDot > 0 ? path.substring(0, indexOfLastDot) :
                                        length(path) > 0 ? "" : null;
        });

        // skip the first element (i.e. last property in the path) if it does not terminate with an entity-typed property
        // if this check fails then the path itself is in error...
        final Either<Exception, Stream<String>> either = TryWrapper.Try(() -> AbstractEntity.class.isAssignableFrom(determinePropertyType(root.getType(), dslName(propertyPath))) ? paths : paths.skip(1));
        return takeWhile(either.getOrElse(Stream::empty), Objects::nonNull).map(path -> {
            return t2(path, ofNullable(isEmpty(path) ? root : root.get(path)));
        });
    }

    /**
     * Gets list of all properties paths representing value of entity key. For composite entities props are listed in key members declaration order taking into account cases of multilevel nesting.
     *
     * @param parentContextPath -- path to key property within EQL query context.
     * @param entityType -- entity type containing key property.
     * @return
     */
    public static List<String> keyPaths(final Class<? extends AbstractEntity<?>> entityType, final String parentContextPath) {
        if (isEmpty(parentContextPath)) {
            throw new IllegalArgumentException("Parent context path is required.");
        }

        return keyPaths(entityType, Optional.of(parentContextPath));
    }

    /**
     * Gets a list of all property paths representing a value of an entity key.
     * For composite entities, props are listed in the order of key member declarations, taking into account cases of multilevel nesting.
     *
     * @param entityType -- entity type containing key property.
     * @return
     */
    public static List<String> keyPaths(final Class<? extends AbstractEntity<?>> entityType) {
        return keyPaths(entityType, Optional.empty());
    }

    private static List<String> keyPaths(final Class<? extends AbstractEntity<?>> entityType, final Optional<String> parentContextPath) {
        final List<String> result = new ArrayList<>();

        for (final Field keyMember : getKeyMembers(entityType)) {
            final String pathToSubprop = parentContextPath.map(path -> path + PROPERTY_SPLITTER + keyMember.getName()).orElse(keyMember.getName());
            final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(entityType, keyMember.getName());
            if (isPersistentEntityType(propType)) {
                result.addAll(keyPaths((Class<? extends AbstractEntity<?>>) propType, pathToSubprop));
            }
            else if (isUnionEntityType(propType)) {
                result.add(pathToSubprop + "." + KEY);
            }
            else {
                // Let's explicitly expand money types property path with its single subproperty "amount".
                // This will facilitate the usage of the keyPaths(..) method within KeyPropertyExtractor logic, which in its turn requires explicit "amount" to be specified.
                final var enhancedPathToSubprop = propType.equals(Money.class) ? pathToSubprop + ".amount" : pathToSubprop;
                result.add(enhancedPathToSubprop);
            }
        }

        return result;
    }

    /// A convenient method checking whether entity values should be enlisted in descending (key) order.
    ///
    public static boolean isNaturalOrderDescending(final Class<? extends AbstractEntity<?>> type) {
        return AnnotationReflector.getAnnotationOptionally(type, KeyType.class).map(KeyType::descendingOrder).orElse(false);
    }

    /// Determines if `propertyName` in `entityType` is a [DateOnly] property.
    ///
    public static boolean isDateOnly(final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        return isAnnotationPresent(findFieldByName(entityType, propertyName), DateOnly.class);
    }

    /// Determines if [MetaProperty] `mp` represents a [DateOnly] property.
    ///
    public static boolean isDateOnly(final MetaProperty<Date> mp) {
        return isAnnotationPresent(findFieldByName(mp.getEntity().getType(), mp.getName()), DateOnly.class);
    }

    /// Determines if `propertyName` in `entityType` is a [TimeOnly] property.
    ///
    public static boolean isTimeOnly(final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        return isAnnotationPresent(findFieldByName(entityType, propertyName), TimeOnly.class);
    }

    /// Determines if [MetaProperty] `mp` represents a [TimeOnly] property.
    ///
    public static boolean isTimeOnly(final MetaProperty<Date> mp) {
        return isAnnotationPresent(findFieldByName(mp.getEntity().getType(), mp.getName()), TimeOnly.class);
    }

}
