package ua.com.fielden.platform.expression.entity.validator;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IValidator;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.expression.ExpressionTextToModelConverter;
import ua.com.fielden.platform.expression.entity.ExpressionEntity;

/**
 * {@link IValidator} for expression property of the {@link ExpressionEntity}.
 * 
 * @author TG Team
 *
 */
public class ExpressionValidator implements IValidator {

    @Override
    public Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	final ExpressionEntity expressionEntity = (ExpressionEntity)property.getEntity();
	final ExpressionTextToModelConverter mc = new ExpressionTextToModelConverter(expressionEntity.getEntityClass(), newValue.toString());
	property.getEntity();
	try{
	    mc.convert();
	}catch(final Exception ex){
	    return new Result(ex);
	}
	return Result.successful(expressionEntity);
    }

}
