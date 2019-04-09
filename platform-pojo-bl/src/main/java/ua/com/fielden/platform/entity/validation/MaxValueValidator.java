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
 * This validator implements a check for a numerical value to be not greater than the specified limit.
 *
 * @author TG Team
 *
 */
public class MaxValueValidator implements IBeforeChangeEventHandler<Object> {
    protected String limit;

    protected MaxValueValidator() { }

    public MaxValueValidator(final Integer limit) {
        this.limit = limit.toString();
    }

    @Override
    public Result handle(final MetaProperty<Object> property, final Object newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) { // no violation
            return successful("Value is null.");
        }
        // Money new value should be correctly converted :
        final String strValue = (newValue instanceof Money) ? ((Money) newValue).getAmount().toString() : newValue.toString();
        final BigDecimal numValue = new BigDecimal(strValue);

        return numValue.compareTo(new BigDecimal(limit)) > 0
                ? failure(property.getEntity(), "Value '" + numValue + "' is greater than the maximum limit of " + limit + ".") //
                : successful(property.getEntity());
    }

}
