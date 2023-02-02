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
            return Result.failure(newValue, "Over 9999.", "<b>The value is over 9999.</b><br><i>Please correct this to continue<i/>");
        } else if (newValue > 100) {
            return Result.warning(newValue, "Over 100.", "<b>The value is over 100.</b><br><i>Please correct this to continue<i/>");
        } else if (newValue > 50) {
            return Result.informative(newValue, "Over 50.", "<b>The value is over 50.</b><br><i>Please correct this to continue<i/>");
        } else {
            return Result.successful(newValue);
        }
    }

}