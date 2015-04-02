package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics;

import java.util.Optional;

/**
 * A data structure to capture a value for a multi-valued kind criterion of type boolean.
 * Its main, but not restricted to, purpose is to specify default criteria values that could carry both the actual values and meta-values to
 * describe the multi-valued criteria conditions.
 *
 * @author TG Team
 *
 */
public class MultiCritBooleanValueMnemonic {
    public final Optional<Boolean> isValue;
    public final Optional<Boolean> isNotValue;

    public final boolean checkForMissingValue;
    public final boolean negateCondition;

    public MultiCritBooleanValueMnemonic(
            final Optional<Boolean> isValue,
            final Optional<Boolean> isNotValue,
            final boolean checkForMissingValue,
            final boolean negateCondition
            ) {
        this.isValue = isValue;
        this.isNotValue = isNotValue;
        this.checkForMissingValue = checkForMissingValue;
        this.negateCondition = negateCondition;

        // let's perform some validation
        if (!isValue.isPresent() && !isNotValue.isPresent() && !checkForMissingValue) {
            throw new IllegalArgumentException("Either boolean criteria is/isNot values or a check for missing values is required.");
        }
    }
}
