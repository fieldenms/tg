package ua.com.fielden.platform.entity.meta.entities;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.error.Result.warning;

public class BceWithWarning implements IBeforeChangeEventHandler<Integer> {

    @Override
    public Result handle(final MetaProperty<Integer> property, final Integer newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue > 100) {
            return warning("You have been warned!");
        }
        
        return successful(newValue);
    }

}
