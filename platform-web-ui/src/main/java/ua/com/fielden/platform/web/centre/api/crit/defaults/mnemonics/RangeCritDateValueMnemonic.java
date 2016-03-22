package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics;

import java.util.Date;
import java.util.Optional;

import ua.com.fielden.snappy.DateRangeConditionEnum;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * A data structure to capture a value for a range kind criterion of type {@link Date}.
 * Its main, but not restricted to, purpose is to specify default criteria values that could carry both the actual from/to values and meta-values to
 * describe the date period condition.
 *
 * @author TG Team
 *
 */
public class RangeCritDateValueMnemonic {
    public final Optional<Date> fromValue;
    public final Optional<Date> toValue;

    public final Optional<DateRangePrefixEnum> prefix;
    public final Optional<MnemonicEnum> period;
    public final Optional<DateRangeConditionEnum> beforeOrAfter;

    public final Optional<Boolean> excludeFrom;
    public final Optional<Boolean> excludeTo;

    public final boolean checkForMissingValue;
    public final boolean negateCondition;

    public RangeCritDateValueMnemonic(
            final Optional<Date> fromValue,
            final Optional<Date> toValue,
            final Optional<DateRangePrefixEnum> prefix,
            final Optional<MnemonicEnum> period,
            final Optional<DateRangeConditionEnum> beforeOrAfter,
            final Optional<Boolean> excludeFrom,
            final Optional<Boolean> excludeTo,
            final boolean checkForMissingValue,
            final boolean negateCondition
            ) {
        this.fromValue = fromValue;
        this.toValue = toValue;
        this.prefix = prefix;
        this.period = period;
        this.beforeOrAfter = beforeOrAfter;
        this.excludeFrom = excludeFrom;
        this.excludeTo = excludeTo;
        this.checkForMissingValue = checkForMissingValue;
        this.negateCondition = negateCondition;

        // let's perform some validation
        if (period.isPresent() && (fromValue.isPresent() || toValue.isPresent())) {
            throw new IllegalArgumentException("Criteria from/to values should not be provided at the same time as period mnemonic.");
        }

        if (!fromValue.isPresent() && !fromValue.isPresent() && !period.isPresent() && !checkForMissingValue) {
            throw new IllegalArgumentException("Either criteria from/to values or a period mnemonic or a check for missing values is required.");
        }

        if (!period.isPresent() && !fromValue.isPresent() && excludeFrom.isPresent()) {
            throw new IllegalArgumentException("ExcludeFrom condition should not be present if neither fromValue nor period mnemonic is provided.");
        }

        if (!period.isPresent() && !toValue.isPresent() && excludeTo.isPresent()) {
            throw new IllegalArgumentException("ExcludeTo condition should not be present if neither toValue nor period mnemonic is provided.");
        }

        if (!period.isPresent() && prefix.isPresent()) {
            throw new IllegalArgumentException(String.format("Prefix %s should not be specified without a period mnemonic.", prefix.get()));
        }

        if (!period.isPresent() && beforeOrAfter.isPresent()) {
            throw new IllegalArgumentException(String.format("Conditional mnemonic %s is only relevant in the context of a period.", beforeOrAfter.get()));
        }
    }
}
