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
public class BceTitleValidation implements IBeforeChangeEventHandler<String> {
    @Override
    public Result handle(final MetaProperty<String> property, final String newTitle, final String oldValue, final Set<Annotation> mutatorAnnotations) {
        final CalculatedProperty calcProperty = (CalculatedProperty) property.getEntity();
        if (calcProperty.getCustomPropertyName() != null) {
            try {
                CalculatedProperty.validateName(calcProperty, calcProperty.getCustomPropertyName());
            } catch (final IncorrectCalcPropertyException e) {
                return e;
            }
        } else {
            try {
                CalculatedProperty.validateTitle(calcProperty, newTitle);
            } catch (final IncorrectCalcPropertyException e) {
                return e;
            }
        }
        return Result.successful(newTitle);
    }
}
