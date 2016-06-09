package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics;

import java.util.Date;
import java.util.Optional;

/**
 * A data structure to capture a value for a range kind criterion of any type rather than {@link Date}.
 * Its main, but not restricted to, purpose is to specify default criteria values that could carry both the actual from/to values and meta-values to
 * describe range criteria conditions.
 *
 * @author TG Team
 *
 */
public class RangeCritOtherValueMnemonic<V> {
    public final Optional<V> fromValue;
    public final Optional<V> toValue;

    public final Optional<Boolean> excludeFrom;
    public final Optional<Boolean> excludeTo;

    public final boolean checkForMissingValue;
    public final boolean negateCondition;

    public RangeCritOtherValueMnemonic(
            final Optional<V> fromValue,
            final Optional<V> toValue,
            final Optional<Boolean> excludeFrom,
            final Optional<Boolean> excludeTo,
            final boolean checkForMissingValue,
            final boolean negateCondition
            ) {
        this.fromValue = fromValue;
        this.toValue = toValue;
        this.excludeFrom = excludeFrom;
        this.excludeTo = excludeTo;
        this.checkForMissingValue = checkForMissingValue;
        this.negateCondition = negateCondition;

        // let's perform some validation
        if (!fromValue.isPresent() && !fromValue.isPresent() && !toValue.isPresent() && !toValue.isPresent() && !checkForMissingValue) {
            throw new IllegalArgumentException("Either criteria from/to values or a check for missing values is required.");
        }

        if ((fromValue.isPresent() && fromValue.get() instanceof Date) ||
            (toValue.isPresent() && toValue.get() instanceof Date)) {
            throw new IllegalArgumentException(String.format("Date selection criteria should use %s as a data structure for specifying the conditions.", RangeCritDateValueMnemonic.class.getSimpleName()));
        }

        if (!fromValue.isPresent() && excludeFrom.isPresent()) {
            throw new IllegalArgumentException("ExcludeFrom condition should not be present if fromValue is not provided.");
        }

        if (!toValue.isPresent() && excludeTo.isPresent()) {
            throw new IllegalArgumentException("ExcludeTo condition should not be present if toValue is not provided.");
        }

    }
}
