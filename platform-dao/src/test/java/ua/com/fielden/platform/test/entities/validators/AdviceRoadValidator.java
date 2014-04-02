package ua.com.fielden.platform.test.entities.validators;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.test.domain.entities.Advice;

/**
 * Domain validator for property <code>road</code> ({@link Advice#setRoad(boolean)}).
 * 
 * @author 01es
 * 
 */
public class AdviceRoadValidator implements IBeforeChangeEventHandler<Boolean> {

    @Override
    public Result handle(final MetaProperty property, final Boolean newValue, final Boolean oldValue, final Set<Annotation> mutatorAnnotations) {
        final Advice advice = (Advice) property.getEntity();
        if (advice.isDispatched() && (newValue != oldValue)) {
            return new Result(advice, new IllegalStateException("Road property cannot be changed once advice is dispatched."));
        }
        if (newValue) {
            advice.setCarrier(null);
        }
        return new Result(advice, "Road property value is correct.");
    }

}
