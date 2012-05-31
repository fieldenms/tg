package ua.com.fielden.platform.domaintree.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.CalcPropertyKeyWarning;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyKeyException;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;

/**
 * {@link CalculatedProperty} validation for its expression in a provided context.
 *
 * @author TG Team
 *
 */
public class BceOriginationPropertyValidation implements IBeforeChangeEventHandler<String> {
    @Override
    public Result handle(final MetaProperty property, final String newOriginationProperty, final String oldValue, final Set<Annotation> mutatorAnnotations) {
	final CalculatedProperty cp = (CalculatedProperty) property.getEntity();

	if (cp.validateRootAndContext() != null) {
	    return cp.validateRootAndContext();
	}

	if (newOriginationProperty == null) {
	    return new IncorrectCalcPropertyKeyException("The origination property cannot be null.");
	}
	// check if the "originationProperty" is correct in context of "contextType":
	if (property.isRequired() && StringUtils.isEmpty(newOriginationProperty)) {
	    return new IncorrectCalcPropertyKeyException("The origination property cannot be empty for Aggregated Expressions.");
	}
	if (cp.getEnhancer() != null) {
	    final Class<?> managedType = cp.getEnhancer().getManagedType(cp.getRoot());
	    final String realOriginationProperty = Reflector.fromRelative2AbsotulePath(cp.getContextPath(), newOriginationProperty);
	    try {
		DomainTreeEnhancer.validatePath(managedType, realOriginationProperty, "The origination property [" + newOriginationProperty + "] does not exist in type [" + managedType + "].");
	    } catch (final IncorrectCalcPropertyKeyException e) {
		return e;
	    }

	    //TODO must determine first whether contextual expression is null or not.
	    final Field field = StringUtils.isEmpty(realOriginationProperty) ? null : Finder.findFieldByName(managedType, realOriginationProperty);
	    final String originationPropExpression;
	    if (field != null && field.isAnnotationPresent(Calculated.class)) {
		originationPropExpression = field.getAnnotation(Calculated.class).value();
	    } else {
		originationPropExpression = newOriginationProperty;
	    }
	    if (cp.getContextualExpression() == null || !cp.getContextualExpression().contains(originationPropExpression)) {
		return new CalcPropertyKeyWarning("The origination property does not take a part in the expression. Is that correct?");
	    }
	}
	return Result.successful(newOriginationProperty);
    }

}
