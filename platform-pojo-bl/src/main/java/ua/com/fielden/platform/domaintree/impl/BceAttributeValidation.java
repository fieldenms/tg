package ua.com.fielden.platform.domaintree.impl;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
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
public class BceAttributeValidation implements IBeforeChangeEventHandler<CalculatedPropertyAttribute> {
    @Override
    public Result handle(final MetaProperty<CalculatedPropertyAttribute> property, final CalculatedPropertyAttribute newAttribute, final CalculatedPropertyAttribute oldValue, final Set<Annotation> mutatorAnnotations) {
        try {
            CalculatedProperty.validateAttribute((CalculatedProperty) property.getEntity(), newAttribute);
        } catch (final IncorrectCalcPropertyException e) {
            return e;
        }
        return Result.successful(newAttribute);
    }

}
