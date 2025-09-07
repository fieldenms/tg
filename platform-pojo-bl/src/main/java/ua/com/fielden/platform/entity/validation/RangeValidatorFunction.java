package ua.com.fielden.platform.entity.validation;

import com.google.inject.Injector;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.annotation.DateOnly;
import ua.com.fielden.platform.entity.annotation.TimeOnly;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.AnnotationReflector;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import static ua.com.fielden.platform.entity.exceptions.InvalidArgumentException.requireNotNullArgument;

/// A function used to implement [LePropertyValidator] and [GePropertyValidator].
///
/// Range validation is supported only for some types.
/// Method [#forPropertyType(Class)] can be used to get a function type that is suitable for a given property type.
///
/// Most implementations should only override [#coreValidate(MetaProperty,Object,MetaProperty,Object)]},
/// but if more precise control over properties with `null` values is needed, then override [#validate(MetaProperty,Object,MetaProperty,Object)]}.
///
public abstract class RangeValidatorFunction<T> {

    public static final String ERR_NOT_SUPPORTED_PROPERTY_TYPE = "Range validation is not supported for property type [%s].";

    public Result validate(final MetaProperty<T> startProperty, final T startValue,
                           final MetaProperty<T> endProperty, final T endValue)
    {
        if (startValue == null && endValue == null) {
            return Result.Success;
        } else if (startValue == null && endValue != null) {
            return Result.EmptyStart;
        } else if (startValue != null && endValue == null) {
            return Result.Success;
        } else {
            return coreValidate(startProperty, startValue, endProperty, endValue);
        }
    }

    protected abstract Result coreValidate(final MetaProperty<T> startProperty, final T startValue,
                                           final MetaProperty<T> endProperty, final T endValue);

    public enum Result {
        Success,
        /**
         * A failure condition, where the start value of the range is empty, while the end value is not.
         */
        EmptyStart,
        /**
         * A failure condition, where the start value of the range is greater than the end value.
         */
        Failure;
    }

    /**
     * Returns a function type that supports range validation for properties of the given type.
     * If the type is unsupported, an exception is thrown.
     * Returned function type should be instantiated with an {@linkplain Injector#getInstance(Class) injector}.
     */
    public static <T> Class<RangeValidatorFunction<T>> forPropertyType(final Class<T> propertyType) {
        // use a raw type to satisfy the compiler
        final Class klass;
        if (Comparable.class.isAssignableFrom(propertyType) || propertyType == int.class || propertyType == double.class) {
            klass = ComparableValidator.class;
        }
        // add more clauses if needed
        else {
            throw new InvalidArgumentException(ERR_NOT_SUPPORTED_PROPERTY_TYPE.formatted(propertyType.getTypeName()));
        }
        return klass;
    }

    @Singleton
    public static final class ComparableValidator<X extends Comparable<X>> extends RangeValidatorFunction<X> {

        @Override
        public Result coreValidate(final MetaProperty<X> startProperty, final X startValue,
                                   final MetaProperty<X> endProperty, final X endValue)
        {
            requireNotNullArgument(startValue, "startValue");
            requireNotNullArgument(endValue, "endValue");

            final int cmp;
            // Date properties may can be @TimeOnly or @DateOnly.
            // Comparison needs to take this into account.
            if (startValue instanceof Date fromDate && endValue instanceof Date toDate) {
                final var entityType = startProperty.getEntity().getType();
                if (AnnotationReflector.isPropertyAnnotationPresent(TimeOnly.class, entityType, startProperty.getName())
                    && AnnotationReflector.isPropertyAnnotationPresent(TimeOnly.class, entityType, endProperty.getName()))
                {
                    cmp = compareTimeOnly(fromDate, toDate);
                }
                else if (AnnotationReflector.isPropertyAnnotationPresent(DateOnly.class, entityType, startProperty.getName())
                        && AnnotationReflector.isPropertyAnnotationPresent(DateOnly.class, entityType, endProperty.getName()))
                {
                    cmp = compareDateOnly(fromDate, toDate);
                }
                else {
                    cmp = startValue.compareTo(endValue);
                }
            }
            else {
                cmp = startValue.compareTo(endValue);
            }

            return cmp > 0 ? Result.Failure : Result.Success;
        }
    }

    /// Compares only the time portion using [LocalTime].
    /// @return  Negative value if `time(from) < time(to)``, 0 if equal, positive if `time(from) > time(to)`.
    ///
    public static int compareTimeOnly(final Date from, final Date to) {
        requireNotNullArgument(from, "from");
        requireNotNullArgument(to, "to");

        final LocalTime fromTime = from.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
        final LocalTime toTime = to.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime();

        return fromTime.compareTo(toTime);
    }

    /// Compares only the date portion using [LocalDate].
    /// @return  Negative value if `date(from) < date(to)``, 0 if equal, positive if `date(from) > date(to)`.
    ///
    public static int compareDateOnly(final Date from, final Date to) {
        requireNotNullArgument(from, "from");
        requireNotNullArgument(to, "to");

        final LocalDate fromDate = from.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        final LocalDate toDate = to.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return fromDate.compareTo(toDate);
    }

}
