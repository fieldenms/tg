package ua.com.fielden.platform.entity.validation;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failure;

/**
 * Unhappy validator always returns a failure.
 * Mainly exists to assist with testing.
 *
 * @author TG Team
 *
 */
public class UnhappyValidator implements IBeforeChangeEventHandler<Object> {

    public static final String ERR_UNHAPPY_VALIDATOR = "Unhappy validator reject any value.";

    @Override
    public Result handle(final MetaProperty<Object> property, final Object newValue, final Set<Annotation> mutatorAnnotations) {
        return failure(ERR_UNHAPPY_VALIDATOR);
    }

}
