package ua.com.fielden.platform.example.entities.validators;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IValidator;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.example.entities.Advice;

/**
 * Domain validator for property <code>road</code> ({@link Advice#setRoad(boolean)}).
 *
 * @author 01es
 *
 */
public class AdviceRoadValidator implements IValidator {

    @Override
    public Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	final Advice advice = (Advice) property.getEntity();
	if (advice.isDispatched() && (newValue != oldValue)) {
	    return new Result(advice, new IllegalStateException("Road property cannot be changed once advice is dispatched."));
	}
	if ((Boolean) newValue) {
	    advice.setCarrier(null);
	}
	return new Result(advice, "Road property value is correct.");
    }

}
