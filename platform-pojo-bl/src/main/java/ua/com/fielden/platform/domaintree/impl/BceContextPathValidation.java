package ua.com.fielden.platform.domaintree.impl;

import java.lang.annotation.Annotation;
import java.util.Set;

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
public class BceContextPathValidation implements IBeforeChangeEventHandler<String> {
    @Override
    public Result handle(final MetaProperty property, final String newContextPath, final String oldValue, final Set<Annotation> mutatorAnnotations) {
	final CalculatedProperty cp = (CalculatedProperty) property.getEntity();

	final String message = "The context path [" + newContextPath + "] in type [" + cp.getRoot() + "] of calculated property does not exist.";

	if (!cp.getProperty("root").isValid()) {
	    return new IncorrectCalcPropertyKeyException(message);
	}
	try {
	    DomainTreeEnhancer.validatePath(cp.getRoot(), newContextPath, message);
	} catch (final IncorrectCalcPropertyKeyException e) {
	    return e;
	}
	return Result.successful(newContextPath);
    }
}
