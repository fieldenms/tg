package ua.com.fielden.platform.entity.validation;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Money;

/**
 * This validator implements a check for limit to be greater to the specified limit.
 *
 * @author TG Team
 *
 */
public class GreaterValidator implements IBeforeChangeEventHandler<Object> {
    protected String limit;

    @Override
    public Result handle(final MetaProperty<Object> property, final Object newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return new Result(null, "Value is null and thus not applicable for validation.");
        }
        // Money new value should be correctly converted.
        final String strValue = (newValue instanceof Money) ? ((Money) newValue).getAmount().toString() : newValue.toString();
        final BigDecimal numValue = new BigDecimal(strValue);

        return numValue.compareTo(new BigDecimal(limit)) <= 0 
                ? failure(property.getEntity(), "Value is less than or equal to " + limit + ".")
                : successful(property.getEntity());
    }

}
