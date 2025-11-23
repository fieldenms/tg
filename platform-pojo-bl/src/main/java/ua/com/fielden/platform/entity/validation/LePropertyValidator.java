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
import java.util.Date;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;

public final class LePropertyValidator<T> implements IBeforeChangeEventHandler<T> {

    public static final String
            ERR_STRICT_CMP_FAILURE_DATES = "Property [%s] (value [%s]) cannot be after or equal to property [%s] (value [%s]).",
            ERR_CMP_FAILURE_DATES = "Property [%s] (value [%s]) cannot be after property [%s] (value [%s]).",
            ERR_STRICT_CMP_FAILURE = "%s cannot be greater or equal to %s.",
            ERR_CMP_FAILURE = "%s cannot be greater than %s.",
            ERR_EMPTY_START_PROP = "Property [%s] cannot be specified without property [%s]",
            ERR_INVALID_CONFIG = "LeProperty attribute [lt] can either be empty or match the number of properties in attribute [value].";

    private final IDates dates;
    private final String[] otherProperties;
    private final boolean[] lts;
    private final RangeValidatorFunction<T> validator;

    /// @see RangeValidatorFunction#forPropertyType(Class)
    ///
    @Inject
    public LePropertyValidator(
            final IDates dates,
            @Assisted final String[] otherProperties,
            @Assisted final boolean[] lts,
            @Assisted final RangeValidatorFunction<T> validator)
    {
        if (lts.length > 0 && lts.length != otherProperties.length) {
            throw new InvalidArgumentException(ERR_INVALID_CONFIG);
        }
        this.dates = dates;
        this.otherProperties = otherProperties;
        this.lts = lts;
        this.validator = validator;
    }

    @ImplementedBy(LePropertyValidator.FactoryImpl.class)
    public interface Factory {
        <T> LePropertyValidator<T> create(String[] otherProperties, boolean[] lts, RangeValidatorFunction<T> validator);
    }

    @Override
    public Result handle(final MetaProperty<T> property, final T newValue, final Set<Annotation> mutatorAnnotations) {
        for (int index = 0; index < otherProperties.length; index++) {
            final var otherProp = otherProperties[index];
            final var lt = lts.length > 0 && lts[index];
            final MetaProperty<T> otherMp = property.getEntity().getProperty(otherProp);
            final var result = toResult(validator.validate(property, newValue, otherMp, otherMp.getValue(), lt),
                                        property, newValue, otherMp, otherMp.getValue(), lt);
            if (!result.isSuccessful()) {
                return result;
            }
        }
        return Result.successful();
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
                          ? failuref(ERR_STRICT_CMP_FAILURE_DATES, startProperty.getTitle(), strStartValue, endProperty.getTitle(), strEndValue)
                          : failuref(ERR_CMP_FAILURE_DATES, startProperty.getTitle(), strStartValue, endProperty.getTitle(), strEndValue);
                } else {
                    yield strictComparison
                          ? failuref(ERR_STRICT_CMP_FAILURE, startProperty.getTitle(), endProperty.getTitle())
                          : failuref(ERR_CMP_FAILURE, startProperty.getTitle(), endProperty.getTitle());
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
        public <T> LePropertyValidator<T> create(
                final String[] otherProperties,
                final boolean[] lts,
                final RangeValidatorFunction<T> validator)
        {
            return new LePropertyValidator<>(dates, otherProperties, lts, validator);
        }
    }

}

