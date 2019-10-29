package ua.com.fielden.platform.entity.meta.entities;

import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;

public class BceWithWarning implements IBeforeChangeEventHandler<Integer> {

    @Override
    public Result handle(final MetaProperty<Integer> property, final Integer newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue > 100) {
            return new Warning("You have been warned!");
        }
        
        return successful(newValue);
    }

}
