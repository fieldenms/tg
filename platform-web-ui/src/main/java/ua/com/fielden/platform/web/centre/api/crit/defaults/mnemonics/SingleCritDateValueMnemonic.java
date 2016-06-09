package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics;

import java.util.Date;
import java.util.Optional;

import ua.com.fielden.snappy.DateRangeConditionEnum;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * A data structure to capture a value for a single kind criterion of type {@link Date}.
 * Its main, but not restricted to, purpose is to specify default criteria values that could carry both the actual value and meta-values.
 *
 * @author TG Team
 *
 */
public class SingleCritDateValueMnemonic {
    public final Optional<Date> value;

    public final Optional<DateRangePrefixEnum> prefix;
    public final Optional<MnemonicEnum> period;
    public final Optional<DateRangeConditionEnum> beforeOrAfter;

    public final Optional<Boolean> excludeFrom;
    public final Optional<Boolean> excludeTo;

    public final boolean checkForMissingValue;
    public final boolean negateCondition;

    public SingleCritDateValueMnemonic(
            final Optional<Date> value,
            final Optional<DateRangePrefixEnum> prefix,
            final Optional<MnemonicEnum> period,
            final Optional<DateRangeConditionEnum> beforeOrAfter,
            final Optional<Boolean> excludeFrom,
            final Optional<Boolean> excludeTo,
            final boolean checkForMissingValue,
            final boolean negateCondition
            ) {
        this.value = value;
        this.prefix = prefix;
        this.period = period;
        this.beforeOrAfter = beforeOrAfter;
        this.excludeFrom = excludeFrom;
        this.excludeTo = excludeTo;
        this.checkForMissingValue = checkForMissingValue;
        this.negateCondition = negateCondition;

        // let's perform some validation
        if (value.isPresent() && period.isPresent()) {
            throw new IllegalArgumentException("Criteria value should not be provided at the same time as period mnemonic.");
        }

        if (!value.isPresent() && !period.isPresent() && !checkForMissingValue) {
            throw new IllegalArgumentException("Either criteria value or a period mnemonic or a check of missing values is required.");
        }

        if (!period.isPresent() && prefix.isPresent()) {
            throw new IllegalArgumentException(String.format("Prefix %s should not be specified without a period mnemonic.", prefix.get()));
        }

        if (!period.isPresent() && beforeOrAfter.isPresent()) {
            throw new IllegalArgumentException(String.format("Conditional mnemonic %s is only relevant in the context of a period.", beforeOrAfter.get()));
        }

        if (!period.isPresent() && (excludeFrom.isPresent() || excludeTo.isPresent())) {
            throw new IllegalArgumentException("Exclusion is applicable only of a period mnemonic is used.");
        }

    }
}
