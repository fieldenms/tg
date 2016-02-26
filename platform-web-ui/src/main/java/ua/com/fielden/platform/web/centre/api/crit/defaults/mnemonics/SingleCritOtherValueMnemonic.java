package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics;

import java.util.Date;
import java.util.Optional;

/**
 * A data structure to capture a value for a single kind criterion of any relevant type, but {@link Date}, which has its own dedicated structure.
 * Its main, but not restricted to, purpose is to specify default criteria values that could
 * carry both the actual value and meta-values.
 *
 * @author TG Team
 *
 */
public class SingleCritOtherValueMnemonic<V> {
    public final Optional<V> value;
    public final boolean checkForMissingValue;
    public final boolean negateCondition;

    public SingleCritOtherValueMnemonic(
            final Optional<V> value,
            final boolean checkForMissingValue,
            final boolean negateCondition) {
        this.value = value;
        this.checkForMissingValue = checkForMissingValue;
        this.negateCondition = negateCondition;

        // let's perform some validation
        if (!value.isPresent() && !checkForMissingValue) {
            throw new IllegalArgumentException("Either criteria value or a check of missing values is required.");
        }

        if (value.isPresent() && value.get() instanceof Date) {
            throw new IllegalArgumentException(String.format("Date selection criteria should use %s as a data structure for specifying the conditions.", SingleCritDateValueMnemonic.class.getSimpleName()));
        }

    }
}
