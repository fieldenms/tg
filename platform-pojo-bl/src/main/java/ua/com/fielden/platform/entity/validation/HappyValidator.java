package ua.com.fielden.platform.entity.validation;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

/**
 * Happy validator always returns a successful validation result.
 *
 * @author TG Team
 *
 */
public class HappyValidator implements IBeforeChangeEventHandler<Object> {

    @Override
    public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	return Result.successful(property.getEntity());
    }

}
