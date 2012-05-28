package ua.com.fielden.platform.domaintree.impl;

import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isCollectionOrInCollectionHierarchy;

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
public class BceContextualExpressionValidation implements IBeforeChangeEventHandler<String> {
    @Override
    public Result handle(final MetaProperty property, final String newContextualExpression, final String oldValue, final Set<Annotation> mutatorAnnotations) {
	final CalculatedProperty cp = (CalculatedProperty) property.getEntity();

	if (cp.validateRootAndContext() != null) {
	    return cp.validateRootAndContext();
	}

	if (StringUtils.isEmpty(newContextualExpression)) {
	    throw new IncorrectCalcPropertyKeyException("The expression cannot be empty.");
	}

	try {
	    cp.initAst(newContextualExpression);
	} catch (final Exception ex) {
	    return new Result(ex);
	}

	final int levelsToRaiseTheProperty = cp.levelsToRaiseTheProperty();
	if (isCollectionOrInCollectionHierarchy(cp.getRoot(), cp.getContextPath())) { // collectional hierarchy
	    if (levelsToRaiseTheProperty >= 1) {
		return new Result(new IllegalStateException("Aggregated collections are currently unsupported. Please try to use simple expressions under collections (or with ALL / ANY attributes)."));
	    }
	} else {
	    if (levelsToRaiseTheProperty > 1) {
		return new Result(new IllegalStateException("The aggregation cannot be applied twice or more for simple non-collectional entity hirerarchy (\"Total\" values cannot be aggregated)."));
	    }
	}
	return Result.successful(cp.getAst());
    }
}
