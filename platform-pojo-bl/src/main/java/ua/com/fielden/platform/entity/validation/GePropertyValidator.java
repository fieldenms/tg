package ua.com.fielden.platform.entity.validation;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GePropertyValidator<T> implements IBeforeChangeEventHandler<T> {

    private final String[] otherProperties;
    private final RangeValidatorFunction<T> validator;

    public GePropertyValidator(final String[] otherProperties, final RangeValidatorFunction<T> validator) {
        this.otherProperties = otherProperties;
        this.validator = validator;
    }

    @Override
    public Result handle(final MetaProperty<T> property, final T newValue, final Set<Annotation> mutatorAnnotations) {
        var result = Result.successful();
        final List<Result> successfulResults = new ArrayList<>();
        for (final var otherProp : otherProperties) {
            final MetaProperty<T> otherMp = property.getEntity().getProperty(otherProp);
            final var otherValue = otherMp.getValue();
            if (otherValue != null || successfulResults.isEmpty()) {
                result = validator.validate(otherMp, otherValue, property, newValue);
                if (result.isSuccessful()) {
                    successfulResults.add(result);
                }
            }
        }
        return result;
    }

}
