package ua.com.fielden.platform.entity.validation;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

/**
 * This validator implements a check for the length of a string property.
 * 
 * @author 01es
 * 
 */
public class MaxLengthValidator implements IBeforeChangeEventHandler {
    private final Integer limit;

    public MaxLengthValidator(final Integer limit) {
	this.limit = limit;
    }

    @Override
    public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	final Object entity = property.getEntity();
	final String value = newValue + ""; // added empty string to convert newValue to string without invoking toString()
	if (StringUtils.isEmpty(value)) { // no violation
	    return new Result(entity, "Value is empty.");
	}

	return value.length() > limit ? new Result(entity, new Exception("Value '" + value + "' is longer than " + limit + " characters.")) : new Result(entity, "Value '" + value
		+ "' is within the limit of " + limit + " characters.");
    }

}
