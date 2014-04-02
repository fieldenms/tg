package ua.com.fielden.platform.entity.validation;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

/**
 * This validator ensures that new value can be set to a field (via setter) only when an old value is null.
 * 
 * @author TG Team
 * 
 */
public class FinalValidator implements IBeforeChangeEventHandler<Object> {
    @Override
    public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
        final AbstractEntity<?> entity = property.getEntity();
        if ((entity instanceof AbstractEntity) && !(entity).isPersisted()) {
            // everything is allowed for transient instance of AbstractEntity
            return new Result(entity, "Final validator : Value assignment is permitted.");
        } else {
            // otherwise standard validation rules for final are applied
            if (oldValue != null) {
                return new Result(entity, "Final validator : Reassigning the value is not permitted.", new Exception("Final validator : Reassigning the value is not permitted."));
            } else {
                return new Result(entity, "Final validator : Value assignment is permitted.");
            }
        }
    }
}
