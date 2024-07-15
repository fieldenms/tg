package ua.com.fielden.platform.entity.validators;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

/**
 * A validator for testing purposes that always fails with a rude message.
 *
 * @author TG Team
 */
public class RudeValidator implements IBeforeChangeEventHandler<Object> {

    public static final String ERR_RUDE_MESSAGE = "Seriously? Don't even bother. Your values will never pass my validation.";

    @Override
    public Result handle(MetaProperty<Object> property, Object newValue, Set<Annotation> mutatorAnnotations) {
        return Result.failure(ERR_RUDE_MESSAGE);
    }

}
