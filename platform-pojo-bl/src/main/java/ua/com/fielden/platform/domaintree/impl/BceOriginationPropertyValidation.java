package ua.com.fielden.platform.domaintree.impl;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.CalcPropertyWarning;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyException;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

/**
 * {@link CalculatedProperty} validation for its expression in a provided context.
 *
 * @author TG Team
 *
 */
public class BceOriginationPropertyValidation implements IBeforeChangeEventHandler<String> {
    @Override
    public Result handle(final MetaProperty property, final String newOriginationProperty, final String oldValue, final Set<Annotation> mutatorAnnotations) {
	try {
	    CalculatedProperty.validateOriginationProperty((CalculatedProperty) property.getEntity(), newOriginationProperty);
	} catch (final IncorrectCalcPropertyException e) {
	    return e;
	} catch (final CalcPropertyWarning w) {
	    return w;
	}
	return Result.successful(newOriginationProperty);
    }

}
