package ua.com.fielden.platform.domaintree.impl;

import java.lang.annotation.Annotation;
import java.util.Set;

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
public class BceContextualExpressionValidation implements IBeforeChangeEventHandler<String> {
    @Override
    public Result handle(final MetaProperty property, final String newContextualExpression, final String oldValue, final Set<Annotation> mutatorAnnotations) {
        try {
            CalculatedProperty.validateContextualExpression((CalculatedProperty) property.getEntity(), newContextualExpression);
        } catch (final IncorrectCalcPropertyException e) {
            return e;
        }
        return Result.successful(newContextualExpression);
    }
}