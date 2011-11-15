package ua.com.fielden.platform.test.entities.validators;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.test.domain.entities.AdvicePosition;
import ua.com.fielden.platform.test.domain.entities.Rotable;

/**
 * Domain validator for property <code>rotable</code> of {@link AdvicePosition}.
 *
 * @author 01es
 *
 */
public class AdvicePositionRotableValidator implements IBeforeChangeEventHandler<Rotable> {

    @Override
    public Result handle(final MetaProperty property, final Rotable newValue, final Rotable oldValue, final Set<Annotation> mutatorAnnotations) {
	final AdvicePosition pos = (AdvicePosition) property.getEntity();
	if (!newValue.equals(oldValue)) {
	    if (pos.getAdvice().rotables().contains(newValue)) {
		return new Result(pos, new IllegalArgumentException("The same rotable cannot be used twice."));
	    }
	}

	return new Result(pos, "Rotable property value is correct.");
    }

}
