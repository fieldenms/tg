package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics;

import java.util.List;
import java.util.Optional;

/**
 * A data structure to capture a value for a multi-valued kind criterion of type string, which covers mutli-valued criteria of an entity type.
 * Its main, but not restricted to, purpose is to specify default criteria values that could carry both the actual values and meta-values to
 * describe the multi-valued criteria conditions.
 *
 * @author TG Team
 *
 */
public class MultiCritStringValueMnemonic {
    public final Optional<List<String>> values;

    public final boolean checkForMissingValue;
    public final boolean negateCondition;

    public MultiCritStringValueMnemonic(
            final Optional<List<String>> values,
            final boolean checkForMissingValue,
            final boolean negateCondition
            ) {
        this.values = values;
        this.checkForMissingValue = checkForMissingValue;
        this.negateCondition = negateCondition;

        // let's perform some validation
        if (!values.isPresent() && !checkForMissingValue) {
            throw new IllegalArgumentException("Either values or a check for missing values is required.");
        }
    }
}
