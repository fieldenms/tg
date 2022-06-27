package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Money;

/**
 * This validator implements a check for limit to be greater or equal to the specified limit. If applicable to properties of type BigDecimal and Money.
 *
 * @author TG Team
 *
 */
public class GreaterOrEqualValidator implements IBeforeChangeEventHandler<Object> {

    public static final String ERR_VALUE_SHOULD_BE_GREATER_THAN_OR_EQUAL_TO = "Value should be greater than or equal to %s.";

    protected String limit;
    protected String customErrorMsg;

    protected GreaterOrEqualValidator() { }

    public GreaterOrEqualValidator(final Integer limit) {
        this.limit = limit.toString();
    }

    @Override
    public Result handle(final MetaProperty<Object> property, final Object newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) { // no violation
            return successful("Value is null and thus not applicable for validation.");
        }
        // Money new value should be correctly converted :
        final String strValue = newValue instanceof Money ? ((Money) newValue).getAmount().toString() : newValue.toString();
        final BigDecimal numValue = new BigDecimal(strValue);

        return numValue.compareTo(new BigDecimal(limit)) >= 0
               ? successful(property.getEntity())
               : failure(property.getEntity(), format(isEmpty(customErrorMsg) ? ERR_VALUE_SHOULD_BE_GREATER_THAN_OR_EQUAL_TO : customErrorMsg, limit));
    }

}
