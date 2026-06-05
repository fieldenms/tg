package ua.com.fielden.platform.entity.validation;

import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.IDates;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;

public final class GePropertyValidator<T> implements IBeforeChangeEventHandler<T> {

    public static final String
            ERR_STRICT_CMP_FAILURE_DATES = "Property [%s] (value [%s]) cannot be before or equal to property [%s] (value [%s]).",
            ERR_CMP_FAILURE_DATES = "Property [%s] (value [%s]) cannot be before property [%s] (value [%s]).",
            ERR_STRICT_CMP_FAILURE = "%s cannot be less or equal to %s.",
            ERR_CMP_FAILURE = "%s cannot be less than %s.",
            ERR_EMPTY_START_PROP = "Property [%s] cannot be specified without property [%s]",
            ERR_INVALID_CONFIG = "GeProperty attribute [gt] can either be empty or match the number of properties in attribute [value].";

    private final IDates dates;
    private final String[] otherProperties;
    private final boolean[] gts;
    private final RangeValidatorFunction<T> validator;

    @Inject
    GePropertyValidator(
            final IDates dates,
            @Assisted final String[] otherProperties,
            @Assisted final boolean[] gts,
            @Assisted final RangeValidatorFunction<T> validator)
    {
        if (gts.length > 0 && gts.length != otherProperties.length) {
            throw new InvalidArgumentException(ERR_INVALID_CONFIG);
        }

        this.dates = dates;
        this.otherProperties = otherProperties;
        this.gts = gts;
        this.validator = validator;
    }

    @ImplementedBy(FactoryImpl.class)
    public interface Factory {
        <T> GePropertyValidator<T> create(String[] otherProperties, final boolean[] gts, final RangeValidatorFunction<T> validator);
    }

    @Override
    public Result handle(final MetaProperty<T> property, final T newValue, final Set<Annotation> mutatorAnnotations) {
        var result = successful();
        final List<Result> successfulResults = new ArrayList<>();
        for (int index = 0; index < otherProperties.length; index++) {
            final var otherProp = otherProperties[index];
            final var gt = gts.length > 0 && gts[index];
            final MetaProperty<T> otherMp = property.getEntity().getProperty(otherProp);
            final var otherValue = otherMp.getValue();
            if (otherValue != null || successfulResults.isEmpty()) {
                result = toResult(validator.validate(otherMp, otherValue, property, newValue, gt),
                                  otherMp, otherValue, property, newValue, gt);
                if (result.isSuccessful()) {
                    successfulResults.add(result);
                }
            }
        }
        return result;
    }

    private Result toResult(
            final RangeValidatorFunction.Result validationResult,
            final MetaProperty<T> startProperty,
            final T startValue,
            final MetaProperty<T> endProperty,
            final T endValue,
            final boolean strictComparison)
    {
        return switch (validationResult) {
            case Success ->
                    successful();
            case EmptyStart ->
                    failuref(ERR_EMPTY_START_PROP, endProperty.getTitle(), startProperty.getTitle());
            case Failure -> {
                if (EntityUtils.isDate(startProperty.getType())) {
                    final String strStartValue;
                    final String strEndValue;
                    if (EntityUtils.isDateOnly((MetaProperty<Date>) startProperty)) {
                        strStartValue = dates.toStringAsDateOnly((Date) startValue);
                        strEndValue = dates.toStringAsDateOnly((Date) endValue);

                    }
                    else if (EntityUtils.isTimeOnly((MetaProperty<Date>) startProperty)) {
                        strStartValue = dates.toStringAsTimeOnly((Date) startValue);
                        strEndValue = dates.toStringAsTimeOnly((Date) endValue);
                    }
                    else {
                        strStartValue = dates.toString((Date) startValue);
                        strEndValue = dates.toString((Date) endValue);
                    }
                    yield strictComparison
                          ? failuref(ERR_STRICT_CMP_FAILURE_DATES, endProperty.getTitle(), strEndValue, startProperty.getTitle(), strStartValue)
                          : failuref(ERR_CMP_FAILURE_DATES, endProperty.getTitle(), strEndValue, startProperty.getTitle(), strStartValue);
                } else {
                    yield strictComparison
                          ? failuref(ERR_STRICT_CMP_FAILURE, endProperty.getTitle(), startProperty.getTitle())
                          : failuref(ERR_CMP_FAILURE, endProperty.getTitle(), startProperty.getTitle());
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
                final boolean[] gts,
                final RangeValidatorFunction<T> validator)
        {
            return new GePropertyValidator<>(dates, otherProperties, gts, validator);
        }
    }

}
