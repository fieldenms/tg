package ua.com.fielden.platform.entity.validation;

import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.RangeValidatorFunction.Result.EmptyStart;
import ua.com.fielden.platform.entity.validation.RangeValidatorFunction.Result.Fail;
import ua.com.fielden.platform.entity.validation.RangeValidatorFunction.Result.Ok;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.utils.IDates;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;

public final class GePropertyValidator<T> implements IBeforeChangeEventHandler<T> {

    private final IDates dates;
    private final String[] otherProperties;
    private final RangeValidatorFunction<T> validator;

    @Inject
    GePropertyValidator(
            final IDates dates,
            @Assisted final String[] otherProperties,
            @Assisted final RangeValidatorFunction<T> validator)
    {
        this.dates = dates;
        this.otherProperties = otherProperties;
        this.validator = validator;
    }

    @ImplementedBy(FactoryImpl.class)
    public interface Factory {
        <T> GePropertyValidator<T> create(String[] otherProperties, RangeValidatorFunction<T> validator);
    }

    @Override
    public Result handle(final MetaProperty<T> property, final T newValue, final Set<Annotation> mutatorAnnotations) {
        var result = successful();
        final List<Result> successfulResults = new ArrayList<>();
        for (final var otherProp : otherProperties) {
            final MetaProperty<T> otherMp = property.getEntity().getProperty(otherProp);
            final var otherValue = otherMp.getValue();
            if (otherValue != null || successfulResults.isEmpty()) {
                result = toResult(validator.validate(otherMp, otherValue, property, newValue),
                                  otherMp, otherValue, property, newValue);
                if (result.isSuccessful()) {
                    successfulResults.add(result);
                }
            }
        }
        return result;
    }

    private <T> Result toResult(
            final RangeValidatorFunction.Result validationResult,
            final MetaProperty<T> startProperty,
            final T startValue,
            final MetaProperty<T> endProperty,
            final T endValue)
    {
        return switch (validationResult) {
            case Ok $ -> successful();
            case EmptyStart $ ->
                    failuref("Property [%s] cannot be specified without property [%s]", endProperty.getTitle(), startProperty.getTitle());
            case Fail $ -> {
                if (Date.class.isAssignableFrom(startProperty.getType())) {
                    yield failuref("Property [%s] (value [%s]) cannot be before property [%s] (value [%s]).",
                                   endProperty.getTitle(), dates.toString((Date) endValue) ,
                                   startProperty.getTitle(), dates.toString((Date) startValue));
                }
                else {
                    yield failuref("%s cannot be less than %s.", endProperty.getTitle(), startProperty.getTitle());
                }
            }
        };
    }

    static final class FactoryImpl implements Factory {
        private final IDates dates;

        @Inject
        FactoryImpl(final IDates dates) {
            this.dates = dates;
        }

        @Override
        public <T> GePropertyValidator<T> create(
                final String[] otherProperties,
                final RangeValidatorFunction<T> validator)
        {
            return new GePropertyValidator<>(dates, otherProperties, validator);
        }
    }

}
