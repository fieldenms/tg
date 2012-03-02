package ua.com.fielden.platform.domaintree.impl;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyKeyException;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * {@link CalculatedProperty} validation for its expression in a provided context.
 *
 * @author TG Team
 *
 */
public class BceRootValidation implements IBeforeChangeEventHandler<Class<?>> {
    @Override
    public Result handle(final MetaProperty property, final Class<?> newRoot, final Class<?> oldValue, final Set<Annotation> mutatorAnnotations) {
	if (newRoot == null) {
	    return new IncorrectCalcPropertyKeyException("The Root type of calculated property cannot be 'null'.");
	}
	if (!EntityUtils.isEntityType(newRoot)) {
	    return new IncorrectCalcPropertyKeyException("The Root type [this case " + newRoot.getSimpleName() + "] of calculated property cannot be non-AbstractEntity type.");
	}
	return Result.successful(newRoot);
    }

}
