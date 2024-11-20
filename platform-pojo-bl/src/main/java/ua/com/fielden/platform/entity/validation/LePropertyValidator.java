package ua.com.fielden.platform.entity.validation;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

import java.lang.annotation.Annotation;
import java.util.Set;

public class LePropertyValidator<T> implements IBeforeChangeEventHandler<T> {

    private final String[] otherProperties;
    private final RangeValidatorFunction<T> validator;

    /**
     * @see RangeValidatorFunction#forPropertyType(Class)
     */
    public LePropertyValidator(final String[] otherProperties, final RangeValidatorFunction<T> validator) {
        this.otherProperties = otherProperties;
        this.validator = validator;
    }

    @Override
    public Result handle(final MetaProperty<T> property, final T newValue, final Set<Annotation> mutatorAnnotations) {
        for (final var otherProp : otherProperties) {
            final MetaProperty<T> otherMp = property.getEntity().getProperty(otherProp);
            final var result = validator.validate(property, newValue, otherMp, otherMp.getValue());
            if (!result.isSuccessful()) {
                return Result.failuref("Property [%s] (value: %s) cannot be greater than [%s] (value: %s).",
                                       property.getTitle(), newValue, otherMp.getTitle(), otherMp.getValue());
            }
        }
        return Result.successful();
    }

}
