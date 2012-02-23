package ua.com.fielden.platform.domaintree.impl;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
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
public class BceAttributeValidation implements IBeforeChangeEventHandler<CalculatedPropertyAttribute> {
    @Override
    public Result handle(final MetaProperty property, final CalculatedPropertyAttribute newAttribute, final CalculatedPropertyAttribute oldValue, final Set<Annotation> mutatorAnnotations) {
	final CalculatedProperty cp = (CalculatedProperty) property.getEntity();
	if (newAttribute == null) {
	    return new IncorrectCalcPropertyKeyException("The attribute cannot be null.");
	}
	if (!CalculatedPropertyAttribute.NO_ATTR.equals(newAttribute) && cp.category() != null && !CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION.equals(cp.category())) {
	    return new IncorrectCalcPropertyKeyException("ALL / ANY attribute can not be applied to non-collectional sub-property [" + cp.getContextualExpression() + "].");
	}
	return Result.successful(newAttribute);
    }

}
