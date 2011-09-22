package ua.com.fielden.platform.entity.before_change_event_handling;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

/**
 * BCE event handler for testing purposes.
 *
 * @author TG Team
 *
 */
public class BeforeChangeEventHandler2 implements IBeforeChangeEventHandler {

       @Override
    public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	return Result.successful(null);
    }

}
