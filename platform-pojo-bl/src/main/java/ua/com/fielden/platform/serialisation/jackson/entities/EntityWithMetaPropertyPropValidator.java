package ua.com.fielden.platform.serialisation.jackson.entities;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.error.Result.warning;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

/**
 * Validator for 'prop' property of {@link EntityWithMetaProperty} testing entity.
 * 
 * @author TG Team
 *
 */
public class EntityWithMetaPropertyPropValidator implements IBeforeChangeEventHandler<String> {
    
    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        if ("Not Ok".equals(newValue)) {
            return failure(newValue, "Custom failure.");
        }
        if ("Ok Ok Warn".equals(newValue)) {
            return warning(newValue, "Custom warning.");
        }
        return successful(newValue);
    }
    
}
