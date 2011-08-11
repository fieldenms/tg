package ua.com.fielden.platform.property.validator;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IValidator;
import ua.com.fielden.platform.error.Result;

/**
 * Can be used to validate any property of type string based on a regular expression.
 *
 * @author TG Team
 *
 */
public class StringValidator implements IValidator {

    private final String regex;

    public StringValidator(final String regex) {
	this.regex = regex;
    }

    @Override
    public Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	if (newValue != null && !newValue.toString().matches(regex)) {
		 return new Result(newValue, new IllegalArgumentException("Value '" + newValue + "' of " + property.getTitle() + " does not match the required pattern."));
	}

	return Result.successful(newValue);
    }

}
