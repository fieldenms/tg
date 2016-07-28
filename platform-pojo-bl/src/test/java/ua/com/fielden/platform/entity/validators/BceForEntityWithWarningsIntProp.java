package ua.com.fielden.platform.entity.validators;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;

public class BceForEntityWithWarningsIntProp implements IBeforeChangeEventHandler<Integer> {

    @Override
    public Result handle(MetaProperty<Integer> property, Integer newValue, Integer oldValue, Set<Annotation> mutatorAnnotations) {
        if (newValue != null && newValue > 100) {
            return new Warning("Value is potentially too large.");
        }
        return Result.successful(newValue);
    }

}
