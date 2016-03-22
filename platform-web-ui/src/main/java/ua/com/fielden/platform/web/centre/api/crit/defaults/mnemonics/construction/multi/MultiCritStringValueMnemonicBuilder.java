package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.multi;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritStringValueMnemonic;

public class MultiCritStringValueMnemonicBuilder implements IMultiCritStringValueMnemonic, IMultiCritStringValueMnemonic1MissingValue {
    private Optional<List<String>> values = Optional.empty();

    private boolean checkForMissingValue = false;
    private boolean negateCondition = false;

    @Override
    public MultiCritStringValueMnemonic value() {
        return new MultiCritStringValueMnemonic(values, checkForMissingValue, negateCondition);
    }

    @Override
    public IMultiCritStringValueMnemonic1MissingValue setValues(final String... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("At least one values is expected.");
        }

        final List<String> list = Arrays.asList(values);
        this.values = Optional.of(list);
        return this;
    }

    @Override
    public IMultiCritStringValueMnemonic2Value canHaveNoValue() {
        this.checkForMissingValue = true;
        return this;
    }

    @Override
    public IMultiCritStringValueMnemonic0Values not() {
        this.negateCondition = true;
        return this;
    }

}
