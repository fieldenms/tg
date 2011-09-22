package ua.com.fielden.platform.entity.validation;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Money;

/**
 * This validator implements a check for a numerical value to be not greater than the specified limit.
 * 
 * @author 01es
 * 
 */
public class MaxValueValidator implements IBeforeChangeEventHandler {
    private final BigDecimal limit;

    public MaxValueValidator(final Integer limit) {
	this.limit = new BigDecimal(limit);
    }

    @Override
    public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	final Object entity = property.getEntity();
	if (newValue == null) { // no violation
	    return new Result(entity, "Value is null.");
	}
	// Money new value should be correctly converted :
	final String strValue = (newValue instanceof Money) ? ((Money) newValue).getAmount().toString() : newValue.toString();
	final BigDecimal value = new BigDecimal(strValue);

	return value.compareTo(limit) > 0 //
	? new Result(entity, new Exception("Value '" + value + "' is greater than the maximum limit of " + limit + ".")) //
		: new Result(entity, "Value '" + value + "' is less than the maximum limit of " + limit + ".");
    }

}
