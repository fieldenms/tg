package ua.com.fielden.platform.entity.validation;

import com.google.inject.Injector;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

import static java.lang.String.format;

/**
 * A function used to implement {@link LePropertyValidator} and {@link GePropertyValidator}.
 * Although it returns a {@link Result}, returned instances are not guaranteed to contain an informative message.
 * It is up to the actual validators to provide them.
 * <p>
 * Range validation is supported only for some types. {@link #forPropertyType(Class)} can be used to obtain a function
 * type suitable for a given property type.
 * <p>
 * Most implementations should only override {@link #coreValidate(MetaProperty, Object, MetaProperty, Object)}}, but if
 * more precise control over properties with {@code null} values is needed, then override {@link #validate(MetaProperty, Object, MetaProperty, Object)}}.
 */
public abstract class RangeValidatorFunction<T> {

    public Result validate(final MetaProperty<T> startProperty, final T startValue,
                           final MetaProperty<T> endProperty, final T endValue)
    {
        if (startValue == null && endValue == null) {
            return Result.successful("Null is not applicable for validation.");
        } else if (startValue == null && endValue != null) {
            return Result.failure(format("Property [%s] cannot be specified without property [%s]",
                                         startProperty.getTitle(), startProperty.getTitle()));
        } else if (startValue != null && endValue == null) {
            return Result.successful();
        } else {
            return coreValidate(startProperty, startValue, endProperty, endValue);
        }
    }

    protected abstract Result coreValidate(final MetaProperty<T> startProperty, final T startValue,
                                           final MetaProperty<T> endProperty, final T endValue);

    /**
     * Returns a function type that supports range validation of properties of the given type.
     * If the type is unsupported, throws an exception.
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
            return startValue.compareTo(endValue) > 0
                    ? Result.failuref("Property [%s] (value: %s) cannot be greater than property [%s] (value: %s).",
                                      startProperty.getTitle(), startValue, endProperty.getTitle(), endValue)
                    : Result.successful();
        }
    }

}
