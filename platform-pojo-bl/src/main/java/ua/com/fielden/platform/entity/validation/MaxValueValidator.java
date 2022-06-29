package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.types.try_wrapper.TryWrapper.Try;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Money;

/**
 * This validator implements a check for a numerical value to be not greater than the specified limit.
 * <p>
 * There are two mutually exclusive parameters -- {@code limit} and {@code limitPropName}:
 * <ul>
 * <li>Parameter {@code limit} should specify a string representation of the limit value.
 * <li>Parameter {@code limitPropName} should specify a property name of the target entity where the limit value should be read at the time of validation.
 * </ul>
 * If both parameters are specified, {@code limit} takes precedence.
 * <p>
 * @author TG Team
 *
 */
public class MaxValueValidator implements IBeforeChangeEventHandler<Object> {
    public static final String ERR_VALUE_SHOULD_NOT_EXCEED_MAX = "Value should be less or equal to %s.";
    public static final String ERR_LIMIT_VALUE_COULD_NOT_BE_DETERMINED = "Limit value could not be determined.";
    public static final String ERR_VALUE_COULD_NOT_BE_DETERMINED = "Value could not be determined.";

    protected String limit;
    protected String limitPropName;
    protected String customErrorMsg;

    protected MaxValueValidator() { }

    public MaxValueValidator(final Integer limit) {
        this.limit = limit.toString();
    }

    @Override
    public Result handle(final MetaProperty<Object> property, final Object newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) { // no violation
            return successful("Value is null.");
        }

        final AbstractEntity<?> entity = property.getEntity();
        final Optional<BigDecimal> limitValue = determineLimitValue(entity);
        if (!limitValue.isPresent()) {
            return failure(ERR_LIMIT_VALUE_COULD_NOT_BE_DETERMINED);
        }
        // Money new value should be correctly converted :
        final Optional<BigDecimal> numValue = Try(() -> new BigDecimal((newValue instanceof Money) ? ((Money) newValue).getAmount().toString()
                : newValue.toString())).map(v -> of(v)).getOrElse(() -> empty());
        if (!numValue.isPresent()) {
            return failure(ERR_VALUE_COULD_NOT_BE_DETERMINED);
        }

        return numValue.get().compareTo(limitValue.get()) <= 0
                ? successful(entity)
                : failure(entity, format(isEmpty(customErrorMsg) ? ERR_VALUE_SHOULD_NOT_EXCEED_MAX : customErrorMsg, limitValue.get()));
    }
    
    /**
     * A method to determine the limit value from {@code limit} and {@code limitPropName}.
     * 
     * @param entity
     * @return
     */
    private Optional<BigDecimal> determineLimitValue(final AbstractEntity<?> entity) {
        if (limit == null && limitPropName == null) {
            return empty();
        }
        
        if (limit != null) {
            return Try(() -> new BigDecimal(limit)).map(v -> of(v)).getOrElse(() -> empty());
        }
        
        final Object limitPropValue = entity.get(limitPropName);
        if (limitPropValue instanceof Money) {
            return of(((Money) limitPropValue).getAmount());
        } else {
            return Try(() -> new BigDecimal(limitPropValue.toString())).map(v -> of(v)).getOrElse(() -> empty());
        }
    }

}
