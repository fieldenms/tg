package ua.com.fielden.platform.expression.entity.validator;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.expression.ExpressionTextToModelConverter;
import ua.com.fielden.platform.expression.entity.ExpressionEntity;

/**
 * {@link IBeforeChangeEventHandler} for expression property of the {@link ExpressionEntity}.
 *
 * @author TG Team
 *
 */
public class ExpressionValidator implements IBeforeChangeEventHandler<String> {

    @Override
    public Result handle(final MetaProperty property, final String newValue, final String oldValue, final Set<Annotation> mutatorAnnotations) {
	final ExpressionEntity expressionEntity = (ExpressionEntity) property.getEntity();
	final ExpressionTextToModelConverter mc = new ExpressionTextToModelConverter(expressionEntity.getEntityClass(), newValue);
	property.getEntity();
	try{
	    mc.convert();
	}catch(final Exception ex){
	    return new Result(ex);
	}
	return Result.successful(expressionEntity);
    }

}
