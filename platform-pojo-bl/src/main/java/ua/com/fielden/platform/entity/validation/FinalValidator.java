package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * BCE implementation to enforce {@link Final} semantics.
 *
 * @author TG Team
 *
 */
public class FinalValidator implements IBeforeChangeEventHandler<Object> {
    
    private final boolean persistentOnly;
    
    public FinalValidator(final boolean persistentOnly) {
        this.persistentOnly = persistentOnly;
    }
    
    @Override
    public Result handle(final MetaProperty<Object> property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
        final AbstractEntity<?> entity = property.getEntity();
        
        if (persistentOnly) {
            // original value should only exist for a persisted property or the ones with explicitly reset state
            // however, there could still be a BCE invocation with already assigned value as part of the dependent properties handling logic
            // that is why need to compare the original an the passed in value
            if (entity.isPersisted() && property.getOriginalValue() != null && !EntityUtils.equalsEx(property.getOriginalValue(), newValue)) {
                return Result.failure(entity, format("Reassigning a persisted value for property [%s] in entity [%s] is not permitted.", property.getName(), entity.getType().getSimpleName()));
            }
        } else if (oldValue != null) {
            return Result.failure(entity, format("Reassigning a value for property [%s] in entity [%s] is not permitted.", property.getName(), entity.getType().getSimpleName()));
        }
        
        return Result.successful("Value is being assigned for the first time.");
    }
}