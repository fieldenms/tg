package ua.com.fielden.platform.sample.domain.validators;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

import java.lang.annotation.Annotation;
import java.util.Set;

public class StringPropValidator implements IBeforeChangeEventHandler<String> {

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue.contains(",")) {
            return Result.failure("The value can not contain comma");
        }
        return Result.successful(newValue);
    }
}
