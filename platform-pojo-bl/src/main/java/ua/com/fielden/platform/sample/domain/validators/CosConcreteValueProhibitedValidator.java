package ua.com.fielden.platform.sample.domain.validators;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;

/**
 * Validator for {@link TgPersistentEntityWithProperties#getCosConcreteValueProhibited()} property.
 * 
 * @author TG Team
 *
 */
public class CosConcreteValueProhibitedValidator implements IBeforeChangeEventHandler<TgPersistentEntityWithProperties> {
    
    @Override
    public Result handle(final MetaProperty<TgPersistentEntityWithProperties> property, final TgPersistentEntityWithProperties newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue != null && "KEY8".equals(newValue.getKey())) {
            return failure("KEY8 value is not permitted.");
        }
        return successful(newValue);
    }
    
}