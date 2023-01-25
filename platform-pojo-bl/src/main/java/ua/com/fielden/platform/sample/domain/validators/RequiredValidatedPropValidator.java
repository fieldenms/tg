package ua.com.fielden.platform.sample.domain.validators;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

public class RequiredValidatedPropValidator implements IBeforeChangeEventHandler<Integer> {

    @Inject
    public RequiredValidatedPropValidator() {
        super();
    }

    @Override
    public Result handle(final MetaProperty<Integer> property, final Integer newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue > 9999) {
            return Result.failure(newValue, "Over 9999.");
        } else if (newValue > 100) {
            return Result.warning(newValue, "Over 100.");
        } else if (newValue > 50) {
            return Result.information(newValue, "Over 50.");
        } else {
            return Result.successful(newValue);
        }
    }

}