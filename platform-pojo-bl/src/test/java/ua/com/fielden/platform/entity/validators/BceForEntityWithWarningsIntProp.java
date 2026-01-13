package ua.com.fielden.platform.entity.validators;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.error.Result.warning;

public class BceForEntityWithWarningsIntProp implements IBeforeChangeEventHandler<Integer> {

    @Override
    public Result handle(MetaProperty<Integer> property, Integer newValue, Set<Annotation> mutatorAnnotations) {
        if (newValue != null && newValue > 100) {
            return warning("Value is potentially too large.");
        }
        return successful(newValue);
    }

}
