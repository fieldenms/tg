package ua.com.fielden.platform.entity.meta.entities;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failure;

public class BceNotPermittedValue implements IBeforeChangeEventHandler<String> {
    private String notPermittedValue = "failure";

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        if (notPermittedValue.equalsIgnoreCase(newValue)) {
            return failure("The value is not permitted");
        }
        return Result.successful(newValue);
    }

}
