package ua.com.fielden.platform.swing.egi.models.mappings;

import static java.math.BigDecimal.ZERO;
import static ua.com.fielden.platform.types.Money.zero;

import java.math.BigDecimal;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Interface representing aggregation function that automatically returns {@link String} representation of result. It is performed on list of {@link AbstractEntity} instances, and
 * particular implementations may depend on particular property.
 * 
 * @author yura
 * 
 * @param <T>
 */
@SuppressWarnings("unchecked")
public interface AggregationFunction<T extends AbstractEntity> {

    /**
     * Calculates and returns {@link String} representation of aggregate function invoked on list of {@link AbstractEntity}s.
     * 
     * @param elements
     * @return
     */
    String calc(List<T> elements);

    /**
     * Aggregate function which provides summing of particular properties of {@link AbstractEntity} instances.<br>
     * <br>
     * Note : specified property should be either derivative of {@link Number} or {@link Money} class.
     * 
     * @author yura
     * 
     * @param <T>
     */
    public static class Sum<T extends AbstractEntity> implements AggregationFunction<T> {

        private final String propertyName;

        public Sum(final String propertyName) {
            this.propertyName = propertyName;
        }

        @Override
        public String calc(final List<T> elements) {
            // sum will be calculated either in bdTotal or in mTotal variable
            BigDecimal bdTotal = ZERO;
            Money mTotal = zero;

            boolean moneyType = false;
            for (final T elem : elements) {
                final Object propValue = elem.get(propertyName);
                // passing by null values
                if (propValue != null) {
                    moneyType = propValue instanceof Money;
                    if (moneyType) {
                        mTotal = mTotal.plus((Money) propValue);
                    } else {
                        bdTotal = bdTotal.add(new BigDecimal(((Number) propValue).doubleValue()));
                    }
                }
            }
            final Object result = moneyType ? mTotal : bdTotal;
            return EntityUtils.toString(result, result.getClass());
        }

    }

}
