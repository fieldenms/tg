package ua.com.fielden.platform.entity.validation;

import com.google.inject.Injector;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.meta.MetaProperty;

/**
 * A function used to implement {@link LePropertyValidator} and {@link GePropertyValidator}.
 * <p>
 * Range validation is supported only for some types.
 * Method {@link #forPropertyType(Class)} can be used to get a function type that is suitable for a given property type.
 * <p>
 * Most implementations should only override {@link #coreValidate(MetaProperty, Object, MetaProperty, Object)}},
 * but if more precise control over properties with {@code null} values is needed, then override {@link #validate(MetaProperty, Object, MetaProperty, Object)}}.
 */
public abstract class RangeValidatorFunction<T> {

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
            throw new InvalidArgumentException("Range validation is not supported for property type [%s].".formatted(propertyType.getTypeName()));
        }
        return klass;
    }

    @Singleton
    public static final class ComparableValidator<X extends Comparable<X>> extends RangeValidatorFunction<X> {

        @Override
        public Result coreValidate(final MetaProperty<X> startProperty, final X startValue,
                                   final MetaProperty<X> endProperty, final X endValue)
        {
            return startValue.compareTo(endValue) > 0 ? Result.Failure : Result.Success;
        }
    }

}
