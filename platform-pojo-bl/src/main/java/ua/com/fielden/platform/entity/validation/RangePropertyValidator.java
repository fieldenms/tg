package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * This validator implements a check that a value of the property representing upper boundary is greater or equal to the value of the property representing the lower boundary.
 *
 * @author TG Team
 *
 */
public class RangePropertyValidator implements IBeforeChangeEventHandler<Object> {
    private final String[] opositeRangeProperties;
    private final boolean upperBoundaryRangePropery;

    /**
     * Constructs range property validator for a property with <code>opositeRangeProperty</code> representing a property of the opposite side of the range.
     *
     * If <code>upperBoundaryRangePropery</code> is <code>true</code> then <code>opositeRangeProperty</code> indicates the property representing the lower boundary of the range.
     *
     * @param opositeRangeProperty
     * @param upperBoundaryRangePropery
     */
    public RangePropertyValidator(final String[] opositeRangeProperties, final boolean upperBoundaryRangePropery) {
        this.opositeRangeProperties = opositeRangeProperties;
        this.upperBoundaryRangePropery = upperBoundaryRangePropery;
    }

    @Override
    public Result handle(final MetaProperty<Object> property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
        Result result = new Result(null, "Valid");
        final List<Result> successfulResult = new ArrayList<Result>();
        for (final String opositeRangeProperty : opositeRangeProperties) {
            // if the upperBoundaryRangePropery is being validated then need to take into account only those lower property counterparts, which are assigned and
            // perform validation only if the even lower counterparts did not succeed.
            if (!upperBoundaryRangePropery || property.getEntity().getProperty(opositeRangeProperty).getValue() != null || successfulResult.isEmpty()) {
                result = validateProperty(property, newValue, opositeRangeProperty);
                if (upperBoundaryRangePropery && result.isSuccessful()) {
                    successfulResult.add(result);
                }
            }

            if (!upperBoundaryRangePropery && !result.isSuccessful()) {
                return result;
            }
        }
        return result;
    }

    private Result validateProperty(final MetaProperty<?> property, final Object newValue, final String opositeRangeProperty) {
        final MetaProperty startProperty = upperBoundaryRangePropery ? property.getEntity().getProperty(opositeRangeProperty) : property;
        final MetaProperty finishProperty = upperBoundaryRangePropery ? property : property.getEntity().getProperty(opositeRangeProperty);

        final Object lowerBoundaryPropertyValue = upperBoundaryRangePropery ? property.getEntity().get(opositeRangeProperty) : newValue;
        final Object upperBoundaryPropertyValue = upperBoundaryRangePropery ? newValue : property.getEntity().get(opositeRangeProperty);

        final Result valid = new Result(null, "Valid");

        if (lowerBoundaryPropertyValue == null && upperBoundaryPropertyValue == null) {
            return Result.successful("Null is not applicable for validation.");
        } else if (lowerBoundaryPropertyValue == null && upperBoundaryPropertyValue != null) {
            return Result.failure(format("Property [%s] cannot be specified without property [%s]", finishProperty.getTitle(), startProperty.getTitle()));
        } else if (lowerBoundaryPropertyValue != null && upperBoundaryPropertyValue == null) {
            return valid;
        }

        if (property.getType() == Integer.class || property.getType() == int.class) {
            try {
                EntityUtils.validateIntegerRange(Integer.valueOf(lowerBoundaryPropertyValue.toString()), Integer.valueOf(upperBoundaryPropertyValue.toString()), startProperty, finishProperty, upperBoundaryRangePropery);
            } catch (final Result res) {
                return res;
            }
            return valid;
        } else if (property.getType() == Double.class || property.getType() == double.class) {
            try {
                EntityUtils.validateDoubleRange(Double.valueOf(lowerBoundaryPropertyValue.toString()), Double.valueOf(upperBoundaryPropertyValue.toString()), startProperty, finishProperty, upperBoundaryRangePropery);
            } catch (final Result res) {
                return res;
            }
            return valid;
        } else if (Money.class.isAssignableFrom(property.getType())) {
            try {
                EntityUtils.validateMoneyRange((Money) lowerBoundaryPropertyValue, (Money) upperBoundaryPropertyValue, startProperty, finishProperty, upperBoundaryRangePropery);
            } catch (final Result res) {
                return res;
            }
            return valid;
        } else if (Date.class.isAssignableFrom(property.getType())) {
            try {
                EntityUtils.validateDateRange((Date) lowerBoundaryPropertyValue, (Date) upperBoundaryPropertyValue, startProperty, finishProperty, upperBoundaryRangePropery);
            } catch (final Result res) {
                return res;
            }
            return valid;
        } else if (property.getType() == DateTime.class) {
            try {
                EntityUtils.validateDateTimeRange((DateTime) lowerBoundaryPropertyValue, (DateTime) upperBoundaryPropertyValue, startProperty, finishProperty, upperBoundaryRangePropery);
            } catch (final Result res) {
                return res;
            }
            return valid;
        } else {
            return new Result(null, new Exception("Properties " + startProperty.getTitle() + " and " + finishProperty.getTitle() + " cannot form a range due to unsupported type ("
                    + finishProperty.getType().getSimpleName() + ")"));
        }
    }

}
