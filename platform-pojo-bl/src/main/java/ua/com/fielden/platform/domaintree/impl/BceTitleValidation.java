package ua.com.fielden.platform.domaintree.impl;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyKeyException;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

/**
 * {@link CalculatedProperty} validation for its expression in a provided context.
 *
 * @author TG Team
 *
 */
public class BceTitleValidation implements IBeforeChangeEventHandler<String> {
    @Override
    public Result handle(final MetaProperty property, final String newTitle, final String oldValue, final Set<Annotation> mutatorAnnotations) {
	if (StringUtils.isEmpty(newTitle)) {
	    return new IncorrectCalcPropertyKeyException("A title of the calculated property cannot be empty.");
	}
	final String name = CalculatedProperty.generateNameFrom(newTitle);
	if (StringUtils.isEmpty(name)) {
	    return new IncorrectCalcPropertyKeyException("Please specify more appropriate title with some characters (and perhaps digits).");
	}
	return Result.successful(newTitle);
    }
}
